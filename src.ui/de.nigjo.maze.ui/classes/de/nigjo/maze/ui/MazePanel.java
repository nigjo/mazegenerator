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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import static javax.swing.KeyStroke.getKeyStroke;
import javax.swing.UIManager;

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

  private static final String PREFIX = "de.nigjo.maze.ui.MazePanel";
  private static final String PROP_COLOR_BACKGROUND = PREFIX + ".background";
  private static final String PROP_COLOR_CEILING_FROM = PREFIX + ".ceilingFrom";
  private static final String PROP_COLOR_CEILING_TO = PREFIX + ".ceilingTo";
  private static final String PROP_COLOR_WALL = PREFIX + ".wall";
  private static final String PROP_COLOR_FLOOR_FROM = PREFIX + ".floorFrom";
  private static final String PROP_COLOR_FLOOR_TO = PREFIX + ".floorTo";
  private static final String PROP_COLOR_WALL_CROSS = PREFIX + ".wallCross";
  private static final String PROP_COLOR_WALL_BOUND = PREFIX + ".wallBound";
  private static final String PROP_COLOR_DOOR_ENTRANCE = PREFIX + ".doorEntrance";
  private static final String PROP_COLOR_DOOR_EXIT = PREFIX + ".doorExit";

  static
  {
    UIManager.getDefaults().putIfAbsent("de.nigjo.maze.ui.MazePanel.prefWidth", 800);
    UIManager.getDefaults().putIfAbsent("de.nigjo.maze.ui.MazePanel.prefHeight", 800);
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_BACKGROUND, Color.BLACK);
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_CEILING_FROM, Color.ORANGE.darker());
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_CEILING_TO, Color.RED);
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_WALL, new Color(128, 0, 128));
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_WALL_CROSS, Color.GRAY);
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_WALL_BOUND, Color.BLACK);
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_FLOOR_FROM, Color.BLUE);
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_FLOOR_TO, Color.CYAN.darker());
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_DOOR_ENTRANCE, Color.YELLOW.darker());
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_DOOR_EXIT, Color.GREEN.darker());
  }

  public MazePanel()
  {
    super(null);
    super.setPreferredSize(new java.awt.Dimension(
        UIManager.getInt("de.nigjo.maze.ui.MazePanel.prefWidth"),
        UIManager.getInt("de.nigjo.maze.ui.MazePanel.prefHeight")));
    super.setBackground(UIManager.getColor(PROP_COLOR_BACKGROUND));
    super.setFocusable(true);
    super.setDoubleBuffered(true);
  }

  @Override
  public void addNotify()
  {
    super.addNotify(); //To change body of generated methods, choose Tools | Templates.

    InputMap inputMap = getInputMap(WHEN_FOCUSED);
    inputMap.put(getKeyStroke(KeyEvent.VK_UP, 0), "moveForward");
    inputMap.put(getKeyStroke(KeyEvent.VK_LEFT, 0), "rotateLeft");
    inputMap.put(getKeyStroke(KeyEvent.VK_RIGHT, 0), "rotateRight");
    ActionMap actionMap = getActionMap();
    actionMap.put("moveForward", new MoveAction(this::moveForward));
    actionMap.put("rotateLeft", new MoveAction(this::rotateLeft));
    actionMap.put("rotateRight", new MoveAction(this::rotateRight));

    requestFocusInWindow();
  }

  BufferedImage mauer;
  ComponentAdapter resizer;

  private void moveToUpperRight(WalkedHint hint)
  {
    Dimension compsize = hint.getPreferredSize();
    hint.setLocation(getWidth() - compsize.width - 5, 5);
    hint.setSize(compsize);
  }

  public void setMaze(Maze maze)
  {
    this.maze = maze;
    this.current = maze.getEntance();

    if(this.maze != null)
    {
      super.removeAll();
      removeComponentListener(resizer);
      WalkedHint hint = new WalkedHint();
      hint.setMaze(maze);
      moveToUpperRight(hint);
      resizer = new ComponentAdapter()
      {
        @Override
        public void componentResized(ComponentEvent e)
        {
          super.componentResized(e);
          moveToUpperRight(hint);
        }

      };
      addComponentListener(resizer);
      add(hint);
    }

    repaint();
  }

  private void moveForward()
  {
    if(!current.hasWall(this.direction))
    {
      current.setMark(QuadraticMazePainter.MARK_WALKED);
      current = current.getSiblings().get(direction);
      current.setMark(QuadraticMazePainter.MARK_CURRENT);
      //System.out.println(QuadraticMazePainter.toString(maze));
      repaint();
    }
    else if(maze.isExit(current) && direction == 2)
    {
      putClientProperty("MazePanel.solved", true);
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

  private Color getColor(Color from, Color to, int pos, int max)
  {
    double percent = pos / (double)max;
    int red = from.getRed() + (int)((to.getRed() - from.getRed()) * percent);
    int green = from.getGreen() + (int)((to.getGreen() - from.getGreen()) * percent);
    int blue = from.getBlue() + (int)((to.getBlue() - from.getBlue()) * percent);
    return new Color(red, green, blue);
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

  private final int VIEW_WIDTH = 6;
  private final int X = 0;
  private final int Y = 1;
  private final int OL = 0;
  private final int OR = 1;
  private final int UR = 2;
  private final int UL = 3;

  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    Dimension size = getSize();

    List<Cell> ahead = new ArrayList<>();
    ahead.add(current);
    Cell sibling = current;
    while(ahead.size() < VIEW_WIDTH && !sibling.hasWall(this.direction))
    {
      sibling = sibling.getSiblings().get(this.direction);
      ahead.add(0, sibling);
    }

    int quader[] =
    {
      size.width - 1, size.height - 1
    };
    int minx = 0;//(size.width - quader) / 2;
    int miny = 0;//(size.height - quader) / 2;
    int stepcount = ((VIEW_WIDTH + 1) * (VIEW_WIDTH + 2)) / 2;
    double stepWidth = quader[X] / 2. / (stepcount + 1);
    double stepHeight = quader[Y] / 2. / (stepcount + 1);

    List<Cell> view = ahead;
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
      int viewindex = (VIEW_WIDTH - pos + 1);
      int innerCount = ((viewindex * (viewindex + 1)) / 2) + 1;
      int outerCount = (((viewindex + 1) * (viewindex + 2)) / 2) + 1;
      int[] innerDelta =
      {
        quader[X] / 2 - (int)(innerCount * stepWidth),
        quader[Y] / 2 - (int)(innerCount * stepHeight)
      };
      int[] outerDelta =
      {
        quader[X] / 2 - (int)(outerCount * stepWidth),
        quader[Y] / 2 - (int)(outerCount * stepHeight)
      };
      int[] innerWidth =
      {
        quader[X] - (innerDelta[X] * 2),
        quader[Y] - (innerDelta[Y] * 2)
      };
      int[] outerWidth =
      {
        quader[X] - (outerDelta[X] * 2),
        quader[Y] - (outerDelta[Y] * 2)
      };
      int[] wallwidth =
      {
        innerDelta[X] - outerDelta[X],
        innerDelta[Y] - outerDelta[Y]
      };
      boolean isExit = maze.isExit(cell);
      boolean isEntrance = cell == maze.getEntance();

      outer[OL].x = minx + outerDelta[X];
      outer[OL].y = miny + outerDelta[Y];
      outer[OR].x = minx + outerDelta[X] + outerWidth[X];
      outer[OR].y = miny + outerDelta[Y];
      outer[UR].x = minx + outerDelta[X] + outerWidth[X];
      outer[UR].y = miny + outerDelta[Y] + outerWidth[Y];
      outer[UL].x = minx + outerDelta[X];
      outer[UL].y = miny + outerDelta[Y] + outerWidth[Y];
      inner[OL].x = minx + innerDelta[X];
      inner[OL].y = miny + innerDelta[Y];
      inner[OR].x = minx + innerDelta[X] + innerWidth[X];
      inner[OR].y = miny + innerDelta[Y];
      inner[UR].x = minx + innerDelta[X] + innerWidth[X];
      inner[UR].y = miny + innerDelta[Y] + innerWidth[Y];
      inner[UL].x = minx + innerDelta[X];
      inner[UL].y = miny + innerDelta[Y] + innerWidth[Y];

      int id = maze.getCellId(cell);
      int cCol = id % maze.getWidth();
      int cRow = (id - cCol) / maze.getWidth();

      //Decke
      g.setColor(getColor(UIManager.getColor(PROP_COLOR_CEILING_FROM),
          UIManager.getColor(PROP_COLOR_CEILING_TO), cCol, maze.getWidth()));
      g.fillRect(outer[OL].x, outer[OL].y, outerWidth[X], wallwidth[Y]);
      //Boden
      g.setColor(getColor(UIManager.getColor(PROP_COLOR_FLOOR_FROM),
          UIManager.getColor(PROP_COLOR_FLOOR_TO), cRow, maze.getWidth()));
      g.fillRect(outer[UL].x, inner[UL].y, outerWidth[X], wallwidth[Y]);
      if(cell.getMark() == 1)
      {
        g.setColor(g.getColor().darker());
        int dotW = (inner[UR].x - inner[UL].x) / 2;
        int dotH = (outer[UL].y - inner[UL].y) / 2;
        g.fillOval(inner[UL].x + (dotW / 2), inner[UL].y + (dotH / 2), dotW, dotH);
      }
      //Waende
      g.setColor(UIManager.getColor(PROP_COLOR_WALL));
      if(mauer != null)
      {
        ((Graphics2D)g).setPaint(new TexturePaint(mauer,
            new Rectangle(inner[OR].x, inner[OR].y, wallwidth[X], innerWidth[Y])));
      }
//      ((Graphics2D)g).setPaint(new GradientPaint(
//          0f, inner[OL].y, Color.RED, 0f, inner[UL].y, Color.BLUE));
      if(cell.hasWall((direction + 4 - 1) % 4))
      {
        //links
        Polygon polygon = new Polygon(
            new int[]
            {
              outer[OL].x, inner[OL].x, inner[UL].x, outer[UL].x
            },
            new int[]
            {
              outer[OL].y, inner[OL].y, inner[UL].y, outer[UL].y
            },
            4);
        g.fillPolygon(polygon);
        if((isExit && direction == 3) || (isEntrance && direction == 1))
        {
          polygon.xpoints[OL] += (polygon.xpoints[UR] - polygon.xpoints[OL]) * .25;
          polygon.ypoints[OL] += (polygon.ypoints[UL] - polygon.ypoints[OL]) * .33;
          polygon.xpoints[OR] -= (polygon.xpoints[OR] - polygon.xpoints[UL]) * .20;
          polygon.ypoints[OR] += (polygon.ypoints[UR] - polygon.ypoints[OR]) * .33;

          polygon.xpoints[UL] += (polygon.xpoints[UR] - polygon.xpoints[UL]) * .25;
          polygon.ypoints[UL] += (polygon.ypoints[UR] - polygon.ypoints[UL]) * .25;
          polygon.xpoints[UR] -= (polygon.xpoints[UR] - polygon.xpoints[UL]) * .25;
          polygon.ypoints[UR] -= (polygon.ypoints[UR] - polygon.ypoints[UL]) * .25;
          Color old = g.getColor();

          g.setColor(UIManager.getColor(
              isExit ? PROP_COLOR_DOOR_EXIT : PROP_COLOR_DOOR_ENTRANCE));
          g.fillPolygon(polygon);
          g.setColor(old);
        }
      }
      else
      {
        g.fillRect(outer[OL].x, inner[OL].y, wallwidth[X], innerWidth[Y]);
      }
      if(cell.hasWall((direction + 1) % 4))
      {
        //rechts
        Polygon polygon = new Polygon(
            new int[]
            {
              outer[OR].x, outer[UR].x, inner[UR].x, inner[OR].x
            },
            new int[]
            {
              outer[OR].y, outer[UR].y, inner[UR].y, inner[OR].y
            },
            4);
        g.fillPolygon(polygon);
        if((isExit && direction == 1) || (isEntrance && direction == 3))
        {
          polygon.xpoints[3] += (polygon.xpoints[1] - polygon.xpoints[3]) * .25;
          polygon.ypoints[3] += (polygon.ypoints[2] - polygon.ypoints[3]) * .33;
          polygon.xpoints[0] -= (polygon.xpoints[0] - polygon.xpoints[2]) * .20;
          polygon.ypoints[0] += (polygon.ypoints[1] - polygon.ypoints[0]) * .33;

          polygon.xpoints[2] += (polygon.xpoints[1] - polygon.xpoints[2]) * .25;
          polygon.ypoints[2] += (polygon.ypoints[1] - polygon.ypoints[2]) * .25;
          polygon.xpoints[1] -= (polygon.xpoints[1] - polygon.xpoints[2]) * .25;
          polygon.ypoints[1] -= (polygon.ypoints[1] - polygon.ypoints[2]) * .25;
          Color old = g.getColor();
          g.setColor(UIManager.getColor(
              isExit ? PROP_COLOR_DOOR_EXIT : PROP_COLOR_DOOR_ENTRANCE));
          g.fillPolygon(polygon);
          g.setColor(old);
        }
      }
      else
      {
        g.fillRect(inner[OR].x, inner[OR].y, wallwidth[X], innerWidth[Y]);
      }
      ((Graphics2D)g).setPaint(null);
      if(cell.hasWall(direction))
      {
        g.fillRect(inner[OL].x, inner[OL].y, innerWidth[X], innerWidth[Y]);
        if((isExit && direction == 2)
            || (isEntrance && direction == 0))
        {
          Color old = g.getColor();
          g.setColor(UIManager.getColor(
              isExit ? PROP_COLOR_DOOR_EXIT : PROP_COLOR_DOOR_ENTRANCE));
          g.fillRect(minx + innerDelta[X] + 2 * wallwidth[X],
              miny + innerDelta[Y] + 2 * wallwidth[Y],
              innerWidth[X] - (4 * wallwidth[X]),
              innerWidth[Y] - (2 * wallwidth[Y]));
          g.setColor(old);
        }
      }

      g.setColor(UIManager.getColor(PROP_COLOR_WALL_BOUND));
      g.drawLine(outer[OL].x, outer[OL].y, inner[OL].x, inner[OL].y);
      g.drawLine(outer[OR].x, outer[OR].y, inner[OR].x, inner[OR].y);
      g.drawLine(outer[UR].x, outer[UR].y, inner[UR].x, inner[UR].y);
      g.drawLine(outer[UL].x, outer[UL].y, inner[UL].x, inner[UL].y);
      g.drawRect(minx + innerDelta[X], miny + innerDelta[Y], innerWidth[X], innerWidth[Y]);
      if(!cell.hasWall((direction + 4 - 1) % 4))
      {
        g.setColor(UIManager.getColor(PROP_COLOR_WALL_BOUND));
        g.drawRect(outer[OL].x, inner[OL].y, wallwidth[X], innerWidth[Y]);
      }
      else
      {
        g.setColor(UIManager.getColor(PROP_COLOR_WALL_CROSS));
        g.drawLine(outer[OL].x, outer[OL].y, inner[UL].x, inner[UL].y);
        g.drawLine(outer[UL].x, outer[UL].y, inner[OL].x, inner[OL].y);
      }
      if(!cell.hasWall((direction + 1) % 4))
      {
        g.setColor(UIManager.getColor(PROP_COLOR_WALL_BOUND));
        g.drawRect(inner[OR].x, inner[OR].y, wallwidth[X], innerWidth[Y]);
      }
      else
      {
        g.setColor(UIManager.getColor(PROP_COLOR_WALL_CROSS));
        g.drawLine(outer[OR].x, outer[OR].y, inner[UR].x, inner[UR].y);
        g.drawLine(outer[UR].x, outer[UR].y, inner[OR].x, inner[OR].y);
      }
      pos--;
    }
    g.setColor(UIManager.getColor(PROP_COLOR_WALL_BOUND));
    g.drawRect(minx, miny, quader[X], quader[Y]);
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
