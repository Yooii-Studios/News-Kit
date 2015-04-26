package com.yooiistudios.newskit.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 5.
 *
 * MainImageCardViewTarget
 *  메인 액티비티의 뉴스 이미지 표시에 사용될 타겟
 */
public class MainImageCardViewTarget implements Target {
    private Context mContext;
    private ImageCardView mImageCardView;
    private boolean mHasValidImage = false;

    public MainImageCardViewTarget(ImageCardView imageCardView) {
        mImageCardView = imageCardView;
        mContext = imageCardView.getContext();
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
        mImageCardView.setMainImage(bitmapDrawable);
        mHasValidImage = true;
    }

    @Override
    public void onBitmapFailed(Drawable drawable) {
        mImageCardView.setMainImage(drawable);
    }

    @Override
    public void onPrepareLoad(Drawable drawable) {
        mImageCardView.setMainImage(drawable);
    }

    public boolean hasValidImage() {
        return mHasValidImage;
    }
}
