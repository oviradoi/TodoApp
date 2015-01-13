package com.ove.todoapp.adapters;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ove.todoapp.R;
import com.ove.todoapp.common.TodoNote;
import com.ove.todoapp.db.TodosDataSource;
import com.ove.todoapp.main.TodoActivityDrawer;

public class TodosCursorAdapter extends CursorAdapter
{
    static class TodosViewHolder{
        public TextView txtView1;
        public TextView txtView2;
        public CheckBox chkView;
    }

    private LayoutInflater inflater;
    private ListView mListView;
    private TodoActivityDrawer mActivity;
    private boolean detailsMode = false;

    public TodosCursorAdapter(TodoActivityDrawer activity, Cursor cursor, int flags)
    {
        super(activity, cursor, flags);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListView = (ListView) activity.findViewById(android.R.id.list);
        mActivity = activity;
    }

    public TodoNote getTodoNote(int position)
    {
        Cursor cursor = getCursor();
        boolean success = cursor.moveToPosition(position);
        TodoNote todonote = null;
        if (success)
        {
            todonote = TodosDataSource.noteFromCursor(cursor);
        }
        return todonote;
    }

    public void setDetailsMode(boolean detailsMode){
        this.detailsMode = detailsMode;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = null;
        TodosViewHolder vh = new TodosViewHolder();

        if (detailsMode) {
            view = inflater.inflate(R.layout.chkview, parent, false);
            vh.txtView1 = (TextView) view.findViewById(android.R.id.text1);
            vh.txtView2 = (TextView) view.findViewById(android.R.id.text2);
            vh.chkView = (CheckBox) view.findViewById(android.R.id.checkbox);
        } else {
            view = inflater.inflate(R.layout.chkview_short, parent, false);
            vh.txtView1 = (TextView) view.findViewById(android.R.id.text1);
            vh.txtView2 = null;
            vh.chkView = (CheckBox) view.findViewById(android.R.id.checkbox);
        }

        view.setTag(vh);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TodosViewHolder holder = (TodosViewHolder) view.getTag();
        TodoNote todonote = TodosDataSource.noteFromCursor(cursor);
        holder.txtView1.setText(todonote.getText());
        if (holder.txtView2 != null) {
            if (todonote.isChecked()) {
                holder.txtView2.setText("Done " + SimpleDateFormat.getDateTimeInstance()
                        .format(todonote.getDoneDate()));
            } else {
                holder.txtView2.setText("Created " + SimpleDateFormat.getDateTimeInstance()
                        .format(todonote.getCreatedDate()));
            }
        }
        holder.chkView.setChecked(todonote.isChecked());
        holder.chkView.setOnClickListener(mOnCheckboxClickListener);
        if (todonote.isChecked()) {
            holder.txtView1.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | holder.txtView1.getPaintFlags());
            holder.txtView1.setTextColor(Color.GRAY);
            if (holder.txtView2 != null) {
                holder.txtView2.setTextColor(Color.GRAY);
            }
        } else {
            holder.txtView1.setPaintFlags(~Paint.STRIKE_THRU_TEXT_FLAG & holder.txtView1.getPaintFlags());
            holder.txtView1.setTextColor(Color.BLACK);
            if (holder.txtView2 != null) {
                holder.txtView2.setTextColor(Color.BLACK);
            }
        }
    }

    private OnClickListener mOnCheckboxClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = mListView.getPositionForView((View) v.getParent());
            mActivity.onCheckboxClick(position);
        }
    };
} 