package com.scheduler.management.dao.impl;

import java.util.HashMap;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.management.dao.ProfileDao;
import com.scheduler.management.vo.UserVo;

/**
 * ProfileDaoImpl
 * - namespace: sql.UserManage (기존 사용자 쿼리 재사용)
 *   · selectUserInfo
 *   · updateUser
 *   · updatePw
 *   · insertUserHistory
 */
public class ProfileDaoImpl extends SqlSessionDaoSupport implements ProfileDao {

    private static final Logger logger = LoggerFactory.getLogger(ProfileDaoImpl.class);
    private static final String NS = "sql.Profile.";

    @Override
    public UserVo selectMyProfile(HashMap<String, String> param) throws Exception {
        try {
            return (UserVo) getSqlSession().selectOne(NS+"selectUserInfo", param);
        } catch (Exception e) {
            logger.error("selectMyProfile error", e);
            throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
    }

    @Override
    public int updateMyProfile(HashMap<String, String> param) throws Exception {
    	System.out.println(param);
        try {
        	
            int r = getSqlSession().update(NS+"updateUser", param);
            // 이력
            getSqlSession().insert(NS+"insertUserHistory", param);
            return r;
        } catch (Exception e) {
            logger.error("updateMyProfile error", e);
            throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
    }

    @Override
    public int updateMyPassword(HashMap<String, String> param) throws Exception {
        try {
            return getSqlSession().update(NS+"updatePw", param);
        } catch (Exception e) {
            logger.error("updateMyPassword error", e);
            throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
    }
}
