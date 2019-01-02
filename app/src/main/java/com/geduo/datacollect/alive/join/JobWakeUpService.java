package com.geduo.datacollect.alive.join;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.List;

/**
 * Description: <><br>
 * Author:      gxl<br>
 * Date:        2019/1/2<br>
 * Version:     V1.0.0<br>
 * Update:     <br>
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)//5.0以后可用
public class JobWakeUpService extends JobService {
    private int JobWakeUpId = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //开启轮寻
        JobInfo.Builder mJobBulider = new JobInfo.Builder(JobWakeUpId, new ComponentName(this, JobWakeUpService.class));
        //设置轮寻时间
        mJobBulider.setPeriodic(2000);
        JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mJobScheduler.schedule(mJobBulider.build());
        return START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //开启定时任务 定时轮寻 判断应用Service是否被杀死
        //如果被杀死则重启Service
        boolean messageServiceAlive = serviceAlive(StepService.class.getName());
        if (!messageServiceAlive) {
            Log.v("MYTAG1","onStartJob start...");
            startService(new Intent(this, StepService.class));
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        return false;
    }

    /**
     * 判断某个服务是否正在运行的方法
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    private boolean serviceAlive(String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
