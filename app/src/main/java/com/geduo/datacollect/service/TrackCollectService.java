package com.geduo.datacollect.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;

import com.geduo.datacollect.MainActivity;
import com.geduo.datacollect.R;
import com.geduo.datacollect.collect.TripTrackCollection;
import com.geduo.datacollect.contract.ITripTrackCollection;

/**
 * Description: <TrackCollectService><br>
 * Author:      gxl<br>
 * Date:        2018/12/7<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
public class TrackCollectService extends Service {
    private TripTrackCollection mTrackCollection;
    private boolean isstarting = false;

    @Override
    public IBinder onBind(Intent intent) {
        return new DataBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTrackCollection = TripTrackCollection.getInstance(this);
        startFrontService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTrackCollection.destory();
    }

    class DataBinder extends Binder implements ITripTrackCollection {

        @Override
        public void start() {
            if (isstarting) {
                return;
            }
            if (mTrackCollection == null) {
                mTrackCollection = TripTrackCollection.getInstance(TrackCollectService.this);
            }
            mTrackCollection.start();
            isstarting = true;
        }

        @Override
        public void stop() {
            if (mTrackCollection != null) {
                mTrackCollection.stop();
            }
            isstarting = false;
        }

        @Override
        public void pause() {

        }

        @Override
        public void saveHoldStatus() {

        }

        @Override
        public void destory() {

        }
    }

    // 发一个通知，使该服务成为一个前台服务，延长其服务的生命周期
    private void startFrontService() {
        Intent targetIntent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, targetIntent, 0);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("DataCollect");
        builder.setContentText("正在进行数据采集");
        builder.setContentIntent(pIntent);
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
            startForeground(100, notification);// 开启前台服务
        }
    }
}
