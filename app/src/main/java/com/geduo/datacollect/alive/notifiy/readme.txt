1.服务注册
<service android:name=".alive.notifiy.DaemonService"
                 android:enabled="true"
                 android:exported="true"
                 android:process=":daemon_service"/>


<service android:name=".alive.notifiy.CancelService"
                 android:enabled="true"
                 android:exported="true"
                 android:process=":service"/>
2.开启、停止
      private void startDaemonService() {
        Intent intent = new Intent(this, DaemonService.class);
        startService(intent);
      }

      private void stopDaemonService() {
      	Intent intent = new Intent(this, DaemonService.class);
      	stopService(intent);
      }