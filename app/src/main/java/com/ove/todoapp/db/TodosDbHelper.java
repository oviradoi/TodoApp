package com.ove.todoapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.InputStream;
import java.util.Scanner;

import com.ove.todoapp.R;

class TodosDbHelper extends SQLiteOpenHelper {

	public static final class ColumnsLists {
		public static final String ID = "_id";
		public static final String CREATEDDATE = "created_date";
		public static final String NAME = "name";
	}

	public static final class ColumnsTodos {
		public static final String ID = "_id";
		public static final String LISTID = "listid";
		public static final String TEXT = "text";
		public static final String CHECKED = "checked";
		public static final String CREATEDDATE = "created_date";
		public static final String DONEDATE = "done_date";
	}

	private static final String DATABASE_NAME = "todos.db";
	private static final int DATABASE_VERSION = 1;
	static final String TABLE_LISTS = "lists";
	static final String TABLE_TODOS = "todos";
	private final String[] createStr;

	TodosDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		InputStream dbCreateStream = context.getResources().openRawResource(R.raw.dbcreate);
		createStr = (new Scanner(dbCreateStream)).useDelimiter("\\A").next().split(";");
	}

	public void onCreate(SQLiteDatabase sqlitedatabase) {
		for (String s : createStr) {
			sqlitedatabase.execSQL(s);
		}
	}

	public void onUpgrade(SQLiteDatabase sqlitedatabase, int i, int j) {
		sqlitedatabase.execSQL("DROP TABLE IF EXISTS lists");
		sqlitedatabase.execSQL("DROP TABLE IF EXISTS todos");
		onCreate(sqlitedatabase);
	}
}