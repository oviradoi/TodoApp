package com.ove.todoapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import com.ove.todoapp.common.TodoList;
import com.ove.todoapp.common.TodoNote;

public class TodosDataSource {

	private static TodosDataSource _singletonInstance = null;

	public static TodosDataSource getDataSource() {
		return _singletonInstance;
	}

	public static void InitDataSource(Context context) {
		_singletonInstance = new TodosDataSource(context);
	}
	
	public static final String TABLE_LISTS = TodosDbHelper.TABLE_LISTS;
	public static final String TABLE_TODOS = TodosDbHelper.TABLE_TODOS;

	public final String allListsColumns[] = { TodosDbHelper.ColumnsLists.ID,
			TodosDbHelper.ColumnsLists.NAME, TodosDbHelper.ColumnsLists.CREATEDDATE };

	public final String allTodosColumns[] = { TodosDbHelper.ColumnsTodos.ID,
			TodosDbHelper.ColumnsTodos.LISTID, TodosDbHelper.ColumnsTodos.TEXT,
			TodosDbHelper.ColumnsTodos.CHECKED, TodosDbHelper.ColumnsTodos.CREATEDDATE,
			TodosDbHelper.ColumnsTodos.DONEDATE };

	private SQLiteDatabase database;
	private TodosDbHelper dbHelper;
	
	public interface DataChangeListener{
		void dataChanged(String table);
	}
	
	private ArrayList<DataChangeListener> changeListeners = new ArrayList<DataChangeListener>();

	private TodosDataSource(Context context) {
		dbHelper = new TodosDbHelper(context);
	}

	public static TodoList listFromCursor(Cursor cursor) {
		TodoList todolist = null;
		if (cursor != null && cursor.getCount() > 0) {
			todolist = new TodoList();
			todolist.setId(cursor.getInt(cursor.getColumnIndex(TodosDbHelper.ColumnsLists.ID)));
			todolist.setName(cursor.getString(cursor
					.getColumnIndex(TodosDbHelper.ColumnsLists.NAME)));
			todolist.setCreatedDate(new Date(cursor.getLong(cursor
					.getColumnIndex(TodosDbHelper.ColumnsLists.CREATEDDATE))));
		}
		return todolist;
	}

	public static TodoNote noteFromCursor(Cursor cursor) {
		TodoNote todonote = null;
		if (cursor != null && cursor.getCount() > 0) {
			todonote = new TodoNote();
			todonote.setId(cursor.getInt(cursor.getColumnIndex(TodosDbHelper.ColumnsTodos.ID)));
			todonote.setText(cursor.getString(cursor
					.getColumnIndex(TodosDbHelper.ColumnsTodos.TEXT)));
			int checkedInt = cursor.getInt(cursor
					.getColumnIndex(TodosDbHelper.ColumnsTodos.CHECKED));
			boolean checked = checkedInt != 0;
			todonote.setChecked(checked);
			long createdLong = cursor.getLong(cursor
					.getColumnIndex(TodosDbHelper.ColumnsTodos.CREATEDDATE));
			long doneLong = cursor.getLong(cursor
					.getColumnIndex(TodosDbHelper.ColumnsTodos.DONEDATE));
			todonote.setCreatedDate(new Date(createdLong));
			todonote.setDoneDate(doneLong > 0 ? new Date(doneLong) : null);
		}
		return todonote;
	}
	
	public void addChangeListener(DataChangeListener listener) {
		changeListeners.add(listener);
	}

	public void removeChangeListener(DataChangeListener listener) {
		changeListeners.remove(listener);
	}

	private void fireChangeListeners(String table) {
		for (DataChangeListener l : changeListeners) {
			l.dataChanged(table);
		}
	}

	public void close() {
		dbHelper.close();
		database = null;
	}

