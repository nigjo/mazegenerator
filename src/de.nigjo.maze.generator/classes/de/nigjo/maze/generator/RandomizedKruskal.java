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
package de.nigjo.maze.generator;

import java.util.*;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;
import de.nigjo.maze.core.MazeGenerator;

/**
 *
 * @author nigjo
 */
public class RandomizedKruskal implements MazeGenerator
{
  @Override
  public Maze generateMaze(long seed, Map<String, Object> parameters)
  {
    int width = (Integer)parameters.get("width");
    int height = (Integer)parameters.get("height");

    return generate(seed, width, height);
  }

  public Maze generate(long rndSeed, int width, int height)
  {
    Random rnd = new Random(rndSeed);
    QuadraticMaze maze = new QuadraticMaze(width, height);
    maze.setName("Randomized Kruskal");

    // Start in der "Mitte" der oberen Reihe
    int start = rnd.nextInt(width / 2) + (width / 4);
    // Ende in der "Mitte" der unteren Reihe
    int end = rnd.nextInt(width / 2) + (width / 4);

    List<Cell> cells = maze.getCells();
    //maze.setCells(Arrays.asList(cells));
    maze.setEntance(cells.get(start));
    maze.setExit(cells.get(cells.size() - end - 1));

    removeWalls(rnd, findWalls(cells, height, width));

//    Map<Cell, Set<Cell>> sets = new HashMap<>(Arrays.stream(cells).collect(
//        Collectors.toMap(Function.identity(), Collections::singleton)));
    return maze;
  }

  /**
   * Erzeugt einen Irrgarten nach den zufallsbestimmten "Kuskal"-Algoritmus.
   *
   * @param rnd Zufallsgenerator.
   * @param walls Menge an "Wänden". Zu Beginn sollten alle "Wände" gesetzt sein.
   */
  public static void removeWalls(Random rnd, List<Cell[]> walls)
  {
    Map<Cell, Set<Cell>> sets = new HashMap<>();
//    walls.stream().collect(Collectors.toMap(Function.identity(), Collections::singleton));
    List<Cell[]> wallStack = new ArrayList<>(walls);
    while(!wallStack.isEmpty())
    {
      Cell[] pair = wallStack.remove(rnd.nextInt(wallStack.size()));
      Set<Cell> s1 = getSet(sets, pair[0]);
      Set<Cell> s2 = getSet(sets, pair[1]);
      if(s1 != s2)
      {
        pair[0].removeWall(pair[1]);
        if(s1.size() >= s2.size())
        {
          s1.addAll(s2);
          s2.forEach(c -> sets.put(c, s1));
        }
        else
        {
          s2.addAll(s1);
          s1.forEach(c -> sets.put(c, s2));
        }
      }
    }
  }

  private static Set<Cell> getSet(Map<Cell, Set<Cell>> sets, Cell cell)
  {
    Set<Cell> s1 = sets.get(cell);
    if(s1 == null)
    {
      sets.put(cell, s1 = new HashSet<>(Collections.singleton(cell)));
    }
    return s1;
  }

  protected List<Cell[]> findWalls(List<Cell> cells, int height, int width)
  {
    Cell[] data = cells.toArray(new Cell[cells.size()]);
    int wallrowLength = (2 * width) - 1;
    Cell[][] walls = new Cell[wallrowLength * (height - 1) + width - 1][2];
    for(int i = 0; i < data.length; i++)
    {
      int row = i / width;
      int col = i % width;
      int wallindex = row * wallrowLength + col;
      if(col + 1 < width && wallindex < walls.length)
      {
        walls[wallindex][0] = data[i];
        walls[wallindex][1] = data[i + 1];
      }
      if(row + 1 < height)
      {
        walls[wallindex + width - 1][0] = data[i];
        walls[wallindex + width - 1][1] = data[i + width];
      }
    }
    List<Cell[]> w = new ArrayList<>(Arrays.asList(walls));
    return w;
  }

}
