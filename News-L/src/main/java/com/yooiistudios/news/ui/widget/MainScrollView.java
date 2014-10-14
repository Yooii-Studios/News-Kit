package com.yooiistudios.news.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import lombok.Setter;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 10. 14.
 *
 * MainScrollView
 *  메인 스크롤뷰의 터치 리스너를 오버라이딩하기 위해 구현
 */
public class MainScrollView extends ScrollView {
    public interface OnTouchUpDownListener {
        public void onActionDown();
        public void onActionUpCancel();
    }

    @Setter OnTouchUpDownListener listener;

    public MainScrollView(Context context) {
        super(context);
    }

    public MainScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MainScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (listener != null) {
                listener.onActionDown();
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP) {
            if (listener != null) {
                listener.onActionUpCancel();
            }
        }
        return super.onTouchEvent(ev);
    }
}
