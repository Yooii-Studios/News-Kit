package com.yooiistudios.newsflow.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 26.
 *
 * Timestamp
 *  특정 태그가 시작하고 끝난 시점을 체크하는 유틸
 */
public class Timestamp {
    private static final String TAG = Timestamp.class.getSimpleName();
    private static final Map<String, Long> sStartTimes = new HashMap<>();

    public static void start(String tag) {
        sStartTimes.put(tag, now());
    }

//    private static String getStackTractTag() {
//        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//        StringBuilder builder = new StringBuilder();
//        for (int i = 4 ; i < elements.length; i++) {
//            StackTraceElement element = elements[i];
//            if () {
//                break;
//            }
//            builder.append("[")
//                    .append(element.getClassName())
//                    .append(".")
//                    .append(element.getMethodName())
//                    .append("]");
//            if (i < elements.length - 1) {
//                builder.append("->");
//            }
//        }
//        return builder.toString();
//    }

    public static void end(String tag) {
//        String tag = getStackTractTag();
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
