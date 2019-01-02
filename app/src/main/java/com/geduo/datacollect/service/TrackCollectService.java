package com.geduo.datacollect.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.geduo.datacollect.MainActivity;
import com.geduo.datacollect.R;
import com.geduo.datacollect.collect.TripTrackCollection;
import com.geduo.datacollect.contract.ITripTrackCollection;
import com.geduo.datacollect.onepx.ScreenReceiver;

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
    private BroadcastReceiver mReceiver;
    private static int ROGUE_ID = 1;
    @Override
    public IBinder onBind(Intent intent) {
        return new DataBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTrackCollection = TripTrackCollection.getInstance(this);
        //法1：Notificationt提升优先级，用户有感知
        //startFrontService();

        //法2：Notificationt提升优先级，用户无感知
//        Intent intent = new Intent(this, RogueIntentService.class);
//        startService(intent);
//        startForeground(ROGUE_ID, new Notification());

        //法3：栈顶的Activity来保活
//        MyReceiver = new ScreenReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("android.intent.action.SCREEN_OFF");
//        intentFilter.addAction("android.intent.action.SCREEN_ON");
//        intentFilter.addAction("android.intent.action.USER_PRESENT");
//        registerReceiver(MyReceiver, intentFilter);



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTrackCollection.destory();
        //stopForeground(true);
        if(mReceiver != null){
            unregisterReceiver(mReceiver);
        }
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


    public static class RogueIntentService extends IntentService {

        //流氓相互唤醒Service
        public RogueIntentService(String name) {
            super(name);
        }

        public RogueIntentService() {
            super("RogueIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {

        }
        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(ROGUE_ID, new Notification());
        }
        @Override
        public void onDestroy() {
            stopForeground(true);
            super.onDestroy();
        }
    }

}
