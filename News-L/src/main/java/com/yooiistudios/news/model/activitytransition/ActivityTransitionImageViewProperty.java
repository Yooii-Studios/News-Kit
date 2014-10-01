package com.yooiistudios.news.model.activitytransition;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 10. 1.
 *
 * ActivityTransitionImageProperty
 *  이미지의 속성 저장
 */
public class ActivityTransitionImageViewProperty {

    private int mLeft;
    private int mTop;
    private int mWidth;
    private int mHeight;

    public ActivityTransitionImageViewProperty setLeft(int left) {
        mLeft = left;

        return this;
    }

    public ActivityTransitionImageViewProperty setTop(int top) {
        mTop = top;

        return this;
    }

    public ActivityTransitionImageViewProperty setWidth(int width) {
        mWidth = width;

        return this;
    }

    public ActivityTransitionImageViewProperty setHeight(int height) {
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
