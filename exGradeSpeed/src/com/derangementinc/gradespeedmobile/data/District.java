package com.derangementinc.gradespeedmobile.data;

public class District {
	
	public  static final int HAS_NO_CERTIFICATE = -1;
	
	private String name      = ""; 
	private String logInURL  = "";
	private String gradesURL = "";
	
	private int certificate_id = HAS_NO_CERTIFICATE;
	
	public District() {}
	
	public District(String parcel) {
		unparcelize(parcel);
	}
	
	public District(String name, String logInURL, String gradesURL, int certificate) {
		this.name           = name;
		this.logInURL       = logInURL;
		this.gradesURL      = gradesURL;
		this.certificate_id = certificate;
	}
	
	private void unparcelize(String parcel) {
		try {
			String[] data = parcel.split("\n");
			this.name           = data[0];
			this.logInURL       = data[1];
			this.gradesURL      = data[2];
			this.certificate_id = Integer.parseInt(data[3]);
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
	
	public  String parcelize() {
		return name + "\n" + logInURL + "\n" + gradesURL + "\n" + certificate_id;  
	}
	
	public String getName() {
		return name;
	}
	
	public String getLogInURL() {
		return logInURL;
	}
	
	public String getGradesURL() {
		return gradesURL;
	}
	
	public int getCertificateId() {
		return certificate_id;
	}
	
	public void setLogInURL(String url) {
		this.logInURL = url;
	}
	
	public void setGradesURL(String url) {
		this.gradesURL = url;
	}
	
	// TODO: Find out how to get and save an unknown grades URL to the system
}