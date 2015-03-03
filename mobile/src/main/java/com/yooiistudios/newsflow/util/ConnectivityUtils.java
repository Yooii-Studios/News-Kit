package com.yooiistudios.newsflow.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Dongheyon Jeong on in ServiceWithTaskTest from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * ConnectivityUtils
 *  인터넷 연결 상태를 확인하는 유틸 클래스
 */
public class ConnectivityUtils {
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean isWifiAvailable(Context context) {
        NetworkInfo info = ConnectivityUtils.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = ConnectivityUtils.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }
}
