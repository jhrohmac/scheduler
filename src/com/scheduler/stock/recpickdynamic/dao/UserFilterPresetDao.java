package com.scheduler.stock.recpickdynamic.dao;

import java.util.HashMap;

import com.scheduler.stock.recpickdynamic.vo.UserFilterPresetVo;

public interface UserFilterPresetDao {

    /** 단건 조회 (USER_ID + PRESET_TYPE) */
    UserFilterPresetVo selectPreset(HashMap<String, Object> map) throws Exception;

    /** MERGE 저장 (있으면 UPDATE, 없으면 INSERT) */
    int mergePreset(HashMap<String, Object> map) throws Exception;

    /** 삭제 */
    int deletePreset(HashMap<String, Object> map) throws Exception;
}
