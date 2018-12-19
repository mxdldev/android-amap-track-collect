package com.geduo.datacollect.alive.music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.geduo.datacollect.R;

/**
 * Description: <PlayerMusicService><br>
 * Author:      gxl<br>
 * Date:        2018/12/19<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
public class PlayerMusicService extends Service {
    private final static String TAG = "PlayerMusicService";
    private MediaPlayer mMediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
            Log.d(TAG,"onCreate start...");
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.silent);
        mMediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPlayMusic();
            }
        }).start();
        return START_STICKY;
    }

    private void startPlayMusic(){
        if(mMediaPlayer != null){
                Log.d(TAG,"startPlayMusic start...");
            mMediaPlayer.start();
        }
    }

    private void stopPlayMusic(){
        if(mMediaPlayer != null){
                Log.d(TAG,"stopPlayMusic start...");
            mMediaPlayer.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayMusic();
        Log.d(TAG,TAG+"onDestroy start...");
        // 重启自己
        Intent intent = new Intent(getApplicationContext(),PlayerMusicService.class);
        startService(intent);
    }
}
