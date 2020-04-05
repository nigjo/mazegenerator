@echo off

if not "%~1" == "auto" (
  if not defined MAZE_WIDTH set /p "MAZE_WIDTH=Breite (15): "
  if not defined MAZE_HEIGHT set /p "MAZE_HEIGHT=Hoehe (10): "
  if not defined MAZE_SEED set /p "MAZE_SEED=Zufallscode ("%DATE%"): "
)
if not defined MAZE_WIDTH set MAZE_WIDTH=15
if not defined MAZE_HEIGHT set MAZE_HEIGHT=10
if not defined MAZE_SEED set "MAZE_SEED=%DATE%"

if not "%~1" == "auto" (
call bin\MazeGenerator.bat %MAZE_WIDTH% %MAZE_HEIGHT% "%MAZE_SEED%"
) else (
echo "%MAZE_SEED%"
call bin\MazeGenerator.bat %MAZE_WIDTH% %MAZE_HEIGHT% "%MAZE_SEED%"
)>>%~n0.log

if not "%~1" == "auto" pause
