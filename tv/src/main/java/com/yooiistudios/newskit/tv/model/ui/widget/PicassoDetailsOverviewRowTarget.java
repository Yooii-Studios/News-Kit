package com.yooiistudios.newskit.tv.model.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.DetailsOverviewRow;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 14.
 */
public class PicassoDetailsOverviewRowTarget implements Target {
    private Context mContext;
    private DetailsOverviewRow mRow;

    public PicassoDetailsOverviewRowTarget(Context context, DetailsOverviewRow row) {
        mContext = context;
        mRow = row;
    }
    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        mRow.setImageBitmap(mContext, bitmap);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        mRow.setImageDrawable(errorDrawable);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        mRow.setImageDrawable(placeHolderDrawable);
    }
}
