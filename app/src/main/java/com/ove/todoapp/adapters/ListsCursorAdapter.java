package com.ove.todoapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.ove.todoapp.common.TodoList;
import com.ove.todoapp.db.TodosDataSource;

public class ListsCursorAdapter extends CursorAdapter {

    private LayoutInflater inflater;

    public ListsCursorAdapter(Context context, Cursor cursor, int i) {
        super(context, cursor, i);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view.findViewById(android.R.id.text1);
        TodoList list = TodosDataSource.listFromCursor(cursor);
        tv.setText(list.getName());
    }

    public TodoList getTodoList(int position){
        Cursor cursor = getCursor();
        boolean success = cursor.moveToPosition(position);
        TodoList todolist = null;
        if (success)
        {
            todolist = TodosDataSource.listFromCursor(cursor);
        }
        return todolist;
    }
}