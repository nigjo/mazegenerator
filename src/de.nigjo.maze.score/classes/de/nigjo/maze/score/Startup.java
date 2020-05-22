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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import de.nigjo.maze.core.Config;
import de.nigjo.maze.core.Maze;
import de.nigjo.maze.core.MazeGenerator;
import de.nigjo.maze.core.QuadraticMazePainter;
import de.nigjo.maze.score.api.MazeInfo;
import de.nigjo.maze.score.api.ScoreInfo;
import de.nigjo.maze.score.api.Scorer;

/**
 *
 * @author nigjo
 */
public class Startup
{
  public static void main(String[] args)
  {
    Config cfg = new Config();
    cfg.parseCommandline(args, 3);
    int count = args.length > 2 ? Integer.parseInt(args[2]) : 1;

    List<MazeInfo> mazes = MazeGenerationManager.generateMazes(cfg, count);

    Collection<ScoreInfo> scores = findScores(mazes);

    ResultPrinter.printMazes(scores);

    HashfileManager.store(scores);
  }

  private static Collection<ScoreInfo> findScores(List<MazeInfo> mazes)
  {
    List<ScoreInfo> scores = new ArrayList<>();
    ServiceLoader<Scorer> scorers = ServiceLoader.load(Scorer.class);
    Scorer scorer = scorers.findFirst().orElseThrow();
    String config = System.getProperty("de.nigjo.maze.scorer");
    if(config != null)
    {
      for(Scorer impl : scorers)
      {
        if(config.equals(impl.getClass().getName())
            || config.equalsIgnoreCase(impl.getName()))
        {
          scorer = impl;
          break;
        }
      }
    }
    for(MazeInfo info : mazes)
    {
      ScoreInfo scoreData = scorer.getScores(info);
      if(scoreData == null)
      {
        continue;
      }
      if(mazes.size() == 1)
      {
        if(scoreData.name == null
            && System.getProperty("de.nigjo.maze.score.name") != null)
        {
          scoreData.name = System.getProperty("de.nigjo.maze.score.name");
        }
        if(scoreData.id <= 0
            && System.getProperty("de.nigjo.maze.score.id") != null)
        {
          Integer id = Integer.getInteger("de.nigjo.maze.score.id");
          if(id != null)
          {
            scoreData.id = id;
          }
        }
      }

      scores.add(scoreData);
    }
    scores.sort(Startup::sortByScore);
    return scores;
  }

  private static int sortByScore(ScoreInfo s1, ScoreInfo s2)
  {
    if(s2.scores == null)
    {
      if(s1.scores == null)
      {
        return 0;
      }
      else
      {
        return -1;
      }
    }
    else if(s1 == null)
    {
      return 1;
    }
    double delta = s1.scores.getOrDefault("score", 0.).doubleValue()
        - s2.scores.getOrDefault("score", 0.).doubleValue();

    return delta < 0 ? -1 : (delta > 0 ? 1 : 0);
  }

  private static class MazeGenerationManager
  {
    private static List<MazeInfo> generateMazes(Config cfg, int count)
    {
      List<MazeInfo> mazes;
      List<String> hashes = HashfileManager.getKnownHashes();
      if(hashes != null)
      {
        mazes = generateKnownMazes(cfg, hashes);
      }
      else
      {
        mazes = generateNewMazes(cfg, count);
      }
      return mazes;
    }

    private static List<MazeInfo> generateNewMazes(Config config, int count)
    {
      long seed = config.getSeed();
      String hash = config.getHashBase() != null
          ? config.getHashBase() : String.format("%d", seed);

      if(count > 1)
      {
        Random rnd = new Random(seed);
        Map<Long, String> hashes = new LinkedHashMap<>();
        for(int i = 0; i < count; i++)
        {
          seed = rnd.nextLong();
          hashes.put(seed, String.format("%d", seed));
        }
        return generateMazes(config, hashes);
      }
      else
      {
        return generateMazes(config, Map.of(seed, hash));
      }
    }

    private static long hashHash(String hashBase)
    {
      try
      {
        return Long.parseLong(hashBase);
      }
      catch(NumberFormatException ex)
      {
        return hashBase.hashCode();
      }
    }

    private static List<MazeInfo> generateKnownMazes(Config config, List<String> hashes)
    {
      Collector<String, ?, Map<Long, String>> toMap = Collectors.toMap(
          MazeGenerationManager::hashHash, Function.identity(),
          (a, b) -> a, LinkedHashMap::new);
      return generateMazes(config, hashes.stream()
          .collect(toMap));
    }

    private static List<MazeInfo> generateMazes(Config config, Map<Long, String> hashes)
    {
      Map<String, Object> parameters = Map.of(
          "width", config.getWidth(), "height", config.getHeight());

      MazeGenerator generator = getGenerator("kruskal");

      List<MazeInfo> mazes = new ArrayList<>();
      for(Map.Entry<Long, String> entry : hashes.entrySet())
      {
        long seed = entry.getKey();
        Maze maze = generator.generateMaze(seed, parameters);

        int length = Solver.solve(maze);
        MazeInfo info = new MazeInfo(maze, seed, entry.getValue(), length);

        mazes.add(info);
      }
      return mazes;
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

  private static class HashfileManager
  {
    private static final String HASHES_FILENAME =
        System.getProperty("de.nigjo.maze.score.hashesfile");

    static void store(Collection<ScoreInfo> scores)
    {
      if(HASHES_FILENAME != null)
      {
        Path hashesFiles = Paths.get(HASHES_FILENAME);
        if(!Files.exists(hashesFiles))
        {
          try(BufferedWriter out = Files.newBufferedWriter(hashesFiles,
              StandardCharsets.UTF_8,
              StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
          {
            for(ScoreInfo score : scores)
            {
              out.write(score.mazeInfo.hash);
              out.newLine();
            }
          }
          catch(IOException ex)
          {
            System.err.println(ex.toString());
          }
        }
      }
    }

    private static List<String> getKnownHashes()
    {
      if(HASHES_FILENAME != null)
      {
        Path hashesFiles = Paths.get(HASHES_FILENAME);
        if(Files.exists(hashesFiles))
        {
          try
          {
            return Files.readAllLines(hashesFiles, StandardCharsets.UTF_8);
          }
          catch(IOException ex)
          {
            ex.printStackTrace(System.err);
            System.exit(1);
            return null;
          }
        }
      }
      return null;
    }
  }

  private static class ResultPrinter
  {
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

    private static void printMazes(Collection<ScoreInfo> scores)
    {
      printMazes(scores, System.out);
    }

    private static void printMazes(Collection<ScoreInfo> scores, PrintStream out)
    {
      List<ScoreInfo> sorted = new ArrayList<>(scores);
      for(ScoreInfo item : scores)
      {
        MazeInfo info = item.mazeInfo;
        if(info == null)
        {
          continue;
        }
        String levelView =
            QuadraticMazePainter.toString(info.maze, '·', item.marker);
        Map<String, Number> scoreData = new TreeMap<>(ResultPrinter::sortWithScoreFirst);

        String name;
        if(item.name != null)
        {
          name = item.name;
        }
        else
        {
          int id = item.id;
          if(id <= 0)
          {
            id = sorted.indexOf(item) + 1;
          }
          name = String.format("id-%03d", id);
        }
        StringBuilder data = new StringBuilder();
        data.append(name);
        if(item.scores != null)
        {
          scoreData.putAll(item.scores);

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
        }

        out.println(levelView);
        out.println(info.hash);
        out.println(data);
      }
    }
  }
}
