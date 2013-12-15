package com.derangementinc.gradespeedmobile.activities;

import java.util.ArrayList;

import com.derangementinc.gradespeedmobile.R;
import com.derangementinc.gradespeedmobile.SettingsManager;
import com.derangementinc.gradespeedmobile.dialogs.EditDistrictDialog;
import com.derangementinc.gradespeedmobile.dialogs.EditDistrictDialog.EditDistrcitDialogListener;
import com.derangementinc.gradespeedmobile.dialogs.QuestionDialog;
import com.derangementinc.gradespeedmobile.dialogs.QuestionDialog.QuestionDialogListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DistrictPicker extends FragmentActivity implements OnItemClickListener, OnItemLongClickListener, QuestionDialogListener, EditDistrcitDialogListener {
	
	private ListView districtList;
	private ArrayAdapter<String> adapter; 
	private ArrayList<String> list;
	
	private static final String TAG_DESTROY_DATA = "destroyData";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_district_picker);
		
		districtList = (ListView) this.findViewById(R.id.districtPickerList);
		list = SettingsManager.districts.format();
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		
		districtList.setAdapter(adapter);
		districtList.setOnItemClickListener(this);
		districtList.setOnItemLongClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int index = position;
		
		if (index < SettingsManager.districts.defaultSize()) {
			// Ask to remove data to fit a new URL from Default Districts
			askToDeleteData(SettingsManager.districts.getName(index), position, false);
		}
		else if ((index -= SettingsManager.districts.defaultSize()) < SettingsManager.districts.customSize()) {
			// This is a custom district!
			askToDeleteData(SettingsManager.districts.getCustomName(index), position, true);
		}
		else {
			// Ask for a custom URL
			createCustomDistrict();
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		int index = position;
		
		if (index < SettingsManager.districts.defaultSize()) {
			createCustomDistrictFromExisting(
					"Custom " + SettingsManager.districts.getName(index), 
					SettingsManager.districts.getMainURL(index),
					SettingsManager.districts.getGradesURL(index));
			return true;
		}
		else if ((index -= SettingsManager.districts.defaultSize()) < SettingsManager.districts.customSize()) {
			createCustomDistrictFromExisting(
					SettingsManager.districts.getCustomName(index), 
					SettingsManager.districts.getCustomMainURL(index),
					SettingsManager.districts.getCustomGradesURL(index));
			return true;
		}
		return false;
	}

	@Override
	public void onQuestionDialogAnswer(QuestionDialog dialog, boolean answerPositive) {
		
		if (answerPositive && dialog.getTag().equals(TAG_DESTROY_DATA)) {
			int index = dialog.getIndex();
			
			// Wipe all data for new account
			SettingsManager.account.wipeStoredData();
			SettingsManager.newAccount();
			
			// Set new URLs
			SettingsManager.districts.setCurrentDistrict(index);
			
			this.finish();
		}
	}
	
	@Override
	public void onEditDistrictDialogSubmit(DialogFragment dialog, String name, String mainURL, String gradesURL) {
		if (!name.equals("")) {
			SettingsManager.districts.addNewDistrict(name, mainURL, gradesURL);
			//Log.i("exGradeSpeed onSubmit: ", name + "\n" + mainURL + "\n" + gradesURL);
			update();
		}
	}
	
	@Override
	public void onEditDistrictDialogRemove(DialogFragment dialog, String name) {
		SettingsManager.districts.removeDistrict(name);
		update();
	}

	public void askToDeleteData(String district, int index, boolean isCustom) {
		QuestionDialog deleteData = new QuestionDialog();
		deleteData.setQuestion("Switching Districs will remove all your saved data. Continue?", "Change to " + district, "Cancel");
		deleteData.setDistrictMetadata(index, isCustom);
		deleteData.show(getSupportFragmentManager(), TAG_DESTROY_DATA);
	}
	
	public void createCustomDistrict() {
		EditDistrictDialog editor = new EditDistrictDialog();
		editor.show(getSupportFragmentManager(), "dEditor");
	}
	
	public void createCustomDistrictFromExisting(String name, String mainURL, String gradesURL) {
		EditDistrictDialog editor = new EditDistrictDialog();
		editor.setSetStartingValues(name, mainURL, gradesURL);
		editor.show(getSupportFragmentManager(), "dEditor");
	}
	
	private void update() {
		list = SettingsManager.districts.format();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		districtList.setAdapter(adapter);
	}
}