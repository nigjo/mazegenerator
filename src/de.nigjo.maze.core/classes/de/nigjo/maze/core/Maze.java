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
package de.nigjo.maze.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author nigjo
 */
public abstract class Maze
{
  private Collection<Cell> cells;
  private Cell entance;
  private Cell exit;
  private int height;
  private int width;
  private String name;

  public void setCells(Collection<Cell> cells)
  {
    this.cells = cells;
  }

  public Collection<Cell> getCells()
  {
    return Collections.unmodifiableCollection(cells);
  }

  public void setEntance(Cell entance)
  {
    if(!cells.contains(entance))
    {
      throw new IllegalArgumentException("entance not in cells");
    }
    this.entance = entance;
  }

  public Cell getEntrance()
  {
    return getEntance();
  }

  /**
   * @deprecated use {@link #getEntrance()}
   */
  @Deprecated
  public Cell getEntance()
  {
    return entance;
  }

  public void setExit(Cell exit)
  {
    if(!cells.contains(exit))
    {
      throw new IllegalArgumentException("exit not in cells");
    }
    this.exit = exit;
  }

  public boolean isExit(Cell exit)
  {
    return exit == this.exit;
  }

  public int getHeight()
  {
    return height;
  }

  public void setHeight(int height)
  {
    this.height = height;
  }

  public int getWidth()
  {
    return width;
  }

  public void setWidth(int width)
  {
    this.width = width;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    if(name == null)
    {
      return getClass().getSimpleName();
    }
    return name;
  }

  public int getCellId(Cell c)
  {
    if(cells != null && cells.contains(c))
    {
      if(cells instanceof List)
      {
        return ((List)cells).indexOf(c);
      }
      else
      {
        int index = 0;
        for(Cell cell : cells)
        {
          if(cell == c)
          {
            return index;
          }
          ++index;
        }
      }
    }
    return -1;
  }
}
