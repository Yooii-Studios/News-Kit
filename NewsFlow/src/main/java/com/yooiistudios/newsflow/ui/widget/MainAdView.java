package com.yooiistudios.newsflow.ui.widget;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.yooiistudios.orientationadview.OrientationAdView;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 17.
 *
 * MainAdView
 *  메인화면에 사용될 광고뷰
 */
public class MainAdView extends OrientationAdView {
    private static final String AD_UNIT_ID_PORTRAIT = "ca-app-pub-2310680050309555/9954673820";
    private static final String AD_UNIT_ID_LANDSCAPE = "ca-app-pub-2310680050309555/1247095825";

    public MainAdView(Context context) {
        super(context, AD_UNIT_ID_PORTRAIT, AD_UNIT_ID_LANDSCAPE);
    }

    public void attachToView(RelativeLayout parent) {
        int orientation = getResources().getConfiguration().orientation;

        // TODO 가로 세로 대응
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        parent.addView(this, lp);
    }

    public void applyBottomMarginOnPortrait(int bottomMargin) {
        RelativeLayout.LayoutParams adViewLp =
                (RelativeLayout.LayoutParams)getLayoutParams();
        adViewLp.bottomMargin = bottomMargin;
    }
}
