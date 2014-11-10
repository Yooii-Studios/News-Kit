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
import com.yooiistudios.news.util.NLLog;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * BackgroundServiceUtils
 *  백그라운드 캐싱시 필요한 서비스 실행, 취소 로직 래핑한 클래스
 */
public class BackgroundServiceUtils {
    public static final long CACHE_INTERVAL = 4 * DateUtils.HOUR_IN_MILLIS;
    private static final int RC_INTENT_SERVICE = 10004;
    private static final String TAG = BackgroundServiceUtils.class.getName();

    private static PendingIntent sPendingIntent;

    public static void startService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startServiceAfterLollipop(context);
        } else {
            // 1. 앱 초기 구동시
            // 2. 재부팅시
            // 위 두 상황에서만 알람을 등록하면 된다.
            // 아래는 위 1번 상황에 대응하기 위한 내용이다.

            // 알람 메니저는 등록된 알람의 조회에 대한 API를 제공하지 않음.
            // 대신, 특정 PendingIntent가 예약되어 있는지는 확인할 수 있음.
            boolean pendingIntentExists = isPendingIntentExists(context);
            NLLog.i(TAG, "PendingIntent : " + (pendingIntentExists? "Exists" : "Does not exists"));
            if (!pendingIntentExists) {
                NLLog.i(TAG, "서비스를 알람 매니저에 등록함");
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
                alarmManager.setRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + CACHE_INTERVAL,
                        CACHE_INTERVAL,
                        makePendingIntent(context)
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

            NLLog.i(TAG, "서비스를 JobScheduler에 등록함");
            // build job info
            JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(0, componentName);
            jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            jobInfoBuilder.setPeriodic(CACHE_INTERVAL);
            jobInfoBuilder.setPersisted(true);
            jobScheduler.schedule(jobInfoBuilder.build());
        } else {
            NLLog.i(TAG, "서비스가 JobScheduler에 이미 등록되어 있음");
        }
    }

    public static void cancelService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cancelServiceAfterLollipop(context);
        } else {
            PendingIntent pendingIntent = makePendingIntent(context);
            AlarmManager alarmManager =
                    (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
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
            sPendingIntent =
                    PendingIntent.getService(context, RC_INTENT_SERVICE, makeServiceIntent(context), 0);
        }

        return sPendingIntent;
    }

    private static boolean isPendingIntentExists(Context context) {
        return PendingIntent.getService(context, RC_INTENT_SERVICE, makeServiceIntent(context),
                        PendingIntent.FLAG_NO_CREATE) != null;
    }

    private static Intent makeServiceIntent(Context context) {
        return new Intent(context, BackgroundCacheIntentService.class);
    }
}
