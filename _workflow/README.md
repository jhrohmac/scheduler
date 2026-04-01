# Scheduler Workflow (Obsidian Vault)

이 폴더는 **프로젝트 진행/설계/튜닝(백테스트, MFE/MAE)** 를 시각화(Obsidian Canvas)로 관리하기 위한 전용 Vault 입니다.
노션 `MASTER WORKFLOW DB` 와 짝을 맞춰 같이 운영합니다.

- 공식 Canvas 파일: `canvas/MASTER_WORKFLOW.canvas`
- 보조 설명 노트: `notes/MASTER_WORKFLOW_NOTE.md`
- 핵심 노트: `notes/` 하위

원칙:
- 코드/SQL은 기존 위치 유지
- 진행상태/설계/의사결정/체크리스트는 여기서 관리
- 구조와 연결은 `MASTER_WORKFLOW.canvas` 기준
- 상태와 다음 액션은 Notion 기준
- 구현 결과는 실제 Source 기준
- 보조 캔버스는 두지 않고, 추가 설명은 `MASTER_WORKFLOW_NOTE.md` 에 정리
- `.obsidian/` 설정은 환경별이므로 git에는 보통 제외 권장

관련 문서:
- `notes/90_DEV_SYNC_RULE.md`
- `notes/92_SOURCE_SYNC_MATRIX.md`
