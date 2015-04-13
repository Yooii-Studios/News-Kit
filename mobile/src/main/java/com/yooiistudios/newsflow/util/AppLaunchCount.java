package com.yooiistudios.newsflow.util;

import android.content.Context;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 4. 13.
 *
 * AppLaunchCount
 *  단순하게 앱 실행횟수를 체크
 */
public class AppLaunchCount {
    private static final String APP_LAUCNH_SHARED_PREFERENCES = "app_laucnh_shared_preferences";
    private static final String LAUNCH_COUNT_KEY = "launch_count_key";

    public static boolean isFirstAppLaunch(Context context) {
        int launchCount = context.getSharedPreferences(APP_LAUCNH_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(LAUNCH_COUNT_KEY, 1);
        if (launchCount == 1) {
            context.getSharedPreferences(APP_LAUCNH_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                    .edit().putInt(LAUNCH_COUNT_KEY, launchCount + 1).apply();
            return true;
        } else {
            return false;
        }
    }
}
