@echo off
REM Compile & jalankan ANIMON. Argumen diteruskan ke game, mis: run.bat --seed 42
setlocal
cd /d "%~dp0"
if exist out rmdir /s /q out
REM Path di-quote & relatif agar aman untuk folder yang mengandung spasi (mis. "Semester 5").
powershell -NoProfile -Command "Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { '\"' + (Resolve-Path -Relative $_.FullName).Replace('\','/') + '\"' } | Set-Content sources.txt"
javac -encoding UTF-8 -d out @sources.txt
set ERR=%ERRORLEVEL%
del sources.txt
if not %ERR%==0 (
    echo Kompilasi gagal.
    exit /b %ERR%
)
java -cp out animon.Main %*
