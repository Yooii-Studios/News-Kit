package com.yooiistudios.news.service;

import android.app.IntentService;
import android.content.Intent;

import com.yooiistudios.news.util.ConnectivityUtils;
import com.yooiistudios.news.util.NLLog;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 6.
 *
 * BackgroundCacheIntentService
 *  롤리팝 이전 버전용 백그라운드 캐시 서비스
 */
public class BackgroundCacheIntentService extends IntentService {

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
            return;
        }
        NLLog.i("BackgroundServiceUtils", "onHandleIntent");
//        BackgroundCacheUtils.getInstance().cache(getApplicationContext());
    }
}
