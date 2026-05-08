-- =============================================================================
-- TB_USER_FILTER_PRESET — 사용자별 화면 필터/지표 설정 프리셋
--
-- 목적: 동적 추천 종목 선별(recPickDynamic) 등 사용자가 화면에서 조정한
--       필터/지표 설정을 ID별로 1건 저장하여 다음 진입 시 자동 복원
--
-- 설계:
--   - PK: (USER_ID, PRESET_TYPE) — 화면별 1건씩 보관
--   - PRESET_TYPE 으로 향후 다른 화면 재활용 가능
--     예) 'REC_PICK_DYNAMIC' (동적 선별), 'WATCHLIST_FILTER' (관심종목 필터)
--   - FILTER_JSON: 전체 설정을 JSON 문자열로 저장 (스키마 변경 부담 최소화)
-- =============================================================================

CREATE TABLE TB_USER_FILTER_PRESET (
    USER_ID       VARCHAR2(50)  NOT NULL,
    PRESET_TYPE   VARCHAR2(30)  DEFAULT 'REC_PICK_DYNAMIC' NOT NULL,
    FILTER_JSON   CLOB          NOT NULL,
    REG_DT        DATE          DEFAULT SYSDATE NOT NULL,
    UPD_DT        DATE          DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_USER_FILTER_PRESET PRIMARY KEY (USER_ID, PRESET_TYPE)
);

COMMENT ON TABLE  TB_USER_FILTER_PRESET           IS '사용자 화면 필터/지표 설정 프리셋';
COMMENT ON COLUMN TB_USER_FILTER_PRESET.USER_ID     IS '사용자 ID (UserSession.user_id)';
COMMENT ON COLUMN TB_USER_FILTER_PRESET.PRESET_TYPE IS '프리셋 종류: REC_PICK_DYNAMIC, WATCHLIST_FILTER, ...';
COMMENT ON COLUMN TB_USER_FILTER_PRESET.FILTER_JSON IS '전체 필터/지표 설정 JSON (mktGroup, market, indexFilter, stockType, sortColumn, indicators[])';
COMMENT ON COLUMN TB_USER_FILTER_PRESET.REG_DT      IS '최초 저장일';
COMMENT ON COLUMN TB_USER_FILTER_PRESET.UPD_DT      IS '최종 수정일';
