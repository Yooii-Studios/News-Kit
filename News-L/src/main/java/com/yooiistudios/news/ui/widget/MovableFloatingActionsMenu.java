package com.yooiistudios.news.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 11.
 *
 * MovableFloatingActionButton
 *  웹뷰의 스크롤을 캐치해서 숨고 나타나는 FloatingActionButton
 */
public class MovableFloatingActionsMenu extends FloatingActionsMenu implements ScrollDetector.OnScrollDirectionListener {
    private static final int TRANSLATE_DURATION_MILLIS = 200;

    private boolean mVisible;
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public MovableFloatingActionsMenu(Context context) {
        super(context);
    }

    public MovableFloatingActionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovableFloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void attachToWebView(@NonNull ObservableWebView webView) {
        ScrollDetector mScrollDetector = new ScrollDetector(getContext(), this);
        webView.setScrollDetector(mScrollDetector);
    }

    public void show() {
        show(true);
    }

    public void hide() {
        hide(true);
    }

    public void show(boolean animate) {
        toggle(true, animate, false);
    }

    public void hide(boolean animate) {
        toggle(false, animate, false);
    }

    private void toggle(final boolean visible, final boolean animate, boolean force) {
        /*
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height + getMarginBottom();
            if (animate) {
                ViewPropertyAnimator.animate(this).setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY);
            } else {
                ViewHelper.setTranslationY(this, translationY);
            }
        }
        */
    }

    @Override
    public void onScrollUp() {
//        NLLog.now("onScrollUp");
        hide();
    }

    @Override
    public void onScrollDown() {
//        NLLog.now("onScrollDown");
        show();
    }
}
