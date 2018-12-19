package com.geduo.datacollect.alive.onepx;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.geduo.datacollect.MainActivity;
import com.geduo.datacollect.alive.onepx.ScreenManager;
import com.geduo.datacollect.alive.onepx.SystemUtils;

/**
 * Description: <SinglePixelActivity>
 * Author:      gxl<br>
 * Date:        2018/12/7<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
public class SinglePixelActivity extends AppCompatActivity {
    private static final String TAG = "SinglePixelActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window mWindow = getWindow();
        mWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams attrParams = mWindow.getAttributes();
        attrParams.x = 0;
        attrParams.y = 0;
        attrParams.height = 300;
        attrParams.width = 300;
        mWindow.setAttributes(attrParams);
        // 绑定SinglePixelActivity到ScreenManager
        ScreenManager.getScreenManagerInstance(this).setSingleActivity(this);
    }

    @Override
    protected void onDestroy() {
        if(! SystemUtils.isAPPALive(this,"com.geduo.datacollect")){
            Intent intentAlive = new Intent(this, MainActivity.class);
            intentAlive.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentAlive);
        }
        super.onDestroy();
    }
}
