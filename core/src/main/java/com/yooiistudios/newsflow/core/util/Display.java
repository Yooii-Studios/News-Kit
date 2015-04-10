package com.yooiistudios.newsflow.core.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.WindowManager;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 24.
 *
 * NLScreenUtils
 *  스크린 사이즈와 관련된 유틸리티 클래스. Google I/O 2014 참고
 */
public class Display {
    private Display() { throw new AssertionError("MUST not create this class!"); }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /** Calculates the Action Bar height in pixels. */
    
    private static final int[] RES_IDS_ACTION_BAR_SIZE = { android.R.attr.actionBarSize };
    public static int getActionBarSize(Context context) {
        if (context == null) {
            return 0;
        }

        Resources.Theme curTheme = context.getTheme();
        if (curTheme == null) {
            return 0;
        }

        TypedArray att = curTheme.obtainStyledAttributes(RES_IDS_ACTION_BAR_SIZE);
        if (att == null) {
            return 0;
        }

        float size = att.getDimensionPixelSize(0, 0);
        att.recycle();
        return (int) size;
    }

    /*
    // 제대로 결과가 안나와서 아래 메서드로 대체
    public static int calculateStatusBarSize(Window window) {
        Rect rectangle= new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentViewTop - statusBarHeight;

        NLLog.i("*** Jorgesys :: ", "StatusBar Height= " + statusBarHeight + " , TitleBar Height = " + titleBarHeight);

        return statusBarHeight;
    }
    */

    public static int getStatusBarHeight(final Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static Point getDisplaySizeOnPortrait(Context context) {
        Point displaySize = getDisplaySize(context);
        if (Device.isLandscape(context)) {
            displaySize = new Point(displaySize.y, displaySize.x);
        }
        return displaySize;
    }

    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        android.view.Display display = wm.getDefaultDisplay();

        Point displaySize = new Point();
        display.getSize(displaySize);

        return displaySize;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void applyTranslucentNavigationBarAfterLollipop(Activity activity) {
        if (Device.hasLollipop()) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void removeTranslucentNavigationBarAfterLollipop(Activity activity) {
        if (Device.hasLollipop()) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static int getNavigationBarHeight(Context context) {
        return getNavigationBarRect(context).height();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Rect getNavigationBarRect(Context context) {
        Point displaySize = getDisplaySize(context);
        Point realSize = getRealDisplaySize(context);

        return new Rect(0, 0, displaySize.x, realSize.y - displaySize.y);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Point getRealDisplaySize(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        android.view.Display display = wm.getDefaultDisplay();

        Point realSize = new Point();
        display.getRealSize(realSize);

        return realSize;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasTranslucentNavigationBar(Activity activity) {
        int flags = activity.getWindow().getAttributes().flags;
        return (flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) != 0;
    }
}
