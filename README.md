# Maze Generator

A simple generator for Mazes. More of a test for JPMS. After building the jars (`ant jar` will do)
run the following command from the `dist` folder:

    java.exe --module-path . -m de.nigjo.maze/de.nigjo.maze.Generator [<width> <height> <seed>]

The default values are `10 10 <currenttimestamp>`

The values `10 5 Hello` will create an output like this:

    ┌───┬───┬───┬───┬───┬───┬─S─┬───┬───┬───┐
    │ · │ · · · │ # # # # # # # · · │ · · · │
    ├ · ┼ · ┼───┼ # ┼───┼ · ┼───┼───┼───┼ · ┤
    │ · │ · · · │ # · · │ · │ · · · │ · · · │
    ├ · ┼───┼ · ┼ # ┼───┼───┼ · ┼ · ┼ · ┼ · ┤
    │ · │ · │ # # # │ · │ · │ · │ · · · │ · │
    ├ · ┼ · ┼ # ┼───┼ · ┼ · ┼───┼───┼ · ┼───┤
    │ · · · · # # # # # # # # # # # · · · · │
    ├───┼ · ┼ · ┼───┼ · ┼───┼───┼ # ┼ · ┼───┤
    │ · · · │ · · · │ · · · │ · · # │ · · · │
    └───┴───┴───┴───┴───┴───┴───┴─E─┴───┴───┘
