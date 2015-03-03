package com.yooiistudios.newsflow.service;

import android.app.IntentService;
import android.content.Intent;

import com.yooiistudios.newsflow.model.BackgroundCacheUtils;
import com.yooiistudios.newsflow.model.BackgroundServiceUtils;
import com.yooiistudios.newsflow.util.ConnectivityUtils;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 6.
 *
 * BackgroundCacheIntentService
 *  롤리팝 이전 버전용 백그라운드 캐시 서비스
 */
public class BackgroundCacheIntentService extends IntentService
        implements BackgroundCacheUtils.OnCacheDoneListener {

    private static final String NAME = "TaskIntentService";

    public BackgroundCacheIntentService() {
        super(NAME);
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BackgroundCacheIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // intent.getExtras().getString("url")
        if (!ConnectivityUtils.isWifiAvailable(getApplicationContext())) {
            BackgroundServiceUtils.saveMessageAndPrintLogDebug(getApplicationContext(), "Wifi unavailable.");
            return;
        }
        BackgroundServiceUtils.saveMessageAndPrintLogDebug(getApplicationContext(), "Start caching.");
        BackgroundCacheUtils.getInstance().cache(getApplicationContext(), this);

//        int uniqueKey = intent.getExtras().getInt(BackgroundServiceUtils.KEY_CACHE_TIME_ID);
//        BackgroundServiceUtils.CacheTime cacheTime = BackgroundServiceUtils.CacheTime.getByUniqueKey(uniqueKey);
//        NLLog.i("BackgroundServiceUtils", "onHandleIntent : " + cacheTime.name());
    }

    @Override
    public void onDone() {
        BackgroundServiceUtils.saveMessageAndPrintLogDebug(getApplicationContext(), "Cache done.");
    }
}
