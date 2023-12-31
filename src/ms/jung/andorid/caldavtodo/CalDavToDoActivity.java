/*
 * CalDavToDoActivity.java
 *
 * Created: Mo 5. Mär 09:29:53 CET 2012
 *
 * Copyright (C) 2012 Dipl.-Ing. Matthias Jung (matthias@jung.ms)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package ms.jung.andorid.caldavtodo;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CalDavToDoActivity extends ListActivity
{
	/// Some Definitions
	private static final int EDIT_DELETE = 1;
	private static final int ADD = 2;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		/// Setup Activity and View
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ListView listView =  getListView();

		//---add todo---
		//ContentValues values = new ContentValues();
		//values.put(CalDavToDoProvider.TODO, "Aufgabe 1");
		//values.put(CalDavToDoProvider.STATE, 1);     
		//values.put(CalDavToDoProvider.COLOR, 0xffff0000);        


		/// Create cursor to transfer data from SQLite to ListView 
		/// Fields from the database are linked to the text fields of the views.
		Cursor c = managedQuery(CalDavToDoProvider.CONTENT_URI , null, null, null, "todo asc");  

		String[] from = {
				CalDavToDoProvider._ID,
				CalDavToDoProvider.TODO,
				CalDavToDoProvider.STATE,
				CalDavToDoProvider.COLOR
		};  

		int[] to = {
				R.id.sqlID,
				R.id.checkBox,
				R.id.checkBox,
				R.id.colorBar
		}; 

		/// Create Cursor Adapter
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row, c, from, to);

		/// For special values, which are supposed to go to non-text fields we have a special ViewBinder
		adapter.setViewBinder(new CalDavToDoViewBinder());

		listView.setAdapter(adapter);

		/// Recognize simple Click 
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener
		(
				new OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						/// Change in the Content Provider
						CheckBox cb  = (CheckBox)view.findViewById(R.id.checkBox);
						TextView tv  = (TextView)view.findViewById(R.id.sqlID);
						ContentValues values = new ContentValues();

						/// Toggle Value
						values.put(CalDavToDoProvider.STATE, cb.isChecked() ? 0 : 1 );

						/// Update it
						getContentResolver().update(Uri.parse(CalDavToDoProvider.CONTENT_URI+"/"+tv.getText()), values, null, null);
					}
				}
				);

		/// Recognize long Click
		listView.setOnItemLongClickListener(
				new OnItemLongClickListener()
				{
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
					{
						/// Change in the Content Provider
						TextView tv = (TextView)view.findViewById(R.id.sqlID);
						CheckBox cb = (CheckBox)view.findViewById(R.id.checkBox);

						/// Call the Editor
						Intent i = new Intent(CalDavToDoActivity.this,CalDavToDoEditor.class);
						i.putExtra("todo", cb.getText());
						i.putExtra("id", tv.getText());
						i.putExtra("state", "null");
						startActivityForResult(i, EDIT_DELETE);

						return true; // if false is returned onItemClick() will do the job
					}
				}
				);
	}

	/// Callback for called Activities
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK)
		{
			if(requestCode == EDIT_DELETE) 
			{
				String todo = data.getStringExtra("todo");
				String id = data.getStringExtra("id");
				String state = data.getStringExtra("state");

				if(state.equals("edit"))
				{		  
					ContentValues values = new ContentValues();
					values.put(CalDavToDoProvider.TODO, todo);
					getContentResolver().update(Uri.parse(CalDavToDoProvider.CONTENT_URI+"/"+id), values, null, null);
				}
				else if(state.equals("delete"))
				{
					getContentResolver().delete(Uri.parse(CalDavToDoProvider.CONTENT_URI+"/"+id), null, null);
				}
			}
			if(requestCode == ADD ) 
			{
				String todo = data.getStringExtra("todo");
				int color = data.getIntExtra("color", 0xff000000);
				ContentValues values = new ContentValues();
				values.put(CalDavToDoProvider.TODO, todo);
				values.put(CalDavToDoProvider.STATE, 0);     
				values.put(CalDavToDoProvider.COLOR, color);        
				getContentResolver().insert(CalDavToDoProvider.CONTENT_URI, values);
			}
		}
	}

	/// Inflate OptionsMenu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/// Callback for the menu clicks
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
		case R.id.add:
			Intent i = new Intent(CalDavToDoActivity.this,CalDavToDoAdd.class);
			startActivityForResult(i, ADD); // add
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/// Short method to generate Toast
	private void prost(String s)
	{
		Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
		toast.show();
	}
}