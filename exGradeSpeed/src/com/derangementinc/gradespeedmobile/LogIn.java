package com.derangementinc.gradespeedmobile;

import com.derangementinc.gradespeedmobile.activities.AboutActivity;
import com.derangementinc.gradespeedmobile.activities.GradeSheet;
import com.derangementinc.gradespeedmobile.activities.SettingsActivity;
import com.derangementinc.gradespeedmobile.dialogs.BrotherDialog;
import com.derangementinc.gradespeedmobile.dialogs.InfoDialog;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Toast;

public class LogIn extends FragmentActivity implements OnClickListener, OnCheckedChangeListener, OnFocusChangeListener, OnEditorActionListener, DialogInterface.OnClickListener {
	private InfoDialog          Info;
	private FragmentTransaction ft;
	
	private CheckBox rememBox;
	private TextView userView;
	private TextView passView;
	private int extraMetaformation;
	private boolean firstChange = true;
	
	public static final String TAG = "extra";  
	public static final int TIMEOUT = 1;
	public static final int SWITCH_BROS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {  //SettingsManager.loadPhonyPastGrades();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
		Info = new InfoDialog();
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
		
		SettingsManager.load(getBaseContext(), extraMetaformation != SWITCH_BROS);
		
		if (SettingsManager.isCredentialsRemembered()) {
			userView.setText(SettingsManager.getSavedUsername());
			passView.setText(SettingsManager.getSavedPassword());
			rememBox.setChecked(true);
			
			if (extraMetaformation == 0) {
				new retreiveGrades().execute(SettingsManager.getSavedUsername(), SettingsManager.getSavedPassword());
			}
		}
		else if (extraMetaformation != SWITCH_BROS) {
			SettingsManager.clearBrothers();
		}
		
		userView.setImeActionLabel("Login", KeyEvent.KEYCODE_ENTER);
		passView.setImeActionLabel("Login", KeyEvent.KEYCODE_ENTER);
		userView.setOnEditorActionListener(this);
		passView.setOnEditorActionListener(this);
		userView.setOnFocusChangeListener(this);
		userView.setOnClickListener(this);
		passView.setOnFocusChangeListener(this);
		rememBox.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.logOnBtn) {
			startLogIn();
			return;
		}
		((TextView) view).setText("");
		if (SettingsManager.isCredentialsRemembered()) {
			SettingsManager.credentialsIsNOTRemembered();
			rememBox.setChecked(false);
		}
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
			Intent settings = new Intent(getBaseContext(), SettingsActivity.class);
			startActivity(settings);
			return true;
		case R.id.action_switchstudents:
			if (SettingsManager.isOnlyChild() || !SettingsManager.isCredentialsRemembered()) {
				showInfoDialog("Login first to retreive data about your siblings.");
			}
			else {
				SettingsManager.setDefaultBrother("");
				new retreiveGrades().execute(SettingsManager.getSavedUsername(), SettingsManager.getSavedPassword());
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void startLogIn() {
		String user = userView.getText().toString();
		String pass = passView.getText().toString();
		if (SettingsManager.isRememberBoxChecked() && !SettingsManager.isCredentialsRemembered()) {
			SettingsManager.setCredentials(user, pass);
		}
		new retreiveGrades().execute(user.trim(), pass);
	}
	
	public void showInfoDialog(String info) {
		if (Info.isAdded()) {
			ft.remove(Info);
		}
		if (info != null) {
			Info.setInfo(info);
		}
		else {
			Info.setInfo("No info.");
		}
		Info.show(getSupportFragmentManager(), "InfoBox");
	}
	
	@Override
	public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
		SettingsManager.setRememberBoxChecked(isChecked);
		SettingsManager.credentialsIsNOTRemembered();
	}
	
	public void initiateGradesIntent() {
		if (SettingsManager.oldGradesEmpty() || SettingsManager.getGradesLength() != ConnectionManager.ShortGrades.size()) {
			SettingsManager.buildGradeTable();
		}
		
		Intent gradeSheet = new Intent(getBaseContext(), GradeSheet.class);
		startActivity(gradeSheet);
	}
	
	private class retreiveGrades extends AsyncTask<String, Void, Integer> {
		private ProgressDialog progress = new ProgressDialog(LogIn.this, ProgressDialog.STYLE_SPINNER);
		private String[] credentials    = new String[2];
		private int orientation = 0;
		
