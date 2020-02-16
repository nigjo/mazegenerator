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
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

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
        mp ->
    {
      mp.addPropertyChangeListener(MazePanel.PROP_SOLVED, Startup::mazeSolved);
      mp.addPropertyChangeListener(MazePanel.PROP_CURRENT_CELL, Startup::startTimer);
      mp.addPropertyChangeListener(MazePanel.PROP_DIRECTION, Startup::startTimer);
    }));
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

  private static long starttime = 0;

  private static void startTimer(PropertyChangeEvent evt)
  {
    if(starttime == 0)
    {
      starttime = System.currentTimeMillis();
    }
  }

  private static void mazeSolved(PropertyChangeEvent evt)
  {
    String message = BUNDLE.getString("Startup.exit_found");
    if(starttime > 0)
    {
      long finishedAt = System.currentTimeMillis();
      long seconds = TimeUnit.MILLISECONDS.toSeconds(finishedAt - starttime);
      System.out.println("seconds to solve: " + seconds);
      message += "\n" + MessageFormat.format(BUNDLE.getString("Startup.duration"),
          String.format("%02d:%02d", seconds / 60, seconds % 60));
    }

    MazePanel mp = (MazePanel)evt.getSource();
    JOptionPane.showMessageDialog(mp,
        message,
        BUNDLE.getString("Startup.dlg_title"),
        JOptionPane.INFORMATION_MESSAGE);
    SwingUtilities.getWindowAncestor(mp).dispose();
  }

}
