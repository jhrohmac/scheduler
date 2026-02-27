package com.scheduler.comm.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class OsCommendUtil {
	public static void main(String[] args){
		String agent_id ="TEST_JDBC";
    	String ag_path ="";
    	String cmd ="eaiadm  6252     1 241 10:04:37 ?         0:09 /package/tibco/bw/5.13/bin/bwengine --pid --run --propFile C:/tibco/tra/domain/TIBCO_ADMIN/application/TEST_JDBC/TEST_JDBC-TEST_JDBC.tra --innerProcess";
    	FileReader resources= null;
    	Properties prop = new Properties();
    	
    	Map<String, String> result_map = new HashMap<String, String>();
    	
    	if(!cmd.equals("")){
    		String app_tra = cmd;
    		String[] cmdline = app_tra.split(" ");
    		
    		System.out.println(cmdline.length);
    		String tra_path ="";
    		for(String path : cmdline){
    			System.out.println("===");
    			if(path.contains(agent_id)){
    				tra_path = path;
    				System.out.println("path.contains=="+tra_path);
    				try {
						resources= new FileReader(tra_path);
						prop.load(resources);
	    				String ag_dir =  prop.getProperty("tibco.repourl");
	    				String ag_log =  prop.getProperty("Engine.Log.Dir");
	    				
	    				System.out.println("ag_dir=="+ag_dir);
	    				System.out.println("ag_log=="+ag_log);
	    				result_map.put("ag_dir", ag_dir);
	    				result_map.put("ag_log", ag_log);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				
    			}
    		}
    	}
	}
    /**
     * cmd 명령어 실행
     *
     * @param cmd
     */
    public static Map<String, String> commend(String cmd) {
    	
    	Map<String, String> result_map = new HashMap<String, String>();
    	
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        StringBuffer successOutput = new StringBuffer(); 	// 성공 스트링 버퍼
        StringBuffer errorOutput = new StringBuffer(); 		// 오류 스트링 버퍼
        BufferedReader successBufferReader = null; 			// 성공 버퍼
        BufferedReader errorBufferReader = null; 			// 오류 버퍼
        String msg = null; 									// 메시지
        
        List<String> cmdList = new ArrayList<String>();
 
        String os = System.getProperty("os.name").toLowerCase();
        // 운영체제 구분 (window, window 가 아니면 무조건 linux 로 판단)
        if (System.getProperty("os.name").indexOf("Windows") > -1) {
            cmdList.add("cmd");
            cmdList.add("/c");
        } else {
            cmdList.add("ksh");
            cmdList.add("-c");
        }
        
        // 명령어 셋팅
        cmdList.add(cmd);
        
        String[] array = cmdList.toArray(new String[cmdList.size()]);
 
        try {
 
            // 명령어 실행
            process = runtime.exec(array);
 
            // shell 실행 정상
            successBufferReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "EUC-KR"));
            while ((msg = successBufferReader.readLine()) != null) {
                successOutput.append(msg + System.getProperty("line.separator"));
            }
            
            // shell 실행 에러
            errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "EUC-KR"));
            while ((msg = errorBufferReader.readLine()) != null) {
                errorOutput.append(msg + System.getProperty("line.separator"));
            }
 
            // 프로세스의 수행이 끝날때까지 대기
            process.waitFor();
 
            // shell 실행이 정상 종료되었을 경우
            if (process.exitValue() == 0) {
                System.out.println("========Shell Commend Success========"+successOutput.length());
            } else {
                // shell 실행이 비정상 종료되었을 경우
            	System.out.println(errorOutput.toString());
              //  System.out.println("========Shell Commend ERROR========");
                result_map.put("err", errorOutput.toString());
            }
 
        } catch (IOException e) {
        	System.out.println("IOException End");
            System.out.println(e.getMessage());
            result_map.put("err", e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("InterruptedException End");
            System.out.println(e.getMessage());
            result_map.put("err", e.getMessage());
        } finally {
            try {
                process.destroy();
                msg = successOutput.toString();
                result_map.put("msg", msg);
                if (successBufferReader != null) successBufferReader.close();
                if (errorBufferReader != null) errorBufferReader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
		return result_map;
    }
    
    public static Map<String, String> cmd_path(String agent_id){
    	FileReader resources= null;
    	Properties prop = new Properties();
    	String cmd ="";

        String os = System.getProperty("os.name").toLowerCase();
        // 운영체제 구분 (window, window 가 아니면 무조건 linux 로 판단)
        if (System.getProperty("os.name").indexOf("Windows") > -1) {
            cmd ="tasklist | findstr "+ agent_id;
        } else {
            cmd ="ps -efx |grep bwengine | grep "+ agent_id;
        }
    	System.out.println("cmd========="+cmd);
    	Map<String, String> result_map = new HashMap<String, String>();
    	result_map = commend(cmd);
    	
    	System.out.println(result_map.get("msg"));
    	
    	if(!result_map.get("msg").equals("")){
    		String app_tra = result_map.get("msg");
    		String[] cmdline = app_tra.split(" ");
    		String tra_path ="";
    		String ag_dir = "";
    		String eg_log = "";
    		for(String path : cmdline){
    			if(path.contains(agent_id)){
    				tra_path = path;
    				try {
						resources= new FileReader(tra_path);
						prop.load(resources);
	    				ag_dir =  prop.getProperty("tibco.repourl");
	    				eg_log =  prop.getProperty("Engine.Log.Dir");
	    				
	    				result_map.put("ag_dir", ag_dir);
	    				result_map.put("eg_log", eg_log);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
    			}
    		}
    	}
    	return result_map; 
    }
}
