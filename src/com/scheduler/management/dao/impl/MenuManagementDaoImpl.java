
package com.scheduler.management.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.comm.vo.MenuVo;
import com.scheduler.management.dao.MenuManagementDao;

public class MenuManagementDaoImpl extends SqlSessionDaoSupport implements MenuManagementDao
{
	private static final String NS = "sql.MenuManage.";
	
    public int selectOneCnt(HashMap<String, String> param) throws Exception {
        int cnt = 0;
        try {
            cnt = (int)this.getSqlSession().selectOne(NS+"selectOneCnt", param);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
        return cnt;
    }
    
    public MenuVo selectMenuSeq(HashMap<String, String> param) throws Exception {
        MenuVo menuList = new MenuVo();
        try {
            menuList = getSqlSession().selectOne(NS+"selectMenuSeq", param);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
        return menuList;
    }

    public MenuVo selectMenuBySeq(HashMap<String, String> param) throws Exception {
        MenuVo menu = null;
        try {
            menu = getSqlSession().selectOne(NS + "selectMenuBySeq", param);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
        return menu;
    }
    
    public List<MenuVo> selectMenuList(HashMap<String, String> map) throws Exception {
        List<MenuVo> menuList = null;
        try {
            menuList = getSqlSession().selectList(NS+"selectMenuList", map);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
        return menuList;
    }

    
    @Override
	public List<?> selectMenuGroupList(HashMap<String, String> map) throws Exception {
    	
    	List<?> list = null;
    	        		
		try{
			list = getSqlSession().selectList(NS+"selectMenuGroupList");
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		return list;
		
	}
    
    @Override
    public List<?> selectMenuAccessUsers(HashMap<String, String> map) throws Exception {
    	
    	List<?> list = null;
    	
    	try{
    		list = getSqlSession().selectList(NS+"selectMenuAccessUsers", map);
    	}catch(Exception e){
    		e.printStackTrace();
    		throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
    	}
    	return list;
    	
    }
    
    public int saveMenuOrder(HashMap<String, String> map) throws Exception {
        int result = 0;
        try {
        	map.put("in_newOrder", String.valueOf(Integer.parseInt(map.get("in_newOrder"))+1));
        	map.put("in_oldOrder", String.valueOf(Integer.parseInt(map.get("in_oldOrder"))+1));
        	
        	//변경되는 Order Update 후 나머지 Order 수정
        	result = getSqlSession().update(NS+"saveMenuOrder", map);
        	
        	List<MenuVo> list = new ArrayList<MenuVo>();
			list = getSqlSession().selectList(NS+"selectMenuSort", map);
			
			for(int i=0; i<list.size(); i++) {
				HashMap<String, String> seqMap = new HashMap<String, String>();
				int num = i+1;
				seqMap.put("in_menuSeq", map.get("in_menuSeq"));
				seqMap.put("in_userId", map.get("in_userId"));
				seqMap.put("in_newOrder", String.valueOf(num));
				seqMap.put("in_parentId", map.get("in_parentId"));
				seqMap.put("in_sortOrder", list.get(i).getSort_order());
				if(!map.get("in_newOrder").equals(list.get(i).getSort_order())){
					result = getSqlSession().update(NS+"saveMenuOrder", seqMap);
				}else{
					num++;
				}
			}
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
        return result;
    }
    
    @Override
    public List<?> checkMenuAccessUsers(HashMap<String, String> map) throws Exception {
    	
    	List<?> list = null;
    	try {
    		String in_menuSeq = map.get("in_menuSeq");
    		String[] user_list = map.get("in_userList").split(",");
    		
			if(user_list.length > 0){
				for (int i = 0; i < user_list.length; i++) {
					HashMap<String, String> param = new HashMap<String, String>(); 
					System.out.println(user_list[i]);
					param.put("in_menuSeq",	in_menuSeq);
					param.put("in_userId",	user_list[i]);
					list = getSqlSession().selectOne(NS+"checkMenuAccessUsers", map);
				}
			}
    	}catch (Exception e) {
    		e.printStackTrace();
    		throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
    	}
    	return list;
    }
    
    /**
     * 메뉴 권한 사용자 등록
     */
    public int saveMenuAccessUsers(HashMap<String, String> map) throws Exception {
    	
    	int result = 0;
    	try {
    		String in_menuSeq = map.get("in_menuSeq");
    		String in_accessAll = map.get("in_accessAll");
    		String pop_user_List = map.get("pop_user_List");
    		getSqlSession().delete(NS+"deleteMenuAccessUsers", map);
    		
    		if(pop_user_List.equals("")) {
    			return result;
    		}
    		String[] user_list = pop_user_List.split(",");
    		List<MenuVo> chListVo = new ArrayList<MenuVo>();
    		if(in_accessAll.equals("Y")){
        		chListVo = getSqlSession().selectList(NS+"selectChildrenMenuList", map);
        		MenuVo addVo = new MenuVo();
        		addVo.setMenu_seq(in_menuSeq);
        		chListVo.add(addVo);
    			if(user_list.length > 0){
    				for (int i = 0; i < user_list.length; i++) {
    					for(MenuVo vo : chListVo) {
    						HashMap<String, String> param = new HashMap<String, String>();
    						param.put("in_menuSeq",	vo.getMenu_seq());
        					param.put("in_userId",	user_list[i]);
        					
        					getSqlSession().delete(NS+"deleteMenuAccessUsers", param);
        					getSqlSession().insert(NS+"saveMenuAccessUsers", param);
    					}
    				}
    			}
    		}else {
    			if(user_list.length > 0){
    				for (int i = 0; i < user_list.length; i++) {
    					HashMap<String, String> param = new HashMap<String, String>();
    					param.put("in_menuSeq",	in_menuSeq);
    					param.put("in_userId",	user_list[i]);
    					getSqlSession().delete(NS+"deleteMenuAccessUsers", param);
    					getSqlSession().insert(NS+"saveMenuAccessUsers", param);
    				}
    			}
    		}
    	}catch (Exception e) {
    		e.printStackTrace();
    		throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
    	}
    	return result;
    }
    
    public int save(HashMap<String, String> map) throws Exception {
    	
    	int result = 0;
    	
    	try {
    		String eventDiv = map.get("in_eventDiv");
    		String menuId = map.get("in_menuId");
    		String currentMenuId = "";
    		if (menuId != null) {
    			map.put("in_menuId", menuId.trim());
    		}

    		if ("update".equals(eventDiv)) {
    			MenuVo currentMenu = this.selectMenuBySeq(map);
    			if (currentMenu == null) {
    				throw new RuntimeException("수정 대상 메뉴를 찾을 수 없습니다.");
    			}
    			currentMenuId = currentMenu.getMenu_id() == null ? "" : currentMenu.getMenu_id().trim();
    		}

    		if ("insert".equals(eventDiv) || ("update".equals(eventDiv) && !map.get("in_menuId").equals(currentMenuId))) {
    			int duplicateCnt = (int) getSqlSession().selectOne(NS + "selectOneCnt", map);
    			if (duplicateCnt > 0) {
    				throw new RuntimeException("이미 사용 중인 메뉴 ID입니다. 메뉴 ID를 변경해주세요.");
    			}
    		}
    		
    		//신규 메뉴 저장
    		if ("insert".equals(eventDiv)) {
    			result = getSqlSession().insert(NS+"saveMenu", map);
    		}
    		
    		// 메뉴 업데이트
    		if ("update".equals(eventDiv)) {
    			result = getSqlSession().insert(NS+"updateMenu", map);
    		}
    		
    	}catch (Exception e) {
    		e.printStackTrace();
    		getSqlSession().rollback();
    		throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
    	}
    	return result;
    }
    
    public int delete(HashMap<String, String> map) throws Exception {
        int result = 0;
        try {
            result = getSqlSession().delete(NS+"deleteMenu", map);
        }
        catch (Exception e) {
        	getSqlSession().rollback();
        	e.printStackTrace();
        	throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
        return result;
    }

    // ===== [TREE VIEW INTEGRATION] =====
    @Override
    public List<?> selectTreeViewUsers(HashMap<String, String> map) throws Exception {
        try { return getSqlSession().selectList(NS + "selectTreeViewUsers", map); }
        catch (Exception e) { e.printStackTrace(); throw new RuntimeException(this.getClass().getName() + e.getMessage(), e); }
    }

    @Override
    public List<?> selectAllMenus(HashMap<String, String> map) throws Exception {
        try { return getSqlSession().selectList(NS + "selectAllMenus", map); }
        catch (Exception e) { e.printStackTrace(); throw new RuntimeException(this.getClass().getName() + e.getMessage(), e); }
    }

    @Override
    public List<?> selectUserMenuAuth(HashMap<String, String> map) throws Exception {
        try { return getSqlSession().selectList(NS + "selectUserMenuAuth", map); }
        catch (Exception e) { e.printStackTrace(); throw new RuntimeException(this.getClass().getName() + e.getMessage(), e); }
    }

    @Override
    public int deleteUserMenuAuth(HashMap<String, String> map) throws Exception {
        try { return getSqlSession().delete(NS + "deleteUserMenuAuth", map); }
        catch (Exception e) { e.printStackTrace(); throw new RuntimeException(this.getClass().getName() + e.getMessage(), e); }
    }

    @Override
    public int insertUserMenuAuth(HashMap<String, String> map) throws Exception {
        try { return getSqlSession().insert(NS + "insertUserMenuAuth", map); }
        catch (Exception e) { e.printStackTrace(); throw new RuntimeException(this.getClass().getName() + e.getMessage(), e); }
    }
}
