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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A configuration to build a maze.
 *
 * @author nigjo
 */
public class Config
{
  @CliParameter(defaultValue = "10", singleChar = 'w', longOption = "width")
  private int width = 10;
  @CliParameter(defaultValue = "10", singleChar = 'h', longOption = "height")
  private int height = 10;
  @CliParameter(defaultValue = "<now>", longOption = "seed")
  private String hashBase;
  private long seed = System.currentTimeMillis();

  public void parseCommandline(String[] args)
  {
    parseCommandline(args, 2);
  }

  private final Map<CliParameter, Field> knownParameters = new HashMap<>();
  private final Map<String, CliParameter> argNames = new HashMap<>();
  private boolean legacyMode = false;

  public void parseCommandline(String[] args, int seedIndex)
  {
    parseCommandline(args, null, seedIndex);
  }

  public void parseCommandline(String[] args, Class<?> parameterContext)
  {
    parseCommandline(args, parameterContext, -1);
  }

  public void parseCommandline(
      String[] args, Class<?> parameterContext, int legacySeedIndex)
  {
    loadCliParameters(parameterContext);

    List<String> unknown = new ArrayList<>();
    List<String> walker = Arrays.asList(args);

    try
    {
      boolean skipNext = false;
      for(String arg : walker)
      {
        if(skipNext)
        {
          skipNext = false;
          continue;
        }
        CliParameter parameter = argNames.get(arg);
        if(parameter != null)
        {
          unknown = null;
          Field field = knownParameters.get(parameter);
          Class<?> type = field.getType();
          if(!parameter.standalone())
          {
            skipNext = true;
            String next = walker.get(walker.indexOf(arg) + 1);
            if(type == Boolean.class || type == boolean.class)
            {
              field.setAccessible(true);
              field.set(this, Boolean.parseBoolean(next));
            }
            else if(type == Integer.class || type == int.class)
            {
              field.setAccessible(true);
              field.set(this, Integer.parseInt(next));
            }
            else if(type == String.class)
            {
              field.setAccessible(true);
              field.set(this, next);
            }
            else
            {
              throw new IllegalArgumentException(
                  "unknown type " + type.getName() + " for " + arg);
            }
          }
          else
          {
            if(type == Boolean.class || type == boolean.class)
            {
              field.setAccessible(true);
              field.set(this, true);
            }
            else
            {
              throw new IllegalArgumentException(
                  "unknown type " + type.getName() + " for " + arg);
            }
          }
        }
        else if(unknown != null)
        {
          unknown.add(arg);
        }
      }
    }
    catch(IllegalArgumentException | ReflectiveOperationException ex)
    {
      System.err.println(ex.toString());
      System.exit(1);
    }

    if(unknown != null)
    {
      legacyMode = true;
      String[] legacyArgs = unknown.toArray(String[]::new);
      if(legacyArgs.length > 0)
      {
        width = Integer.parseInt(legacyArgs[0]);
      }
      if(legacyArgs.length > 1)
      {
        height = Integer.parseInt(legacyArgs[1]);
      }
      if(legacyArgs.length > legacySeedIndex && !legacyArgs[legacySeedIndex].isEmpty())
      {
        hashBase = legacyArgs[legacySeedIndex];
        try
        {
          seed = Long.parseLong(legacyArgs[legacySeedIndex]);
        }
        catch(NumberFormatException ex)
        {
          seed = legacyArgs[legacySeedIndex].hashCode();
        }
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

  public String getHashBase()
  {
    return hashBase;
  }

  public Map<String, Object> getParameters()
  {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("width", width);
    parameters.put("height", height);
    return parameters;
  }

  private void loadCliParameters(Class<?> parameterContext)
  {
    scanClass(Config.class);

    if(parameterContext != null)
    {
      scanClass(parameterContext);
    }
    else
    {
      String callerClassName = null;
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      for(int i = 1; i < stackTrace.length; i++)
      {
        if(!Config.class.getName().equals(stackTrace[i].getClassName()))
        {
          callerClassName = stackTrace[i].getClassName();
          break;
        }
      }
      try
      {
        Class<?> callerClass =
            Thread.currentThread().getContextClassLoader().loadClass(callerClassName);
        scanClass(callerClass);
      }
      catch(ClassNotFoundException ex)
      {
        System.err.println(ex.toString());
      }
    }
  }

  private void scanClass(Class<?> aClass)
  {
    for(Field field : aClass.getDeclaredFields())
    {
      CliParameter paramDef = field.getAnnotation(CliParameter.class);
      if(paramDef != null)
      {
        knownParameters.put(paramDef, field);
        String longName = paramDef.longOption();
        if(!longName.isBlank())
        {
          argNames.putIfAbsent("--" + longName, paramDef);
        }
        char shortName = paramDef.singleChar();
        if(shortName != '\0')
        {
          argNames.putIfAbsent("-" + shortName, paramDef);
        }
        else if(longName.isBlank())
        {
          argNames.putIfAbsent("--" + field.getName(), paramDef);
        }
      }
    }
  }

  public boolean isLegacyMode()
  {
    return legacyMode;
  }

  /**
   * Marks a command line parameter. The parameter can be a {@code String}, {@code int} or
   * {@code boolean} (or their wrapper classes) and must be {@code static}. A value
   * defined on command line will be written directly to the field.
   *
   * The package containing a class with CliParameter Fields must be opened to the
   * {@code core} module.
   * <pre>opens this.is.my.app to de.nigjo.maze.core;</pre>
   *
   * @see Config#parseCommandline(String[], Class)
   */
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface CliParameter
  {
    boolean standalone() default false;

    char singleChar() default '\0';

    String longOption() default "";

    public String defaultValue() default "";
  }
}
