package com.scheduler.stock.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.scheduler.stock.dao.RecPickDao;
import com.scheduler.stock.dto.RecProbabilityDto;

public class RecProbabilityService {

    private RecPickDao recPickDao;

    public void setRecPickDao(RecPickDao recPickDao) {
        this.recPickDao = recPickDao;
    }

    public List<RecProbabilityDto> selectProbability(HashMap<String, String> map) throws Exception {
        List<RecProbabilityDto> list = recPickDao.selectProbabilityStats(map);
        return list == null ? new ArrayList<RecProbabilityDto>() : list;
    }
}
