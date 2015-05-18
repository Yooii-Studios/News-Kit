package com.yooiistudios.newskit.model;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yooiistudios.newskit.core.debug.DebugSettings;
import com.yooiistudios.newskit.core.news.util.NewsFeedArchiveUtils;
import com.yooiistudios.newskit.core.util.NLLog;
import com.yooiistudios.newskit.service.BackgroundCacheIntentService;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * BackgroundServiceUtils
 *  백그라운드 캐싱시 필요한 서비스 실행, 취소 로직 래핑한 클래스
 */
public class BackgroundServiceUtils {
    public static final long CACHE_INTERVAL_DAILY = 24 * DateUtils.HOUR_IN_MILLIS;
    private static final String TAG = BackgroundServiceUtils.class.getName();
    public static final String KEY_CACHE_TIME_ID = "KEY_CACHE_TIME_ID";

    public static final boolean DEBUG = false;
//    public static final boolean DEBUG = true;

//    public static final int JOB_PERIODIC = 0;
//    public static final int JOB_LATENCY = 1;
//
//    @IntDef({
//            JOB_PERIODIC,
//            JOB_LATENCY,
//    })
//    @Retention(RetentionPolicy.SOURCE)
//    public @interface JobName {}


    /**
     * 기획이 바껴 캐시 타임이 바뀐 경우
     * 새로 추가된 시간 정보를 밑에 붙이고 uniqueKey 를 증가하게 붙인다.
     * 서비스를 등록할 때에는 현재 있는 시간정보 중 가장 낮은 uniqueKey 를 기준으로
     * 그 보다 낮은 uniqueKey 로 등록된 스케쥴을 cancel 하도록 한다.
     */
    public enum CacheTime {
//        FOUR_THIRTY_AM  (0,  4, 30),
//        ELEVEN_AM       (1, 11,  0),
//        FOUR_PM         (2, 16,  0),
        MIDNIGHT        ( 3,  0,  0),
        SIX_AM          ( 4,  6,  0),
        NOON            ( 5, 12,  0),
        SIX_PM          ( 6, 18,  0);
        /*
        ,
        TEST1           ( 7, 14, 30),
        TEST2           ( 8, 15,  0),
        TEST3           ( 9, 15, 30),
        TEST4           (10, 16,  0),
        TEST5           (11, 16, 30),
        TEST6           (12, 17,  0),
        TEST7           (13, 17,  30)

        isTimeToIssueNotification 가 true 를 반환하도록 해야함
         */

        public static final int INVALID_KEY = -1;
        private static final CacheTime DEFAULT = MIDNIGHT;

        private int uniqueKey;
        private int hour;
        private int minute;

        CacheTime(int uniqueKey, int hour, int minute) {
            this.uniqueKey = uniqueKey;
            this.hour = hour;
            this.minute = minute;
        }

        public static CacheTime getByUniqueKey(int uniqueKey) {
            for (CacheTime cacheTime : CacheTime.values()) {
                if (cacheTime.uniqueKey == uniqueKey) {
                    return cacheTime;
                }
            }

            return DEFAULT;
        }

        public static boolean isValidKey(int key) {
            CacheTime[] values = CacheTime.values();

            return values.length > 0 && key >= values[0].uniqueKey;
        }

        public static boolean isTimeToIssueNotification(CacheTime cacheTime) {
            return cacheTime.equals(SIX_AM);
        }
    }

