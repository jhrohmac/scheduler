#!/usr/bin/env bash
set -euo pipefail

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$REPO_DIR"

NOTE="${1:-}"
if [[ -z "$NOTE" ]]; then
  echo "사용법: ./end-work.sh \"오늘 한 일 요약\""
  exit 1
fi

echo "🧾 end-work: $REPO_DIR"

# NEXT_ACTION.md 맨 위에 로그 삽입
if [[ -f NEXT_ACTION.md ]]; then
  TMP=$(mktemp)
  {
    echo "# LAST UPDATE"
    echo "- $(date '+%Y-%m-%d %H:%M') ${NOTE}"
    echo ""
    cat NEXT_ACTION.md
  } > "$TMP"
  mv "$TMP" NEXT_ACTION.md
fi

echo ""
echo "== git status =="
git status -sb || true

echo ""
echo "✅ 종료 체크"
echo "1) NEXT_ACTION.md 업데이트 확인"
echo "2) 커밋: git add -A && git commit -m 'docs: update next action'"
echo "3) 푸시(원격 있으면): git push"
