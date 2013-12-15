package com.derangementinc.gradespeedmobile;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.derangementinc.gradespeedmobile.enums.Errors;
import com.derangementinc.gradespeedmobile.R;

public class ConnectionManager {
	private static SSLContext SecureContext = null;
	private static String Cookies = new String(); 
	private static Map<String, String> logInFormValues = new HashMap<String, String>();
	
	// public static ArrayList<String[]> ShortGrades = new ArrayList<String[]>(14);
	// TODO: I don't know what the '14' in the constructor does...
	public static String[][][] ShortGrades;
	
	public static       int CURRENT_GRADE;
	public static final int TEACHER_NAME     = 0 ;
	public static final int NOTE             = 1 ;
	public static final int COURSE_NAME      = 2 ;
	public static final int COURSE_PERIOD    = 3 ;
	public static final int CYCLE_1_GRADE    = 4 ;
	public static final int CYCLE_2_GRADE    = 5 ;
	public static final int EXAM_MIDTERM     = 6 ;
	public static final int SEMESTER_1_GRADE = 7 ;
	public static final int CYCLE_3_GRADE    = 8 ;
	public static final int CYCLE_4_GRADE    = 9 ;
	public static final int EXAM_FINAL       = 10;
	public static final int SEMESTER_2_GRADE = 11;	// Warning: When changing these also change the 'order' attribute in cycle_menu.xml 
	
	public static final int TEXT = 0;
	public static final int URL  = 1;
	
	//public static final int CYCLE_4_LINK     = 12;
	//public static final int CYCLE_3_LINK     = 10;
	//public static final int CYCLE_2_LINK     = 7 ;
	//public static final int CYCLE_1_LINK     = 5 ;
	//public static final int TEACHER_EMAIL    = 1 ;
	
	public static LinkedList<String[][]> LongGrades = new LinkedList<String[][]>();
	public static LinkedList<String>  LongGradesHeaders = new LinkedList<String>();
	
	public static String error = "";
	private static Errors ErrorStream = Errors.NONE;
	
	public ConnectionManager() {}
	
	public static Errors logIn(String user, String pass, Activity activity) {
		ErrorStream = Errors.NONE;		// So far so good..
		
		if (!isNetworkAvailable(activity)) {
			return Errors.NO_WIFI;
		}
		
		// Check if this district has a certificate 
		if (SettingsManager.districts.currentDistrictHasCertificate()) {
			// Manually trust this certificate on file.
			loadPrivateSSLCertificate(activity, SettingsManager.districts.getCurrentCertificateId());
		}
		
		try {
			
			ErrorStream = findFormValues(user, pass);
			
			if (ErrorStream.equals(Errors.HTTP_NOT_FOUND)) {
				return Errors.INVALID_DISTRICT_URL;
			}
			else if (!ErrorStream.equals(Errors.NONE)) {
				return ErrorStream;
			}
		} catch (UnsupportedEncodingException e) {
			return Errors.ENCODING_ERROR;
		}
		
		
		String homePageURL = verifyWithServerWithSSL();
		if (!ErrorStream.equals(Errors.NONE)) {
			return ErrorStream;
		}
		
		
		if (SettingsManager.districts.getCurrentGradesURL().equals("")) {
			SettingsManager.districts.setCurrentGradesURL(getGradesPage(homePageURL));
		}
		String gradesURL = SettingsManager.districts.getCurrentGradesURL();
		
		if (gradesURL.equals("")) {
			return Errors.COULD_NOT_FIND_GRADES_PAGE;
		}
		else {
			
			if (SettingsManager.account.hasOneChild()) {
				
				Document gradesPage = getWebPage(gradesURL, false, true);
				if (gradesPage == null) {
					return Errors.COULD_NOT_PARSE_FOR_GRADES;
				}
				ErrorStream = findBros(gradesPage);
				
				if (ErrorStream.equals(Errors.FOUND_SIBLINGS)) {
					
					try {
						getBroFormValues(gradesPage);
					}
					catch (UnsupportedEncodingException e) {
						return Errors.ENCODING_ERROR;
					}
					
					return Errors.FOUND_SIBLINGS;
				}
				else {
					ErrorStream = parseForShortGrades(gradesPage);
					
					if (!ErrorStream.equals(Errors.NONE)) {
						return ErrorStream;
					}
				}
			}
			else {
				ErrorStream = getGradesWithBro(activity);
				
				if (!ErrorStream.equals(Errors.NONE)) {
					return ErrorStream;
				}
			}

		}
		
		ErrorStream = Errors.NONE;
		return Errors.NONE;
	}
	
