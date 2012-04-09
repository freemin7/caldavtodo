/*
 * CalDavToDoProvider.java
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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class CalDavToDoProvider extends ContentProvider 
{
	public static final String PROVIDER_NAME = "ms.jung.android.caldavtodo.provider";
	public static final Uri CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/todos");

	public static final String _ID = "_id";
	public static final String TODO = "todo";
	public static final String STATE = "state";
	public static final String COLOR = "color";
	
	// fields for Marten:
	public static final String SYNC_SOURCE = "sync_source";
	public static final String SYNC_VERSION = "sync_version";
	public static final String UID = "uid";
	public static final String ORIGINAL_INSTANCE = "original_instance";
	public static final String ORIGINAL_INSTANCE_TIME = "original_instance_time";
	public static final String DIRTY = "dirty";
	public static final String DELETED = "deleted";


	private static final int TODOS = 1;
	private static final int TODO_ID = 2;   

	private static final UriMatcher uriMatcher;
	static
	{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "todos", TODOS);
		uriMatcher.addURI(PROVIDER_NAME, "todos/#", TODO_ID);      
	}

	// For Database use:

	private SQLiteDatabase todosDB;
	private static final String DATABASE_NAME = "CalDavToDoDatabase";
	private static final String DATABASE_TABLE = "Todos";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_CREATE =
			"create table " + DATABASE_TABLE + 
			" (_id integer primary key autoincrement, "+
			TODO +" text not null, "+
			STATE+" int not null, "+
			COLOR+" int not null, "+
			// Fields for Marten:
			SYNC_SOURCE +" text, "+
			SYNC_VERSION +" text, "+
			UID +" text, " + 
			ORIGINAL_INSTANCE +" int, "+
			ORIGINAL_INSTANCE_TIME + " long, "+
			DIRTY +" int, "+
			DELETED +" int);";

	private static class DatabaseHelper extends SQLiteOpenHelper 
	{
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, 
				int newVersion) {
			Log.w("Content provider database", 
					"Upgrading database from version " + 
							oldVersion + " to " + newVersion + 
					", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS Todos");
			onCreate(db);
		}
	}   

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// arg0 = uri 
		// arg1 = selection
		// arg2 = selectionArgs
		int count=0;
		switch (uriMatcher.match(arg0)){
		case TODOS:
			count = todosDB.delete(
					DATABASE_TABLE,
					arg1, 
					arg2);
			break;
		case TODO_ID:
			String id = arg0.getPathSegments().get(1);
			count = todosDB.delete(
					DATABASE_TABLE,                        
					_ID + " = " + id + 
					(!TextUtils.isEmpty(arg1) ? " AND (" + 
							arg1 + ')' : ""), 
							arg2);
			break;
		default: throw new IllegalArgumentException(
				"Unknown URI " + arg0);    
		}       
		getContext().getContentResolver().notifyChange(arg0, null);
		return count;      
	}
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
		//---get all books---
		case TODOS:
			return "mvnd.android.cursor.dir/vnd.ms.jung.todos ";
			//---get a particular book---
		case TODO_ID:                
			return "vnd.android.cursor.item/vnd.ms.jung.todos ";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);        
		}   
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//---add a new book---
		long rowID = todosDB.insert(
				DATABASE_TABLE, "", values);

		//---if added successfully---
		if (rowID>0)
		{
			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);    
			return _uri;                
		}        
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		todosDB = dbHelper.getWritableDatabase();
		return (todosDB == null)? false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(DATABASE_TABLE);

		if (uriMatcher.match(uri) == TODO_ID)
			//---if getting a particular book---
			sqlBuilder.appendWhere(
					_ID + " = " + uri.getPathSegments().get(1));                

		if (sortOrder==null || sortOrder=="")
			sortOrder = TODO;

		Cursor c = sqlBuilder.query(
				todosDB, 
				projection, 
				selection, 
				selectionArgs, 
				null, 
				null, 
				sortOrder);

		//---register to watch a content URI for changes---
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, 
			String selection, String[] selectionArgs) 
	{
		int count = 0;
		switch (uriMatcher.match(uri)){
		case TODOS:
			count = todosDB.update(
					DATABASE_TABLE, 
					values,
					selection, 
					selectionArgs);
			break;
		case TODO_ID:                
			count = todosDB.update(
					DATABASE_TABLE, 
					values,
					_ID + " = " + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + 
							selection + ')' : ""), 
							selectionArgs);
			break;
		default: throw new IllegalArgumentException(
				"Unknown URI " + uri);    
		}       
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}