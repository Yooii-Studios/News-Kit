package com.yooiistudios.newsflow.core.debug;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 26.
 *
 * DebugSettings
 *  개발자 세팅을 켜고 끌 수 있도록 함
 */
public class DebugSettings {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_LOG = true;
    private static final boolean DEBUG_STRICT_MODE = true;

    public static boolean isDebugBuild() {
        return isDebugInternal();
    }

    public static boolean debugLog() {
        return isDebugInternal() && isDebugLogInternal();
    }

    public static boolean debugStrictMode() {
        return isDebugInternal() && isDebugStrictModeInternal();
    }

    private static boolean isDebugInternal() {
        return DEBUG;
    }

    private static boolean isDebugLogInternal() {
        return DEBUG_LOG;
    }

    private static boolean isDebugStrictModeInternal() {
        return DEBUG_STRICT_MODE;
    }
}
