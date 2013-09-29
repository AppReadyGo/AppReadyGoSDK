/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.appreadygo.sdk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * @author Philip Belder
 * @version 1.2
 * @since 0.3
 */

class DbAdapter {

	// what
	int i = 0;
	public static final String KEY_XCOORD = "XTouch";
	public static final String KEY_YCOORD = "YTouch";
	public static final String KEY_WIDTH = "Width";
	public static final String KEY_PRESSURE = "Pressure";
	public static final String KEY_SEQUENCE = "Sequence";
	public static final String KEY_TIMESTAMP = "Timestamp";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_APPLICATION_ID = "AppId";
	public static final String KEY_SCREEN_ID = "ScreenId";
	public static final String KEY_VIEW_PART_X = "ViewPartX";
	public static final String KEY_VIEW_PART_Y = "ViewPartY";
	public static final String KEY_OFFSET_X = "OffsetX";
	public static final String KEY_OFFSET_Y = "OffsetY";
	public static final String KEY_TIMESTAMP_START = "TimestampStart";
	public static final String KEY_TIMESTAMP_END = "TimestampEnd";
	public static final String KEY_SCROLL_ID = "ScrollId";
	public static final String KEY_ORIENTATION = "Orientation";
	
	public static final String KEY_VIEWID = "ViewID";
	public static final String KEY_CLIENTH = "ClientH";
	public static final String KEY_CLIENTW = "ClientW";
	public static final String KEY_SESSION_ID = "SessionId";
	public static final String KEY_LIMIT = "LIMIT";
	
	public static final String KEY_CONTROL_TAG = "ControlTag";
	

	private static final String TAG = DbAdapter.class.getSimpleName();
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */

	private static final String DATABASE_NAME = "TouchDb";

	private static final String TABLE_TOUCH = "Touch";
	private static final String TABLE_VIEW_PART = "ViewPart";
	private static final String TABLE_CONTROL_CLICK = "ControlClick";
	protected static final String TABLE_SESSION = "Session";

	private static final int DATABASE_VERSION = 20;

	private static final String CREATE_TOUCH_TABLE = "create table "
			+ TABLE_TOUCH
			+ " (_id integer primary key autoincrement, "
			+ "XTouch real , YTouch real, Width real, Pressure real, Timestamp text"
			+ ", SessionId text,"
			+ "AppId text, ScreenId text,Sequence integer,OffsetX real, OffsetY real,ScrollId text"
			+ ", Orientation real" + ");";

	private static final String CREATE_VIEW_PART_TABLE = "create table "
			+ TABLE_VIEW_PART + " (_id integer primary key autoincrement, "
			+ "Timestamp text, TimestampEnd text" + ", SessionId text,"
			+ "ScreenId text, ViewPartX real, ViewPartY real, Orientation real"
			+ ");";

	private static final String CREATE_SESSION_TABLE = "create table "
			+ TABLE_SESSION
			+ " (_id integer primary key autoincrement,SessionId text, "
			+ "ViewID text, ClientH integer, ClientW integer, TimestampStart text,"
			+ "TimestampEnd text" + ");";
	
