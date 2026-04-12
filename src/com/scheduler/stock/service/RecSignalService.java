package com.scheduler.stock.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import com.scheduler.stock.dao.BatchExecItemLogDao;
import com.scheduler.stock.dao.BatchExecLogDao;
import com.scheduler.stock.dao.RecSignalDao;
import com.scheduler.stock.dao.StkMasterDao;
import com.scheduler.stock.dao.TradeDateDao;
import com.scheduler.stock.dto.DlyPriceDto;
import com.scheduler.stock.dto.MaResultDto;
import com.scheduler.stock.dto.RecSignalDto;

public class RecSignalService {

    private static final String MARKET_KR = "KR";
    private static final String MARKET_US = "US";
    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_KOSPI = "KOSPI";
    private static final String FILTER_KOSDAQ = "KOSDAQ";
    private static final String FILTER_NASDAQ = "NASDAQ";
    private static final String FILTER_NYSE = "NYSE";
    private static final String FILTER_DOW = "DOW";
    private static final int DEFAULT_FETCH_DAYS = 400;
    private static final long DEFAULT_PRIMARY_INTERVAL_MS = 1000L;
    private static final long DEFAULT_RETRY_INTERVAL_MS = 2000L;
    private static final int ITEM_LOG_ERROR_MAX_BYTES = 1900;

    private static final class EffectiveBaseDateResult {
        private String effectiveBaseDt;
        private final Map<String, List<DlyPriceDto>> prefetchedPriceMap = new HashMap<String, List<DlyPriceDto>>();
        private int probeAttemptCount;
        private String probeErrorMessage;
    }

    private StkMasterDao stkMasterDao;
    private RecSignalDao recSignalDao;
    private BatchExecLogDao batchExecLogDao;
    private BatchExecItemLogDao batchExecItemLogDao;
    private TradeDateDao tradeDateDao;
    private TradeDateService tradeDateService;
    private MaCalculateService maCalculateService;
    private KisDlyPriceSyncService kisDlyPriceSyncService;

    public void setStkMasterDao(StkMasterDao stkMasterDao) {
        this.stkMasterDao = stkMasterDao;
    }

    public void setRecSignalDao(RecSignalDao recSignalDao) {
        this.recSignalDao = recSignalDao;
    }

    public void setBatchExecLogDao(BatchExecLogDao batchExecLogDao) {
        this.batchExecLogDao = batchExecLogDao;
    }

    public void setBatchExecItemLogDao(BatchExecItemLogDao batchExecItemLogDao) {
        this.batchExecItemLogDao = batchExecItemLogDao;
    }

    public void setTradeDateDao(TradeDateDao tradeDateDao) {
        this.tradeDateDao = tradeDateDao;
    }

    public void setTradeDateService(TradeDateService tradeDateService) {
        this.tradeDateService = tradeDateService;
    }

    public void setMaCalculateService(MaCalculateService maCalculateService) {
        this.maCalculateService = maCalculateService;
    }

    public void setKisDlyPriceSyncService(KisDlyPriceSyncService kisDlyPriceSyncService) {
        this.kisDlyPriceSyncService = kisDlyPriceSyncService;
    }

    public List<RecSignalDto> selectRecSignalList(HashMap<String, String> map) throws Exception {
        HashMap<String, String> queryMap = copyMap(map);
        normalizeMarketFilter(queryMap);

        String latestBaseDt = selectLatestBaseDt(queryMap);
        String baseDt = tradeDateService.resolveReadBaseDate(queryMap.get("baseDt"), latestBaseDt, queryMap.get("mktCd"));
        queryMap.put("baseDt", baseDt);

        if (isBlank(queryMap.get("recYn"))) {
            queryMap.put("recYn", "Y");
        }

        List<RecSignalDto> list = recSignalDao.selectRecSignalList(queryMap);
        return list == null ? new ArrayList<RecSignalDto>() : list;
    }

    public RecSignalDto selectRecSignalDetail(HashMap<String, String> map) throws Exception {
        HashMap<String, String> queryMap = copyMap(map);
        normalizeMarketFilter(queryMap);

        String latestBaseDt = selectLatestBaseDt(queryMap);
        queryMap.put("baseDt", tradeDateService.resolveReadBaseDate(queryMap.get("baseDt"), latestBaseDt, queryMap.get("mktCd")));
        return recSignalDao.selectRecSignalDetail(queryMap);
    }

