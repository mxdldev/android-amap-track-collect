package com.geduo.datacollect.alive.onepx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Description: <ScreenManager>
 * Author:      gxl<br>
 * Date:        2018/12/7<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */

public class ScreenManager {
    private static final String TAG = "ScreenManager";
    private Context mContext;
    private static ScreenManager mSreenManager;
    private WeakReference<Activity> mActivityRef;

    private ScreenManager(Context mContext){
        this.mContext = mContext;
    }

    // 单例模式
    public static ScreenManager getScreenManagerInstance(Context context){
        if(mSreenManager == null){
            mSreenManager = new ScreenManager(context);
        }
        return mSreenManager;
    }

    public void setSingleActivity(Activity mActivity){
        mActivityRef = new WeakReference<>(mActivity);
    }

    public void startActivity(){
        Intent intent = new Intent(mContext,SinglePixelActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void finishActivity(){
        if(mActivityRef != null){
            Activity mActivity = mActivityRef.get();
            if(mActivity != null){
                mActivity.finish();
            }
        }
    }
}
