package com.derangementinc.gradespeedmobile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.StringEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ConnectionManager {
	private static String Cookies = new String(); 
	private static Map<String, String> logInFormValues = new HashMap<String, String>();
	
	public static List<String[]> ShortGrades = new ArrayList<String[]>(14);
	public static       int CURRENT_GRADE;
	public static final int TEACHER_NAME     = 0 ;
	public static final int TEACHER_EMAIL    = 1 ;
	public static final int COURSE_NAME      = 2 ;
	public static final int COURSE_PERIOD    = 3 ;
	public static final int CYCLE_1_GRADE    = 4 ;
	public static final int CYCLE_1_LINK     = 5 ;
	public static final int CYCLE_2_GRADE    = 6 ;
	public static final int CYCLE_2_LINK     = 7 ;
	public static final int SEMESTER_1_GRADE = 8 ;
	public static final int CYCLE_3_GRADE    = 9 ;
	public static final int CYCLE_3_LINK     = 10;
	public static final int CYCLE_4_GRADE    = 11;
	public static final int CYCLE_4_LINK     = 12;
	public static final int SEMESTER_2_GRADE = 13;
	
	public LinkedList<String[][]> LongGrades = new LinkedList<String[][]>();
	public LinkedList<String>  LongGradesHeaders = new LinkedList<String>();
	
	public static String error = "";
	
	public ConnectionManager() {}
	/*
	public static String debug(String user, String pass) {
		try {
			if (!findFormValues(user, pass)) {
				return "Could not find the form values";
			}
		} catch (UnsupportedEncodingException e) {
			return "Could not encode pass/user";
		}
		
		return verifyWithServer();
	}
	*/
	public static int logOn(String user, String pass) {
		
		//Log.i("gradespeed", "Starting LogOn Procedure");
		
		try {
			if (!findFormValues(user, pass))
				return 1;
		} catch (UnsupportedEncodingException e) {
			return 4;
		}
		
		//Log.i("gradespeed", "ended find form values. Verifying..");
		
		String homePageURL = verifyWithServer();
		if (homePageURL == "") {
			//Log.i("gradespeed", "verify failed.");
			return 2;
		}
		
		//Log.i("gradespeed", "end verifyWithServer(), checking for grades url in saved settings");
		
		if (SettingsManager.getGradesURL() == "") {
			//Log.i("gradespeed", "Finding the grades URL of the District..");
			getGradesPage(homePageURL);
			//Log.i("gradespeed", "got grades for unknown grade URL.");
		}
		String gradesURL = SettingsManager.getGradesURL();
		if (gradesURL == "") {
			//Log.i("gradespeed", "Could not find grades URL.");
			return 3;
		}
		else {
			if (SettingsManager.getDefaultBrother().equals("")) {
				Document gradesPage = getWebPage(gradesURL, false, true);
				if (findBros(gradesPage)) {
					getBroFormValues(gradesPage);
					return 5;
				}
				else {
					if (!parseForShortGrades(gradesPage))
						return 1;
				}
			}
			else {
				getGradesWithBro();
			}
			//Log.i("gradespeed", "parsing files for short grades.");
		}
		//Log.i("gradespeed", "Everything ran fine, finish.");
		return 0;
	}
	
	private static void getGradesPage(String url) {
		Element gradeLink = getWebPage(url, false, true).getElementById("lnkGrades");
		if (gradeLink != null) {
			SettingsManager.setGradesURL(url.substring(0, url.lastIndexOf('/') + 1) + gradeLink.attr("href"));
		}
	}
	
	private static boolean findFormValues(String username, String password) throws UnsupportedEncodingException {
			String language = SettingsManager.getLanguage();
			Cookies = "PCLanguage=Code=" + language.substring(0, 2) + "; PcLogin=Type=Parent; ";
			logInFormValues.clear();
			
			if (SettingsManager.isCredentialsRemembered()) {
				getWebPage(SettingsManager.getLogInURL(), true, false);
				logInFormValues.put("", SettingsManager.getSavedURLForm());
				return true;
			}
			
			//Log.i("gradespeed", "Getting webpage for form value parsing.");
			
			Document parsedLogIn = getWebPage(SettingsManager.getLogInURL(), true, true);
			if (parsedLogIn == (Document) null)
				return false;
			
			//Log.i("gradespeed", "Got cookies: '" + Cookies + "' for the session ID and now parsing for form values.");
			
			for (Element inputElement : parsedLogIn.getElementsByTag("input")) {
				String inputName = inputElement.attr("name");
				if (!(inputName.equals("txtUserName") || inputName.equals("txtPassword"))) {
					logInFormValues.put(inputName, URLEncoder.encode(inputElement.attr("value")));
					
					//Log.i("gradespeed", "Scanned and found '" + inputName + "=" + inputElement.attr("value") + "' Put into form values.");
				}
			}
			
			logInFormValues.put("txtUserName", URLEncoder.encode(username));
			logInFormValues.put("txtPassword", URLEncoder.encode(password));
			logInFormValues.put("ddlLanguage", URLEncoder.encode(language.substring(0, 2)));
			
			return true;
			// __EVENTTARGET=&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=%2FwEPDwULLTEyMzYwMTEzMTBkZA%3D%3D&__scrollLeft=0&__scrollTop=0&ddlDistricts=&txtUserName=eden&txtPassword=ab123456&ddlLanguage=en&btnLogOn=Log+On
	}

	/* Change to using HttpClient for compatibility.
	private static String verifyWithServer() {
		// Verify Session ID with User & Pass through the server
		URL url;
		HttpURLConnection connection = null;
		String redirectedURL = "";
		String urlParameters = buildUrlArgs(logInFormValues);
		
		Log.i("gradespeed", "Built URL Form args: " + urlParameters);
		
		try {

			// Create connection & properties.
			url = new URL(SettingsManager.getLogInURL());
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setChunkedStreamingMode(0);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", SettingsManager.getLanguage());
			//connection.setRequestProperty("User-Agent", SettingsManager.getUserAgent());
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setRequestProperty("Cookie", Cookies);
			
			connection.setRequestProperty("Connection", "close");
			
			Log.i("gradespeed", "Created new connection and request." + connection.getRequestProperties().toString());
			
			// Send Request and URL Parameters
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			
			Log.i("gradespeed", "Sent data.");
			
			// Get Response & Redirected URL
			// We don't care about the actual data, just the headers.
			/*
			InputStream in = new BufferedInputStream(connection.getInputStream());
		    BufferedReader rd = new BufferedReader(new InputStreamReader(in));
		    rd.close();
			
			if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
				redirectedURL = url.getProtocol() + "://" + url.getHost() + connection.getHeaderField("Location");
				
				if (SettingsManager.isRememberBoxChecked() && !SettingsManager.isCredentialsRemembered()) {
					SettingsManager.saveCredentials(urlParameters);
				}
			}
			else {
				redirectedURL = "";
			}

		} catch (Exception e) {
			e.printStackTrace();
			redirectedURL = "";
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		
		Log.i("gradespeed", "Redirect: " + redirectedURL);
		
		return redirectedURL;
	}
	*/
	
	private static String verifyWithServer() {
		HttpClient client  = new DefaultHttpClient();
		HttpPost   postReq = new HttpPost(SettingsManager.getLogInURL());
		String     urlParameters = buildUrlArgs(logInFormValues);
		
		try {
			client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
			
			StringEntity body = new StringEntity(urlParameters);
			body.setContentType("application/x-www-form-urlencoded");
			body.setContentEncoding("UTF-8");
			
			postReq.setEntity(body);
			postReq.addHeader("Cookie", Cookies);
			HttpResponse response = client.execute(postReq);
			
			if (response.getStatusLine().getStatusCode() == 302) {
				if (SettingsManager.isRememberBoxChecked() && !SettingsManager.isCredentialsRemembered()) {
					SettingsManager.saveCredentials(urlParameters);
				}
				
				return postReq.getURI().resolve(response.getHeaders("Location")[0].getValue()).toString();
			}
		} catch (Exception e) {}
		
		return "";
	}
	
	private static Document getWebPage(String url, boolean addCookies, boolean Parse) {
		URL logInPage;
		HttpURLConnection connection = null;
		String language = SettingsManager.getLanguage();
		Document response = null;
		try {
			
			// Create the new connection
			logInPage = new URL(url);
			connection = (HttpURLConnection) logInPage.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Cache-Control", "no-cache");
			connection.setRequestProperty("Content-Language", language);
			connection.setRequestProperty("User-Agent", SettingsManager.getUserAgent());
			connection.setRequestProperty("Accept", "text/html");
			connection.setRequestProperty("Accept-Encoding", ""); //: gzip,deflate,sdch\r\n
			connection.setUseCaches(false);
			connection.setDoInput(true);
			//connection.setDoOutput(true);
			connection.setRequestProperty("Cookie", Cookies);
			connection.setInstanceFollowRedirects(false);
			
			//Log.i("gradespeed", "Created new connection and request." + connection.getRequestProperties().toString());
			
			// Get Response & Cookies
			if (addCookies) {
				Cookies += connection.getHeaderField("Set-Cookie");
				if (!Parse)
					return response;
			}
			if ((connection.getResponseCode() != HttpURLConnection.HTTP_OK) || (!logInPage.getHost().equals(connection.getURL().getHost()))) 
				return (Document) null;
			
			response = Jsoup.parse(connection.getInputStream(), null, url);
			
		} catch (IOException e) {
			e.printStackTrace();
			return response;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return response;
	}
	
	private static boolean parseForShortGrades(Document gradesPage) {
		ShortGrades.clear();
		Element gradesTable = gradesPage.select("table[class=DataTable]").first();
		Iterator<Element> itGT;
		
		if (gradesTable != null) {
			itGT = gradesTable.getElementsByTag("tr").iterator();
		}
		else {
			error = gradesPage.html();
			return false;
		}
		
		itGT.next(); // First row is headers skip it.
		CURRENT_GRADE = ConnectionManager.CYCLE_1_GRADE;

		while (itGT.hasNext()) {
			// Fill in data values.
			String[] tempVals = new String[14];
			Element gradeRow = itGT.next();
			Iterator<Element> gradeCells = gradeRow.getElementsByTag("td").iterator();

			Element teacher  = gradeRow.getElementsByClass("EmailLink").first();
			tempVals[ConnectionManager.TEACHER_NAME] = teacher.html();
			String emailURL = teacher.attr("href");
			tempVals[ConnectionManager.TEACHER_EMAIL] = emailURL.substring(emailURL.lastIndexOf(":") + 1);

			gradeCells.next();  // First value is "Notes" Section, skip.
			tempVals[ConnectionManager.COURSE_NAME] = gradeCells.next().html();
			tempVals[ConnectionManager.COURSE_PERIOD] = gradeCells.next().html(); 
			
			try {
			for (int tdIndex = ConnectionManager.CYCLE_1_GRADE; gradeCells.hasNext(); tdIndex++) {
				Element gradeCell = gradeCells.next();

				Elements value = gradeCell.getElementsByTag("a");
				if (!value.isEmpty()) {
					tempVals[tdIndex] = value.first().html();
					if (CURRENT_GRADE < tdIndex)
						CURRENT_GRADE = tdIndex;
					
					tdIndex++;
					tempVals[tdIndex] = value.first().attr("href");
				}
				else {
					value = gradeCell.getElementsByTag("span");
					if (!value.isEmpty()) {
						tempVals[tdIndex] = value.first().html();
					}
					else {
						if (tdIndex == ConnectionManager.SEMESTER_1_GRADE || tdIndex == ConnectionManager.SEMESTER_2_GRADE) {
							tempVals[tdIndex] = "";
						}
						else {
							tempVals[tdIndex] = "";
							tdIndex++;
							tempVals[tdIndex] = "";
						}
					}
				}
			}
			} catch (ArrayIndexOutOfBoundsException e) {}
			// TODO: Find out why the index goes out of bounds.

			ShortGrades.add(tempVals);
		}
		
		return true;
	}
	
	private static String buildUrlArgs(Map<String, String> args) {
		Iterator<String> itt = args.keySet().iterator();
		String key      = itt.next();
		String finalURL = key + "=" + args.get(key);
		while (itt.hasNext()) {
			key = itt.next();
			finalURL += "&" + key + "=" + args.get(key);
		}
		
		return finalURL;
	}
	
	private String[][] buildTable(Element table) {
		Elements rows    = table.getElementsByTag("tr");
		Elements columns = rows.first().getElementsByTag("th");
		String[][] compiledT = new String[rows.size()][columns.size()];
		
		int columnI = 0;
		int rowI    = 0;
		for (Element row : rows) {
			columns = row.getElementsByTag("td");
			columnI = 0;
			if (columns.isEmpty())
				columns = row.getElementsByTag("th");
			
			for (Element column : columns) {
				compiledT[rowI][columnI] = column.html(); 
				columnI++;
			}
			rowI++;
		}
		
		return compiledT;
	}
	
	public boolean getLongGrades(String cycleURL) {
		Document courseGrades = getWebPage(SettingsManager.getGradesURL() + cycleURL, false, true);
		if (courseGrades == (Document) null)
			return false;
		
		Elements maybeTables = courseGrades.getElementsByClass("DataTable");
		if (maybeTables.size() < 2) {
			return false;
		}
		
		Iterator<Element> tables = maybeTables.iterator();
		tables.next(); // first element is grades.
		while (tables.hasNext()) {
			LongGrades.add(buildTable(tables.next()));
		}
		
		for (Element description : courseGrades.getElementsByClass("CategoryName")) {
			LongGradesHeaders.add(description.html());
		}
		
		return true;
	}
	
	private static boolean findBros(Document doc) {
		Element comboBros = doc.getElementById(SettingsManager.combro);
		
		if (comboBros == null)
			return false;
		
		SettingsManager.clearBrothers();
		for (Element child : comboBros.children()) {
			if (child.tagName().equals("option")) {
				SettingsManager.addBrother(child.html(), child.attr("value"));
			}
		}
		
		return true;
	}
	
	public static int getGradesWithBro() {
		String gradesURLagain = sendSwitchBroRequest(SettingsManager.getDefaultBrother());
		
		if (gradesURLagain != "") {
			if (parseForShortGrades(getWebPage(gradesURLagain, false, true)))
				return 0;
			else
				return 1;
		}
		return 1;
	}
	
	private static void getBroFormValues(Document doc) {
		for (Element input : doc.getElementsByTag("input")) {
			SettingsManager.brothersFormValues += input.attr("name") + "=" + URLEncoder.encode(input.attr("value")) + "&";
		}
		SettingsManager.brothersFormValues += "_ctl0%3AddlStudents=";  
	}
	
	private static String sendSwitchBroRequest(String studentID) {
		HttpClient client  = new DefaultHttpClient();
		HttpPost   postReq = new HttpPost(SettingsManager.getGradesURL());
		
		try {
			client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
			
			StringEntity body = new StringEntity(SettingsManager.brothersFormValues + studentID);
			body.setContentType("application/x-www-form-urlencoded");
			body.setContentEncoding("UTF-8");
			
			postReq.setEntity(body);
			postReq.addHeader("Cookie", Cookies);
			HttpResponse response = client.execute(postReq);
			
			if (response.getStatusLine().getStatusCode() == 302) { 
				return postReq.getURI().resolve(response.getHeaders("Location")[0].getValue()).toString();
			}
		} catch (Exception e) {}
		
		return "";
	}
	/*
	 __EVENTTARGET=_ctl0%24ddlStudents
	&__EVENTARGUMENT=
	&__LASTFOCUS=
	&__VIEWSTATE=%2FwEPDwUKLTkwNTg4MzgyMg9kFgJmD2QWBAIBD2QWAgIDDxYCHgRUZXh0BUk8bGluayBocmVmPSJTdHlsZVNoZWV0cy9QQ01hc3Rlci5jc3MiIHR5cGU9InRleHQvY3NzIiByZWw9InN0eWxlc2hlZXQiIC8%2BZAIDD2QWAgIBD2QWBgIBD2QWAmYPFgQeB2NvbHNwYW4FATIeCWlubmVyaHRtbAUWJm5ic3A7UGFyZW50Q29ubmVjdGlvbmQCAw9kFg4CAw8WAh8ABRhOYXNodmlsbGUgUHVibGljIFNjaG9vbHNkAgUPFgIfAAVmPGJyIC8%2BPGEgaHJlZj1odHRwOi8vd3d3LnBlYXJsY29obmhzLm1ucHMub3JnL1BhZ2U4MjMxLmFzcHggdGFyZ2V0PSJfYmxhbmsiPlBlYXJsIENvaG4gSGlnaCBTY2hvb2w8L2E%2BZAIJDxBkEBUDE0l6YWd1aXJyZSwgQ2F0YWxpbmEYSXphZ3VpcnJlLCBGZWxpcGUgRGFuaWVsGEl6YWd1aXJyZSwgTHVjYXMgSWduYWNpbxUDCTE5MDE1NzQ1MAkxOTAwNzI1NTYJMTkwMDE4NjcxFCsDA2dnZxYBZmQCDQ8PFgIeB1Zpc2libGVoZGQCEQ8WAh8ABY8IICAgICAgICAgICAgICAgPHVsIGNsYXNzPSJOYXZCYXJMaW5rcyI%2BDQogICAgICAgICAgICAgICAgICA8bGkgaWQ9Imxua0Fzc2lnbm1lbnRzQnVsbGV0Ij48YSBocmVmPSJQYXJlbnRBc3NpZ25tZW50cy5hc3B4IiBpZD0ibG5rQXNzaWdubWVudHMiPkFzc2lnbm1lbnRzPC9hPjwvbGk%2BDQogICAgICAgICAgICAgICAgICA8bGkgaWQ9Imxua0dyYWRlc0J1bGxldCI%2BPGEgaHJlZj0iUGFyZW50U3R1ZGVudEdyYWRlcy5hc3B4IiBpZD0ibG5rR3JhZGVzIj5HcmFkZXM8L2E%2BPC9saT4NCiAgICAgICAgICAgICAgICAgIDxsaSBpZD0ibG5rQXR0ZW5kYW5jZUJ1bGxldCI%2BPGEgaHJlZj0iUGFyZW50U3R1ZGVudEF0dGVuZC5hc3B4IiBpZD0ibG5rQXR0ZW5kYW5jZSI%2BQXR0ZW5kYW5jZTwvYT48L2xpPg0KICAgICAgICAgICAgICAgICAgPGxpIGlkPSJsbmtUcmlnZ2Vyc0J1bGxldCI%2BPGEgaHJlZj0iUGFyZW50TWFuYWdlVHJpZ2dlcnMuYXNweCIgaWQ9Imxua1RyaWdnZXJzIj5UcmlnZ2VyczwvYT48L2xpPg0KICAgICAgICAgICAgICAgICAgPGxpIGlkPSJsbmtDb3Vyc2VSZXF1ZXN0c0J1bGxldCI%2BPGEgaHJlZj0iU3R1ZGVudENvdXJzZVJlcXVlc3RzLmFzcHgiIGlkPSJsbmtDb3Vyc2VSZXF1ZXN0cyI%2BQ291cnNlIFJlcXVlc3RzPC9hPjwvbGk%2BDQogICAgICAgICAgICAgICAgICA8bGkgaWQ9Imxua0NhbGVuZGFyQnVsbGV0Ij48YSBocmVmPSJQYXJlbnRDYWxlbmRhci5hc3B4IiBpZD0ibG5rQ2FsZW5kYXIiPkNhbGVuZGFyPC9hPjwvbGk%2BDQogICAgICAgICAgICAgICAgICA8bGkgaWQ9Imxua01hbmFnZVN0dWRlbnRzQnVsbGV0Ij48YSBocmVmPSJQYXJlbnRNYW5hZ2VTdHVkZW50cy5hc3B4IiBpZD0ibG5rTWFuYWdlU3R1ZGVudHMiPk1hbmFnZSBTdHVkZW50czwvYT48L2xpPg0KICAgICAgICAgICAgICAgICAgPGxpIGlkPSJsbmtNeVNldHRpbmdzQnVsbGV0Ij48YSBocmVmPSJQYXJlbnRNeVNldHRpbmdzLmFzcHgiIGlkPSJsbmtNeVNldHRpbmdzIj5NeSBTZXR0aW5nczwvYT48L2xpPg0KICAgICAgICAgICAgICAgPC91bD4NCmQCFQ8PFgIfAAUIcGFwdWxpbm9kZAIZDxYCHwBlZAIFDxYEHgVhbGlnbgUEbGVmdB4FY2xhc3MFD01haW5Db250ZW50TGVmdBYCAgEPZBYCAgMPFgIfAGVkZOrp84a1%2FVpNbg1%2BvzGgDSmBt6Bz
	&__scrollLeft=0
	&__scrollTop=0 
	&__EVENTVALIDATION=%2FwEWBgKAoIy8AgLdgY3LBwLu6s7OCALL1LwrAt%2BbxTsCiKOqhQvSxYQUv4zZqKC7HYXLc3rt089Gpg%3D%3D
	&__RUNEVENTTARGET=
	&__RUNEVENTARGUMENT=
	&__RUNEVENTARGUMENT2=
	&_ctl0%3AddlStudents=190018671
	*/
}