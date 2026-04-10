package com.scheduler.comm.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
	
    public static String getTime()
    {
        Date oDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(oDate);
    }
    public static String getTime(String format)
    {
        Date oDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);  
        return sdf.format(oDate);
    }
    public static String getTime(Calendar c, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(c.getTime());
    }
    public static String getTime(Timestamp c, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(c.getTime());
    }

    public static String getCurrentDate()
    {
        return getCurrentDate("yyyyMMdd");
    }
    
    public static String getCurrentDate(String format)
    {
        Date oDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(oDate);
    }

    public static String getThisMonth()
    {
        Date oDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        return sdf.format(oDate);
    }
    
    public static String getThisDay()
    {
        Date oDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        return sdf.format(oDate);
    }
    
    public static String getThisYear()
    {
        Date oDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        return sdf.format(oDate);
    }

    public static String getCurrentTime()
    {
        Date oDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        return sdf.format(oDate);
    }

    public static String getNextMonDate(String format,int addMonth){

		Calendar calendar = Calendar.getInstance( Locale.KOREA );
		calendar.set(Calendar.YEAR, Integer.parseInt(getThisYear()));
		calendar.set(Calendar.MONTH, Integer.parseInt(getThisMonth())-1);
		calendar.set(Calendar.DATE, Integer.parseInt(getThisDay()));
		calendar.add(Calendar.MONTH, addMonth);
		
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		return sdf.format(calendar.getTime());
    }    
    
    public static String getFirstMonthOfYear(String format){

		Calendar calendar = Calendar.getInstance( Locale.KOREA );
		calendar.set(Calendar.YEAR, Integer.parseInt(getThisYear()));
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DATE, 1);
		
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		return sdf.format(calendar.getTime());
    }    

    public static String getMonthLastDate(String date){

		int year = Integer.parseInt(date.substring(0,4));
		int mon  = Integer.parseInt(date.substring(4,6));
		
		Calendar calendar = Calendar.getInstance( Locale.KOREA );
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH,mon);
		calendar.set(Calendar.DATE, 1);
		
		calendar.add(Calendar.DATE, -1);
		
		return String.valueOf(calendar.get(Calendar.DATE));
    }

	public static String convertDateType(String date) {
		return date.length() != 8 ? date : (new StringBuffer(date.substring(0,4)).append("/").append(date.substring(4,6)).append("/").append(date.substring(6)).toString());
	}
	static String getAmPm(Calendar c) {
		return (c.get( Calendar.HOUR_OF_DAY) <= 12) ? "AM" : "PM";
	}
	public static String getClock(String format) {
		return getFromatClock(Calendar.getInstance(DateUtil.getUserTimeZone()), format);
	}
	public static String getDate(Calendar c) {
		String s = String.valueOf(c.get( Calendar.DATE));
		return (s.length() == 1)  ?  ( "0"+ s )  :  s;		
	}
	public static String getDateByGap(int dateGap, String format) {
		Calendar c = Calendar.getInstance(DateUtil.getUserTimeZone());
		c.add(Calendar.DATE, dateGap);
		return getFromatClock(c, format);
	}
	public static String getDateByGap(int dateGap, String format, String yyyyMMdd) {
		Calendar c = Calendar.getInstance(DateUtil.getUserTimeZone());
		c.set(Integer.parseInt( yyyyMMdd.substring(0, 4)), Integer.parseInt(yyyyMMdd.substring(4,6)) -1, Integer.parseInt(yyyyMMdd.substring(6)));
		c.add(Calendar.DATE, dateGap);
		return getFromatClock(c, format);
	}	
	public static String getDateTimeByGap(int dateGap, String format, String yyyyMMdd) {
		Calendar c = Calendar.getInstance(DateUtil.getUserTimeZone());
		c.set(Integer.parseInt( yyyyMMdd.substring(0, 4)), Integer.parseInt(yyyyMMdd.substring(5,7)) -1, Integer.parseInt(yyyyMMdd.substring(8)));
		c.add(Calendar.DATE, dateGap);
		return getFromatClock(c, format);
	}
	
	public static String getMonthByGap(int monthGap, String format, String yyyyMM) {
		
		Calendar c = Calendar.getInstance(DateUtil.getUserTimeZone());
		c.set(Integer.parseInt( yyyyMM.substring(0, 4)), Integer.parseInt(yyyyMM.substring(4)) -1, 1);
		c.add(Calendar.MONTH, monthGap);
		return getFromatClock(c, format);
	}	
	public final static int getDayOfWeek() {
		Calendar cal = Calendar.getInstance(DateUtil.getUserTimeZone());
		return cal.get(java.util.Calendar.DAY_OF_WEEK);
	}
	
	static String getFromatClock(Calendar c, String format) {
		if (format.indexOf("yyyy") > -1) {
			format = StringUtil.replaceString(format, "yyyy", DateUtil.getYear(c));
		}
		if (format.indexOf("MM") > -1) {
			format = StringUtil.replaceString(format, "MM", DateUtil.getMonth(c));
		}
		if (format.indexOf("dd") > -1) {
			format = StringUtil.replaceString(format, "dd", DateUtil.getDate(c));
		}
		if (format.indexOf("HH") > -1) {
			format = StringUtil.replaceString(format, "HH", DateUtil.getHour(c));
		}
		if (format.indexOf("mm") > -1) {
			format = StringUtil.replaceString(format, "mm", DateUtil.getMinute(c));
		}
		if (format.indexOf("ss") > -1) {
			format = StringUtil.replaceString(format, "ss", DateUtil.getSecond(c));
		}
		if (format.indexOf("ap") > -1) {
			format = StringUtil.replaceString(format, "ap", DateUtil.getAmPm(c));
		}
		return format;
	}
	public static int getGapTwoDate(String yyyyMMdd1, String yyyyMMdd2) {
		return getGapTwoDateTime(yyyyMMdd1, "000000", yyyyMMdd2, "000000")[0];
	}
	public static int [] getGapTwoDateTime(String yyyyMMdd1, String HHmmss1, String yyyyMMdd2, String HHmmss2) {
		int nYear1= Integer.parseInt(yyyyMMdd1.substring(0, 4));
		int nMonth1= Integer.parseInt(yyyyMMdd1.substring(4, 6));
		int nDate1= Integer.parseInt(yyyyMMdd1.substring(6));
		int nHour_of_Day1= Integer.parseInt(HHmmss1.substring(0, 2));
		int nMinute1= Integer.parseInt(HHmmss1.substring(2, 4));
		int nSecond1= Integer.parseInt(HHmmss1.substring(4));
	 																	   
		int nYear2 = Integer.parseInt(yyyyMMdd2.substring(0, 4));
		int nMonth2 = Integer.parseInt(yyyyMMdd2.substring(4, 6));
		int nDate2 = Integer.parseInt(yyyyMMdd2.substring(6));
		int nHour_of_Day2 = Integer.parseInt(HHmmss2.substring(0, 2));
		int nMinute2 = Integer.parseInt(HHmmss2.substring(2, 4));
		int nSecond2 = Integer.parseInt(HHmmss2.substring(4));
	
		Calendar cal = Calendar.getInstance(DateUtil.getUserTimeZone());
	
		int nTotalDate1 = 0, nTotalDate2 = 0, nDiffOfYear = 0, nDiffOfDay = 0;
		int process_day = 0, process_hour = 0, process_minute = 0, process_second = 0;
	
		if (nYear1 > nYear2) {
			for(int i=nYear2; i < nYear1; i++) {
				cal.set(i,12,0);
				nDiffOfYear+=cal.get(Calendar.DAY_OF_YEAR);
			}
			nTotalDate1+=nDiffOfYear;
		}
		else if (nYear1 < nYear2) {
			for(int i=nYear1; i < nYear2; i++) {
				cal.set(i,12,0);
				nDiffOfYear+=cal.get(Calendar.DAY_OF_YEAR);
			}
			nTotalDate2+=nDiffOfYear;
		}
	
		cal.set(nYear1,nMonth1-1,nDate1);
		nDiffOfDay=cal.get(Calendar.DAY_OF_YEAR);
		nTotalDate1+=nDiffOfDay;
	
		cal.set(nYear2,nMonth2-1,nDate2);
		nDiffOfDay=cal.get(Calendar.DAY_OF_YEAR);
		nTotalDate2+=nDiffOfDay;
	
		process_day = nTotalDate1-nTotalDate2; 
	
		if ( process_day >= 0 ) {
			if (nSecond1 < nSecond2) {
				process_second = nSecond1 + 60 - nSecond2;
				--nMinute1;
			}
			else {
				process_second = nSecond1 - nSecond2;
			}
	
			if (nMinute1 < nMinute2) {
				process_minute = nMinute1 + 60 - nMinute2;
				--nHour_of_Day1;
			}
			else {
				process_minute = nMinute1 - nMinute2;
			}
				
			if (nHour_of_Day1 < nHour_of_Day2) {
				process_hour = nHour_of_Day1 + 24 - nHour_of_Day2;
				--process_day;
			}
			else {
				process_hour = nHour_of_Day1 - nHour_of_Day2;
			}
		}
		else {
			if (nSecond1 > nSecond2) {
				process_second = -(nSecond2 + 60 - nSecond1);
				--nMinute2;
			}
			else {
				process_second = -(nSecond2 - nSecond1);
			}
	
			if (nMinute1 > nMinute2) {
				process_minute = -(nMinute2 + 60 - nMinute1);
				--nHour_of_Day2;
			}
			else {
				process_minute = -(nMinute2 - nMinute1);
			}
				
			if (nHour_of_Day1 > nHour_of_Day2) {
				process_hour = -(nHour_of_Day2 + 24 - nHour_of_Day1);
				++process_day;
			}
			else {
				process_hour = -(nHour_of_Day2 - nHour_of_Day1);
			}
		}
		return new int[] { process_day, process_hour, process_minute, process_second };
	}
	static String getHour(Calendar c) {
		String s = String.valueOf(c.get( Calendar.HOUR_OF_DAY));
		return (s.length() == 1)  ?  ( "0"+ s )  :  s;		
	}
	static String getMinute(Calendar c) {
		String s = String.valueOf(c.get( Calendar.MINUTE));
		return (s.length() == 1)  ?  ( "0"+ s )  :  s;		
	}
	public static String getMonth(Calendar c) {
		String s = String.valueOf(c.get( Calendar.MONTH)+1);
		return (s.length() == 1)  ?  ( "0"+ s )  :  s;		
	}
	static String getSecond(Calendar c) {
		String s = String.valueOf(c.get( Calendar.SECOND));
		return (s.length() == 1)  ?  ( "0"+ s )  :  s;		
	}
	public static TimeZone getUserTimeZone() {
		TimeZone tz = TimeZone.getDefault();
		tz.setRawOffset(60*60*1000*9);
		TimeZone.setDefault(tz);
		return tz;
	}
	public final static int getWeekOfMonth() {
		Calendar cal = Calendar.getInstance(DateUtil.getUserTimeZone());
		return cal.get(java.util.Calendar.WEEK_OF_MONTH);
	}
	public final static int getWeekOfMonth(int year, int month, int date) {
		Calendar cal = Calendar.getInstance(DateUtil.getUserTimeZone());
		cal.set(year, month, date);
		return cal.get(java.util.Calendar.WEEK_OF_MONTH);
	}
	
	public final static int iGetDayOfWeek(int year, int month, int date) {
		Calendar cal = Calendar.getInstance(DateUtil.getUserTimeZone());
		cal.set(year, month, date);
		return cal.get(java.util.Calendar.DAY_OF_WEEK);
	}		
	public final static int iGetDayOfWeek(String strDate){
		 if(strDate.length() != 8) {
			 return 0;		 
		 }
		 return iGetDayOfWeek(Integer.parseInt(strDate.substring(0,4)),Integer.parseInt(strDate.substring(4,6))-1,Integer.parseInt(strDate.substring(6)));
	}	
	public final static int getWeekOfYear() {
		Calendar cal = Calendar.getInstance(DateUtil.getUserTimeZone());
		return cal.get(java.util.Calendar.WEEK_OF_YEAR);
	}
	public final static int getWeekOfYear(int year, int month, int date) {
		Calendar cal = Calendar.getInstance(DateUtil.getUserTimeZone());
		cal.set(year, month-1, date);
		return cal.get(java.util.Calendar.WEEK_OF_YEAR);
	}
	public final static int getWeekOfYear(String str) {
		
		int year  = StringUtil.to_number(str.substring(0,4));
		int month = StringUtil.to_number(str.substring(4,6));
		int date  = StringUtil.to_number(str.substring(6));
		
		return getWeekOfYear(year, month, date);
	}
	
	public static String getYear(Calendar c) {
		return String.valueOf(c.get( Calendar.YEAR));
	} 
	public static String getToday(){
		Calendar cal = Calendar.getInstance(DateUtil.getUserTimeZone());		
		return getYear(cal) + getMonth(cal) + getDate(cal);
	}
	public static int getCurrentHour(){
		Calendar cal = Calendar.getInstance(DateUtil.getUserTimeZone());
		return cal.get(Calendar.HOUR_OF_DAY);
	}
	
	public static long convertLong(String str){
		
		int year = Integer.parseInt(str.substring(0,4));
		int mon  = Integer.parseInt(str.substring(4,6));
		int date = Integer.parseInt(str.substring(6,8));
		
		Calendar calendar = Calendar.getInstance( Locale.KOREA );
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH,mon-1);
		calendar.set(Calendar.DATE, date);
		
		return calendar.getTimeInMillis();
	}
	public static long convertTime(String str){
		return convertTime(str, "hh:mm:ss");
	}
	public static long convertTime(String str, String format){
		
		DateFormat time_sdf = new SimpleDateFormat(format);		
		try{
			Date date = time_sdf.parse(str);
			return date.getTime();
		}catch(Exception e){
			return 0;
		}
	}
	
	public static String getTime(Date date, String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	// 날짜 유효성 체크 
	public static boolean validationDate(String checkDate) {

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MMM dd", Locale.US); // the format of your date
			
			dateFormat.setLenient(false);
			dateFormat.parse(checkDate);
			
			return true;

		} catch (ParseException e) {
			return false;
		}
	}

    /**
     * 주어진 연도의 각 월에 대해 마지막 영업일을 계산하는 메서드.
     * @param year 연도 (yyyy 형식)
     * @return 각 월의 마지막 영업일 (yyyyMMdd 형식)의 리스트
     * 사용하는곳 IPS TRADE
     */
    public static List<String> getLastBusinessDaysOfYear(String year) {
        List<String> lastBusinessDays = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(Integer.parseInt(year), month);
            LocalDate lastDay = yearMonth.atEndOfMonth();

            // 마지막 영업일 계산
            while (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
                lastDay = lastDay.minusDays(1);
            }

            lastBusinessDays.add(dateFormat.format(java.sql.Date.valueOf(lastDay)));
        }
        return lastBusinessDays;
    }

    /**
     * 지정된 기간의 각 월 마지막 영업일을 반환하는 메서드
     * @param startDate 시작일 (yyyyMMdd)
     * @param endDate 종료일 (yyyyMMdd)
     * @return 각 월 마지막 영업일의 리스트
     */
    public static List<String> getLastBusinessDays(String startDate, String endDate) {
        List<String> lastBusinessDays = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        LocalDate start = LocalDate.parse(startDate, formatter).withDayOfMonth(1);
        LocalDate end = LocalDate.parse(endDate, formatter).withDayOfMonth(1);

        while (!start.isAfter(end)) {
            YearMonth yearMonth = YearMonth.from(start);
            LocalDate lastDay = yearMonth.atEndOfMonth();

            // 마지막 영업일 계산 (토/일 제외)
            while (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
                lastDay = lastDay.minusDays(1);
            }
System.out.println("lastDay.format(formatter)============="+lastDay.format(formatter));
            lastBusinessDays.add(lastDay.format(formatter));
            start = start.plusMonths(1);
        }
        return lastBusinessDays;
    }
}
