package com.yooiistudios.newskit.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.newskit.R;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 4. 20.
 *
 * ReviewRequest
 *  리뷰 요청 다이얼로그
 */
public class ReviewRequest {
    public static final String REVIEW_REQUEST_PREFS= "review_request_prefs";
    public static final String KEY_REVIEWED = "key_reviewed";

    public static void showDialog(final Context context) {
        // Material Dialog 로 변경
        /*
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(context.getString(R.string.rate_news_kit_title));
        String appFullName = context.getString(R.string.app_name_full);
        String message = context.getString(R.string.rate_it_contents, appFullName);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.rate_it_rate, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences prefs = context.getSharedPreferences(
                        REVIEW_REQUEST_PREFS, Context.MODE_PRIVATE);
                prefs.edit().putBoolean(KEY_REVIEWED, true).apply();
                ReviewUtils.showReviewActivity(context);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.rate_it_no_thanks, null);
        AlertDialog reviewDialog = builder.create();
        reviewDialog.setCancelable(false);
        reviewDialog.setCanceledOnTouchOutside(false);
        reviewDialog.show();
        */

        String appFullName = context.getString(R.string.app_name_full);
        MaterialDialog materialDialog = new MaterialDialog.Builder(context)
                .title(R.string.rate_news_kit_title)
                .content(context.getString(R.string.rate_it_contents, appFullName))
                .positiveText(R.string.rate_it_rate)
                .negativeText(R.string.rate_it_no_thanks)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        SharedPreferences prefs = context.getSharedPreferences(
                                REVIEW_REQUEST_PREFS, Context.MODE_PRIVATE);
                        prefs.edit().putBoolean(KEY_REVIEWED, true).apply();
                        ReviewUtils.showReviewActivity(context);
                    }
                })
                .build();

        materialDialog.setCancelable(false);
        materialDialog.setCanceledOnTouchOutside(false);
        materialDialog.show();
    }
}
