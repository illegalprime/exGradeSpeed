package com.derangementinc.gradespeedmobile;

import com.derangementinc.gradespeedmobile.activities.AboutActivity;
import com.derangementinc.gradespeedmobile.activities.DistrictPicker;
import com.derangementinc.gradespeedmobile.activities.GradeSheet;
import com.derangementinc.gradespeedmobile.dialogs.BrotherDialog;
import com.derangementinc.gradespeedmobile.dialogs.InfoDialog;
import com.derangementinc.gradespeedmobile.dialogs.QuestionDialog;
import com.derangementinc.gradespeedmobile.dialogs.QuestionDialog.QuestionDialogListener;
import com.derangementinc.gradespeedmobile.enums.Errors;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.TextView.OnEditorActionListener;

public class LogIn extends FragmentActivity implements OnClickListener, OnCheckedChangeListener, OnFocusChangeListener, OnEditorActionListener, DialogInterface.OnClickListener, QuestionDialogListener {
	protected ProgressDialog progress;
	
	// TODO: Stop making dialogs compatible with v4, switch to API 8 for reliability.
	
	private CheckBox rememBox;
	private TextView userView;
	private TextView passView;
	private int extraMetaformation;
	private boolean firstChange = true;
	
	public static final String TAG = "extra";
	public static final String TAG_DESTROY_DATA_DIALOG = "destroyData";
	public static final String TAG_CAPTIVE_PORTAL = "captivePortal";
	public static final String TAG_GO_TO_SETTINGS = "gotoSettings";
	public static final String TAG_BUNDLE_NEW_ACTIVITY = "isNewActivity";
	public static final int TIMEOUT = 1;
	public static final int SWITCH_BROS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
		extraMetaformation = getIntent().getIntExtra(TAG, 0);
		
		((Button) this.findViewById(R.id.logOnBtn)).setOnClickListener(this);
		
		rememBox = (CheckBox) this.findViewById(R.id.rememberCheck); 
		userView = (TextView) this.findViewById(R.id.userFeild);
		passView = (TextView) this.findViewById(R.id.passFeild);
		
		switch (extraMetaformation) {
		case 0:
			break;
		case TIMEOUT:
			showInfoDialog("You have been logged out due to inactivity longer than 30 mins. Sorry!");
			break;
		case SWITCH_BROS:
			showBrotherSelectionDialog();
			break;
		}
		
		SettingsManager.setContext(getBaseContext());
		
		boolean isNewActivity = savedInstanceState == null || savedInstanceState.getBoolean(TAG_BUNDLE_NEW_ACTIVITY, true);
		
		if (!SettingsManager.account.guestSession) {
			userView.setText(SettingsManager.account.getUsername());
			passView.setText(SettingsManager.account.getPassword());
			rememBox.setChecked(true);
			
			if (extraMetaformation == 0 && isNewActivity) {
				startLogIn();
			}
		}
		
