package com.yooiistudios.newsflow.ui.widget;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewParent;

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

    public void detachFromParent() {
        ViewGroup parent = getParentViewGroup();
        if (parent != null) {
            parent.removeView(this);
        }
    }

    private ViewGroup getParentViewGroup() {
        ViewParent parent = getParent();
        ViewGroup rootView = null;
        if (parent != null) {
            rootView = (ViewGroup)parent;
        }

        return rootView;
    }

    public void applyBottomMarginOnPortrait(int bottomMargin) {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            MarginLayoutParams adViewLp =
                    (MarginLayoutParams) getLayoutParams();
            adViewLp.bottomMargin = bottomMargin;
        }
    }
}
