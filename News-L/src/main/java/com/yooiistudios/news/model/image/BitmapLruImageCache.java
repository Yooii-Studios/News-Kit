package com.yooiistudios.news.model.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.yooiistudios.news.util.log.NLLog;

/**
 * Basic LRU Memory cache.
 * 
 * @author Trey Robinson
 *
 */
public class BitmapLruImageCache extends LruCache<String, Bitmap> implements ImageCache {

	private static final String TAG = BitmapLruImageCache.class.getName();
    private static final int DISK_CACHE_SIZE_MB = 50 * 1024 * 1024;

    private static BitmapLruImageCache instance;

    private DiskLruImageCache mDiskLruImageCache;

    public static BitmapLruImageCache getInstance(Context context) {
        if (instance == null) {
            instance = new BitmapLruImageCache(context, DISK_CACHE_SIZE_MB,
                    (int)(Runtime.getRuntime().maxMemory() / 8));
        }

        return instance;
    }
	
	private BitmapLruImageCache(Context context, int diskCacheSize,
                               int memoryCacheSize) {
		super(memoryCacheSize);
        mDiskLruImageCache = new DiskLruImageCache(context,
                context.getPackageCodePath(), diskCacheSize,
                Bitmap.CompressFormat.PNG, 100);
	}
	
	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getRowBytes() * value.getHeight();
	}
	
	@Override
	public Bitmap getBitmap(String url) {
		NLLog.i(TAG, "Retrieved item from Mem Cache");

        Bitmap bitmap = get(url);
        if (bitmap == null) {
            NLLog.i(TAG, "Memory get failed. url : " + url);
            try {
                bitmap = mDiskLruImageCache.getBitmap(url);
                NLLog.i(TAG, "Disk get succeed. url : " + url);
            } catch (IllegalArgumentException e) {
                NLLog.i(TAG, "Disk get failed. url : " + url);
//                e.printStackTrace();

                return null;
            }
        }
        NLLog.i(TAG, "Memory get succeed. url : " + url);

		return bitmap;
	}
 
	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		NLLog.i(TAG, "Added item to Mem Cache");
		if (put(url, bitmap) != null) {
            NLLog.i(TAG, "Memory put succeed. url : " + url);
        } else {
            NLLog.i(TAG, "Memory put failed. url : " + url);
        }
        try {
            mDiskLruImageCache.putBitmap(url, bitmap);
            NLLog.i(TAG, "Disk put succeed. url : " + url);
        } catch (IllegalArgumentException e) {
            NLLog.i(TAG, "Disk put failed. url : " + url);
//            e.printStackTrace();
        }
	}
}
