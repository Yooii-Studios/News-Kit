package com.yooiistudios.news.model;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.text.format.DateUtils;

import com.yooiistudios.news.service.BackgroundCacheIntentService;
import com.yooiistudios.news.service.BackgroundCacheJobService;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * BackgroundServiceUtils
 *  백그라운드 캐싱시 필요한 서비스 실행, 취소 로직 래핑한 클래스
 */
public class BackgroundServiceUtils {
    private static final long INTERVAL = 4 * DateUtils.HOUR_IN_MILLIS;

    private static PendingIntent sPendingIntent;

    public static void startService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startServiceAfterLollipop(context);
        } else {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
            alarmManager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(), INTERVAL, makePendingIntent(context)
            );
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("ResourceType")
    public static void startServiceAfterLollipop(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler.getAllPendingJobs().size() == 0) {
            Intent jobServiceIntent = new Intent(context, BackgroundCacheJobService.class);
//            jobServiceIntent.putExtra("url", "working...");
            context.startService(jobServiceIntent);

            ComponentName componentName = new ComponentName(context, BackgroundCacheJobService.class);

            // build job info
            JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(0, componentName);
            jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            jobInfoBuilder.setPeriodic(INTERVAL);
            jobInfoBuilder.setPersisted(true);
            jobScheduler.schedule(jobInfoBuilder.build());
        }
    }

    public static void cancelService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cancelServiceAfterLollipop(context);
        } else {
            AlarmManager alarmManager =
                    (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
            alarmManager.cancel(makePendingIntent(context));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("ResourceType")
    private static void cancelServiceAfterLollipop(Context context) {
        JobScheduler tm =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancelAll();
    }

    private static PendingIntent makePendingIntent(Context context) {
        if (sPendingIntent == null) {
            Intent serviceIntent = new Intent(context, BackgroundCacheIntentService.class);
//            serviceIntent.putExtra("url", "working...");

            sPendingIntent = PendingIntent.getService(context, 10004, serviceIntent, 0);
        }

        return sPendingIntent;
    }
}
