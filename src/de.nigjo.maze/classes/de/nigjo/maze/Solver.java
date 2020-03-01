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

import java.util.ArrayList;
import java.util.List;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;

/**
 *
 * @author nigjo
 */
class Solver
{
  static int MARK_NEVER = Cell.MARK_UNKNOWN;
  static int MARK_WALK = Cell.MARK_WALKED;
  static int MARK_DEADEND = 903;

  static int solve(Maze generated)
  {
    Cell entance = generated.getEntance();
    List<Cell> checked = new ArrayList<>();
    List<Cell> deadends = new ArrayList<>();
    checked.add(entance);
    while(!checked.isEmpty())
    {
      Cell current = checked.remove(0);
      current.setMark(MARK_WALK);
      if(generated.isExit(current))
      {
        while(!checked.isEmpty())
        {
          Cell unchecked = checked.remove(0);
          unchecked.setMark(MARK_DEADEND);
          deadends.add(unchecked);
        }
        break;
      }
      List<Cell> siblings = current.getSiblings();
      boolean added = false;
      for(int i = 0; i < siblings.size(); i++)
      {
        if(!current.hasWall(i))
        {
          Cell sibling = siblings.get(i);
          if(sibling.getMark() == 0)
          {
            added = true;
            checked.add(sibling);
          }
        }
      }
      if(!added)
      {
        current.setMark(MARK_DEADEND);
        deadends.add(current);
      }
    }
    // Alle Umwege wieder entfernen
    while(!deadends.isEmpty())
    {
//      System.out.println(QuadraticMazePainter.toString(generated));
      Cell current = deadends.remove(0);
      List<Cell> siblings = current.getSiblings();
      Cell walked = null;
      for(int i = 0; i < siblings.size(); i++)
      {
        if(!current.hasWall(i))
        {
          if(siblings.get(i).getMark() == MARK_WALK)
          {
            if(walked == null)
            {
              walked = siblings.get(i);
            }
            else
            {
              walked = null;
              break;
            }
          }
        }
      }
      if(walked != null && !generated.isExit(current)
          && current != generated.getEntance())
      {
        current.setMark(MARK_DEADEND);
        deadends.add(walked);
      }
    }
    int counter = 0;
    for(Cell cell : generated.getCells())
    {
      if(cell.getMark() == MARK_WALK)
      {
        counter++;
      }
    }
    return counter;
  }

}
