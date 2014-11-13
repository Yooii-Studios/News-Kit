package com.yooiistudios.news.service;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yooiistudios.news.model.BackgroundCacheUtils;
import com.yooiistudios.news.model.BackgroundServiceUtils;
import com.yooiistudios.news.util.ConnectivityUtils;
import com.yooiistudios.news.util.NLLog;

import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 6.
 *
 * BackgroundCacheJobService
 *  롤리팝 이전 버전용 백그라운드 캐시 서비스
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BackgroundCacheJobService extends JobService {

    private LinkedList<JobParameters> mJobParamsMap;

    public BackgroundCacheJobService() {
        mJobParamsMap = new LinkedList<JobParameters>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        mJobParamsMap.add(params);

        Context context = getApplicationContext();

        if (!ConnectivityUtils.isWifiAvailable(getApplicationContext())) {
            saveMessage(context, "Wifi unavailable.");
            jobFinished(params, false);
            return true;
        }
//        int jobId = params.getJobId();
//        int jobType = BackgroundServiceUtils.getJobType(jobId);
//        String message = "job type : " + jobType
//                + ", cache time : " + BackgroundServiceUtils.getCacheTime(jobId);

        NLLog.i("BackgroundServiceUtils", "onStartJob.");

//        Context context = getApplicationContext();
//
//        switch (jobType) {
//            case BackgroundServiceUtils.JOB_LATENCY:
//                BackgroundServiceUtils.rescheduleAfterLollipop(
//                        context, jobId, BackgroundServiceUtils.JOB_PERIODIC);
//                break;
//            case BackgroundServiceUtils.JOB_PERIODIC:
//                break;
//            default:
//                // Do Nothing, just end current job.
//                jobFinished(params, false);
//                return true;
//        }

        if (BackgroundServiceUtils.isTimeToCache(getApplicationContext())) {
            NLLog.i("BackgroundServiceUtils", "Time to cache.");
            saveMessage(context, "Time to cache.");
//            jobFinished(params, false);
            BackgroundCacheUtils.getInstance().cache(getApplicationContext(),
                    new BackgroundCacheUtils.OnCacheDoneListener() {
                        @Override
                        public void onDone() {
                            jobFinished(params, false);
                            saveMessage(getApplicationContext(), "Cache done.");
                        }
                    });
        } else {
            NLLog.i("BackgroundServiceUtils", "Not the time to cache.");
//            saveMessage(context, "Not the time to cache.");
            jobFinished(params, false);
        }


        return true;
    }

    private static void saveMessage(Context context, String message) {
        SharedPreferences sharedPreferences
                = context.getSharedPreferences("DEBUG_SERVICE", Context.MODE_PRIVATE);
        String prevMessage = sharedPreferences.getString("message", "");

        message = prevMessage + new Date(System.currentTimeMillis()).toString() + "\n\n" + message + "\n\n\n\n";

        sharedPreferences.edit().putString("message", message).apply();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobParamsMap.remove(params);

        return true;
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
}
