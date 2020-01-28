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
package de.nigjo.maze;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import de.nigjo.maze.core.Maze;
import de.nigjo.maze.core.MazeGenerator;

/**
 * Eine neue Klasse von Jens Hofschröer. Erstellt Jan 28, 2020, 8:52:23 AM.
 *
 * @todo Hier fehlt die Beschreibung der Klasse.
 *
 * @author Jens Hofschröer
 */
public class Generator
{
  public static void main(String[] args)
  {
    int width = 10;
    int height = 10;
    long seed = System.currentTimeMillis();
    if(args.length > 0)
    {
      width = Integer.parseInt(args[0]);
    }
    if(args.length > 1)
    {
      height = Integer.parseInt(args[1]);
    }
    if(args.length > 2 && !args[2].isBlank())
    {
      try
      {
        seed = Long.parseLong(args[2]);
      }
      catch(NumberFormatException ex)
      {
        System.out.println("getting seed from \"" + args[2] + "\"");
        seed = args[2].hashCode();
      }
    }

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("width", width);
    parameters.put("height", height);

    System.out.println("used seed is " + seed);

    ServiceLoader<MazeGenerator> generators = ServiceLoader.load(MazeGenerator.class);

    long randomStart = seed;
    for(MazeGenerator generator : generators)
    {
      Maze maze = generator.generateMaze(randomStart, parameters);
      if(maze != null)
      {
        Solver.solve(maze);
        printSingle(maze);
      }
    }
  }

  private static void solveAndPrint(Maze m1, Maze m2)
  {
    int count = Solver.solve(m1);
    System.out.print(count + " ");
    count = Solver.solve(m2);
    System.out.println(count);
    printMerged(m1.toString(), m2.toString());
  }

  private static void printSingle(Maze maze)
  {
    System.out.println(" " + maze.getName());
    System.out.println(QuadraticMazePainter.toString(maze));
  }

  private static void printMerged(Maze m1, Maze m2)
  {
    char[] spaces = new char[m1.getWidth() * 4 + 1];
    System.out.println(
        " " + (m1.getName() + new String(spaces)).substring(0, spaces.length)
        + " " + m2.getName());
    printMerged(
        QuadraticMazePainter.toString(m1),
        QuadraticMazePainter.toString(m2));
  }

  private static void printMerged(String s1, String s2)
  {
    String[] lines1 = s1.split("[\n\r]+");
    String[] lines2 = s2.split("[\n\r]+");
    for(int i = 0; i < lines1.length; i++)
    {
      System.out.println(lines1[i] + ' ' + lines2[i]);
    }
  }

}
