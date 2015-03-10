package com.yooiistudios.newsflow.core.ui.activitytransition;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 10. 1.
 *
 * ActivityTransitionImageProperty
 *  이미지의 속성 저장
 */
public class ActivityTransitionTextViewProperty extends ActivityTransitionProperty {

    private int mPadding;

    private String mText;
    private float mTextSize;
    private int mTextColor;
    private int mGravity;
    private int mEllipsizeOrdinal;
    private int mMaxLine;


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

    @Override
    public ActivityTransitionTextViewProperty setLeft(int left) {
        super.setLeft(left);

        return this;
    }

    @Override
    public ActivityTransitionTextViewProperty setTop(int top) {
        super.setTop(top);

        return this;
    }

    @Override
    public ActivityTransitionTextViewProperty setWidth(int width) {
        super.setWidth(width);

        return this;
    }

    @Override
    public ActivityTransitionTextViewProperty setHeight(int height) {
        super.setHeight(height);

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
