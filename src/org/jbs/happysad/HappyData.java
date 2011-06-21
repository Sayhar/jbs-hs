/**
 * 
 * Pattered off of the EventsData (v3) class in Hello Android.
 */

package org.jbs.happysad;


import static android.provider.BaseColumns._ID;
import static org.jbs.happysad.Constants.EMO;
import static org.jbs.happysad.Constants.LAT;
import static org.jbs.happysad.Constants.LONG;
import static org.jbs.happysad.Constants.MSG;
import static org.jbs.happysad.Constants.TABLE_NAME;
import static org.jbs.happysad.Constants.TIME;
import static org.jbs.happysad.Constants.UID;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HappyData {

	private HappyDB h;
	private static final String TAG = "HappyData";
	private static final int MyUserID = 1; 
	// ^ this should not be hardcoded to 1
	private static String[] FROM = { _ID, LAT, LONG, EMO, MSG, TIME, UID };
	private static String ORDER_BY = TIME + " DESC";
	
	
	public HappyData(Context ctx){
		h = new HappyDB(ctx);
	}
	
	
	/**
	 * Tries to add a bottle to the local db. Return true/false if it was successful or not.
	 * @param b
	 * @return Did our attempt to add the bottle succeed?
	 */
	public boolean addBottle(HappyBottle b){
		boolean toreturn = false;
		SQLiteDatabase db = h.getWritableDatabase();
		ContentValues values = b.getAll();
		try {
			db.insertOrThrow(TABLE_NAME, null, values);
			Log.d(TAG, "saved update to db");
			toreturn = true;
		}
		catch(Exception e){
			toreturn = false;
			Log.d(TAG, "failed to save update");
		}
		finally{
			h.close();
			return toreturn;
		}
				
	}
	
	public ArrayList<HappyBottle> getAllHistory(){
		Cursor cursor = getCursor();
		ArrayList<HappyBottle> a = new ArrayList<HappyBottle>();
		while (cursor.moveToNext() ){
			HappyBottle b = createBottle(cursor);
			a.add(b);
		}
		return a;
	}
	
	private HappyBottle createBottle(Cursor cursor){
		
		long uid = cursor.getLong(1);
		float latitude = cursor.getFloat(2);
		float longitude = cursor.getFloat(3);
		float emo = cursor.getFloat(4);
		String msg = cursor.getString(5);
		long time = cursor.getLong(6);
		
		HappyBottle b = new HappyBottle(uid, latitude, longitude, emo, msg, time);
		return b;
	}
	
	public ArrayList<HappyBottle> getMyHistory(){
		Cursor cursor = getCursor();
		ArrayList<HappyBottle> a = new ArrayList<HappyBottle>();
		while (cursor.moveToNext() ){
			long id = cursor.getLong(0);
			if (id == MyUserID) {
				HappyBottle b = createBottle(cursor);
				a.add(b);
				}
		}
		return a;
	}
	
	private Cursor getCursor(){
		SQLiteDatabase db = h.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, FROM, null, null, null,
		           null, ORDER_BY);
		return cursor;
		
	}

	
}
