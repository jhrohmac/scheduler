package com.scheduler.comm.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.ibatis.logging.LogFactory;

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
		LogFactory.useStdOutLogging();

		System.err.println("==========property Start filePropertySet================");
		Properties prop = new Properties();
		loadProperties(prop, sce.getServletContext());
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

    private void loadProperties(Properties prop, ServletContext servletContext) {
        File propertyFile = resolvePropertyFile(servletContext,
                "/WEB-INF/resources/config/mybatis/oracle/oracle.db.properties",
                "config/mybatis/oracle/oracle.db.properties");
        if (propertyFile != null && propertyFile.isFile()) {
            try (InputStream in = new java.io.FileInputStream(propertyFile)) {
                prop.load(in);
                return;
            } catch (FileNotFoundException e) {
                System.err.println("filePropertySet() Method : " + e.getMessage());
            } catch (IOException e) {
                System.err.println("filePropertySet() Method : " + e.getMessage());
            }
        }

        try (InputStream in = InitializedSetting.class.getClassLoader()
                .getResourceAsStream("config/mybatis/oracle/oracle.db.properties")) {
            if (in != null) {
                prop.load(in);
            }
        } catch (IOException e) {
            System.err.println("filePropertySet() Method : " + e.getMessage());
        }
    }

    private File resolvePropertyFile(ServletContext servletContext, String webInfPath, String classpathResource) {
        if (servletContext != null) {
            String realPath = servletContext.getRealPath(webInfPath);
            if (realPath != null) {
                File realFile = new File(realPath);
                if (realFile.isFile()) {
                    return realFile;
                }
            }
        }

        File classpathFile = resolveClasspathFile(classpathResource);
        if (classpathFile != null && classpathFile.isFile()) {
            return classpathFile;
        }

        String osPath = System.getProperty("os.name").indexOf("Windows") > -1 ? "wtpwebapps" : "webapps";
        File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();
        return new File(catalinaBase, osPath + "/scheduler/WEB-INF/resources/config/mybatis/oracle/oracle.db.properties");
    }

    private File resolveClasspathFile(String classpathResource) {
        try {
            java.net.URL url = InitializedSetting.class.getClassLoader().getResource(classpathResource);
            if (url != null && "file".equals(url.getProtocol())) {
                return new File(url.toURI());
            }
        } catch (Exception ignore) {
        }
        return null;
    }
    
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// Connection 해제 (null-safe)
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			System.err.println("InitializedSetting.contextDestroyed() DB close error: " + e.getMessage());
		} finally {
			conn = null;
		}
	}
}
