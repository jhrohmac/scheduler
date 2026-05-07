package com.scheduler.stock.indicator;

import java.util.List;

import com.scheduler.stock.dto.DlyPriceDto;
import com.scheduler.stock.dto.RecSignalDto;

/**
 * 지표 평가 시 전달되는 종목 컨텍스트.
 * - dto: TB_REC_SIGNAL 행 (기본 사전계산 지표값)
 * - priceList: 일봉 (실시간 계산 지표가 필요로 할 때만 prefetch됨)
 */
public class IndicatorContext {

    private final RecSignalDto dto;
    private List<DlyPriceDto> priceList;

    public IndicatorContext(RecSignalDto dto) {
        this.dto = dto;
    }

    public IndicatorContext(RecSignalDto dto, List<DlyPriceDto> priceList) {
        this.dto = dto;
        this.priceList = priceList;
    }

    public RecSignalDto getDto() { return dto; }
    public List<DlyPriceDto> getPriceList() { return priceList; }
    public void setPriceList(List<DlyPriceDto> priceList) { this.priceList = priceList; }
}
