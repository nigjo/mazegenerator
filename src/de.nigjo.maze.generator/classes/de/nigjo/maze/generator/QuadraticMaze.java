package de.nigjo.maze.generator;

import java.util.Arrays;
import java.util.List;

import de.nigjo.maze.core.Cell;
import de.nigjo.maze.core.Maze;
import static de.nigjo.maze.core.MazeGenerator.fillCells;

/**
 * Eine neue Klasse von Jens Hofschröer. Erstellt Jan 28, 2020, 10:52:12 AM.
 *
 * @todo Hier fehlt die Beschreibung der Klasse.
 *
 * @author Jens Hofschröer
 */
public class QuadraticMaze extends Maze
{

  public QuadraticMaze(int width, int height)
  {
    super();
    initCells(width, height);
  }

  private void initCells(int width, int height)
  {
    super.setWidth(width);
    super.setHeight(height);
    setCells(Arrays.asList(createQuadratic(width, height)));
  }

  /**
   * Erzeugt eine quadratische Matrix für einen Irrgarten.
   *
   * @param width Anzahl der Zellen in Breitenrichtung der Matrix.
   * @param height Anzahl der Zellen in Hoehenrichtung der Matrix.
   *
   * @return Zellenmatrix. Die Größe entspricht {@code width * height}.
   */
  @SuppressWarnings("unchecked")
  public static Cell[] createQuadratic(int width, int height)
  {
    return fillCells(width * height,
        i -> i - width, i -> (i + 1) % width == 0 ? -1 : i + 1,
        i -> i + width, i -> (i) % width == 0 ? -1 : i - 1
    );
  }

  @Override
  public List<Cell> getCells()
  {
    return (List<Cell>)super.getCells();
  }

}
