package com.derangementinc.gradespeedmobile.activities;

import java.net.MalformedURLException;
import java.net.URL;

import com.derangementinc.gradespeedmobile.R;
import com.derangementinc.gradespeedmobile.SettingsManager;
import com.derangementinc.gradespeedmobile.dialogs.InfoDialog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SettingsActivity extends FragmentActivity implements OnClickListener {
	
	private InfoDialog Info;
	private FragmentTransaction ft;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		((Button) this.findViewById(R.id.settingsSaveBtn)).setOnClickListener(this);
		((TextView) this.findViewById(R.id.urlInput)).setText(SettingsManager.getLogInURL());
		
		Info = new InfoDialog();
	}

	@Override
	public void onClick(View button) {
		String currURL = ((TextView) this.findViewById(R.id.urlInput)).getText().toString();
		try {
			new URL(currURL);
			SettingsManager.saveURL(currURL);
			this.finish();
		} catch (MalformedURLException e) {
			showInfoDialog("You haven't entered the URL correctley.");
		}
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
}