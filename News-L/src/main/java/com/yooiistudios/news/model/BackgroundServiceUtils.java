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
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;

import com.yooiistudios.news.service.BackgroundCacheIntentService;
import com.yooiistudios.news.service.BackgroundCacheJobService;
import com.yooiistudios.news.util.NLLog;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * BackgroundServiceUtils
 *  백그라운드 캐싱시 필요한 서비스 실행, 취소 로직 래핑한 클래스
 */
public class BackgroundServiceUtils {
    public static final long CACHE_INTERVAL = 3000;//4 * DateUtils.HOUR_IN_MILLIS;
    private static final String TAG = BackgroundServiceUtils.class.getName();
    public static final String SP_NAME_SERVICE = "SP_NAME_SERVICE";
    public static final String SP_KEY_ALARM_SET = "SP_KEY_ALARM_SET";

    private static PendingIntent sPendingIntent;

    public static void startService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startServiceAfterLollipop(context);
        } else {
            // 1. 앱 초기 구동시
            // 2. 재부팅시
            // 위 두 상황에서만 알람을 등록하면 된다.
            // 아래는 위 1번 상황에 대응하기 위한 내용이다.

            // 알람 메니저는 등록된 알람의 조회에 대한 API를 제공하지 않기 때문에,
            // 시스템 설정에서 앱 캐시 삭제시에는 shared preferences도 날아가기 떄문에
            // 새로 알람을 세팅할 수 밖에 없다.

            SharedPreferences prefs =
                    context.getSharedPreferences(SP_NAME_SERVICE, Context.MODE_PRIVATE);
            boolean hasAlarmSet = prefs.getBoolean(SP_KEY_ALARM_SET, false);
            if (!hasAlarmSet) {
                prefs.edit().putBoolean(SP_KEY_ALARM_SET, true).apply();


                NLLog.i(TAG, "서비스를 알람 매니저에 등록함");
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
                alarmManager.setRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime(), CACHE_INTERVAL, makePendingIntent(context)
                );
            } else {
                NLLog.i(TAG, "서비스가 알람 매니저에 이미 등록되어 있음");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("ResourceType")
    private static void startServiceAfterLollipop(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler.getAllPendingJobs().size() == 0) {
            Intent jobServiceIntent = new Intent(context, BackgroundCacheJobService.class);
//            jobServiceIntent.putExtra("url", "working...");
            context.startService(jobServiceIntent);

            ComponentName componentName = new ComponentName(context, BackgroundCacheJobService.class);

            // build job info
            JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(0, componentName);
            jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            jobInfoBuilder.setPeriodic(CACHE_INTERVAL);
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
