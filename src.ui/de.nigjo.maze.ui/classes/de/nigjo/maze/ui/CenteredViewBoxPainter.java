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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import javax.swing.UIManager;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;

/**
 *
 * @author nigjo
 */
public class CenteredViewBoxPainter implements MazePainter
{
  @Override
  public void paintMaze(Maze maze, Cell current, int direction, Dimension size, Graphics g)
  {
    //Dimension size = context.getSize();
    int boxwidth = (int)(size.width);
    int boxheight = (int)(size.height);

    Color oben = UIManager.getColor(MazePainter.PROP_COLOR_CEILING_FROM);
    Color unten = UIManager.getColor(MazePainter.PROP_COLOR_FLOOR_FROM);
    Color wand = UIManager.getColor(MazePainter.PROP_COLOR_WALL);
    Color line = UIManager.getColor(MazePainter.PROP_COLOR_WALL_BOUND);
    Color door = UIManager.getColor(MazePainter.PROP_COLOR_DOOR_EXIT);

    g.setColor(line);
    g.fillRect(0, 0, size.width, size.height);

    Graphics2D g2 = (Graphics2D)g;
    g2.translate((int)(size.width * .5), (int)(size.height * .5));

    for(Cell cell : getViewAhead(current, direction, 5))
    {
      int stepwidth = (int)(boxwidth * .2);
      int stepheight = (int)(boxheight * .2);

      Polygon plinks = new Polygon();
      plinks.addPoint(-boxwidth / 2, -boxheight / 2);
      plinks.addPoint(-boxwidth / 2 + stepwidth, -boxheight / 2 + stepheight);
      plinks.addPoint(-boxwidth / 2 + stepwidth, boxheight / 2 - stepheight);
      plinks.addPoint(-boxwidth / 2, boxheight / 2);

      Polygon prechts = new Polygon();
      prechts.addPoint(boxwidth / 2, -boxheight / 2);
      prechts.addPoint(boxwidth / 2 - stepwidth, -boxheight / 2 + stepheight);
      prechts.addPoint(boxwidth / 2 - stepwidth, boxheight / 2 - stepheight);
      prechts.addPoint(boxwidth / 2, boxheight / 2);

      g2.setColor(oben);
      g.fillRect(-boxwidth / 2, -boxheight / 2, boxwidth, stepheight);
      oben = g.getColor().darker();

      g2.setColor(unten);
      g.fillRect(-boxwidth / 2, boxheight / 2 - stepheight, boxwidth, stepheight);

      if(cell.getMark() == Cell.MARK_WALKED)
      {
        Polygon floor = new Polygon();
        int insetY = (int)(stepheight * .2);
        int insetXn = (int)(boxwidth * .15);
        int insetXd = (int)((boxwidth - 2 * stepwidth) * .1);
        //"near" line
        floor.addPoint(-boxwidth / 2 + insetXn, boxheight / 2 - insetY - 1);
        floor.addPoint(boxwidth / 2 - insetXn, boxheight / 2 - insetY - 1);
        //"distant" line
        floor.addPoint(boxwidth / 2 - stepwidth - insetXd,
            boxheight / 2 - stepheight + insetY - 1);
        floor.addPoint(-boxwidth / 2 + stepwidth + insetXd,
            boxheight / 2 - stepheight + insetY - 1);
        //g.setColor(unten.darker());
        g.setColor(unten.brighter());
        g.fillPolygon(floor);
        g.setColor(unten);
      }
      unten = g.getColor().darker();

      g.setColor(wand);
      if(!cell.hasWall(direction - 1))
      {
        Color oldCol = g.getColor();
        g.fillRect(-boxwidth / 2, -boxheight / 2 + stepheight,
            stepwidth, boxheight - 2 * stepheight);
        g.setColor(line);
        g.drawRect(-boxwidth / 2, -boxheight / 2 + stepheight,
            stepwidth, boxheight - 2 * stepheight);
        g.setColor(oldCol);
      }
      else
      {
        g.fillPolygon(plinks);
        if(MazePainter.isDoor(maze, cell, direction - 1))
        {
          Polygon lDoor = new Polygon();
          lDoor.addPoint(-boxwidth / 2 + (int)(stepwidth * .2),
              -boxheight / 2 + (int)(stepheight * .2));
          lDoor.addPoint(-boxwidth / 2 + (int)(stepwidth * .8),
              -boxheight / 2 + (int)(stepheight * .8));
          lDoor.addPoint(-boxwidth / 2 + (int)(stepwidth * .8),
              boxheight / 2 - (int)(stepheight * .8));
          lDoor.addPoint(-boxwidth / 2 + (int)(stepwidth * .2),
              boxheight / 2 - (int)(stepheight * .2));
          lDoor.ypoints[0] *= .5;
          lDoor.ypoints[1] *= .5;

          Color oldC = g.getColor();
          g.setColor(door);
          g.fillPolygon(lDoor);
          g.setColor(oldC);
        }
      }

      if(!cell.hasWall(direction + 1))
      {
        Color oldCol = g.getColor();
        g.fillRect(boxwidth / 2 - stepwidth - 1, -boxheight / 2 + stepheight,
            stepwidth, boxheight - 2 * stepheight);
        g.setColor(line);
        g.drawRect(boxwidth / 2 - stepwidth - 1, -boxheight / 2 + stepheight,
            stepwidth, boxheight - 2 * stepheight);
        g.setColor(oldCol);
      }
      else
      {
        g.fillPolygon(prechts);
        if(MazePainter.isDoor(maze, cell, direction + 1))
        {
          Polygon rDoor = new Polygon();
          rDoor.addPoint(boxwidth / 2 - (int)(stepwidth * .2),
              -boxheight / 2 + (int)(stepheight * .2));
          rDoor.addPoint(boxwidth / 2 - (int)(stepwidth * .8),
              -boxheight / 2 + (int)(stepheight * .8));
          rDoor.addPoint(boxwidth / 2 - (int)(stepwidth * .8),
              boxheight / 2 - (int)(stepheight * .8));
          rDoor.addPoint(boxwidth / 2 - (int)(stepwidth * .2),
              boxheight / 2 - (int)(stepheight * .2));
          rDoor.ypoints[0] *= .5;
          rDoor.ypoints[1] *= .5;

          Color oldC = g.getColor();
          g.setColor(door);
          g.fillPolygon(rDoor);
          g.setColor(oldC);
        }
      }

      if(cell.hasWall(direction))
      {
        Rectangle wall = new Rectangle(
            stepwidth - boxwidth / 2, stepheight - boxheight / 2,
            boxwidth - 2 * stepwidth, boxheight - 2 * stepheight);
        g.fillRect(wall.x, wall.y, wall.width, wall.height);
        if(MazePainter.isDoor(maze, cell, direction))
        {
          Color oldC = g.getColor();
          g.setColor(door);
          g.fillRect(wall.x / 2, wall.y / 2, wall.width / 2, (int)(wall.height * .75));
          g.setColor(oldC);
        }
      }

      wand = g.getColor().darker();
      door = door.darker();

//      g.setClip(clip);
      g.setColor(line);
      g.drawRect(-boxwidth / 2, -boxheight / 2, boxwidth - 1, boxheight - 1);

      boxwidth -= stepwidth * 2;
      boxheight -= stepheight * 2;
    }
    g.drawLine(-size.width / 2, -size.height / 2, -boxwidth / 2, -boxheight / 2);
    g.drawLine(size.width / 2, -size.height / 2, boxwidth / 2, -boxheight / 2);
    g.drawLine(-size.width / 2, size.height / 2, -boxwidth / 2, boxheight / 2);
    g.drawLine(size.width / 2, size.height / 2, boxwidth / 2, boxheight / 2);
    g.drawRect(-boxwidth / 2, -boxheight / 2, boxwidth - 1, boxheight - 1);
  }

}
