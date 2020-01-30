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

import java.util.ArrayList;
import java.util.List;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import static javax.swing.KeyStroke.getKeyStroke;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;
import de.nigjo.maze.core.QuadraticMazePainter;

/**
 *
 * @author nigjo
 */
public class MazePanel extends JPanel
{
  private int direction = 2;
  private Cell current;
  private Maze maze;

  public MazePanel()
  {
    super(new FlowLayout(FlowLayout.LEADING));
    super.setPreferredSize(new java.awt.Dimension(800, 800));
    super.setBackground(Color.BLACK);
  }

  BufferedImage mauer;

  void setMaze(Maze maze)
  {
    this.maze = maze;
    this.current = maze.getEntance();

    System.out.println(QuadraticMazePainter.toString(maze));

//    try
//    {
//      mauer = ImageIO.read(getClass().getResource("mauer.png"));
//    }
//    catch(IOException ex)
//    {
//      ex.printStackTrace();
//    }
    InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(getKeyStroke(KeyEvent.VK_UP, 0), "moveForward");
    inputMap.put(getKeyStroke(KeyEvent.VK_LEFT, 0), "rotateLeft");
    inputMap.put(getKeyStroke(KeyEvent.VK_RIGHT, 0), "rotateRight");
    ActionMap actionMap = getActionMap();
    actionMap.put("moveForward", new MoveAction(this::moveForward));
    actionMap.put("rotateLeft", new MoveAction(this::rotateLeft));
    actionMap.put("rotateRight", new MoveAction(this::rotateRight));

    repaint();
  }

  private void moveForward()
  {
    if(!current.hasWall(this.direction))
    {
      current.setMark(QuadraticMazePainter.MARK_WALKED);
      current = current.getSiblings().get(direction);
      current.setMark(QuadraticMazePainter.MARK_CURRENT);
      System.out.println(QuadraticMazePainter.toString(maze));
      repaint();
    }
  }

  private void rotateLeft()
  {
    direction = (direction + 4 - 1) % 4;
    repaint();
  }

  private void rotateRight()
  {
    direction = (direction + 1) % 4;
    repaint();
  }

  private static class MoveAction extends AbstractAction
  {
    private final Runnable runner;

    public MoveAction(Runnable runner)
    {
      this.runner = runner;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      runner.run();
    }

  }

  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    Dimension size = getSize();

    g.setColor(Color.BLUE);

//    g.drawLine(0, 0, size.width - 1, size.height - 1);
//    g.drawLine(0, size.height - 1, size.width - 1, 0);
    List<Cell> ahead = new ArrayList<>();
    ahead.add(current);
    Cell sibling = current;
    while(!sibling.hasWall(this.direction))
    {
      sibling = sibling.getSiblings().get(this.direction);
      ahead.add(0, sibling);
    };

    int viewwidth = 6;
    int quader = size.width < size.height ? size.width - 1 : size.height - 1;
    int minx = (size.width - quader) / 2;
    int miny = (size.height - quader) / 2;
    int stepcount = ((viewwidth + 1) * (viewwidth + 2)) / 2;
    double stepwidth = quader / 2. / (stepcount + 1);

