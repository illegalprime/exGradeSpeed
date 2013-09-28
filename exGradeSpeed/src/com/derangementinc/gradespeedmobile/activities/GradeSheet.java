package com.derangementinc.gradespeedmobile.activities;

import com.derangementinc.gradespeedmobile.ConnectionManager;
import com.derangementinc.gradespeedmobile.LogIn;
import com.derangementinc.gradespeedmobile.R;
import com.derangementinc.gradespeedmobile.SettingsManager;
import com.derangementinc.gradespeedmobile.adapters.SummaryAdapter;

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
import android.content.Intent;

public class GradeSheet extends Activity implements OnItemClickListener {
	private ListView gradeList = null;

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
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info  = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		startSpecificsActivity(info.position, item.getOrder());
		return true;
	}
	
	private void startSpecificsActivity(int course, int cycleURLindex) {
		Intent intent = new Intent(getBaseContext(), Specifics.class);
		String[] grades = ConnectionManager.ShortGrades.get(course);
		
		SettingsManager.updateGrade(grades[ConnectionManager.CURRENT_GRADE], course);
		
		intent.putExtra("url", grades[cycleURLindex]);
		intent.putExtra("description", grades[ConnectionManager.COURSE_NAME] + " (Period " + grades[ConnectionManager.COURSE_PERIOD].substring(0, 1) + "): " + grades[ConnectionManager.TEACHER_NAME] );		
		startActivity(intent);
	}
	
	private void startSpecificsActivity(int course) {
		startSpecificsActivity(course, ConnectionManager.CURRENT_GRADE + 1);
	}
	
	private void changeStudents() {
		if (SettingsManager.isOnlyChild()) {
			Toast.makeText(getApplicationContext(), "Could not find data about your siblings.", Toast.LENGTH_LONG).show();
		}
		else {
			Intent logBackIn = new Intent(getBaseContext(), LogIn.class);
			logBackIn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			logBackIn.putExtra(LogIn.TAG, LogIn.SWITCH_BROS);
			startActivity(logBackIn);
		}
	}
 }