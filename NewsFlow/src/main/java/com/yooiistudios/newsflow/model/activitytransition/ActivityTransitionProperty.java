package com.yooiistudios.newsflow.model.activitytransition;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 10. 1.
 *
 * ActivityTransitionProperty
 *  액티비티 트랜지션 프로퍼티 부모 클래스
 */
public abstract class ActivityTransitionProperty {

    private int mLeft;
    private int mTop;
    private int mWidth;
    private int mHeight;

    public ActivityTransitionProperty setLeft(int left) {
        mLeft = left;

        return this;
    }

    public ActivityTransitionProperty setTop(int top) {
        mTop = top;

        return this;
    }

    public ActivityTransitionProperty setWidth(int width) {
        mWidth = width;

        return this;
    }

    public ActivityTransitionProperty setHeight(int height) {
        mHeight = height;

        return this;
    }

    public int getLeft() {
        return mLeft;
    }

    public int getTop() {
        return mTop;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
