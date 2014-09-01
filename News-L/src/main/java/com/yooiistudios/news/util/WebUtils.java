package com.yooiistudios.news.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 *
 * NLWebUtils
 *  특정 링크의 기사를 웹으로 보여주는 클래스
 */
public class WebUtils {
    private WebUtils() { throw new AssertionError("MUST not create this class!"); }

    public static void openLink(Context context, String linkUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
        context.startActivity(intent);
    }
}
