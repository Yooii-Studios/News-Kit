package com.yooiistudios.newskit.core.news;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.yooiistudios.newskit.core.cache.volley.CacheAsyncTask;
import com.yooiistudios.newskit.core.cache.volley.ImageCache;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 23.
 *
 * TwoLevelImageCache
 *  L2 디스크 캐시를 초기화하는 코드의 래퍼
 */
public class SimpleImageCache {
    private static final int DEFAULT_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 10MB

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

    public ImageCache getNonRetainingDiskOnlyImageCache(Context context) {
        if (mNonRetainingImageCache == null) {
            createNonRetainingDiskOnlyImageCache(context);
            initDiskCache(mNonRetainingImageCache);
        }
        return mNonRetainingImageCache;
    }

    private void createRetainingImageCache(Context context, FragmentManager fragmentManager) {
        mRetainingImageCache = ImageCache.getInstance(
                context.getResources(),
                fragmentManager,
                createCacheParams(context)
        );
    }

    private void createNonRetainingDiskOnlyImageCache(Context context) {
        mNonRetainingImageCache = ImageCache.createNewInstance(
                context.getResources(),
                createDiskOnlyImageCacheParams(context)
        );
    }

    private void initDiskCache(ImageCache imageCache) {
        CacheAsyncTask.initDiskCache(imageCache);
    }

    private ImageCache.ImageCacheParams createCacheParams(Context context) {
        ImageCache.ImageCacheParams cacheParams = createDefaultCacheParams(context);
        cacheParams.setMemCacheSizePercent(0.25f);
        cacheParams.diskCacheSize = DEFAULT_DISK_CACHE_SIZE;

        return cacheParams;
    }

    private ImageCache.ImageCacheParams createDiskOnlyImageCacheParams(Context context) {
        ImageCache.ImageCacheParams cacheParams = createDefaultCacheParams(context);
        cacheParams.memoryCacheEnabled = false;
        cacheParams.diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        return cacheParams;
    }

    private ImageCache.ImageCacheParams createDefaultCacheParams(Context context) {
        return new ImageCache.ImageCacheParams(context.getApplicationContext(), "image");
    }
}