    public HashMap<String, Object> selectBatchStatus(HashMap<String, String> map) throws Exception {
        HashMap<String, String> queryMap = copyMap(map);
        normalizeMarketFilter(queryMap);

        String marketGroup = normalizeMarketGroup(queryMap.get("mktCd"));
        String inputBaseDt = isBlank(queryMap.get("baseDt"))
                ? null
                : tradeDateService.normalizeInputBaseDate(queryMap.get("baseDt"));

        HashMap<String, Object> primaryLog = selectLatestBatchExecLog(resolveBatchId(marketGroup, false), inputBaseDt);
        HashMap<String, Object> retryLog = selectLatestBatchExecLog(resolveBatchId(marketGroup, true), inputBaseDt);

        HashMap<String, String> latestMap = new HashMap<String, String>();
        latestMap.put("mktCd", marketGroup);
        String latestStoredBaseDt = selectLatestBaseDt(latestMap);
        String displayBaseDt = resolveDisplayBaseDt(inputBaseDt, latestStoredBaseDt, primaryLog, retryLog);

        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("marketGroup", marketGroup);
        resultMap.put("requestedBaseDt", inputBaseDt);
        resultMap.put("baseDt", displayBaseDt);
        resultMap.put("latestStoredBaseDt", latestStoredBaseDt);
        resultMap.put("primary", toBatchStatusMap(primaryLog));
        resultMap.put("retry", toBatchStatusMap(retryLog));
        resultMap.put("hasError", isFail(primaryLog) || isFail(retryLog) ? "Y" : "N");
        resultMap.put("runningYn", isRunning(primaryLog) || isRunning(retryLog) ? "Y" : "N");
        return resultMap;
    }

    public HashMap<String, Object> runBatch(HashMap<String, String> map) throws Exception {
        HashMap<String, String> requestMap = copyMap(map);
        String marketGroup = normalizeMarketGroup(requestMap.get("marketGroup"));
        boolean retryOnly = isYes(requestMap.get("retryOnly"));
        String inputBaseDt = tradeDateService.normalizeInputBaseDate(requestMap.get("baseDt"));
        int fetchDays = parsePositiveInt(requestMap.get("days"), DEFAULT_FETCH_DAYS);
        long requestIntervalMs = parsePositiveLong(requestMap.get("requestIntervalMs"),
                retryOnly ? DEFAULT_RETRY_INTERVAL_MS : DEFAULT_PRIMARY_INTERVAL_MS);
        String batchId = resolveBatchId(marketGroup, retryOnly);
        String sourceBatchId = resolveBatchId(marketGroup, false);
        String execId = resolveExecId(requestMap);
        List<RecSignalDto> marketUniverse = resolveMarketUniverse(requestMap, marketGroup);
        EffectiveBaseDateResult baseDateResult = resolveEffectiveBaseDate(requestMap, marketGroup, inputBaseDt,
                marketUniverse, fetchDays, requestIntervalMs);
        String effectiveBaseDt = baseDateResult.effectiveBaseDt;

        List<RecSignalDto> stockList = resolveTargetStockList(requestMap, marketUniverse, marketGroup, retryOnly,
                effectiveBaseDt, sourceBatchId);
        if (stockList == null) {
            stockList = new ArrayList<RecSignalDto>();
        }

        HashMap<String, Object> startLogMap = new HashMap<String, Object>();
        startLogMap.put("execId", execId);
        startLogMap.put("batchId", batchId);
        startLogMap.put("baseDt", effectiveBaseDt);
        batchExecLogDao.insertBatchExecLog(startLogMap);

        boolean fullRefresh = isFullRefreshRequest(requestMap);

        if (!retryOnly && fullRefresh) {
            HashMap<String, String> deleteMap = new HashMap<String, String>();
            deleteMap.put("mktCd", marketGroup);
            recSignalDao.deleteRecSignalByMarket(deleteMap);
        }

        int totalCnt = stockList.size();
        int successCnt = 0;
        int failCnt = 0;
        int excludedCnt = 0;
        String lastError = null;
        String status = "SUCCESS";
        Set<String> mergedTradeDateSet = new LinkedHashSet<String>();

        try {
            for (int i = 0; i < stockList.size(); i++) {
                if (Thread.currentThread().isInterrupted()) {
                    status = "FAIL";
                    lastError = "BATCH INTERRUPTED";
                    break;
                }

                RecSignalDto stock = stockList.get(i);
                if (stock == null || isBlank(stock.getStkCd())) {
                    continue;
                }

                try {
                    List<DlyPriceDto> priceList = baseDateResult.prefetchedPriceMap.remove(stock.getStkCd());
                    if (priceList == null) {
                        priceList = kisDlyPriceSyncService.fetchAdjustedDailyPrices(
                            stock.getStkCd(),
                            stock.getMktCd(),
                            marketGroup,
                            effectiveBaseDt,
                            fetchDays,
                            requestIntervalMs
                        );
                    }

                    mergeTradeCalendar(priceList, marketGroup, mergedTradeDateSet);

                    RecSignalDto recSignal = buildRecSignal(stock, marketGroup, effectiveBaseDt, priceList);
                    if (recSignal == null) {
                        excludedCnt++;
                        insertItemLog(execId, batchId, effectiveBaseDt, marketGroup, stock, "EXCLUDED", null);
                        continue;
                    }

                    recSignalDao.mergeRecSignal(recSignal);
                    insertItemLog(execId, batchId, effectiveBaseDt, marketGroup, stock, "SUCCESS", null);
                    successCnt++;
                } catch (Exception e) {
                    failCnt++;
                    status = "FAIL";
                    lastError = stock.getStkCd() + " : " + e.getLocalizedMessage();
                    insertItemLog(execId, batchId, effectiveBaseDt, marketGroup, stock, "FAIL",
                            truncateByUtf8Bytes(e.getLocalizedMessage(), ITEM_LOG_ERROR_MAX_BYTES));
                    if (isInterruptedError(e)) {
                        lastError = buildInterruptedBatchMessage(stock.getStkCd(), e);
                        break;
                    }
                }
            }
        } finally {
            HashMap<String, Object> endLogMap = new HashMap<String, Object>();
            endLogMap.put("execId", execId);
            endLogMap.put("status", status);
            endLogMap.put("totalCnt", Integer.valueOf(totalCnt));
            endLogMap.put("successCnt", Integer.valueOf(successCnt));
            endLogMap.put("failCnt", Integer.valueOf(failCnt));
            endLogMap.put("errorMsg", buildBatchMessage(marketGroup, retryOnly, excludedCnt, lastError));
            batchExecLogDao.updateBatchExecLog(endLogMap);
        }

        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("execId", execId);
        resultMap.put("batchId", batchId);
        resultMap.put("marketGroup", marketGroup);
        resultMap.put("retryOnly", retryOnly ? "Y" : "N");
        resultMap.put("inputBaseDt", inputBaseDt);
        resultMap.put("effectiveBaseDt", effectiveBaseDt);
        resultMap.put("days", Integer.valueOf(fetchDays));
        resultMap.put("requestIntervalMs", Long.valueOf(requestIntervalMs));
        resultMap.put("fullRefreshYn", fullRefresh ? "Y" : "N");
        resultMap.put("totalCnt", Integer.valueOf(totalCnt));
        resultMap.put("successCnt", Integer.valueOf(successCnt));
        resultMap.put("failCnt", Integer.valueOf(failCnt));
        resultMap.put("excludedCnt", Integer.valueOf(excludedCnt));
        resultMap.put("mergedTradeDateCnt", Integer.valueOf(mergedTradeDateSet.size()));
        resultMap.put("status", status);
        resultMap.put("message", buildBatchMessage(marketGroup, retryOnly, excludedCnt, lastError));
        return resultMap;
    }

