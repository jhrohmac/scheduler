#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PROP_FILE="$ROOT_DIR/webapp/WEB-INF/resources/config/mybatis/oracle/oracle.db.properties"
OJDBC_JAR="$ROOT_DIR/webapp/WEB-INF/lib/ojdbc11.jar"
WORK="$ROOT_DIR/db/tools"
OUT="$WORK/out-rec-signal"

if [[ ! -f "$PROP_FILE" ]]; then
  echo "oracle.db.properties not found: $PROP_FILE" >&2
  exit 2
fi

if [[ ! -f "$OJDBC_JAR" ]]; then
  echo "ojdbc11.jar not found: $OJDBC_JAR" >&2
  exit 2
fi

mkdir -p "$OUT"

echo "[1/2] Compile rec signal DDL runner"
javac -encoding UTF-8 -cp "$OJDBC_JAR" -d "$OUT" "$WORK/DbRecSignalDdlRunner.java"

echo "[2/2] Apply rec signal DDL"
java -cp "$OUT:$OJDBC_JAR" db.tools.DbRecSignalDdlRunner "$PROP_FILE" "$ROOT_DIR/db"
