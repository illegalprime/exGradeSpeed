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
		gradesLength = ConnectionManager.ShortGrades.size();
		if (!SettingsManager.isOnlyChild()) {
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
		if ((position == gradesLength - 1) && !SettingsManager.isOnlyChild()) {
			return -1;
		}
		else {
			return position;
		}
	}

	@Override
	public View getView(int index, View converter, ViewGroup parent) {
		try {

			View view = converter;
			if (converter == null)
				view = inflater.inflate(R.layout.list_row, null);

			if ((index == gradesLength - 1) && !SettingsManager.isOnlyChild()) { 
				return inflater.inflate(R.layout.list_row_switch_student, null);
			}

			String[] data = ConnectionManager.ShortGrades.get(index);

			String changeGrade = SettingsManager.isNewGrade(data[ConnectionManager.CURRENT_GRADE], index);
			if (!changeGrade.equals("")) {
				TextView flagChange = (TextView) view.findViewById(R.id.flagNEW);

				if (changeGrade.charAt(0) == '+') {
					flagChange.setTextColor(Color.GREEN);
				}

				flagChange.setText(changeGrade);
			}

			((TextView) view.findViewById(R.id.teacherTitle)).setText(data[ConnectionManager.TEACHER_NAME]);
			((TextView) view.findViewById(R.id.gradeNo)).setText(data[ConnectionManager.CURRENT_GRADE]);
			((TextView) view.findViewById(R.id.cycle1)).setText(data[ConnectionManager.CYCLE_1_GRADE]);
			((TextView) view.findViewById(R.id.cycle2)).setText(data[ConnectionManager.CYCLE_2_GRADE]);
			((TextView) view.findViewById(R.id.semester1)).setText(data[ConnectionManager.SEMESTER_1_GRADE]);
			((TextView) view.findViewById(R.id.cycle3)).setText(data[ConnectionManager.CYCLE_3_GRADE]);
			((TextView) view.findViewById(R.id.cycle4)).setText(data[ConnectionManager.CYCLE_4_GRADE]);
			((TextView) view.findViewById(R.id.semester2)).setText(data[ConnectionManager.SEMESTER_2_GRADE]);

			return view;
		} catch (Exception error) {
			return inflater.inflate(R.layout.blank, null);
		}
	}
}