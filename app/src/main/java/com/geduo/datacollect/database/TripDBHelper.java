package com.geduo.datacollect.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <><br>
 * Author: gxl<br>
 * Date: 2018/12/6<br>
 * Version: V1.0.0<br>
 * Update: <br>
 */
public class TripDBHelper extends SQLiteOpenHelper {
  public static final String TAG = TripDBHelper.class.getName();
  private static String mDBName = "yesway_track.db";
  private static int VERSION = 1;
  private static String TABLAE_NAME = "track";
  private static TripDBHelper mTripDBHelper;
  private StringBuffer mStringBuffer;
  private ContentValues mContentValues;// 要插入的数据包

  public static TripDBHelper getInstance(Context context) {
    if (mTripDBHelper == null) {
      synchronized (TripDBHelper.class) {
        mTripDBHelper = new TripDBHelper(context);
      }
    }
    return mTripDBHelper;
  }

  private TripDBHelper(Context context) {
    super(context, mDBName, null, VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(
        "create table " + TABLAE_NAME + "(trackid varchar(64),tracktime varchar(20),latlngs text)");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }

  /**
   * 添加轨迹数据
   *
   * @param
   * @param trackid
   * @param tracktime
   * @param newLatLngs
   */
  public void addTrack(String trackid, String tracktime, String newLatLngs) {
    Log.v("MYTAG", "addTrack start...");
    if (TextUtils.isEmpty(newLatLngs)) {
      Log.v("MYTAG", "Vector nodata");
      return;
    }
    if (mStringBuffer == null) {
      mStringBuffer = new StringBuffer();
    }
    SQLiteDatabase mDatabase = null;
    try {
      mDatabase = getReadableDatabase();
      Cursor cursor = null;
      // 查找库里面有没有之前存储过当前trackid的数据
      if (!TextUtils.isEmpty(trackid)) {
        cursor = mDatabase.rawQuery("select * from " + TABLAE_NAME + " where trackid = ?",
            new String[] {trackid});
      }
      // 如果之前存储过
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        String latlngs = cursor.getString(cursor.getColumnIndex("latlngs"));
        if (!TextUtils.isEmpty(latlngs)) {
          mStringBuffer.append(latlngs);
          Log.v("MYTAG", "old data:" + mStringBuffer.toString());
        }
        if (!TextUtils.isEmpty(newLatLngs)) {
          mStringBuffer.append(newLatLngs);
          Log.v("MYTAG", "new data:" + mStringBuffer.toString());
        }
        if (mContentValues == null) {
          mContentValues = new ContentValues();
        }
        mContentValues.clear();
        mContentValues.put("trackid", trackid);
        mContentValues.put("tracktime", tracktime);
        mContentValues.put("latlngs", mStringBuffer.toString());
        mDatabase.update(TABLAE_NAME, mContentValues, "trackid = ?", new String[] {trackid});
        Log.v("MYTAG", "update data succ");
      } else {
        if (mContentValues == null) {
          mContentValues = new ContentValues();
        }
        mContentValues.clear();
        mContentValues.put("trackid", trackid);
        mContentValues.put("tracktime", tracktime);
        mContentValues.put("latlngs", mStringBuffer.append(newLatLngs).toString());
        Log.v("MYTAG", "init data:" + mStringBuffer.toString());
        mDatabase.insert(TABLAE_NAME, null, mContentValues);
        Log.v("MYTAG", "init data succ");
      }
    } catch (Exception e) {
      Log.v("MYTAG", "addTrack error:" + e.toString());
      e.printStackTrace();
    } finally {
      if (mDatabase != null) {
        mDatabase.close();
      }
      if (mStringBuffer != null && !TextUtils.isEmpty(mStringBuffer.toString())) {
        mStringBuffer.delete(0, mStringBuffer.toString().length());
      }
    }
    Log.v("MYTAG", "addTrack end...");
  }

  public List<LatLng> getTrack(String trackid) {
    Log.v("MYTAG", "getTrack start...");
    SQLiteDatabase mDatabase = null;
    List<LatLng> listTrack = null;
    try {
      mDatabase = getReadableDatabase();
      Cursor cursor = null;
      // 查找库里面有没有之前存储过当前trackid的数据
      if (!TextUtils.isEmpty(trackid)) {
        cursor = mDatabase.rawQuery("select * from " + TABLAE_NAME + " where trackid = ?",
            new String[] {trackid});
      }
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        Log.v("MYTAG","hava data...");
        String latlngs = cursor.getString(cursor.getColumnIndex("latlngs"));
        if (!TextUtils.isEmpty(latlngs)) {
          listTrack = new ArrayList<>();
          String[] lonlats = latlngs.split("\\|");
          if (lonlats != null && lonlats.length > 0) {
            for (int i = 0; i < lonlats.length; i++) {
              String lonlat = lonlats[i];
              String[] split = lonlat.split(",");
              if (split != null && split.length > 0) {
                try {
                  listTrack
                      .add(new LatLng(Double.valueOf(split[0]), Double.valueOf(split[1])));
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (mDatabase != null) {
        mDatabase.close();
      }
    }
    return listTrack;
  }
}
