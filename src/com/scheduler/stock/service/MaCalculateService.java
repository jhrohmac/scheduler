package com.scheduler.stock.service;

import java.util.ArrayList;
import java.util.List;

import com.scheduler.stock.dto.DlyPriceDto;
import com.scheduler.stock.dto.MaResultDto;

public class MaCalculateService {

    public MaResultDto calculateMaResult(List<DlyPriceDto> priceList) {
        List<Double> closePriceList = new ArrayList<Double>();
        double minPrice = Double.MAX_VALUE;

        if (priceList != null) {
            for (int i = 0; i < priceList.size(); i++) {
                DlyPriceDto dto = priceList.get(i);
                if (dto == null || dto.getAdjClosePrice() == null) {
                    continue;
                }
                double closePrice = dto.getAdjClosePrice().doubleValue();
                if (closePrice <= 0d) {
                    continue;
                }
                closePriceList.add(Double.valueOf(closePrice));
                if (closePrice < minPrice) {
                    minPrice = closePrice;
                }
            }
        }

        MaResultDto result = new MaResultDto();
        result.setCntValid(Integer.valueOf(closePriceList.size()));
        result.setMinPrice(closePriceList.isEmpty() ? null : Double.valueOf(minPrice));
        result.setMa5(averageOfLast(closePriceList, 5));
        result.setMa20(averageOfLast(closePriceList, 20));
        result.setMa60(averageOfLast(closePriceList, 60));
        result.setMa120(averageOfLast(closePriceList, 120));
        result.setMa240(averageOfLast(closePriceList, 240));
        return result;
    }

    public boolean isValidMaResult(MaResultDto maResult) {
        if (maResult == null) {
            return false;
        }
        if (maResult.getCntValid() == null || maResult.getCntValid().intValue() != 240) {
            return false;
        }
        if (maResult.getMinPrice() == null || maResult.getMinPrice().doubleValue() <= 0d) {
            return false;
        }
        return maResult.getMa5() != null
            && maResult.getMa20() != null
            && maResult.getMa60() != null
            && maResult.getMa120() != null
            && maResult.getMa240() != null;
    }

    public boolean isGoldenArray(MaResultDto maResult) {
        return maResult != null
            && gt(maResult.getMa5(), maResult.getMa20())
            && gt(maResult.getMa20(), maResult.getMa60())
            && gt(maResult.getMa60(), maResult.getMa120())
            && gt(maResult.getMa120(), maResult.getMa240());
    }

    public boolean isMonthUp(Double currentPrice, Double monthOpenPrice) {
        return currentPrice != null
            && monthOpenPrice != null
            && currentPrice.doubleValue() > monthOpenPrice.doubleValue();
    }

    public Double calculateMonthChangeRate(Double currentPrice, Double monthOpenPrice) {
        if (currentPrice == null || monthOpenPrice == null || monthOpenPrice.doubleValue() <= 0d) {
            return null;
        }
        return ((currentPrice.doubleValue() - monthOpenPrice.doubleValue()) / monthOpenPrice.doubleValue()) * 100d;
    }

    public Double calculateTrendStrength(MaResultDto maResult) {
        if (!isValidMaResult(maResult)) {
            return null;
        }
        return ratio(maResult.getMa5(), maResult.getMa20())
            + ratio(maResult.getMa20(), maResult.getMa60())
            + ratio(maResult.getMa60(), maResult.getMa120())
            + ratio(maResult.getMa120(), maResult.getMa240());
    }

    private boolean gt(Double left, Double right) {
        return left != null && right != null && left.doubleValue() > right.doubleValue();
    }

    private double ratio(Double upper, Double lower) {
        if (upper == null || lower == null || lower.doubleValue() == 0d) {
            throw new IllegalArgumentException("추세 강도 계산 중 분모가 0 이거나 NULL 입니다.");
        }
        return ((upper.doubleValue() - lower.doubleValue()) / lower.doubleValue()) * 100d;
    }

    private Double averageOfLast(List<Double> values, int count) {
        if (values == null || values.size() < count || count <= 0) {
            return null;
        }

        double sum = 0d;
        for (int i = values.size() - count; i < values.size(); i++) {
            sum += values.get(i).doubleValue();
        }
        return Double.valueOf(sum / count);
    }
}
