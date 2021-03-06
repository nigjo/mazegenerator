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
package de.nigjo.maze.score.api;

import de.nigjo.maze.core.Maze;

/**
 *
 * @author nigjo
 */
public class/*record*/ MazeInfo
{
  public final Maze maze;
  public final long seed;
  public final String hash;
  public final int length;

  public MazeInfo(Maze maze, long seed, String hash, int length)
  {
    this.maze = maze;
    this.seed = seed;
    this.hash = hash;
    this.length = length;
  }
}
