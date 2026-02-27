#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

# Build classpath: compiled classes + required libs
WORK="$ROOT_DIR/db/tools"
OUT="$WORK/out"
mkdir -p "$OUT"

# Compile against webapp libs (KIS client classes are in WEB-INF/classes after build, but also in src build output).
CP="$ROOT_DIR/webapp/WEB-INF/classes:$ROOT_DIR/webapp/WEB-INF/lib/*"

javac -encoding UTF-8 -cp "$CP" -d "$OUT" "$WORK/KisProbeOverseasIndex.java"
java -cp "$OUT:$CP" db.tools.KisProbeOverseasIndex
