#!/usr/bin/env bash
# TB_STK_BATCH_JOB_PARAM ORA-12860 근본 해결
# - PK_STK_BATCH_JOB_PARAM → REVERSE KEY 변환
# - SEQ_STK_BATCH_JOB_PARAM NOCACHE → CACHE 20
#
# 사용법: ./db/apply_batch_param_reverse_idx.sh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PROP_FILE="$ROOT_DIR/webapp/WEB-INF/resources/config/mybatis/oracle/oracle.db.properties"
OJDBC_JAR="$ROOT_DIR/webapp/WEB-INF/lib/ojdbc11.jar"
TOOLS_OUT="$ROOT_DIR/db/tools/out"

java -cp "$TOOLS_OUT:$OJDBC_JAR" db.tools.DbSqlFileRunner \
  "$PROP_FILE" \
  "$ROOT_DIR/db/ALTER_TB_STK_BATCH_JOB_PARAM_REVERSE_IDX.sql"

echo ""
echo "=== 완료 ==="
echo "  PK_STK_BATCH_JOB_PARAM: REVERSE KEY 변환"
echo "  SEQ_STK_BATCH_JOB_PARAM: CACHE 20 적용"
