package com.geduo.datacollect.alive.join;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Description: <><br>
 * Author:      gxl<br>
 * Date:        2019/1/2<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */


public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        Intent mIntent = new Intent(context,StepService.class);
        context.startService(mIntent);
    }
}

