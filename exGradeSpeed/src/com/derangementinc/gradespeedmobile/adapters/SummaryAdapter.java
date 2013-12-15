package com.derangementinc.gradespeedmobile.adapters;

import com.derangementinc.gradespeedmobile.ConnectionManager;
import com.derangementinc.gradespeedmobile.R;
import com.derangementinc.gradespeedmobile.SettingsManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SummaryAdapter extends BaseAdapter {
	
	private static LayoutInflater inflater = null;
	private Activity activity;
	private int gradesLength;
	
	public SummaryAdapter(Activity a) {
		activity = a;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		gradesLength = ConnectionManager.ShortGrades.length;
		
		if (!SettingsManager.account.hasOneChild()) {
			gradesLength++;
		}
	}

	@Override
	public int getCount() {
		return gradesLength;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		if ((position == gradesLength - 1) && !SettingsManager.account.hasOneChild()) {
			return -1;
		}
		else {
			return position;
		}
	}

	@Override
	public View getView(int index, View converter, ViewGroup parent) {
		View view = inflater.inflate(R.layout.list_row, null);
		
		try {
			if ((index == gradesLength - 1) && !SettingsManager.account.hasOneChild()) { 
				return inflater.inflate(R.layout.list_row_switch_student, null);
			}

			String[][] data = ConnectionManager.ShortGrades[index];
			
			String changeGrade = SettingsManager.account.isNewGradeForCurrent(data[ConnectionManager.CURRENT_GRADE][ConnectionManager.TEXT], index);
			if (!changeGrade.equals("")) {
				TextView flagChange = (TextView) view.findViewById(R.id.flagNEW);

				if (changeGrade.charAt(0) == '+') {
					flagChange.setTextColor(Color.GREEN);
				}

				flagChange.setText(changeGrade);
			}
			
			
			((TextView) view.findViewById(R.id.teacherTitle)).setText(data[ConnectionManager.TEACHER_NAME][ConnectionManager.TEXT]);
			
			
			boolean noSem1 = data[ConnectionManager.CYCLE_1_GRADE][ConnectionManager.TEXT].charAt(0) == 160;
			boolean noSem2 = data[ConnectionManager.CYCLE_3_GRADE][ConnectionManager.TEXT].charAt(0) == 160; 
			
			if (noSem1) {
				view.findViewById(R.id.row_semester_1).setVisibility(View.GONE);
			}
			else {
				((TextView) view.findViewById(R.id.cycle1)).setText(data[ConnectionManager.CYCLE_1_GRADE][ConnectionManager.TEXT]);
				((TextView) view.findViewById(R.id.cycle2)).setText(data[ConnectionManager.CYCLE_2_GRADE][ConnectionManager.TEXT]);
				((TextView) view.findViewById(R.id.exam1)).setText(data[ConnectionManager.EXAM_MIDTERM][ConnectionManager.TEXT]);
				((TextView) view.findViewById(R.id.semester1)).setText(data[ConnectionManager.SEMESTER_1_GRADE][ConnectionManager.TEXT]);
			}
			if (noSem2) {
				view.findViewById(R.id.row_semester_2).setVisibility(View.GONE);
			}
			else {
				((TextView) view.findViewById(R.id.cycle3)).setText(data[ConnectionManager.CYCLE_3_GRADE][ConnectionManager.TEXT]);
				((TextView) view.findViewById(R.id.cycle4)).setText(data[ConnectionManager.CYCLE_4_GRADE][ConnectionManager.TEXT]);
				((TextView) view.findViewById(R.id.exam2)).setText(data[ConnectionManager.EXAM_FINAL][ConnectionManager.TEXT]);
				((TextView) view.findViewById(R.id.semester2)).setText(data[ConnectionManager.SEMESTER_2_GRADE][ConnectionManager.TEXT]);
			}
			if (noSem1 && noSem2) {
				view.findViewById(R.id.gradeNo).setVisibility(View.GONE);
			}
			else {
				((TextView) view.findViewById(R.id.gradeNo)).setText(data[ConnectionManager.CURRENT_GRADE][ConnectionManager.TEXT]);
			}
		} 
		catch (Exception error) {
			error.printStackTrace();
		}
		
		return view;
	}
}