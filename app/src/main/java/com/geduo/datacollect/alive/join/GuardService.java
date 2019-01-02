package com.geduo.datacollect.alive.join;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.geduo.datacollect.ProcessConnection;

/**
 * Description: <><br>
 * Author:      gxl<br>
 * Date:        2019/1/2<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */

public class GuardService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ProcessConnection.Stub() {
        };
    }


    @Override


    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1004, new Notification());
        //绑定建立链接
        bindService(new Intent(this, StepService.class), mServiceConnection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override


        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //链接上
            Log.d("MYTAG1", "GuardService:建立链接");
        }

        @Override


        public void onServiceDisconnected(ComponentName componentName) {
            //断开链接
            startService(new Intent(GuardService.this, StepService.class));
            //重新绑定
            bindService(new Intent(GuardService.this, StepService.class), mServiceConnection, Context.BIND_IMPORTANT);
        }

    };

}
 
