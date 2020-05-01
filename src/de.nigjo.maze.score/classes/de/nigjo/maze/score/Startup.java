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
package de.nigjo.maze.score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ServiceLoader;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;
import de.nigjo.maze.core.MazeGenerator;
import de.nigjo.maze.core.QuadraticMazePainter;

/**
 *
 * @author nigjo
 */
public class Startup
{
  public static void main(String[] args)
  {
    int width = Integer.parseInt(args[0]);
    int height = Integer.parseInt(args[1]);
    int count = Integer.parseInt(args[2]);

    String argHash = args.length > 3 ? args[3] : null;
    long seed;
    if(argHash != null)
    {
      try
      {
        seed = Long.parseLong(argHash, 10);
      }
      catch(NumberFormatException ex)
      {
        seed = argHash.hashCode();
      }
    }
    else
    {
      seed = System.currentTimeMillis();
    }
    String hash = argHash != null ? argHash : String.format("%d", seed);

    Map<String, Object> parameters = Map.of(
        "width", width, "height", height);

    MazeGenerator generator = getGenerator("kruskal");

    Random rnd = new Random(seed);

    class MazeInfo
    {
      Maze maze;
      long seed;
      String hash;
      int startCount;
      int endCount;
      int length;
      int level;
    }

    List<MazeInfo> mazes = new ArrayList<>();

    for(int i = 0; i < count; i++)
    {
      MazeInfo info = new MazeInfo();
      if(count > 1)
      {
        seed = rnd.nextLong();
        hash = String.format("%d", seed);
      }

      info.seed = seed;
      info.hash = hash;

      Maze maze = generator.generateMaze(seed, parameters);

      info.maze = maze;

      info.length = Solver.solve(maze);

      Map<Cell, Integer> startDistance = new HashMap<>();
      Map<Cell, Integer> endDistance = new HashMap<>();

      Cell exit = findDistance(maze, maze.getEntance(), startDistance);
      findDistance(maze, exit, endDistance);

      info.startCount = 0;
      info.endCount = 0;
      //TODO: Markierungen setzen
      for(Cell cell : maze.getCells())
      {
        int start = startDistance.get(cell);
        int ende = endDistance.get(cell);
        if(cell.getMark() != Cell.MARK_WALKED)
        {
          if(start > ende)
          {
            cell.setMark(200);
            info.endCount++;
          }
          else
          {
            cell.setMark(100);
            info.startCount++;
          }
        }
      }

      double sum = (info.length + info.startCount + info.endCount) / 100.;

      if(info.endCount / sum > 66 || info.length < (info.maze.getHeight() * 1.5))
      {
        info.level = 1;
      }
      else if(info.endCount > info.startCount)
      {
        info.level = 2;
      }
      else if(info.length / sum > 30)
      {
        info.level = 6;
      }
      else if(info.length / sum > 25)
      {
        info.level = 5;
      }
      else if(info.startCount > info.endCount)
      {
        info.level = 3;
      }
      else
      {
        info.level = 4;
      }

      mazes.add(info);
    }

    mazes.sort((i1, i2) ->
    {
      int delta = i1.startCount - i2.startCount;
      if(delta != 0)
      {
        return delta;
      }

      delta = i1.length - i2.length;
      if(delta == 0)
      {
        return i1.endCount - i2.endCount;
      }
      return delta;
    });

    for(MazeInfo info : mazes)
    {
      double sum = (info.length + info.startCount + info.endCount) / 100.;
      String levelView =
          QuadraticMazePainter.toString(info.maze, '·', Map.of(
              100, '+', 200, '-'
          ));
      System.out.println(levelView);
      System.out.println(info.hash);
      System.out.println(String.format("id-%03d", mazes.indexOf(info) + 1)
          + ", path: " + String.format("%4.1f%%", info.length / sum)
          + ", start: " + String.format("%4.1f%%", info.startCount / sum)
          + ", end: " + String.format("%4.1f%%", info.endCount / sum)
          + ", lvl?: " + String.format("%2d", info.level)
      );
      //System.out.println("Level " + info.level+"?");
    }
  }

  private static Cell findDistance(Maze maze, Cell entance,
      Map<Cell, Integer> distances)
  {
    Cell exitCell = null;
    List<Cell> stack = new ArrayList<>();
    stack.add(entance);
    distances.put(entance, 0);

    while(!stack.isEmpty())
    {
      Cell current = stack.remove(0);
      if(exitCell == null && maze.isExit(current))
      {
        exitCell = current;
      }
      Integer distance = distances.get(current);
      List<Cell> siblings = current.getSiblings();
      for(int sIdx = 0; sIdx < siblings.size(); sIdx++)
      {
        //Cell get = current.getSiblings().get(sIdx);
        if(!current.hasWall(sIdx))
        {
          Integer sibDist = distances.putIfAbsent(siblings.get(sIdx), distance + 1);
          if(sibDist == null)
          {
            stack.add(siblings.get(sIdx));
          }
        }
      }
    }

    return exitCell;
  }

  public static MazeGenerator getGenerator(String name)
  {
    ServiceLoader<MazeGenerator> services = ServiceLoader.load(MazeGenerator.class);
    return services.stream()
        .map(ServiceLoader.Provider::get)
        .filter(gen -> gen.getClass().getSimpleName()
        .toLowerCase().contains(name.toLowerCase()))
        .findFirst()
        .orElse(null);
  }
}
