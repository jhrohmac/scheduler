# WORKFLOW

## Branch 규칙
- `feat/<short-topic>`: 기능 개발
- `fix/<short-topic>`: 버그 수정
- `ops/<short-topic>`: 운영/배포/인프라
- `docs/<short-topic>`: 문서

예시:
- `feat/wf-1-3-position-ui`
- `fix/mobile-chart-pan`
- `ops/tomcat-8080-restore`

## 작업 시작 루틴
1. `./start-work.sh`
2. `NEXT_ACTION.md`의 **Now** 1개만 선택
3. 30~60분 단위로 구현
4. 검증 후 커밋

## 커밋 규칙
- `feat: ...`
- `fix: ...`
- `ops: ...`
- `docs: ...`

## 종료 루틴 (반드시)
- `NEXT_ACTION.md` 업데이트
- 다음 1스텝 1줄 작성
- 커밋/푸시 상태 확인
