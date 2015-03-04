package com.yooiistudios.newsflow.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 6.
 *
 * FacebookUtils
 *  페이스북의 유이스튜디오 페이지 링크를 열어주는 유틸
 */
public class FacebookUtils {
    private static final String LINK_APP_PREFIX = "fb://profile/";
    private static final String FB_YOOII_ID = "652380814790935";

    private FacebookUtils() { throw new AssertionError("You can't create this class!"); }

    public static void openYooiiPage(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                packageManager.getPackageInfo("com.facebook.katana", 0);
            }
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LINK_APP_PREFIX + FB_YOOII_ID)));
        } catch (Exception e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/YooiiMooii")));
        }
    }
}
