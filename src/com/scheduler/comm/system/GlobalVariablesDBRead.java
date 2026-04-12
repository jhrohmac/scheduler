package com.scheduler.comm.system;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

public class GlobalVariablesDBRead implements ServletContextListener {
	
	public static enum OSNAME {AIX, HP, WIN, Windows, Sun, Linux, Unknown};
	public static String OS_NAME = null;
	private static Logger logger = Logger.getLogger(GlobalVariablesDBRead.class.getName());
	
	/********************************************************************************
	 * MOM Info
	 ********************************************************************************/
	public static String EMS_URL = ""; 						// 점검대상 EMS 접속 URL 정보
	public static String EMS_USER = ""; 					// 점검대상 EMS 접속 User 정보
	public static String EMS_PASS = ""; 					// 점검대상 EMS 접속 Pass 정보
	
	/********************************************************************************
	 * Hawk Domain Info for Monitoring MicroAgent and EAI Engine
	 ********************************************************************************/
	public static String HAWK_DOMAIN_HOSTNAME = ""; 		// HAWK 도메인의 호스트 명
	public static String HAWK_DOMAIN_NAME = ""; 			// HAWK 도메인 명
	public static String HAWK_DOMAIN_RVDAEMON = ""; 		// HAWK 도메인 RVDAEMON 정보
	public static String HAWK_DOMAIN_RVNETWORK = ""; 		// HAWK 도메인 RVNETWORK 정보
	public static String HAWK_DOMAIN_RVSERVICE = ""; 		// HAWK 도메인 RVSERVICE 정보
	public static int HAWK_DOMAIN_MINNUM = 1; 				// HAWK 도메인의 수
	
	/********************************************************************************
	 * File Save root
	 ********************************************************************************/
	public static String DATA_FILES_DIV = "";
	public static String PRODUCTS_LIST_ROOT = "";
	public static String APPLICATION_LOG_ROOT = "";
	public static String PRODUCTS_LOG_ROOT = "";
	
	/********************************************************************************
	 * EAI Log DB TableSpace Check
	 ********************************************************************************/
	public static String TABLESPACE_DB_DRIVER = ""; 	// TABLESPACE DRIVER
	public static String TABLESPACE_DB_URL = ""; 		// TABLESPACE URL
	public static String TABLESPACE_DB_ID = ""; 		// TABLESPACE ID
	public static String TABLESPACE_DB_PASS = ""; 		// TABLESPACE PASSWORD
	
	/********************************************************************************
	 * EAI SERVER SETTING
	 ********************************************************************************/	
	public static String EAI_SERVER_LIST = ""; 			// EAI SERVER LIST
	public static String EAI_SERVER_REMOTE = ""; 		// EAI SERVER REMOTE
	public static String EAI_SERVER_PROTOCOL = ""; 		// EAI SERVER PROTOCOL
	public static int EAI_SERVER_PORT = 7000; 			// EAI SERVER PORT
	/********************************************************************************
	 * SHELL SCRIPT ROOT
	 ********************************************************************************/	
	public static String SHELL_MONITORING_ROOT = ""; 	// SHELL SCRIPT ROOT
	
	public static String agent_id = "";
	public static int seq = 0;
	
	public static void main(String[] args) throws IOException {
		//globalvariable_info_Tobe("HSEAI_DFS_POSVAN_01");
	}
	
	/********************************************************************************
	 * 서버 Datafiles path 경로 찾기
	 ********************************************************************************/
	public static String dataFileSearch() {
		// tibco main
		String main_path = DATA_FILES_DIV;
		//OS Check
		boolean os_div = false; // false = other OS
		
        // 운영체제 구분 (window, window 가 아니면 무조건 linux 로 판단)
		 if (System.getProperty("os.name").indexOf("Windows") > -1) {
			 os_div = true; // true = windows
			 main_path = main_path.replaceAll("\\\\","/");
		 }
		String dataFilePath ="";
		String[] path = main_path.split("/");
        
		for(int i=0; i<path.length; i++){
		//	System.out.println("path[i]="+i+"=="+path[i]);
			if(!path[i].equals("")){		
				if(!path[i].contains("tibco")){
					if (os_div) {
						dataFilePath+=path[i];
					}else{
						dataFilePath+="/"+path[i];
					}
				}else{
					dataFilePath+="/"+path[i];
					break;
				}
			}
		}
		dataFilePath+="/tra/domain";
	//	System.out.println("[dataFilePath]="+dataFilePath);
		File sourceDir = new File(dataFilePath);
		File[] sourceFileList = sourceDir.listFiles();
		if (sourceFileList.length == 0) {
			System.out.println("Folder Count 0.");
		}else{
			for (int i = 0; i < sourceFileList.length; i++) {
				File file = sourceFileList[i];
				if (file.isDirectory()) {
					dataFilePath+="/"+file.getName()+"/datafiles";
					break;
				}
			}
		}
		return dataFilePath;
	}

