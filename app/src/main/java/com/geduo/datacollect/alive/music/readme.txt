1.注册服务
      <service android:name=".alive.music.PlayerMusicService"
                 android:enabled="true"
                 android:exported="true"
                 android:process=":music_service"/>
2.开始、停止
    private void stopPlayMusicService() {
		Intent intent = new Intent(this, PlayerMusicService.class);
		stopService(intent);
	}

	private void startPlayMusicService() {
		Intent intent = new Intent(this,PlayerMusicService.class);
		startService(intent);
	}