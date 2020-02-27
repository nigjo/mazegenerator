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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;

/**
 *
 * @author nigjo
 */
public interface MazePainter
{
  static String PREFIX = MazePainter.class.getName();
  static String PROP_COLOR_BACKGROUND = PREFIX + ".background";
  static String PROP_COLOR_CEILING_FROM = PREFIX + ".ceilingFrom";
  static String PROP_COLOR_CEILING_TO = PREFIX + ".ceilingTo";
  static String PROP_COLOR_WALL = PREFIX + ".wall";
  static String PROP_COLOR_FLOOR_FROM = PREFIX + ".floorFrom";
  static String PROP_COLOR_FLOOR_TO = PREFIX + ".floorTo";
  static String PROP_COLOR_WALL_CROSS = PREFIX + ".wallCross";
  static String PROP_COLOR_WALL_BOUND = PREFIX + ".wallBound";
  static String PROP_COLOR_DOOR_ENTRANCE = PREFIX + ".doorEntrance";
  static String PROP_COLOR_DOOR_EXIT = PREFIX + ".doorExit";

  static int DIR_NORTH = 0;
  static int DIR_EAST = 1;
  static int DIR_SOUTH = 2;
  static int DIR_WEST = 3;

  public static Color getColor(Color from, Color to, int pos, int max)
  {
    double percent = pos / (double)max;
    int red = from.getRed() + (int)((to.getRed() - from.getRed()) * percent);
    int green = from.getGreen() + (int)((to.getGreen() - from.getGreen()) * percent);
    int blue = from.getBlue() + (int)((to.getBlue() - from.getBlue()) * percent);
    return new Color(red, green, blue);
  }

  public default List<Cell> getViewAhead(Cell current, int direction, int maxView)
  {
    List<Cell> ahead = new ArrayList<>();
    ahead.add(current);
    Cell sibling = current;
    while(ahead.size() < maxView && !sibling.hasWall(direction))
    {
      sibling = sibling.getSiblings().get(direction);
      ahead.add(sibling);
    }
    return ahead;
  }

  static boolean isDoor(Maze maze, Cell current, int wallDirection)
  {
    return (maze.isExit(current) && current.norm(wallDirection) == DIR_SOUTH)
        || (maze.getEntance() == current && current.norm(wallDirection) == DIR_NORTH);
  }

  public void paintMaze(Maze maze, Cell current, int direction,
      Dimension size, Graphics g);

}
