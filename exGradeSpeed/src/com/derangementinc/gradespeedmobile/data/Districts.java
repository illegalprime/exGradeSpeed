package com.derangementinc.gradespeedmobile.data;

import java.util.ArrayList;

import com.derangementinc.gradespeedmobile.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Districts {
	
	private static final String TAG_CD       = "custom_district_";
	private static final String KEY_DISTRICT = "currentDistrict"; 
	public  static final int    NO_CURRENT   = -1;
	
	private int      currentIndex    = NO_CURRENT;
	private District currentDistrict;
	
	// public final String[] KellerISD    = {"Keller ISD", ""};
	// public final String[] DentonISD    = {"Denton ISD", "", ""};
	// public final String[] Ballinger    = {"Ballinger", "https://gs.ballingerisd.net/PC/studentlogin.aspx", ""};
	// TODO: On the above there is a combo list needing parsing
	
	private District[] Districts = {new District("MNPS",           "https://gradespeed.mnps.org/pc/Default.aspx",        "https://gradespeed.mnps.org/pc/ParentStudentGrades.aspx",  R.raw.der_mnps_cert),
									new District("DODEA",          "https://dodea.gradespeed.net/pc/default.aspx",       "https://dodea.gradespeed.net/pc/ParentStudentGrades.aspx", R.raw.der_dodea_cert),
									new District("Austin ISD",     "https://gradespeed.austinisd.org/pc/",               "", R.raw.der_austin_isd_cert),
									new District("Williamson CS",  "https://parentconnection.wcs.edu/",                  "", R.raw.der_wcs_cert),
									new District("Klein ISD",      "https://gradespeed.kleinisd.net/pc/Default.aspx",    "", NO_CURRENT),
									new District("Dallas ISD",     "https://gradespeed.dallasisd.org/pc/",               "", NO_CURRENT),
									new District("Round Rock ISD", "https://gradebook.roundrockisd.org/pc/Default.aspx", "", R.raw.der_roundrock_isd_cert),
									new District("SCUC ISD",       "https://gsnet.scuc.txed.net/pc/",                    "", NO_CURRENT)
									};
	
	private ArrayList<String>   formated_districts = null;
	private ArrayList<District> custom_districts   = new ArrayList<District>();
	
	private Context context;
	
	public Districts(Context context) {
		setContext(context);
		load();
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public ArrayList<String> format() {
		int NoOfElements = Districts.length + custom_districts.size() + 1;
		
		if (formated_districts == null || formated_districts.size() != NoOfElements) {
			
			formated_districts = new ArrayList<String>(NoOfElements);
			
			for (int i = 0; i < Districts.length; ++i) {
				String name = Districts[i].getName();
				if (Districts[i].getGradesURL().equals("")) {
					name += " *";
				}
				
				formated_districts.add(name);
			}
			
			for (int i = 0; i < custom_districts.size(); ++i) {
				formated_districts.add(custom_districts.get(i).getName());
			}
			
			formated_districts.add("Add a new District...");
		}
		return formated_districts;
	}
	
	public int defaultSize() {
		return Districts.length;
	}
	
	public int customSize() {
		return custom_districts.size();
	}
	
	public void addNewDistrict(String name, String mainURL, String gradesURL) {
		//Log.i("exGradeSpeed addNewDistrict: ", name + "\n" + mainURL + "\n" + gradesURL);
		int index;
		int location = findCustomDistrict(name);
		District temp = new District(name, mainURL, gradesURL, District.HAS_NO_CERTIFICATE);
		
		if (location == -1) {
			custom_districts.add(temp);
			index = customSize() - 1;
		}
		else {
			custom_districts.get(location).setLogInURL(mainURL);
			custom_districts.get(location).setGradesURL(gradesURL);
			index = location;
		}
		
		//Log.i("exGradeSpeed addNewDistrict(temp_parcel): ", temp.parcelize());
		//Log.i("exGradeSpeed addNewDistrict(csdt_parcel): ", custom_districts.get(index).parcelize());
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefsEdit.putString(TAG_CD + index, temp.parcelize());
		prefsEdit.commit();
	}
	
	public boolean removeDistrict(String name) {
		boolean removed = false;
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		
		for (int i = 0; i < custom_districts.size(); ++i) {
			
			if (custom_districts.get(i).getName().equals(name)) {
				custom_districts.remove(i);
				
				prefsEdit.remove(TAG_CD + i);
				removed = true;
			}
			else if (removed) {
				District temp = custom_districts.get(i);
				prefsEdit.putString(TAG_CD + (i - 1), temp.parcelize());
			}
		}
		
		prefsEdit.commit();
		return removed;
	}
	
	private void load() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		int i = -1;
		String cd = prefs.getString(TAG_CD + ++i, "");
		
		while (!cd.equals("")) {
			custom_districts.add(new District(cd));
			cd = prefs.getString(TAG_CD + ++i, ""); 
		}
		
		currentIndex = prefs.getInt(KEY_DISTRICT, 0);
		currentDistrict = getDistrict(currentIndex);
	}
	
	public String getCustomName(int index) {
		return custom_districts.get(index).getName();
	}
	
	public String getCustomMainURL(int index) {
		return custom_districts.get(index).getLogInURL();
	}
	
	public String getCustomGradesURL(int index) {
		return custom_districts.get(index).getGradesURL();
	}
	
	public int findCustomDistrict(String name) {
		for (int i = 0; i < customSize(); ++i) {
			if (custom_districts.get(i).getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public District getDistrict(int index) {
		int position = index;
		
		if (position < Districts.length) {
			return Districts[position];
		}
		
		position -= Districts.length;
		if (position < custom_districts.size()) {
			return custom_districts.get(position);
		}
		
		return (District) null;
	}
	
	public void setCurrentDistrict(int index) {
		currentIndex = index;
		
		if (index != NO_CURRENT) {
			District current = getDistrict(index);
			
			if (current != null) {
				currentDistrict = current;
			}
		}
		
		saveCurrentDistrict(index);
	}
	
	private void saveCurrentDistrict(int index) {
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefsEdit.putInt(KEY_DISTRICT, index);
		prefsEdit.commit();
	}
	
	public String getName(int index) {
		return getDistrict(index).getName();
	}
	
	public String getMainURL(int index) {
		return getDistrict(index).getLogInURL();
	}
	
	public String getGradesURL(int index) {
		return getDistrict(index).getGradesURL();
	}
	
	public String getCurrentLogInURL() {
		return currentDistrict.getLogInURL();
	}
	
	public String getCurrentGradesURL() {
		return currentDistrict.getGradesURL();
	}
	
	public int getCurrentCertificateId() {
		return currentDistrict.getCertificateId();
	}
	
	public void setCurrentGradesURL(String url) {
		currentDistrict.setGradesURL(url);
	}
	
	public boolean currentDistrictHasCertificate() {
		return currentDistrict.getCertificateId() != District.HAS_NO_CERTIFICATE;
	}
}