package com.geduo.datacollect.provider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.geduo.datacollect.util.SyncControl;

/**
 * Description: <SyncContentProvider><br>
 * Author:      gxl<br>
 * Date:        2018/12/15<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */

public class SyncContentProvider extends ContentProvider {
    public static final String CONTENT_URI_BASE = "content://" + SyncControl.CONTENT_AUTHORITY;
    public static final String TABLE_NAME = "data";
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_BASE + "/" + TABLE_NAME);

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return new String();
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}