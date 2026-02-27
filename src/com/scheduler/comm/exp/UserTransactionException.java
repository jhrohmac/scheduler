package com.scheduler.comm.exp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.comm.dao.impl.ItemCodeDaoImpl;


public class UserTransactionException extends SchedulerException {
	
	private static final Logger logger = LoggerFactory.getLogger(ItemCodeDaoImpl.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9055750256427659744L;

	public UserTransactionException(String code, String message){
		super(code, message);
		logger.error(code + " / " + message);
	}
}
