package com.scheduler.comm.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.json.JSONObject;

import com.scheduler.comm.dao.ItemCodeDao;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.vo.ItemCodeVo;
import com.scheduler.util.handler.ResponseHandler;
import com.scheduler.util.handler.ResultMsg;
import com.scheduler.util.handler.SessionValidator;

@Controller
public class ItemCodeController {

	public ItemCodeDao itemCodeDao;
	
	public ItemCodeDao getItemCodeDao() {
		return itemCodeDao;
	}

	public void setItemCodeDao(ItemCodeDao itemCodeDao) {
		this.itemCodeDao = itemCodeDao;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping({"/comm/itemCode.do" })
    public ModelAndView c_getItemCode(HttpServletRequest req, HttpServletResponse res){
		
		String itemObj = StringUtil.nvl(req.getParameter("itemObj"));
		
		JSONObject objects = new JSONObject(itemObj);
		
		System.out.println(objects);
		/* Request Parameter Set */
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("itemName",		StringUtil.nvl((String) objects.get("itemName")));
		map.put("itemALL",	StringUtil.nvl((String) objects.get("itemALL")));
		
		try {			
			List<ItemCodeVo> list = null;
			list = (List<ItemCodeVo>) itemCodeDao.selectItemCode(map);
			HashMap<String, Object> resParams = new HashMap<String, Object>();
			resParams.put("itemList", list);
			resParams.put("view_Name", "comm/code/item_code");
			
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_COMPLITE, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}
}