package com.yooiistudios.news.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.yooiistudios.news.model.BackgroundServiceUtils;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 6.
 *
 * BootUpReceiver
 *  디바이스가 부팅될 경우 알림 받는 리시버
 */
public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                BackgroundServiceUtils.startService(context);
            }
        }
    }
}
