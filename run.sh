#!/usr/bin/env sh
# Compile & jalankan ANIMON. Argumen diteruskan ke game, mis: ./run.sh --seed 42
set -e
cd "$(dirname "$0")"
rm -rf out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out animon.Main "$@"
