package com.scheduler.comm.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.util.TimeUtil;
import com.scheduler.comm.vo.InitializedSettingVo;

@WebListener
public class InitializedSetting implements ServletContextListener {
	static Connection conn;
	public static List<InitializedSettingVo> initList = null;
	
	public static void getInitialized(Properties prop){
		
		String driver = StringUtil.nvl(prop.getProperty("jdbc.driverClassName"));
		String url = StringUtil.nvl(prop.getProperty("jdbc.url"));
		String user = StringUtil.nvl(prop.getProperty("jdbc.username"));
		String password = StringUtil.nvl(prop.getProperty("jdbc.password"));
		
		String run_sql = "  SELECT SYS_CFG_ID, SYS_CFG_VALUE FROM TB_M_SYSCFG   WHERE CLASSIFICATION = 'UI' ORDER BY SYS_CFG_ID";
		int result_size = 0;

		if(driver == ""){
			System.err.println("driver가 잘못되었습니다.");
		}
		if(url == ""){
			System.err.println("url가 잘못되었습니다.");
		}
		if(user == ""){
			System.err.println("user가 잘못되었습니다.");
		}
		if(password == ""){
			System.err.println("password가 잘못되었습니다.");
		}
		
		HashMap<String, String> map =new HashMap<String, String>();
		map.put("driver", driver.trim());
		map.put("url", url.trim());
		map.put("user", user.trim());
		map.put("password", password.trim());
		map.put("run_sql", run_sql.trim());
		
		try {
			PreparedStatement pstmt = null;
			initList = new ArrayList<InitializedSettingVo>();

			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);

			pstmt = conn.prepareStatement(run_sql);
			ResultSet rs = pstmt.executeQuery();

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCnt = rsmd.getColumnCount(); // 컬럼의 수
			System.out.println("======columnCnt=================" + columnCnt);
			ArrayList<String> collist = new ArrayList<String>();
			if (rs.next()) {
				for (int i = 1; i <= columnCnt; i++) {
					collist.add(rsmd.getColumnName(i));
					/*
					 * InitializedSettingVo itemVo = new InitializedSettingVo();
					 * itemVo.setCode(rsmd.getColumnName(i));
					 * itemVo.setValue(rs.getString(rsmd.getColumnName(i)));
					 * initList.add(itemVo);
					 */
				}
			}
			int nst = 0;
			while (rs.next()) {
				nst++;
				String code = rs.getString(collist.get(0));
				String value = rs.getString(collist.get(1));
				InitializedSettingVo itemVo = new InitializedSettingVo();
				itemVo.setCode(code);
				itemVo.setValue(value);
				initList.add(itemVo);
			}

			pstmt.close();
			conn.close();

			try {
				if (pstmt != null)
					pstmt.close();
			} catch (Exception e) {
			}

			result_size = initList.size();
			System.out.println("======result_size=================");
			System.out.println(result_size);

		} catch (Exception e) {
			e.getMessage();
		}
	}
	
    @Override
    public void contextInitialized(ServletContextEvent sce) {

		System.err.println("==========property Start filePropertySet================");
		String os_path ="webapps";
		String filepath ="file.os.order.root";
        // 운영체제 구분 (windows 가 아니면 무조건 linux 로 판단)
		 if (System.getProperty("os.name").indexOf("Windows") > -1) {
			 os_path = "wtpwebapps";
			 filepath ="file.os.win.root";
		 }
		 
		Properties prop = new Properties();
		FileReader resources= null;
		
    	File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
    	File propertyFile = new File( catalinaBase, os_path+"/scheduler/WEB-INF/resources/config/mybatis/oracle/oracle.db.properties");
			try {
				resources= new FileReader(propertyFile);
				prop.load(resources);
			} catch (FileNotFoundException e) {
				System.err.println("filePropertySet() Method : "+e.getMessage());
			} catch (IOException e) {
				System.err.println("filePropertySet() Method : "+e.getMessage());
			}
			// Root Info
			System.out.println(prop.getProperty("jdbc.driverClassName"));
		// Root Info
			System.out.println(prop.getProperty("jdbc.url"));
		// Root Info
			System.out.println(prop.getProperty("jdbc.username"));
		// Root Info
			System.out.println(prop.getProperty("jdbc.password"));
			getInitialized(prop);
			TimeUtil.getCountry();
    }
    
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		 // Connection 해제
        try {
            conn.close();
        } catch (SQLException e) { }
	}
}
