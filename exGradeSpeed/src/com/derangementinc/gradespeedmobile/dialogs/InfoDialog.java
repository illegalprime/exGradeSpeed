package com.derangementinc.gradespeedmobile.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class InfoDialog extends DialogFragment {
	String info = "";
	
	public void setInfo(String info) {
		this.info = info;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstance) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(this.info)
		       .setTitle("Info")
			   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
		return builder.create();
	}
}