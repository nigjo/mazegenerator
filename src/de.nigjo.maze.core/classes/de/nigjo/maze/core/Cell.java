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
package de.nigjo.maze.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author nigjo
 */
public class Cell
{
  private int mark;
  private final boolean walls[];
  private Cell siblings[];

  public Cell(int wallcount)
  {
    walls = new boolean[wallcount];
    Arrays.fill(walls, true);
    siblings = new Cell[wallcount];
  }

  public boolean hasWall(int index)
  {
    return walls[index];
  }

  public void setSiblings(Cell... siblings)
  {
    if(siblings.length != walls.length)
    {
      throw new IllegalArgumentException("sibling count must match walls count");
    }
    this.siblings = siblings;
  }

  public void addWall(int index)
  {
    this.setWall(index, true);
    this.siblings[index].setWall(this.siblings[index].indexOf(this), true);
  }

  public void removeWall(Cell sibling)
  {
    removeWall(indexOf(sibling));
  }

  public void removeWall(int index)
  {
    this.setWall(index, false);
    this.siblings[index].setWall(this.siblings[index].indexOf(this), false);
  }

  private void setWall(int index, boolean wall)
  {
    this.walls[index] = wall;
  }

  private int indexOf(Cell other)
  {
    for(int i = 0; i < siblings.length; i++)
    {
      if(other == siblings[i])
      {
        return i;
      }
    }
    return -1;
  }

  public List<Cell> getSiblings()
  {
    return Collections.unmodifiableList(Arrays.asList(siblings));
  }

  public void setMark(int markid)
  {
    this.mark = markid;
  }

  public int getMark()
  {
    return mark;
  }

}
