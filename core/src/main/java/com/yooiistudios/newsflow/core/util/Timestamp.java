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

//    public static void start() {
//        start(getStackTractTag());
//    }

    public static void start(String tag) {
        sStartTimes.put(tag, now());
    }

//    public static void end() {
//        end(getStackTractTag());
//    }

    public static void end(String tag) {
//        String tag = getStackTractTag();
        if (sStartTimes.containsKey(tag)) {
            NLLog.i(TAG, tag + ": " + getTimeTaken(tag));
            sStartTimes.remove(tag);
        }
    }

    private static String getStackTractTag() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StringBuilder builder = new StringBuilder();

        // 1: dalvik.system.VMStack.getThreadStackTrace
        // 2: java.lang.Thread.getStackTrace
        // 3: getStackTractTag
        // 4. start() or end()
        final int startIndex = 4;
        for (int i = startIndex ; i < elements.length; i++) {
            // android.app.Activity.performCreate
            // android.app.Instrumentation.callActivityOnCreate
            // android.app.ActivityThread.performLaunchActivity
            // android.app.ActivityThread.handleLaunchActivity
            // android.app.ActivityThread.access$700
            // android.os.Handler.dispatchMessage
            // android.os.Looper.loop
            // java.lang.reflect.Method.invokeNative
            // java.lang.reflect.Method.invoke
            // com.android.internal.os
            // com.android.internal.os.ZygoteInit.main
            // dalvik.system.NativeStart.main
            StackTraceElement element = elements[i];

            builder.append("[")
                    .append(element.getClassName())
                    .append(".")
                    .append(element.getMethodName())
                    .append("]");
            if (i == startIndex) {
                builder.append("\ndetail:\n");
            }
            if (i < elements.length - 1) {
                builder.append("->");
            }
        }
        return builder.toString();
    }

    private static long getTimeTaken(String tag) {
        long startMilli = sStartTimes.get(tag);
        return now() - startMilli;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
