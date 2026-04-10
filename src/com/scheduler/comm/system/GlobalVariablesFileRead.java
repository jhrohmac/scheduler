package com.scheduler.comm.system;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GlobalVariablesFileRead implements ServletContextListener {
	public static enum OSNAME {AIX, HP, WIN, Windows, Sun, Linux, Unknown};
	public static String OS_NAME = null;
	private static Logger logger = Logger.getLogger(GlobalVariablesFileRead.class.getName());
	
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
	
	public static String domain_nm = "";
	public static String agent_id = "";
	public static int seq = 0;
	
	public static void main(String[] args) throws IOException {
		filePropertySet();
		//globalvariable_info_Tobe("HSEAI_DFS_POSVAN_01");
	}
	
	/********************************************************************************
	 * 서버 Datafiles path 경로 찾기
	 ********************************************************************************/
	public static String dataFileSearch() {
		
		filePropertySet();
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
	 * Server glovalVariable xml 읽기
	 ********************************************************************************/	
	public static String reader(String filedir, String parentFileName)throws ParserConfigurationException, SAXException, IOException {
		 
		String readFile = "";
		
		// XML 문서 파싱
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document document = documentBuilder.parse(filedir);

		// root 구하기
		Element root = document.getDocumentElement();
		// NodeList globalVariables =
		// root.getElementsByTagName("globalVariables"); //부모루트
		NodeList childeren = root.getChildNodes(); // 자식 노드 목록 get

		for (int i = 0; i < childeren.getLength(); i++) {
			
			Node node = childeren.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) { // 해당 노드의 종류
														   // 판정(Element일 때)
				Element ele = (Element) node;
				String nodeName = ele.getNodeName();
				if (nodeName.equals("globalVariables")) {
					// 이름이 globalVariables인 노드는 자식노드가 더 존재함
					NodeList childeren2 = ele.getChildNodes();
					for (int a = 0; a < childeren2.getLength(); a++) {
						Node node2 = childeren2.item(a);
						if (node2.getNodeType() == Node.ELEMENT_NODE) {
							Element ele2 = (Element) node2;
							String nodeName2 = ele2.getNodeName();
							if (nodeName2.equals("globalVariable")) {

								// 이름이 globalVariable인 노드는 자식노드가 더 존재함
								NodeList childeren3 = ele2.getChildNodes();

								Node childeren_node1 = childeren3.item(1);
								Node childeren_node3 = childeren3.item(3);
								Element eleT1 = (Element) childeren_node1;
								Element eleT3 = (Element) childeren_node3;
								String nodeNameeleT1 = eleT1.getTextContent();
								String nodeNameeleT3 = eleT3.getTextContent();
								if(nodeNameeleT1 == null || nodeNameeleT1.equals("")){
									nodeNameeleT1 = " ";
								}
								if(nodeNameeleT3 == null || nodeNameeleT3.equals("")){
									nodeNameeleT3 = " ";
								}
							}
						}
					}
				}
			}
		}
		return readFile;
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
    		String path = GlobalVariablesFileRead.PRODUCTS_LIST_ROOT+File.separator+w_div;
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
	public static void filePropertySet(){
		filePropertySet(null);
	}

	public static void filePropertySet(ServletContext servletContext){
		
		System.err.println("==========property Start filePropertySet================");
		String filepath ="file.os.order.root";
        // 운영체제 구분 (windows 가 아니면 무조건 linux 로 판단)
		 if (System.getProperty("os.name").indexOf("Windows") > -1) {
			 filepath ="file.os.win.root";
		 }
		 
		Properties prop = new Properties();
		loadProperties(prop, servletContext);
			
		// Root Info
		if (prop.getProperty(filepath) == null) {
			logger.warn("### Can not set [DATA_FILES_DIV] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.DATA_FILES_DIV);
		} else {
			GlobalVariablesFileRead.DATA_FILES_DIV = prop.getProperty(filepath);
		}
		
		if (prop.getProperty("file.products.list.root") == null) {
			logger.warn("### Can not set [PRODUCTS_LIST_ROOT] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.PRODUCTS_LIST_ROOT);
		} else {
			GlobalVariablesFileRead.PRODUCTS_LIST_ROOT = prop.getProperty("file.products.list.root");
		}
		
		if (prop.getProperty("file.application.log.root") == null) {
			logger.warn("### Can not set [APPLICATION_LOG_ROOT] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.APPLICATION_LOG_ROOT);
		} else {
			GlobalVariablesFileRead.APPLICATION_LOG_ROOT = prop.getProperty("file.application.log.root");
		}
		
		// MOM Info
		if (prop.getProperty("mom.ems.url") == null) {
			logger.warn("### Can not set [mom.ems.url] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.EMS_URL);
		} else {
			GlobalVariablesFileRead.EMS_URL = prop.getProperty("mom.ems.url");
		}
		if (prop.getProperty("mom.ems.user") == null) {
			logger.warn("### Can not set [mom.ems.user] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.EMS_USER);
		} else {
			GlobalVariablesFileRead.EMS_USER = prop.getProperty("mom.ems.user");
		}
		if (prop.getProperty("mom.ems.pass") == null) {
			logger.warn("### Can not set [mom.ems.pass] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.EMS_PASS);
		} else {
			GlobalVariablesFileRead.EMS_PASS = prop.getProperty("mom.ems.pass");
		}
        
		// Hawk Domain Info for Monitoring MicroAgent and EAI Engine
		if (prop.getProperty("hawk.domain.hostName") == null) {
			logger.warn("### Can not set [hawk.domain.hostName] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.HAWK_DOMAIN_HOSTNAME);
		} else {
			GlobalVariablesFileRead.HAWK_DOMAIN_HOSTNAME = prop.getProperty("hawk.domain.hostName");
		}
		if (prop.getProperty("hawk.domain.name") == null) {
			logger.warn("### Can not set [hawk.domain.name] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.HAWK_DOMAIN_NAME);
		} else {
			GlobalVariablesFileRead.HAWK_DOMAIN_NAME = prop.getProperty("hawk.domain.name");
		}
		if (prop.getProperty("hawk.domain.rvDaemon") == null) {
			logger.warn("### Can not set [hawk.domain.rvDaemon] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.HAWK_DOMAIN_RVDAEMON);
		} else {
			GlobalVariablesFileRead.HAWK_DOMAIN_RVDAEMON = prop.getProperty("hawk.domain.rvDaemon");
		}
		if (prop.getProperty("hawk.domain.rvNetwork") == null) {
			logger.warn("### Can not set [hawk.domain.rvNetwork] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.HAWK_DOMAIN_RVNETWORK);
		} else {
			GlobalVariablesFileRead.HAWK_DOMAIN_RVNETWORK = prop.getProperty("hawk.domain.rvNetwork");
		}
		if (prop.getProperty("hawk.domain.rvService") == null) {
			logger.warn("### Can not set [hawk.domain.rvService] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.HAWK_DOMAIN_RVSERVICE);
		} else {
			GlobalVariablesFileRead.HAWK_DOMAIN_RVSERVICE = prop.getProperty("hawk.domain.rvService");
		}
		if (prop.getProperty("hawk.domain.minNum") == null) {
			logger.warn("### Can not set [hawk.domain.minNum] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.HAWK_DOMAIN_MINNUM);
		} else {
			GlobalVariablesFileRead.HAWK_DOMAIN_MINNUM = 1;
		}
		//TABLE SPACE INFO
		if (prop.getProperty("sub.db.driver") == null) {
			logger.warn("### Can not set [sub.db.driver] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.TABLESPACE_DB_DRIVER);
		} else {
			GlobalVariablesFileRead.TABLESPACE_DB_DRIVER = prop.getProperty("sub.db.driver");
		}
		if (prop.getProperty("sub.db.url") == null) {
			logger.warn("### Can not set [sub.db.url] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.TABLESPACE_DB_URL);
		} else {
			GlobalVariablesFileRead.TABLESPACE_DB_URL = prop.getProperty("sub.db.url");
		}
		if (prop.getProperty("sub.db.id") == null) {
			logger.warn("### Can not set [sub.db.id] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.TABLESPACE_DB_ID);
		} else {
			GlobalVariablesFileRead.TABLESPACE_DB_ID = prop.getProperty("sub.db.id");
		}
		if (prop.getProperty("sub.db.passwd") == null) {
			logger.warn("### Can not set [sub.db.passwd] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.TABLESPACE_DB_PASS);
		} else {
			GlobalVariablesFileRead.TABLESPACE_DB_PASS = prop.getProperty("sub.db.passwd");
		}
		/********************************************************************************
		 * EAI SERVER SETTING
		 ********************************************************************************/	
		if (prop.getProperty("mo.server.list") == null) {
			logger.warn("### Can not set [mo.server.list] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.EAI_SERVER_LIST);
		} else {
			GlobalVariablesFileRead.EAI_SERVER_LIST = prop.getProperty("mo.server.list");
		}
		if (prop.getProperty("mo.server.remote") == null) {
			logger.warn("### Can not set [mo.server.remote] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.EAI_SERVER_REMOTE);
			try {
				EAI_SERVER_REMOTE = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {				
				e.printStackTrace();
			}
		} else {
			GlobalVariablesFileRead.EAI_SERVER_REMOTE = prop.getProperty("mo.server.remote");
		}
		if (prop.getProperty("mo.server.protocol") == null) {
			logger.warn("### Can not set [mo.server.protocol] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.EAI_SERVER_PROTOCOL);
		} else {
			GlobalVariablesFileRead.EAI_SERVER_PROTOCOL = prop.getProperty("mo.server.protocol");
		}
		if (prop.getProperty("mo.server.port") == null) {
			logger.warn("### Can not set [mo.server.port] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.EAI_SERVER_PORT);
		} else {
			GlobalVariablesFileRead.EAI_SERVER_PORT = Integer.parseInt(prop.getProperty("mo.server.port"));
		}
		/********************************************************************************
		 * SHELL SCRIPT ROOT
		 ********************************************************************************/	
		if (prop.getProperty("shell.monitoring.root") == null) {
			logger.warn("### Can not set [shell.monitoring.root] Set Default(or Previous Set Value) : " + GlobalVariablesFileRead.SHELL_MONITORING_ROOT);
		} else {
			GlobalVariablesFileRead.SHELL_MONITORING_ROOT = prop.getProperty("shell.monitoring.root");
		}
		
		// File Log Initializing(EMS Queue List, BW Engine List, SAP Adapter List)
		///logs/scheduler/monitoring/logs/
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
		String tomonth = dateFormat.format(cal.getTime()).substring(0,6);
		cal.add(Calendar.MONTH,-1);
		String asmonth = dateFormat.format(cal.getTime()).substring(0,6);
		
		//AS-IS
		try {
			int emsQueueList = readFile("EMS", asmonth+"_EMSQueue.log").split("\n").length;
			if(emsQueueList == 0){
				System.out.println("[ERROR] EMS Queue List File is Not Found");
			}
			
			int asEngList = readFile("BWEngine", asmonth+"_EngineList.log").split("\n").length;
			int asAdr3List = readFile("Adapter", asmonth+"_AdapterList.log").split("\n").length;
			
			//TO-BE
			int toEngList = readFile("BWEngine", tomonth+"_EngineList.log").split("\n").length;
			int toAdr3List = readFile("Adapter", tomonth+"_AdapterList.log").split("\n").length;
			
			if(asEngList == 0 || asAdr3List == 0 ){
				System.out.println("[ERROR] Engine,Adapter AS-IS File is Not Found");
			}
			if(toEngList == 0 || toAdr3List == 0 ){
				System.out.println("[ERROR] Engine,Adapter TO-BE File is Not Found");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[ERROR] GlobalVariablesFileRead : File Log Initializing"+e.getMessage());
		}
		
		// get OS NAME
		String osName = System.getProperty("os.name");
		if ( osName.startsWith(OSNAME.AIX.toString())){
			OS_NAME = OSNAME.AIX.toString();
		}else if(osName.startsWith(OSNAME.HP.toString())){
			OS_NAME = OSNAME.HP.toString();
		}else if(osName.startsWith(OSNAME.Windows.toString())){
			OS_NAME = OSNAME.WIN.toString();
		}else if(osName.startsWith(OSNAME.Sun.toString())){
			OS_NAME = OSNAME.Sun.toString();
		}else if(osName.startsWith(OSNAME.Linux.toString())){
			OS_NAME = OSNAME.Linux.toString();
		}else {
			OS_NAME = OSNAME.Unknown.toString();
		}
		logger.info("### OS Name: " + osName + " " + OS_NAME);
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		filePropertySet(arg0.getServletContext());
	}

	private static void loadProperties(Properties prop, ServletContext servletContext) {
		File propertyFile = resolvePropertyFile(servletContext,
				"/WEB-INF/resources/config/repository/file.properties",
				"config/repository/file.properties");
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

		try (InputStream in = GlobalVariablesFileRead.class.getClassLoader()
				.getResourceAsStream("config/repository/file.properties")) {
			if (in != null) {
				prop.load(in);
			}
		} catch (IOException e) {
			System.err.println("filePropertySet() Method : " + e.getMessage());
		}
	}

	private static File resolvePropertyFile(ServletContext servletContext, String webInfPath, String classpathResource) {
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
		return new File(catalinaBase, osPath + "/scheduler/WEB-INF/resources/config/repository/file.properties");
	}

	private static File resolveClasspathFile(String classpathResource) {
		try {
			java.net.URL url = GlobalVariablesFileRead.class.getClassLoader().getResource(classpathResource);
			if (url != null && "file".equals(url.getProtocol())) {
				return new File(url.toURI());
			}
		} catch (Exception ignore) {
		}
		return null;
	}
}
