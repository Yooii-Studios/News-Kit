package com.yooiistudios.news.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 20.
 *
 * AppInfo
 *  앱의 일반적인 정보를 제공하는 클래스
 */
public class AppInfo {
    public static final int INVALID_VERSION_CODE = -1;

    public static int getVersionCode(Context context) {
        PackageInfo pInfo;
        try {
            if (context.getPackageManager() != null) {
                pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                return pInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return INVALID_VERSION_CODE;
    }

    public static String getVersionName(Context context) {
        PackageInfo pInfo;
        try {
            if (context.getPackageManager() != null) {
                pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                return pInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }
}
