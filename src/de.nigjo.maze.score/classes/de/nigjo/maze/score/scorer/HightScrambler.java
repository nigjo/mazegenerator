/*
 * Copyright 2020 nigjo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.nigjo.maze.score.scorer;

import java.util.*;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.score.api.MazeInfo;
import de.nigjo.maze.score.api.ScoreInfo;
import de.nigjo.maze.score.api.Scorer;

/**
 * A pseudo scorer which will suggest a "height" information to each cell of the maze.
 * The overall score of each maze is always 0. The "height" is defined by a letter
 * beginning with "A". "B" will be one height information above or below the height of "A".
 * Its up to user to decide if the maze will "raise" or "go down". Upper case letters
 * do show the optimal path, lower case letters are used for "side ways".
 *
 * The default maximum height is 10. This can be changed by the system property
 * "de.nigjo.maze.score.height".
 *
 * @author nigjo
 */
public class HightScrambler implements Scorer
{
  private static final int CONFIG_HEIGHT =
      Integer.getInteger("de.nigjo.maze.score.height", 10);

  @Override
  public ScoreInfo getScores(MazeInfo mazeInfo)
  {
    ScoreInfo info = new ScoreInfo();
    info.mazeInfo = mazeInfo;
    info.scores = Map.of(
        KEY_SCORE, 0,
        "length", mazeInfo.length,
        "height", CONFIG_HEIGHT
    );

    Cell entance = mazeInfo.maze.getEntance();

    List<Cell> fixedHeightCells = new ArrayList<>();
    fixedHeightCells.add(entance);
    //rnd.ints(rnd.nextLong()).count();
    Map<Cell, Integer> heights = new LinkedHashMap<>();
    //Collection<Cell> cells = mazeInfo.maze.getCells();
    Queue<Cell> queue = new ArrayDeque<>();
    queue.add(entance);

    Random rnd = new Random(mazeInfo.seed);
    while(!queue.isEmpty())
    {
      Cell next = queue.poll();
      if(next == null || heights.containsKey(next))
      {
        continue;
      }
      if(mazeInfo.maze.isExit(next))
      {
        fixedHeightCells.add(next);
        heights.put(next, 0);
      }
      else
      {
        heights.put(next, rnd.nextInt(CONFIG_HEIGHT));
      }
      for(Cell sibling : next.getSiblings())
      {
        if(sibling != null)
        {
          queue.add(sibling);
        }
      }
    }

    heights.put(entance, 0);

    levelOutHeights(entance, heights, fixedHeightCells, rnd);

    Map<Integer, Character> chars = new HashMap<>();
    for(Map.Entry<Cell, Integer> entry : heights.entrySet())
    {
      Cell cell = entry.getKey();
      int height = entry.getValue();
      if(cell.getMark() == Cell.MARK_WALKED)
      {
        cell.setMark(200 + height);
        chars.put(200 + height, (char)('A' + height));
      }
      else
      {
        cell.setMark(100 + height);
        chars.put(100 + height, (char)('a' + height));
      }
    }
    info.marker = chars;

    return info;
  }

  @Override
  public String getName()
  {
    return "height";
  }

  private void levelOutHeights(Cell entrance, Map<Cell, Integer> heights,
      List<Cell> fixedHeightCells, Random rnd)
  {
    boolean levelChanged;
    do
    {
      levelChanged = false;
      Set<Cell> done = new HashSet<>();
      List<Cell> todo = new ArrayList<>();
      todo.add(entrance);
      while(!todo.isEmpty())
      {
        Cell cell = todo.remove(0);
        done.add(cell);
        List<Cell> siblings = cell.getSiblings();
        for(Cell sibling : siblings)
        {
          if(done.contains(sibling)
              || cell.hasWall(siblings.indexOf(sibling)))
          {
            continue;
          }
          todo.add(sibling);
          int h1 = heights.get(cell);
          int h2 = heights.get(sibling);
          int delta = h2 - h1;
          if(Math.abs(delta) > 1)
          {
            levelChanged = true;
            if(fixedHeightCells.contains(cell))
            {
              levelOutCells(false, heights, cell, sibling);
            }
            else if(fixedHeightCells.contains(sibling))
            {
              if(delta < 0)
              {
                heights.put(cell, heights.get(sibling) + 1);
              }
              else
              {
                heights.put(cell, heights.get(sibling) - 1);
              }
              fixedHeightCells.add(cell);
            }
            else
            {
              boolean levelOutFirstCell = rnd.nextBoolean();
              levelOutCells(levelOutFirstCell, heights, cell, sibling);
            }
          }
        }
      }
      //log(heights.values());
    }
    while(levelChanged);

    validateHeights(entrance, heights);
  }

  private void validateHeights(Cell entrance, Map<Cell, Integer> heights)
  {
    Set<Cell> done = new HashSet<>();
    Queue<Cell> queue = new ArrayDeque<>();
    queue.add(entrance);
    while(!queue.isEmpty())
    {
      Cell cell = queue.poll();

      int cellHeight = heights.get(cell);

      List<Cell> siblings = cell.getSiblings();
      for(Cell sibling : siblings)
      {
        if(sibling != null && !done.contains(sibling)
            && !cell.hasWall(siblings.indexOf(sibling)))
        {
          int sibHeight = heights.get(sibling);
          if(Math.abs(sibHeight - cellHeight) > 1)
          {
            throw new IllegalStateException("height difference to high");
          }
        }
      }
      done.add(cell);
    }
  }

  private static void log(Collection<Integer> heights)
  {
    StringBuilder b = new StringBuilder();
    for(Integer height : heights)
    {
      b.append(',').append(height);
    }
    b.append('=').append(b.toString().hashCode());
    System.out.println(b);
  }

  private void levelOutCells(boolean levelOutFirstCell,
      Map<Cell, Integer> heights, Cell cell, Cell sibling)
  {
    int h1 = heights.get(cell);
    int h2 = heights.get(sibling);
    int delta = h2 - h1;
    if(levelOutFirstCell)
    {
      h1 += delta < 0 ? -1 : 1;
      heights.put(cell, h1);
    }
    else
    {
      h2 += delta > 0 ? -1 : 1;
      heights.put(sibling, h2);
    }
  }

}
