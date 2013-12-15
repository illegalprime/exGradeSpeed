package com.derangementinc.gradespeedmobile.activities;

import com.derangementinc.gradespeedmobile.R;
import com.derangementinc.gradespeedmobile.adapters.SpecificsExpandableAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ExpandableListView;

public class Specifics extends Activity {
	
	protected ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_specifics);
		
		((TextView) this.findViewById(R.id.courseDescription)).setText(getIntent().getStringExtra("description"));
		
		((ExpandableListView) Specifics.this.findViewById(R.id.specificsExpandable)).setAdapter(new SpecificsExpandableAdapter(Specifics.this));
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
	
	@Override
	public void onPause() {
	    super.onPause();

	    if (progress != null)
	    	progress.dismiss();
	    progress = null;
	}
}
