package com.scheduler.comm.util;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {
	
    private static Calendar getCalendar(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar;
    }

    // Checks if the current time is between startHour: startMinute and endHour: endMinute
    private static boolean isTimeInRange(int startHour, int startMinute, int endHour, int endMinute) {
        Calendar currentTime = Calendar.getInstance();
        Calendar startTime = getCalendar(startHour, startMinute, 0);
        Calendar endTime = getCalendar(endHour, endMinute, 0);
        return currentTime.after(startTime) && currentTime.before(endTime);
    }

    // Checks if the current time is between 09:00 and 10:00
    public static boolean isTimeMoning(int startHour, int startMinute, int endHour, int endMinute) {
    						//9,0,10,0
        return isTimeInRange(startHour, startMinute, endHour, endMinute);
    }
    
    // Checks if the current time is between 09:00 and 20:00
    public static boolean isTimeKR(int startHour, int startMinute, int endHour, int endMinute) {
        return isTimeInRange(startHour, startMinute, endHour, endMinute);
    }
    
    // Checks if the current time is after 23:00 or before 09:00
    public static boolean isTimeUS(int afterHour, int afterMinute, int beforeHour, int beforeMinute) {
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(afterHour, afterMinute);
        LocalTime endTime = LocalTime.of(beforeHour, beforeMinute);
        return currentTime.isAfter(startTime) || currentTime.isBefore(endTime);
    }
    
    // Checks if the current time is between 09:00 and 20:00
    public static boolean isTimeKR() {
    	return isTimeInRange(9,0,16,0);
    }
    // Checks if the current time is after 23:00 or before 09:00
    public static boolean isTimeUS() {
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(16, 00);
        LocalTime endTime = LocalTime.of(9, 00);
        return currentTime.isAfter(startTime) || currentTime.isBefore(endTime);
    }
    
    // Checks if the current time is after 23:00 or before 09:00
    public static String getCountry() {
    	String country = "";
    	SimpleDateFormat sdf = null;
    	try {
			// 09시~15시30분 사이는 KR 그외는 US 시간에 따라 종목 Analysis 업데이트
    		sdf = new SimpleDateFormat("yyyyMMddkk"); // yyyyMMddkk (년월일시)
			if (isTimeKR()) {
				country = "KR";
				sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
			}else if (isTimeUS()) {
				country = "US";
				//sdf = new SimpleDateFormat("yyyyMMdd"); // yyyyMMddkk (년월일시)
				sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return country;
    }
    
    public static String getCountryTime() {
    	String today = "";
    	SimpleDateFormat sdf = null;
    	try {
    		// 09시~15시30분 사이는 KR 그외는 US 시간에 따라 종목 Analysis 업데이트
    		sdf = new SimpleDateFormat("yyyyMMddkk"); // yyyyMMddkk (년월일시)
			if (isTimeKR()) {
				sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
			}else if (isTimeUS()) {
				//sdf = new SimpleDateFormat("yyyyMMdd"); // yyyyMMddkk (년월일시)
				sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			}
	    	String str = sdf.format(new Date());
			Date date = sdf.parse(str);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			today = sdf.format(cal.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return today;
    }

    public static String getFormatTimestamp(String timestamp) {
        try {
        	DateFormat format = null;
        	Date date = null;
        	if(timestamp.length() > 8) {
        		format = new SimpleDateFormat("yyyyMMddHH");
        		date = format.parse(timestamp);
        		format = new SimpleDateFormat("yyyy-MM-dd HH");
        	}else {
        		format = new SimpleDateFormat("yyyyMMdd");
        		date = format.parse(timestamp);
        		format = new SimpleDateFormat("yyyy-MM-dd");
        	}
            return format.format(date);
        } catch (ParseException e) {
            e.printStackTrace(); // Handle parsing exception if needed
            return ""; // Return an empty string or handle error case accordingly
        }
    }
}