	public void createList(String listName) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(TodosDbHelper.ColumnsLists.NAME, listName);
		contentvalues.put(TodosDbHelper.ColumnsLists.CREATEDDATE,
				Calendar.getInstance().getTimeInMillis());
		database.insert(TodosDbHelper.TABLE_LISTS, null, contentvalues);
		fireChangeListeners(TodosDbHelper.TABLE_LISTS);
	}
	
	private long createList(String listName, long milis) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(TodosDbHelper.ColumnsLists.NAME, listName);
		contentvalues.put(TodosDbHelper.ColumnsLists.CREATEDDATE, milis);
		long rowid = database.insert(TodosDbHelper.TABLE_LISTS, null, contentvalues);
		fireChangeListeners(TodosDbHelper.TABLE_LISTS);
		return rowid;
	}

	public void renameList(long listId, String newName) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(TodosDbHelper.ColumnsLists.NAME, newName);
		database.update(TodosDbHelper.TABLE_LISTS, contentvalues, TodosDbHelper.ColumnsLists.ID + "=" + listId, null);
		fireChangeListeners(TodosDbHelper.TABLE_LISTS);
	}
	
	public boolean listExists(long listId) {
		Cursor c = database.query(TodosDbHelper.TABLE_LISTS,
				allListsColumns, TodosDbHelper.ColumnsLists.ID
						+ "=" + listId, null, null, null, null);
		return (c.getCount() > 0);
	}

	public void createTodo(String text, long listId) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(TodosDbHelper.ColumnsTodos.TEXT, text);
		contentvalues.put(TodosDbHelper.ColumnsTodos.CHECKED, false);
		contentvalues.put(TodosDbHelper.ColumnsTodos.LISTID, listId);
		contentvalues.put(TodosDbHelper.ColumnsTodos.CREATEDDATE,
				Calendar.getInstance().getTimeInMillis());
		database.insert(TodosDbHelper.TABLE_TODOS, null, contentvalues);
		fireChangeListeners(TodosDbHelper.TABLE_TODOS);
	}
	
	private long createTodo(long listId, String text, Date createdDate, Date doneDate, boolean checked) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(TodosDbHelper.ColumnsTodos.LISTID, listId);
		contentvalues.put(TodosDbHelper.ColumnsTodos.TEXT, text);
		contentvalues.put(TodosDbHelper.ColumnsTodos.CHECKED, checked);
		contentvalues.put(TodosDbHelper.ColumnsTodos.CREATEDDATE, createdDate.getTime());
		if(doneDate != null){
			contentvalues.put(TodosDbHelper.ColumnsTodos.DONEDATE, doneDate.getTime());	
		}
		long newid = database.insert(TodosDbHelper.TABLE_TODOS, null, contentvalues);
		fireChangeListeners(TodosDbHelper.TABLE_TODOS);
		return newid;
	}

	public void deleteList(long listId) {
		database.delete(TodosDbHelper.TABLE_LISTS, TodosDbHelper.ColumnsLists.ID + "=" + listId, null);
		fireChangeListeners(TodosDbHelper.TABLE_LISTS);
	}

	public void deleteTodoNote(TodoNote todonote) {
		long id = todonote.getId();
		database.delete(TodosDbHelper.TABLE_TODOS, TodosDbHelper.ColumnsTodos.ID + "=" + id, null);
		fireChangeListeners(TodosDbHelper.TABLE_TODOS);
	}

	public Cursor getListsCursor() {
		return database.query(TodosDbHelper.TABLE_LISTS, allListsColumns, null, null, null, null, null);
	}

	public Cursor getTodosCursor() {
		return database.query(TodosDbHelper.TABLE_TODOS, allTodosColumns, null, null, null, null, null);
	}

	public Cursor getTodosCursor(long listId) {
		return database.query(TodosDbHelper.TABLE_TODOS, allTodosColumns,
				TodosDbHelper.ColumnsTodos.LISTID + "=" + listId, null, null, null, null);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void setChecked(TodoNote todonote, boolean checked) {
		database.beginTransaction();
		int checkedInt = checked ? 1 : 0;
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(TodosDbHelper.ColumnsTodos.CHECKED, checkedInt);
		if (checked) {
			contentvalues.put(TodosDbHelper.ColumnsTodos.DONEDATE, Calendar.getInstance()
					.getTimeInMillis());
		}
		database.update(TodosDbHelper.TABLE_TODOS, contentvalues, TodosDbHelper.ColumnsTodos.ID
				+ "=" + todonote.getId(), null);
		database.setTransactionSuccessful();
		database.endTransaction();
		fireChangeListeners(TodosDbHelper.TABLE_TODOS);
	}

	public void renameTodoNote(TodoNote note) {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(TodosDbHelper.ColumnsTodos.TEXT, note.getText());
		database.update(TodosDbHelper.TABLE_TODOS, contentvalues, TodosDbHelper.ColumnsTodos.ID
				+ "=" + note.getId(), null);
		fireChangeListeners(TodosDbHelper.TABLE_TODOS);
	}
	
	public void exportDatabase(File f) {
		try {
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			
			final String ns = null;

			XmlSerializer serializer = Xml.newSerializer();
			serializer.setFeature(
					"http://xmlpull.org/v1/doc/features.html#indent-output",
					true);
			serializer.setOutput(fos, "UTF-8");
			serializer.startDocument("UTF-8", true);
			serializer.startTag(ns, "Lists");

			Cursor listsCursor = getListsCursor();
			while (listsCursor.moveToNext() && listsCursor != null) {
				TodoList lst = listFromCursor(listsCursor);

				serializer.startTag(ns, "List");
				serializer.attribute(ns, "Name", lst.getName());
				serializer.attribute(ns, "CreatedDate",
						String.valueOf(lst.getCreatedDate().getTime()));

				Cursor todosCursor = getTodosCursor(lst.getId());
				while (todosCursor.moveToNext() && todosCursor != null) {
					TodoNote note = noteFromCursor(todosCursor);
					serializer.startTag(ns, "Note");
					serializer.attribute(ns, "Text", note.getText());
					serializer.attribute(ns, "Checked", String.valueOf(note.isChecked()));
					serializer.attribute(ns, "CreatedDate",
							String.valueOf(note.getCreatedDate().getTime()));
					if (note.getDoneDate() != null) {
						serializer.attribute(ns, "DoneDate",
								String.valueOf(note.getDoneDate().getTime()));
					}
					serializer.endTag(ns, "Note");
				}

				serializer.endTag(ns, "List");
			}
			serializer.endTag(ns, "Lists");
			serializer.endDocument();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void importDatabase(File f) {
		ContentHandler zz = new ContentHandler(){
			
			private long currentListId;
			
			@Override
			public void setDocumentLocator(Locator locator) {
			}

			@Override
			public void startDocument() throws SAXException {
			}

			@Override
			public void endDocument() throws SAXException {
			}

			@Override
			public void startPrefixMapping(String prefix, String uri)
					throws SAXException {
			}

			@Override
			public void endPrefixMapping(String prefix) throws SAXException {
			}

			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes atts) throws SAXException {
				if (localName.equals("List")) {
					String listname = atts.getValue("Name");
					String datestr = atts.getValue("CreatedDate");
					long milis = Long.parseLong(datestr);
					currentListId = createList(listname, milis);
				} else if (localName.equals("Note")) {
					String text = atts.getValue("Text");
					boolean checked = Boolean.valueOf(atts.getValue("Checked"));
					String createdDateStr = atts.getValue("CreatedDate");
					String doneDateStr = atts.getValue("DoneDate");
					Date createdDate = new Date(Long.parseLong(createdDateStr));
					Date doneDate = null;
					if (doneDateStr != null) {
						doneDate = new Date(Long.parseLong(doneDateStr));
					}
					createTodo(currentListId, text, createdDate, doneDate,
							checked);
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
			}

			@Override
			public void characters(char[] ch, int start, int length)
					throws SAXException {
			}

			@Override
			public void ignorableWhitespace(char[] ch, int start, int length)
					throws SAXException {
			}

			@Override
			public void processingInstruction(String target, String data)
					throws SAXException {
			}

			@Override
			public void skippedEntity(String name) throws SAXException {
			}
			
		};
		
		try {
			Xml.parse(new FileReader(f), zz);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
}