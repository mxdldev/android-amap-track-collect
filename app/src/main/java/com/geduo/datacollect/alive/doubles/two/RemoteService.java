package com.geduo.datacollect.alive.doubles.two;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.geduo.datacollect.IMyAidlInterface;

/**
 * Description: <><br>
 * Author:      gxl<br>
 * Date:        2018/12/18<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
public class RemoteService extends Service {
    MyConn conn;
    MyBinder binder;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("MYTAG", "remote onCreate start...");
        conn = new MyConn();
        binder = new MyBinder();
        bindService(new Intent(this, MainService.class), conn, Context.BIND_IMPORTANT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return RemoteService.class.getSimpleName();
        }
    }

    class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v("MYTAG", "remote onServiceConnected start...");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v("MYTAG", "remote onServiceDisconnected start...");
            startService(new Intent(RemoteService.this, MainService.class));
            bindService(new Intent(RemoteService.this, MainService.class), conn, Context.BIND_IMPORTANT);
        }

    }

    @Override
    public void onDestroy() {
        startService(new Intent(RemoteService.this, MainService.class));
        bindService(new Intent(RemoteService.this, MainService.class), conn, Context.BIND_IMPORTANT);
        Log.v("MYTAG", "remote onDestroy start...");
    }
}
