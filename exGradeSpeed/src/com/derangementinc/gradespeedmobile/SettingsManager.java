package com.derangementinc.gradespeedmobile;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsManager {
	private static String languageEnum = "en-US";
	private static String defaultUserAgent = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36";
	private static String logInURL;
	private static String gradesURL;
	
	private static String  defaultURLForm;
	private static String  savedPass;
	private static String  savedUser;
	private static boolean isRemembered = false;
	private static boolean isCredentialsRemembered;
	private static Context context;
	
	private static List<String[]> Brothers = new LinkedList<String[]>();
	public static String defaultBrother = "";
	public static String brothersFormValues = "";
	private static final int    BROTHER_NAME = 0;
	private static final int    BROTHER_ID   = 1;
	private static final String dataBreak = "%%";
	private static final String rowBreak  = "&&";
	public  static final String combro    = "_ctl0_ddlStudents";
	
	public SettingsManager() {}
	
	public static void setCredentials(String user, String pass) {
		savedPass = pass;
		savedUser = user;
	}
	
	public static void setContext(Context cntxt) {
		context = cntxt;
	}
	
	public static void saveCredentials(String urlForm) {
		defaultURLForm = urlForm;
		isCredentialsRemembered = true;
		writeCredentials();
	}
	
	private static void writeCredentials() {
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		
		prefsEdit.putString("defaultURLForm", defaultURLForm);
		prefsEdit.putString("savedPass", savedPass);
		prefsEdit.putString("savedUser", savedUser);
		//prefsEdit.putBoolean("isRemembered", false);
		prefsEdit.putBoolean("isCredentialsRemembered", isCredentialsRemembered);
		
		prefsEdit.commit();
	}
	
	public static void saveURL(String url) {
		logInURL = url;
		writeURLs();
	}
	
	private static void writeURLs() {
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		
		prefsEdit.putString("logInURL", logInURL);
		prefsEdit.putString("gradesURL", "");
		
		prefsEdit.commit();
	}
	
	public static void load(Context cntxt, boolean loadBrothers) {
		setContext(cntxt);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		logInURL = prefs.getString("logInURL", "http://gradespeed.mnps.org/pc/Default.aspx");
		gradesURL = prefs.getString("gradesURL", "http://gradespeed.mnps.org/pc/ParentStudentGrades.aspx");
		defaultURLForm = prefs.getString("defaultURLForm", "");
		savedPass = prefs.getString("savedPass", "");
		savedUser = prefs.getString("savedUser", "");
		//isRemembered = prefs.getBoolean("isRemembered", false);
		isCredentialsRemembered = prefs.getBoolean("isCredentialsRemembered", false);
		
		// Brothers
		if (loadBrothers) {
			Brothers.clear();
			String brotherSet = prefs.getString("BrothersSet", "");
			if (brotherSet != "") {
				for (String entry : brotherSet.split(rowBreak)) {
					String[] data = entry.split(dataBreak);
					Brothers.add(data);
				}
				defaultBrother = prefs.getString("defaultBrother", "");
				brothersFormValues = prefs.getString("brothersFormValues", "");
			}
		}
	}
	
	public static String getLogInURL() {
		return logInURL;
	}
	
	public static String getUserAgent() {
		return defaultUserAgent;
	}
	
	public static String getLanguage() {
		return languageEnum;
	}

	public static String getGradesURL() {
		return gradesURL;
	}
	
	public static void setGradesURL(String url) {
		gradesURL = url;
		
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefsEdit.putString("gradesURL", url);
		prefsEdit.commit();
	}
	
	public static String getSavedURLForm() {
		return defaultURLForm;
	}
	
	public static String getSavedPassword() {
		return savedPass;
	}
	
	public static String getSavedUsername() {
		return savedUser;
	}

	public static boolean isRememberBoxChecked() {
		return isRemembered;
	}

	public static void setRememberBoxChecked(boolean isChecked) {
		isRemembered = isChecked;		
	}

	public static boolean isCredentialsRemembered() {
		return isCredentialsRemembered;
	}
	
	public static void credentialsIsNOTRemembered() {
		if (isCredentialsRemembered) {
			isCredentialsRemembered = false;
			SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
			prefsEdit.putBoolean("isCredentialsRemembered", false);
			prefsEdit.commit();
		}
	}
	//------------------//
	// BROS BEFORE EOFS //
	//------------------//
	public static void clearBrothers() {
		if (!isOnlyChild()) {
			Brothers.clear();
			defaultBrother = "";
			brothersFormValues = "";
			writeBroData();
		}
	}
	
	public static void addBrother(String name, String id) {
		String[] temp = new String[2];
		temp[BROTHER_NAME] = name;
		temp[BROTHER_ID]   = id;
		Brothers.add(temp);
	}

	public static String findDatBro(int brindex) {
		try {
			return Brothers.get(brindex)[BROTHER_ID];
		} catch (IndexOutOfBoundsException ex) {
			return "";
		}
	}
	
	public static CharSequence[] formatBrotherNames() {
		String[] names = new String[Brothers.size() + 1];
		
		int i = 0;
		for (String[] name : Brothers) {
			names[i++] = name[BROTHER_NAME];
		}
		names[i] = "Jean Valjean!";
		return names;
	}
	
	public static boolean isOnlyChild() {
		return Brothers.isEmpty();
	}
	
	public static void writeBroData() {
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		
		if (!isOnlyChild()) {
			String broData = "";
			for (String[] data : Brothers) {
				broData += data[BROTHER_NAME] + dataBreak + data[BROTHER_ID] + rowBreak;
			}
			prefsEdit.putString("BrothersSet", broData.substring(0, broData.length() - rowBreak.length()));
		}
		else {
			prefsEdit.putString("BrothersSet", "");
		}
		
		prefsEdit.putString("defaultBrother", defaultBrother);
		prefsEdit.putString("brothersFormValues", brothersFormValues);
		prefsEdit.commit();
	}
	
	public static void writeDefaultBrother() {
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefsEdit.putString("defaultBrother", defaultBrother);
		prefsEdit.commit();
	}

	public static void loadPhonies() {
		String[] student = {"Michael Eden", "190035708"};
		Brothers.add(student.clone());
		student[0] = "Ittai Eden";
		Brothers.add(student.clone());
	}
}