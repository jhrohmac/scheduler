-- WF-2-4: 보유종목 POSITION_ID 시퀀스
-- Oracle 11g 이하는 IDENTITY 지원 안 하므로 시퀀스 별도 생성

CREATE SEQUENCE TB_S_POSITION_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 권한 부여 (필요시)
-- GRANT SELECT ON TB_S_POSITION_SEQ TO <사용자명>;
