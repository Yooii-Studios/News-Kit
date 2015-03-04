package com.yooiistudios.newsflow.core.util;

import android.os.Build;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 2. 19.
 *
 * Device
 *  안드로이드 버전을 체크할 수 있는 클래스
 */
public class Device {
    private Device() { throw new AssertionError("You MUST not create this class!"); }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
