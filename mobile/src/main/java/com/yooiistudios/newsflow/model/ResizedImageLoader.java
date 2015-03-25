package com.yooiistudios.newsflow.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.support.v7.graphics.Palette;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.cache.volley.CacheAsyncTask;
import com.yooiistudios.newsflow.core.cache.volley.ImageCache;
import com.yooiistudios.newsflow.core.cache.volley.ImageResizer;
import com.yooiistudios.newsflow.core.news.ImageRequestQueue;
import com.yooiistudios.newsflow.core.news.SimpleImageCache;
import com.yooiistudios.newsflow.core.news.database.NewsDb;
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
        public void onSuccess(ImageResponse response);
        public void onFail(VolleyError error);
    }

    private interface ThumbnailListener {
        public void onSuccess(Bitmap resizedBitmap);
    }
    private interface PaletteListener {
        public void onSuccess(int vibrantColor);
    }

    private Context mContext;
    private ImageLoader mImageLoader;
    private ImageCache mCache;
    private Point mImageSize;

    private ResizedImageLoader(FragmentActivity activity) {
        mContext = activity.getApplicationContext();
        initImageLoader(activity);
        initImageSize(activity.getApplicationContext());
    }

    private ResizedImageLoader(Context context) {
        mContext = context;
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
        mImageSize.x -= mImageSize.x % 2;
        mImageSize.y -= mImageSize.y % 2;
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

    public ImageLoader.ImageContainer getThumbnail(final String requestUrl,
                                                   final ImageListener imageListener) {
        final Bitmap bitmap = getCachedThumbnail(requestUrl);
        if (bitmap != null) {
            getPaletteColors(requestUrl, bitmap, new PaletteListener() {
                @Override
                public void onSuccess(int vibrantColor) {
                    imageListener.onSuccess(new ImageResponse(requestUrl, bitmap, vibrantColor));
                }
            });

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
                final Bitmap bitmap = response.getBitmap();
                if (bitmap != null) {
                    getPaletteColors(request.url, bitmap, new PaletteListener() {

                        @Override
                        public void onSuccess(final int vibrantColor) {
                            if (request.type == ImageRequest.TYPE_LARGE) {
                                imageListener.onSuccess(
                                        new ImageResponse(request.url, bitmap, vibrantColor));
                            }
                            cacheThumbnail(bitmap, request, new ThumbnailListener() {
                                @Override
                                public void onSuccess(Bitmap thumbnailBitmap) {
                                    if (request.type == ImageRequest.TYPE_THUMBNAIL) {
                                        ImageResponse imageResponse = new ImageResponse(
                                                request.url,
                                                thumbnailBitmap,
                                                vibrantColor
                                        );
                                        imageListener.onSuccess(imageResponse);
                                    }
                                }
                            });
//                            ImageResizer.createScaledBitmap(bitmap,
//                                    bitmap.getWidth() - bitmap.getWidth() % 2,
//                                    bitmap.getHeight() - bitmap.getHeight() % 2,
//                                    false, true,
//                                    new ImageResizer.ResizeListener() {
//                                        @Override
//                                        public void onResize(Bitmap resizedBitmap) {
//                                        }
//                                    }
//                            );
                        }
                    });
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                imageListener.onFail(error);
            }
        }, mImageSize.x, mImageSize.y);
    }

    private void getPaletteColors(final String url, Bitmap bitmap, final PaletteListener listener) {
        int vibrantColor = NewsDb.getInstance(mContext).loadVibrantColor(url);
        if (vibrantColor != Color.TRANSPARENT) {
            listener.onSuccess(vibrantColor);
        } else {
            Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    final int vibrantColor = palette.getVibrantColor(Color.TRANSPARENT);
                    NewsDb.getInstance(mContext).savePaletteColor(url, palette);
                    listener.onSuccess(vibrantColor);
                }
            });
        }
    }

    private void cacheThumbnail(final Bitmap bitmap, final ImageRequest request,
                                final ThumbnailListener listener) {
        Bitmap thumbnail = getCachedThumbnail(request.url);
        if (thumbnail == null) {
            NLLog.now("original image. width: " + bitmap.getWidth() + ", height: " + bitmap.getHeight());
            int targetWidth = bitmap.getWidth() / 2;
            int targetHeight = bitmap.getHeight() / 2;
            NLLog.now("original image. width: " + bitmap.getWidth() + ", height: " + bitmap.getHeight());
            ImageResizer.createScaledBitmap(bitmap, targetWidth, targetHeight, false, false,
                    new ImageResizer.ResizeListener() {
                        @Override
                        public void onResize(Bitmap resizedBitmap) {
                            mCache.putBitmap(getThumbnailCacheKey(request.url), resizedBitmap);
                            listener.onSuccess(resizedBitmap);
                        }
                    });
        }
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

    public static class ImageResponse {
        public final String url;
        public final Bitmap bitmap;
        public final int vibrantColor;

        public ImageResponse(String url, Bitmap bitmap, int vibrantColor) {
            this.url = url;
            this.bitmap = bitmap;
            this.vibrantColor = vibrantColor;
        }
    }
}
