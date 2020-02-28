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
package de.nigjo.maze.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;

/**
 *
 * @author nigjo
 */
public class SimpleGridPainter implements MazePainter
{
  public SimpleGridPainter()
  {
  }
  Map<String, Color> defaultColors = new HashMap<>();

  @Override
  public void init()
  {
    UIDefaults defaults = UIManager.getDefaults();
    defaultColors.put(PROP_COLOR_BACKGROUND,
        (Color)defaults.put(PROP_COLOR_BACKGROUND, Color.WHITE));
    defaultColors.put(PROP_COLOR_WALL_BOUND,
        (Color)defaults.put(PROP_COLOR_WALL_BOUND, Color.BLACK));
    defaultColors.put(WalkedHint.PROP_COLOR_WALL,
        (Color)defaults.put(WalkedHint.PROP_COLOR_WALL, Color.CYAN));
  }

  @Override
  public void reset()
  {
    defaultColors.forEach((k, c) -> UIManager.getDefaults().put(k, c));
  }

  @Override
  public void paintMaze(Maze maze, Cell current, int direction,
      Dimension size, Graphics g)
  {
    g.setColor(UIManager.getColor(PROP_COLOR_WALL_BOUND));

    g.drawRect(0, 0, size.width - 1, size.height - 1);

    List<Cell> viewAhead = getViewAhead(current, direction, 5);
    Rectangle out = new Rectangle(size.width - 1, size.height - 1);
    for(Cell cell : viewAhead)
    {
      Rectangle in = out.getBounds();
      in.grow((int)(out.width * -.15), (int)(out.height * -.15));
      g.drawRect(in.x, in.y, in.width, in.height);

      if(!cell.hasWall(direction - 1))
      {
        g.drawRect(out.x, in.y, in.x - out.x, in.height);
      }
      else if(MazePainter.isDoor(maze, cell, direction - 1))
      {
        g.drawOval(out.x + (int)((in.x - out.x) * .1), in.y,
            in.x - out.x - (int)((in.x - out.x) * .2), in.height);
      }
      if(!cell.hasWall(direction + 1))
      {
        g.drawRect(in.x + in.width, in.y, in.x - out.x, in.height);
      }
      else if(MazePainter.isDoor(maze, cell, direction + 1))
      {
        g.drawOval(in.x + in.width + (int)((in.x - out.x) * .1), in.y,
            in.x - out.x - (int)((in.x - out.x) * .2), in.height);
      }

      if(MazePainter.isDoor(maze, cell, direction))
      {
        g.drawOval(in.x + (int)((in.x - out.x) * .25),
            in.y + (int)((out.height - in.height) * .25),
            in.width - (int)((in.x - out.x) * .5),
            in.height - (int)((out.height - in.height) * .5));
      }

      out = in;
    }
    g.drawLine(0, 0, out.x, out.y);
    g.drawLine(size.width, 0, out.x + out.width, out.y);
    g.drawLine(0, size.height, out.x, out.y + out.height);
    g.drawLine(size.width, size.height, out.x + out.width, out.y + out.height);
  }

}
