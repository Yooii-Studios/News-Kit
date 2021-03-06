package com.yooiistudios.newskit.core.cache.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.IntDef;
import android.support.v4.app.FragmentActivity;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.newskit.core.news.ImageRequestQueue;
import com.yooiistudios.newskit.core.news.SimpleImageCache;
import com.yooiistudios.newskit.core.ui.RandomMaterialColors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 24.
 *
 * ResizedImageLoader
 *  리사이징을 강제하는 이미지 로더
 */
public abstract class CacheImageLoader<T extends CacheImageLoader.UrlSupplier> {
    public interface ImageListener {
        void onSuccess(CacheImageLoader.ImageResponse response);
        void onFail(VolleyError error);
    }

    @IntDef(value = {TYPE_LARGE, TYPE_THUMBNAIL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    private interface ThumbnailListener {
        void onSuccess(Bitmap resizedBitmap);
    }

    private interface PaletteListener {
        void onSuccess(PaletteColor paletteColor);
    }

    public static final int TYPE_LARGE = 0;
    public static final int TYPE_THUMBNAIL = 1;

    private static final int REQUESTED = 0;
    private static final int CANCEL_REQUESTED = 1;

    private Context mContext;
    private ImageLoader mImageLoader;
    private ImageCache mCache;
    private Map<T, Integer> mRequestedUrlSuppliers = new HashMap<>();
    private boolean mIsDiskOnlyCache;

    protected CacheImageLoader(FragmentActivity activity) {
        mContext = activity.getApplicationContext();
        initImageLoader(activity);
    }

    protected CacheImageLoader(Context context) {
        mContext = context;
        initImageLoaderForBackgroundCache(context);
    }

    private void initImageLoader(FragmentActivity activity) {
        RequestQueue requestQueue =
                ImageRequestQueue.getInstance(activity.getApplicationContext()).getRequestQueue();
        mCache = SimpleImageCache.getInstance().get(activity);
        mImageLoader = new ImageLoader(requestQueue, SimpleImageCache.getInstance().get(activity));
    }

    private void initImageLoaderForBackgroundCache(Context context) {
        RequestQueue requestQueue = ImageRequestQueue.getInstance(context.getApplicationContext())
                .getRequestQueue();
        mCache = SimpleImageCache.getInstance().getNonRetainingDiskOnlyImageCache(context);
        mIsDiskOnlyCache = true;
        mImageLoader = new ImageLoader(requestQueue, mCache);
    }

    public void get(T urlSupplier, ImageListener imageListener) {
        ImageRequest request = new ImageRequest(urlSupplier, CacheImageLoader.TYPE_LARGE);
        get(request, imageListener);
    }

    public void getThumbnail(T urlSupplier, ImageListener imageListener) {
        ImageRequest request = new ImageRequest(urlSupplier, CacheImageLoader.TYPE_THUMBNAIL);
        get(request, imageListener);
    }

    public void cancelRequest(T urlSupplier) {
        if (mRequestedUrlSuppliers.containsKey(urlSupplier)
                && mRequestedUrlSuppliers.get(urlSupplier).equals(REQUESTED)) {
            mRequestedUrlSuppliers.put(urlSupplier, CANCEL_REQUESTED);
        }
    }

    protected abstract Point getImageSize();

    protected Point getThumbnailSize() {
        Point size = new Point(getImageSize());
        size.x /= 2;
        size.y /= 2;

        return size;
    }

    protected Context getContext() {
        return mContext;
    }

    public ImageCache getCache() {
        return mCache;
    }

    private Bitmap getCachedThumbnail(String url) {
        // TODO: possible ui performance improvement
        return mCache.getBitmap(getThumbnailCacheKey(url));
    }

    private void get(final ImageRequest request, final ImageListener imageListener) {
        markRequested(request.urlSupplier);
        if (request.type == CacheImageLoader.TYPE_THUMBNAIL) {
            final Bitmap bitmap = getCachedThumbnail(request.urlSupplier.getUrl());
            if (bitmap != null) {
                getPaletteColors(request.urlSupplier, bitmap, new PaletteListener() {
                    @Override
                    public void onSuccess(PaletteColor paletteColor) {
                        notifyOnSuccess(imageListener,
                                new ImageResponse(request.urlSupplier, bitmap, paletteColor));
                    }
                });
            } else {
                getOriginalImage(request, imageListener);
            }
        } else {
            getOriginalImage(request, imageListener);
        }
    }

    private void getOriginalImage(final ImageRequest request, final ImageListener imageListener) {
        String url = request.urlSupplier.getUrl();

        // com.android.volley.toolbox.ImageLoader.get(~) 메서드에 빈 스트링("")으로 된 url 을
        // 랜덤한 타이밍(AsyncTask 의 onPostExecute )에 여러번 요청하면
        // 콜백이 무시되는 경우가 있음(테스트 결과 20개 요청중 절반 정도가 무시됨).
        // Volley 내부 문제로 추측되지만 확실하지 않음.
        if (TextUtils.isEmpty(url) || hasInvalidUrl(request.urlSupplier)) {
            notifyOnFail(imageListener, request.urlSupplier, null);
            return;
        }
        mImageLoader.get(url, new ImageLoader.ImageListener() {

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                final Bitmap bitmap = response.getBitmap();
                if (bitmap != null) {
                    onGetBitmap(request.urlSupplier);
                    getPaletteColors(request.urlSupplier, bitmap, new PaletteListener() {

                        @Override
                        public void onSuccess(final PaletteColor paletteColor) {
                            if (request.type == CacheImageLoader.TYPE_LARGE) {
                                notifyOnSuccess(imageListener,
                                        new ImageResponse(request.urlSupplier,
                                                bitmap,
                                                paletteColor
                                        ));
                            }
                            cacheThumbnail(bitmap, request, new ThumbnailListener() {
                                @Override
                                public void onSuccess(Bitmap thumbnailBitmap) {
                                    if (request.type == CacheImageLoader.TYPE_THUMBNAIL) {
                                        ImageResponse imageResponse = new ImageResponse(
                                                request.urlSupplier,
                                                thumbnailBitmap,
                                                paletteColor
                                        );
                                        notifyOnSuccess(imageListener, imageResponse);
                                    }
                                    if (mIsDiskOnlyCache) {
                                        bitmap.recycle();
                                        thumbnailBitmap.recycle();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    if (!isImmediate) {
                        notifyOnFail(imageListener, request.urlSupplier, null);
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                notifyOnFail(imageListener, request.urlSupplier, error);
            }
        }, getImageSize().x, getImageSize().y);
    }

    private void getPaletteColors(final T urlSupplier, Bitmap bitmap,
                                  final PaletteListener listener) {
        PaletteColor paletteColor = loadPaletteColor(urlSupplier);
        if (paletteColor != null) {
            listener.onSuccess(paletteColor);
        } else {
            if (!bitmap.isRecycled()) {
                Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        final int darkVibrantColor = palette.getDarkVibrantColor(
                                PaletteColor.FALLBACK_COLOR);
                        PaletteColor paletteColor;
                        if (darkVibrantColor != PaletteColor.FALLBACK_COLOR) {
                            paletteColor = new PaletteColor(darkVibrantColor, PaletteColor.TYPE.GENERATED);
                        } else {
                            // 팔레트가 안 뽑힐 경우 랜덤 팔레트 컬러를 만들어 넣어주게 기획을 변경
                            paletteColor = new PaletteColor(
                                    RandomMaterialColors.get(getContext()), PaletteColor.TYPE.CUSTOM);
                        }
                        savePaletteColor(urlSupplier, paletteColor);
                        listener.onSuccess(paletteColor);
                    }
                });
            }
        }
    }

    protected abstract PaletteColor loadPaletteColor(T urlSupplier);

    protected abstract void savePaletteColor(T urlSupplier, PaletteColor paletteColor);

    protected void onGetBitmap(T urlSupplier) {
        // 이미지를 성공적으로 가져온 경우 sub class 에서 선택적으로 오버라이드해 필요한 처리 수행
    }

    protected void onFailedToGetBitmap(T urlSupplier) {
        // 이미지를 가져오는 데에 실패한 경우 sub class 에서 선택적으로 오버라이드해 필요한 처리 수행
    }

    protected boolean hasInvalidUrl(T urlSupplier) {
        return true;
    }

    private void cacheThumbnail(final Bitmap bitmap, final ImageRequest request,
                                final ThumbnailListener listener) {
        Bitmap thumbnail = getCachedThumbnail(request.urlSupplier.getUrl());
        if (thumbnail != null) {
            listener.onSuccess(thumbnail);
        } else {
            if (shouldCreateThumbnail(bitmap)) {
                double widthRatio = (double)bitmap.getWidth() / (double)getThumbnailSize().x;
                double heightRatio = (double)bitmap.getHeight() / (double)getThumbnailSize().y;

                double ratio = Math.min(widthRatio, heightRatio);

                int targetWidth = (int)(bitmap.getWidth() / ratio);
                int targetHeight = (int)(bitmap.getHeight() / ratio);

                ImageResizer.createScaledBitmap(bitmap, targetWidth, targetHeight,
                        false, false,
                        new ImageResizer.ResizeListener() {
                            @Override
                            public void onResize(Bitmap resizedBitmap) {
                                putThumbnailInCacheAndNotify(resizedBitmap, request, listener);
                            }
                        });
            } else {
                putThumbnailInCacheAndNotify(bitmap, request, listener);
            }
        }
    }

    private void putThumbnailInCacheAndNotify(Bitmap bitmap, ImageRequest request,
                                              ThumbnailListener listener) {
        mCache.putBitmap(getThumbnailCacheKey(request.urlSupplier.getUrl()), bitmap);
        listener.onSuccess(bitmap);
    }

    private boolean shouldCreateThumbnail(Bitmap bitmap) {
        Point thumbnailSize = getThumbnailSize();
        return bitmap.getWidth() > thumbnailSize.x && bitmap.getHeight() > thumbnailSize.y;
    }

    private void notifyOnSuccess(ImageListener listener, ImageResponse response) {
        if (!isCancelRequested(response.urlSupplier)) {
            listener.onSuccess(response);
        }
        markDelivered(response.urlSupplier);
    }

    private void notifyOnFail(ImageListener listener, T urlSupplier, VolleyError error) {
        onFailedToGetBitmap(urlSupplier);
        if (!isCancelRequested(urlSupplier)) {
            listener.onFail(error);
        }
        markDelivered(urlSupplier);
    }

    private boolean isCancelRequested(T supplier) {
        return mRequestedUrlSuppliers.containsKey(supplier)
                && mRequestedUrlSuppliers.get(supplier) == CANCEL_REQUESTED;
    }

    private void markRequested(T supplier) {
        mRequestedUrlSuppliers.put(supplier, REQUESTED);
    }

    private void markDelivered(T supplier) {
        mRequestedUrlSuppliers.remove(supplier);
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

    private class ImageRequest {
        public final T urlSupplier;
        public final @Type int type;

        public ImageRequest(T urlSupplier, @Type int type) {
            this.urlSupplier = urlSupplier;
            this.type = type;
        }
    }

    public class ImageResponse {
        public final T urlSupplier;
        public Bitmap bitmap;
        public final PaletteColor paletteColor;

        public ImageResponse(T urlSupplier, Bitmap bitmap, PaletteColor paletteColor) {
            this.urlSupplier = urlSupplier;
            this.bitmap = bitmap;
            this.paletteColor = paletteColor;
        }
    }

    public static class PaletteColor {
        public static final int FALLBACK_COLOR = Color.TRANSPARENT;

        private final int mPaletteColor;
        private final TYPE mType;

        public PaletteColor(int paletteColor, TYPE type) {
            mPaletteColor = paletteColor;
            mType = type;
        }

        public int getPaletteColor() {
            return mPaletteColor;
        }

        public TYPE getType() {
            return mType;
        }

        public boolean isCustom() {
            return mType.equals(TYPE.CUSTOM);
        }

        public enum TYPE {
            GENERATED,      // 이미지에서 색상을 추출 성공
            CUSTOM          // 그러지 못해 임의로 색을 정함
        }
    }

    public interface UrlSupplier {
        String getUrl();

        @Override
        int hashCode();

        @Override
        boolean equals(Object o);
    }
}
