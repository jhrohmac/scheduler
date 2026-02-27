package com.scheduler.comm.util;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Clob;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

public class StringUtil {
	/**
	 * NULL => ""
	 * 
	 * @param str
	 * @return
	 */
	public static String nvl(String str)
	{
		return nvl(str, "");
	}
	public static String nvl_notrim(String str)
	{
		return nvl_notrim(str, "");
	}

	public static String nvl(String str, String str1)
	{
		if(str == null || "".equals(str)) return str1;
		
		return str.trim();
	}

	public static String nvl_notrim(String str, String str1)
	{
		if(str == null || "".equals(str)) return str1;
		
		return str;
	}
	
	public static String[] toArray(String str, String sep){

		String[] temp = new String[0];
		try{
			StringTokenizer st = new StringTokenizer(str, sep);
			temp = new String[st.countTokens()];
			int index = 0;
			while(st.hasMoreTokens()){
				temp[index++] = st.nextToken().trim();
			}
		}catch(Exception e){
		}
		return temp;
	}

	public static int to_number(String str){
		return to_number(str, 0);
	}
	
	public static int to_number(String str, int nullValue){
		int rv = nullValue;
		if(str == null || "".equals(str)) return rv;
		String temp = strip(str.trim(), ",");
		try{
			rv = Integer.parseInt(temp);
		}catch(NumberFormatException nfe){
		}
		return rv;
	}
	 
	
    public static String strip(String str, String str1){
    	
    	if(str == null || "".equals(str.trim())) return "";
    	
	    String temp = str;
		int pos = -1;
		while((pos = temp.indexOf(str1, pos)) != -1) {
			String left = temp.substring(0, pos);
			String right = temp.substring(pos + 1, temp.length());
			temp = left + "" + right;
			pos += 1;
		}		
		return temp;    	
    }

	public static String to_ksc(String s, String encoding, String decoding){

		try{
			if(s == null) return "";
	    	else return new String(s.getBytes(encoding), decoding);	
		}catch(UnsupportedEncodingException e){
			return s;
		}
	}
	
	
	
	public static String dateFormat(String str){
		return dateFormat(str, "/");
	}
	public static String timeFormat(String str){
		return timeFormat(str, ":");
	}
	/**
	 * 20070901 -> 2007/09/01
	 * 
	 * @param str
	 * @param sep
	 * @return
	 */
	public static String dateFormat(String str, String sep){
		
		if(str == null || "".equals(str)) return "";
		
		str = str.replace("-", "");
		
		if(str.trim().length() < 8){
			if(str.trim().length() == 6){
				return str.substring(0,4) + sep + str.substring(4);
			}
			return str;
		}else{
			return str.substring(0,4) + sep + str.substring(4,6) + sep + str.substring(6);
		}
	}
	public static String timeFormat(String str, String sep){
		
		if(str == null || "".equals(str)) return "";
		
		if(str.trim().length() == 4){
			return str.substring(0,2) + sep + str.substring(2);
		}
		return str;
	}
	public static String nf_format(long digit){
		return nf_format(String.valueOf(digit));
	}
	public static String nf_format(String digit){
		return nf_format(digit, ",");
	}
	public static String nf_format(String digit, String sep)
	{
		if( digit == null ) return "";
		StringBuffer	sb = new StringBuffer() ;
		int n = -1 ;
		int underPoint = digit.indexOf( "." ) ;	
		String underDigit = null ;
		String decimal = null ;
	
		boolean minus = false;
	
		if( underPoint != -1 ){
			decimal    = digit.substring( 0, underPoint ) ;
			underDigit = digit.substring( underPoint ) ;
		}
		else{
			if(digit.startsWith("-")){
				decimal = digit.substring(1);
				minus = true;
			}
			else{
				decimal = digit ;
			}
		}
	
		for( int  len = decimal.length(), i = len-1 ; i > -1 ; i -- ){
			char c = decimal.charAt( i ) ;		
			++n ;		
			if( n != 0 && (n % 3 == 0) ) {
				sb.insert( 0, sep ) ;
				sb.insert( 0, c ) ;
			}
			else
				sb.insert(0, c) ;
		}

		if( underDigit != null ){
			sb.append( underDigit ) ;
		}	
		return minus ? "-"+ sb.toString()  : sb.toString();
	}		
	
	
	public static String replaceString(String s, String from, String to) {
		int sp = 0;
		int ep = 0;
		String smallStr = s;

		while (s.indexOf(from) >= 0) {
			sp = 0;
			ep = 0;
			smallStr = s;
			
			while (ep <= smallStr.length()) {
				ep = smallStr.indexOf(from, sp);
				sp = ep + to.length();

				if (ep < 0) {
					return s;
				} 
				else {
					s = s.substring(0, ep) + to + s.substring(ep+from.length());
					smallStr = smallStr.substring(0, ep) + to + smallStr.substring(ep+from.length());
				}
			}
		}
		return s;
	}		
	
	
	public static String cut(String s, int len) {
		return cut(s, len, "");
	}	
	