    private List<RecSignalDto> resolveMarketUniverse(HashMap<String, String> requestMap, String marketGroup)
            throws Exception {
        List<RecSignalDto> stockList = stkMasterDao.selectStkMasterList(new HashMap<String, String>());
        List<RecSignalDto> filteredList = new ArrayList<RecSignalDto>();
        String targetStkCd = trim(requestMap.get("stkCd"));

        for (int i = 0; stockList != null && i < stockList.size(); i++) {
            RecSignalDto stock = stockList.get(i);
            if (stock == null || isBlank(stock.getStkCd())) {
                continue;
            }
            if (!matchesMarketGroup(stock.getMktCd(), marketGroup)) {
                continue;
            }
            if (!isBlank(targetStkCd) && !targetStkCd.equals(stock.getStkCd())) {
                continue;
            }
            filteredList.add(stock);
        }
        return filteredList;
    }

    private EffectiveBaseDateResult resolveEffectiveBaseDate(HashMap<String, String> requestMap, String marketGroup,
            String inputBaseDt, List<RecSignalDto> marketUniverse, int fetchDays, long requestIntervalMs) throws Exception {
        EffectiveBaseDateResult result = new EffectiveBaseDateResult();
        String calendarBaseDt = null;
        int marketUniverseCnt = marketUniverse == null ? 0 : marketUniverse.size();

        try {
            calendarBaseDt = tradeDateService.resolveEffectiveBaseDate(inputBaseDt, marketGroup);
        } catch (Exception ignore) {
        }

        if (!isBlank(calendarBaseDt) && calendarBaseDt.compareTo(inputBaseDt) >= 0) {
            result.effectiveBaseDt = calendarBaseDt;
            return result;
        }

        EffectiveBaseDateResult probeResult = probeEffectiveBaseDateFromPrices(marketUniverse, marketGroup,
                inputBaseDt, fetchDays, requestIntervalMs);
        if (!isBlank(probeResult.effectiveBaseDt)) {
            mergePrefetchedTradeCalendar(probeResult.prefetchedPriceMap, marketGroup);
            if (isBlank(calendarBaseDt) || probeResult.effectiveBaseDt.compareTo(calendarBaseDt) > 0) {
                return probeResult;
            }
        }

        if (!isBlank(calendarBaseDt)) {
            result.effectiveBaseDt = calendarBaseDt;
            return result;
        }

        throw new IllegalStateException(buildEffectiveBaseDateErrorMessage(marketGroup, inputBaseDt, marketUniverseCnt,
                probeResult.probeAttemptCount, probeResult.probeErrorMessage));
    }

