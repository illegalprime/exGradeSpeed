package com.derangementinc.gradespeedmobile.data;

import java.util.ArrayList;

import com.derangementinc.gradespeedmobile.ConnectionManager;

public class Student {
	
	private String id;
	private String name;
	private ArrayList<String> pastGrades = new ArrayList<String>();
	
	private static String dataBreak = "%%";
	private static String lineBreak = "&&";
	
	public Student(String name, String id) {
		this.id = id;
		this.name = name;
	}
	
	public Student(String parcel) {
		unParcelize(parcel);
	}
	
	public String parcelize() {
		String parcel = "";
		parcel += id + lineBreak;
		parcel += name + lineBreak;
		
		if (pastGrades.size() != 0) {
			for (int i = 0; i < pastGrades.size() - 1; ++i) {
				parcel += pastGrades.get(i) + dataBreak;
			}
			
			parcel += pastGrades.get(pastGrades.size() - 1);
		}
		
		return parcel;
	}
	
	private void unParcelize(String parcel) {
		try {
		
		String[] vars = parcel.split(lineBreak);
		this.id = vars[0];
		this.name = vars[1];
		
		String[] grades = vars[2].split(dataBreak);
		
		if (grades.length > 1) {
			this.pastGrades = new ArrayList<String>(grades.length);
			
			for (String grade : grades) {
				pastGrades.add(grade);
			}
		}
		
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
	
	public String isNewGrade(String grade, int index) {
		String pastGrade = pastGrades.get(index);
		
		if (index < pastGrades.size()) {
			if (!pastGrade.equals(grade) && !grade.equals("")) {
				int change = 0;
				
				try {
					change = Integer.parseInt(grade) - Integer.parseInt(pastGrade); 
				} catch (NumberFormatException error) {
					return "NEW";
				}
				
				if (change > 0) {
					return "+" + change;
				}
				else {
					return  "" + change;
				}
			}
			else {
				return "";
			}
		}
		else {
			return "NEW";
		}
	}
	
	public boolean pastGradesEmpty() {
		return pastGrades.isEmpty();
	}
	
	public int getPastGradesLength() {
		return pastGrades.size();
	}
	
	public void addPastGrade(String grade) {
		pastGrades.add(grade);
	}
	
	public void clearPastGrades() {
		pastGrades.clear();
	}
	
	public void updateGrade(String grade, int index) {
		if (index < pastGrades.size()) {
			pastGrades.set(index, grade);
		}
		else {
			for (int len = pastGrades.size(); len < index; len++) {
				pastGrades.add("");
			}
			
			pastGrades.add(grade);
		}
	}
	
	public boolean isGradesOld(String grade, int index) {
		return pastGrades.get(index).equals(grade);
	}
	
	public void buildGradesTable(String[][][] shortGrades, final int CURRENT_GRADE) {
		pastGrades = new ArrayList<String>(shortGrades.length);
		
		for (int course = 0; course < shortGrades.length; ++course) {
			pastGrades.add(shortGrades[course][ConnectionManager.CURRENT_GRADE][ConnectionManager.TEXT]);
		}
	}
	
	public int getGradesSize() {
		return pastGrades.size();
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public void loadPhonies() {
		pastGrades.clear();
		pastGrades.add("90");
		pastGrades.add("90");
		pastGrades.add("90");
		pastGrades.add("90");
		pastGrades.add("90");
		pastGrades.add("90");
		pastGrades.add("90");
	}
}