    int viewlength = ahead.size() >= viewwidth ? viewwidth : ahead.size();
    List<Cell> view = viewlength == ahead.size() ? ahead : ahead
        .subList(ahead.size() - viewlength, viewlength + 1);
    int pos = view.size();
    Point outer[] = new Point[4];
    Point inner[] = new Point[4];
    for(int i = 0; i < outer.length; i++)
    {
      outer[i] = new Point();
      inner[i] = new Point();
    }
    for(Cell cell : view)
    {
      int viewindex = (viewwidth - pos + 1);
      int innerCount = ((viewindex * (viewindex + 1)) / 2) + 1;
      int outerCount = (((viewindex + 1) * (viewindex + 2)) / 2) + 1;
      int innerDelta = quader / 2 - (int)(innerCount * stepwidth);
      int outerDelta = quader / 2 - (int)(outerCount * stepwidth);
      int innerWidth = quader - (innerDelta * 2);
      int outerWidth = quader - (outerDelta * 2);
      int wallwidth = innerDelta - outerDelta;
      boolean isExit = maze.isExit(cell);
      boolean isEntrance = cell == maze.getEntance();

      outer[0].x = minx + outerDelta;
      outer[0].y = miny + outerDelta;
      outer[1].x = minx + outerDelta + outerWidth;
      outer[1].y = miny + outerDelta;
      outer[2].x = minx + outerDelta + outerWidth;
      outer[2].y = miny + outerDelta + outerWidth;
      outer[3].x = minx + outerDelta;
      outer[3].y = miny + outerDelta + outerWidth;
      inner[0].x = minx + innerDelta;
      inner[0].y = miny + innerDelta;
      inner[1].x = minx + innerDelta + innerWidth;
      inner[1].y = miny + innerDelta;
      inner[2].x = minx + innerDelta + innerWidth;
      inner[2].y = miny + innerDelta + innerWidth;
      inner[3].x = minx + innerDelta;
      inner[3].y = miny + innerDelta + innerWidth;

      //Decke
      g.setColor(Color.RED);
      g.fillRect(outer[0].x, outer[0].y, outerWidth, wallwidth);
      //Boden
      g.setColor(Color.BLUE);
      g.fillRect(outer[3].x, inner[3].y, outerWidth, wallwidth);
      g.setColor(new Color(128, 0, 128));
      if(mauer != null)
      {
        ((Graphics2D)g).setPaint(new TexturePaint(mauer,
            new Rectangle(inner[1].x, inner[1].y, wallwidth, innerWidth)));
      }
//      ((Graphics2D)g).setPaint(new GradientPaint(
//          0f, inner[0].y, Color.RED, 0f, inner[3].y, Color.BLUE));
      if(cell.hasWall((direction + 4 - 1) % 4))
      {
        //links
        Polygon polygon = new Polygon(
            new int[]
            {
              outer[3].x, outer[0].x, inner[0].x, inner[3].x
            },
            new int[]
            {
              outer[3].y, outer[0].y, inner[0].y, inner[3].y
            },
            4);
        g.fillPolygon(polygon);
      }
      else
      {
        g.fillRect(outer[0].x, inner[0].y, wallwidth, innerWidth);
      }
      if(cell.hasWall((direction + 1) % 4))
      {
        //rechts
        Polygon polygon = new Polygon(
            new int[]
            {
              outer[1].x, outer[2].x, inner[2].x, inner[1].x
            },
            new int[]
            {
              outer[1].y, outer[2].y, inner[2].y, inner[1].y
            },
            4);
        g.fillPolygon(polygon);
      }
      else
      {
        g.fillRect(inner[1].x, inner[1].y, wallwidth, innerWidth);
      }
      ((Graphics2D)g).setPaint(null);
      if(cell.hasWall(direction))
      {
        g.fillRect(minx + innerDelta, miny + innerDelta, innerWidth, innerWidth);
        if((isExit && direction == 2)
            || (isEntrance && direction == 0))
        {
          g.setColor(Color.ORANGE.darker());
          g.fillRect(minx + innerDelta + 2 * wallwidth, miny + innerDelta + 2 * wallwidth,
              innerWidth - (4 * wallwidth), innerWidth - (2 * wallwidth));
        }
      }

      g.setColor(Color.BLACK);
      g.drawLine(outer[0].x, outer[0].y, inner[0].x, inner[0].y);
      g.drawLine(outer[1].x, outer[1].y, inner[1].x, inner[1].y);
      g.drawLine(outer[2].x, outer[2].y, inner[2].x, inner[2].y);
      g.drawLine(outer[3].x, outer[3].y, inner[3].x, inner[3].y);
      g.drawRect(minx + innerDelta, miny + innerDelta, innerWidth, innerWidth);
      if(!cell.hasWall((direction + 4 - 1) % 4))
      {
        g.drawRect(outer[0].x, inner[0].y, wallwidth, innerWidth);
      }
      else
      {
        g.drawLine(outer[0].x, outer[0].y, inner[3].x, inner[3].y);
        g.drawLine(outer[3].x, outer[3].y, inner[0].x, inner[0].y);
      }
      if(!cell.hasWall((direction + 1) % 4))
      {
        g.drawRect(inner[1].x, inner[1].y, wallwidth, innerWidth);
      }
      else
      {
        g.drawLine(outer[1].x, outer[1].y, inner[2].x, inner[2].y);
        g.drawLine(outer[2].x, outer[2].y, inner[1].x, inner[1].y);
      }
      pos--;
    }
    g.drawRect(minx, miny, quader, quader);
  }

  private void logCell(Cell cell)
  {
    System.out.print(cell.hasWall(0) ? '-' : 'O');
    System.out.print(cell.hasWall(1) ? '-' : 'R');
    System.out.print(cell.hasWall(2) ? '-' : 'U');
    System.out.print(cell.hasWall(3) ? '-' : 'L');
    System.out.println();
  }

}
