package com.scheduler.comm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class GetHttpsURLConnection {
	public static void main(String[] args) throws Exception {

//		JSONObject json = new JSONObject();
//		json.put("agent_id", "COM.TIBCO.ADAPTER.bwengine.HSEAI_DEV.EAI_Daemon_IFCnt.PA_Daemon_IFCnt");
//		String body = json.toString();
//		ObjectMapper mapper = new ObjectMapper();
//		String jsonString = mapper.writeValueAsString(body);
//
//		String paramUrl = "/scheduler/getAgentInfo_Pending_ListData.do";
//		getHttpsURLConnection(paramUrl, "COM.TIBCO.ADAPTER.bwengine.HSEAI_DEV.EAI_Daemon_Alert.PA_Daemon_Alert_LB");
		String url ="https://sandbox-api.commerce.naver.com:443/partner/v1/oauth2/token";
		url ="https://markets.hankyung.com/api/v2/korean-stocks?sortBy=kospi200";
		url = "https://ktcs-test.krx.co.kr/openapi/svc/auth/token";
        // POST 요청 데이터 준비
        String postData = "{"
                + "\"grant_type\":\"client_credentials\","
                + "\"client_id\":\"00710\","
                + "\"client_secret\":\"90D3E9DABCEE4406872829D73CEC475F38B0ACF7\""
                + "}";
		String result = GetHttpsURLConnection.getHttpsURLConnection2( url, postData);
		System.out.println(result);
	}
	
	
	public static String getHttpsGet(String callUrl) {
	    String inputLine = null;
	    String result = "";

	    System.out.println("getHttpsGet================");
	    System.out.println(callUrl);
	    try {
	        URL url = new URL(callUrl);
	        HttpsURLConnection http;

	        if (url.getProtocol().equalsIgnoreCase("https")) {
	            trustAllHosts();
	            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
	            https.setHostnameVerifier(GetHttpsURLConnection.DO_NOT_VERIFY);
	            http = https;
	        } else {
	            http = (HttpsURLConnection) url.openConnection();
	        }

	        http.setRequestMethod("GET");
	        http.setDoOutput(false); // ★ GET에서는 출력 스트림 사용 X

	        // 필요하면 Accept 정도만
	        http.setRequestProperty("Accept", "application/json");

	        http.setConnectTimeout(115000);
	        http.setReadTimeout(115000);

	        int responseCode = http.getResponseCode();
	        System.out.println("ResponseCode = " + responseCode);

	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            try (BufferedReader in = new BufferedReader(
	                    new InputStreamReader(http.getInputStream(), "UTF-8"))) {
	                while ((inputLine = in.readLine()) != null) {
	                    result += inputLine + "\n";
	                }
	            }
	        } else {
	            result = String.valueOf(responseCode);
	        }

	    } catch (Exception e) {
	        System.out.println("==========Exception call_Url=======");
	        System.out.println(callUrl);
	        e.printStackTrace();
	        System.out.println("==========Exception call_Url=======");
	    }

	    System.out.println("==========REST result =======");
	    //System.out.println(result);
	    System.out.println("==========REST result =======");
	    return result;
	}

	public static String getHttpsURLConnection(String method, String call_Url, String setValue){
		String inputLine = null;
		String result = "";

		System.out.println(call_Url);
		try {
			final URL url = new URL(call_Url);
			HttpsURLConnection http = null;
			if (url.getProtocol().toLowerCase().equals("https")) {
				trustAllHosts();
				final HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
				https.setHostnameVerifier(GetHttpsURLConnection.DO_NOT_VERIFY);
				http = https;
			} else {
				http = (HttpsURLConnection) url.openConnection();
			}
			http.setDoOutput(true);
			http.setRequestMethod(method);
			http.addRequestProperty("Content-Type", "application/json; charset=utf-8");
			http.setConnectTimeout(115000);
			http.setReadTimeout(115000);
			OutputStreamWriter wr = new OutputStreamWriter(http.getOutputStream(), "utf-8");
			wr.write(setValue);
			wr.flush();
			
			if (http.getResponseCode() == 200) {
				final BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
				while ((inputLine = in.readLine()) != null) {
					result = String.valueOf(result) + inputLine + "\n";
				}
				in.close();
			} else {
				result = String.valueOf(http.getResponseCode());
			}
		} catch (Exception e) {
			  System.out.println("==========Exception call_Url=======");
			  System.out.println(call_Url);
			  System.out.println(e);
			  System.out.println("==========Exception call_Url=======");
		}
		System.out.println("==========REST result =======");
		//System.out.println(result);
		System.out.println("==========REST result =======");
		return result;
	}

	public static String getHttpsURLConnection2(String call_Url, String setValue) throws IOException {

        // API 엔드포인트 URL
        String aurl = "https://openapi.koreainvestment.com:9443/oauth2/Approval";

        // 요청 Body에 들어갈 데이터
        String requestBody = "{\n" +
                "    \"grant_type\": \"client_credentials\",\n" +
                "    \"appkey\": \"PSDn1xsRCotMJuIgWAwOeLA1SgMmnLhUYY2U\",\n" +
                "    \"secretkey\": \"DQsgBCi5yEJajPswCS8gY1ipi3UuRuHGfqBzyv2M9exvR1hx5fZhjWwL/Uhzv8QMB6NF0FSs7rerlCOG/6tPjWVuUrjSdAuCEUS4Ecw+Okaa+wI/t8jJ27eEZW/PMooZZ39njo0LDT/XHrkAOIYTJ9DQWLUkgaGlVafiDb8R3swRGqcuet4=\"\n" +
                "}";
        
		/*
		 * protocol = "https"; remote = "host_ip"; port = 7000;
		 */
		String inputLine = null;
		String result = "";
		// new URL("http", "www.myHome.com", 80, "/page/myHome.html");
		final URL url = new URL(aurl);
		HttpsURLConnection http = null;
		if (url.getProtocol().toLowerCase().equals("https")) {
			trustAllHosts();
			HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
			https.setHostnameVerifier(DO_NOT_VERIFY);
			http = https;
			 System.out.println("#SSL Connet>" + url + "\n");
		} else {
			http = (HttpsURLConnection) url.openConnection();
			 System.out.println("#HTTP Connet>" + url + "\n");
		}
		http.setDoOutput(true);
		http.setRequestMethod("POST");
		http.addRequestProperty("Content-Type", "application/json; charset=utf-8");
		http.setConnectTimeout(115000);
		http.setReadTimeout(115000);
		OutputStreamWriter wr = new OutputStreamWriter(http.getOutputStream(), "utf-8");
		wr.write(requestBody);
		wr.flush();

		if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
			while ((inputLine = in.readLine()) != null) {
				result = result + inputLine + "\n";
			}
			in.close();
		} else {
			result = "HttpURLConnection HTTP_ERROR";
		}

		 String parseXml = result;
		 System.out.println(result);
		 System.out.println("\n#Response>" + url + ", result = " + result + "\n"
		 + parseXml);

		wr.close();
		return result;
	}
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
}