	private static Document getWebPage(String url, boolean addCookies, boolean Parse, boolean followRedirects) {
		HttpURLConnection connection = null;
		Document response = (Document) null;
		
		try {
			// Create the new connection
			URL logInPage = new URL(url);
			connection = (HttpURLConnection) logInPage.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Cache-Control", "no-cache");
			connection.setRequestProperty("Content-Language", SettingsManager.getLanguage());
			connection.setRequestProperty("User-Agent", SettingsManager.getUserAgent());
			connection.setRequestProperty("Accept", "text/html");
			connection.setRequestProperty("Accept-Encoding", ""); //: gzip,deflate,sdch\r\n
			connection.setUseCaches(false);
			connection.setDoInput(true);
			//connection.setDoOutput(true);
			connection.setRequestProperty("Cookie", Cookies);
			connection.setInstanceFollowRedirects(followRedirects);
			
			if (logInPage.getProtocol().equals("https") && SettingsManager.districts.currentDistrictHasCertificate()) {
				((HttpsURLConnection) connection).setSSLSocketFactory(SecureContext.getSocketFactory());
			}
			
			// Get Response & Cookies
			if (addCookies) {
				Cookies += connection.getHeaderField("Set-Cookie");
				if (!Parse)
					return (Document) null;		// Don't care about the return value, just run the request.
			}
			
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {		// Everything went swimmingly.
				response = Jsoup.parse(connection.getInputStream(), null, url);
				ErrorStream = Errors.NONE;
			}	
			else if (!logInPage.getHost().equals(connection.getURL().getHost())) {	// Captive Portal
				ErrorStream = Errors.HTTP_CAPTIVE_PORTAL;
			}
			else if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {		// TODO: Does this mean no WiFi?
				ErrorStream = Errors.HTTP_NOT_FOUND;
			}
			else {
				error = connection.getResponseCode() + ": " + connection.getResponseMessage();
				ErrorStream = Errors.UNKNOWN_HTTP_ERROR;
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
			return response;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		
		if (Parse && response == (Document) null) {
			ErrorStream = Errors.COULD_NOT_PARSE_PAGE;
		}
		return response;
	}
	
	private static Document getWebPage(String url, boolean addCookies, boolean Parse) {
		return getWebPage(url, addCookies, Parse, false);
	}
	
	private static String securePOST(String urlParameters, boolean saveFormValues) {
		HttpURLConnection connection = null;
		URL logInURL;
		String language = SettingsManager.getLanguage();
		String homepageURL = "";
		
		try {
			
			// Create the new connection
			logInURL = new URL(SettingsManager.districts.getCurrentLogInURL());
			connection = (HttpURLConnection) logInURL.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", language);
			connection.setUseCaches(false);
			//connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setDoOutput(true);
			connection.setRequestProperty("Cookie", Cookies);
			
			if (logInURL.getProtocol().equals("https") && SettingsManager.districts.currentDistrictHasCertificate()) {
				((HttpsURLConnection) connection).setSSLSocketFactory(SecureContext.getSocketFactory());
			}
			
			DataOutputStream netWr = new DataOutputStream(connection.getOutputStream());
			netWr.writeBytes(urlParameters);
			netWr.flush();
			netWr.close();
			
			if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {	// 302 Redirect User and Pass were right
				homepageURL = logInURL.toURI().resolve(connection.getHeaderField("Location")).toString();
				if (saveFormValues) {
					SettingsManager.account.setFormValues(urlParameters);
				}
				ErrorStream = Errors.NONE;
			}
			else if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				ErrorStream = Errors.INVALID_USER_PASS;
			}
			else {
				error = connection.getResponseCode() + ": " + connection.getResponseMessage();
				ErrorStream = Errors.UNKNOWN_HTTP_ERROR;
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
			ErrorStream = Errors.UNKNOWN_HTTP_ERROR;
		} 
		catch (URISyntaxException e) {
			e.printStackTrace();
			ErrorStream = Errors.INVALID_DISTRICT_URL;
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		
		return homepageURL;
	}
	
	public static String verifyWithServerWithSSL() {
		return securePOST(buildUrlArgs(logInFormValues), true);
	}
	
	public static String sendSwitchBroRequest(String studentID) {
		return securePOST(SettingsManager.account.getStudentFormValues() + studentID, false);
	}
	
	private static String getGradesPage(String url) {
		Element gradeLink = getWebPage(url, false, true).getElementById("lnkGrades");
		
		if (gradeLink != null) {
			return url.substring(0, url.lastIndexOf('/') + 1) + gradeLink.attr("href");
		}
		return "";
	}
	
	private static Errors findFormValues(String username, String password) throws UnsupportedEncodingException {
			String language = SettingsManager.getLanguage();
			Cookies = "PCLanguage=Code=" + language.substring(0, 2) + "; PcLogin=Type=Parent; ";
			logInFormValues.clear();
			
			if (SettingsManager.account.hasCredentials()) {
				getWebPage(SettingsManager.districts.getCurrentLogInURL(), true, false, true);
				logInFormValues.put("", SettingsManager.account.getFormValues());
				
				if (!ErrorStream.equals(Errors.NONE)) {
					return ErrorStream;
				}
				
				return Errors.NONE;
			}
			
			Document parsedLogIn = getWebPage(SettingsManager.districts.getCurrentLogInURL(), true, true, true);
			if (!ErrorStream.equals(Errors.NONE))
				return ErrorStream;
			
			if (parsedLogIn == (Document) null)
				return Errors.COULD_NOT_REACH_PAGE;
			
			for (Element inputElement : parsedLogIn.getElementsByTag("input")) {
				String inputName = inputElement.attr("name");
				
				if (!(inputName.equals("txtUserName") || inputName.equals("txtPassword"))) {
					logInFormValues.put(inputName, URLEncoder.encode(inputElement.attr("value"), "UTF-8"));
				}
			}
			
			logInFormValues.put("txtUserName", URLEncoder.encode(username, "UTF-8"));
			logInFormValues.put("txtPassword", URLEncoder.encode(password, "UTF-8"));
			logInFormValues.put("ddlLanguage", URLEncoder.encode(language.substring(0, 2), "UTF-8"));
			
			return Errors.NONE;
	}
	
	public static Errors getLongGrades(String cycleURL) {
		clearLongGrades();
		
		Document courseGrades = getWebPage(SettingsManager.districts.getCurrentGradesURL() + cycleURL, false, true);
		if (!ErrorStream.equals(Errors.NONE))
			return ErrorStream;
		
		Elements maybeTables = courseGrades.getElementsByClass("DataTable");
		if (maybeTables.size() < 2) {
			return Errors.COULD_NOT_PARSE_FOR_SPECIFIC_GRADES;
		}
		
		Iterator<Element> tables = maybeTables.iterator();
		tables.next(); // first element is grades.
		while (tables.hasNext()) {
			LongGrades.add(buildTable(tables.next()));
		}
		
		for (Element description : courseGrades.getElementsByClass("CategoryName")) {
			LongGradesHeaders.add(description.html());
		}
		
		return Errors.NONE;
	}
	
	private static void clearLongGrades() {
		LongGrades.clear();
		LongGradesHeaders.clear();
	}
	
	public static Errors getGradesWithBro(Activity activity) {
		ErrorStream = Errors.NONE;
		
		if (!isNetworkAvailable(activity)) {
			return Errors.NO_WIFI;
		}
		
		String gradesURLagain = sendSwitchBroRequest(SettingsManager.account.getCurrentId());
		
		if (!ErrorStream.equals(Errors.NONE))
			return ErrorStream;
		
		Document gradesAgain = getWebPage(gradesURLagain, false, true);
		
		if (!ErrorStream.equals(Errors.NONE))
			return ErrorStream;
		
		ErrorStream = Errors.NONE;
		return parseForShortGrades(gradesAgain);
	}
	
	
	public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivity == null) {
            return false;
        } 
        else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
            	
                for (int i = 0; i < info.length; i++) {
                	
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
	
	private static Errors findBros(Document doc) {
		if (!SettingsManager.account.hasOneChild())
			return Errors.FOUND_SIBLINGS;
		
		Element comboBros = doc.getElementById(SettingsManager.getStudentFormPrefix());
		
		if (comboBros == null)
			return Errors.COULD_NOT_PARSE_FOR_OTHER_STUDENTS;
		
		for (Element child : comboBros.children()) {
			if (child.tagName().equals("option")) {
				SettingsManager.account.addStudent(child.html(), child.attr("value"));
			}
		}
		
		return Errors.FOUND_SIBLINGS;
	}
	
	private static void getBroFormValues(Document doc) throws UnsupportedEncodingException {
		String formValues = "";
		
		for (Element input : doc.getElementsByTag("input")) {
			formValues += input.attr("name") + "=" + URLEncoder.encode(input.attr("value"), "UTF-8") + "&";
		}
		formValues += "_ctl0%3AddlStudents=";
		
		SettingsManager.account.setStudentFormValues(formValues);
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
	
	private static String[][] buildTable(Element table) {
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
	
	private static void loadPrivateSSLCertificate(Activity activity, int res_id) {
		try {
			// Load CAs from an InputStream
			// (could be from a resource or ByteArrayInputStream or ...)
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			// Raw Certificate File /res/raw/...
			InputStream caInput = new BufferedInputStream(activity.getResources().openRawResource(res_id));
			Certificate ca;
			try {
				ca = cf.generateCertificate(caInput);
				((X509Certificate) ca).getSubjectDN();
			} finally {
				caInput.close();
			}
			
			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);
			
			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);
			
			// Create an SSLContext that uses our TrustManager
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);
			
			SecureContext = context;
		}
		catch (Exception err) {
			err.printStackTrace();
		}
	}
	
	public static String debugSSL(Activity activity) {
		loadPrivateSSLCertificate(activity, R.raw.der_mnps_cert);
		Document logIn = getWebPage("https://gradespeed.mnps.org/pc/Default.aspx", true, true, true);
		return logIn.toString();
	}
	
	/*public static Errors parsedDebugger(Activity activity) {
		String output = "Form Values: ";
		Document parsedLogIn = null, parsedHomepage = null, parsedGrades = null;
		
		try {
			parsedLogIn = Jsoup.parse(activity.getResources().openRawResource(R.raw.default_htm), null, "https://dodea.gradespeed.net/pc/default.aspx");
			parsedHomepage = Jsoup.parse(activity.getResources().openRawResource(R.raw.parent_main_htm), null, "https://dodea.gradespeed.net/pc/ParentMain.aspx");
			parsedGrades = Jsoup.parse(activity.getResources().openRawResource(R.raw.parent_student_grades_htm), null, "https://dodea.gradespeed.net/pc/ParentStudentGrades.aspx");
		} catch (NotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		try {
			for (Element inputElement : parsedLogIn.getElementsByTag("input")) {
				String inputName = inputElement.attr("name");
				
				if (!(inputName.equals("txtUserName") || inputName.equals("txtPassword"))) {
					logInFormValues.put(inputName, URLEncoder.encode(inputElement.attr("value"), "UTF-8"));
				}
			}
			logInFormValues.put("txtUserName", URLEncoder.encode("username", "UTF-8"));
			logInFormValues.put("txtPassword", URLEncoder.encode("password", "UTF-8"));
			logInFormValues.put("ddlLanguage", URLEncoder.encode("en", "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		output += buildUrlArgs(logInFormValues);
		
		
		Element gradeLink = parsedHomepage.getElementById("lnkGrades");
		if (gradeLink != null) {
			output += "\n" + "Relative grades path: " + gradeLink.attr("href");
		}
		
		ErrorStream = parseForShortGrades(parsedGrades);
		
		ErrorStream = findBros(parsedGrades);
		if (ErrorStream.equals(Errors.FOUND_SIBLINGS)) {
			
			try {
				getBroFormValues(parsedGrades);
				output += "\nSwitch Student Request Values: " + SettingsManager.account.getStudentFormValues();
			}
			catch (UnsupportedEncodingException e) {
				return Errors.ENCODING_ERROR;
			}
			error = output;
			return Errors.FOUND_SIBLINGS;
		}
		
		return Errors.NONE;
	}*/
	
	private static Errors parseForShortGrades(Document document) {
		Element gradesTable, row;
		Iterator<Element> rows;
		
		try {
			gradesTable = document.select("table[class=DataTable]").first().getElementsByTag("tbody").first();
			rows        = gradesTable.children().iterator();
			row = rows.next();
		}
		catch (Exception error) {
			error.printStackTrace();
			return Errors.COULD_NOT_PARSE_FOR_GRADES;
		}
		CURRENT_GRADE = CYCLE_1_GRADE;          
		
		int size_rows    = gradesTable.children().size() - 1;		// One row is the headers
		int size_columns = row.children().size();   
		
		ShortGrades = new String[size_rows][size_columns][2]; // The 2 is for metadata on the table element.
		
		for (int row_i = 0; row_i < size_rows && rows.hasNext(); ++row_i) {
			
			row = rows.next();
			Iterator<Element> columns = row.children().iterator();
			Element column;
			
			for (int column_i = 0; column_i < size_columns && columns.hasNext(); ++column_i) {
				
				column = columns.next();
				
				if (column.children().size() == 0) {
					ShortGrades[row_i][column_i][0] = StringEscapeUtils.unescapeHtml4(column.html());
				}
				else {
					ShortGrades[row_i][column_i][0] = StringEscapeUtils.unescapeHtml4(column.children().first().html());
					ShortGrades[row_i][column_i][1] = column.children().first().attr("href");
					
					switch (column_i) {
					case CYCLE_2_GRADE:
					case CYCLE_3_GRADE:
					case CYCLE_4_GRADE:
						CURRENT_GRADE = column_i;
					}
				}
				//Log.i("Grades Table: ", ShortGrades[row_i][column_i][0]);
			}
		}
		return Errors.NONE;
	}
	
	/* Reverting to older POST Method function to get SSL Support for DODEA District
	 * 
	 *private static String verifyWithServer() {
		HttpClient client  = new DefaultHttpClient();
		HttpPost   postReq = new HttpPost(SettingsManager.account.getMainURL());
		String     urlParameters = buildUrlArgs(logInFormValues);
		
		try {
			client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
			
			StringEntity body = new StringEntity(urlParameters);
			body.setContentType("application/x-www-form-urlencoded");
			body.setContentEncoding("UTF-8");
			
			postReq.setEntity(body);
			postReq.addHeader("Cookie", Cookies);
			HttpResponse response = client.execute(postReq);
			
			if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
				ErrorStream = Errors.NONE;
				SettingsManager.account.setFormValues(urlParameters);
				return postReq.getURI().resolve(response.getHeaders("Location")[0].getValue()).toString();
			}
			else if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
				ErrorStream = Errors.INVALID_USER_PASS;
			}
			else {
				ErrorStream = Errors.UNKNOWN_HTTP_ERROR;
			}
		} 
		catch (UnsupportedEncodingException error) {
			ErrorStream = Errors.ENCODING_ERROR;
		} 
		catch (ClientProtocolException error) {
			ErrorStream = Errors.UNKNOWN_HTTP_ERROR;
		}
		catch (IOException error) {
			ErrorStream = Errors.INVALID_DISTRICT_URL;
		}
		
		return "";
	} */
	
	/* Reverted to previous method to get SSL Support for DODEA
	 * 
	private static String sendSwitchBroRequest(String studentID) {
		HttpClient client  = new DefaultHttpClient();
		HttpPost   postReq = new HttpPost(SettingsManager.account.getGradesURL());
		
		try {
			client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
			
			StringEntity body = new StringEntity(SettingsManager.account.getStudentFormValues() + studentID);
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
	}*/
	
	/* IDEA for redirecting HTTP to HTTPS (currently not supported by android)
	 * 
	 * else if (followRedirects && 
			!ErrorStream.equals(Errors.DOING_HTTPS_REDIRECT) && 
			(connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP || 
			 connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM)) {	// 302 Redirect (Most Likely to HTTPS)
		
		String new_url = connection.getHeaderField("Location");
		ErrorStream = Errors.DOING_HTTPS_REDIRECT;
		
		response = getWebPage(new_url, addCookies, Parse, true);
	}*/
	
	/* private static Errors parseForShortGrades(Document gradesPage) {
	ShortGrades.clear();
	Element gradesTable = gradesPage.select("table[class=DataTable]").first();
	Iterator<Element> itGT;
	
	if (gradesTable != null) {
		itGT = gradesTable.getElementsByTag("tr").iterator();
	}
	else {
		error = gradesPage.html();
		return Errors.COULD_NOT_PARSE_FOR_GRADES;
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
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		// TODO: Find out why the index goes out of bounds.

		ShortGrades.add(tempVals);
	}
	
	return Errors.NONE;
} */
}