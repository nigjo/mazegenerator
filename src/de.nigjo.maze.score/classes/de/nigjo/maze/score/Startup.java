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
    if("--list".equals(args[0]))
    {
      ServiceLoader<Scorer> scorers = ServiceLoader.load(Scorer.class);
      scorers.forEach(s -> System.out.println(s.getName()));
      return;
    }
    List<String> hashes;
    if("--all".equals(args[0]))
    {
      String[] shifted = Arrays.asList(args)
          .subList(1, args.length)
          .toArray(String[]::new);
      Config cfg = new Config();
      cfg.parseCommandline(shifted, 3);
      int count = shifted.length > 2 ? Integer.parseInt(shifted[2]) : 1;

      hashes = performAllScores(cfg, count);

    }
    else
    {

      Config cfg = new Config();
      cfg.parseCommandline(args, 3);
      int count = args.length > 2 ? Integer.parseInt(args[2]) : 1;

      List<MazeInfo> mazes = MazeGenerationManager.generateMazes(cfg, count);

      Collection<ScoreInfo> scores = findScores(mazes);

      ResultPrinter.printMazes(scores);
      hashes = new ArrayList<>();
      scores.forEach(i -> hashes.add(i.mazeInfo.hash));
    }

    HashfileManager.store(hashes);
  }

  private static Collection<ScoreInfo> findScores(List<MazeInfo> mazes)
  {
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
    return findScores(mazes, scorer);
  }

  private static Collection<ScoreInfo> findScores(List<MazeInfo> mazes, Scorer scorer)
  {
    List<ScoreInfo> scores = new ArrayList<>();
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

  private static List<String> performAllScores(Config cfg, int count)
  {
    Map<Scorer, Map<Long, ScoreInfo>> scorerData = new LinkedHashMap<>();
    Map<Long, Integer> scoreboard = new HashMap<>();
    ServiceLoader< Scorer> scorers = ServiceLoader.load(Scorer.class);
    for(Scorer scorer : scorers)
    {
      List<MazeInfo> mazes = MazeGenerationManager.generateMazes(cfg, count);
      Collection<ScoreInfo> scores = findScores(mazes, scorer);
      Set<Number> harvester = new TreeSet<>();
      scores.forEach(s -> harvester.add(s.scores.get("score")));
      List<Number> punkte = new ArrayList<>(harvester);
      for(ScoreInfo score : scores)
      {
        int punkt = scores.size() - punkte.size()
            + punkte.indexOf(score.scores.get("score"));
        scoreboard.merge(score.mazeInfo.seed, punkt, (o, n) -> o + n);
      }
      scorerData.put(scorer, scores.stream().collect(
          Collectors.toMap(s -> s.mazeInfo.seed, Function.identity())));
    }
    List<Long> places = new ArrayList<>(scoreboard.keySet());
    places.sort((l1, l2) -> scoreboard.get(l1) - scoreboard.get(l2));

    ResultPrinter.printFullScores(places, scorerData);

    List<String> hashes = new ArrayList<>();
    Map<Long, ScoreInfo> data = scorerData.get(scorers.findFirst().orElseThrow());
    places.forEach(seed -> hashes.add(data.get(seed).mazeInfo.hash));
    return hashes;
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

    static void store(Collection<String> hashes)
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
            for(String hash : hashes)
            {
              out.write(hash);
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

          appendScores(scoreData, data);
        }

        out.println(levelView);
        out.println(info.hash);
        out.println(data);
      }
    }

    private static void appendScores(Map<String, Number> scoreData, StringBuilder data)
    {
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

    private static void printFullScores(List<Long> seedOrder,
        Map<Scorer, Map<Long, ScoreInfo>> scorerData)
    {
      for(Long seed : seedOrder)
      {
        System.out.println("seed: " + seed);
        List<String[]> lines = new ArrayList<>();
        List<String[]> params = new ArrayList<>();
        for(Map<Long, ScoreInfo> data : scorerData.values())
        {
          ScoreInfo info = data.get(seed);
          lines.add(
              QuadraticMazePainter.toString(info.mazeInfo.maze,
                  '·', info.marker).split("\n"));
          StringBuilder scoreValues = new StringBuilder();
          Map<String, Number> scores = new TreeMap<>(ResultPrinter::sortWithScoreFirst);
          scores.putAll(info.scores);

          ResultPrinter.appendScores(scores, scoreValues);
          params.add(scoreValues.toString().split(", "));
        }

        int lineCount = lines.get(0).length;
        for(int i = 0; i < lineCount; i++)
        {
          StringBuilder b = new StringBuilder();
          for(String[] line : lines)
          {
            b.append(' ').append(line[i]);
          }
          System.out.println(b);
        }
        int colsize = lines.get(0)[0].length();
        char[] spaces = new char[colsize];
        Arrays.fill(spaces, ' ');
        for(int i = 0; i < lineCount; i++)
        {
          StringBuilder b = new StringBuilder();
          for(String[] line : params)
          {
            b.append(' ');
            if(i < line.length)
            {
              b.append(' ');
              b.append(line[i]);
              b.append(spaces, 0, colsize - line[i].length() - 1);
            }
            else
            {
              b.append(spaces, 0, colsize);
            }
          }
          if(!b.toString().isBlank())
          {
            System.out.println(b);
          }
        }
      }
    }

  }
}
