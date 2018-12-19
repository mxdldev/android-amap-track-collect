package com.geduo.datacollect.onepx;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

/**
 * Description: <OnePxActivity><br>
 * Author:      gxl<br>
 * Date:        2018/12/18<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
public class OnePxActivity extends AppCompatActivity {
    private static final String TAG = "OnePxActivity";
    public static WeakReference<OnePxActivity> instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(): savedInstanceState = [" + savedInstanceState + "]");
        instance = new WeakReference<>(this);
        Window window = getWindow();
        window.setGravity(Gravity.TOP | Gravity.LEFT);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.x = 0;
        attributes.y = 0;
        attributes.height = 1;
        attributes.width = 1;
        window.setAttributes(attributes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if (isScreenOn()) {
            finishSelf();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        if (instance != null && instance.get() == this) {
            instance = null;
        }
    }

    public void finishSelf() {
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        finishSelf();
        return super.dispatchTouchEvent(motionEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        finishSelf();
        return super.onTouchEvent(motionEvent);
    }

    private boolean isScreenOn() {
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                return powerManager.isInteractive();
            } else {
                return powerManager.isScreenOn();
            }
        } catch (Exception e) {
            Log.e(TAG, "e:", e);
        }
        return false;
    }
}
