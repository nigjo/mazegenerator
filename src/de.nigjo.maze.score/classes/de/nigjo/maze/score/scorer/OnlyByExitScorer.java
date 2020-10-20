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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.score.api.MazeInfo;
import de.nigjo.maze.score.api.ScoreInfo;
import de.nigjo.maze.score.api.Scorer;

/**
 * A scorer "exit" which will test what ways are "beyond" the exit. These ways are defined
 * by that cells which are only reachable from the start by passing by the exit. The more
 * cells are "exit-cells" the lower the score. Longer "optimal ways" will result in a
 * higher score.
 *
 * <pre>score = (&lt;non-exit-cell-count&gt; * 0.1) * &lt;maze-length&gt;</pre>
 *
 * @author nigjo
 */
public class OnlyByExitScorer implements Scorer
{
  @Override
  public String getName()
  {
    return "exit";
  }

  @Override
  public ScoreInfo getScores(MazeInfo mazeInfo)
  {
    ScoreInfo score = new ScoreInfo();
    score.mazeInfo = mazeInfo;
    score.marker = Map.of(100, '*');

    Collection<Cell> cells = mazeInfo.maze.getCells();
    for(Cell cell : cells)
    {
      if(mazeInfo.maze.isExit(cell))
      {
        int cellCount = countExitCells(cell);
        List<Cell> siblings = cell.getSiblings();
        for(Cell sibling : siblings)
        {
          if(sibling != null && sibling.getMark() == Cell.MARK_WALKED)
          {
            cellCount += countExitCells(sibling);
          }
        }

        score.scores = Map.of(
            KEY_SCORE, (cells.size() - cellCount) * .1 * mazeInfo.length,
            "length", mazeInfo.length,
            "endcount", cellCount
        );
      }
    }

    return score;
  }

  private int countExitCells(Cell cell)
  {
    int count = 0;
    List<Cell> siblings = cell.getSiblings();
    for(Cell sibling : siblings)
    {
      if(sibling == null
          || !(sibling.getMark() == Cell.MARK_UNKNOWN)
          || cell.hasWall(siblings.indexOf(sibling)))
      {
        continue;
      }
      sibling.setMark(100);
      count++;
      count += countExitCells(sibling);
    }
    return count;
  }

}
