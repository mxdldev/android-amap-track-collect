## 前言：
&nbsp;&nbsp;&nbsp;&nbsp;最近的京城的天气有些冷，天寒地冻，天气虽冷，但也无法阻挡我写文章的热情，之前很少写文章，记得写文章已经是很久很久以前的事情了，一直有计划说要写点什么，但是一直感觉没时间，没有什么可写，最近机会来了。
<p>&nbsp;&nbsp;&nbsp;&nbsp;这阵子由于项目需要，需要从手机上采集用户的运动轨迹数据，这样的功能大家都见到的很多了，比如咕咚、悦动圈，对跑步运动轨迹数据进行采集，再如，微信运动、钉钉运动，对于每一天走步进行计数，如果要记录轨迹就离不开的手机定位，如果要记录步数那就离不开陀螺仪（角速度传感器），花了一天多的时间，实现了一个定位数据实时采集的功能。
<p>&nbsp;&nbsp;&nbsp;&nbsp;技术类的文章不好写，现在写的人也不少，有的人虽然写的多，但是评价并不高，并不是技术不好，而是写的太枯燥了，深度把握不当，而且大部分读者都是初学者，所以我尽量以浅显易懂的文字把每个问题讲清楚。
<p>&nbsp;&nbsp;&nbsp;&nbsp;运动轨迹数据采集，那就离不开手机定位，定位不是本文的重点，如果不太熟悉定位知识请移步:https://www.jianshu.com/p/00420c1fefe2, 这篇文章很详细的阐述了，GPS定位，A-GPS，基站定位，WIFI定位等技术实现原理，本文重点在于GPS数据采集，数据存储。