    private EffectiveBaseDateResult probeEffectiveBaseDateFromPrices(List<RecSignalDto> marketUniverse,
            String marketGroup, String inputBaseDt, int fetchDays, long requestIntervalMs) throws Exception {
        EffectiveBaseDateResult result = new EffectiveBaseDateResult();

        for (int i = 0; marketUniverse != null && i < marketUniverse.size(); i++) {
            RecSignalDto stock = marketUniverse.get(i);
            if (stock == null || isBlank(stock.getStkCd())) {
                continue;
            }

            result.probeAttemptCount++;

            try {
                List<DlyPriceDto> priceList = kisDlyPriceSyncService.fetchAdjustedDailyPrices(
                    stock.getStkCd(),
                    stock.getMktCd(),
                    marketGroup,
                    inputBaseDt,
                    fetchDays,
                    requestIntervalMs
                );
                String fetchedBaseDt = findLatestTradeDate(priceList, inputBaseDt);
                if (!isBlank(fetchedBaseDt)) {
                    result.effectiveBaseDt = fetchedBaseDt;
                    result.prefetchedPriceMap.put(stock.getStkCd(), priceList);
                    return result;
                }
            } catch (Exception e) {
                result.probeErrorMessage = stock.getStkCd() + " : " + safeErrorMessage(e);
            }
        }

        return result;
    }

    private void mergePrefetchedTradeCalendar(Map<String, List<DlyPriceDto>> prefetchedPriceMap, String marketGroup)
            throws Exception {
        if (prefetchedPriceMap == null || prefetchedPriceMap.isEmpty()) {
            return;
        }

        Set<String> mergedTradeDateSet = new LinkedHashSet<String>();
        for (List<DlyPriceDto> priceList : prefetchedPriceMap.values()) {
            mergeTradeCalendar(priceList, marketGroup, mergedTradeDateSet);
        }
    }

    private List<RecSignalDto> resolveTargetStockList(HashMap<String, String> requestMap, List<RecSignalDto> marketUniverse,
            String marketGroup,
            boolean retryOnly, String effectiveBaseDt, String sourceBatchId) throws Exception {
        List<RecSignalDto> filteredList = new ArrayList<RecSignalDto>();
        int limit = parsePositiveInt(requestMap.get("limit"), 0);

        Set<String> retryStockCodeSet = new LinkedHashSet<String>();
        if (retryOnly) {
            HashMap<String, String> retryMap = new HashMap<String, String>();
            retryMap.put("sourceBatchId", sourceBatchId);
            retryMap.put("baseDt", effectiveBaseDt);
            List<RecSignalDto> retryList = batchExecItemLogDao.selectRetryStockList(retryMap);
            for (int i = 0; retryList != null && i < retryList.size(); i++) {
                RecSignalDto retryStock = retryList.get(i);
                if (retryStock != null && !isBlank(retryStock.getStkCd())) {
                    retryStockCodeSet.add(retryStock.getStkCd());
                }
            }
        }

        for (int i = 0; marketUniverse != null && i < marketUniverse.size(); i++) {
            RecSignalDto stock = marketUniverse.get(i);
            if (retryOnly && !retryStockCodeSet.contains(stock.getStkCd())) {
                continue;
            }

            filteredList.add(stock);
            if (limit > 0 && filteredList.size() >= limit) {
                break;
            }
        }
        return filteredList;
    }

    private String findLatestTradeDate(List<DlyPriceDto> priceList, String inputBaseDt) {
        String latestTradeDt = null;
        for (int i = 0; priceList != null && i < priceList.size(); i++) {
            DlyPriceDto dto = priceList.get(i);
            if (dto == null || isBlank(dto.getTradeDt())) {
                continue;
            }
            if (dto.getTradeDt().compareTo(inputBaseDt) > 0) {
                continue;
            }
            if (latestTradeDt == null || dto.getTradeDt().compareTo(latestTradeDt) > 0) {
                latestTradeDt = dto.getTradeDt();
            }
        }
        return latestTradeDt;
    }

