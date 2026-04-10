package com.scheduler.comm.util;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

public class AgentUtil {

	public static String getTibcoClientVar(String var){
		
		if(var.startsWith("%") && var.endsWith("%")){
			//var = com.tibco.pe.plugin.PluginProperties.getProperty("tibco.clientVar."+ var.substring(1,(var.length()-1)));
		}
		return var;
	}
	
	public static String checkSeq(){
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyyMMddHHmmss", Locale.KOREA );
		Date currentTime = new Date ( );
		String checkSeq = mSimpleDateFormat.format ( currentTime );
//		System.out.println ( checkSeq );
		return checkSeq;
	}
	
	public static String checkTime(){
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyyMMddHHmmssSSS", Locale.KOREA );
		Date currentTime = new Date ( );
		String checkTime = mSimpleDateFormat.format ( currentTime );
		return checkTime;
	}
	
	public static String getDateTimeFromMills(String mills){
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyyMMddHHmmssSS", Locale.KOREA );
		Timestamp timeStamp = new Timestamp(Long.parseLong(mills));
		String dateTime = mSimpleDateFormat.format(timeStamp);
		return dateTime;
	}
	
	public static String getDateTimeFromMills(long mills){
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyyMMddHHmmss", Locale.KOREA );
		Timestamp timeStamp = new Timestamp(mills);
		String dateTime = mSimpleDateFormat.format(timeStamp);
		return dateTime;
	}
	
