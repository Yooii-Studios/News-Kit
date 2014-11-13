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

import com.yooiistudios.news.model.news.NewsFeedArchiveUtils;
import com.yooiistudios.news.service.BackgroundCacheIntentService;
import com.yooiistudios.news.service.BackgroundCacheJobService;
import com.yooiistudios.news.util.NLLog;

import java.util.Calendar;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * BackgroundServiceUtils
 *  백그라운드 캐싱시 필요한 서비스 실행, 취소 로직 래핑한 클래스
 */
public class BackgroundServiceUtils {
    public static final long CACHE_INTERVAL_DAILY = 24 * DateUtils.HOUR_IN_MILLIS;
    private static final int RC_INTENT_SERVICE = 10004;
    private static final String TAG = BackgroundServiceUtils.class.getName();

//    public static final int JOB_PERIODIC = 0;
//    public static final int JOB_LATENCY = 1;
//
//    @IntDef({
//            JOB_PERIODIC,
//            JOB_LATENCY,
//    })
//    @Retention(RetentionPolicy.SOURCE)
//    public @interface JobName {}

    private enum CACHE_TIME {
        FOUR_THIRTY_AM(0, 4, 30),
        ELEVEN_AM(1, 11, 0),
        FOUR_PM(2, 16, 0);

        private int uniqueKey;
        private int hour;
        private int minute;

        private CACHE_TIME(int uniqueKey, int hour, int minute) {
            this.uniqueKey = uniqueKey;
            this.hour = hour;
            this.minute = minute;
        }

        public static CACHE_TIME getByUniqueKey(int uniqueKey) {
            for (CACHE_TIME cacheTime : CACHE_TIME.values()) {
                if (cacheTime.uniqueKey == uniqueKey) {
                    return cacheTime;
                }
            }

            return null;
        }
    }

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
                        SystemClock.elapsedRealtime() + CACHE_INTERVAL_DAILY,
                        CACHE_INTERVAL_DAILY,
                        makePendingIntent(context)
                );
            } else {
                NLLog.i(TAG, "서비스가 알람 매니저에 이미 등록되어 있음");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void startServiceAfterLollipop(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        ComponentName componentName = new ComponentName(context, BackgroundCacheJobService.class);

        if (jobScheduler.getAllPendingJobs().size() == 0) {
            Intent jobServiceIntent = new Intent(context, BackgroundCacheJobService.class);
            context.startService(jobServiceIntent);

            NLLog.i(TAG, "서비스를 JobScheduler에 등록함.");
            // build job info
            JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(0, componentName);
            jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            jobInfoBuilder.setPeriodic(20 * DateUtils.MINUTE_IN_MILLIS);
//            jobInfoBuilder.setPeriodic(5 * DateUtils.MINUTE_IN_MILLIS);
            jobInfoBuilder.setPersisted(true);
            jobScheduler.schedule(jobInfoBuilder.build());
        }
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public static void rescheduleAfterLollipop(Context context, int previousJobId, int jobType) {
//        cancelServiceAfterLollipop(context, previousJobId);
//
//        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        ComponentName componentName = new ComponentName(context, BackgroundCacheJobService.class);
//
//        CACHE_TIME cacheTime = getCacheTime(previousJobId);
//
//        NLLog.i(TAG, "서비스가 JobScheduler에 이미 등록되어 있음. 24시간 주기로 변경 : " + cacheTime.name());
//
//        // build job info
//        JobInfo.Builder jobInfoBuilder =
//                new JobInfo.Builder(getJobId(cacheTime, jobType), componentName);
//        jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
//        jobInfoBuilder.setPeriodic(DEBUG_CACHE_INTERVAL_DAILY);
//        jobInfoBuilder.setPersisted(true);
//        jobScheduler.schedule(jobInfoBuilder.build());
//    }

    public static void cancelAllServices(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cancelAllServicesAfterLollipop(context);
        } else {
            PendingIntent pendingIntent = makePendingIntent(context);
            AlarmManager alarmManager =
                    (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void cancelAllServicesAfterLollipop(Context context) {
        JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancelAll();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void cancelServiceAfterLollipop(Context context, int jobId) {
        JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancel(jobId);
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

//    private static long getDelay(CACHE_TIME cacheTime) {
//        return getAlarmCalendar(cacheTime.hour, cacheTime.minute).getTimeInMillis();
//    }
//
//    private static Calendar getAlarmCalendar(int hour, int minute) {
//        Calendar targetCalendar = Calendar.getInstance();
//        targetCalendar.set(Calendar.HOUR_OF_DAY, hour);
//        targetCalendar.set(Calendar.MINUTE, minute);
//        targetCalendar.set(Calendar.SECOND, 0);
//        targetCalendar.set(Calendar.MILLISECOND, 0);
//
//        if (targetCalendar.before(Calendar.getInstance())) {
//            targetCalendar.add(Calendar.DATE, 1);
//        }
//
//        return targetCalendar;
//    }

    public static boolean isTimeToCache(Context context) {
        Calendar currentCalendar = Calendar.getInstance();

        long recentRefreshMillisec = NewsFeedArchiveUtils.getRecentRefreshMillisec(context);
        Calendar recentCacheCalendar = Calendar.getInstance();
        recentCacheCalendar.setTimeInMillis(recentRefreshMillisec);

        for (CACHE_TIME cacheTime : CACHE_TIME.values()) {
            Calendar toCalendar = Calendar.getInstance();
            toCalendar.set(Calendar.HOUR_OF_DAY, cacheTime.hour);
            toCalendar.set(Calendar.MINUTE, cacheTime.minute);
            toCalendar.set(Calendar.SECOND, 0);
            toCalendar.set(Calendar.MILLISECOND, 0);

            Calendar fromCalendar = (Calendar)toCalendar.clone();
            // 20분 전 달력 만들어 현재 시간이 목표시간에서 그 20분 전 사이에 들어가는지 체크
            fromCalendar.add(Calendar.HOUR, -1);

            if (compareCalendar(currentCalendar, fromCalendar, toCalendar)) {

                // 해당 범위(-20 ~ 해당 시간) 안에서 캐시한 적이 있으면 캐시 안함.
                if (compareCalendar(recentCacheCalendar, fromCalendar, toCalendar)) {
                    NLLog.i(TAG, "Cache exists");
                    return false;
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean compareCalendar(Calendar calToCompare, Calendar fromCal, Calendar toCal) {
        return calToCompare.after(fromCal) && calToCompare.before(toCal);
    }
}
