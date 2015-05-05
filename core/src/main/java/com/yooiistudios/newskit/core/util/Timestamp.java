package com.yooiistudios.newskit.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 26.
 *
 * Timestamp
 *  특정 태그가 시작하고 끝난 시점을 체크하는 유틸
 */
public class Timestamp {
    public static final String TAG = "NL" + Timestamp.class.getSimpleName();
    private static final Map<String, Long> sStartTimes = new HashMap<>();

    public static void start(String tag) {
        sStartTimes.put(tag, now());
    }

    public static void end(String tag) {
        if (sStartTimes.containsKey(tag)) {
            NLLog.i(TAG, tag + ": " + getTimeTaken(tag));
            sStartTimes.remove(tag);
        }
    }

    private static long getTimeTaken(String tag) {
        long startMilli = sStartTimes.get(tag);
        return now() - startMilli;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