	public static String getTimeFromMills(String mills){
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "HHmmss", Locale.KOREA );
		Timestamp timeStamp = new Timestamp(Long.parseLong(mills));
		String time = mSimpleDateFormat.format(timeStamp);
		return time;
	}
	
	public static String getTimeFromMills(long mills){
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "HHmmss", Locale.KOREA );
		Timestamp timeStamp = new Timestamp(mills);
		String time = mSimpleDateFormat.format(timeStamp);
		return time;
	}
	
	public static String[] split(String s, String s1) {
		Integer m = s.length();
		String as[] = new String[m];
		int i = 0;
		int j = 0;
		int l = s.length();
		do {
			if (j > l)
				break;
			int k = s.indexOf(s1, j);
			if (k == -1) {
				as[i] = s.substring(j, s.length());
				break;
			}
			as[i] = s.substring(j, k);
			j = k + s1.length();
			i++;
		} while (true);
		String as1[] = new String[i + 1];
		for (int i1 = 0; i1 <= i; i1++)
			as1[i1] = as[i1];

		return as1;
	}
	
	public static String getHumanReadableSize(long paramLong) {
		long l1 = (long) Math.pow(2.0D, 20.0D);
		long l2 = (long) Math.pow(2.0D, 10.0D);
		long l3 = (long) Math.pow(2.0D, 30.0D);
		NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
		localNumberFormat.setMaximumFractionDigits(1);
		double d = 0.0D;
		long l4 = Math.abs(paramLong);
		String str = "";
		if (l4 / l3 >= 1L) {
			d = l4 / l3;
			str = "GB";
		} else if (l4 / l1 >= 1L) {
			d = l4 / l1;
			str = "MB";
		} else if (l4 / l2 >= 1L) {
			d = l4 / l2;
			str = "KB";
		} else {
			d = l4;
			str = "b";
		}
		return localNumberFormat.format(((paramLong < 0L) ? -1 : 1) * d) + str;
	}

	public static String getHumanReadableMBSize(long paramLong) {
		String pattern = "#.###";
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		
		double d = 0.0D;
		double mb = 1048576.0;
		
		long l4 = Math.abs(paramLong);
		d = l4 / mb;
					
		return decimalFormat.format(((paramLong < 0L) ? -1 : 1) * d);
	}

	public static String getHumanReadableKBSize(long paramLong) {
		String pattern = "#.###";
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		
		double d = 0.0D;
		double kb = 1024.0;
		
		long l4 = Math.abs(paramLong);
		d = l4 / kb;
		
		return decimalFormat.format(((paramLong < 0L) ? -1 : 1) * d);
	}

	public static String dumpBytes(byte[] paramArrayOfByte) {
		StringBuffer localStringBuffer = new StringBuffer(
				paramArrayOfByte.length);
		for (int i = 0; i < paramArrayOfByte.length; ++i) {
			String str = Integer
					.toHexString(256 + (paramArrayOfByte[i] & 0xFF)).substring(
							1);
			localStringBuffer.append(((str.length() < 2) ? "0" : "") + str);
		}
		return localStringBuffer.toString();
	}

	public static String arrayToString(Object[] paramArrayOfObject) {
		StringBuffer localStringBuffer = new StringBuffer();
		for (int i = 0; (paramArrayOfObject != null)
				&& (i < paramArrayOfObject.length); ++i) {
			localStringBuffer.append(paramArrayOfObject[i].toString());
			if ((!(paramArrayOfObject[i] instanceof String))
					|| (i + 1 >= paramArrayOfObject.length))
				continue;
			localStringBuffer.append(", ");
		}
		return localStringBuffer.toString();
	}
	
	public static String getFullHumanReadableTime(long paramLong) {
		long l1 = 1000L;
		long l2 = 60L * l1;
		long l3 = 60L * l2;
		long l4 = 24L * l3;
		NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
		localNumberFormat.setMaximumFractionDigits(0);
		StringBuffer localStringBuffer = new StringBuffer("");
		double d = 0.0D;
		int i = 1;
		int j = 0;
		long l5 = Math.abs(paramLong);
		String str = null;
		while (l5 >= 1L) {
			localStringBuffer.append(" ");
			if (l5 / l4 >= 1L) {
				j = 0;
				d = l5 / l4;
				l5 -= l4 * (long) d;
				i = 0;
				str = "Days";
			} else if (l5 / l3 >= 1L) {
				j = 0;
				d = l5 / l3;
				str = "Hours";
				l5 -= l3 * (long) d;
			} else if (l5 / l2 >= 1L) {
				j = 0;
				d = l5 / l2;
				str = "Minutes";
				l5 -= l2 * (long) d;
			} else {
				label276 : 
				if (l5 / l1 >= 1L) {
					j = 0;
					d = l5 / l1;
					str = "Seconds";
					l5 -= l1 * (long) d;
					if (i != 0)
						break label276;
				}
				if (j != 0) {
					d = l5;
					str = "Millisconds";
				}
				l5 -= l5;
				if (j == 0)
					continue;
			}
//			 label276 : 
				 Object[] arrayOfObject = { localNumberFormat.format(Math.floor(d)), str };
			 localStringBuffer.append(MessageFormat.format("{0}{1}", arrayOfObject));
		}
		return localStringBuffer.toString().trim();
	}

	public static String stripSpaces(String paramString) {
		return paramString.replaceAll("\\s+", "");
	}
	
	
    public static double round(double num, int pos){
        
        // double temp = (int)((num+0.05)*10) / 10.0; // xxx.x
       
        //[1]
        double result = 0.0;
        double half = 0.5;
        double factor = 1;
       
        //[2]
        for (int i = 0; i< pos; i++){
            half *= 0.1;
            factor *= 10;
        }
        result = (int)((num+half)*factor)/factor;
       
        //[3]      
        return result;     
    }
    
    // String �迭���� ���ڿ� ���� �ִ��� ã�´�.
    public static int findString(String[] array, String s){
    	for (int i = 0; i < array.length; i++) {
			if (array[i].equals(s)) {
				return i;
			}
		}
    	return -1;
    }
    
    // String �迭���� �ߺ��� �׸��� �����Ѵ�.
	public static Object[] removeDuplicateArray(String[] array){
		Object[] removeArray=null;
		TreeSet ts=new TreeSet();
		for(int i=0; i<array.length; i++){
			ts.add(array[i]);
		}
	 
		removeArray= ts.toArray();
		return removeArray;
		
	}
}
