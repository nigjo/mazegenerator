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

module de.nigjo.maze.score
{
  requires de.nigjo.maze.core;
  requires de.nigjo.maze.generator;

  uses de.nigjo.maze.core.MazeGenerator;
  uses de.nigjo.maze.score.Scorer;

  provides de.nigjo.maze.score.Scorer with
      de.nigjo.maze.score.StartEndScorer,
      de.nigjo.maze.score.JunctionCounter;
}
