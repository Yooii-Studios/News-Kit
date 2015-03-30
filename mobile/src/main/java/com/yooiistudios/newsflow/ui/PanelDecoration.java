package com.yooiistudios.newsflow.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.model.ResizedImageLoader;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 4.
 *
 * PanelDecoration
 *  메인, 뉴스피드 디테일 색, 이미지 관련 클래스
 */
public class PanelDecoration {
    public interface OnLoadBitmapListener {
        public void onLoad(Bitmap bitmap);
    }

    private static final int RES_DUMMY_IMAGE = R.drawable.img_news_dummy;
    private static final int RES_SMALL_DUMMY_IMAGE = R.drawable.img_news_dummy_small;

//    public static Bitmap getDummyNewsImage(ResizedImageLoader imageLoader) {
//        return ImageResizer.decodeBitmapFromResource(activity.getResources(),
//                RES_DUMMY_IMAGE, SimpleImageCache.getInstance().get(activity));
////        return BitmapFactory.decodeResource(context.getResources(), RES_DUMMY_IMAGE);
//    }

//    public static Bitmap getSmallDummyNewsImage(ResizedImageLoader imageLoader) {
//        return imageLoader.getSmallDummyImage();
//    }

//    public static void applyDummyNewsImageInto(Context context, final ImageView imageView) {
//        getDummyNewsImageAsync(context, new OnLoadBitmapListener() {
//            @Override
//            public void onLoad(Bitmap bitmap) {
//                imageView.setImageBitmap(bitmap);
//            }
//        });
//    }

    public static void applySmallDummyNewsImageInto(ResizedImageLoader imageLoader,
                                                    final ImageView imageView) {
        getSmallDummyNewsImageAsync(imageLoader, new OnLoadBitmapListener() {
            @Override
            public void onLoad(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    public static void getDummyNewsImageAsync(final ResizedImageLoader imageLoader,
                                              final OnLoadBitmapListener listener) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return imageLoader.getDummyImage();
//                return getDummyNewsImage(activity);
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

    public static void getSmallDummyNewsImageAsync(final ResizedImageLoader imageLoader,
                                                   final OnLoadBitmapListener listener) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return imageLoader.getSmallDummyImage();
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

    /**
     * Color used to Main Top news image and Dummy image
     */
    public static int getTopGrayFilterColor() {
        return Color.argb(127, 16, 16, 16);
    }

    public static int getTopDummyImageFilterColor() {
        return getTopGrayFilterColor();
    }

    public static int getBottomGrayFilterColor(Context context) {
        int grayColor = context.getResources().getColor(R.color.material_blue_grey_500);
        int red = Color.red(grayColor);
        int green = Color.green(grayColor);
        int blue = Color.blue(grayColor);
        int alpha = context.getResources().getInteger(R.integer.vibrant_color_tint_alpha);
        return Color.argb(alpha, red, green, blue);
    }

    public static int getBottomDummyImageFilterColor(Context context) {
        return getBottomGrayFilterColor(context);
    }

    public static int getBottomRssNotFoundImgFilterColor(Context context) {
        return context.getResources().getColor(R.color.main_bottom_rss_not_found_img_filter_color);
    }

    public static int getMainBottomDefaultBackgroundColor() {
        return Color.argb(200, 16, 16, 16);
    }
}
