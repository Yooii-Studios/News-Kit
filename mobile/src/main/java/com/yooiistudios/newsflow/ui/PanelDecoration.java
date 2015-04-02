package com.yooiistudios.newsflow.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.cache.volley.CacheImageLoader;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 4.
 *
 * PanelImageLoader
 *  메인, 뉴스피드 디테일 색, 이미지 관련 클래스
 */
public class PanelDecoration {
    public interface OnLoadBitmapListener {
        void onLoad(Bitmap bitmap);
    }

//    private static final int RES_DUMMY_IMAGE = R.drawable.img_news_dummy;
//    private static final int RES_SMALL_DUMMY_IMAGE = R.drawable.img_news_dummy_small;

    public static void applySmallDummyNewsImageInto(Context context, CacheImageLoader imageLoader,
                                                    final ImageView imageView) {
        getSmallDummyNewsImageAsync(context, imageLoader, new OnLoadBitmapListener() {
            @Override
            public void onLoad(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    public static void getDummyNewsImageAsync(final Context context,
                                              final CacheImageLoader imageLoader,
                                              final OnLoadBitmapListener listener) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return getDummyImage(context, imageLoader);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (listener != null) {
                    listener.onLoad(bitmap);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getSmallDummyNewsImageAsync(final Context context,
                                                   final CacheImageLoader imageLoader,
                                                   final OnLoadBitmapListener listener) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return getSmallDummyImage(context, imageLoader);
//                return getSmallDummyNewsImage(activity);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (listener != null) {
                    listener.onLoad(bitmap);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static Bitmap getDummyImage(Context context, CacheImageLoader imageLoader) {
        final String key = "dummy";
        Bitmap bitmap = imageLoader.getCache().getBitmap(key);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.img_news_dummy);
            imageLoader.getCache().putBitmap(key, bitmap);
        }
        return bitmap;
    }

    public static Bitmap getSmallDummyImage(Context context, CacheImageLoader imageLoader) {
        final String key = "small_dummy";
        Bitmap bitmap = imageLoader.getCache().getBitmap(key);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.img_news_dummy_small);
            imageLoader.getCache().putBitmap(key, bitmap);
        }
        return bitmap;
    }

    /**
     * Color used to Main Top news image and Dummy image
     */
    public static int getDefaultTopPaletteColor() {
        return Color.argb(127, 16, 16, 16);
    }

    public static int getTopDummyImageFilterColor() {
        return getDefaultTopPaletteColor();
    }

    public static int getPaletteColorWithAlpha(Context context, int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int alpha = context.getResources().getInteger(R.integer.vibrant_color_tint_alpha);
        return Color.argb(alpha, red, green, blue);
    }

    public static int getDefaultBottomPaletteColor(Context context) {
        int grayColor = context.getResources().getColor(R.color.material_blue_grey_500);
        int red = Color.red(grayColor);
        int green = Color.green(grayColor);
        int blue = Color.blue(grayColor);
        int alpha = context.getResources().getInteger(R.integer.vibrant_color_tint_alpha);
        return Color.argb(alpha, red, green, blue);
    }

    public static int getBottomDummyImageFilterColor(Context context) {
        return getDefaultBottomPaletteColor(context);
    }

    public static int getMainBottomDefaultBackgroundColor() {
        return Color.argb(200, 16, 16, 16);
    }
}
