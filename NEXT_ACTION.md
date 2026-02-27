# NEXT ACTION - scheduler

## Now
- [ ] Tomcat 8080 운영 안정성 확인 (외부접속 + WebSocket)
- [ ] 최근 변경사항 smoke test (모바일 watchlist/chart)

## Next (30분 단위)
1. [ ] WF-1-3 보유종목 UI 액션 연결 (프론트)
2. [ ] 관심종목 UX 보강(검색/응답속도)
3. [ ] 지수 모듈 UI 연동 마무리

## Blocked
- 없음

## Verify
- URL: http://scheduler.iptime.org:8080/scheduler/
- 모바일: /finance/mobile/watchlist.do
- 체크: 실시간 가격/차트/버튼 동작

## Notes
- 파괴적 변경 전 확인 요청
- 비밀정보(kis.properties) 커밋 금지