	public static String cut(String s, int len, String tail) {
		
		if (s == null)  return null;
	    
	    int srcLen = realLength(s);
	    if (srcLen < len){
        	return s;
        }
	        
        String tmpTail = tail;
        if(tail == null) tmpTail = "";
	 
        int tailLen = realLength(tmpTail);
        if (tailLen > len)  return "";
	        
        char a;
        int i = 0;
        int realLen = 0;
        for (i = 0; i < len - tailLen && realLen < len - tailLen; i++) {
           a = s.charAt(i);
           if ((a & 0xFF00) == 0)  realLen += 1;
           else					   realLen += 2;
        }
	        
        while (realLength(s.substring(0, i)) > len - tailLen) {
            i--;
        }
        return s.substring(0, i) + tmpTail;
    }
    public static int realLength(String s) {
        return s.getBytes().length;
    }	  
    public static String getFormatString(String str, int num){
    	return getFormatString(str, " ", num);
    }
    public static String getFormatString(String str, String format, int num){
        String temp = "";
        if(str == null){
        	str = "";
        }
        byte[] b = str.getBytes();
        int len = num - b.length;
        for(int i = 0; i < len; i++){
        	temp += format;
        }
        if(format == "0"){
        	str = temp + str;
        } else{
        	str = str + temp;
        }
        return str;
    }
    public static String substr(String str, int s, int e){
    	String rv = "";
    	if(str == null) return rv;
    	try{
    		rv = str.substring(s, e);
    	}catch(Exception ex){
    		
    	}
    	return rv;
    }

    public static String[] toArray(String str, int length){
    	
    	int size = str.length() / length;
    	String[] rv = new String[size];
    	int idx  = 0;

    	for(int i=0; i<str.length(); i += length){
    		try{
	    		rv[idx++] = str.substring(i, i+length);
    		}catch(Exception e){
    			break;
    		}
    	}
    	
    	return rv;
    }
    
