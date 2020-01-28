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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;
import de.nigjo.maze.core.MazeGenerator;

/**
 *
 * @author nigjo
 */
public class BacktrackerMaze implements MazeGenerator
{
  @Override
  public Maze generateMaze(long seed, Map<String, Object> parameters)
  {
    int width = (Integer)parameters.get("width");
    int height = (Integer)parameters.get("height");

    return generateMaze(seed, width, height);
  }

  public Maze generateMaze(long rndSeed, int width, int height)
  {
    QuadraticMaze simple = new QuadraticMaze(width, height);
    simple.setName("Backtracker");

    Random rnd = new Random(rndSeed);

    int start = rnd.nextInt(width / 2) + (width / 4);
    int end = rnd.nextInt(width / 2) + (width / 4);

    List<Cell> cells = simple.getCells();

    Cell entrance = cells.get(start);
    simple.setEntance(entrance);
    simple.setExit(cells.get(cells.size() - end - 1));

//    System.out.println(simple);
    Set<Cell> visited = new HashSet<>();
    visited.add(entrance);
    List<Cell> stack = new ArrayList<>(width * (height / 2));
    stack.add(0, entrance);

    while(!stack.isEmpty())
    {
      Cell current = stack.remove(0);
      List<Cell> sibs = new ArrayList<>(current.getSiblings());
      do
      {
        int sIndex = rnd.nextInt(sibs.size());
        Cell sib = sibs.remove(sIndex);
        if(sib != null && !visited.contains(sib))
        {
          stack.add(0, current);
          current.removeWall(sib);
          visited.add(sib);
          stack.add(0, sib);
          sibs.clear();
        }
      }
      while(!sibs.isEmpty());
    }

    return simple;
  }

}
