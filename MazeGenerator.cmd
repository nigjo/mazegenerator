@echo off
setlocal

if not defined DEFAULT_WIDTH set DEFAULT_WIDTH=20
if not defined DEFAULT_HEIGHT set DEFAULT_HEIGHT=16
if not "%~1" == "auto" (
  if not defined MAZE_WIDTH set /p "MAZE_WIDTH=Breite (%DEFAULT_WIDTH%): "
  if not defined MAZE_HEIGHT set /p "MAZE_HEIGHT=Hoehe (%DEFAULT_HEIGHT%): "
  if not defined MAZE_SEED set /p "MAZE_SEED=Zufallscode ("%DATE%"): "
)
if not defined MAZE_WIDTH set MAZE_WIDTH=%DEFAULT_WIDTH%
if not defined MAZE_HEIGHT set MAZE_HEIGHT=%DEFAULT_HEIGHT%
if not defined MAZE_SEED set "MAZE_SEED=%DATE%"

if not "%~1" == "auto" (
call bin\MazeGenerator.bat %MAZE_WIDTH% %MAZE_HEIGHT% "%MAZE_SEED%"
) else (
echo "%MAZE_SEED%"
call bin\MazeGenerator.bat %MAZE_WIDTH% %MAZE_HEIGHT% "%MAZE_SEED%"
)>>~n0.log

if not "%~1" == "auto" pause