    public static void startService(Context context) {
        // 특정 시간에 캐싱을 하는 현재 기획이 JobScheduler 에서 제공하는 api 와 맞지 않아
        // 디바이스를 무의미하게 깨우는 문제가 있어 기존의 롤리팝 로직을 폐기하고 전부 AlarmManager 를 사용하도록 함.
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startServiceAfterLollipop(context);
        } else {
        */
            // 1. 앱 초기 구동시
            // 2. 재부팅시
            // 위 두 상황에서만 알람을 등록하면 된다.
            // 아래는 위 1번 상황에 대응하기 위한 내용이다.

            // 알람 메니저는 등록된 알람의 조회에 대한 API를 제공하지 않음.
            // 대신, 특정 PendingIntent가 예약되어 있는지는 확인할 수 있음.

            // 알람 등록 이전에 필요 없는 알람은 모두 cancel 하자.
            /**
             * @see CacheTime
             */
            int minUniqueKey = CacheTime.values()[0].uniqueKey;
            for (int cacheTimeUniqueKey = 0; cacheTimeUniqueKey < minUniqueKey; cacheTimeUniqueKey++) {
                if (isPendingIntentExists(context, cacheTimeUniqueKey)) {
                    NLLog.i(TAG, cacheTimeUniqueKey + "에 해당하는 PendingIntent cancel 함.");

                    // Cancel
                    PendingIntent pendingIntent = makePendingIntent(context, cacheTimeUniqueKey);
                    AlarmManager alarmManager =
                            (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
            for (CacheTime cacheTime : CacheTime.values()) {
                boolean pendingIntentExists = isPendingIntentExists(context, cacheTime);

                // Register alarm
                if (!pendingIntentExists) {
                    long delay = getDelay(cacheTime);
                    NLLog.i(TAG, "서비스를 알람 매니저에 등록함. Enum name : " + cacheTime.name());

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
                    alarmManager.setRepeating(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + delay,
                            CACHE_INTERVAL_DAILY,
                            makePendingIntent(context, cacheTime)
                    );
                } else {
                    NLLog.i(TAG, "서비스가 알람 매니저에 이미 등록되어 있음. Enum name : " + cacheTime.name());
                }
            }
        /*
        }
        */
    }

    /*
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void startServiceAfterLollipop(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        ComponentName componentName = new ComponentName(context, BackgroundCacheJobService.class);

        // Version code 5부터 스케쥴링 정책 바뀜.
//        if (previousVersionCode < 5) {
//            NLLog.i(TAG, "앱 버전이 업데이트됨. JobScheduler 를 cancel 함.");
//            cancelJobSchedulerAfterLollipop(context);
//        }
        if (jobScheduler.getAllPendingJobs().size() == 0) {
            Intent jobServiceIntent = new Intent(context, BackgroundCacheJobService.class);
            context.startService(jobServiceIntent);

            String debugMessage = "서비스를 JobScheduler 에 등록함.";
            NLLog.i(TAG, debugMessage);

            // build job info
            JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(0, componentName);
            jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            if (DEBUG) {
                // 30초
                jobInfoBuilder.setPeriodic(30 * DateUtils.SECOND_IN_MILLIS);
            } else {
                // 20분
                jobInfoBuilder.setPeriodic(20 * DateUtils.MINUTE_IN_MILLIS);
            }
            jobInfoBuilder.setPersisted(true);
            jobScheduler.schedule(jobInfoBuilder.build());
        } else {
            String debugMessage = "서비스가 JobScheduler 에 이미 등록되어 있음.";
            NLLog.i(TAG, debugMessage);
        }
    }
    */

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

//    public static void cancelAllServices(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            cancelAllServicesAfterLollipop(context);
//        } else {
//            PendingIntent pendingIntent = makePendingIntent(context);
//            AlarmManager alarmManager =
//                    (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
//            alarmManager.cancel(pendingIntent);
//            pendingIntent.cancel();
//        }
//    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private static void cancelJobSchedulerAfterLollipop(Context context) {
//        JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        tm.cancelAll();
//    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public static void cancelServiceAfterLollipop(Context context, int jobId) {
//        JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        tm.cancel(jobId);
//    }

    private static PendingIntent makePendingIntent(Context context, CacheTime cacheTime) {
        return makePendingIntent(context, cacheTime.uniqueKey);
    }

    private static PendingIntent makePendingIntent(Context context, int cacheTimeUniqueKey) {
        return PendingIntent.getService(context, cacheTimeUniqueKey,
                makeServiceIntent(context, cacheTimeUniqueKey), 0);
    }

    private static boolean isPendingIntentExists(Context context, CacheTime cacheTime) {
        return isPendingIntentExists(context, cacheTime.uniqueKey);
    }

    private static boolean isPendingIntentExists(Context context, int cacheTimeUniqueKey) {
        return PendingIntent.getService(context, cacheTimeUniqueKey,
                makeServiceIntent(context, cacheTimeUniqueKey), PendingIntent.FLAG_NO_CREATE) != null;
    }

    private static Intent makeServiceIntent(Context context, CacheTime cacheTime) {
        return makeServiceIntent(context, cacheTime.uniqueKey);
    }

    private static Intent makeServiceIntent(Context context, int cacheTimeUniqueKey) {
        Intent intent = new Intent(context, BackgroundCacheIntentService.class);
        intent.putExtra(KEY_CACHE_TIME_ID, cacheTimeUniqueKey);
        return intent;
    }

    private static long getDelay(CacheTime cacheTime) {
        return getNextAlarmCalendar(cacheTime.hour, cacheTime.minute).getTimeInMillis()
                - Calendar.getInstance().getTimeInMillis();
    }

    private static Calendar getNextAlarmCalendar(int hour, int minute) {
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(Calendar.HOUR_OF_DAY, hour);
        targetCalendar.set(Calendar.MINUTE, minute);
        targetCalendar.set(Calendar.SECOND, 0);
        targetCalendar.set(Calendar.MILLISECOND, 0);

        if (targetCalendar.before(Calendar.getInstance())) {
            targetCalendar.add(Calendar.DATE, 1);
        }

        return targetCalendar;
    }

    public static int getCacheTimeKeyIfNecessary(Context context) {
        Calendar currentCalendar = Calendar.getInstance();

        long recentRefreshMillisec = NewsFeedArchiveUtils.getRecentRefreshMillisec(context);
        Calendar recentCacheCalendar = Calendar.getInstance();
        recentCacheCalendar.setTimeInMillis(recentRefreshMillisec);

        int cacheTimeKey = CacheTime.INVALID_KEY;
        for (CacheTime cacheTime : CacheTime.values()) {
            Calendar toCalendar = Calendar.getInstance();
            toCalendar.set(Calendar.HOUR_OF_DAY, cacheTime.hour);
            toCalendar.set(Calendar.MINUTE, cacheTime.minute);
            toCalendar.set(Calendar.SECOND, 0);
            toCalendar.set(Calendar.MILLISECOND, 0);

            Calendar fromCalendar = (Calendar)toCalendar.clone();
            // 30분 전 달력 만들어 현재 시간이 목표시간에서 그 30분 전 사이에 들어가는지 체크
            if (DEBUG) {
                fromCalendar.add(Calendar.SECOND, -30);
            } else {
                fromCalendar.add(Calendar.MINUTE, -30);
            }

            if (compareCalendar(currentCalendar, fromCalendar, toCalendar)) {
                // 해당 범위(-30 ~ 해당 시간) 안에서 캐시한 적이 있으면 캐시 안함.
                if (!compareCalendar(recentCacheCalendar, fromCalendar, toCalendar)) {
                    cacheTimeKey = cacheTime.uniqueKey;
                }
            }
        }

        return cacheTimeKey;
    }

    public static void saveMessageAndPrintLogDebug(Context context, String message) {
        if (DebugSettings.debugLog()) {
            if (DEBUG) {
                NLLog.i(TAG, message);
            }
            saveMessage(context, message);
        }
    }
    public static void saveMessage(Context context, String message) {
        SharedPreferences sharedPreferences
                = context.getSharedPreferences("DEBUG_SERVICE", Context.MODE_PRIVATE);
        String prevMessage = sharedPreferences.getString("message", "");

        String newMessage = " [ " + new Date(System.currentTimeMillis()).toString() + "\n\n"
                + message + " ]\n\n\n\n";

        message = prevMessage + newMessage;

        sharedPreferences.edit().putString("message", message).apply();
    }

    public static void showDialog(Context context) {
        SharedPreferences sharedPreferences
                = context.getSharedPreferences("DEBUG_SERVICE", Context.MODE_PRIVATE);
        String message = sharedPreferences.getString("message", "");

        TextView logView = new TextView(context);
        logView.setText(message);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
        );
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        ScrollView scrollContainer = new ScrollView(context);
        scrollContainer.addView(logView, lp);

        new AlertDialog.Builder(context)
                .setTitle("Service log")
                .setView(scrollContainer)
                .setNeutralButton("dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private static boolean compareCalendar(Calendar calToCompare, Calendar fromCal, Calendar toCal) {
        return calToCompare.after(fromCal) && calToCompare.before(toCal);
    }
}
