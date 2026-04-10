package com.scheduler.comm.dao;

import java.util.HashMap;
import java.util.List;

public interface ItemCodeDao{
	
    public List<?> selectItemCode(HashMap<String, String> params) throws Exception;
}