package com.geduo.datacollect.collect;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.geduo.datacollect.bean.LocationInfo;
import com.geduo.datacollect.contract.ITripTrackCollection;
import com.geduo.datacollect.database.TripDBHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description: <数据采集管理器><br>
 * Author: gxl<br>
 * Date: 2018/12/6<br>
 * Version: V1.0.0<br>
 * Update: <br>
 */
public class TripTrackCollection implements ITripTrackCollection {
  private Context mContext;
  private static TripTrackCollection mTripTrackCollection;
  private AMapLocationClient mlocationClient;
  private AMapLocationListener mAMapLocationListener;
  private Vector<LocationInfo> mLocations;
  private ScheduledExecutorService mDataBaseThread;// 入库线程
  private ExecutorService mVectorThread;// 入缓存线程
  private boolean isshowerror = true;
  private TripTrackCollection(Context context) {
    mContext = context;
    // 初始缓存集合
    mLocations = new Vector<>();
  }

  public static TripTrackCollection getInstance(Context context) {
    if (mTripTrackCollection == null) {
      synchronized (TripTrackCollection.class) {
        if (mTripTrackCollection == null) {
          mTripTrackCollection = new TripTrackCollection(context);
        }
      }
    }
    return mTripTrackCollection;
  }
  // 开始采集数据
  @Override
  public void start() {
    startLocation();
    startCollect();
  }

  // 开启定位服务
  private void startLocation() {
    Log.v("MYTAG", "startLocation start...");
    // 初始定位服务
    if(mlocationClient == null){
      mlocationClient = new AMapLocationClient(mContext);
    }
    // 初始化定位参数
    AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
    // 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
    // 设置定位间隔,单位毫秒,默认为2000ms
    mLocationOption.setInterval(2000);
    // 设置定位参数
    mlocationClient.setLocationOption(mLocationOption);

    mLocationOption.setOnceLocation(false);// 是否定位一次
    // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
    // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
    // 在定位结束后，在合适的生命周期调用onDestroy()方法
    // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
    // 启动定位
    // 设置定位监听
    mlocationClient.setLocationListener(new AMapLocationListener() {
      @Override
      public void onLocationChanged(final AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
          // 定位成功回调信息，设置相关消息
          // amapLocation.getLocationType();// 获取当前定位结果来源，如网络定位结果，详见定位类型表
          // amapLocation.getLatitude();// 获取纬度
          // amapLocation.getLongitude();// 获取经度
          // amapLocation.getAccuracy();// 获取精度信息
          if (mAMapLocationListener != null) {
            mAMapLocationListener.onLocationChanged(amapLocation);
          }
          if (mVectorThread == null) {
            mVectorThread = Executors.newSingleThreadExecutor();
          }
          Log.d("MYTAG",
              "lat:" + amapLocation.getLatitude() + "lon:" + amapLocation.getLongitude());
          // 避免阻塞UI主线程，开启一个单独线程来存入内存
          mVectorThread.execute(new Runnable() {
            @Override
            public void run() {
              mLocations
                  .add(new LocationInfo(amapLocation.getLatitude(), amapLocation.getLongitude()));
            }
          });
        } else {
          // 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
          Log.d("MYTAG", "location Error, ErrCode:" + amapLocation.getErrorCode() + ", errInfo:"
              + amapLocation.getErrorInfo());
          if(isshowerror){
            isshowerror = false;
            Toast.makeText(mContext, amapLocation.getErrorInfo(), Toast.LENGTH_LONG).show();
          }
        }
      }
    });
    mlocationClient.startLocation();
  }

  // 开启数据入库线程，五秒中入一次库
  private void startCollect() {
    Log.v("MYTAG", "startCollect start...");
    if (mDataBaseThread == null) {
      mDataBaseThread = Executors.newSingleThreadScheduledExecutor();
    }
    mDataBaseThread.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        // 取出缓存数据
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < mLocations.size(); i++) {
          LocationInfo locationInfo = mLocations.get(i);
          stringBuffer.append(locationInfo.getLat()).append(",").append(locationInfo.getLon())
              .append("|");
        }
        // 取完之后清空数据
        mLocations.clear();
        String trackid = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        TripDBHelper.getInstance(mContext).addTrack(trackid, trackid, stringBuffer.toString());
      }
    }, 1000 * 20, 1000 * 20, TimeUnit.MILLISECONDS);
  }
  //停止采集
  @Override
  public void stop() {
    Log.v("MYTAG", "stop start...");
    if (mlocationClient != null) {
      mlocationClient.stopLocation();
      mlocationClient = null;
    }
    // 关闭Vector线程
    if (mVectorThread != null) {
      mVectorThread.shutdownNow();
      mVectorThread = null;
    }
    // 关闭SaveDabase线程
    if (mDataBaseThread != null) {
      mDataBaseThread.shutdownNow();
      mDataBaseThread = null;
    }
    // 定期任务关闭后，需要把最后的数据同步到数据库
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 0; i < mLocations.size(); i++) {
      LocationInfo locationInfo = mLocations.get(i);
      stringBuffer.append(locationInfo.getLat()).append(",").append(locationInfo.getLon())
          .append("|");
    }
    // 取完之后清空数据
    mLocations.clear();
    String trackid = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    TripDBHelper.getInstance(mContext).addTrack(trackid, trackid, stringBuffer.toString());
  }

  @Override
  public void pause() {

  }

  @Override
  public void saveHoldStatus() {

  }


  @Override
  public void destory() {
    Log.v("MYTAG", "destory start...");
    stop();
  }

  public void setAMapLocationListener(AMapLocationListener AMapLocationListener) {
    mAMapLocationListener = AMapLocationListener;
  }
}
