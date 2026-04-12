@echo off
setlocal
cd /d "%~dp0"
go build -o dps.exe .
if errorlevel 1 exit /b 1
echo Build OK. Run: dps.exe
