package com.geduo.datacollect;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.geduo.datacollect.alive.doubles.two.MainService;
import com.geduo.datacollect.alive.doubles.two.RemoteService;
import com.geduo.datacollect.alive.job.JobSchedulerManager;
import com.geduo.datacollect.alive.join.GuardService;
import com.geduo.datacollect.alive.join.JobWakeUpService;
import com.geduo.datacollect.alive.join.StepService;
import com.geduo.datacollect.alive.notifiy.DaemonService;
import com.geduo.datacollect.alive.music.PlayerMusicService;
import com.geduo.datacollect.alive.onepx.ScreenManager;
import com.geduo.datacollect.alive.onepx.ScreenReceiverUtil;
import com.geduo.datacollect.util.SyncSharedPreference;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.geduo.datacollect.contract.ITripTrackCollection;
import com.geduo.datacollect.database.TripDBHelper;
import com.geduo.datacollect.service.TrackCollectService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

	MapView mMapView = null;
	@BindView(R.id.btn_start)
	Button btnStart;

	@BindView(R.id.btn_stop)
	Button btnStop;

	@BindView(R.id.btn_show)
	Button btnShow;
	ITripTrackCollection mTrackCollection;
	private AMap mMap;
	private ScreenReceiverUtil mScreenListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.map);
		// 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
		mMapView.onCreate(savedInstanceState);
		mMap = mMapView.getMap();
		mMap.getUiSettings().setZoomControlsEnabled(false);
		startTrackCollectService();
		new RxPermissions(this).request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
			@Override
			public void accept(Boolean aBoolean) throws Exception {
				if (!aBoolean) {
					Toast.makeText(MainActivity.this, "没有相关权限", Toast.LENGTH_LONG).show();
				}
			}
		});
		//startPlayMusicService();
		//startAllServices();
		//法1：双服务保活
		//startService (new Intent (this, MainService.class));
		//startService (new Intent (this, RemoteService.class));
		startAllServices();
	}

	// 启动轨迹信息收集服务
	private void startTrackCollectService() {
		Intent intent = new Intent(this, TrackCollectService.class);
		startService(intent);
		bindService(intent, new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mTrackCollection = (ITripTrackCollection) service;
				SyncSharedPreference.getInstance(MainActivity.this).addCollectStartTime();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {

			}
		}, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		SyncSharedPreference.getInstance(MainActivity.this).addCollectEndTime("end_time_destory");
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
		mMapView.onPause();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		SyncSharedPreference.getInstance(MainActivity.this).addCollectEndTime("start_time_re_save_instance");
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		SyncSharedPreference.getInstance(MainActivity.this).addCollectEndTime("end_time_save_instance");

		super.onSaveInstanceState(outState);
		// 在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState
		// (outState)，保存地图当前的状态
		mMapView.onSaveInstanceState(outState);
	}

	// 轨迹展示
	private void showTrack(List<LatLng> list) {
		if (list == null || list.size() == 0) {
			return;
		}

		final LatLngBounds.Builder mBuilder = new LatLngBounds.Builder();
		PolylineOptions polylineOptions = new PolylineOptions().setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tour_track)).addAll(list);
		if (mMap != null) {
			mMap.clear();
			mMap.addPolyline(polylineOptions);
		}
		for (int i = 0; i < list.size(); i++) {
			mBuilder.include(list.get(i));
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				CameraUpdate cameraUpdate;
				// 判断,区域点计算出来,的两个点相同,这样地图视角发生改变,SDK5.0.0会出现异常白屏(定位到海上了)
				if (mBuilder != null && mMap != null) {
					LatLng northeast = mBuilder.build().northeast;
					if (northeast != null && northeast.equals(mBuilder.build().southwest)) {
						cameraUpdate = CameraUpdateFactory.newLatLngZoom(mBuilder.build().southwest, 17);
					} else {
						cameraUpdate = CameraUpdateFactory.newLatLngBounds(mBuilder.build(), 20);
					}
					mMap.animateCamera(cameraUpdate);
				}
			}

		}, 500);
	}

	@OnClick(R.id.btn_start)
	void onStartClick() {
		Log.v("MYTAG", "thread:" + Thread.currentThread().getId());
		// mTrackCollection = TripTrackCollection.getInstance(this);
		// mTrackCollection.start();
		if (mTrackCollection != null) {
			mTrackCollection.start();
		}
	}

	@OnClick(R.id.btn_stop)
	void onStopClick() {
		if (mTrackCollection != null) {
			mTrackCollection.stop();
			SyncSharedPreference.getInstance(MainActivity.this).addCollectEndTime("end_time_stop");
		}
	}

	@OnClick(R.id.btn_show)
	void onShowClick() {
		String trackid = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		List<LatLng> track = TripDBHelper.getInstance(this).getTrack(trackid);
		showTrack(track);
	}

	private void startDaemonService() {
		Intent intent = new Intent(this, DaemonService.class);
		startService(intent);
	}

	private void stopDaemonService() {
		Intent intent = new Intent(this, DaemonService.class);
		stopService(intent);
	}

	private void stopPlayMusicService() {
		Intent intent = new Intent(this, PlayerMusicService.class);
		stopService(intent);
	}

	private void startPlayMusicService() {
		Intent intent = new Intent(this, PlayerMusicService.class);
		startService(intent);
	}

	/**
	 * 开启所有Service
	 */
	private void startAllServices() {
		startService(new Intent(this, StepService.class));
		startService(new Intent(this, GuardService.class));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Log.d("MYTAG", "startAllServices: ");
			startService(new Intent(this, JobWakeUpService.class));
		}
	}

}