		userView.setImeActionLabel("Login", KeyEvent.KEYCODE_ENTER);
		passView.setImeActionLabel("Login", KeyEvent.KEYCODE_ENTER);
		userView.setOnEditorActionListener(this);
		passView.setOnEditorActionListener(this);
		userView.setOnFocusChangeListener(this);
		userView.setOnClickListener(this);
		passView.setOnFocusChangeListener(this);
		rememBox.setOnCheckedChangeListener(this);		//ConnectionManager.parsedDebugger(this);  initiateGradesIntent();
	}
	
	@Override
	public void onPause() {
	    super.onPause();

	    if (progress != null)
	    	progress.dismiss();
	    progress = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(TAG_BUNDLE_NEW_ACTIVITY, false);
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.logOnBtn) {
			startLogIn();
			return;
		}
		((TextView) view).setText("");
		
		rememBox.setChecked(false);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		new RetreiveGradesForBro().execute(which);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			Intent about = new Intent(getBaseContext(), AboutActivity.class);
			startActivity(about);
			return true;
		case R.id.action_settings:
			Intent settings = new Intent(getBaseContext(), DistrictPicker.class);
			startActivity(settings);
			return true;
		case R.id.action_switchstudents:
			if (SettingsManager.account.hasOneChild() || !SettingsManager.account.hasCredentials()) {
				showInfoDialog("Login first to retreive data about other students on the account.");
			}
			else {
				SettingsManager.account.requestNewStudent();
				startLogIn();
				
				// TODO: Make this better by showing the selection dialogue from already saved student data.
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void startLogIn() {
		String user = userView.getText().toString().trim();
		String pass = passView.getText().toString();
		SettingsManager.account.guestSession = !rememBox.isChecked(); 
		SettingsManager.account.setCredentials(user, pass);
		new retreiveGrades().execute(user, pass);
		// TODO: Make a button or dialogue to delete someone's data.
	}
	
	public void showInfoDialog(String info) {
		InfoDialog Info = new InfoDialog();
		Info.setInfo(info);
		Info.show(getSupportFragmentManager(), "InfoBox");
	}
	
	@Override
	public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
		if (!isChecked && SettingsManager.account.hasCredentials()) {
			askToDestroyData();
		}
		// TODO: Here is probably the best place to ask whether to wipe user data, override it, or launch a guest session.
		
	}
	
	public void initiateGradesIntent() {
		SettingsManager.account.buildGradesTableForCurrent(ConnectionManager.ShortGrades, ConnectionManager.CURRENT_GRADE);
		
		//SettingsManager.account.loadPhonyGradesForCurrent();
		Intent gradeSheet = new Intent(getBaseContext(), GradeSheet.class);
		startActivity(gradeSheet);
	}
	
	private class retreiveGrades extends AsyncTask<String, Void, Errors> {
		private int orientation = 0;
		
		@Override
		protected void onPreExecute() {
			LogIn.this.progress = new ProgressDialog(LogIn.this, ProgressDialog.STYLE_SPINNER); 
			
			orientation = LogIn.this.getRequestedOrientation();
			LogIn.this.setRequestedOrientation(orientation);
			
			progress.setMessage("Signing in and retreiving grades...");
			progress.show();
		}
		
		@Override
		protected Errors doInBackground(String... creds) {
			return ConnectionManager.logIn(creds[0], creds[1], LogIn.this);
		}
		
		@Override
		protected void onPostExecute(Errors result) {
			try {
				progress.dismiss();
				progress = null;
			}
			catch (Exception err) {}
			
			LogIn.this.setRequestedOrientation(orientation);
			
			switch (result) {
			case NONE:
				SettingsManager.account.createOnlyChild();
				initiateGradesIntent();
				break;
				
			case NO_WIFI:
				showInfoDialog("Could not connect to gradespeed." + "\n" + 
							   "No WiFi Connection?");
				break;
				
			case INVALID_USER_PASS:
				askToGoToSettings("Invalid username/password." + "\n" +
							   "The URL for your district could also be wrong.");	
				break;
				
			case ENCODING_ERROR:
				showInfoDialog("Could not encode your credentials into 'UTF-8' url form.");
				break;
				
			case COULD_NOT_FIND_GRADES_PAGE:
				showInfoDialog("Could not find grades page");
				// TODO: Add a dialog to accept a new grades URL.
				break;
				
			case FOUND_SIBLINGS:
				showBrotherSelectionDialog();
				break;
				
			case HTTP_CAPTIVE_PORTAL:
				LogIn.this.askToOpenBrowser();
				break;
				
			case INVALID_DISTRICT_URL:
				showInfoDialog("Could not connect to the URL given for your district:" + "\n" + 
							   SettingsManager.districts.getCurrentLogInURL());
				break;
			
			case COULD_NOT_PARSE_FOR_GRADES:
			case COULD_NOT_PARSE_PAGE:
				showInfoDialog("A parsing error ocurred, could not extract grades from webpage.");
				break;
				
			case COULD_NOT_REACH_PAGE:
				askToGoToSettings("Could not reach gradespeed.net" + "\n" +
							      "Maybe it is blocked, or the District URL entered was wrong.");
				break;
				
			case UNKNOWN_HTTP_ERROR:
				showInfoDialog("An unknown error ocurred with the internet, sorry!" + "\n" + 
							   ConnectionManager.error);
				break;
				
			default:
				showInfoDialog("An error has ocurred, sorry!");
				break;
			}
		}
	}
	
	private class RetreiveGradesForBro extends AsyncTask<Integer, Void, Errors> {
		private int orientation = 0;
		
		@Override
		protected void onPreExecute() {
			LogIn.this.progress = new ProgressDialog(LogIn.this, ProgressDialog.STYLE_SPINNER);
			
			orientation = LogIn.this.getRequestedOrientation();
			LogIn.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			
			progress.setMessage("Getting personalized grades...");
			progress.show();
		}

		@Override
		protected Errors doInBackground(Integer... brindex) {
			SettingsManager.account.setCurrentStudent(brindex[0]);
						
			return ConnectionManager.getGradesWithBro(LogIn.this);
		}
		
		@Override
		protected void onPostExecute(Errors result) {
			try {
				progress.dismiss();
				progress = null;
			}
			catch (Exception err) {} 
			
			LogIn.this.setRequestedOrientation(orientation);
			
			switch (result) {
			case NONE:
				initiateGradesIntent();
				break;
			case NO_WIFI:
				if (ConnectionManager.error.equals("")) {
					showInfoDialog("Could not connect to gradespeed.\nNo WiFi Connection?");
				}
				else {
					showInfoDialog("Congratulations! You have found an error that the developer can't recreate and has been plaguing users for a very long time, send this to my email themichaeleden@gmail.com, or show it to me: '" + ConnectionManager.error + "'");
				}
				break;
				
			case COULD_NOT_FIND_GRADES_PAGE:
				showInfoDialog("1");
				break;
			case COULD_NOT_PARSE_FOR_GRADES:
				showInfoDialog("2");
				break;
			case COULD_NOT_PARSE_FOR_OTHER_STUDENTS:
				showInfoDialog("3");
				break;
			case COULD_NOT_PARSE_FOR_SPECIFIC_GRADES:
				showInfoDialog("4");
				break;
			case COULD_NOT_PARSE_PAGE:
				showInfoDialog("5");
				break;
			case DOING_HTTPS_REDIRECT:
				showInfoDialog("6");
				break;
			case ENCODING_ERROR:
				showInfoDialog("7");
				break;
			case FOUND_SIBLINGS:
				showInfoDialog("8");
				break;
			case HTTP_CAPTIVE_PORTAL:
				showInfoDialog("9");
				break;
			case INVALID_DISTRICT_URL:
				showInfoDialog("10");
				break;
			case INVALID_USER_PASS:
				showInfoDialog("11");
				break;
			case NO_CURRENT_SIBLING:
				showInfoDialog("12");
				break;
			case UNKNOWN_HTTP_ERROR:
				showInfoDialog("13");
				break;
			default:
				showInfoDialog("Something went wrong");
				break;
			}
		}
	}
	
	@Override
	public boolean onEditorAction(TextView textbox, int event, KeyEvent key) {
		if (textbox.getId() == R.id.passFeild) {
			startLogIn();
			return true;
		}
		else {
			passView.setText("");
			return false;
		}
	}
	
	public void showBrotherSelectionDialog() {
		DialogFragment dialog = new BrotherDialog();
		dialog.show(getSupportFragmentManager(), "brotherSelection");
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		if (hasFocus) {
			
			if ((view.getId() == R.id.passFeild) && !((TextView) view).getText().equals("Password")) {
				if (firstChange) {
					firstChange = false;
					return;
				}
				
				((TextView) view).setText("");
			}
			else if ((view.getId() == R.id.userFeild) && !((TextView) view).getText().equals("Username")) {
				if (firstChange) {
					firstChange = false;
					return;
				}
			
				((TextView) view).setText("");
			}
			rememBox.setChecked(false);
		}
	}

	@Override
	public void onQuestionDialogAnswer(QuestionDialog dialog, boolean answerPositive) {
		if (dialog.getTag().equals(TAG_DESTROY_DATA_DIALOG)) {
			if (answerPositive) {
				SettingsManager.account.wipeStoredData();
				SettingsManager.newAccount();
			}
			else {
				SettingsManager.account.guestSession = true;
			}
		}
		else if (dialog.getTag().equals(TAG_CAPTIVE_PORTAL) 
				&& answerPositive) {
			
			sendToCaptivePortal();
		}
		else if (dialog.getTag().equals(TAG_GO_TO_SETTINGS)
				&& answerPositive) {
			
			Intent settings = new Intent(getBaseContext(), DistrictPicker.class);
			startActivity(settings);
		}
	}
	
	public void askToDestroyData() {
		QuestionDialog destroyDataP = new QuestionDialog();
		destroyDataP.setQuestion("Wipe all user data or start a temporary guest session?", "Clear user data", "Use App as Guest");
		destroyDataP.show(getSupportFragmentManager(), TAG_DESTROY_DATA_DIALOG);
	}
	
	public void sendToCaptivePortal() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(android.net.Uri.parse("http://www.gstatic.com/generate_204"));
		startActivity(intent);
	}
	
	public void askToOpenBrowser() {
		QuestionDialog destroyDataP = new QuestionDialog();
		destroyDataP.setQuestion("Detected a captive portal." + "\n" + 
				   				 "Do you need to login to your wifi hotspot?",
				   				 "Open Browser",		 "Cancel");
		destroyDataP.show(getSupportFragmentManager(), TAG_CAPTIVE_PORTAL);
	}
	
	public void askToGoToSettings(String question) {
		QuestionDialog settingsP = new QuestionDialog();
		settingsP.setQuestion(question, "Go to Settings", "OK");
		settingsP.show(getSupportFragmentManager(), TAG_GO_TO_SETTINGS);
	}
}