	/********************************************************************************
	 * 해당 디렉토리가 없을경우 디렉토리를 생성합니다.
	 ********************************************************************************/	
	public static void createDir(File file) {

		if (!file.exists()) {
			try {
				file.mkdirs(); // 폴더 생성합니다.
				System.out.println("폴더가 생성되었습니다.");
			} catch (Exception e) {
				e.getStackTrace();
				System.out.println(e.getMessage()+" createDir");;
			}
		}
		//System.out.println("SaveFiles : " + file.getPath());
	}
	/********************************************************************************
	 * glovalVariableFile 생성 및 glovalVariable 저장
	 ********************************************************************************/	
	public static void createTXT(String txtFile, String writeFile) {
		try {
			//txt파일을 UTF-8로 생성한다.
			//System.err.println(txtFile);
			FileOutputStream fileOutputStream = new FileOutputStream(txtFile);
			OutputStreamWriter OutputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
			BufferedWriter fw = new BufferedWriter(OutputStreamWriter);

			fw.write(writeFile);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.out.println(e.getMessage()+" createTXT");
		}
	}
	
	/********************************************************************************
	 *  File Read
	 ********************************************************************************/
	public static String readFile(String w_div,String title) throws IOException{
		String readFile ="";
        try{
    		String path = GlobalVariablesDBRead.PRODUCTS_LIST_ROOT+File.separator+w_div;
            //파일 객체 생성
    		
            File file = new File(path+File.separator+title);
            //입력 스트림 생성
            FileReader filereader = new FileReader(file);
            int singleCh = 0;
            while((singleCh = filereader.read()) != -1){
                readFile+=(char)singleCh;
            }
            filereader.close();
        }catch (FileNotFoundException e) {
        }catch(IOException e){
            System.out.println(e);
        }
        return readFile;
	}
	
	/********************************************************************************
	 *  Log File Write
	 ********************************************************************************/	
	public static void writeFile(String w_div,String title ,String w_dtlmsg) throws IOException{
		String logMsg ="";
		logMsg = w_dtlmsg;
		String path = PRODUCTS_LIST_ROOT; //"C:/tibcoMonitoring/logs/";//
		//System.out.println("path-==="+path);
		path = path+w_div;
		File outFile = new File(path);
		createDir(outFile);			//디렉토리 생성
		if (outFile.exists()) {
			createTXT(path+File.separator+title, logMsg); //로그 write
		}
	}
	
	/********************************************************************************
	 *  Initializing Configurable
	 ********************************************************************************/
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		
		System.err.println("==========property Start filePropertySet================");
		 ServletContext ctx = event.getServletContext();
	     try (Reader reader = openMybatisConfigReader(ctx)) {
	      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
	      ctx.setAttribute("sqlSessionFactory", sqlSessionFactory);
	     }
	     catch(Exception e){
	    	 System.out.println(e);
	      System.out.println("FATAL ERROR: myBatis could not be initialized");
	      System.exit(1);
	     }
	}

	private Reader openMybatisConfigReader(ServletContext servletContext) throws Exception {
		File configFile = resolveConfigFile(servletContext,
				"/WEB-INF/resources/config/mybatis/oracle/oracle_mybatis-config.xml",
				"config/mybatis/oracle/oracle_mybatis-config.xml");
		if (configFile != null && configFile.isFile()) {
			System.out.println("=============>>>>" + configFile.getPath());
			return new java.io.FileReader(configFile);
		}

		return Resources.getResourceAsReader("config/mybatis/oracle/oracle_mybatis-config.xml");
	}

	private File resolveConfigFile(ServletContext servletContext, String webInfPath, String classpathResource) {
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
		return new File(catalinaBase, osPath + "/scheduler/WEB-INF/resources/config/mybatis/oracle/oracle_mybatis-config.xml");
	}

	private File resolveClasspathFile(String classpathResource) {
		try {
			java.net.URL url = GlobalVariablesDBRead.class.getClassLoader().getResource(classpathResource);
			if (url != null && "file".equals(url.getProtocol())) {
				return new File(url.toURI());
			}
		} catch (Exception ignore) {
		}
		return null;
	}
}
