package com.geduo.datacollect.alive.notifiy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.geduo.datacollect.R;

/**
 * Description: <DaemonService><br>
 * Author:      gxl<br>
 * Date:        2018/12/19<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
public class DaemonService extends Service {
    private static final String TAG = DaemonService.class.getSimpleName();
    public static final int NOTICE_ID = 100;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle("DataCollect");
            builder.setContentText("DataCollect runing...");
            startForeground(NOTICE_ID, builder.build());

            Intent intent = new Intent(this, CancelService.class);
            startService(intent);
        } else {
            startForeground(NOTICE_ID, new Notification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mManager.cancel(NOTICE_ID);
        }
        Intent intent = new Intent(getApplicationContext(), DaemonService.class);
        startService(intent);
    }
}

