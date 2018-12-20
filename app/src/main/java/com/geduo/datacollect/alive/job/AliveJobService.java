package com.geduo.datacollect.alive.job;

import android.annotation.TargetApi;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.geduo.datacollect.MainActivity;
import com.geduo.datacollect.SplashActivity;
import com.geduo.datacollect.alive.onepx.SystemUtils;

/**
 * Description: <PlayerMusicService><br>
 * Author:      gxl<br>
 * Date:        2018/12/19<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
@TargetApi(21)
public class AliveJobService extends JobService {
    private final static String TAG = AliveJobService.class.getSimpleName();
    private volatile static Service mKeepAliveService = null;
    public static boolean isJobServiceAlive() {
        return mKeepAliveService != null;
    }

    private static final int MESSAGE_ID_TASK = 0x01;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (SystemUtils.isAPPALive(getApplicationContext(), "com.geduo.datacollect")) {
                Toast.makeText(getApplicationContext(), "app alive", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "app die", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AliveJobService.this,MainActivity.class));
            }
            jobFinished((JobParameters) msg.obj, false);
            return true;
        }
    });

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob start...");
        mKeepAliveService = this;
        Message msg = Message.obtain(mHandler, MESSAGE_ID_TASK, params);
        mHandler.sendMessage(msg);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob start...");
        mHandler.removeMessages(MESSAGE_ID_TASK);
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy start...");
    }

}
