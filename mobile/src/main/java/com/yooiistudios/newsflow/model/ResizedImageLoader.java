package com.yooiistudios.newsflow.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.cache.volley.CacheAsyncTask;
import com.yooiistudios.newsflow.core.cache.volley.ImageCache;
import com.yooiistudios.newsflow.core.cache.volley.ImageResizer;
import com.yooiistudios.newsflow.core.news.ImageRequestQueue;
import com.yooiistudios.newsflow.core.news.SimpleImageCache;
import com.yooiistudios.newsflow.core.util.Display;
import com.yooiistudios.newsflow.core.util.NLLog;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 24.
 *
 * ResizedImageLoader
 *  리사이징을 강제하는 이미지 로더
 */
public class ResizedImageLoader {
    public interface ImageListener {
        public void onSuccess(String url, Bitmap bitmap, boolean isImmediate);
        public void onFail(VolleyError error);
    }
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

    public ImageLoader.ImageContainer get(String requestUrl, ImageListener imageListener) {
        ImageRequest request = new ImageRequest();
        request.url = requestUrl;
        request.type = ImageRequest.TYPE_LARGE;

        return get(request, imageListener);
    }

    public ImageLoader.ImageContainer getThumbnail(String requestUrl, ImageListener imageListener) {
        Bitmap bitmap = getCachedThumbnail(requestUrl);
        if (bitmap != null) {
            imageListener.onSuccess(requestUrl, bitmap, true);

            return null;
        } else {
            ImageRequest request = new ImageRequest();
            request.url = requestUrl;
            request.type = ImageRequest.TYPE_THUMBNAIL;

            return get(request, imageListener);
        }
    }

    private Bitmap getCachedThumbnail(String requestUrl) {
        return mCache.getBitmap(getThumbnailCacheKey(requestUrl));
    }

    private ImageLoader.ImageContainer get(final ImageRequest request,
                                          final ImageListener imageListener) {
        return mImageLoader.get(request.url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                if (bitmap != null) {
                    NLLog.now(String.format("TYPE_LARGE\nbitmap width: %4d, height: %4d",
                            bitmap.getWidth(), bitmap.getHeight()));
                    if (request.type == ImageRequest.TYPE_LARGE) {
                        imageListener.onSuccess(request.url, bitmap, isImmediate);
                    }
                    cacheThumbnail(bitmap, request, imageListener);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                imageListener.onFail(error);
            }
        }, mImageSize.x, mImageSize.y);
    }

    private void cacheThumbnail(final Bitmap bitmap, final ImageRequest request,
                                final ImageListener imageListener) {
        Bitmap thumbnail = getCachedThumbnail(request.url);
        if (thumbnail == null) {
            int targetWidth = bitmap.getWidth() / 2;
            int targetHeight = bitmap.getHeight() / 2;
            ImageResizer.createScaledBitmap(bitmap, targetWidth, targetHeight, false,
                    new ImageResizer.ResizeListener() {
                        @Override
                        public void onResize(Bitmap resizedBitmap) {
                            NLLog.now(String.format("TYPE_THUMBNAIL\nbitmap width: %4d, height: %4d",
                                    resizedBitmap.getWidth(), resizedBitmap.getHeight()));
                            mCache.putBitmap(getThumbnailCacheKey(request.url), resizedBitmap);
                            if (request.type == ImageRequest.TYPE_LARGE) {
//                                resizedBitmap.recycle();
                            } else if (request.type == ImageRequest.TYPE_THUMBNAIL) {
                                imageListener.onSuccess(request.url, resizedBitmap, false);
//                                bitmap.recycle();
                            }
                        }
                    });
        }
//        else {
//            if (request.type == ImageRequest.TYPE_LARGE) {
//                thumbnail.recycle();
//            } else if (request.type == ImageRequest.TYPE_THUMBNAIL) {
//                bitmap.recycle();
//            }
//        }
    }

    private static String getThumbnailCacheKey(String url) {
        return "th_" + url;
    }

    public void flushCache() {
        CacheAsyncTask.flushCache(mCache);
    }

    public void closeCache() {
        CacheAsyncTask.closeCache(mCache);
    }

    private static class ImageRequest {
        public static final int TYPE_LARGE = 0;
        public static final int TYPE_THUMBNAIL = 1;
        public String url;
        public int type;
    }
}
