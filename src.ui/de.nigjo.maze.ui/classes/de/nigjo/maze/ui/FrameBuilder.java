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

import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ServiceLoader;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import de.nigjo.maze.core.Maze;

/**
 *
 * @author nigjo
 */
public class FrameBuilder
{
  private static final ResourceBundle BUNDLE =
      ResourceBundle.getBundle(FrameBuilder.class.getPackageName() + ".Bundle");
  private static final String FRAME_TITLE = BUNDLE.getString("FrameBuilder.title");

  public static void buildFrame()
  {
    JFrame frame = new JFrame(FRAME_TITLE);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setLocationByPlatform(true);
    frame.setIconImages(Arrays.asList(
        new ImageIcon(FrameBuilder.class.getResource("frameicon.png")).getImage(),
        new ImageIcon(FrameBuilder.class.getResource("frameicon32.png")).getImage(),
        new ImageIcon(FrameBuilder.class.getResource("frameicon64.png")).getImage()
    ));

    MazePanel panel = new MazePanel();

    ServiceLoader<MazePainter> painters = ServiceLoader.load(MazePainter.class);
    painters.findFirst()
        .ifPresent(panel::setPainter);
    frame.getContentPane().add(panel);

    frame.pack();
    frame.setVisible(true);
  }

  public static void setMaze(Maze maze)
  {
    findMazePanel().ifPresent(panel -> panel.setMaze(maze));
  }

  public static Optional<MazePanel> findMazePanel()
  {
    return Arrays.stream(JFrame.getFrames())
        .filter(frame -> FRAME_TITLE.equals(frame.getTitle()))
        .findAny()
        .map(JFrame.class::cast)
        .map(JFrame::getContentPane)
        .map(c -> c.getComponent(0))
        .map(MazePanel.class::cast);
  }

}
