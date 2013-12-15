package com.derangementinc.gradespeedmobile.activities;

import com.derangementinc.gradespeedmobile.ConnectionManager;
import com.derangementinc.gradespeedmobile.LogIn;
import com.derangementinc.gradespeedmobile.R;
import com.derangementinc.gradespeedmobile.SettingsManager;
import com.derangementinc.gradespeedmobile.adapters.SummaryAdapter;
import com.derangementinc.gradespeedmobile.enums.Errors;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

public class GradeSheet extends Activity implements OnItemClickListener {
	private ListView gradeList = null;
	protected ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grade_sheet);
		
		gradeList = (ListView) this.findViewById(R.id.GradesList);
		SummaryAdapter adapter = new SummaryAdapter(this);
		
		gradeList.setAdapter(adapter);
		gradeList.setOnItemClickListener(this);
		this.registerForContextMenu(gradeList);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		gradeList.setAdapter(new SummaryAdapter(this));
	}
	
	@Override
	public void onPause() {
	    super.onPause();

	    if (progress != null)
	    	progress.dismiss();
	    progress = null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.no_settings, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			Intent about = new Intent(getBaseContext(), AboutActivity.class);
			startActivity(about);
			return true;
		case R.id.action_switchstudents:
			changeStudents();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (id == -1) {
			changeStudents();
		}
		else {
			startSpecificsActivity(position);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuinfo) {
		super.onCreateContextMenu(menu, view, menuinfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.cycle_menu, menu);
		
		// TODO: Make it obvious that users can do this.
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info  = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int position = info.position;
		int order    = item.getOrder();
		
		if (position >= ConnectionManager.ShortGrades.length) {
			return true;
		}
		if (order < 10) {
			startSpecificsActivity(position, order);
		}
		else if (order == 100) {
			// View Note
		}
		else if (order == 200) {
			// Send email
			startEmailIntent(ConnectionManager.ShortGrades[position][ConnectionManager.TEACHER_NAME][ConnectionManager.URL].substring(7));
		}
		return true;
	}
	
	private void startSpecificsActivity(int course, int cycleUrlIndex) {
		
		String[][] grades  = ConnectionManager.ShortGrades[course];
		String description = grades[ConnectionManager.COURSE_NAME][ConnectionManager.TEXT] + " (Period " + grades[ConnectionManager.COURSE_PERIOD][ConnectionManager.TEXT].substring(0, 1) + "): " + grades[ConnectionManager.TEACHER_NAME][ConnectionManager.TEXT];
		String url         = grades[ConnectionManager.CURRENT_GRADE][ConnectionManager.TEXT];
		
		if (cycleUrlIndex == ConnectionManager.CURRENT_GRADE) {
			SettingsManager.account.updateGradeForCurrent(url, course);
		}
		
		getSpecifics specificsLoader = new getSpecifics();
		specificsLoader.setDescription(description);
		specificsLoader.execute(grades[cycleUrlIndex][ConnectionManager.URL]);
	}
	
	public void startSpecificsActivity(int course) {
		startSpecificsActivity(course, ConnectionManager.CURRENT_GRADE);
	}
	
	public void startEmailIntent(String address) {
		// Create the Email intent
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		// Define the email address
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{address});

		// Send it off to the Activity-Chooser 
		startActivity(Intent.createChooser(emailIntent, "Email Teacher with..."));
	}
	
	private void changeStudents() {
		if (SettingsManager.account.hasOneChild()) {
			Toast.makeText(getApplicationContext(), "Could not find data about your siblings.", Toast.LENGTH_LONG).show();
		}
		else {
			Intent logBackIn = new Intent(getBaseContext(), LogIn.class);
			logBackIn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			logBackIn.putExtra(LogIn.TAG, LogIn.SWITCH_BROS);
			startActivity(logBackIn);
		}
	}
	
	private class getSpecifics extends AsyncTask<String, Void, Errors> {
		private String description = "";
		
		@Override
		protected void onPreExecute() {
			GradeSheet.this.progress = new ProgressDialog(GradeSheet.this, ProgressDialog.STYLE_SPINNER);
			progress.setMessage("Getting specifics...");
			progress.show();
		}

		@Override
		protected Errors doInBackground(String... urls) {
			return ConnectionManager.getLongGrades(urls[0]);
		}
		
		@Override
		protected void onPostExecute(Errors successful) {
			try {
				progress.dismiss();
				progress = null;
			}
			catch (Exception err) {}
			
			if (successful.equals(Errors.NONE)) {
				Intent intent = new Intent(getBaseContext(), Specifics.class);
				intent.putExtra("description",  description);		
				startActivity(intent);
			}
			else {
				Intent logBackIn = new Intent(GradeSheet.this.getBaseContext(), LogIn.class);
				logBackIn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				logBackIn.putExtra(LogIn.TAG, LogIn.TIMEOUT);
				startActivity(logBackIn);
			}
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
	}
 }