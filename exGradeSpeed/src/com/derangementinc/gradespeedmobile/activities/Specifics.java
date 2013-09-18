package com.derangementinc.gradespeedmobile.activities;

import com.derangementinc.gradespeedmobile.ConnectionManager;
import com.derangementinc.gradespeedmobile.LogIn;
import com.derangementinc.gradespeedmobile.R;
import com.derangementinc.gradespeedmobile.adapters.SpecificsExpandableAdapter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ExpandableListView;

public class Specifics extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_specifics);
		
		String url = getIntent().getStringExtra("url");
		String description = getIntent().getStringExtra("description");
		((TextView) this.findViewById(R.id.courseDescription)).setText(description);
		
		new getSpecifics().execute(url);
	}
	
	private class getSpecifics extends AsyncTask<String, Void, Boolean> {
		ConnectionManager cm = new ConnectionManager();
		private ProgressDialog progress = new ProgressDialog(Specifics.this, ProgressDialog.STYLE_SPINNER);
		
		@Override
		protected void onPreExecute() {
			progress.setMessage("Getting specifics...");
			progress.show();
		}

		@Override
		protected Boolean doInBackground(String... urls) {
			for (String url : urls) {
				return cm.getLongGrades(url);
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean successful) {
			progress.dismiss();
			if (successful) {
				((ExpandableListView) Specifics.this.findViewById(R.id.specificsExpandable)).setAdapter(new SpecificsExpandableAdapter(Specifics.this, cm));
			}
			else {
				Intent logBackIn = new Intent(Specifics.this.getBaseContext(), LogIn.class);
				logBackIn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				logBackIn.putExtra(LogIn.TAG, LogIn.TIMEOUT);
				startActivity(logBackIn);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			Intent about = new Intent(getBaseContext(), AboutActivity.class);
			startActivity(about);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
