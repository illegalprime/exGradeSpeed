package com.derangementinc.gradespeedmobile.data;

import java.util.ArrayList;
import java.util.List;

import com.derangementinc.gradespeedmobile.ConnectionManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Family {
	
	private static final int    STUDENT_NAME    =  0;
	private static final int    STUDENT_ID      =  1;
	private static final int    NO_CURRENT      = -1;
	private static final String KEY_FORM_VALUES = "urlFormValues";
	private static final String KEY_USERNAME    = "username";
	private static final String KEY_PASSWORD    = "password";
	private static final String KEY_STUDENTS    = "Students";
	private static final String KEY_CURRENT     = "currentStudent";
	private static final String KEY_STUDENT_FV  = "studentForm";
	
	private int currentStudent = NO_CURRENT;
	private ArrayList<Student> Students = new ArrayList<Student>();
	private String username      = "";
	private String password      = "";
	private String urlFormValues = "";
	private String studentForm   = "";
	public boolean guestSession  = true;
	
	private Context context;
	
	public Family(Context context) {
		this.context  = context;
		load();
	}
	
	public void addStudents(List<String[]> students) {
		Students = new ArrayList<Student>(students.size());
		
		for (String[] student : students) {
			Students.add(new Student(student[STUDENT_NAME], student[STUDENT_ID]));
		}
		saveStudents();
	}
	
	public String getStudentId(int index) {
		return Students.get(index).getId();
	}
	
	public String getCurrentId() {
		return getStudentId(currentStudent);
	}
	
	public void setCurrentStudent(int index) {
		if (index == currentStudent)
			return;
		
		currentStudent = index;
		
		if (guestSession)
			return;
		
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefsEdit.putInt(KEY_CURRENT, currentStudent);
		prefsEdit.commit();
	}
	
	public boolean hasOneChild() {
		return Students.size() < 2;
	}
	
	public CharSequence[] toCharSequence() {
		String[] names = new String[Students.size()];
		
		int i = -1;
		for (Student student : Students) {
			names[++i] = student.getName();
		}
		
		return names;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public void goShutterIslandOnThisBitch() {
		Students.clear();
	}
	
	public void addStudent(String name, String id) {
		Students.add(new Student(name, id));
		
		if (guestSession)
			return;
		
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefsEdit.putString(KEY_STUDENTS + (Students.size() - 1), Students.get(Students.size() - 1).parcelize());
		prefsEdit.commit();
	}
	
	public void saveStudents() {
		if (guestSession)
			return;
		
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		
		int i = -1;
		for (Student student : Students) {
			prefsEdit.putString(KEY_STUDENTS + ++i, student.parcelize());
		}
		prefsEdit.commit();
	}
	
	private void load() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		password = prefs.getString(KEY_PASSWORD, "");
		username = prefs.getString(KEY_USERNAME, "");
		urlFormValues = prefs.getString(KEY_FORM_VALUES, "");
		currentStudent = prefs.getInt(KEY_CURRENT, NO_CURRENT);
		
		String parcel = "";
		for (int i = 0; !(parcel = prefs.getString(KEY_STUDENTS + i, "NILL")).equals("NILL"); ++i) {
			Students.add(new Student(parcel));
		}
		
		if (hasCredentials()) {
			guestSession = false;
		}
	}
	
	public void wipeStoredData() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefsEdit = prefs.edit();
		
		prefsEdit.remove(KEY_CURRENT);
		prefsEdit.remove(KEY_FORM_VALUES);
		prefsEdit.remove(KEY_PASSWORD);
		prefsEdit.remove(KEY_STUDENT_FV);
		prefsEdit.remove(KEY_USERNAME);
		
		for (int i = 0; !prefs.getString(KEY_STUDENTS + i, "NILL").equals("NILL"); ++i) {
			prefsEdit.remove(KEY_STUDENTS + i);
		}
		
		prefsEdit.commit();
	}
	
	public void updateGradeForCurrent(String grade, int index) {
		if (guestSession)
			return;
		
		if (Students.get(currentStudent).isGradesOld(grade, index))
			return;
		
		Students.get(currentStudent).updateGrade(grade, index);
		
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefsEdit.putString(KEY_STUDENTS + currentStudent, Students.get(currentStudent).parcelize());
		prefsEdit.commit();
	}
	
	public boolean hasCredentials() {
		return !username.equals("") && !password.equals("") && !urlFormValues.equals(""); 
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getFormValues() {
		return urlFormValues;
	}
	
	public String getStudentFormValues() {
		return studentForm;
	}
	
	public void setStudentFormValues(String values) { 
		if (values.equals(studentForm))
			return;
		
		studentForm = values;
		
		if (guestSession)
			return;
		
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefsEdit.putString(KEY_STUDENT_FV, studentForm);
		prefsEdit.commit();
	}
	
	public void setCredentials(String user, String pass) {
		if (user.equals(username) && pass.equals(password))
			return;
		
		username = user;
		password = pass;
		
		//	Must clear all private data so new user (guest or permanent) cannot see
		
		goShutterIslandOnThisBitch();
		currentStudent = NO_CURRENT;
		urlFormValues = "";
		studentForm = "";
		
		if (guestSession)
			return;
		
		//	New user is coming in, delete all other user's data. 
		
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefsEdit.putString(KEY_USERNAME, username);
		prefsEdit.putString(KEY_PASSWORD, password);
		
		prefsEdit.remove(KEY_CURRENT);
		prefsEdit.remove(KEY_FORM_VALUES);
		prefsEdit.remove(KEY_STUDENT_FV);
		int i = -1;
		for (@SuppressWarnings("unused") Student student : Students) {
			prefsEdit.remove(KEY_STUDENTS + ++i);
		}
		
		prefsEdit.commit();
	}
	
	public void setFormValues(String values) {
		if (values.equals(urlFormValues))
			return;
		
		urlFormValues = values;
		
		if (guestSession)
			return;
		
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefsEdit.putString(KEY_FORM_VALUES, urlFormValues);
		prefsEdit.commit();
	}
	
	public void requestNewStudent() {
		currentStudent = NO_CURRENT;
	}
	
	public boolean isNewStudentRequested() {
		return currentStudent == NO_CURRENT; 
	}
	
	public void buildGradesTableForCurrent(String[][][] shortGrades, final int CURRENT_GRADE) {
		if (guestSession)
			return;
		
		if (ConnectionManager.ShortGrades.length != Students.get(currentStudent).getGradesSize()) {
			Students.get(currentStudent).buildGradesTable(shortGrades, CURRENT_GRADE);
			
			SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
			prefsEdit.putString(KEY_STUDENTS + currentStudent, Students.get(currentStudent).parcelize());
			prefsEdit.commit();
		}
	}
	
	public String isNewGradeForCurrent(String grade, int course) {
		if (guestSession) {
			return "";
		}
		else {
			return Students.get(currentStudent).isNewGrade(grade, course);
		}
	}

	public void createOnlyChild() {
		if (guestSession || Students.size() != 0)
			return;
		
		addStudent("default", "default");
		setCurrentStudent(0);				// Students.size() - 1
		// TODO: This will usually be zero, otherwise this could hide a logic error.
		// TODO: Stop the accumulation of students from re-logging in
	}
	
	public void loadPhonyGradesForCurrent() {
		Students.get(currentStudent).loadPhonies();
	}
}