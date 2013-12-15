package com.derangementinc.gradespeedmobile.dialogs;

import com.derangementinc.gradespeedmobile.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class EditDistrictDialog extends DialogFragment {
	private String name = "New Custom";
	private String main_url = "";
	private String grades_url = "";
	
	public View parent;  
	
	public interface EditDistrcitDialogListener {
		public void onEditDistrictDialogSubmit(DialogFragment dialog, String name, String mainURL, String gradesURL);
		public void onEditDistrictDialogRemove(DialogFragment dialog, String name);
	}
	
	EditDistrcitDialogListener mListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mListener = (EditDistrcitDialogListener) activity;
		} 
		catch (ClassCastException err) {
			throw new ClassCastException(activity.toString() 
					+ " has to implement QuestionDialogListener interface");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		parent = inflater.inflate(R.layout.dialog_edit_districts, null);
		
		((TextView) EditDistrictDialog.this.parent.findViewById(R.id.district_name)).setText(name);
		((TextView) EditDistrictDialog.this.parent.findViewById(R.id.district_main)).setText(main_url);
		((TextView) EditDistrictDialog.this.parent.findViewById(R.id.district_grades)).setText(grades_url);
		
		builder.setTitle("Create a Custom District:")
			   .setView(parent)
			   .setPositiveButton("Save", new DialogInterface.OnClickListener() {
				  
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   String name      = ((TextView) EditDistrictDialog.this.parent.findViewById(R.id.district_name)).getText().toString();
					   String mainURL   = ((TextView) EditDistrictDialog.this.parent.findViewById(R.id.district_main)).getText().toString();
					   String gradesURL = ((TextView) EditDistrictDialog.this.parent.findViewById(R.id.district_grades)).getText().toString();
					   
					   mListener.onEditDistrictDialogSubmit(EditDistrictDialog.this, name, mainURL, gradesURL);
				   }
			   })
			   .setNegativeButton("Delete", new DialogInterface.OnClickListener() {

				   	@Override
					public void onClick(DialogInterface dialog, int which) {	
						mListener.onEditDistrictDialogRemove(EditDistrictDialog.this, 
								((TextView) EditDistrictDialog.this.parent.findViewById(R.id.district_name)).getText().toString());
					}
			   })
			   .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
				   
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   EditDistrictDialog.this.getDialog().cancel();
				   }
			   });
		return builder.create();
	}
	
	public void setSetStartingValues(String name, String mainURL, String gradesURL) {
		this.name = name;
		this.main_url = mainURL;
		this.grades_url = gradesURL;
	}
}