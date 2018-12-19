package com.geduo.datacollect;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class MyApplication extends Application {
    public static MyApplication application;
    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Stetho.initializeWithDefaults(this);
    }
    public static MyApplication getInstance(){
        return application;
    }
}
