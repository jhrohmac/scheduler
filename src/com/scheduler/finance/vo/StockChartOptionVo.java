package com.scheduler.finance.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * TB_S_CHART_OPTION VO
 */
public class StockChartOptionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    // OPTION_ID
    private Long optionId;

    // CHART_ID (예: KIS_ITEMCHART)
    private String chartId;

    // SERIES_TYPE (예: MA)
    private String seriesType;

    // SERIES_KEY (예: ma5, ma20 등)
    private String seriesKey;

    // SERIES_LABEL (예: 5일선)
    private String seriesLabel;

    // SERIES_PERIOD (예: 5, 20, 60, 120, 240)
    private Integer seriesPeriod;

    // SERIES_COLOR (#RRGGBB)
    private String seriesColor;

    // LINE_WIDTH
    private Double lineWidth;

    // ENABLED_YN (Y/N)
    private String enabledYn;

    // DISPLAY_ORDER
    private Integer displayOrder;

    // MODIFY_DATE
    private Date modifyDate;

    // CREATE_DATE
    private Date createDate;

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    public String getChartId() {
        return chartId;
    }

    public void setChartId(String chartId) {
        this.chartId = chartId;
    }

    public String getSeriesType() {
        return seriesType;
    }

    public void setSeriesType(String seriesType) {
        this.seriesType = seriesType;
    }

    public String getSeriesKey() {
        return seriesKey;
    }

    public void setSeriesKey(String seriesKey) {
        this.seriesKey = seriesKey;
    }

    public String getSeriesLabel() {
        return seriesLabel;
    }

    public void setSeriesLabel(String seriesLabel) {
        this.seriesLabel = seriesLabel;
    }

    public Integer getSeriesPeriod() {
        return seriesPeriod;
    }

    public void setSeriesPeriod(Integer seriesPeriod) {
        this.seriesPeriod = seriesPeriod;
    }

    public String getSeriesColor() {
        return seriesColor;
    }

    public void setSeriesColor(String seriesColor) {
        this.seriesColor = seriesColor;
    }

    public Double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(Double lineWidth) {
        this.lineWidth = lineWidth;
    }

    public String getEnabledYn() {
        return enabledYn;
    }

    public void setEnabledYn(String enabledYn) {
        this.enabledYn = enabledYn;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
