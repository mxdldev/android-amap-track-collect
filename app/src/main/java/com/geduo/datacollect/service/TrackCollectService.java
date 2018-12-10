package com.geduo.datacollect.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

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
            if(isstarting){
                return;
            }
            if(mTrackCollection == null){
                mTrackCollection = TripTrackCollection.getInstance(TrackCollectService.this);
            }
            mTrackCollection.start();
            isstarting = true;
        }

        @Override
        public void stop() {
            if(mTrackCollection != null){
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
}
