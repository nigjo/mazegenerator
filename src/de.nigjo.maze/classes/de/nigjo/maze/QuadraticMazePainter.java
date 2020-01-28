/*
 * Copyright 2020 Jens Hofschröer.
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
package de.nigjo.maze;

import java.util.List;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;

/**
 *
 * @author nigjo
 */
public class QuadraticMazePainter
{
  private static char way = '·';
  private static char deadend = '·';//'◦';
  private static char walked = System.console()==null?'•':'#';//'•';

  public static String toString(Maze maze)
  {
    List<Cell> cells = (List<Cell>)maze.getCells();
    if(cells == null || cells.isEmpty())
    {
      return "┌┐\n└┘";
    }
    StringBuilder b = new StringBuilder("┌");
    int width = maze.getWidth();
    for(int c = 0; c < width; c++)
    {
      b.append(cells.get(c) == maze.getEntance() ? "─S─" : "───");
      b.append(c + 1 < width ? '┬' : '┐');
    }
    int height = maze.getHeight();

    for(int row = 0; row < height; row++)
    {
      b.append("\n│");
      StringBuilder line = new StringBuilder(row + 1 < height ? "├" : "└");
      char middle = row + 1 < height ? '┼' : '┴';
      char right = row + 1 < height ? '┤' : '┘';
      for(int col = 0; col < width; col++)
      {
        Cell c = cells.get(row * width + col);
        char mark = c.getMark() == 1 ? walked : c.getMark() == 2 ? deadend : way;
        b.append(' ').append(mark).append(' ');
        char waypoint = way;
        if(mark == walked && !c.hasWall(1) && c.getSiblings().get(1).getMark() == 1)
        {
          waypoint = walked;
        }
        b.append(c.hasWall(1) ? '│' : waypoint);
        waypoint = way;
        if(mark == walked && !c.hasWall(2) && c.getSiblings().get(2).getMark() == 1)
        {
          waypoint = walked;
        }
        line//.append("-")
            .append(maze.isExit(c) ? "─E─" : (c.hasWall(2) ? "───"
                : (" " + waypoint + " ")));
        line.append(col + 1 < width ? middle : right);
      }
      b.append('\n').append(line);
    }

    return b.toString();
  }

  private QuadraticMazePainter()
  {
  }

}
