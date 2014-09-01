package com.yooiistudios.news.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Wooseong Kim in MorningKit from Yooii Studios Co., LTD. on 2014. 4. 21.
 *
 * StoreDebugCheckUtils
 *  테스트용으로 만든 클래스, 디버그 모드에서 상점/비상점 체크가 가능하다
 *  출시 모드가 되었을 경우에는 스토어에서 버튼을 사라지게 만들어준다
 */
public class StoreDebugCheckUtils {
    private static final String PREFS = "MNStoreDebugChecker_PREFS";
    private static final String IS_USING_STORE = "MNStoreDebugChecker_IS_USING_STORE";
    private boolean isUsingStore;

    /**
     * Singleton
     */
    private volatile static StoreDebugCheckUtils instance;
    private StoreDebugCheckUtils() {}
    public static StoreDebugCheckUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (StoreDebugCheckUtils.class) {
                if (instance == null) {
                    instance = new StoreDebugCheckUtils();
                    SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                    instance.isUsingStore = prefs.getBoolean(IS_USING_STORE, true);
                }
            }
        }
        return instance;
    }

    public static boolean isUsingStore(Context context) {
        return getInstance(context).isUsingStore;
    }

    public static void setUsingStore(boolean isUsingStore, Context context) {
        getInstance(context).isUsingStore = isUsingStore;
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(IS_USING_STORE, isUsingStore).commit();

    }
}
