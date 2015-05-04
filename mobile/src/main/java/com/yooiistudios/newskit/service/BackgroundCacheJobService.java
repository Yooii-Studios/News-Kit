package com.yooiistudios.newskit.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.yooiistudios.newskit.core.util.ConnectivityUtils;
import com.yooiistudios.newskit.model.BackgroundCacheUtils;
import com.yooiistudios.newskit.model.BackgroundServiceUtils;

import java.util.LinkedList;

import static com.yooiistudios.newskit.model.BackgroundServiceUtils.CacheTime;

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
        mJobParamsMap = new LinkedList<>();
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
            BackgroundServiceUtils.saveMessageAndPrintLogDebug(context, "Wifi unavailable.");
            jobFinished(params, false);
            // 아래 코드에서 true 를 반환하면 시간이 많이 걸리는 일을 하겠다는 의미이므로
            // 만약 이 클래스를 다시 쓰게 된다면 false 로 바꿀지 생각해 봐야 함.
            return true;
        }

        int cacheTimeKey = BackgroundServiceUtils.getCacheTimeKeyIfNecessary(getApplicationContext());
        if (CacheTime.isValidKey(cacheTimeKey)) {
            CacheTime cacheTime = CacheTime.getByUniqueKey(cacheTimeKey);
            BackgroundServiceUtils.saveMessageAndPrintLogDebug(getApplicationContext(), "Start caching.");
            BackgroundCacheUtils.getInstance().cache(getApplicationContext(), cacheTime,
                    new BackgroundCacheUtils.OnCacheDoneListener() {
                        @Override
                        public void onDone() {
                            // jobFinished 는 sync 로 불러줘야 다시 불리는 일이 없다.
                            // 그러므로 이 코드는 sync 로 추출할 필요가 있다.
                            jobFinished(params, false);
                            BackgroundServiceUtils.saveMessageAndPrintLogDebug(getApplicationContext(), "Cache done.");
                        }
                    });
        } else {
            jobFinished(params, false);
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobParamsMap.remove(params);

        return true;
    }
}
