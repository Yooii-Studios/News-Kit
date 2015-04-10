package com.yooiistudios.newsflow.model.cache;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newsflow.core.news.database.NewsDb;
import com.yooiistudios.newsflow.core.util.Device;
import com.yooiistudios.newsflow.core.util.Display;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 4. 1.
 *
 * NewsImageLoader
 *  Volley ImageLoader 와 ImageCache 를 사용해 뉴스 이미지를 가져옴
 */
public class NewsImageLoader extends CacheImageLoader<NewsUrlSupplier> {
    public NewsImageLoader(FragmentActivity activity) {
        super(activity);
    }

    public NewsImageLoader(Context context) {
        super(context);
    }

    public static NewsImageLoader create(FragmentActivity activity) {
        return new NewsImageLoader(activity);
    }

    public static NewsImageLoader createWithNonRetainingCache(Context context) {
        return new NewsImageLoader(context);
    }

    @Override
    protected Point getImageSize() {
        // 무조건 세로 기준에서 이미지 사이즈를 얻어냄
        Point imageSize = Display.getDisplaySizeOnPortrait(getContext());
        imageSize.y = getContext().getResources().getDimensionPixelSize(
                R.dimen.detail_top_image_view_height_port);
        if (Device.hasLollipop()) {
            imageSize.y += Display.getStatusBarHeight(getContext());
        }
        imageSize.x -= imageSize.x % 2;
        imageSize.y -= imageSize.y % 2;
        return imageSize;
    }

    @Override
    protected PaletteColor loadPaletteColor(NewsUrlSupplier urlSupplier) {
        return NewsDb.getInstance(getContext()).loadPaletteColor(
                urlSupplier.getNewsFeedPosition(), urlSupplier.getGuid());
    }

    @Override
    protected void savePaletteColor(NewsUrlSupplier urlSupplier, PaletteColor paletteColor) {
        NewsDb.getInstance(getContext()).savePaletteColor(
                urlSupplier.getNewsFeedPosition(), urlSupplier.getGuid(), paletteColor);
    }
}