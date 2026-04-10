package com.scheduler.finance.dao;

import java.util.List;
import java.util.Map;

import com.scheduler.finance.vo.StockChartOptionVo;

public interface StockChartOptionDao {

    /**
     * CHART_ID, SERIES_TYPE 기준 차트 옵션 목록 조회
     */
    List<StockChartOptionVo> selectChartOptionList(Map<String, String> paramMap);

    /**
     * 단일 차트 옵션 업데이트
     */
    int updateChartOption(StockChartOptionVo vo);

    /**
     * 단일 차트 옵션 신규 등록
     */
    int insertChartOption(StockChartOptionVo vo);

    /**
     * 특정 CHART_ID, SERIES_TYPE 의 모든 옵션 삭제
     *  - Mapper id: sql.StockChartOptionManage.deleteChartOptions
     */
    int deleteChartOptions(Map<String, String> paramMap);
}
