package com.yooiistudios.newskit.tv.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.yooiistudios.newskit.tv.model.ui.fragment.MainFragment;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * DebugSharedPreferencesUtil
 *  디버그 모드에서 사용될 옵션 저장, 불러오기에 사용
 */
public class DebugSharedPreferencesUtil {
    private static final String SP_NAME = "debug_shared_preferences";
    private static final String SP_KEY_DETAIL_ACTIVITY_CONTENT = "sp_key_detail_activity_content";

    private DebugSharedPreferencesUtil() {
        throw new AssertionError("You MUST NOT create the instance of this class!!");
    }

    public static String getDetailActivityMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return prefs.getString(SP_KEY_DETAIL_ACTIVITY_CONTENT, MainFragment.DETAIL_REFINED_CONTENT);
    }

    public static void toggleDetailActivityMode(Context context) {
        String mode;
        String previousMode = getDetailActivityMode(context);
        if (previousMode.equals(MainFragment.DETAIL_REFINED_CONTENT)) {
            mode = MainFragment.DETAIL_WEB_CONTENT;
        } else {
            mode = MainFragment.DETAIL_REFINED_CONTENT;
        }
        context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit()
                .putString(SP_KEY_DETAIL_ACTIVITY_CONTENT, mode).apply();
    }
}