## 1.UI效果图
&nbsp;&nbsp;&nbsp;&nbsp;我们先把实现的效果先看一下，有一个宏观上的认识，其实很简单，就一个打底的地图，三个按钮，开始、停止、显示。
点击开始按钮启动服务开始采集数据，点击停止按钮停止数据采集，点击显示把采集到的轨迹数据在地图上展示一下。<br>
![效果图](https://github.com/geduo83/TrackDataCollect/blob/master/app/src/main/assets/demo_map.jpg)
## 2.数据源的选择
&nbsp;&nbsp;&nbsp;&nbsp;数据采集可采用Android系统原生的定位服务，也可以使用第三方的定位服务比如高德定位，百度定位等，根据多年来的开发经验，还是高德好用些，曾经做导航的时候，就发现百度导航会出现主路辅路不分等其他的一些情况，前阵子还曝光了百度地图盗用高德地图的采集数据的丑闻，高德毕竟页是专业做地图出身的，而且现在都是免费的 ，高德定位的优势请参见：https://lbs.amap.com/faq/android/android-location/15
## 2.数据持久化
&nbsp;&nbsp;&nbsp;&nbsp;解决了数据源问题，接下来就问题就是，数据往哪里存的问题，在android系统中实现数据持久化通常有一下几个解决方案，
* 2.1 SharedPreferences<p>
适用与存储一些app的配置信息，例如缓存用户登录的用户名，密码等信息，版本信息等小量信息
* 2.2. ContentProvider<p>
它为不同的应用程序之间数据访问提供了统一的访问接口，例如通讯录数据，相册数据，这些数据在第三方app中经常会用到
* 2.3  File<p>
通过IO流，把数据存储于文件，文件内容可以是xml形式，也可以是json形式
* 2.4 SQLiteDatabase<p>
android系统自带的一个小型的关系型数据库<p>
&nbsp;&nbsp;&nbsp;&nbsp;很显然SharedPreferences、ContentProvider不在考虑范围，由于数据采集是一个持续时间长，频率高的操作，对于频繁对文件进行读写操作是非常消耗系统资源的，对于采集的多个文件也不好管理，如果删除某个点的数据，在整个文件中进行检索将是非常痛苦的，最要命的是，File文件只能存储在机身存储的外部存储，这个区域是一个共享区域，如果用户手贱，私自删除数据也是有可能的，存在极大的安全隐患。
<br>&nbsp;&nbsp;&nbsp;&nbsp;毋庸置疑使用SQLiteDatabase存储将是您最佳的选择

## 3.数据怎么存
&nbsp;&nbsp;&nbsp;&nbsp;解决了数据源和数据存到哪的问题，接下来就是怎么存的问题，数据采集操作一个持久操作，不能阻塞UI主线程，那就需要启动一个子线程了，直接让DB里面存，合适吗？采集一个往DB里面存储一个，如果按照1秒采集一次的速度来计算的话，那就就一分钟向数据库有60次的读写操作，要知道，在Android的世界里，所有的IO操作都是耗时的操作，怎么办，很简单，先把采集到数据缓存到内存中，缓存到一定程度，一次性全部取出来一把存入库中，问题不就解决了，按照20秒取一次的速度来取的话，一分钟只要存储三次就行了，一分钟就减少了57次对数据库的操作，大大的提升了数据采集的性能问题，分析到了这里，我们也就不难下结论了，毫不含糊先开启一个子线程来采集数据并将数据存入到内存，再开启一个定期任务的子线程负责从内存中取数据，并将数据存入数据库，有一点需要注意下，内存的数据结构我们用ArrayList实为不妥，多线程中有数据同步的问题，所以就只能Vector了，说道这里我们不难发现，这数据采集实现的过程其实就是我们常说的生产者与消费者的问题了
## 4. 提高进程优先级
&nbsp;&nbsp;&nbsp;&nbsp;数据采集是持久的操作，如果程序进入后台，过一段时间就很有可能被系统杀死，我们知道android系统的的进程，按照进程的优先级可划分为：前台进程、可见进程间、服务进程、后台进程、空进程，很显然我们需要启动一个Service服务来对数据进行采集和存储的操作，这样如果程序进入了后台，我们将一个后台进程提升为了服务进程，提升了系统的优先级，服务进程被系统杀死的概率将会大大降低。
<p>&nbsp;&nbsp;&nbsp;&nbsp;在长期的开发实践中证明后台服务进程在某些机型，也有被杀死的可能，需要我们进一步需要进程优先级，怎么办，真正的“黑科技”来了，通过android系统提供的账号同步机制SyncAdapter来实现进程的优先级，SysnAdapter服务工作在独立的进程，由操作系统调度，进程属于系统核心级别，系统不会被杀掉，而使用SyncAdapter的进程优先级本身也会提供，服务关联SyncAdapter后，进程的优先级变为1，仅仅低于前台正在运行的进程，因此大大降低了被系统杀掉的概率。

## 5.工作流程
有了以上的分析，现在不妨画个流程图，以便加深理解<br>
![工作流程图](https://github.com/geduo83/TrackDataCollect/blob/master/app/src/main/assets/collectdata_activity.png)
## 6.类关系图
![类关系图](https://github.com/geduo83/TrackDataCollect/blob/master/app/src/main/assets/collectdata_class.png)
## 7.代码实现
画好UML类图后，再去看源码，再也不怕迷路了
* 7.1 启动服务
```
 //MainActivity：启动轨迹信息收集服务
  private void startTrackCollectService() {
    Intent intent = new Intent(this, TrackCollectService.class);
    startService(intent);
    bindService(intent, new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        mTrackCollection = (ITripTrackCollection) service;
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {

      }
    }, Context.BIND_AUTO_CREATE);
  }
```
* 7.2 开始采集
```
  //TripTrackCollection：开始采集数据
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

  // 开启数据入库线程，二十秒秒中入一次库
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

```
* 7.3 停止采集
```
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
```
* 7.4 轨迹展示
```
//MainActivity：轨迹展示
  private void showTrack(List<LatLng> list) {
    if (list == null || list.size() == 0) {
      return;
    }

    final LatLngBounds.Builder mBuilder = new LatLngBounds.Builder();
    PolylineOptions polylineOptions = new PolylineOptions()
        .setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tour_track))
        .addAll(list);
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
            cameraUpdate = CameraUpdateFactory.newLatLng(mBuilder.build().southwest);
          } else {
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(mBuilder.build(), 20);
          }
          mMap.animateCamera(cameraUpdate);
        }
      }

    }, 500);
  }
```
## 问题反馈
在使用中有任何问题，欢迎反馈给我，可以用以下联系方式跟我交流
* QQ：303704981
* email：geduo_83@163.com
* weibo：[@geduo_83](http://www.weibo.com/geduo83)

## 关于作者
```
  var geduo_83 = {
    nickName  : "geduo_83",
    site : "http://www.weibo.com/geduo83"
  }
```

