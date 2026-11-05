#!/usr/bin/env sh
# Compile & jalankan GUI ANIMON (Swing). Argumen diteruskan ke MainFrame, mis: ./run_gui.sh --seed 42
set -e
cd "$(dirname "$0")"
rm -rf out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out animon.gui.MainFrame "$@"

