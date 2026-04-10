#!/usr/bin/env bash
# 종목 마스터 갱신 배치 DB 설정 적용
# 1. TB_STK_MASTER INDEX_CD 컬럼 추가 / DOW_MEMBER_YN 제거 (PL/SQL)
# 2. STK_MASTER_REFRESH 잡 seed (DML)
#
# 사용법: ./db/apply_stk_master_refresh.sh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PROP_FILE="$ROOT_DIR/webapp/WEB-INF/resources/config/mybatis/oracle/oracle.db.properties"
OJDBC_JAR="$ROOT_DIR/webapp/WEB-INF/lib/ojdbc11.jar"
TOOLS_SRC="$ROOT_DIR/db/tools"
TOOLS_OUT="$ROOT_DIR/db/tools/out"

if [[ ! -f "$PROP_FILE" ]]; then
  echo "[ERROR] oracle.db.properties not found: $PROP_FILE" >&2; exit 2
fi
if [[ ! -f "$OJDBC_JAR" ]]; then
  echo "[ERROR] ojdbc11.jar not found: $OJDBC_JAR" >&2; exit 2
fi

mkdir -p "$TOOLS_OUT"

# DbSqlFileRunner 컴파일 (이미 있으면 skip)
if [[ ! -f "$TOOLS_OUT/db/tools/DbSqlFileRunner.class" ]]; then
  echo "[1/3] DbSqlFileRunner 컴파일..."
  javac -encoding UTF-8 -cp "$OJDBC_JAR" -d "$TOOLS_OUT" "$TOOLS_SRC/DbSqlFileRunner.java"
fi

echo "[1/2] SEED: STK_MASTER_REFRESH 잡 등록"
java -cp "$TOOLS_OUT:$OJDBC_JAR" db.tools.DbSqlFileRunner \
  "$PROP_FILE" \
  "$ROOT_DIR/db/SEED_STK_MASTER_REFRESH_JOB.sql"

echo ""
echo "=== 완료 ==="
echo "  잡 등록: STK_MASTER_REFRESH_ALL"
echo ""
echo "[주의] INDEX_CD migration (PL/SQL 블록 포함)은 sqlplus로 직접 실행하세요:"
echo "  sqlplus user/pass@db @db/MIGRATE_TB_STK_MASTER_INDEX_CD.sql"
