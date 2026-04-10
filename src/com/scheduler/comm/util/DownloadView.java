package com.scheduler.comm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.view.AbstractView;

public class DownloadView extends AbstractView{
	
	private Log logger = LogFactory.getLog(DownloadView.class);
	
	public void Download(){     
		setContentType("application/download; utf-8");    
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		logger.info("Start DownloadView -> fileDownload");
		
		File file = (File)model.get("downloadFile"); 
		response.setContentType(getContentType());  
		response.setContentLength((int)file.length());      
		String userAgent = request.getHeader("User-Agent");   
		boolean ie = userAgent.indexOf("MSIE") > -1;    
		String fileName = null;        
		if(ie){                  
			fileName = URLEncoder.encode(file.getName(), "utf-8");     
		}else {                       
			fileName = new String(file.getName().getBytes("utf-8")); 
		}        
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\";");      
		response.setHeader("Content-Transfer-Encoding", "binary");
		OutputStream out = response.getOutputStream();
		FileInputStream fis = null;        
			try {                     
				fis = new FileInputStream(file);
				FileCopyUtils.copy(fis, out);       
				}
			catch(Exception e){
				e.printStackTrace();           
			}finally{                     
				if(fis != null){
					try{                   
						fis.close();            
						}catch(Exception e){
							
						}   
				}                      
			}
		out.flush();
		
		if(file.getAbsolutePath().startsWith(System.getProperty("user.dir"))){
			file.delete();
		}else if(fileName.startsWith("InterfaceDefinition")){
			int pos = fileName.lastIndexOf( "." );
			String ext = fileName.substring( pos + 1 );
			
			if(ext.equals("zip")){
				file.delete();
			}
		}
		
		logger.info("End DownloadView -> fileDownload");
	}
}
