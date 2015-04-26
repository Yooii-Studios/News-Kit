package com.yooiistudios.newskit.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 4. 13.
 *
 * AppLaunchCount
 *  단순하게 앱 실행횟수를 체크
 */
public class AppLaunchCount {
    private static final String APP_LAUNCH_SHARED_PREFERENCES = "app_launch_shared_preferences";
    private static final String LAUNCH_COUNT_KEY = "launch_count_key";

    public static boolean isFirstAppLaunch(Context context) {
        int launchCount = context.getSharedPreferences(APP_LAUNCH_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(LAUNCH_COUNT_KEY, 1);
        return launchCount == 1;
    }

    public static boolean isTimeToShowReviewRequestDialog(Context context) {
        int launchCount = context.getSharedPreferences(APP_LAUNCH_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(LAUNCH_COUNT_KEY, 1);

        boolean isTimeToShow = false;

        if (launchCount == 10 || launchCount == 40) {
            SharedPreferences prefs = context.getSharedPreferences(
                    ReviewRequest.REVIEW_REQUEST_PREFS, Context.MODE_PRIVATE);
            if (!prefs.getBoolean(ReviewRequest.KEY_REVIEWED, false)) {
                isTimeToShow = true;
            }
        }

        // 41회부터는 카운트할 것이 없으므로 더이상 카운트하지 않음
        if (launchCount <= 40) {
            context.getSharedPreferences(APP_LAUNCH_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                    .edit().putInt(LAUNCH_COUNT_KEY, launchCount + 1).apply();
        }

        return isTimeToShow;
    }
}
