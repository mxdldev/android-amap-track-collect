package com.geduo.datacollect.alive.notifiy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.geduo.datacollect.R;

/**
 * Description: <CancelService><br>
 * Author:      gxl<br>
 * Date:        2018/12/19<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
public class CancelService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            startForeground(DaemonService.NOTICE_ID, builder.build());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(1000);
                    stopForeground(true);
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    manager.cancel(DaemonService.NOTICE_ID);
                    stopSelf();
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}