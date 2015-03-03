package com.yooiistudios.newsflow.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.yooiistudios.newsflow.model.BackgroundServiceUtils;
import com.yooiistudios.newsflow.util.NLLog;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 6.
 *
 * BootUpReceiver
 *  디바이스가 부팅될 경우 알림 받는 리시버
 */
public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // 시스템이 재부팅된 경우 알람을 새로 등록해야 한다.
                NLLog.i("BackgroundServiceUtils", "onReceive");
                BackgroundServiceUtils.startService(context);
            }
        }
//        else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
//            Toast.makeText(context, "Starting Caching...", Toast.LENGTH_LONG).show();
//            BackgroundCacheUtils.getInstance().cache(context, new BackgroundCacheUtils.OnCacheDoneListener() {
//                @Override
//                public void onDone() {
//                    Toast.makeText(context, "Cache Done...", Toast.LENGTH_LONG).show();
//                }
//            });
////            BackgroundServiceUtils.startService(context);
//        }
    }
}
