package com.yooiistudios.newskit.util;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yooiistudios.newskit.NewsApplication;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.iab.IabProducts;
import com.yooiistudios.newskit.ui.activity.MainActivity;
import com.yooiistudios.newskit.ui.activity.StoreActivity;

import java.util.List;

/**
 * Created by Wooseong Kim in MorningKit from Yooii Studios Co., LTD. on 2014. 6. 17.
 *
 * MNAdChecker
 *  광고를 주기적으로 체크해서 10회 실행 이후부터 5번에 한번씩 전면광고를 실행
 *  50회 이상 실행이면 4번에 한번씩 전면광고를 실행
 *
 *  -> 수정:
 *  20 60 100 140 180 ~ : 뉴스키트 풀버전 구매 요청
 *  80 120 160 200 ~ : 모닝키트 다운 요청
 *
 *  20에 뉴스키트 광고, 60부터 시작해서 20번마다 뉴스키트, 모닝키트 번갈아 광고
 */
public class AdUtils {
    private AdUtils() { throw new AssertionError("You MUST not create this class!"); }
    private static final String KEY = "AdUtils";
    private static final String LAUNCH_COUNT = "launch_count";
    private static final String EACH_LAUNCH_COUNT = "each_launch_count";
    private static final String EACH_AD_COUNT = "each_ad_count";
    private static final String MORNING_KIT_PACKAGE_NAME = "com.yooiistudios.morningkit";

    // 전면 광고 아이디는 각자의 앱에 맞는 전면 광고 ID를 추가
//    private static final String INTERSTITIAL_ID = "ca-app-pub-2310680050309555/8912992223";

    public static void showPopupAdIfSatisfied(Context context) {
        if (context == null) {
            return;
        }

        showMorningKitAd(context);

        List<String> ownedSkus = IabProducts.loadOwnedIabProducts(context);

        // 풀버전 구매 아이템이 없을 경우만 진행
        if (!ownedSkus.contains(IabProducts.SKU_PRO_VERSION)) {
            SharedPreferences prefs = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
            int launchCount = prefs.getInt(LAUNCH_COUNT, 1);
            if (shouldShowAd(prefs, launchCount)) {

                // 풀버전이 나올 때 아이템들을 체크
                AnalyticsUtils.trackInterstitialAd(
                        (NewsApplication) context.getApplicationContext(),
                        MainActivity.TAG);

                // 3번째 마다 인하우스 스토어 광고를 보여주게 로직 수정
                int eachAdCount = prefs.getInt(EACH_AD_COUNT, 1);
                if (eachAdCount >= 2) {
                    prefs.edit().remove(EACH_AD_COUNT).apply();
                    showMorningKitAd(context);
                } else if (eachAdCount < 2 || launchCount == 20) {
                    if (launchCount > 20) {
                        prefs.edit().putInt(EACH_AD_COUNT, ++eachAdCount).apply();
                    }
                    showInHouseStoreAd(context);
                }
            }
            // 40회 부터 시작해서 20번 실행마다 광고를 보여주면 되기에 더이상 체크 X
            if (launchCount < 41) {
                launchCount++;
                prefs.edit().putInt(LAUNCH_COUNT, launchCount).apply();
            }
        }
    }


    private static boolean shouldShowAd(SharedPreferences prefs, final int launchCount) {
        // 일정 카운트(40) 이상부터는 launchCount 는 더 증가시킬 필요가 없음. 실행 횟수만 체크
        if (launchCount >= 41) {
            int threshold = 20;

            int eachLaunchCount = prefs.getInt(EACH_LAUNCH_COUNT, 1);
            if (eachLaunchCount >= threshold) {
                // 광고 표시와 동시에 다시 초기화
                prefs.edit().remove(EACH_LAUNCH_COUNT).apply();
                return true;
            } else {
                eachLaunchCount++;
                prefs.edit().putInt(EACH_LAUNCH_COUNT, eachLaunchCount).apply();
            }
        } else if (launchCount == 20) {
            return true;
        }
        return false;
    }



    /*
    private static void showInterstitialAd(Context context) {
        // 전체 광고 표시
        final InterstitialAd interstitialAdView = new InterstitialAd(context);
        interstitialAdView.setAdUnitId(INTERSTITIAL_ID);
        interstitialAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (interstitialAdView.isLoaded()) {
                    interstitialAdView.show();
                }
            }
        });
        AdRequest fullAdRequest = new AdRequest.Builder()
//                            .addTestDevice("D9XXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
                .build();
        interstitialAdView.loadAd(fullAdRequest);
    }
    */

    private static void showInHouseStoreAd(final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.store_ad_dialog_layout);

        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 다이얼로그 중앙 정렬용 코드였지만 위의 FEATURE_NO_TITLE 설정으로 인해 필요 없게 됨
//        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        window.setGravity(Gravity.CENTER);

        TextView titleTextView =
                (TextView) dialog.findViewById(R.id.store_ad_dialog_title_text_view);
        titleTextView.setText(context.getString(R.string.app_name) + " PRO");
        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                context.startActivity(new Intent(context, StoreActivity.class));
            }
        });

        ImageView storeImageView = (ImageView) dialog.findViewById(R.id.store_ad_dialog_image_view);
        storeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                context.startActivity(new Intent(context, StoreActivity.class));
            }
        });

        TextView storeButtonView = (TextView) dialog.findViewById(R.id.store_ad_dialog_ok_button);
        storeButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                context.startActivity(new Intent(context, StoreActivity.class));
            }
        });

        TextView cancelButtonView = (TextView) dialog.findViewById(R.id.store_ad_dialog_cancel_button);
        cancelButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // 기타 필요한 설정
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private static void showMorningKitAd(final Context context) {
        // 모닝키트가 깔려 있으면 보여주지 말기
        if (!isPackageExisted(context, "com.yooiistudios.morningkit")) {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.morningkit_ad_dialog_layout);

            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            ImageView imageView = (ImageView) dialog.findViewById(R.id.morningkit_ad_dialog_image_view);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    goToPlayStoreForMorningKit(context);
                }
            });

            TextView downloadButtonView = (TextView) dialog.findViewById(R.id.morningkit_ad_dialog_download_button);
            downloadButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    goToPlayStoreForMorningKit(context);
                }
            });

            TextView closeButtonView = (TextView) dialog.findViewById(R.id.morningkit_ad_dialog_close_button);
            closeButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            // 기타 필요한 설정
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    private static boolean isPackageExisted(Context context, String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage)) return true;
        }
        return false;
    }

    private static void goToPlayStoreForMorningKit(Context context) {
        Uri uri = Uri.parse("market://details?id=" + MORNING_KIT_PACKAGE_NAME);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Couldn't launch the market", Toast.LENGTH_SHORT).show();
        }
    }
}
