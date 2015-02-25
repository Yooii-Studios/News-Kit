package com.yooiistudios.newsflow.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.yooiistudios.newsflow.R;

/**
 * Created by Wooseong Kim in News L from Yooii Studios Co., LTD. on 2014. 10. 16.
 *
 * AdDialogFactory
 *  종료시 애드뷰를 띄워주는 팩토리 클래스
 */
public class AdDialogFactory {
    private AdDialogFactory() { throw new AssertionError("Must not create this class!"); }

    public static final String AD_UNIT_ID = "ca-app-pub-2310680050309555/2431407023";

    public static AdView initAdView(Context context,
                                    final com.google.android.gms.ads.AdRequest adRequest) {
        // make AdView again for next quit dialog
        // prevent child reference
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(AdDialogFactory.AD_UNIT_ID);
        adView.loadAd(adRequest);
        return adView;
    }

    public static AlertDialog makeAdDialog(final Activity activity, final AdView adView) {
        Context context = activity.getApplicationContext();
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(R.string.quit_ad_dialog_title_text);
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                activity.finish();
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setView(adView); // Android L 에서 윗 공간이 좀 이상하긴 하지만 기본으로 가야할듯

        // 기타 필요한 설정
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}