		@Override
		protected void onPreExecute() {
			orientation = LogIn.this.getRequestedOrientation();
			LogIn.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			
			if (progress.isShowing()) {
				progress.dismiss();
			}
			
			progress.setMessage("Signing in and retreiving grades...");
			progress.show();
		}
		
		@Override
		protected Integer doInBackground(String... creds) {
			int i = 0;
			for (String cred : creds) {
				credentials[i++] = cred; 
			}
			return ConnectionManager.logOn(credentials[0], credentials[1]);
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			progress.dismiss();
			if (Info.isAdded()) {
				ft.remove(Info);
			}
			
			LogIn.this.setRequestedOrientation(orientation);
			
			switch (result) {
			case 0:
				initiateGradesIntent();
				break;
			case 1:
				showInfoDialog("Could not connect to gradespeed.\nNo WiFi Connection?");
				break;
			case 2:
				showInfoDialog("Invalid username/password.");
				break;
			case 3:
				showInfoDialog("Could not find the webpage containing grades in your District. Please email me at themichaeleden@gmail.com with your district for support.");
				break;
			case 4:
				showInfoDialog("Could not encode your credentials into 'UTF-8' url form.");
				break;
			case 5:
				showBrotherSelectionDialog();
				break;
			}
		}
	}
	
	private class RetreiveGradesForBro extends AsyncTask<Integer, Void, Integer> {
		private ProgressDialog progress = new ProgressDialog(LogIn.this, ProgressDialog.STYLE_SPINNER);
		private int orientation = 0;
		
		@Override
		protected void onPreExecute() {
			orientation = LogIn.this.getRequestedOrientation();
			LogIn.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			
			progress.setMessage("Getting personalized grades...");
			progress.show();
		}

		@Override
		protected Integer doInBackground(Integer... args) {
			for (Integer brindex : args) {
				SettingsManager.setDefaultBrother(SettingsManager.findDatBro(brindex));
				if (extraMetaformation == SWITCH_BROS) {
					SettingsManager.writeDefaultBrother();
				}
				else {
					SettingsManager.writeBroData();
				}
				
				if (!SettingsManager.getDefaultBrother().equals("")) {
					return ConnectionManager.getGradesWithBro();
				}
				else {
					return 2;
				}
			}
			return 1;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			progress.dismiss();
			
			LogIn.this.setRequestedOrientation(orientation);
			
			switch (result) {
			case 0:
				initiateGradesIntent();
				break;
			case 1:
				if (ConnectionManager.error.equals("")) {
					showInfoDialog("Could not connect to gradespeed.\nNo WiFi Connection?");
				}
				else {
					showInfoDialog("Congratulations! You have found an error that the developer can't recreate and has been plaguing users for a very long time, send this to my email themichaeleden@gmail.com, or show it to me: '" + ConnectionManager.error + "'");
				}
				break;
			case 2:
				Toast.makeText(LogIn.this, "24601!", Toast.LENGTH_LONG).show();
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
			if (firstChange) {
				firstChange = false;
				return;
			}
			if ((view.getId() == R.id.passFeild) && !((TextView) view).getText().equals("Password")) { //passNotClicked
				((TextView) view).setText("");
				//passNotClicked = false;
			}
			else if ((view.getId() == R.id.userFeild) && !((TextView) view).getText().equals("Username")) { //userNotClicked
				((TextView) view).setText("");
				//userNotClicked = false;
			}
			SettingsManager.credentialsIsNOTRemembered();
			rememBox.setChecked(false);
		}
	}
	
	//------------------------------//
	//		DEBUGGING METHOD		//
	//------------------------------//
	/*
	
	private class DebugIceCream extends AsyncTask<String, Void, String> {
		private ProgressDialog progress = new ProgressDialog(LogIn.this, ProgressDialog.STYLE_SPINNER);
		private String[] credentials    = new String[2];
		
		@Override
		protected void onPreExecute() {
			progress.setMessage("Signing in and retreiving grades...");
			progress.show();
		}
		
		@Override
		protected String doInBackground(String... creds) {
			int i = 0;
			for (String cred : creds) {
				credentials[i++] = cred; 
			}
			return ConnectionManager.debug(credentials[0], credentials[1]);
		}
		
		@Override
		protected void onPostExecute(String result) {
			progress.dismiss();
			
			showInfoDialog(result);
		}
	}
	*/
}