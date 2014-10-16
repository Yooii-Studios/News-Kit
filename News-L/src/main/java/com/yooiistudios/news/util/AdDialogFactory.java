package com.yooiistudios.news.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import com.google.android.gms.ads.AdView;
import com.yooiistudios.news.R;

/**
 * Created by Wooseong Kim in News L from Yooii Studios Co., LTD. on 2014. 10. 16.
 *
 * AdDialogFactory
 *  종료시 애드뷰를 띄워주는 팩토리 클래스
 */
public class AdDialogFactory {
    private AdDialogFactory() { throw new AssertionError("Must not create this class!"); }

    public static final String AD_UNIT_ID = "ca-app-pub-2310680050309555/2431407023";

    public static AlertDialog makeAdDialog(final Activity activity, final AdView adView) {
        Context context = activity.getApplicationContext();
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(R.string.ad_dialog_title_text);
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

        AlertDialog wakeDialog = builder.create();
        /*
        // 이렇게 하면 한 광고로 계속 보여줄 수 있지만 새 광고를 계속 새로 띄우기 위해 일부러 구현 안함
        wakeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }
        });
        */

        wakeDialog.setView(adView); // Android L 에서 윗 공간이 좀 이상하긴 하지만 기본으로 가야할듯
//        wakeDialog.setView(adView, 0, 0, 0, 0);
//        wakeDialog.setView(adView, 0,
//                context.getResources().getDimensionPixelSize(R.dimen.ad_dialog_top_margin), 0, 0);

        // 기타 필요한 설정
        wakeDialog.setCanceledOnTouchOutside(false);

        return wakeDialog;
    }
}
