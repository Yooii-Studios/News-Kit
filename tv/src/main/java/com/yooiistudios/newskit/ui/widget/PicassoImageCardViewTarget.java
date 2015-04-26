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
 */
public class PicassoImageCardViewTarget implements Target {
    private Context mContext;
    private ImageCardView mImageCardView;

    public PicassoImageCardViewTarget(ImageCardView imageCardView) {
        mImageCardView = imageCardView;
        mContext = imageCardView.getContext();
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
        mImageCardView.setMainImage(bitmapDrawable);
    }

    @Override
    public void onBitmapFailed(Drawable drawable) {
        mImageCardView.setMainImage(drawable);
    }

    @Override
    public void onPrepareLoad(Drawable drawable) {
        mImageCardView.setMainImage(drawable);
    }
}
