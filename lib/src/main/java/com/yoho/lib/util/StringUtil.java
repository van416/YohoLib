/*
 * Created by fanchao
 * 
 * Date:2014年10月8日上午10:23:31 
 * 
 * Copyright (c) 2014, Show(R). All rights reserved.
 * 
 */
package com.yoho.lib.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * Function: 字符判断的工具类  
 *
 * Date: 2014年10月8日  上午10:23:31  
 * 
 * @author fanchao 
 */
public final class StringUtil {

	/**
	 * 判断字符长度是否为空
	 * @param s 传入的字符
	 * @param trim 是否把空格裁剪
	 * @return true 字符为空
	 */
	public static boolean isNullOrEmpty(String s, boolean trim) {
		return s == null || (trim ? s.trim().length() == 0 : s.length() == 0);
	}

	/**
	 * 判断字符长度是否为空
	 * @param s 传入的字符
	 * @return true 字符为空
	 */
	public static boolean isNullOrEmpty(String s) {
		return isNullOrEmpty(s, false);
	}

	/**
	 * 判断传入的路径是否为Gif
	 * @param url 路径
	 * @return true: 为gif
	 */
	public static boolean isGif(String url) {
		String extension = FileUtil.getExtension(url);
		return extension.contains("gif");
	}

	/**
	 * 是否为数字
	 * @param str 待判断的String
	 * @return true 数字，false 不是数字
	 */
	public static boolean isNumeric(String str) {
		if (str == null || str.length() == 0) {
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	/**
	 * 判定一个string是否是邮箱格式
	 * @param string 待判断的String
	 * @return true 是，fase 不是 
	 */
	public static boolean isEmail(String string) {
		if (string == null || string.length() == 0) {
			return false;
		}
		String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(string);
		return m.find();
	}

	/**
	 * 判断一个str在去掉空格/回车/换行之后是否为空
	 * @param str 待检测是字符
	 * @return true 空，false 不为空 
	 */
	public static boolean isEmpty(String str) {
		if (str == null || str.length() == 0) {
			return true;
		}
		str = str.replaceAll(" ", "");
		if (str == null || str.length() == 0) {
			return true;
		}
		str = str.replaceAll("\n", "");
		str = str.replaceAll("\r", "");
		str = str.replaceAll("\t", "");
		if (str == null || str.length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * sqlite进行转义
	 * @param keyWord
	 * @return
	 */
	public static String sqliteEscape(String keyWord) {
		keyWord = keyWord.replace("/", "//");
		keyWord = keyWord.replace("'", "''");
		keyWord = keyWord.replace("[", "/[");
		keyWord = keyWord.replace("]", "/]");
		keyWord = keyWord.replace("%", "/%");
		keyWord = keyWord.replace("&", "/&");
		keyWord = keyWord.replace("_", "/_");
		keyWord = keyWord.replace("(", "/(");
		keyWord = keyWord.replace(")", "/)");
		return keyWord;
	}

	/**
	 * string转化为int
	 * @param string
	 * @param defaultValue 默认值
	 * @return 转化后得到的int
	 */
	public static int valueOfInt(String string, int defaultValue) {
		int value = defaultValue;
		if (string == null || string.length() == 0) {
			return value;
		}
		try {
			value = Integer.parseInt(string);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * string转化为long
	 * @param string
	 * @param defaultValue 默认值
	 * @return 转化后得到的值
	 */
	public static long valueOfLong(String string, long defaultValue) {
		long value = defaultValue;
		if (string == null || string.length() == 0) {
			return value;
		}
		try {
			value = Long.parseLong(string);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * string转化为float
	 * @param string
	 * @param defaultValue 默认值
	 * @return 转化后得到的值
	 */
	public static float valueOfFloat(String string, float defaultValue) {
		float value = defaultValue;
		if (string == null || string.length() == 0) {
			return value;
		}
		try {
			value = Float.parseFloat(string);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * string转化为boolean
	 * @param string
	 * @param defaultValue 默认值
	 * @return 转化后得到的值
	 */
	public static boolean valueOfBoolean(String string, boolean defaultValue) {
		boolean value = defaultValue;
		if (string == null || string.length() == 0) {
			return value;
		}
		try {
			value = Boolean.parseBoolean(string);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	public static String substring(String str, int toCount, String more) throws Exception {
		int reInt = 0;
		String reStr = "";
		if (str == null)
			return "";
		char[] tempChar = str.toCharArray();
		for (int kk = 0; (kk < tempChar.length && toCount > reInt); kk++) {
			String s1 = str.valueOf(tempChar[kk]);
			byte[] b = s1.getBytes();
			reInt += b.length;
			reStr += tempChar[kk];
		}
		if (toCount == reInt || (toCount == reInt - 1))
			reStr += more;
		return reStr;
	}

	/**
	 * 从对象获取一个字节数组 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static byte[] getBytesFromObject(Serializable obj) {
		if (obj == null) {
			return null;
		}
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo;
		try {
			oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bo.toByteArray();
	}

	/** 
	 * byte(字节)根据长度转成kb(千字节)和mb(兆字节) 
	 *  
	 * @param bytes 
	 * @return 
	 */
	public static float bytesToKb(long bytes) {
		BigDecimal filesize = new BigDecimal(bytes);
		BigDecimal megabyte = new BigDecimal(1024 * 1024);
		float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP).floatValue();
		if (returnValue > 1) {
			return returnValue;
		}
		BigDecimal kilobyte = new BigDecimal(1024);
		returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP).floatValue();
		return returnValue;
	}

	/**
	 * 把list转成string 以,分隔
	 * @return
	 */
	public static String getDeleteImages(List<String> strings) {
		StringBuilder result = new StringBuilder();
		boolean flag = false;
		for (String s : strings) {
			if (flag) {
				result.append(",");
			} else {
				flag = true;
			}
			result.append(s);
		}
		return result.toString();
	}
}
