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

import de.nigjo.maze.core.Maze;
import de.nigjo.maze.generator.RandomizedKruskal;
import de.nigjo.maze.generator.SimpleMaze;

/**
 *
 * @author nigjo
 */
public class Maintest
{
  public static void main(String[] args)
  {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("width", 10);
    parameters.put("height", 10);
    long seed = "Hallo-Welt".hashCode();

    Maze generated1 = SimpleMaze.generate("Hallo Welt", 10, 10);
    Maze generated2 = new RandomizedKruskal().generateMaze(seed, parameters);

    printMerged(
        QuadraticMazePainter.toString(generated1),
        QuadraticMazePainter.toString(generated2));
    int count;
    count = Solver.solve(generated1);
    System.out.print(count + " ");
    count = Solver.solve(generated2);
    System.out.println(count);
    printMerged(
        QuadraticMazePainter.toString(generated1),
        QuadraticMazePainter.toString(generated2));
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
