@echo off
REM Compile & jalankan GUI ANIMON (Swing). Argumen diteruskan ke MainFrame, mis: run_gui.bat --seed 42
setlocal
cd /d "%~dp0"
if exist out rmdir /s /q out
powershell -NoProfile -Command "Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { '\"' + (Resolve-Path -Relative $_.FullName).Replace('\','/') + '\"' } | Set-Content sources.txt"
javac -encoding UTF-8 -d out @sources.txt
set ERR=%ERRORLEVEL%
del sources.txt
if not %ERR%==0 (
    echo Kompilasi gagal.
    exit /b %ERR%
)
java -cp out animon.gui.MainFrame %*

