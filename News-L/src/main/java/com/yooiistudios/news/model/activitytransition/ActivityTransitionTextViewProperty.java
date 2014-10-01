package com.yooiistudios.news.model.activitytransition;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 10. 1.
 *
 * ActivityTransitionImageProperty
 *  이미지의 속성 저장
 */
public class ActivityTransitionTextViewProperty {

    private int mLeft;
    private int mTop;
    private int mWidth;
    private int mHeight;
    private int mPadding;

    private String mText;
    private float mTextSize;
    private int mTextColor;
    private int mGravity;
    private int mEllipsizeOrdinal;
    private int mMaxLine;

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

    public int getPadding() {
        return mPadding;
    }

    public String getText() {
        return mText;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getGravity() {
        return mGravity;
    }

    public int getEllipsizeOrdinal() {
        return mEllipsizeOrdinal;
    }

    public int getMaxLine() {
        return mMaxLine;
    }

    public ActivityTransitionTextViewProperty setLeft(int left) {
        mLeft = left;

        return this;
    }

    public ActivityTransitionTextViewProperty setTop(int top) {
        mTop = top;

        return this;
    }

    public ActivityTransitionTextViewProperty setWidth(int width) {
        mWidth = width;

        return this;
    }

    public ActivityTransitionTextViewProperty setHeight(int height) {
        mHeight = height;

        return this;
    }

    public ActivityTransitionTextViewProperty setPadding(int padding) {
        mPadding = padding;

        return this;
    }

    public ActivityTransitionTextViewProperty setText(String text) {
        mText = text;

        return this;
    }

    public ActivityTransitionTextViewProperty setTextSize(float textSize) {
        mTextSize = textSize;

        return this;
    }

    public ActivityTransitionTextViewProperty setTextColor(int textColor) {
        mTextColor = textColor;

        return this;
    }

    public ActivityTransitionTextViewProperty setGravity(int gravity) {
        mGravity = gravity;

        return this;
    }

    public ActivityTransitionTextViewProperty setEllipsizeOrdinal(int ellipsizeOrdinal) {
        mEllipsizeOrdinal = ellipsizeOrdinal;

        return this;
    }

    public ActivityTransitionTextViewProperty setMaxLine(int maxLine) {
        mMaxLine = maxLine;

        return this;
    }
}
