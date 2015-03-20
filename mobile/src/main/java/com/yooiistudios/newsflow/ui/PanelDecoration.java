package com.yooiistudios.newsflow.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.yooiistudios.newsflow.R;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 4.
 *
 * PanelDecoration
 *  메인, 뉴스피드 디테일 색, 이미지 관련 클래스
 */
public class PanelDecoration {
    public static Bitmap getDummyNewsImage(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.img_news_dummy);
    }

    /**
     * Color used to Main Top news image and Dummy image
     */
    public static int getTopGrayFilterColor() {
        return Color.argb(127, 16, 16, 16);
    }

    public static int getTopDummyImageFilterColor() {
        return getTopGrayFilterColor();
    }

    public static int getBottomGrayFilterColor(Context context) {
        int grayColor = context.getResources().getColor(R.color.material_blue_grey_500);
        int red = Color.red(grayColor);
        int green = Color.green(grayColor);
        int blue = Color.blue(grayColor);
        int alpha = context.getResources().getInteger(R.integer.vibrant_color_tint_alpha);
        return Color.argb(alpha, red, green, blue);
    }

    public static int getBottomDummyImageFilterColor(Context context) {
        return getBottomGrayFilterColor(context);
    }

    public static int getMainBottomDefaultBackgroundColor() {
        return Color.argb(200, 16, 16, 16);
    }
}
