package com.yooiistudios.newsflow.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.yooiistudios.newsflow.R;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 17.
 *
 * FeedbackUtils
 *  앱 피드백을 보내는 기능을 담은 유틸리티 클래스 */
public class FeedbackUtils {
    private FeedbackUtils() { throw new AssertionError("Must not create this class!"); }

    public static void sendFeedback(Activity activity) {
        Context context = activity.getApplicationContext();
        String appName = context.getString(R.string.app_name);
        String title = "[" + appName + "] User Feedback";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/email");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "yooiistudiosb@gmail.com" });
        intent.putExtra(Intent.EXTRA_SUBJECT, title);

        String version = "";
        try {
            if (context.getPackageManager() != null) {
                PackageInfo pInfo;
                pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                version = pInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String line = "-----------------------------------";
        String deviceInformation = line + "\n" + "Model: " + Build.BRAND + " " + Build.MODEL + "\n" +
                "OS: Android(SDK " + Build.VERSION.SDK_INT + ")\n" +
                "App Version: " + version + "\n" + line;
        String message = context.getString(R.string.feedback_description) + "\n\n\n\n\n" + deviceInformation;
        intent.putExtra(Intent.EXTRA_TEXT, message);
        activity.startActivity(intent);

        // createChooser Intent
//        Intent createChooser = Intent.createChooser(intent, context.getString(R.string.action_send_feedback));
//        activity.startActivity(createChooser);

        // PendingIntent 가 완벽한 해법
        // (가로 모드에서 설정으로 와서 친구 추천하기를 누를 때 계속 반복 호출되는 상황을 막기 위함)
//        PendingIntent pendingIntent =
//                PendingIntent.getActivity(activity, 0, createChooser, 0);
//        try {
//            pendingIntent.send();
//        } catch (PendingIntent.CanceledException e) {
//            e.printStackTrace();
//        }
    }
}
