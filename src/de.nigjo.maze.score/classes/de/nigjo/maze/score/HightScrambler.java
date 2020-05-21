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
package de.nigjo.maze.score;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import de.nigjo.maze.core.Cell;

/**
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
        KEY_SCORE, mazeInfo.length,
        "length", mazeInfo.length,
        "height", CONFIG_HEIGHT
    );

    String propHeight = System.getProperty("de.nigjo.maze.score.height");

    Cell entance = mazeInfo.maze.getEntance();

    Cell exit = null;
    List<Cell> fixedHeightCells = new ArrayList<>();
    fixedHeightCells.add(entance);
    //rnd.ints(rnd.nextLong()).count();
    Map<Cell, Integer> heights = new HashMap<>();
    //Collection<Cell> cells = mazeInfo.maze.getCells();
    Queue<Cell> queue = new ArrayDeque();
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
        exit = next;
        fixedHeightCells.add(exit);
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

    levelOutHeights(heights, fixedHeightCells, rnd);

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

  private void levelOutHeights(Map<Cell, Integer> heights, List<Cell> fixedHeightCells,
      Random rnd)
  {
    boolean levelChanged;
    do
    {
      levelChanged = false;
      for(Cell cell : heights.keySet())
      {
        List<Cell> siblings = cell.getSiblings();
        for(Cell sibling : siblings)
        {
          if(sibling == null
              || cell.hasWall(siblings.indexOf(sibling)))
          {
            continue;
          }
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
              levelOutCells(true, heights, cell, sibling);
            }
            else
            {
              boolean levelOutFirstCell = rnd.nextBoolean();
              levelOutCells(levelOutFirstCell, heights, cell, sibling);
            }
          }
        }
      }
    }
    while(levelChanged);
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