	private static final String CREATE_CONTROL_CLICK_TABLE = "create table "
		+ TABLE_CONTROL_CLICK
		+ " (_id integer primary key autoincrement,SessionId text, "
		+ "ControlTag text,"
		+ "Timestamp text" + ");";

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(CREATE_TOUCH_TABLE);
			db.execSQL(CREATE_VIEW_PART_TABLE);
			db.execSQL(CREATE_SESSION_TABLE);
			db.execSQL(CREATE_CONTROL_CLICK_TABLE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			InternalLog
					.d(TAG, "Upgrading database from version " + oldVersion
							+ " to " + newVersion
							+ ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOUCH);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIEW_PART);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTROL_CLICK);

			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public DbAdapter(Context ctx) {
		this.mCtx = ctx;

	}

	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Create a new note using the title and body provided. If the note is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param title
	 *            the title of the note
	 * @param body
	 *            the body of the note
	 * @return rowId or -1 if failed
	 */
	public long createTouch(WSInnerTouchData touch) {

		try {
			if (fetchTableCount(TABLE_TOUCH) > ApplicationConstants.maxRowsInTable) {
				long rowId = fetchMin(TABLE_TOUCH, KEY_ROWID);
				deleteTouch(rowId);
			}
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_TIMESTAMP, touch.getTimestamp());
			initialValues.put(KEY_XCOORD, touch.getX());
			initialValues.put(KEY_YCOORD, touch.getY());
			initialValues.put(KEY_PRESSURE, touch.getmPressure());
			initialValues.put(KEY_WIDTH, touch.getmWidth());
			initialValues.put(KEY_SESSION_ID, touch.getSessionId());
			initialValues.put(KEY_SEQUENCE, touch.getSequence());
			initialValues.put(KEY_APPLICATION_ID, touch.getmApplicationId());
			initialValues.put(KEY_SCREEN_ID, touch.getmScreenId());
			initialValues.put(KEY_OFFSET_X, touch.getmOffsetX());
			initialValues.put(KEY_OFFSET_Y, touch.getmOffsetY());
			initialValues.put(KEY_SCROLL_ID, touch.getScrollId());
			initialValues.put(KEY_ORIENTATION, touch.getOrientation());

			return mDb.insert(TABLE_TOUCH, null, initialValues);
			/*
			 * else { InternalLog.e
			 * (TAG,"Touch was not created; maximum number of rows exceeded");
			 * return -1; }
			 */
		} catch (Exception e) {
			InternalLog.d(TAG, "Failed to createTooch " + e.getMessage());
			return -1;
		}
	}

	public long createViewPart(WSViewAreaData wsViewPart, String sessionId) {
		/*
		 * try {
		 */
		if (fetchTableCount(TABLE_VIEW_PART) < ApplicationConstants.maxRowsInTable) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_TIMESTAMP, wsViewPart.getSessionStart());
			initialValues.put(KEY_TIMESTAMP_END, wsViewPart.getSessionEnd());
			initialValues.put(KEY_VIEW_PART_X, wsViewPart.getViewPartLeft());
			initialValues.put(KEY_VIEW_PART_Y, wsViewPart.getViewPartTop());
			initialValues.put(KEY_ORIENTATION, wsViewPart.getOrientation());
			initialValues.put(KEY_SESSION_ID, sessionId); // touch.getSession());

			return mDb.insert(TABLE_VIEW_PART, null, initialValues);
		} else {
			InternalLog
					.d(TAG,
							"ViewPart was not created; maximum number of rows exceeded");
			return -1;
		}

		/*
		 * }
		 * 
		 * catch(Exception e) { InternalLog.d (TAG,"Failed to createViewPart " +
		 * e.getMessage()); return -1; }
		 */

	}

	/**
	 * Delete all the Coordinates
	 * 
	 * @param delete
	 *            all coordinates TABLE_COORDINATES
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteTouch() {

		return mDb.delete(TABLE_TOUCH, null, null) > 0;
	}

	public boolean deleteSroll() {

		return mDb.delete(TABLE_VIEW_PART, null, null) > 0;
	}

	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteTouch(long rowId) {

		return mDb.delete(TABLE_TOUCH, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public boolean deleteControlClick(long rowId) {

		return mDb.delete(TABLE_CONTROL_CLICK, KEY_ROWID + "=" + rowId, null) > 0;
	}


	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllTouches() {

		return mDb.query(TABLE_TOUCH, new String[] { KEY_ROWID, KEY_SEQUENCE,
				KEY_XCOORD, KEY_YCOORD, KEY_PRESSURE, KEY_WIDTH, KEY_TIMESTAMP,
				KEY_SESSION_ID, KEY_APPLICATION_ID, KEY_SCREEN_ID,
				KEY_VIEW_PART_X, KEY_VIEW_PART_Y, KEY_ORIENTATION }, null,
				null, null, null, null);
	}

	public Cursor fetchAllViewParts() {

		return mDb.query(TABLE_VIEW_PART, new String[] { KEY_ROWID,
				KEY_TIMESTAMP, KEY_TIMESTAMP_END, KEY_SESSION_ID,
				KEY_SCREEN_ID, KEY_VIEW_PART_X, KEY_VIEW_PART_Y,
				KEY_ORIENTATION }, null, null, null, null, null);
	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */

	public Cursor fetchTouches(int numberOfRows, String sessionId)
			throws SQLException {

		return mDb.query(true, TABLE_TOUCH, new String[] { KEY_ROWID,
				KEY_XCOORD, KEY_YCOORD, KEY_TIMESTAMP, KEY_PRESSURE,
				KEY_SCROLL_ID, KEY_ORIENTATION }, KEY_SESSION_ID + "="
				+ sessionId, null, null, null, KEY_ROWID,
				Integer.toString(numberOfRows));

	}

	public Cursor fetchViewParts(int numberOfRows, String sessionId)
			throws SQLException {

		return mDb.query(TABLE_VIEW_PART, new String[] { KEY_ROWID,
				KEY_TIMESTAMP, KEY_TIMESTAMP_END, KEY_SESSION_ID,
				KEY_SCREEN_ID, KEY_VIEW_PART_X, KEY_VIEW_PART_Y,
				KEY_ORIENTATION }, KEY_SESSION_ID + "=" + sessionId, null,
				null, null, KEY_ROWID, Integer.toString(numberOfRows));
	}

	public void deleteTouches(WSInnerTouchData[] parrCd) {
		for (int i = 0; i < parrCd.length; i++) {
			WSInnerTouchData click = parrCd[i];
			if (click != null) {
				deleteTouch(click.getTimestamp());
			}
		}
		return;
	}

	public void deleteViewParts(WSViewAreaData[] parrVpd) {
		for (int i = 0; i < parrVpd.length; i++) {
			WSViewAreaData viewPart = parrVpd[i];
			if (viewPart != null) {
				deleteViewPart(viewPart.sd);
			}
		}
		return;
	}

	public long fetchTableCount(String tablename) {
		String sql = "SELECT COUNT(*) FROM " + tablename;
		SQLiteStatement statement = mDb.compileStatement(sql);
		long count = statement.simpleQueryForLong();
		return count;
	}

	private long fetchMin(String tablename, String columnName) {
		String sql = "SELECT min (" + columnName + ") FROM " + tablename;
		SQLiteStatement statement = mDb.compileStatement(sql);
		long min = statement.simpleQueryForLong();
		return min;
	}

	/**
	 * Update the note using the details provided. The note to be updated is
	 * specified using the rowId, and it is altered to use the title and body
	 * values passed in
	 * 
	 * @param rowId
	 *            id of note to update
	 * @param title
	 *            value to set note title to
	 * @param body
	 *            value to set note body to
	 * @return true if the note was successfully updated, false otherwise
	 */
	/******************************************************* SESSION TABLE *****************************************/
	/**
	 * 
	 * ViewId - text - text the Id of the view for this session ClientH -
	 * integer - content height ClientW - integer - content width SessionB -
	 * text - date of session beginning "EEE, dd MMM yyyy HH:mm:ss GMT" SessionC
	 * - text - date of session closing "EEE, dd MMM yyyy HH:mm:ss GMT"
	 * 
	 * @param sData
	 *            SessionData structure - all information from current session
	 * @return id of the current session -1 if the session is not saved
	 */

	Boolean createSessionData(WSSessionData sData) {
		try {

			// the new one
			if (fetchTableCount(TABLE_SESSION) < ApplicationConstants.maxRowsInTable) {
				ContentValues initialValues = new ContentValues();

				initialValues.put(KEY_VIEWID, sData.getViewId());
				initialValues.put(KEY_CLIENTH, sData.getCh());
				initialValues.put(KEY_CLIENTW, sData.getCw());
				initialValues.put(KEY_TIMESTAMP_START, sData.getSessionStart());
				initialValues.put(KEY_TIMESTAMP_END, sData.getSessionEnd());
				initialValues.put(KEY_SESSION_ID, sData.getSessionId());

				mDb.insert(TABLE_SESSION, null, initialValues);

				return true;
			} else {
				InternalLog
						.d(TAG,
								"ViewPart was not created; maximum number of rows exceeded");
				return false;
			}
		} catch (Exception e) {
			InternalLog.d(TAG, "Failed to createSession " + e.getMessage());
			return false;
		}
	}

	boolean deleteSession() {

		return mDb.delete(TABLE_SESSION, null, null) > 0;
	}

	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	boolean deleteSession(long rowId) {

		return mDb.delete(TABLE_TOUCH, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/* NOT TESTED */
	boolean deleteSession(String sessionId) {

		return mDb
				.delete(TABLE_SESSION, KEY_SESSION_ID + "=" + sessionId, null) > 0;
	}

	boolean deleteTouch(String sessionId) {

		return mDb.delete(TABLE_TOUCH, KEY_SESSION_ID + "=" + sessionId, null) > 0;
	}

	boolean deleteViewPart(String sessionId) {

		return mDb.delete(TABLE_VIEW_PART, KEY_SESSION_ID + "=" + sessionId,
				null) > 0;
	}

	Cursor fetchSessions(int numberOfRows) throws SQLException {

		return mDb.query(TABLE_SESSION, new String[] { KEY_ROWID, KEY_VIEWID,
				KEY_CLIENTH, KEY_CLIENTW, KEY_TIMESTAMP_START,
				KEY_TIMESTAMP_END, KEY_SESSION_ID }, null, null, null, null,
				KEY_ROWID, Integer.toString(numberOfRows));
	}

	boolean deleteSessionData(WSSessionData[] sessData) {
		try {
			InternalLog.d(TAG, "Deleting Session Data");
			open();
			mDb.beginTransaction();
			String sessionId;
			for (int i = 0; i < sessData.length; i++) {
				sessionId = sessData[i].getSessionId();
				deleteSession(sessionId);
				deleteTouch(sessionId);
				deleteViewPart(sessionId);
			}
		}
		catch (Exception e) {
			InternalLog.d(TAG, "Unable to delete Session");
			return false;
		} finally {
			mDb.setTransactionSuccessful();
			mDb.endTransaction();
			close();
		}
		return true;
	}//EOM

	public long createClick(Click click, String sessionid) {
		try {
			if (fetchTableCount(TABLE_CONTROL_CLICK) > ApplicationConstants.maxRowsInTable) {
				long rowId = fetchMin(TABLE_CONTROL_CLICK, KEY_ROWID);
				deleteTouch(rowId);
			}
			ContentValues initialValues = new ContentValues();

			initialValues.put(KEY_CONTROL_TAG, click.getControlTag());
			initialValues.put(KEY_SESSION_ID, sessionid);
			initialValues.put(KEY_TIMESTAMP, click.getTimestamp());
			
			return mDb.insert(TABLE_CONTROL_CLICK, null, initialValues);
			
		} catch (Exception e) {
			InternalLog.d(TAG, "Failed to createClick " + e.getMessage());
			return -1;
		}
		
	}

	public Cursor fetchClicks(int numberOfRows, String sessionId)
			throws SQLException {

		return mDb.query(TABLE_CONTROL_CLICK, new String[] { KEY_ROWID,
				KEY_TIMESTAMP, KEY_CONTROL_TAG }, KEY_SESSION_ID + "=" + sessionId, null,
				null, null, KEY_ROWID, Integer.toString(numberOfRows));
		
	}

}
