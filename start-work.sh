#!/usr/bin/env bash
set -euo pipefail

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$REPO_DIR"

echo "📁 repo: $REPO_DIR"
echo "🌿 branch: $(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'N/A')"
echo ""
echo "== git status =="
git status -sb || true

echo ""
if [[ -f NEXT_ACTION.md ]]; then
  echo "== NEXT_ACTION (Now/Next) =="
  awk 'BEGIN{show=0} /^## Now/{show=1} /^## Blocked/{if(show==1) print ""; show=0} show==1{print} /^## Next/{show=1} /^## Verify/{if(show==1) print ""; show=0}' NEXT_ACTION.md
else
  echo "NEXT_ACTION.md 없음"
fi

echo ""
echo "✅ 시작 전 체크"
echo "1) 브랜치 생성: git switch -c feat/<topic>"
echo "2) 30~60분 작업"
echo "3) 검증 후 커밋"
