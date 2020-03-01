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
package de.nigjo.maze.ui;

import java.util.Collection;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.UIManager;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;

/**
 * Paints the walked cells
 *
 * @author Jens Hofschröer
 */
public class WalkedHint extends JComponent
{
  public static final String PROP_COLOR_WALL = "de.nigjo.maze.ui.WalkedHint.line";
  public static final String PROP_COLOR_CURRENT = "de.nigjo.maze.ui.WalkedHint.me";
  private Maze maze;

  static
  {
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_WALL, new Color(0x80FFFFFF, true));
    UIManager.getDefaults().putIfAbsent(PROP_COLOR_CURRENT, Color.BLUE);
  }

  public WalkedHint()
  {
    super.setOpaque(false);
  }

  public void setMaze(Maze maze)
  {
    this.maze = maze;
    setPreferredSize(new java.awt.Dimension(
        maze.getWidth() * 4 + 2, maze.getHeight() * 4 + 2));
  }

  public Maze getMaze()
  {
    return maze;
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);

    if(maze == null)
    {
      return;
    }

    Dimension size = getSize();

    g.setColor(UIManager.getColor(PROP_COLOR_WALL));

    g.drawRect(0, 0, size.width - 1, size.height - 1);

    Collection<Cell> cells = maze.getCells();
    int row = 0;
    int col = 0;
    for(Cell cell : cells)
    {
      if(cell.getMark() == Cell.MARK_CURRENT)
      {
        g.setColor(UIManager.getColor(PROP_COLOR_CURRENT));
        g.fillRect(col * 4 + 1, row * 4 + 1, 4, 4);
        g.setColor(UIManager.getColor(PROP_COLOR_WALL));
      }
      else if(cell.getMark() != Cell.MARK_WALKED)
      {
        //g.setColor(UIManager.getColor(PROP_COLOR_CURRENT));
        g.fillRect(col * 4 + 1, row * 4 + 1, 4, 4);
        //g.setColor(UIManager.getColor(PROP_COLOR_WALL));
      }else{
        if(cell.hasWall(0)){
          g.drawLine(col * 4 + 1, row * 4 + 1, col * 4 + 4, row * 4 + 1);
        }
        if(cell.hasWall(1)){
          g.drawLine(col * 4 + 4, row * 4 + 1, col * 4 + 4, row * 4 + 4);
        }
        if(cell.hasWall(2)){
          g.drawLine(col * 4 + 1, row * 4 + 4, col * 4 + 4, row * 4 + 4);
        }
        if(cell.hasWall(3)){
          g.drawLine(col * 4 + 1, row * 4 + 1, col * 4 + 1, row * 4 + 4);
        }
      }
      col++;
      if(col % maze.getWidth() == 0)
      {
        row++;
        col = 0;
      }
    }
  }

}
