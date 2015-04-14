package com.yooiistudios.newsflow.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.util.AppInfo;
import com.yooiistudios.newsflow.model.cache.NewsImageLoader;

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

    public static void applySmallDummyNewsImageInto(Context context, NewsImageLoader imageLoader,
                                                    final ImageView imageView) {
        getSmallDummyNewsImageAsync(context, imageLoader, new OnLoadBitmapListener() {
            @Override
            public void onLoad(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    public static void applyRssUrlFailedBackgroundInto(Context context, NewsImageLoader imageLoader,
                                                            final ImageView imageView) {
        getRssUrlFailedBackgroundAsync(context, imageLoader, new OnLoadBitmapListener() {
            @Override
            public void onLoad(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    public static void applyRssUrlFailedSmallBackgroundInto(Context context, NewsImageLoader imageLoader,
                                                            final ImageView imageView) {
        getRssUrlFailedSmallBackgroundAsync(context, imageLoader, new OnLoadBitmapListener() {
            @Override
            public void onLoad(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    public static void getDummyNewsImageAsync(final Context context,
                                              final NewsImageLoader imageLoader,
                                              final OnLoadBitmapListener listener) {
        new DecodeResourceAsync(listener) {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return getDummyImage(context, imageLoader);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getSmallDummyNewsImageAsync(final Context context,
                                                   final NewsImageLoader imageLoader,
                                                   final OnLoadBitmapListener listener) {
        new DecodeResourceAsync(listener) {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return getSmallDummyImage(context, imageLoader);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getRssUrlFailedBackgroundAsync(final Context context,
                                                           final NewsImageLoader imageLoader,
                                                           final OnLoadBitmapListener listener) {
        new DecodeResourceAsync(listener) {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return getRssUrlFailedBackground(context, imageLoader);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getRssUrlFailedSmallBackgroundAsync(final Context context,
                                                           final NewsImageLoader imageLoader,
                                                           final OnLoadBitmapListener listener) {
        new DecodeResourceAsync(listener) {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return getRssUrlFailedSmallBackground(context, imageLoader);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static Bitmap getDummyImage(Context context, NewsImageLoader imageLoader) {
        final String key = getKeyWithVersionCode(context, "dummy");
        Bitmap bitmap = imageLoader.getCache().getBitmap(key);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.img_news_dummy);
            imageLoader.getCache().putBitmap(key, bitmap);
        }
        return bitmap;
    }

    public static Bitmap getSmallDummyImage(Context context, NewsImageLoader imageLoader) {
        final String key = getKeyWithVersionCode(context, "small_dummy");
        Bitmap bitmap = imageLoader.getCache().getBitmap(key);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.img_news_dummy_small);
            imageLoader.getCache().putBitmap(key, bitmap);
        }
        return bitmap;
    }

    public static Bitmap getRssUrlFailedBackground(Context context, NewsImageLoader imageLoader) {
        final String key = getKeyWithVersionCode(context, "rss_url_failed_background");
        Bitmap bitmap = imageLoader.getCache().getBitmap(key);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.img_rss_url_failed);
            imageLoader.getCache().putBitmap(key, bitmap);
        }
        return bitmap;
    }

    public static Bitmap getRssUrlFailedSmallBackground(Context context, NewsImageLoader imageLoader) {
        final String key = getKeyWithVersionCode(context, "small_rss_url_failed_background");
        Bitmap bitmap = imageLoader.getCache().getBitmap(key);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.img_rss_url_failed_small);
            imageLoader.getCache().putBitmap(key, bitmap);
        }
        return bitmap;
    }

    private static String getKeyWithVersionCode(Context context, String key) {
        return key + "_" + AppInfo.getVersionCode(context);
    }

    /**
     * Color used to Main Top news image and Dummy image
     */
    public static int getDefaultTopPaletteColor() {
//        return Color.argb(127, 16, 16, 16);
        return Color.argb(100, 16, 16, 16);
    }

    public static int getTopDummyImageFilterColor() {
        return getDefaultTopPaletteColor();
    }

    public static int getPaletteColorWithAlpha(Context context, int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int alpha = context.getResources().getInteger(R.integer.palette_color_alpha);
        return Color.argb(alpha, red, green, blue);
    }

    public static int getDefaultBottomPaletteColor(Context context) {
        int paletteColor = RandomMaterialColors.get(context);
        int red = Color.red(paletteColor);
        int green = Color.green(paletteColor);
        int blue = Color.blue(paletteColor);
        int alpha = context.getResources().getInteger(R.integer.bottom_default_palette_color_alpha);
        return Color.argb(alpha, red, green, blue);
    }

    //
    public static int getBottomDummyImageFilterColor(Context context) {
        int grayColor = context.getResources().getColor(R.color.material_brown_700);
        int red = Color.red(grayColor);
        int green = Color.green(grayColor);
        int blue = Color.blue(grayColor);
        int alpha = context.getResources().getInteger(R.integer.bottom_dummy_palette_color_alpha);
        return Color.argb(alpha, red, green, blue);
    }

//    public static int getMainBottomDefaultBackgroundColor() {
//        return Color.argb(200, 16, 16, 16);
//    }

    private static abstract class DecodeResourceAsync extends AsyncTask<Void, Void, Bitmap> {
        private OnLoadBitmapListener mListener;

        public DecodeResourceAsync(
                final OnLoadBitmapListener listener) {
            mListener = listener;
        }

        @Override
        protected abstract Bitmap doInBackground(Void... params);

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (mListener != null) {
                mListener.onLoad(bitmap);
            }
        }
    }
}
