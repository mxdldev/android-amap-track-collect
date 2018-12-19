package com.geduo.datacollect.alive.sync.service;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.geduo.datacollect.MainActivity;
import com.geduo.datacollect.util.SyncSharedPreference;

/**
 * Description: <SyncAdapterService><br>
 * Author: gxl<br>
 * Date: 2018/12/17<br>
 * Version: V1.0.0<br>
 * Update: <br>
 */
public class SyncAdapterService extends Service {
	private static final Object lock = new Object();
	private static SyncAdapter mSyncAdapter = null;
	private static int sync_count = 0;
	@Override
	public void onCreate() {
		synchronized (lock) {
			if (mSyncAdapter == null) {
				mSyncAdapter = new SyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mSyncAdapter.getSyncAdapterBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
	}

	static class SyncAdapter extends AbstractThreadedSyncAdapter {
		public SyncAdapter(Context context, boolean autoInitialize) {
			super(context, autoInitialize);
		}

		@Override
		public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			// 具体的同步操作，这里主要是为了提高进程优先级
			Log.v("MYTAG", "onPerformSync start...");
			SyncSharedPreference.getInstance(getContext()).addSyncTime("sync_time"+(++sync_count));
		}
	}
}