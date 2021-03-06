/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yooiistudios.newskit.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.yooiistudios.newskit.core.util.DipToPixel;

import java.util.ArrayList;

/**
 * A custom ScrollView that can accept a scroll listener.
 *
 * ObservableScrollView
 *  Google I/O 2014 에서 가져온 스크롤 할 경우 callback 을 시켜주는 스크롤뷰 클래스
 */
public class ObservableScrollView extends ScrollView {
    private ArrayList<Callbacks> mCallbacks = new ArrayList<>();

    // Swipe 기능
    private float downX, downY;
    private final int MIN_DISTANCE_DP = 60;

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        for (Callbacks c : mCallbacks) {
            c.onScrollChanged(l - oldl, t - oldt);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();

                for (Callbacks c : mCallbacks) {
                    c.onScrollStarted();
                }
                break;

            case MotionEvent.ACTION_UP: {
                float upX = event.getX();
                float upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                // 가로 스와이프를 할 때 세로로는 조금만 움직일 때만 스와이프로 인식하기
                int MIN_DISTANCE = DipToPixel.dpToPixel(getContext(), MIN_DISTANCE_DP);
                if(Math.abs(deltaX) > MIN_DISTANCE && deltaX < 0 &&
                        Math.abs(deltaY) < MIN_DISTANCE * 3){
                    for (Callbacks c : mCallbacks) {
                        c.onSwipeLeft();
                    }
                    return true;
                }
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    public void addCallbacks(Callbacks listener) {
        if (!mCallbacks.contains(listener)) {
            mCallbacks.add(listener);
        }
    }

    public interface Callbacks {
        void onScrollChanged(int deltaX, int deltaY);
        void onScrollStarted();
        void onSwipeLeft();
    }
}
