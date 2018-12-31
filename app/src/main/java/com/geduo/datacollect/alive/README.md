版权声明：本文出自门心叼龙的博客，转载请注明出处。https://blog.csdn.net/geduo_83/article/details/85483898
<br>github源码下载地址：https://github.com/geduo83/TrackDataCollect/blob/sync/app/src/main/java/com/geduo/datacollect/alive/

在上一篇文章[Android车辆运行轨迹大数据采集最佳实践](https://blog.csdn.net/geduo_83/article/details/84943984)这篇文章中我们讲到，数据采集服务是一个持久的操作，当采集服务进入后台后，有可能被系统杀死的可能，可用通过账号同步服务SysAdapter来提升进程的优先级来降低采集服务被后台杀死的几率。 本文共介绍了6中后台服务保活方案，下文将逐一介绍：
## 1.账号同步服务SysAdapter保活
* 1.1 通过ContentProvider实现数据同步
```
public class SyncContentProvider extends ContentProvider {
    public static final String CONTENT_URI_BASE = "content://" + SyncControl.CONTENT_AUTHORITY;
    public static final String TABLE_NAME = "data";
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_BASE + "/" + TABLE_NAME);

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return new String();
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
```
* 1.2 关联SyncAdapter通信服务
```
public class SyncAdapterService extends Service {
	private static final Object lock = new Object();
	private static SyncAdapter mSyncAdapter = null;
	private static int sync_count = 0;
	@Override
	public void onCreate() {
		synchronized (lock) {
			if (mSyncAdapter == null) {
				mSyncAdapter = new SyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mSyncAdapter.getSyncAdapterBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
	}

	static class SyncAdapter extends AbstractThreadedSyncAdapter {
		public SyncAdapter(Context context, boolean autoInitialize) {
			super(context, autoInitialize);
		}

		@Override
		public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			// 具体的同步操作，这里主要是为了提高进程优先级
			Log.v("MYTAG", "onPerformSync start...");
			SyncSharedPreference.getInstance(getContext()).addSyncTime("sync_time"+(++sync_count));
		}
	}
}
```
* 1.3 使用Sync服务
```
public class SyncControl {
	public static final long SYNC_FREQUENCY = 5; // 1 hour (in seconds)
	public static final String CONTENT_AUTHORITY = "com.geduo.datacollect";
	public static final String ACCOUNT_TYPE = "com.geduo.datacollect.account";

	@TargetApi(Build.VERSION_CODES.FROYO)
	public static void createSyncAccount(Context context) {
		Log.v("MYTAG", "createSyncAccount start...");
		Account account = SyncAccountService.getAccount(ACCOUNT_TYPE);
		AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
		if (accountManager.addAccountExplicitly(account, null, null)) {
			ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
			ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
			ContentResolver.addPeriodicSync(account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY);
		}
	}
	public static void triggerRefresh() {
		Log.v("MYTAG", "triggerRefresh start...");
		Bundle b = new Bundle();
		// Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
		b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		ContentResolver.requestSync(
				SyncAccountService.getAccount(ACCOUNT_TYPE), // Sync account
				CONTENT_AUTHORITY,                 // Content authority
				b);                                             // Extras
	}
}
```
## 2.Notication保活
* 2.1 创建前台守护服务DaemonService
```
public class DaemonService extends Service {
    private static final String TAG = DaemonService.class.getSimpleName();
    public static final int NOTICE_ID = 100;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle("DataCollect");
            builder.setContentText("DataCollect runing...");
            startForeground(NOTICE_ID, builder.build());

            Intent intent = new Intent(this, CancelService.class);
            startService(intent);
        } else {
            startForeground(NOTICE_ID, new Notification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mManager.cancel(NOTICE_ID);
        }
        Intent intent = new Intent(getApplicationContext(), DaemonService.class);
        startService(intent);
    }
}
```
* 2.2 创建一个取消通知图标的服务CancelService，让用户在前台无感知
```
public class CancelService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            startForeground(DaemonService.NOTICE_ID, builder.build());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(1000);
                    stopForeground(true);
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    manager.cancel(DaemonService.NOTICE_ID);
                    stopSelf();
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
```
* 2.3 服务的注册
```
<service android:name=".alive.notifiy.DaemonService"
                 android:enabled="true"
                 android:exported="true"
                 android:process=":daemon_service"/>


<service android:name=".alive.notifiy.CancelService"
                 android:enabled="true"
                 android:exported="true"
                 android:process=":service"/>
```
* 2.4 服务开启
```
  private void startDaemonService() {
        Intent intent = new Intent(this, DaemonService.class);
        startService(intent);
      }
```
## 3.通过1像素Activity保活
* 3.1 创建1像素透明Activity
```
public class SinglePixelActivity extends AppCompatActivity {
    private static final String TAG = "SinglePixelActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window mWindow = getWindow();
        mWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams attrParams = mWindow.getAttributes();
        attrParams.x = 0;
        attrParams.y = 0;
        attrParams.height = 300;
        attrParams.width = 300;
        mWindow.setAttributes(attrParams);
        // 绑定SinglePixelActivity到ScreenManager
        ScreenManager.getScreenManagerInstance(this).setSingleActivity(this);
    }

    @Override
    protected void onDestroy() {
        if(! SystemUtils.isAPPALive(this,"com.geduo.datacollect")){
            Intent intentAlive = new Intent(this, MainActivity.class);
            intentAlive.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentAlive);
        }
        super.onDestroy();
    }
}
```
* 3.2 创建屏幕锁屏、开屏监听工具类
```
public class ScreenReceiverUtil {
    private Context mContext;
    // 锁屏广播接收器
    private SreenBroadcastReceiver mScreenReceiver;
    // 屏幕状态改变回调接口
    private SreenStateListener mStateReceiverListener;

    public ScreenReceiverUtil(Context mContext){
        this.mContext = mContext;
    }
    public void setScreenReceiverListener(SreenStateListener mStateReceiverListener){
        mStateReceiverListener = mStateReceiverListener;
        // 动态启动广播接收器
        mScreenReceiver = new SreenBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mScreenReceiver,filter);
    }


    public void stopScreenReceiverListener(){
        mContext.unregisterReceiver(mScreenReceiver);
    }


    public  class SreenBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(mStateReceiverListener == null){
                return;
            }
            if(Intent.ACTION_SCREEN_ON.equals(action)){         // 开屏
                mStateReceiverListener.onSreenOn();
            }else if(Intent.ACTION_SCREEN_OFF.equals(action)){  // 锁屏
                mStateReceiverListener.onSreenOff();
            }else if(Intent.ACTION_USER_PRESENT.equals(action)){ // 解锁
                mStateReceiverListener.onUserPresent();
            }
        }
    }
    // 监听sreen状态对外回调接口
    public interface SreenStateListener {
        void onSreenOn();
        void onSreenOff();
        void onUserPresent();
    }
}
```
## 4.双服务保活
* 4.1 创建主服务MainService
```
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
```
* 4.2 创建守护服务
```
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
        Log.v("MYTAG", "remote onDestroy start...");
    }
}
```
* 4.3 注册服务
```
<service
            android:name=".alive.doubles.two.MainService"
            android:enabled="true"
            android:exported="true"
            />
        <service
            android:name=".alive.doubles.two.RemoteService"
            android:enabled="true"
            android:exported="true"
            android:process=":remote"
            />
```
* 4.4 调用服务
```
startService (new Intent (this, MainService.class));
startService (new Intent (this, RemoteService.class));
```
## 5.定时服务JobService保活
* 5.1 创建定时服务AliveJobService
```
@TargetApi(21)
public class AliveJobService extends JobService {
    private final static String TAG = AliveJobService.class.getSimpleName();
    private volatile static Service mKeepAliveService = null;
    public static boolean isJobServiceAlive() {
        return mKeepAliveService != null;
    }

    private static final int MESSAGE_ID_TASK = 0x01;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (SystemUtils.isAPPALive(getApplicationContext(), "com.geduo.datacollect")) {
                Toast.makeText(getApplicationContext(), "app alive", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "app die", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AliveJobService.this,MainActivity.class));
            }
            jobFinished((JobParameters) msg.obj, false);
            return true;
        }
    });

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob start...");
        mKeepAliveService = this;
        Message msg = Message.obtain(mHandler, MESSAGE_ID_TASK, params);
        mHandler.sendMessage(msg);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob start...");
        mHandler.removeMessages(MESSAGE_ID_TASK);
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy start...");
    }

}
```
* 5.2 创建JobSchedulerManager管理定时服务
```
public class JobSchedulerManager {
    private static final int JOB_ID = 1;
    private static JobSchedulerManager mJobManager;
    private JobScheduler mJobScheduler;
    private static Context mContext;

    private JobSchedulerManager(Context ctxt){
        this.mContext = ctxt;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mJobScheduler = (JobScheduler)ctxt.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
    }

    public final static JobSchedulerManager getJobSchedulerInstance(Context ctxt){
        if(mJobManager == null){
            mJobManager = new JobSchedulerManager(ctxt);
        }
        return mJobManager;
    }

    @SuppressLint("MissingPermission")
    public void startJobScheduler(){
        // 如果JobService已经启动或API<21，返回
        if(AliveJobService.isJobServiceAlive() || isBelowLOLLIPOP()){
            return;
        }
        // 构建JobInfo对象，传递给JobSchedulerService
        int id = JOB_ID;
        mJobScheduler.cancel(id);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,new ComponentName(mContext, AliveJobService.class));
        if (Build.VERSION.SDK_INT >= 24) {
            builder.setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS); //执行的最小延迟时间
            builder.setOverrideDeadline(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);  //执行的最长延时时间
            builder.setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);
            builder.setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS, JobInfo.BACKOFF_POLICY_LINEAR);//线性重试方案
        } else {
            builder.setPeriodic(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);
        }
        builder.setPersisted(true);  // 设置设备重启时，执行该任务
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setRequiresCharging(false); // 当插入充电器，执行该任务
        JobInfo info = builder.build();
        mJobScheduler.schedule(info); //开始定时执行该系统任务

   }

    @TargetApi(21)
    public void stopJobScheduler(){
        if(isBelowLOLLIPOP())
            return;
        mJobScheduler.cancelAll();
    }

    private boolean isBelowLOLLIPOP(){
        // API< 21
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }
}
```

## 6.流氓手段通过无线循环播放一个无声音乐来保活
* 6.1 创建服务PlayerMusicService
```
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
```
* 6.2 注册服务
```
   <service android:name=".alive.music.PlayerMusicService"
                 android:enabled="true"
                 android:exported="true"
                 android:process=":music_service"/>

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

