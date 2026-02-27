#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PROP_FILE="$ROOT_DIR/webapp/WEB-INF/resources/config/mybatis/oracle/oracle.db.properties"
OJDBC_JAR="$ROOT_DIR/webapp/WEB-INF/lib/ojdbc11.jar"

WORK="$ROOT_DIR/db/tools"
OUT="$WORK/out"
mkdir -p "$OUT"

javac -encoding UTF-8 -cp "$OJDBC_JAR" -d "$OUT" "$WORK/DbUpsertMarketRegimeAssets.java"
java -cp "$OUT:$OJDBC_JAR" db.tools.DbUpsertMarketRegimeAssets "$PROP_FILE"
