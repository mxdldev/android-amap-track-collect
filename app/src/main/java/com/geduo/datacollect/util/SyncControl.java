package com.geduo.datacollect.util;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.geduo.datacollect.service.SyncAccountService;

public class SyncControl {
	public static final long SYNC_FREQUENCY = 5; // 1 hour (in seconds)
	public static final String CONTENT_AUTHORITY = "com.geduo.datacollect";
	public static final String ACCOUNT_TYPE = "com.geduo.datacollect.account";

	@TargetApi(Build.VERSION_CODES.FROYO)
	public static void createSyncAccount(Context context) {
		Log.v("MYTAG", "createSyncAccount start...");
		Account account = SyncAccountService.getAccount(ACCOUNT_TYPE);
		AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
		if (accountManager.addAccountExplicitly(account, null, null)) {
			ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
			ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
			ContentResolver.addPeriodicSync(account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY);
		}
	}
	public static void triggerRefresh() {
		Log.v("MYTAG", "triggerRefresh start...");
		Bundle b = new Bundle();
		// Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
		b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		ContentResolver.requestSync(
				SyncAccountService.getAccount(ACCOUNT_TYPE), // Sync account
				CONTENT_AUTHORITY,                 // Content authority
				b);                                             // Extras
	}
}
