package com.geduo.datacollect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * Description: <SplashActivity><br>
 *<li>过渡页面，通过他开启采集页面，用来测试采集服务是否被杀死</li></h1>
 * Author:      gxl<br>
 * Date:        2018/12/7<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        });
    }
}