    private RecSignalDto buildRecSignal(RecSignalDto stock, String marketGroup, String effectiveBaseDt,
            List<DlyPriceDto> priceList) throws Exception {
        List<DlyPriceDto> validPriceList = filterPriceListUpToBaseDate(priceList, effectiveBaseDt);
        DlyPriceDto currentPriceRow = findCurrentPriceRow(validPriceList, effectiveBaseDt);
        if (currentPriceRow == null || currentPriceRow.getAdjClosePrice() == null
                || currentPriceRow.getAdjClosePrice().doubleValue() <= 0d) {
            return null;
        }

        String effectiveYYYYMM = tradeDateService.toYearMonth(effectiveBaseDt);
        Double monOpenPrice = findMonthOpenPrice(validPriceList, effectiveYYYYMM);
        if (monOpenPrice == null || monOpenPrice.doubleValue() <= 0d) {
            return null;
        }

        List<DlyPriceDto> maSourceList = selectLastValidCloseRows(validPriceList, 240);
        MaResultDto maResult = maCalculateService.calculateMaResult(maSourceList);
        if (!maCalculateService.isValidMaResult(maResult)) {
            return null;
        }

        Double currentPrice = currentPriceRow.getAdjClosePrice();
        Double avgTradeValue20 = calculateAvgTradeValue20(validPriceList);

        RecSignalDto dto = new RecSignalDto();
        dto.setBaseDt(effectiveBaseDt);
        dto.setStkCd(stock.getStkCd());
        dto.setStkNm(stock.getStkNm());
        dto.setMktCd(marketGroup);
        dto.setCurPrice(currentPrice);
        dto.setMonOpenPrice(monOpenPrice);
        dto.setMa5(maResult.getMa5());
        dto.setMa20(maResult.getMa20());
        dto.setMa60(maResult.getMa60());
        dto.setMa120(maResult.getMa120());
        dto.setMa240(maResult.getMa240());
        dto.setGoldenYn(toYn(maCalculateService.isGoldenArray(maResult)));
        dto.setMonUpYn(toYn(maCalculateService.isMonthUp(currentPrice, monOpenPrice)));
        dto.setMonChgRate(maCalculateService.calculateMonthChangeRate(currentPrice, monOpenPrice));
        dto.setTrendStrength(maCalculateService.calculateTrendStrength(maResult));
        dto.setAvgTrdVal20(avgTradeValue20 == null ? Double.valueOf(0d) : avgTradeValue20);
        dto.setRecYn(resolveRecYn(dto));
        dto.setRecGrade(resolveRecGrade(dto));
        dto.setRecReason(buildReason(dto));
        return dto;
    }

    private void mergeTradeCalendar(List<DlyPriceDto> priceList, String marketGroup, Set<String> mergedTradeDateSet)
            throws Exception {
        if (priceList == null || priceList.isEmpty()) {
            return;
        }
        for (int i = 0; i < priceList.size(); i++) {
            DlyPriceDto dto = priceList.get(i);
            if (dto == null || isBlank(dto.getTradeDt())) {
                continue;
            }
            if (mergedTradeDateSet.contains(dto.getTradeDt())) {
                continue;
            }

            HashMap<String, Object> tradeDateMap = new HashMap<String, Object>();
            tradeDateMap.put("tradeDt", dto.getTradeDt());
            tradeDateMap.put("mktCd", marketGroup);
            tradeDateMap.put("tradeYn", "Y");
            tradeDateDao.mergeTradeDate(tradeDateMap);
            mergedTradeDateSet.add(dto.getTradeDt());
        }
    }

    private void insertItemLog(String execId, String batchId, String baseDt, String marketGroup, RecSignalDto stock,
            String itemStatus, String errorMsg) throws Exception {
        HashMap<String, Object> itemLogMap = new HashMap<String, Object>();
        itemLogMap.put("execId", execId);
        itemLogMap.put("batchId", batchId);
        itemLogMap.put("baseDt", baseDt);
        itemLogMap.put("mktCd", marketGroup);
        itemLogMap.put("stkCd", stock.getStkCd());
        itemLogMap.put("stkNm", stock.getStkNm());
        itemLogMap.put("itemStatus", itemStatus);
        itemLogMap.put("errorMsg", errorMsg);
        batchExecItemLogDao.insertBatchExecItemLog(itemLogMap);
    }

    private List<DlyPriceDto> filterPriceListUpToBaseDate(List<DlyPriceDto> priceList, String effectiveBaseDt) {
        List<DlyPriceDto> filteredList = new ArrayList<DlyPriceDto>();
        for (int i = 0; priceList != null && i < priceList.size(); i++) {
            DlyPriceDto dto = priceList.get(i);
            if (dto == null || isBlank(dto.getTradeDt())) {
                continue;
            }
            if (dto.getTradeDt().compareTo(effectiveBaseDt) <= 0) {
                filteredList.add(dto);
            }
        }
        return filteredList;
    }

    private DlyPriceDto findCurrentPriceRow(List<DlyPriceDto> priceList, String effectiveBaseDt) {
        for (int i = 0; priceList != null && i < priceList.size(); i++) {
            DlyPriceDto dto = priceList.get(i);
            if (dto != null && effectiveBaseDt.equals(dto.getTradeDt())) {
                return dto;
            }
        }
        return null;
    }

    private Double findMonthOpenPrice(List<DlyPriceDto> priceList, String effectiveYYYYMM) {
        for (int i = 0; priceList != null && i < priceList.size(); i++) {
            DlyPriceDto dto = priceList.get(i);
            if (dto == null || isBlank(dto.getTradeDt()) || dto.getAdjOpenPrice() == null) {
                continue;
            }
            if (dto.getAdjOpenPrice().doubleValue() <= 0d) {
                continue;
            }
            if (dto.getTradeDt().replace("-", "").startsWith(effectiveYYYYMM)) {
                return dto.getAdjOpenPrice();
            }
        }
        return null;
    }

