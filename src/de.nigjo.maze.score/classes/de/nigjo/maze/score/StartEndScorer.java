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
  private static final int MARKER_START = 100;
  private static final int MARKER_END = 200;

  @Override
  public ScoreInfo getScores(MazeInfo info)
  {
    ScoreInfo score = new ScoreInfo();
    score.mazeInfo = info;

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
          cell.setMark(MARKER_END);
          endCount++;
        }
        else
        {
          cell.setMark(MARKER_START);
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

    score.marker = Map.of(
        MARKER_START, '+', MARKER_END, '-'
    );

    score.scores = Map.of(
        KEY_SCORE, pLength * 6 + pStart * 2,
        "startCount", startCount,
        "endCount", endCount,
        "level", level,
        "path", pLength,
        "start", pStart,
        "end", pEnd);
    return score;
  }

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
