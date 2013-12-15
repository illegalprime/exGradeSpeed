package com.derangementinc.gradespeedmobile.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class QuestionDialog extends DialogFragment {
	String positive = "";
	String negative = "";
	String question = "";
	
	private int     index;
	private boolean isCustom;
	
	public interface QuestionDialogListener {
		public void onQuestionDialogAnswer(QuestionDialog dialog, boolean answerPositive);
	}
	
	QuestionDialogListener mListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mListener = (QuestionDialogListener) activity;
		} 
		catch (ClassCastException err) {
			throw new ClassCastException(activity.toString() 
					+ " has to implement QuestionDialogListener interface");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(question)
			   .setPositiveButton(positive, new DialogInterface.OnClickListener() {
				  
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   mListener.onQuestionDialogAnswer(QuestionDialog.this, true);
				   }
			   })
			   .setNegativeButton(negative, new DialogInterface.OnClickListener() {
				   
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   mListener.onQuestionDialogAnswer(QuestionDialog.this, false);
				   }
			   });
		return builder.create();
	}
	
	public void setQuestion(String question, String posAnswer, String negAnswer) {
		this.question = question;
		this.positive = posAnswer;
		this.negative = negAnswer;
	}
	
	public void setDistrictMetadata(int index, boolean isCustom) {
		this.index = index;
		this.isCustom = isCustom;
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isCustomDistrict() {
		return isCustom;
	}
}