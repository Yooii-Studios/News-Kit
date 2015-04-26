package com.yooiistudios.newskit.core.util;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 4. 2.
 *
 * MethodCallPrinter
 *  메소드의 call hierarchy 를 프린트함
 */
public class MethodHierarchy {
    // 0: dalvik.system.VMStack.getThreadStackTrace
    // 1: java.lang.Thread.getStackTrace
    // 2: 현재 메서드(ex. get)
    // 3: 현재 메서드를 부르는 메서드(로그를 찍는 메서드)
    private static final int DEFAULT_FROM_DEPTH = 4;

    public static String get() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        return get(DEFAULT_FROM_DEPTH, elements.length);
    }

    public static String get(int depthCount) {
        return get(DEFAULT_FROM_DEPTH, DEFAULT_FROM_DEPTH + depthCount);
    }


    // TODO: 아래의 리스트들은 system 에서 불리는 메소드로, 제거할 수 있으면 하자
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
    public static String get(int fromDepth, int toDepth) {
        checkIndex(fromDepth, toDepth);

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StringBuilder builder = new StringBuilder();
        for (int i = fromDepth ; i < toDepth; i++) {
            builder = appendElement(builder, elements[i]);
            if (isLastElement(toDepth, i)) {
                appendLineBreak(builder);
            }
        }
        return builder.toString();
    }

    private static StringBuilder appendElement(StringBuilder builder, StackTraceElement element) {
        return builder.append("[")
                .append(element.getClassName())
                .append(".")
                .append(element.getMethodName())
                .append("]");
    }

    private static boolean isLastElement(int toDepth, int i) {
        return i < toDepth - 1;
    }

    private static void appendLineBreak(StringBuilder builder) {
        builder.append("\n");
    }

    private static void checkIndex(int startIndex, int endIndex) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (startIndex < 0 || endIndex >= elements.length || startIndex >= endIndex) {
            throw new IndexOutOfBoundsException("");
        }
    }
}