    private List<DlyPriceDto> selectLastValidCloseRows(List<DlyPriceDto> priceList, int count) {
        List<DlyPriceDto> validList = new ArrayList<DlyPriceDto>();
        for (int i = 0; priceList != null && i < priceList.size(); i++) {
            DlyPriceDto dto = priceList.get(i);
            if (dto == null || dto.getAdjClosePrice() == null) {
                continue;
            }
            if (dto.getAdjClosePrice().doubleValue() <= 0d) {
                continue;
            }
            validList.add(dto);
        }

        if (validList.size() < count) {
            return new ArrayList<DlyPriceDto>();
        }
        return new ArrayList<DlyPriceDto>(validList.subList(validList.size() - count, validList.size()));
    }

    private Double calculateAvgTradeValue20(List<DlyPriceDto> priceList) {
        List<Long> valueList = new ArrayList<Long>();
        for (int i = 0; priceList != null && i < priceList.size(); i++) {
            DlyPriceDto dto = priceList.get(i);
            if (dto == null || dto.getTradeValue() == null) {
                continue;
            }
            valueList.add(dto.getTradeValue());
        }

        if (valueList.isEmpty()) {
            return Double.valueOf(0d);
        }

        int startIndex = Math.max(valueList.size() - 20, 0);
        double sum = 0d;
        int count = 0;
        for (int i = startIndex; i < valueList.size(); i++) {
            sum += valueList.get(i).doubleValue();
            count++;
        }
        return count == 0 ? Double.valueOf(0d) : Double.valueOf(sum / count);
    }

    private String resolveRecYn(RecSignalDto dto) {
        if ("Y".equals(dto.getGoldenYn()) && "Y".equals(dto.getMonUpYn())) {
            return "Y";
        }
        return "N";
    }

    private String resolveRecGrade(RecSignalDto dto) {
        if (!"Y".equals(dto.getRecYn())) {
            return null;
        }

        double monthChangeRate = nvl(dto.getMonChgRate());
        double currentPrice = nvl(dto.getCurPrice());
        double ma5 = nvl(dto.getMa5());

        if (currentPrice > ma5 && monthChangeRate >= 3d) {
            return "A";
        }
        if (currentPrice > ma5 && monthChangeRate > 0d && monthChangeRate < 3d) {
            return "B";
        }
        return "C";
    }

    private String buildReason(RecSignalDto dto) {
        if (!"Y".equals(dto.getRecYn())) {
            List<String> reasons = new ArrayList<String>();
            if (!"Y".equals(dto.getGoldenYn())) {
                reasons.add("정배열 미충족");
            }
            if (!"Y".equals(dto.getMonUpYn())) {
                reasons.add("해당월 상승 미충족");
            }
            if (reasons.isEmpty()) {
                reasons.add("추천 조건 미충족");
            }
            return joinReasons(reasons);
        }

        if ("A".equals(dto.getRecGrade())) {
            return "정배열과 해당월 상승을 만족하고 CURRENT_PRICE > MA5, MONTH_CHANGE_RATE >= 3.0 이므로 A등급";
        }
        if ("B".equals(dto.getRecGrade())) {
            return "정배열과 해당월 상승을 만족하고 CURRENT_PRICE > MA5 이지만 MONTH_CHANGE_RATE < 3.0 이므로 B등급";
        }
        return "정배열과 해당월 상승은 만족하지만 CURRENT_PRICE <= MA5 이므로 C등급";
    }

    private String buildBatchMessage(String marketGroup, boolean retryOnly, int excludedCnt, String lastError) {
        StringBuilder builder = new StringBuilder();
        builder.append(marketGroup).append(' ');
        builder.append(retryOnly ? "retry" : "primary");
        builder.append(" batch 완료");
        builder.append(" / excluded=").append(excludedCnt);
        if (!isBlank(lastError)) {
            builder.append(" / lastError=").append(lastError);
        }
        return truncate(builder.toString(), 390);
    }

    private String buildEffectiveBaseDateErrorMessage(String marketGroup, String inputBaseDt, int marketUniverseCnt,
            int probeAttemptCount, String probeErrorMessage) {
        StringBuilder builder = new StringBuilder();
        builder.append("유효 거래일(EFFECTIVE_BASE_DATE)을 찾을 수 없습니다.");
        builder.append(" marketGroup=").append(normalizeMarketGroup(marketGroup));
        builder.append(", inputBaseDt=").append(inputBaseDt);
        builder.append(", marketUniverseCnt=").append(marketUniverseCnt);
        builder.append(", probeAttemptCnt=").append(probeAttemptCount);
        if (!isBlank(probeErrorMessage)) {
            builder.append(", lastProbeError=").append(probeErrorMessage);
        }
        return truncate(builder.toString(), 390);
    }

