package com.yooiistudios.newskit.ui.widget;

import android.content.Context;

import com.yooiistudios.newskit.core.ui.HTML5WebView;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 11.
 *
 * ObservableWebView
 *  HTML5WebView 에 스크롤 방향을 체크하는 콜백이 구현된 클래스
 */
public class ObservableWebView extends HTML5WebView {
    private ScrollDetector mScrollDetector;

    public ObservableWebView(Context context) {
        super(context);
    }

    public void setScrollDetector(ScrollDetector scrollDetector) {
        mScrollDetector = scrollDetector;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        try {
            mScrollDetector.onScrollChanged(l, t, oldl, oldt);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
