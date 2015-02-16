package com.yooiistudios.newsflow.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 24.
 *
 * NLScreenUtils
 *  스크린 사이즈와 관련된 유틸리티 클래스. Google I/O 2014 참고
 */
public class ScreenUtils {
    private ScreenUtils() { throw new AssertionError("MUST not create this class!"); }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /** Calculates the Action Bar height in pixels. */
    
    private static final int[] RES_IDS_ACTION_BAR_SIZE = { android.R.attr.actionBarSize };
    public static int calculateActionBarSize(Context context) {
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

    public static int calculateStatusBarHeight(final Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point displaySize = new Point();
        display.getSize(displaySize);

        return displaySize;
    }
}
