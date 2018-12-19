package com.geduo.datacollect.alive.doubles.two;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.geduo.datacollect.IMyAidlInterface;
import com.geduo.datacollect.MainActivity;
import com.geduo.datacollect.R;

/**
 * Description: <><br>
 * Author:      gxl<br>
 * Date:        2018/12/18<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
public class MainService extends Service {
    MyBinder binder;
    MyConn conn;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("MYTAG", "main onCreate start...");
        binder = new MyBinder();
        conn = new MyConn();

        //设置该服务未前台服务
        Intent targetIntent = new Intent(this, MainActivity.class);
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            PendingIntent intent = PendingIntent.getActivity(this, 0, targetIntent, 0);
            notification = new Notification.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("DataCollect").setContentText("正在进行数据采集").setContentIntent(intent).build();
        } else {
            notification = new Notification();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        }
        startForeground(1001, notification);
        bindService(new Intent(MainService.this, RemoteService.class), conn, Context.BIND_IMPORTANT);
    }

    class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return MainService.class.getSimpleName();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v("MYTAG", "main onServiceConnected start...");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v("MYTAG", "main onServiceDisconnected start...");
            startService(new Intent(MainService.this, RemoteService.class));
            bindService(new Intent(MainService.this, RemoteService.class), conn, Context.BIND_IMPORTANT);
        }
    }

    @Override
    public void onDestroy() {
        Log.v("onDestroy", "main onDestroy start...");
        startService(new Intent(MainService.this, RemoteService.class));
        bindService(new Intent(MainService.this, RemoteService.class), conn, Context.BIND_IMPORTANT);
    }
}
