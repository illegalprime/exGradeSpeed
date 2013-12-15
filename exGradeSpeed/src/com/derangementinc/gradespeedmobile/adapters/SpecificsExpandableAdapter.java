package com.derangementinc.gradespeedmobile.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringEscapeUtils;

import com.derangementinc.gradespeedmobile.ConnectionManager;
import com.derangementinc.gradespeedmobile.R;

public class SpecificsExpandableAdapter extends BaseExpandableListAdapter {
	
	Context context;
	int[]   colors = {Color.rgb(0x00, 0x55, 0xcc), Color.BLACK};
	int     index  = 0; 
	
	public SpecificsExpandableAdapter(Context context) {
		this.context = context;
	}
	
	private int getNextColor() {
		index = (index + 1) % 2;
		return colors[index];
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return ConnectionManager.LongGrades.get(groupPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convert, ViewGroup parent) {
		String[][] table = ConnectionManager.LongGrades.get(groupPosition);
		if (convert == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convert = inflater.inflate(R.layout.expandable_item, null);
		}
		
		TableLayout tableView = (TableLayout) convert.findViewById(R.id.table_item);
		tableView.removeAllViews();
		index = 0;
		
		for (int rows = 0; rows < table.length; rows++) {
			TableRow row = new TableRow(context);
			tableView.addView(row);
			int rowColor = getNextColor();
			
			for (int columns = 0; columns < table[rows].length; columns++) {
				String cell = table[rows][columns];
				if (cell == null)
					break;
				
				cell = StringEscapeUtils.unescapeHtml4(cell);
				
				TextView column = new TextView(context);
				column.setText(cell);
				row.addView(column);
				column.setTextSize(20);
				column.setPadding(5, 3, 5, 3);
				column.setBackgroundColor(rowColor);
				if (rows == 0) {
					column.setTypeface(null, Typeface.BOLD);
				}
				
			}
		}
		return convert;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return ConnectionManager.LongGradesHeaders.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return ConnectionManager.LongGradesHeaders.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convert, ViewGroup parent) {
		String headerTitle = ConnectionManager.LongGradesHeaders.get(groupPosition);
		
		if (convert == null) {
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convert = inflater.inflate(R.layout.expandable_group, null);
		}
		
		((TextView) convert.findViewById(R.id.lblListHeader)).setText(StringEscapeUtils.unescapeHtml4(headerTitle));
		return convert;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
}