package com.yooiistudios.newskit.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.yooiistudios.newskit.R;

/**
 * Created by Wooseong Kim in News L from Yooii Studios Co., LTD. on 2014. 10. 16.
 *
 * AdDialogFactory
 *  종료시 애드뷰를 띄워주는 팩토리 클래스
 */
public class AdDialogFactory {
    private AdDialogFactory() { throw new AssertionError("Must not create this class!"); }

    public static final String AD_UNIT_ID_PORT = "ca-app-pub-2310680050309555/2431407023";
    public static final String AD_UNIT_ID_LAND = "ca-app-pub-2310680050309555/9247583428";

    public static AdView initAdView(Context context, AdSize adSize, String adUnitId,
                                    final AdRequest adRequest) {
        // make AdView again for next quit dialog
        // prevent child reference
        AdView adView = new AdView(context);
        adView.setAdSize(adSize);
        adView.setAdUnitId(adUnitId);
        adView.loadAd(adRequest);
        return adView;
    }

    public static AlertDialog makeAdDialog(final Activity activity, final AdView mediumAdView,
                                           final AdView largeBannerAdView) {
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

        final AlertDialog quitDialog = builder.create();
        // 세로 전용 앱이라서 동현 파트를 제거할 경우 아래 주석을 해제할 것
//        dialog.setView(adView); // Android L 에서 윗 공간이 좀 이상하긴 하지만 기본으로 가야할듯

        /**
         * 동현 파트
         */
        final View adContentView = new View(activity);
        adContentView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        quitDialog.setView(adContentView);
        quitDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                ViewParent parentView = adContentView.getParent();
                if (parentView instanceof ViewGroup) {
                    final ViewGroup contentWrapper = ((ViewGroup) parentView);
                    contentWrapper.removeView(adContentView);

                    int contentWidth = adContentView.getWidth();
                    int contentHeight = adContentView.getHeight();
                    float screenDensity = activity.getResources().getDisplayMetrics().density;

                    if (contentWidth >= 300 * screenDensity && contentHeight >= 250 * screenDensity) {
                        // medium rectangle
                        contentWrapper.addView(mediumAdView);
                        quitDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                contentWrapper.removeView(mediumAdView);
                            }
                        });
                    } else if (contentWidth >= 320 * screenDensity && contentHeight >= 100 * screenDensity) {
                        // large banner
                        contentWrapper.addView(largeBannerAdView);
                        quitDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                contentWrapper.removeView(largeBannerAdView);
                            }
                        });
                    }
                }
            }
        });
        quitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        /**
         * 동현 파트 끝
         */

        // 기타 필요한 설정
        quitDialog.setCanceledOnTouchOutside(false);

        return quitDialog;
    }
}
