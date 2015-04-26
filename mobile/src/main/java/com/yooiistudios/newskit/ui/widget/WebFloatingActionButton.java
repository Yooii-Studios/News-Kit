package com.yooiistudios.newskit.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.melnykov.fab.FloatingActionButton;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 13.
 *
 * WebFloatingActionButton
 *  FloatingActionButton 을 활용해서 WebView 에서도 콜백을 사용할 수 있게 만든 클래스
 */
public class WebFloatingActionButton extends FloatingActionButton implements ScrollDetector.OnScrollDirectionListener {
    public WebFloatingActionButton(Context context) {
        super(context);
    }

    public WebFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebFloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void attachToWebView(@NonNull ObservableWebView webView) {
        webView.setScrollDetector(new ScrollDetector(getContext(), this));
    }

    @Override
    public void onScrollUp() {
        hide();
    }

    @Override
    public void onScrollDown() {
        show();
    }
}
