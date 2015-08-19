package com.yooiistudios.newskit.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.yooiistudios.newskit.IabInfo;
import com.yooiistudios.newskit.iab.IabProducts;

/**
 * Created by StevenKim in Morning Kit from Yooii Studios Co., LTD. on 2014. 1. 28.
 *
 * MNReviewApp
 */
public class ReviewUtils {
    public static int REQ_REVIEW_APP = 4444;

    public static void showReviewActivity(Context context) {
        Uri uri = Uri.parse(getLink(context));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            ((Activity)context).startActivityForResult(goToMarket, REQ_REVIEW_APP);
//            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Couldn't launch the market", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getLink(Context context) {
        if (IabInfo.STORE_TYPE == IabProducts.StoreType.GOOGLE) {
            return "market://details?id=" + context.getPackageName();
        } else {
            return "http://m.nstore.naver.com/appstore/web/detail.nhn?pkgName=" + context.getPackageName();
        }
    }
}
