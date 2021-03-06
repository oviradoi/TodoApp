package com.ove.todoapp.viewmodels;

import java.io.File;

import com.ove.todoapp.R;
import com.ove.todoapp.common.TodoNote;
import com.ove.todoapp.db.TodosDataSource;
import com.ove.todoapp.utils.SimpleOnClickListener;
import com.ove.todoapp.utils.SimpleTextWatcher;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class TodosViewModel {

	private Context context;
	
	public TodosViewModel(Context activityContext){
		this.context = activityContext;
	}
	
	public void deleteNote(TodoNote n) {
		final TodoNote note = n;
		// Build a dialog to ask the user if he's sure
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Delete?").setMessage("Do you want to delete?")
				.setIcon(R.drawable.ic_launcher)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int j) {
						TodosDataSource.getDataSource().deleteTodoNote(note);
					}
				}).setNegativeButton("No", new SimpleOnClickListener());
		builder.create().show();
	}
	
	public void renameNote(TodoNote n) {
		final TodoNote note = n;
		final EditText et = new EditText(context);
		et.setText(note.getText());
		et.setSelection(note.getText().length());
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Rename").setMessage("Enter new name").setView(et)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String value = et.getText().toString();
						note.setText(value);
						TodosDataSource.getDataSource().renameTodoNote(note);
					}
				}).setNegativeButton("Cancel", new SimpleOnClickListener());
		final AlertDialog dialog = builder.create();
		et.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(
						s.toString().trim().length() > 0);
			}
		});
		dialog.show();
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(
				et.getText().toString().trim().length() > 0);
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}
	
	public void createList(){
		final EditText input = new EditText(context);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("New list").setMessage("Enter list name").setIcon(R.drawable.ic_launcher)
				.setView(input).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String value = input.getText().toString();
						TodosDataSource.getDataSource().createList(value);
					}
				}).setNegativeButton("Cancel", new SimpleOnClickListener());
		final AlertDialog dialog = builder.create();
		input.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(
						s.toString().trim().length() > 0);
			}
		});
		dialog.show();
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}
	
	public void importNotes() {
		if (isExternalStorageWritable()) {
			try {
				File downloadsDir = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File file = new File(downloadsDir, "export.xml");
				TodosDataSource.getDataSource().importDatabase(file);
                Toast successToast = Toast.makeText(context, "Database imported from "+file.getAbsolutePath(), Toast.LENGTH_SHORT);
                successToast.show();
			} catch (Exception ex) {
                Toast errorToast = Toast.makeText(context, "Error while importing", Toast.LENGTH_SHORT);
                errorToast.show();
				ex.printStackTrace();
			}
		}
        else{
            Toast errorToast = Toast.makeText(context, "External storage is not writable", Toast.LENGTH_SHORT);
            errorToast.show();
        }
	}
	
	public void exportNotes() {
        if (isExternalStorageWritable()) {
            try {
                File downloadsDir = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, "export.xml");
                TodosDataSource.getDataSource().exportDatabase(file);
                Toast successToast = Toast.makeText(context, "Database exported to " + file.getAbsolutePath(), Toast.LENGTH_SHORT);
                successToast.show();
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast errorToast = Toast.makeText(context, "Error exporting database", Toast.LENGTH_SHORT);
                errorToast.show();
            }
        } else {
            Toast errorToast = Toast.makeText(context, "External storage is not writable", Toast.LENGTH_SHORT);
            errorToast.show();
        }
    }

	// Checks if external storage is available for read and write
	private boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
	}
}
