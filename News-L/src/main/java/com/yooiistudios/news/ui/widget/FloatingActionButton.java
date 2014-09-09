/*
 * Copyright 2014 Google Inc.
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

package com.yooiistudios.news.ui.widget;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * A Floating Action Button is a {@link android.widget.Checkable} view distinguished by a circled
 * icon floating above the UI, with special motion behaviors.
 */
public class FloatingActionButton extends FrameLayout {

    private static String TAG = "FloatingActionButton";
//    private OnActionListener mOnActionListener;

    /**
     * The coordinates of a touch action.
     */
//    protected Point mTouchPoint;

    /**
     * A {@link android.view.GestureDetector} to detect touch actions.
     */
//    private GestureDetector mGestureDetector;


    public FloatingActionButton(Context context) {
        this(context, null, 0, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr,
                                int defStyleRes) {
        super(context, attrs, defStyleAttr);

        // When a view is clickable it will change its state to "pressed" on every click.
        setClickable(true);

//        // Create a {@link GestureDetector} to detect single taps.
//        mGestureDetector = new GestureDetector(context,
//                new GestureDetector.SimpleOnGestureListener() {
//                    @Override
//                    public boolean onSingleTapConfirmed(MotionEvent e) {
//                        mTouchPoint = new Point((int) e.getX(), (int) e.getY());
//                        NLLog.i(TAG, "Single tap captured.");
//                        mOnActionListener.onClick();
//                        return true;
//                    }
//                }
//        );
    }

//    public interface OnActionListener {
//        public void onClick();
//    }
//
//    public void setOnActionListener(OnActionListener listener) {
//        mOnActionListener = listener;
//    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (mGestureDetector.onTouchEvent(event)) {
//            return true;
//        }
//        if (event.getAction() == MotionEvent.ACTION_UP ||
//                event.getAction() == MotionEvent.ACTION_CANCEL) {
//            refreshDrawableState();
//        }
//        return super.onTouchEvent(event);
//    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Outline outline = new Outline();
        outline.setOval(0, 0, w, h);
        setOutline(outline);
        setClipToOutline(true);
    }

}