    public static String specialChar(String msg){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < msg.length(); i++){
			/*if(msg.charAt(i) == '&'){
				sb.append("&amp;");
			}else 
			*/
			if(msg.charAt(i) == '"'){
				sb.append("&quot;");
			}else if(msg.charAt(i) == '\''){
				sb.append("&#039;");
			}else if(msg.charAt(i) == '<'){
				sb.append("&lt;");
			}else if(msg.charAt(i) == '>'){
				sb.append("&gt;");
			}else if(msg.charAt(i) == '('){
				sb.append("&#40;");
			}else if(msg.charAt(i) == ')'){
				sb.append("&#41;");
			}else{
				sb.append(msg.charAt(i));
			}
		}
		return sb.toString();
	}

    public static String replaceQchar(String msg){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < msg.length(); i++){

			if(msg.charAt(i) == '"'){
				sb.append('\'');
			}else{
				sb.append(msg.charAt(i));
			}
		}
		return sb.toString();
	}
    
    public static String clobToString(Clob clob){

    	StringBuffer buf = new StringBuffer();
        
        try {
        	Reader reader = clob.getCharacterStream();
        	char[] buffer = new char[1024];
        	int byteRead;
         
        	while((byteRead = reader.read(buffer,0,1024))!=-1) {
        		buf.append(buffer,0,byteRead);
        	}
         
        	reader.close();
        } catch(Exception e) {
        	return null;
        }
         
        return buf.toString(); 
    }   

    public static String substr(String str, String start_s, String end_s){
    	if(str == null)	return "";
    	int start_idx 	= str.indexOf(start_s);
    	int end_idx 	= str.indexOf(end_s);
    	if(start_idx != -1 && end_idx != -1){
    		return str.substring(start_idx+1, end_idx).trim();
    	}
    	return "";
    }
    
    public static String substr_before(String str, String start_s){
    	if(str == null)	return "";
    	int idx 	= str.indexOf(start_s);
    	if(idx != -1){
    		return str.substring(0, idx).trim();
    	}
    	return str;
    }
    public static String substr_after(String str, String start_s){
    	if(str == null)	return "";
    	int idx 	= str.indexOf(start_s);
    	if(idx != -1){
    		return str.substring(idx+1).trim();
    	}
    	return str;
    }


    public static String lpad(String s, int i, String s1)
    {
        String s2 = "";
        if(s == null)
            s = "";
        byte abyte0[] = s.getBytes();
        int j = abyte0.length;
        int point = i - j;        
        if(j >= i)
            return s;        
        byte abyte1[] = new byte[point];

        byte byte0 = s1.getBytes()[0];

        for(int k = 0; k < point; k++)
        	abyte1[k] = byte0;
        
        s2 = new String(abyte1) + s;
        return s2;
    } 
    
    public static String rpad(String s, int i, String s1)
    {
        String s2 = "";
        if(s == null)
            s = "";
        byte abyte0[] = s.getBytes();
        byte abyte1[] = new byte[i];
        int j = abyte0.length;
        byte byte0 = s1.getBytes()[0];
        if(j >= i)
            return s;
        for(int k = 0; k < j; k++)
            abyte1[k] = abyte0[k];

        for(int l = j; l < i; l++)
            abyte1[l] = byte0;

        s2 = new String(abyte1);
        return s2;
    }    

    public static String toFirstLowerCase(String str){
    	
    	if(str == null || "".equals(str)) return str;
    	
    	String first = str.substring(0,1);
    	String second = str.substring(1);
    	
    	return first.toLowerCase() + second;
    }
    public static String toFirstUpperCase(String str){
    	
    	if(str == null || "".equals(str)) return str;
    	
    	String first = str.substring(0,1);
    	String second = str.substring(1);
    	
    	return first.toUpperCase() + second;
    }
    
    public static String getSearchParam(String s_param, String str){
    	
    	if(s_param == null || "".equals(s_param)) return "";
    	
    	String[] tmp_arr = s_param.split("&");
    	for(int i=0; i<tmp_arr.length; i++){
    		String p_name 	= "";
    		String p_value 	= "";
    		String[] p_arr = tmp_arr[i].split("=");
    		try{
    			p_name	= p_arr[0];
    			p_value = p_arr[1];
    		}catch(Exception ignore){}
    		
    		if(p_name.equals(str)){
    			return p_value;
    		}
    	}
    	
    	return "";
    }
    
    public static String substrb(String str, int start, int len )
    {
    	if(str == null || "".equals(str))	return "";
        byte[] arr = str.getBytes();
        String retval = new String(arr, start, len);

        return retval.trim();
    }
    
    public static String parseErrorMsg(String original_errorMsg){
    	
    	String errorMsg =  "실패하였습니다. ";
    	
    	errorMsg = errorMsg + original_errorMsg.replaceAll("\n", "");
    	
    	errorMsg = errorMsg.replaceAll("'", "\"");
    	
    	return errorMsg;
    	
    }
    
    public static String parse_cycleUnit(String cycleUnit){
		String result_cycleUnit = "";
		
		if(cycleUnit.equals("년")){
			result_cycleUnit = "Y";
		}else if(cycleUnit.equals("월")){
			result_cycleUnit = "M";
		}else if(cycleUnit.equals("월말")){
			result_cycleUnit = "MF";
		}else if(cycleUnit.equals("요일")){
			result_cycleUnit = "W";
		}else if(cycleUnit.equals("일")){
			result_cycleUnit = "D";
		}else if(cycleUnit.equals("시간")){
			result_cycleUnit = "H";
		}else if(cycleUnit.equals("분")){
			result_cycleUnit = "MI";
		}else{
			result_cycleUnit = cycleUnit;
		}
		
		return result_cycleUnit;
	}
    
    //byte convert
    public String byteCalculation(String bytes) {
           String retFormat = "0";
          Double size = Double.parseDouble(bytes);
           String[] s = { "bytes", "KB", "MB", "GB", "TB", "PB" };
           if (bytes != "0") {
                 int idx = (int) Math.floor(Math.log(size) / Math.log(1024));
                 DecimalFormat df = new DecimalFormat("#,###.##");
                 double ret = ((size / Math.pow(1024, Math.floor(idx))));
                 retFormat = df.format(ret) + "" + s[idx];
            } else {
                 retFormat += "" + s[0];
            }

            return retFormat;
    }
    public static double parseDoubleSafe(String s) {
        if (s == null || s.trim().isEmpty()) {
            return 0.0;
        }
            return Double.parseDouble(s.trim());
    }
}
