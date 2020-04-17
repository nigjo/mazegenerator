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
package de.nigjo.maze.core;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.IntFunction;

/**
 *
 * @author nigjo
 */
public interface MazeGenerator
{
  public Maze generateMaze(long seed, Map<String, Object> parameters);

  /**
   * Erstellt eine Matrix von Zellen für einen Irrgarten. Die einzelnen Zellen haben die
   * Anzahl von Nachbarn die in {@code sibling} angegeben. Die Funktion muss aus dem
   * aktuellen Index der Zelle berechnen, wo im Feld das passende Nachbarfeld liegt.
   *
   * @param max Maximum an zu erstellenden Zellen.
   * @param sibling Funktionen zum Berechnen der Nachbarn. Die Funktion erhält einen
   * {@code int}-Wert und muss einen Integer liefern. Ist der gelieferte Wert zwischen 0
   * (inklusive) und {@code max} (exklusiv) wird der aktuellen Zelle der Nachbar
   * eingetragen.
   *
   * @return Liste aller Zellen. Die Größe wird durch {@code max} bestimmt.
   */
  @SuppressWarnings("unchecked")
  public static Cell[] fillCells(int max, IntFunction<Integer>... sibling)
  {
    Cell cells[] = new Cell[max];
    for(int i = 0; i < cells.length; i++)
    {
      cells[i] = new Cell(sibling.length);
    }
    for(int index = 0; index < cells.length; index++)
    {
      Cell siblings[] = new Cell[sibling.length];
      for(int j = 0; j < siblings.length; j++)
      {
        //Cell sibling1 = siblings[j];
        Integer sibIndex = sibling[j].apply(index);
        if(sibIndex != null && sibIndex >= 0 && sibIndex < max)
        {
          siblings[j] = cells[sibIndex];
        }
      }
      cells[index].setSiblings(siblings);
    }
    return cells;
  }

  public static MazeGenerator getGenerator()
  {
    ServiceLoader<MazeGenerator> services = ServiceLoader.load(MazeGenerator.class);
    return services.findFirst()
        .orElse(null);
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
