package com.yooiistudios.news.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

import com.yooiistudios.news.util.ConnectivityUtils;
import com.yooiistudios.news.util.NLLog;

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

        if (!ConnectivityUtils.isWifiAvailable(getApplicationContext())) {
            jobFinished(params, false);
            return true;
        }
        NLLog.i("qwerasdfzxcv", "onStartJob");

        // call this line after caching finished
        jobFinished(params, false);
//        new TestAsync(new TestAsync.OnFinishListener() {
//            @Override
//            public void onFinish(String result) {
//                SharedPrefsUtils.save(getApplicationContext(), result);
//                Log.i(MyActivity.TAG, "onFinish : " + result);
//                jobFinished(params, false);
//            }
//        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobParamsMap.remove(params);

        return true;
    }
}
