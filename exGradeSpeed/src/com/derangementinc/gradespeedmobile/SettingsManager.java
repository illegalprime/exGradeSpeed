package com.derangementinc.gradespeedmobile;

import com.derangementinc.gradespeedmobile.data.Districts;
import com.derangementinc.gradespeedmobile.data.Family;

import android.content.Context;

public class SettingsManager {
	private static String languageEnum = "en-US";
	private static String defaultUserAgent = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36";
	private static final String combro = "_ctl0_ddlStudents";
	
	private static Context context;
	public static Family    account   = null;
	public static Districts districts = null;
	
	public SettingsManager() {}
	
	public static void setContext(Context cntxt) {
		context = cntxt;
		
		if (districts == null) {
			districts = new Districts(context);
		}
		else {
			districts.setContext(context);
		}
		
		if (account == null) {
			account = new Family(context);
		}
		else {
			account.setContext(context);
		}
	}
	
	public static String getLanguage() {
		return languageEnum;
	}
	
	public static String getUserAgent() {
		return defaultUserAgent;
	}
	
	public static String getStudentFormPrefix() {
		return combro;
	}
	
	public static void newAccount() {
		account = new Family(context);
	}
}