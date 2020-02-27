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
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import static javax.swing.KeyStroke.getKeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;
import de.nigjo.maze.core.QuadraticMazePainter;
import static de.nigjo.maze.ui.MazePainter.PROP_COLOR_BACKGROUND;

/**
 * A Panel to paint and walk a Maze.
 *
 * @author nigjo
 */
public class MazePanel extends JPanel
{
  public static final String PROP_DIRECTION = MazePanel.class.getName() + ".direction";
  public static final String PROP_CURRENT_CELL =
      MazePanel.class.getName() + ".current_cell";
  public static final String PROP_SOLVED = MazePanel.class.getName() + ".solved";

  public static final int DIR_COUNT = 4;
  public static final int DIR_NORTH = 0;
  public static final int DIR_EAST = 1;
  public static final int DIR_SOUTH = 2;
  public static final int DIR_WEST = 3;

  private MazePainter painter;

  private int direction;
  private Cell current;
  private Maze maze;

  private static final String PREFIX = "de.nigjo.maze.ui.MazePanel";
  private static final String PROP_PREFERRED_WIDTH = PREFIX + ".prefWidth";
  private static final String PROP_PREFERRED_HEIGHT = PREFIX + ".prefHeight";

  static
  {
    UIDefaults uidata = UIManager.getDefaults();
    uidata.putIfAbsent(PROP_PREFERRED_WIDTH, 800);
    uidata.putIfAbsent(PROP_PREFERRED_HEIGHT, 600);
    uidata.putIfAbsent(MazePainter.PROP_COLOR_BACKGROUND, Color.BLACK);
    uidata.putIfAbsent(MazePainter.PROP_COLOR_CEILING_FROM, Color.ORANGE.darker());
    uidata.putIfAbsent(MazePainter.PROP_COLOR_CEILING_TO, Color.RED);
    uidata.putIfAbsent(MazePainter.PROP_COLOR_WALL, new Color(128, 0, 128));
    uidata.putIfAbsent(MazePainter.PROP_COLOR_WALL_CROSS, Color.GRAY);
    uidata.putIfAbsent(MazePainter.PROP_COLOR_WALL_BOUND, Color.BLACK);
    uidata.putIfAbsent(MazePainter.PROP_COLOR_FLOOR_FROM, Color.BLUE);
    uidata.putIfAbsent(MazePainter.PROP_COLOR_FLOOR_TO, Color.CYAN.darker());
    uidata.putIfAbsent(MazePainter.PROP_COLOR_DOOR_ENTRANCE, Color.YELLOW.darker());
    uidata.putIfAbsent(MazePainter.PROP_COLOR_DOOR_EXIT, Color.GREEN.darker());
  }

  public MazePanel()
  {
    super(null);
    super.setPreferredSize(new java.awt.Dimension(
        UIManager.getInt(PROP_PREFERRED_WIDTH),
        UIManager.getInt(PROP_PREFERRED_HEIGHT)));
    super.setBackground(UIManager.getColor(PROP_COLOR_BACKGROUND));
    UIManager.getDefaults().addPropertyChangeListener(pce ->
    {
      if(PROP_COLOR_BACKGROUND.equals(pce.getPropertyName()))
      {
        super.setBackground((Color)pce.getNewValue());
      }
    });
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
    putClientProperty(PROP_CURRENT_CELL, current);
    this.direction = DIR_SOUTH;
    putClientProperty(PROP_DIRECTION, direction);

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
      putClientProperty(PROP_CURRENT_CELL, current);

      //System.out.println(QuadraticMazePainter.toString(maze));
      repaint();
    }
    else if(maze.isExit(current) && direction == DIR_SOUTH)
    {
      putClientProperty(PROP_SOLVED, true);
    }
  }

  private void rotateLeft()
  {
    direction = (direction + DIR_COUNT - 1) % DIR_COUNT;
    putClientProperty(PROP_DIRECTION, direction);
    repaint();
  }

  private void rotateRight()
  {
    direction = (direction + 1) % DIR_COUNT;
    putClientProperty(PROP_DIRECTION, direction);
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

  public void setPainter(MazePainter painter)
  {
    this.painter = painter;
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    if(painter != null)
    {
      painter.paintMaze(maze, current, direction, getSize(), g);
    }
  }

}
