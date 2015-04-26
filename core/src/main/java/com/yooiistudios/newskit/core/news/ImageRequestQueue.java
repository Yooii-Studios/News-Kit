package com.yooiistudios.newskit.core.news;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 19.
 *
 * NewsImageRequestQueue
 *  Volley 의 이미지 리퀘스트 인스턴스를 가짐
 */
public class ImageRequestQueue {

    private RequestQueue mRequestQueue;
    private static ImageRequestQueue instance;

    public static ImageRequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new ImageRequestQueue(context);
        }

        return instance;
    }
    private ImageRequestQueue(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
