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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;

/**
 *
 * @author nigjo
 */
public class StartEndScorer implements Scorer
{
  @Override
  public Map<String, Number> getScores(MazeInfo info)
  {
    Map<Cell, Integer> startDistance = new HashMap<>();
    Map<Cell, Integer> endDistance = new HashMap<>();

    Cell exit = findDistance(info.maze, info.maze.getEntance(), startDistance);
    findDistance(info.maze, exit, endDistance);

    int startCount = 0;
    int endCount = 0;
    //TODO: Markierungen setzen
    for(Cell cell : info.maze.getCells())
    {
      int start = startDistance.get(cell);
      int ende = endDistance.get(cell);
      if(cell.getMark() != Cell.MARK_WALKED)
      {
        if(start > ende)
        {
          cell.setMark(200);
          endCount++;
        }
        else
        {
          cell.setMark(100);
          startCount++;
        }
      }
    }

    double sum = (info.length + startCount + endCount) / 100.;

    int level;
    if(endCount / sum > 66 || info.length < (info.maze.getHeight() * 1.5))
    {
      level = 1;
    }
    else if(endCount > startCount)
    {
      level = 2;
    }
    else if(info.length / sum > 30)
    {
      level = 6;
    }
    else if(info.length / sum > 25)
    {
      level = 5;
    }
    else if(startCount > endCount)
    {
      level = 3;
    }
    else
    {
      level = 4;
    }
    float pLength = info.length / (float)sum;
    float pStart = startCount / (float)sum;
    float pEnd = endCount / (float)sum;

    return Map.of(
        "score", pLength * 6 + pStart * 2,
//        "start", (float)startCount,
//        "end", (float)endCount,
        "level", level,
        "path", pLength,
        "start", pStart,
        "end", pEnd);
  }

//  {
//    String levelView =
//        QuadraticMazePainter.toString(info.maze, '·', Map.of(
//            100, '+', 200, '-'
//        ));
//    System.out.println(levelView);
//    System.out.println(info.hash);
//    System.out.println(String.format("id-%03d", mazes.indexOf(info) + 1)
//        + ", path: " + String.format("%4.1f%%", info.length / sum)
//        + ", start: " + String.format("%4.1f%%", info.startCount / sum)
//        + ", end: " + String.format("%4.1f%%", info.endCount / sum)
//        + ", lvl?: " + String.format("%2d", info.level)
//    );
//  }
  private static Cell findDistance(Maze maze, Cell entance,
      Map<Cell, Integer> distances)
  {
    Cell exitCell = null;
    List<Cell> stack = new ArrayList<>();
    stack.add(entance);
    distances.put(entance, 0);

    while(!stack.isEmpty())
    {
      Cell current = stack.remove(0);
      if(exitCell == null && maze.isExit(current))
      {
        exitCell = current;
      }
      Integer distance = distances.get(current);
      List<Cell> siblings = current.getSiblings();
      for(int sIdx = 0; sIdx < siblings.size(); sIdx++)
      {
        //Cell get = current.getSiblings().get(sIdx);
        if(!current.hasWall(sIdx))
        {
          Integer sibDist = distances.putIfAbsent(siblings.get(sIdx), distance + 1);
          if(sibDist == null)
          {
            stack.add(siblings.get(sIdx));
          }
        }
      }
    }

    return exitCell;
  }

}
