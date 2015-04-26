package com.yooiistudios.newskit.core.util;

import android.util.Log;

import com.yooiistudios.newskit.core.debug.DebugSettings;

/**
 * Created by StevenKim in MorningKit from Yooii Studios Co., LTD. on 2014. 2. 18.
 *
 * NLLog
 *  앱의 전체적인 로그를 관리. 출시 시 끌 수 있음
 */
public class NLLog {
    private NLLog() { throw new AssertionError("You MUST not create this class!"); }
//    private static final boolean mIsDebug = true;

    public static void now(String message) {
        if (DebugSettings.debugLog()) {
            Log.i("NLLog", message);
        }
    }

    public static void i(String TAG, String message) {
        if (DebugSettings.debugLog()) {
            Log.i(TAG, message);
        }
    }

    public static void d(String TAG, String message) {
        if (DebugSettings.debugLog()) {
            Log.d(TAG, message);
        }
    }

    public static void e(String TAG, String message) {
        if (DebugSettings.debugLog()) {
            Log.e(TAG, message);
        }
    }
}