    private void normalizeMarketFilter(HashMap<String, String> queryMap) {
        if (queryMap == null) {
            return;
        }

        String requestedMktCd = trim(queryMap.get("mktCd"));
        String requestedMarketGroup = trim(queryMap.get("marketGroup"));
        String requestedMarketFilter = normalizeListingMarketFilter(queryMap.get("marketFilter"));

        if (isBlank(requestedMktCd) && !isBlank(requestedMarketGroup)) {
            requestedMktCd = requestedMarketGroup;
        }

        if (isBlank(requestedMarketFilter)) {
            requestedMarketFilter = promoteLegacyMktCdToFilter(requestedMktCd);
        }

        String marketGroup = normalizeMarketGroup(requestedMktCd);
        if (isBlank(requestedMktCd) && !isBlank(requestedMarketFilter)) {
            marketGroup = marketGroupOfFilter(requestedMarketFilter);
        }

        queryMap.put("mktCd", marketGroup);

        if (isBlank(requestedMarketFilter) || FILTER_ALL.equals(requestedMarketFilter)) {
            queryMap.remove("marketFilter");
            return;
        }

        if (!marketGroup.equals(marketGroupOfFilter(requestedMarketFilter))) {
            queryMap.remove("marketFilter");
            return;
        }

        queryMap.put("marketFilter", requestedMarketFilter);
    }

    private String promoteLegacyMktCdToFilter(String mktCd) {
        String upper = normalizeListingMarketFilter(mktCd);
        if (FILTER_KOSPI.equals(upper) || FILTER_KOSDAQ.equals(upper)
                || FILTER_NASDAQ.equals(upper) || FILTER_NYSE.equals(upper)
                || FILTER_DOW.equals(upper)) {
            return upper;
        }
        return null;
    }

    private String normalizeListingMarketFilter(String marketFilter) {
        if (isBlank(marketFilter)) {
            return null;
        }

        String upper = marketFilter.trim().toUpperCase();
        if (FILTER_KOSPI.equals(upper) || FILTER_KOSDAQ.equals(upper)
                || FILTER_NASDAQ.equals(upper) || FILTER_NYSE.equals(upper)
                || FILTER_DOW.equals(upper) || FILTER_ALL.equals(upper)) {
            return upper;
        }
        return null;
    }

    private String marketGroupOfFilter(String marketFilter) {
        if (FILTER_KOSPI.equals(marketFilter) || FILTER_KOSDAQ.equals(marketFilter)) {
            return MARKET_KR;
        }
        return MARKET_US;
    }

    private String selectLatestBaseDt(HashMap<String, String> queryMap) throws Exception {
        if (queryMap != null && !isBlank(queryMap.get("mktCd"))) {
            return recSignalDao.selectLatestBaseDtByMarket(queryMap);
        }
        return recSignalDao.selectLatestBaseDt();
    }

    private String resolveBatchId(String marketGroup, boolean retryOnly) {
        StringBuilder builder = new StringBuilder("BATCH_REC_SIGNAL_");
        builder.append(normalizeMarketGroup(marketGroup));
        builder.append(retryOnly ? "_RETRY" : "_PRIMARY");
        return builder.toString();
    }

    private HashMap<String, Object> selectLatestBatchExecLog(String batchId, String baseDt) throws Exception {
        HashMap<String, String> queryMap = new HashMap<String, String>();
        queryMap.put("batchId", batchId);
        if (!isBlank(baseDt)) {
            queryMap.put("baseDt", baseDt);
        }
        return batchExecLogDao.selectLatestBatchExecLog(queryMap);
    }

    private HashMap<String, Object> toBatchStatusMap(HashMap<String, Object> rawMap) {
        HashMap<String, Object> statusMap = new HashMap<String, Object>();
        if (rawMap == null || rawMap.isEmpty()) {
            statusMap.put("existsYn", "N");
            statusMap.put("status", "NONE");
            statusMap.put("statusLabel", "실행 이력 없음");
            return statusMap;
        }

        String status = readString(rawMap, "status");
        statusMap.put("execId", readString(rawMap, "execId"));
        statusMap.put("batchId", readString(rawMap, "batchId"));
        statusMap.put("baseDt", readString(rawMap, "baseDt"));
        statusMap.put("startAt", readString(rawMap, "startAt"));
        statusMap.put("endAt", readString(rawMap, "endAt"));
        statusMap.put("status", status);
        statusMap.put("totalCnt", readInt(rawMap, "totalCnt"));
        statusMap.put("successCnt", readInt(rawMap, "successCnt"));
        statusMap.put("failCnt", readInt(rawMap, "failCnt"));
        statusMap.put("errorMsg", readString(rawMap, "errorMsg"));
        statusMap.put("existsYn", "Y");
        statusMap.put("runningYn", "RUNNING".equalsIgnoreCase(status) ? "Y" : "N");
        statusMap.put("statusLabel", toStatusLabel(status));
        return statusMap;
    }

