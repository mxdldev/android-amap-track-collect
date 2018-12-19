1.用法
startService(new Intent(MainActivity.this, PairServiceA.class));

2.注册
<service android:name=".alive.doubles.one.PairServiceA"/>
<service
    android:name=".alive.doubles.one.PairServiceB"
    android:process=":alive"/>