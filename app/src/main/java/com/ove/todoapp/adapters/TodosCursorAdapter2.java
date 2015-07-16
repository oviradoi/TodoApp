package com.ove.todoapp.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ove.todoapp.R;
import com.ove.todoapp.common.TodoNote;
import com.ove.todoapp.db.TodosDataSource;
import com.ove.todoapp.main.TodoActivityDrawer;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TodosCursorAdapter2 extends CursorRecyclerAdapter<TodosCursorAdapter2.TodosViewHolder> {

    private TodoActivityDrawer mActivity;

    public TodosCursorAdapter2(TodoActivityDrawer activity, Cursor cursor) {
        super(cursor);
        mActivity = activity;
    }

    @Override
    public void onBindViewHolderCursor(TodosViewHolder holder, Cursor cursor) {
        TodoNote note = TodosDataSource.noteFromCursor(cursor);
        holder.text1.setText(note.getText());
        if (holder.text2 != null) {
            if (note.isChecked()) {
                holder.text2.setText("Done " + SimpleDateFormat.getDateTimeInstance()
                        .format(note.getDoneDate()));
            } else {
                holder.text2.setText("Created " + SimpleDateFormat.getDateTimeInstance()
                        .format(note.getCreatedDate()));
            }
        }
        holder.chk.setChecked(note.isChecked());
        holder.note = note;
    }

    @Override
    public TodosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.chkview, parent, false);

        return new TodosViewHolder(this, itemView);
    }

    public void onCheckChanged(TodoNote note) {
        mActivity.onCheckboxClick(note);
    }

    public void onLongClick(TodoNote note){
        mActivity.onLongClick(note);
    }

    public static class TodosViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(android.R.id.text1)
        public TextView text1;
        @Bind(android.R.id.text2)
        public TextView text2;
        @Bind(android.R.id.checkbox)
        public CheckBox chk;
        @Bind(R.id.list_item)
        public RelativeLayout list_item;

        public TodoNote note;
        public TodosCursorAdapter2 parentAdapter;

        public TodosViewHolder(TodosCursorAdapter2 adapter, View itemView) {
            super(itemView);

            this.parentAdapter = adapter;

            ButterKnife.bind(this, itemView);

            chk.setOnClickListener(this);
            list_item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    parentAdapter.onLongClick(note);
                    return true;
                }
            });
        }

        @Override
        public void onClick(View view) {
            parentAdapter.onCheckChanged(note);
        }
    }
}
