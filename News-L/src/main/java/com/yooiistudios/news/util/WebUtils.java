package com.yooiistudios.news.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import com.yooiistudios.news.R;

import java.util.List;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 *
 * NLWebUtils
 *  특정 링크의 기사를 웹으로 복사, 공유 등 여러 가지 행동을 하는 클래스
 */
public class WebUtils {
    private WebUtils() { throw new AssertionError("MUST not create this class!"); }

    public static void openLinkInBrowser(Context context, String linkUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
        context.startActivity(intent);
    }

    public static void shareLink(Activity activity, String linkUrl) {
        Context context = activity.getApplicationContext();
        String title = context.getString(R.string.news_detail_share_message);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);

        intent.putExtra(Intent.EXTRA_TEXT, linkUrl);

        // createChooser Intent
        Intent createChooser = Intent.createChooser(intent, context.getString(R.string.share));

        // PendingIntent 가 완벽한 해법
        // (가로 모드에서 설정으로 와서 친구 추천하기를 누를 때 계속 반복 호출되는 상황을 막기 위함)
        PendingIntent pendingIntent =
                PendingIntent.getActivity(activity, 0, createChooser, 0);

        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public static void copyLink(Context context, String linkUrl) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", linkUrl);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, R.string.news_detail_link_copied, Toast.LENGTH_SHORT).show();
    }

    public static void shareLinkToFacebook(Context context, String linkUrl) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        // intent.putExtra(Intent.EXTRA_SUBJECT, "Foo bar"); // NB: has no effect!
        intent.putExtra(Intent.EXTRA_TEXT, linkUrl);

        // See if official Facebook app is found
        boolean facebookAppFound = false;
        List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                intent.setPackage(info.activityInfo.packageName);
                facebookAppFound = true;
                break;
            }
        }

        // As fallback, launch sharer.php in a browser
        if (!facebookAppFound) {
            String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + linkUrl;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
        }

        context.startActivity(intent);
    }
}
