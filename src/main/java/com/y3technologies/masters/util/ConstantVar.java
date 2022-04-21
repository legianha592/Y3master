package com.y3technologies.masters.util;

import java.util.HashMap;
import java.util.Map;

public class ConstantVar {

	public static final String ENGLISH = "en";
	public static final String CHINESE = "zh";
	public static final String ENGLISH_COUNTRY = "US";
	public static final String CHINESE_COUNTRY = "CN";

	public final static String SUCCESS = "SUCCESS";
	public final static String FAILURE = "FAILURE";
	public final static String PAGE = "page";
	public final static String ERROR = "ERROR";

	public final static String DUPLICATE = "The record has been existed!";
	public final static Integer VALID_CODE = -1;
	public final static Integer DUPLICATE_CODE = -2;
	
	public static final Map<Integer, String> ErrorMap = new HashMap<Integer, String>();
	static{
		ErrorMap.put(DUPLICATE_CODE, DUPLICATE);
	}

	public static final String ORPREFIX = "|";
	public static final String DATE_FORMATE = "yyyy-MM-dd";
	public static final String DATETIME_FORMATE = "yyyy-MM-dd HH:mm:ss";

}
