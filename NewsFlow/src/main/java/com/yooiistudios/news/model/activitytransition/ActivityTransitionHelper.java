package com.yooiistudios.news.model.activitytransition;

import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 10. 1.
 *
 * ActivityTransitionProperty
 *  액티비티 전환시 필요한 데이터를 저장하는 클래스
 */
public class ActivityTransitionHelper {
    public static final String KEY_IMAGE = "KEY_IMAGE";
    public static final String KEY_TEXT = "KEY_TEXT";
    public static final String KEY_SUB_TEXT = "KEY_SUB_TEXT";

    private HashMap<String, ActivityTransitionImageViewProperty> mImageViewPropertyMap;
    private HashMap<String, ActivityTransitionTextViewProperty> mTextViewPropertyMap;

    public ActivityTransitionHelper() {
        mImageViewPropertyMap = new HashMap<>();
        mTextViewPropertyMap = new HashMap<>();
    }

    public ActivityTransitionImageViewProperty getImageViewProperty(String key) {
        return mImageViewPropertyMap.get(key);
    }

    public ActivityTransitionTextViewProperty getTextViewProperty(String key) {
        return mTextViewPropertyMap.get(key);
    }

    public ActivityTransitionHelper addImageView(String key, ImageView imageView) {
        int[] screenLocation = new int[2];
        imageView.getLocationOnScreen(screenLocation);

        mImageViewPropertyMap.put(
                key,
                new ActivityTransitionImageViewProperty()
                    .setLeft(screenLocation[0])
                    .setTop(screenLocation[1])
                    .setWidth(imageView.getWidth())
                    .setHeight(imageView.getHeight())
        );

        return this;
    }

    public ActivityTransitionHelper addTextView(String key, TextView textView, int padding) {
        int[] screenLocation = new int[2];
        textView.getLocationOnScreen(screenLocation);

        mTextViewPropertyMap.put(
                key,
                new ActivityTransitionTextViewProperty()
                        .setLeft(screenLocation[0])
                        .setTop(screenLocation[1])
                        .setWidth(textView.getWidth())
                        .setHeight(textView.getHeight())
                        .setPadding(padding)
                        .setText(textView.getText().toString())
                        .setTextSize(textView.getTextSize())
                        .setTextColor(textView.getCurrentTextColor())
                        .setEllipsizeOrdinal(textView.getEllipsize().ordinal())
                        .setGravity(textView.getGravity())
                        .setMaxLine(textView.getMaxLines())
        );

        return this;
    }


    public String toGsonString() {
        return new Gson().toJson(this);
    }
}
