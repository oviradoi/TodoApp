package com.ove.todoapp.main;

import java.lang.reflect.Field;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ove.todoapp.R;
import com.ove.todoapp.common.TodoNote;
import com.ove.todoapp.adapters.TodosCursorAdapter;
import com.ove.todoapp.db.TodosDataSource;
import com.ove.todoapp.viewmodels.TodosViewModel;

public class TodoActivityDrawer extends ListActivity implements
		AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener,
		AbsListView.OnScrollListener,
		android.app.LoaderManager.LoaderCallbacks<Cursor>, TodoDrawerFragment.TodoDrawerCallbacks,
		TodosDataSource.DataChangeListener {

	private static final int LOADER_LISTS = 0;
	private static final int LOADER_TODOS = 1;

    private boolean detailsMode = false;

	// UI controls
	private TodosCursorAdapter adapter;
	private Button btnAdd;
	private TodoDrawerFragment mNavigationDrawerFragment;
	private ListView lstTodos;
	private EditText txtAdd;
    private MenuItem mnuDetailsBtn;
	
	private TodosViewModel viewModel;

	private long selectedListId;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		viewModel = new TodosViewModel(this);

		// Initialize the UI
		initializeUI();

		// Initialize the data source
		TodosDataSource.InitDataSource(this);

		// Fixes loader bug on orientation changed
		getLoaderManager();

		// Setup cursor adapter
		adapter = new TodosCursorAdapter(this, null, 0);
		setListAdapter(adapter);

		// Initialize the UI extras
		initializeUIExtras();

		// Set list listeners
		if (lstTodos != null) {
			// lstTodos.setOnItemClickListener(this);
			lstTodos.setOnItemLongClickListener(this);
			lstTodos.setOnScrollListener(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		getLoaderManager().destroyLoader(LOADER_LISTS);
		getLoaderManager().destroyLoader(LOADER_TODOS);
		TodosDataSource.getDataSource().removeChangeListener(this);
		TodosDataSource.getDataSource().close();
	}

	@Override
	protected void onResume() {
		super.onResume();
		TodosDataSource.getDataSource().open();
		TodosDataSource.getDataSource().addChangeListener(this);
		Loader<?> listLoader = getLoaderManager().getLoader(LOADER_LISTS);
		Loader<?> listTodos = getLoaderManager().getLoader(LOADER_TODOS);
		if (listLoader != null && listLoader.isReset()) {
			getLoaderManager().restartLoader(LOADER_LISTS, null, this);
		} else {
			getLoaderManager().initLoader(LOADER_LISTS, null, this);
		}
		if (listTodos != null && listTodos.isReset()) {
			getLoaderManager().restartLoader(LOADER_TODOS, null, this);
		} else {
			getLoaderManager().initLoader(LOADER_TODOS, null, this);
		}
	}

	private void initializeUI() {
		// Load the content view
		setContentView(R.layout.activity_todo_drawer);

		// Get the controls
		txtAdd = (EditText) findViewById(android.R.id.edit);
		lstTodos = (ListView) findViewById(android.R.id.list);
		btnAdd = (Button) findViewById(R.id.add);
		
		btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAdd();
			}
		});

		// Setup the drawer fragment
		mNavigationDrawerFragment = (TodoDrawerFragment) getFragmentManager().findFragmentById(
				R.id.navigation_drawer);
		mNavigationDrawerFragment.setCallbackReceiver(this);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		// Set the drawer shadow
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// Show overflow menu for devices with hardware menu buttons
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception ex) {
		}
	}

	private void initializeUIExtras() {
		// This shows/hides the Add button
		txtAdd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence str, int s, int b, int c) {
				int visibility = str.length() > 0 ? View.VISIBLE : View.GONE;
				btnAdd.setVisibility(visibility);
			}

			@Override
			public void beforeTextChanged(CharSequence str, int s, int c, int a) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		txtAdd.setText(txtAdd.getText());

		// This sets the IME action label for the soft keyboard
		txtAdd.setImeActionLabel("Add", 100);
		txtAdd.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == 100) {
					onAdd();
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			getMenuInflater().inflate(R.menu.main, menu);
            mnuDetailsBtn = menu.findItem(R.id.action_details);
            mnuDetailsBtn.setIcon(detailsMode ? R.drawable.nodetails : R.drawable.details);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterview, View view, int i, long l) {
		// TodoNote todonote = adapter.getTodoNote(i);
		// TodosDataSource.getDataSource().setChecked(todonote,
		// !todonote.isChecked());
	}

	public void onCheckboxClick(int position) {
		TodoNote todonote = adapter.getTodoNote(position);
		TodosDataSource.getDataSource().setChecked(todonote, !todonote.isChecked());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (menuItem.getItemId() == R.id.action_addList) {
			viewModel.createList();
			return true;
		} else if (menuItem.getItemId() == R.id.action_export) {
			viewModel.exportNotes();
			return true;
		} else if (menuItem.getItemId() == R.id.action_import) {
            viewModel.importNotes();
            return true;
        } else if (menuItem.getItemId() == R.id.action_details){
            detailsMode = !detailsMode;
            mnuDetailsBtn.setIcon(detailsMode ? R.drawable.nodetails : R.drawable.details);
            adapter.setDetailsMode(detailsMode);
            getListView().setAdapter(getListView().getAdapter());
            getLoaderManager().restartLoader(LOADER_TODOS, null, this);
            return true;
		} else {
			return super.onOptionsItemSelected(menuItem);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderid, Bundle bundle) {
		switch (loaderid) {
		case LOADER_LISTS:
			return new CursorLoader(this) {
				public Cursor loadInBackground() {
					return TodosDataSource.getDataSource().getListsCursor();
				}
			};
	
		case LOADER_TODOS:
			final long listid = selectedListId;
			return new CursorLoader(this) {
				public Cursor loadInBackground() {
					return TodosDataSource.getDataSource().getTodosCursor(listid);
				}
			};
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int loaderId = loader.getId();
		if (loaderId == LOADER_LISTS) {
			mNavigationDrawerFragment.swapCursor(cursor);
			if (TodosDataSource.getDataSource().listExists(selectedListId) == false) {
				int listsCount = TodosDataSource.getDataSource().getListsCursor().getCount();
				if (listsCount > 0) {
					mNavigationDrawerFragment.selectPosition(0);
				} else {
					todoListSelected(0);
				}
			}
			btnAdd.setEnabled(selectedListId > 0);
			txtAdd.setEnabled(selectedListId > 0);
		} else if (loaderId == LOADER_TODOS) {
			adapter.swapCursor(cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int loaderId = loader.getId();
		if (loaderId == LOADER_LISTS) {
			mNavigationDrawerFragment.swapCursor(null);
			btnAdd.setEnabled(false);
			txtAdd.setEnabled(false);
		} else if (loaderId == LOADER_TODOS) {
			adapter.swapCursor(null);
		}
	}

	@Override
	public void todoListSelected(long listId) {
		// A new list has been selected, restart the todos loader
		selectedListId = listId;
		getLoaderManager().restartLoader(LOADER_TODOS, null, this);
	}

	public void dataChanged(String table) {
		if (table == TodosDataSource.TABLE_TODOS) {
			getLoaderManager().restartLoader(LOADER_TODOS, null, this);
		} else if (table == TodosDataSource.TABLE_LISTS) {
			getLoaderManager().restartLoader(LOADER_LISTS, null, this);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final TodoNote note = adapter.getTodoNote(position);
	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Edit item").setItems(new String[] { "Rename", "Delete" },
				new DialogInterface.OnClickListener() {
	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							// Rename
							viewModel.renameNote(note);
						} else if (which == 1) {
							// Delete
							viewModel.deleteNote(note);
						}
					}
				});
		builder.create().show();
	
		return true;
	}

	@Override
	public void onScrollStateChanged(AbsListView abslistview, int i) {
		// Hide the soft keyboard when scrolling the list
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(btnAdd.getWindowToken(), 0);
	}

	@Override
	public void onScroll(AbsListView abslistview, int i, int j, int k) {
	}

	// Called when the user wants to add a new todo note
	private void onAdd() {
		// The text of the item we want to add
		String todoTxt = txtAdd.getText().toString();
		if (!todoTxt.trim().isEmpty()) {
			// Create the todo note
			TodosDataSource.getDataSource().createTodo(todoTxt, selectedListId);
			// Clear the edittext text
			txtAdd.setText("");
			txtAdd.clearFocus();
			// Hide the soft keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(btnAdd.getWindowToken(), 0);
			// Select the new todo item, this scrolls the list
			final int position = adapter.getCount() - 1;
			lstTodos.post(new Runnable() {
				@Override
				public void run() {
					lstTodos.setSelection(position);
				}
			});
		}
	}

	private void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
	}
}