package com.scheduler.comm.util;

import java.util.Base64;

public class Base64Util {

	public static String encode(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static String decode(String str) {
		return new String(Base64.getDecoder().decode(str));
	}
}
