package com.yooiistudios.newsflow.core.news;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.yooiistudios.newsflow.core.cache.volley.CacheAsyncTask;
import com.yooiistudios.newsflow.core.cache.volley.ImageCache;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 23.
 *
 * TwoLevelImageCache
 *  L2 디스크 캐시를 초기화하는 코드의 래퍼
 */
public class SimpleImageCache {
    private static SimpleImageCache instance;

    private ImageCache mRetainingImageCache;
    private ImageCache mNonRetainingImageCache;

    private SimpleImageCache() {}

    /**
     * Singleton
     */
    public static SimpleImageCache getInstance() {
        if (instance == null) {
            synchronized (SimpleImageCache.class) {
                if (instance == null) {
                    instance = new SimpleImageCache();
                }
            }
        }
        return instance;
    }

    public ImageCache get(FragmentActivity activity) {
        if (mRetainingImageCache == null) {
            createRetainingImageCache(
                    activity.getApplicationContext(),
                    activity.getSupportFragmentManager()
            );
            initDiskCache(mRetainingImageCache);
        }
        return mRetainingImageCache;
    }

    public ImageCache get(Context context, FragmentManager fragmentManager) {
        if (mRetainingImageCache == null) {
            createRetainingImageCache(context, fragmentManager);
            initDiskCache(mRetainingImageCache);
        }
        return mRetainingImageCache;
    }

    public ImageCache getNonRetainingCache(Context context) {
        if (mNonRetainingImageCache == null) {
            createNonRetainingImageCache(context);
            initDiskCache(mNonRetainingImageCache);
        }
        return mNonRetainingImageCache;
    }

    private void createRetainingImageCache(Context context, FragmentManager fragmentManager) {
        mRetainingImageCache = ImageCache.getInstance(
                context.getResources(),
                fragmentManager,
                createImageCacheParams(context)
        );
    }

    private void createNonRetainingImageCache(Context context) {
        mNonRetainingImageCache = ImageCache.createNewInstance(
                context.getResources(),
                createImageCacheParams(context)
        );
    }

    private void initDiskCache(ImageCache imageCache) {
        CacheAsyncTask.initDiskCache(imageCache);
    }

    private ImageCache.ImageCacheParams createImageCacheParams(Context context) {
        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(context.getApplicationContext(), "image");

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
        cacheParams.diskCacheSize = 50 * 1024 * 1024; // 10MB
        return cacheParams;
    }
}
