package com.yooiistudios.newsflow.model;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newsflow.core.util.Display;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 4. 1.
 *
 * NewsImageLoader
 *  Volley ImageLoader 와 ImageCache 를 사용해 뉴스 이미지를 가져옴
 */
public class NewsImageLoader extends CacheImageLoader {
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
        Point imageSize = Display.getDisplaySize(getContext());
        imageSize.y = getContext().getResources().getDimensionPixelSize(R.dimen.detail_top_image_view_height);
        imageSize.x -= imageSize.x % 2;
        imageSize.y -= imageSize.y % 2;
        return imageSize;
    }
}
