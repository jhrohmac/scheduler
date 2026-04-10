package com.scheduler.management.dao;

import java.util.HashMap;

import com.scheduler.management.vo.UserVo;

/**
 * Profile 전용 DAO
 * - 실제 쿼리는 기존 oracle_UserManage.xml(sql.UserManage.*) 재사용
 */
public interface ProfileDao {
    UserVo selectMyProfile(HashMap<String, String> param) throws Exception;
    int updateMyProfile(HashMap<String, String> param) throws Exception;
    int updateMyPassword(HashMap<String, String> param) throws Exception;
}
