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

import java.util.List;
import java.util.Map;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.QuadraticMazePainter;
import de.nigjo.maze.score.api.MazeInfo;
import de.nigjo.maze.score.api.ScoreInfo;
import de.nigjo.maze.score.api.Scorer;

/**
 * A scorer to count the numbers of junctions and side corridors of the main path.
 * Every junction will count 1.5 points, every side corridor 0.75 points. These points
 * are multiplied by the double length of the main path.
 *
 * @author nigjo
 */
public class JunctionCounter implements Scorer
{
  @Override
  public ScoreInfo getScores(MazeInfo mazeInfo)
  {
    ScoreInfo score = new ScoreInfo();
    score.mazeInfo = mazeInfo;

    int abzweige = 0;
    int abgaenge = 0;
    int lastdir = QuadraticMazePainter.DIR_BOTTOM;
    Cell last = null;
    Cell current = mazeInfo.maze.getEntance();
    while(current != null && !mazeInfo.maze.isExit(current))
    {
      Cell next = null;
      List<Cell> siblings = current.getSiblings();

      for(Cell sibling : siblings)
      {
        if(sibling == last || current.hasWall(siblings.indexOf(sibling)))
        {
          continue;
        }

        if(sibling.getMark() == Cell.MARK_WALKED)
        {
          next = sibling;
        }
        else
        {
          int dir = siblings.indexOf(sibling);
          if(dir == lastdir)
          {
            abzweige++;
          }
          else
          {
            abgaenge++;
          }
        }
      }

      last = current;
      lastdir = siblings.indexOf(next);
      current = next;
    }

    score.scores = Map.of(
        KEY_SCORE, ((abgaenge + 1) * .75 + (abzweige) * 1.5)
        * (2 * score.mazeInfo.length),
        "abzweige", abzweige,
        "abgaenge", abgaenge,
        "laenge", score.mazeInfo.length
    );

    return score;
  }

}
