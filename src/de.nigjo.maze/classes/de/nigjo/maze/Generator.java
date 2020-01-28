package de.nigjo.maze;

import java.util.HashMap;
import java.util.Map;

import de.nigjo.maze.core.Maze;
import de.nigjo.maze.generator.RandomizedKruskal;
import de.nigjo.maze.generator.SimpleMaze;

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
    // Maze generated1 = SimpleMaze.generate("Hallo Welt", 10, 10);
    // Maze generated2 = KruskalMaze.generate("Hallo Welt".hashCode(), 10, 10);
    Maze generated1 = SimpleMaze.generate(seed, width, height);
    Maze generated2 = new RandomizedKruskal().generateMaze(seed, parameters);
    
    Solver.solve(generated1);
    Solver.solve(generated2);

    printSingle(generated1);
    printSingle(generated2);
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
