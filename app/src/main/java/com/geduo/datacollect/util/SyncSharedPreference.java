package com.geduo.datacollect.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Description: <SyncSharedPreference><br>
 * Author: gxl<br>
 * Date: 2018/12/17<br>
 * Version: V1.0.0<br>
 * Update: <br>
 */
public class SyncSharedPreference {
	private static SyncSharedPreference mSyncSharedPreference;
	private SharedPreferences mSharedPreferences;

	private SyncSharedPreference(Context context) {
		mSharedPreferences = context.getSharedPreferences("com.geduo.collect", Context.MODE_PRIVATE);
	}

	public static SyncSharedPreference getInstance(Context context) {
		if (mSyncSharedPreference == null) {
			synchronized (SyncSharedPreference.class) {
				mSyncSharedPreference = new SyncSharedPreference(context);
			}
		}
		return mSyncSharedPreference;
	}
	public void addCollectStartTime(){
        mSharedPreferences.edit().putString("start_time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).apply();
    }
    public void addCollectEndTime(String key){
        mSharedPreferences.edit().putString(key,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).apply();
    }
    public void addSyncTime(String key){
        mSharedPreferences.edit().putString(key,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).apply();
    }
}
