package com.yooiistudios.newskit.core.cache.volley;

import com.yooiistudios.newskit.core.cache.AsyncTask;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * CacheAsyncTask
 *  모든 뉴스피드, 뉴스 이미지 캐싱을 담당하는 클래스
 */
public class CacheAsyncTask extends AsyncTask<Object, Void, Void> {
    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;

    private ImageCache mImageCache;

    public CacheAsyncTask(ImageCache imageCache) {
        mImageCache = imageCache;
    }

    public static void initDiskCache(ImageCache imageCache) {
        new CacheAsyncTask(imageCache).execute(MESSAGE_INIT_DISK_CACHE);
    }

    public static void clearCache(ImageCache imageCache) {
        new CacheAsyncTask(imageCache).execute(MESSAGE_CLEAR);
    }

    public static void flushCache(ImageCache imageCache) {
        new CacheAsyncTask(imageCache).execute(MESSAGE_FLUSH);
    }

    public static void closeCache(ImageCache imageCache) {
        new CacheAsyncTask(imageCache).execute(MESSAGE_CLOSE);
    }

    @Override
    protected Void doInBackground(Object... params) {
        switch ((Integer)params[0]) {
            case MESSAGE_CLEAR:
                clearCacheInternal();
                break;
            case MESSAGE_INIT_DISK_CACHE:
                initDiskCacheInternal();
                break;
            case MESSAGE_FLUSH:
                flushCacheInternal();
                break;
            case MESSAGE_CLOSE:
                closeCacheInternal();
                break;
        }
        return null;
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }
}
