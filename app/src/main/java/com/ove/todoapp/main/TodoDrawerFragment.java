package com.ove.todoapp.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.ove.todoapp.R;
import com.ove.todoapp.adapters.ListsCursorAdapter;
import com.ove.todoapp.common.TodoList;
import com.ove.todoapp.db.TodosDataSource;

public class TodoDrawerFragment extends ListFragment implements OnItemLongClickListener {

	private ListsCursorAdapter adapter;
	private TodoDrawerCallbacks callbackReceiver;
	private ListView list;
	private long selectedId;
	private int selectedPos;
	
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    private ActionBarDrawerToggle mDrawerToggle;

	public TodoDrawerFragment() {
		selectedId = -1;
		selectedPos = -1;
	}

	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		adapter = new ListsCursorAdapter(getActivity(), null, 0);
		list.setAdapter(adapter);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callbackReceiver = (TodoDrawerCallbacks)activity;
	}
	
    @Override
    public void onDetach() {
        super.onDetach();
        callbackReceiver = null;
    }

	@Override
	public View onCreateView(LayoutInflater layoutinflater, ViewGroup viewgroup, Bundle bundle) {
		View view = layoutinflater.inflate(R.layout.fragment_navigation_drawer, viewgroup, false);
		list = (ListView) view.findViewById(android.R.id.list);
		list.setOnItemLongClickListener(this);
		return view;
	}

	public void onListItemClick(ListView listview, View view, int i, long l) {
		selectPosition(i);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

	public void selectPosition(int pos) {
		if (adapter != null) {
			int count = adapter.getCount();
			Cursor cursor = adapter.getCursor();
			if (pos < count && pos >= 0 && cursor != null) {
				selectedId = adapter.getItemId(pos);
				selectedPos = pos;
				list.setItemChecked(selectedPos, true);
				if (mDrawerLayout != null) {
		            mDrawerLayout.closeDrawer(mFragmentContainerView);
		        }
				if (callbackReceiver != null) {
					callbackReceiver.todoListSelected(selectedId);
				}
			}
		}
	}

	public void setCallbackReceiver(TodoDrawerCallbacks tododrawercallbacks) {
		callbackReceiver = tododrawercallbacks;
	}

	public void swapCursor(Cursor cursor) {
		adapter.swapCursor(cursor);
		if ((selectedId == -1 || selectedPos == -1) && cursor != null) {
			selectPosition(0);
		}
	}
	
	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        getActivityActionBar().setDisplayHomeAsUpEnabled(true);
        getActivityActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    // host Activity
                mDrawerLayout,                    // DrawerLayout object
                R.string.navigation_drawer_open,  // "open drawer" description for accessibility
                R.string.navigation_drawer_close  // "close drawer" description for accessibility
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };
        
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
	}
	
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }
	
    private void showGlobalContextActionBar() {
        getActivityActionBar().setDisplayShowTitleEnabled(true);
        getActivityActionBar().setTitle(R.string.app_name);
    }
    
	public void renameList(int position) {
		final TodoList list = adapter.getTodoList(position);
		final EditText input = new EditText(getActivity());
		input.setText(list.getName());
		input.setSelection(list.getName().length());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Rename list")
				.setMessage("Enter list name")
				.setIcon(R.drawable.ic_launcher)
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String value = input.getText().toString();
						TodosDataSource.getDataSource().renameList(list.getId(), value);
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
		
		final AlertDialog dialog = builder.create();
		input.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence str, int s, int b, int c) {
				dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(
						str.toString().trim().length() > 0);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		dialog.show();
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}
	
	public void deleteList(int position) {
		final TodoList list = adapter.getTodoList(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Delete?")
				.setMessage("Do you want to delete?")
				.setIcon(R.drawable.ic_launcher)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int j) {
								TodosDataSource.getDataSource().deleteList(list.getId());
								dialoginterface.dismiss();
							}
						})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int j) {
								dialoginterface.dismiss();
							}
						});
		builder.create().show();
	}

	public static interface TodoDrawerCallbacks {
		public abstract void todoListSelected(long l);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final int finalPos = position;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Edit list").setItems(
				new String[] { "Rename", "Delete" },
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							// Rename
							renameList(finalPos);
						} else if (which == 1) {
							// Delete
							deleteList(finalPos);
						}
					}
				});
		builder.create().show();
		
		return false;
	}

    private ActionBar getActivityActionBar(){
		AppCompatActivity activity = (AppCompatActivity)getActivity();
        if(activity!=null){
            return activity.getSupportActionBar();
        }
        return null;
    }
}