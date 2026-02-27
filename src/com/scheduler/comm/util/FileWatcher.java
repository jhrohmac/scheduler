package com.scheduler.comm.util;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.scheduler.comm.system.GlobalVariablesFileRead;

public class FileWatcher {
	
	private static Path sharedDirectoryPath;
	
	private static WatchKey watchKey;
	private static WatchService watchService;
	/**
	 * 파일 수정 조회
	 * @param args
	 */
	
	public static void main(String[] args) {
		// 모니터링할 파일들을 포함한 디렉터리의 경로를 설정한다
		//String path = "C:\\tibco\\tra\\domain\\TIBCO_ADMIN\\datafiles";
		String applicationDataFileDiv = GlobalVariablesFileRead.dataFileSearch();	//BW Engine Data File 경로 가져온다.
		
		System.out.println("div==========="+applicationDataFileDiv);
		
		sharedDirectoryPath 	= Paths.get(applicationDataFileDiv);
		
		try {
			// FileSystems를 Watch 할 수 있는 인스턴스를 얻는다
			watchService = FileSystems.getDefault().newWatchService();
			// WatchKey에 WatchService 객체와 감지할 이벤트를 등록한다
			watchKey = sharedDirectoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(true) {
			// 이벤트가 발생할 때 마다 콘솔로 출력한다
			for(WatchEvent<?> watchEvent : watchKey.pollEvents()) {
				
				System.out.println(watchEvent.context() + " " + watchEvent.kind());
			}
		}
	}

	public static void checkFileWatcher() {
		// 모니터링할 파일들을 포함한 디렉터리의 경로를 설정한다
		//String path = "C:\\tibco\\tra\\domain\\TIBCO_ADMIN\\datafiles";
		String applicationDataFileDiv = GlobalVariablesFileRead.dataFileSearch();	//BW Engine Data File 경로 가져온다.
		
		System.out.println("div==========="+applicationDataFileDiv);
		sharedDirectoryPath 	= Paths.get(applicationDataFileDiv);
		
		try {
			// FileSystems를 Watch 할 수 있는 인스턴스를 얻는다
			watchService = FileSystems.getDefault().newWatchService();
			// WatchKey에 WatchService 객체와 감지할 이벤트를 등록한다
			watchKey = sharedDirectoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(true) {
			// 이벤트가 발생할 때 마다 콘솔로 출력한다
			for(WatchEvent<?> watchEvent : watchKey.pollEvents()) {
				System.out.println(watchEvent.context() + " " + watchEvent.kind());
			}
		}
	}
	
	public static void checkLogFileWatcher() {
		// 모니터링할 파일들을 포함한 디렉터리의 경로를 설정한다
		//String path = "C:\\tibco\\tra\\domain\\TIBCO_ADMIN\\datafiles";
		String applicationDataFileDiv = GlobalVariablesFileRead.dataFileSearch();	//BW Engine Data File 경로 가져온다.
		
		System.out.println("div==========="+applicationDataFileDiv);
		sharedDirectoryPath 	= Paths.get(applicationDataFileDiv);
		
		try {
			// FileSystems를 Watch 할 수 있는 인스턴스를 얻는다
			watchService = FileSystems.getDefault().newWatchService();
			// WatchKey에 WatchService 객체와 감지할 이벤트를 등록한다
			watchKey = sharedDirectoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while(true) {
			// 이벤트가 발생할 때 마다 콘솔로 출력한다
			for(WatchEvent<?> watchEvent : watchKey.pollEvents()) {
				System.out.println(watchEvent.context() + " " + watchEvent.kind());
				
			}
		}
	}
}