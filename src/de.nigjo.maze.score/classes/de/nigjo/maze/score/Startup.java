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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.TreeMap;

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

    List<MazeInfo> mazes = generateMazes(
        args.length > 3 ? args[3] : null, count, width, height);

    Map<Map<String, Number>, MazeInfo> scores = findScores(mazes);

    printMazes(scores);
  }

  private static Map<Map<String, Number>, MazeInfo> findScores(List<MazeInfo> mazes)
  {
    Map<Map<String, Number>, MazeInfo> scores = new TreeMap<>(Startup::sortByScore);
    Scorer scorer = new StartEndScorer();
    for(MazeInfo info : mazes)
    {
      Map<String, Number> scoreData = scorer.getScores(info);
      scores.put(scoreData, info);
    }
    return scores;
  }

  private static List<MazeInfo> generateMazes(
      String argHash, int count, int width, int height)
  {
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

      mazes.add(info);
    }
    return mazes;
  }

  private static int sortByScore(Map<String, Number> m1, Map<String, Number> m2)
  {
    double delta = m1.getOrDefault("score", 0.).doubleValue()
        - m2.getOrDefault("score", 0.).doubleValue();

    return delta < 0 ? -1 : (delta > 0 ? 1 : 0);
  }

  private static int sortWithScoreFirst(String m1, String m2)
  {
    if("score".equals(m1))
    {
      return -1;
    }
    if("score".equals(m2))
    {
      return 1;
    }
    return m1.compareToIgnoreCase(m2);
  }

  private static void printMazes(Map<Map<String, Number>, MazeInfo> scores)
  {
    List<MazeInfo> sorted = new ArrayList<>(scores.values());
    for(Map.Entry<Map<String, Number>, MazeInfo> item : scores.entrySet())
    {
      MazeInfo info = item.getValue();
      String levelView =
          QuadraticMazePainter.toString(info.maze, '·', Map.of(
              100, '+', 200, '-'
          ));
      Map<String, Number> scoreData = new TreeMap<>(Startup::sortWithScoreFirst);

      scoreData.putAll(item.getKey());
      StringBuilder data = new StringBuilder();
      data.append(String.format("id-%03d", sorted.indexOf(info) + 1));
      for(Map.Entry<String, Number> entry : scoreData.entrySet())
      {
        data.append(", ")
            .append(entry.getKey())
            .append(": ");
        Number num = entry.getValue();
        if(num instanceof Integer
            || num instanceof Long
            || num instanceof Short)
        {
          data.append(num);
        }
        else
        {
          data.append(String.format("%4.1f", num));
        }
      }

      System.out.println(levelView);
      System.out.println(info.hash);
      System.out.println(data);
    }
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
