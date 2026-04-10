package com.scheduler.comm.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.scheduler.finance.vo.StockInfoVo;

public class ImageDownload
{
  public static String imageDownload(StockInfoVo stockInfo){
    String code = stockInfo.getStock_code();
    String logURL = stockInfo.getStock_url();
    String countryCode = stockInfo.getStock_country_code();
    System.out.println("code==="+code);
    System.out.println("logURL==="+logURL);
    System.out.println("countryCode==="+countryCode);
    if(countryCode.equals("null")) {
    	countryCode = "ETC";
    }
    System.out.println("logURL==="+logURL);
	code = code.replace("/", "_");
    //String path2 = ImageDownload.class.getResource("").getPath();
    //File filepath = new File("");
    //String file = filepath.getAbsolutePath();
    //String destinationsPath = path2 + "/appone/plugins/media/stock_logo/" + code + ".png";
	String destinationsPath = "D:\\scheduler\\scheduler\\webapp\\appone\\plugins\\media\\stock_logo\\" + countryCode + "\\" + code + ".png";

	// Create File object for the destination directory
	File directory = new File(destinationsPath).getParentFile();

	try {
	    if (!directory.exists()) {
	        boolean created = directory.mkdirs(); // Create directories recursively
	        if (!created) {
	            System.err.println("Failed to create directories for: " + directory.getAbsolutePath());
	            // Handle the failure accordingly, like throwing an exception or returning from the method
	        }
	    }

	    // Your existing code for downloading the image
	    URL url = new URL(logURL);
	    ReadableByteChannel byteChannel = Channels.newChannel(new BufferedInputStream(url.openStream()));
	    FileOutputStream fileOutputStream = new FileOutputStream(destinationsPath);
	    fileOutputStream.getChannel().transferFrom(byteChannel, 0L, 9223372036854775807L);
	    fileOutputStream.close();
	} catch (MalformedURLException | FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    return destinationsPath;
  }
  
  public static void main(String[] args) {
    File path = new File("");
    System.out.println(path.getAbsolutePath());
    String file = path.getAbsolutePath();

    String path2 = ImageDownload.class.getResource("").getPath();
    System.out.println(path2);
    File fileInSamePackage = new File(path2 + "test.txt");
    System.out.println(fileInSamePackage.getPath());
  }

  public static String getPath()
  {
    File path = new File("");
    System.out.println(path.getAbsolutePath());
    return path.getAbsolutePath();
  }
}