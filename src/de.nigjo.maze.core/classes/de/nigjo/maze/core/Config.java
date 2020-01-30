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
package de.nigjo.maze.core;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nigjo
 */
public class Config
{
  private int width = 10;
  private int height = 10;
  private long seed = System.currentTimeMillis();

  public void parseCommandline(String[] args)
  {
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
        seed = args[2].hashCode();
      }
    }
  }

  public int getWidth()
  {
    return width;
  }

  public int getHeight()
  {
    return height;
  }

  public long getSeed()
  {
    return seed;
  }

  public Map<String, Object> getParameters()
  {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("width", width);
    parameters.put("height", height);
    return parameters;
  }
}
