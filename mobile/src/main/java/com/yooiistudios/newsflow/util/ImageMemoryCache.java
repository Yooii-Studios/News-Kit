/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yooiistudios.newsflow.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

import java.util.Map;

/**
 * An image memory cache implementation for Volley which allows the retrieval of entires via a URL.
 * Volley internally inserts items with a key which is a combination of a the size of the image,
 * and the url.
 *
 */
public class ImageMemoryCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
    private static final String TAG = ImageMemoryCache.class.getName();

    /**
     * Singleton instance which has it's maximum size set to be 1/8th of the allowed memory size.
     */
    private static ImageMemoryCache instance;
    private static final int DISK_CACHE_SIZE_MB = 50 * 1024 * 1024;

//    private DiskLruImageCache mDiskLruImageCache;
    private Map<String, Bitmap> mLastSnapshot;// Cache the last created snapshot

    public static ImageMemoryCache getInstance(Context context) {
        if (instance == null) {
            instance = new ImageMemoryCache(context, DISK_CACHE_SIZE_MB,
                    (int)(Runtime.getRuntime().maxMemory() / 8));
        }

        return instance;
    }

    private ImageMemoryCache(Context context, int diskCacheSize,
                             int memoryCacheSize) {
        super(memoryCacheSize);
//        mDiskLruImageCache = new DiskLruImageCache(context,
//                context.getPackageCodePath(), diskCacheSize,
//                Bitmap.CompressFormat.PNG, 100);
    }

//    public Bitmap getBitmapFromUrl(String url) {
//        // If we do not have a snapshot to use, generate one
//        if (mLastSnapshot == null) {
//            mLastSnapshot = snapshot();
//        }
//
//        Bitmap bitmap = null;
//        // Iterate through the snapshot to find any entries which match our url
//        for (Map.Entry<String, Bitmap> entry : mLastSnapshot.entrySet()) {
//            if (url.equals(extractUrl(entry.getKey()))) {
//                // We've found an entry with the same url, return the bitmap
//                bitmap = entry.getValue();
//            }
//        }
//        if (bitmap != null) {
//            return bitmap;
//        } else {
////            bitmap = getBitmapFromDiskCache(url);
//        }
//
//        return bitmap;
//    }

    @Override
    public Bitmap getBitmap(String key) {

        Bitmap bitmap;
        if ((bitmap = get(key)) != null) {
//            NLLog.i(TAG, "Memory get succeed. url : " + key);
        } else {
//            NLLog.i(TAG, "Memory get failed. url : " + key);
//            bitmap = getBitmapFromDiskCache(key);
        }

        return bitmap;
    }

//    public synchronized Bitmap getBitmapFromDiskCache(String key) {
//        Bitmap bitmap = null;
//        String diskKey = getDiskCacheKey(key);
//        try {
//            bitmap = mDiskLruImageCache.getBitmap(diskKey);
//        } catch (IllegalArgumentException e) {
//            NLLog.i(TAG, "Disk get failed. exception : " + diskKey);
//
//            return null;
//        }
//        if (bitmap != null) {
//            NLLog.i(TAG, "Disk get succeed. url : " + diskKey);
//        } else {
//            NLLog.i(TAG, "Disk get failed. url : " + diskKey);
//        }
//
//        return bitmap;
//    }

    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        put(key, bitmap);
//        NLLog.i(TAG, "Memory put succeed. url : " + key);
//        new NLImageDiskCacheSaveTask(this, key, bitmap).executeOnExecutor(
//                AsyncTask.THREAD_POOL_EXECUTOR);

        // An entry has been added, so invalidate the snapshot
        mLastSnapshot = null;
    }
//    public synchronized void putBitmapIntoDiskCache(String key, Bitmap bitmap) {
//        String diskKey = getDiskCacheKey(key);
//        try {
//            mDiskLruImageCache.putBitmap(diskKey, bitmap);
//            NLLog.i(TAG, "Disk put succeed. url : " + diskKey);
//        } catch (IllegalArgumentException e) {
//            NLLog.i(TAG, "Disk put failed. url : " + diskKey);
////            e.printStackTrace();
//        }
//    }

    @Override
    protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);

        // An entry has been removed, so invalidate the snapshot
        mLastSnapshot = null;
    }

    private static String extractUrl(String key) {
        return key.substring(key.indexOf("http"));
    }
    private static String getDiskCacheKey(String key) {
        return String.valueOf(extractUrl(key).hashCode());
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
//        return value.getAllocationByteCount();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Workaround for KitKat initial release NPE in Bitmap, fixed in MR1. See issue #148.
            try {
                return value.getAllocationByteCount();
            } catch (NullPointerException e) {
                // Do nothing.
            }
        }
        return value.getHeight() * value.getRowBytes();
    }
}