    private String resolveDisplayBaseDt(String inputBaseDt, String latestStoredBaseDt,
            HashMap<String, Object> primaryLog, HashMap<String, Object> retryLog) {
        if (!isBlank(inputBaseDt)) {
            return inputBaseDt;
        }

        String candidate = latestStoredBaseDt;
        candidate = maxDate(candidate, readString(primaryLog, "baseDt"));
        candidate = maxDate(candidate, readString(retryLog, "baseDt"));
        return candidate;
    }

    private String maxDate(String left, String right) {
        if (isBlank(left)) {
            return right;
        }
        if (isBlank(right)) {
            return left;
        }
        return left.compareTo(right) >= 0 ? left : right;
    }

    private String readString(HashMap<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }

        Object value = map.get(key);
        if (value == null) {
            value = map.get(key.toUpperCase());
        }
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private int readInt(HashMap<String, Object> map, String key) {
        String value = readString(map, key);
        if (isBlank(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignore) {
            return 0;
        }
    }

    private String toStatusLabel(String status) {
        if ("RUNNING".equalsIgnoreCase(status)) {
            return "실행 중";
        }
        if ("SUCCESS".equalsIgnoreCase(status)) {
            return "성공";
        }
        if ("FAIL".equalsIgnoreCase(status)) {
            return "실패";
        }
        return "실행 이력 없음";
    }

    private boolean isFail(HashMap<String, Object> map) {
        return "FAIL".equalsIgnoreCase(readString(map, "status"));
    }

    private boolean isRunning(HashMap<String, Object> map) {
        return "RUNNING".equalsIgnoreCase(readString(map, "status"));
    }

    private boolean isFullRefreshRequest(HashMap<String, String> requestMap) {
        if (requestMap == null) {
            return true;
        }
        if (!isBlank(requestMap.get("stkCd"))) {
            return false;
        }
        return parsePositiveInt(requestMap.get("limit"), 0) <= 0;
    }

    private boolean matchesMarketGroup(String mktCd, String marketGroup) {
        if (MARKET_US.equals(normalizeMarketGroup(marketGroup))) {
            return MARKET_US.equals(resolveMarketGroup(mktCd));
        }
        return MARKET_KR.equals(resolveMarketGroup(mktCd));
    }

    private String resolveMarketGroup(String mktCd) {
        String value = trim(mktCd);
        if (isBlank(value)) {
            return MARKET_KR;
        }
        String upper = value.toUpperCase();
        if ("KR".equals(upper) || upper.startsWith("KOS") || upper.startsWith("KQ")) {
            return MARKET_KR;
        }
        return MARKET_US;
    }

    private String normalizeMarketGroup(String marketGroup) {
        if (marketGroup == null || marketGroup.trim().length() == 0) {
            return MARKET_KR;
        }
        return resolveMarketGroup(marketGroup);
    }

    private String joinReasons(List<String> reasons) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < reasons.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(reasons.get(i));
        }
        return builder.toString();
    }

    private HashMap<String, String> copyMap(HashMap<String, String> map) {
        HashMap<String, String> result = new HashMap<String, String>();
        if (map != null) {
            result.putAll(map);
        }
        return result;
    }

    private String resolveExecId(HashMap<String, String> requestMap) {
        String requestedExecId = requestMap == null ? null : trim(requestMap.get("execId"));
        if (!isBlank(requestedExecId)) {
            return requestedExecId;
        }
        return UUID.randomUUID().toString();
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private long parsePositiveLong(String value, long defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            return parsed > 0L ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean isYes(String value) {
        return value != null && "Y".equalsIgnoreCase(value.trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String safeErrorMessage(Exception e) {
        if (e == null) {
            return null;
        }
        if (!isBlank(e.getLocalizedMessage())) {
            return e.getLocalizedMessage();
        }
        return e.getClass().getSimpleName();
    }

    private boolean isInterruptedError(Exception e) {
        if (Thread.currentThread().isInterrupted()) {
            return true;
        }

        Throwable current = e;
        while (current != null) {
            if (current instanceof InterruptedException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String buildInterruptedBatchMessage(String stkCd, Exception e) {
        StringBuilder builder = new StringBuilder("배치가 인터럽트로 중단되었습니다");
        if (!isBlank(stkCd)) {
            builder.append(" / stkCd=").append(stkCd);
        }
        String errorMessage = safeErrorMessage(e);
        if (!isBlank(errorMessage)) {
            builder.append(" / error=").append(errorMessage);
        }
        return truncate(builder.toString(), 390);
    }

    private double nvl(Double value) {
        return value == null ? 0d : value.doubleValue();
    }

    private String toYn(boolean flag) {
        return flag ? "Y" : "N";
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String truncateByUtf8Bytes(String value, int maxBytes) {
        if (value == null) {
            return null;
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return value;
        }

        int endIndex = value.length();
        while (endIndex > 0) {
            String candidate = value.substring(0, endIndex);
            if (candidate.getBytes(StandardCharsets.UTF_8).length <= maxBytes) {
                return candidate;
            }
            endIndex--;
        }
        return "";
    }
}
