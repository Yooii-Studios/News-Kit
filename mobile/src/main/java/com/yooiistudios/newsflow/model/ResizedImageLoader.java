package com.yooiistudios.newsflow.model;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.cache.volley.CacheAsyncTask;
import com.yooiistudios.newsflow.core.cache.volley.ImageCache;
import com.yooiistudios.newsflow.core.news.ImageRequestQueue;
import com.yooiistudios.newsflow.core.news.SimpleImageCache;
import com.yooiistudios.newsflow.core.util.Display;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 24.
 *
 * ResizedImageLoader
 *  리사이징을 강제하는 이미지 로더
 */
public class ResizedImageLoader {
    private ImageLoader mImageLoader;
    private ImageCache mCache;
    private Point mImageSize;

    private ResizedImageLoader(FragmentActivity activity) {
        initImageLoader(activity);

        initImageSize(activity.getApplicationContext());
    }

    private ResizedImageLoader(Context context) {
        initImageLoaderWithNonRetainingCache(context);

        initImageSize(context);
    }

    private void initImageLoader(FragmentActivity activity) {
        RequestQueue requestQueue =
                ImageRequestQueue.getInstance(activity.getApplicationContext()).getRequestQueue();
        mCache = SimpleImageCache.getInstance().get(activity);
        mImageLoader = new ImageLoader(requestQueue, SimpleImageCache.getInstance().get(activity));
    }

    private void initImageLoaderWithNonRetainingCache(Context context) {
        RequestQueue requestQueue = ImageRequestQueue.getInstance(context.getApplicationContext())
                .getRequestQueue();
        mCache = SimpleImageCache.getInstance().getNonRetainingCache(context);
        mImageLoader = new ImageLoader(requestQueue, mCache);
    }

    private void initImageSize(Context context) {
        mImageSize = Display.getDisplaySize(context);
        mImageSize.y = context.getResources().getDimensionPixelSize(R.dimen.detail_top_image_view_height);
    }

    public static ResizedImageLoader create(FragmentActivity activity) {
        return new ResizedImageLoader(activity);
    }

    public static ResizedImageLoader createWithNonRetainingCache(Context context) {
        return new ResizedImageLoader(context);
    }

    public ImageLoader.ImageContainer get(String requestUrl,
                                          ImageLoader.ImageListener imageListener) {
        return mImageLoader.get(requestUrl, imageListener, mImageSize.x, mImageSize.y);
    }

    public void flushCache() {
        CacheAsyncTask.flushCache(mCache);
    }

    public void closeCache() {
        CacheAsyncTask.closeCache(mCache);
    }
}
