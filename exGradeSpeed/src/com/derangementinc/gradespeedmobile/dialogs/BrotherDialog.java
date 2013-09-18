package com.derangementinc.gradespeedmobile.dialogs;

import com.derangementinc.gradespeedmobile.SettingsManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class BrotherDialog extends DialogFragment {
	
	DialogInterface.OnClickListener mListener;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstance) {
		AlertDialog.Builder bob = new AlertDialog.Builder(getActivity());
		bob.setTitle("Who am I?");
		bob.setItems(SettingsManager.formatBrotherNames(), mListener);
		return bob.create();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mListener = (DialogInterface.OnClickListener) activity;
		} catch (ClassCastException err) {
			mListener = null;
		}
	}
}
