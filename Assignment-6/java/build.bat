@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out dps\*.java
if errorlevel 1 exit /b 1
echo Build OK. Run: java -cp out dps.DataProcessingApp
