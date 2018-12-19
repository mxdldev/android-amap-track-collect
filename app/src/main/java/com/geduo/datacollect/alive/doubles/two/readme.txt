1.用法
  startService (new Intent (this, MainService.class));
  startService (new Intent (this, RemoteService.class));
2.注册服务
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
