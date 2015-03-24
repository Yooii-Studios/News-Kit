package com.yooiistudios.newsflow.core.cache.volley;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 19.
 *
 * NewsImageRequestQueue
 *  Volley 의 이미지 리퀘스트 인스턴스를 가짐
 */
public class NewsImageRequestQueue {

    private RequestQueue mRequestQueue;
    private static NewsImageRequestQueue instance;

    public static NewsImageRequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new NewsImageRequestQueue(context);
        }

        return instance;
    }
    private NewsImageRequestQueue(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
