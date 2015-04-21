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
    public interface OnApplyImageListener {
        void onApply();
    }
    public interface OnLoadBitmapListener {
        void onLoad(Bitmap bitmap);
    }

    private static final Decoration DECO_DUMMY;
    private static final Decoration DECO_SMALL_DUMMY;
    private static final Decoration DECO_RSS_FAILED_BACKGROUND;
    private static final Decoration DECO_RSS_FAILED_BACKGROUND_SMALL;

    static {
        DECO_DUMMY = new Decoration(
                "dummy",
                R.drawable.img_news_dummy
        );
        DECO_SMALL_DUMMY = new Decoration(
                "small_dummy",
                R.drawable.img_news_dummy_small
        );
        DECO_RSS_FAILED_BACKGROUND = new Decoration(
                "rss_url_failed_background",
                R.drawable.img_rss_url_failed
        );
        DECO_RSS_FAILED_BACKGROUND_SMALL = new Decoration(
                "small_rss_url_failed_background",
                R.drawable.img_rss_url_failed_small
        );
    }

    public static void applySmallDummyNewsImageInto(Context context, NewsImageLoader imageLoader,
                                                    ImageView imageView,
                                                    OnApplyImageListener listener) {
        applyImageInto(context, imageLoader, imageView, listener, DECO_SMALL_DUMMY);
    }

    public static void applyRssUrlFailedBackgroundInto(Context context, NewsImageLoader imageLoader,
                                                       ImageView imageView,
                                                       OnApplyImageListener listener) {
        applyImageInto(context, imageLoader, imageView, listener, DECO_RSS_FAILED_BACKGROUND);
    }

    public static void applyRssUrlFailedSmallBackgroundInto(Context context, NewsImageLoader imageLoader,
                                                            ImageView imageView,
                                                            OnApplyImageListener listener) {
        applyImageInto(context, imageLoader, imageView, listener, DECO_RSS_FAILED_BACKGROUND_SMALL);
    }

    private static void applyImageInto(Context context, NewsImageLoader imageLoader,
                                       final ImageView imageView,
                                       final OnApplyImageListener listener,
                                       Decoration decoration) {
        getImageAsync(context, imageLoader, new OnLoadBitmapListener() {
            @Override
            public void onLoad(Bitmap bitmap) {
                applyImageAndNotify(bitmap, imageView, listener);
            }
        }, decoration);
    }

    private static void applyImageAndNotify(Bitmap bitmap, ImageView imageView, OnApplyImageListener listener) {
        imageView.setImageBitmap(bitmap);

        if (listener != null) {
            listener.onApply();
        }
    }

    public static void getDummyNewsImageAsync(final Context context,
                                              final NewsImageLoader imageLoader,
                                              final OnLoadBitmapListener listener) {
        getImageAsync(context, imageLoader, listener, DECO_DUMMY);
    }

    public static void getSmallDummyNewsImageAsync(final Context context,
                                                   final NewsImageLoader imageLoader,
                                                   final OnLoadBitmapListener listener) {
        getImageAsync(context, imageLoader, listener, DECO_SMALL_DUMMY);
    }

    public static void getRssUrlFailedBackgroundAsync(final Context context,
                                                           final NewsImageLoader imageLoader,
                                                           final OnLoadBitmapListener listener) {
        getImageAsync(context, imageLoader, listener, DECO_RSS_FAILED_BACKGROUND);
    }

    public static void getRssUrlFailedSmallBackgroundAsync(final Context context,
                                                           final NewsImageLoader imageLoader,
                                                           final OnLoadBitmapListener listener) {
        getImageAsync(context, imageLoader, listener, DECO_RSS_FAILED_BACKGROUND_SMALL);
    }

    private static void getImageAsync(final Context context,
                                      final NewsImageLoader imageLoader,
                                      final OnLoadBitmapListener listener,
                                      final Decoration decoration) {
        Bitmap cachedBitmap = getImageFromCache(context, imageLoader, decoration);
        if (cachedBitmap != null) {
            listener.onLoad(cachedBitmap);
        } else {
            new DecodeResourceAsync(listener) {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return getImage(context, imageLoader, decoration);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static Bitmap getDummyImage(Context context, NewsImageLoader imageLoader) {
        return getImage(context, imageLoader, DECO_DUMMY);
    }

    public static Bitmap getSmallDummyImage(Context context, NewsImageLoader imageLoader) {
        return getImage(context, imageLoader, DECO_SMALL_DUMMY);
    }

    public static Bitmap getRssUrlFailedBackground(Context context, NewsImageLoader imageLoader) {
        return getImage(context, imageLoader, DECO_RSS_FAILED_BACKGROUND);
    }

    public static Bitmap getRssUrlFailedSmallBackground(Context context, NewsImageLoader imageLoader) {
        return getImage(context, imageLoader, DECO_RSS_FAILED_BACKGROUND_SMALL);
    }

    private static Bitmap getImage(Context context, NewsImageLoader imageLoader, Decoration decoration) {
        Bitmap bitmap = getImageFromCache(context, imageLoader, decoration);
        if (bitmap == null) {
            bitmap = createAndCacheResource(context, imageLoader, decoration);
        }
        return bitmap;
    }

    private static Bitmap getImageFromCache(Context context, NewsImageLoader imageLoader, Decoration decoration) {
        String keyWithVersionCode = getKeyWithVersionCode(context, decoration.key);
        return imageLoader.getCache().getBitmap(keyWithVersionCode);
    }

    private static Bitmap createAndCacheResource(Context context, NewsImageLoader imageLoader,
                                                 Decoration decoration) {
        String keyWithVersionCode = getKeyWithVersionCode(context, decoration.key);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), decoration.resId);
        imageLoader.getCache().putBitmap(keyWithVersionCode, bitmap);

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

    public static int getRandomPaletteColorWithAlpha(Context context, int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int alpha = context.getResources().getInteger(R.integer.bottom_default_palette_color_alpha);
        return Color.argb(alpha, red, green, blue);
    }

    /*
    // 테스트 용도로 사용하자. 이걸 사용하고 바텀에서 이걸 사용한 패널에 에딧 모드를 할 경우 색이 계속 바뀌기에
    // 어떤 패널이 랜덤컬러를 사용하고 있는지 확인할 수 있어서 알파값 비교하는데 용이할 것으로 생각
    public static int getDefaultBottomPaletteColor(Context context) {
        int paletteColor = RandomMaterialColors.get(context);
        int red = Color.red(paletteColor);
        int green = Color.green(paletteColor);
        int blue = Color.blue(paletteColor);
        int alpha = context.getResources().getInteger(R.integer.bottom_default_palette_color_alpha);
        return Color.argb(alpha, red, green, blue);
    }
    */

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

    private static class Decoration {
        public final String key;
        public final int resId;

        public Decoration(String key, int resId) {
            this.key = key;
            this.resId = resId;
        }
    }
}
