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
package de.nigjo.maze.ui;

import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.ServiceLoader;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.nigjo.maze.core.Config;
import de.nigjo.maze.core.Maze;
import de.nigjo.maze.core.MazeGenerator;
import de.nigjo.maze.core.QuadraticMazePainter;

/**
 *
 * @author nigjo
 */
public class Startup
{
  private static final ResourceBundle BUNDLE =
      ResourceBundle.getBundle(Startup.class.getPackageName() + ".Bundle");

  public static void main(String[] args)
  {
    Config cfg = new Config();
    cfg.parseCommandline(args);

    ServiceLoader<MazeGenerator> generators = ServiceLoader.load(MazeGenerator.class);
    MazeGenerator generator = null;
    Iterator<MazeGenerator> iterator = generators.iterator();
    while(iterator.hasNext())
    {
      generator = iterator.next();
    }
    if(generator != null)
    {
      SwingUtilities.invokeLater(Startup::initUI);

      initMaze(cfg, generator);
    }
  }

  private static void initMaze(Config cfg, MazeGenerator generator)
  {
    Maze maze = generator.generateMaze(cfg.getSeed(), cfg.getParameters());

    System.out.println(QuadraticMazePainter.toString(maze));
    SwingUtilities.invokeLater(() -> FrameBuilder.setMaze(maze));

    SwingUtilities.invokeLater(() -> FrameBuilder.findMazePanel().ifPresent(
        mp -> mp.addPropertyChangeListener(MazePanel.PROP_SOLVED, Startup::mazeSolved)));
  }

  private static void initUI()
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException ex)
    {
      ex.printStackTrace(System.err);
    }

    FrameBuilder.buildFrame();
  }

  private static void mazeSolved(PropertyChangeEvent evt)
  {
    MazePanel mp = (MazePanel)evt.getSource();
    JOptionPane.showMessageDialog(mp,
        BUNDLE.getString("Startup.exit_found"),
        BUNDLE.getString("Startup.dlg_title"),
        JOptionPane.INFORMATION_MESSAGE);
    SwingUtilities.getWindowAncestor(mp).dispose();
  }

}
