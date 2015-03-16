package com.yooiistudios.newsflow.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.squareup.picasso.Picasso;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 15.
 *
 * BitmapLoadTask
 *  Url 에서 비트맵 불러오는 태스크
 */

public class BitmapLoadTask extends AsyncTask<Void, Void, Drawable> {
    public interface OnSuccessListener {
        public void onLoad(Drawable drawable);
    }
    private Context mContext;
    private String mUrl;
    private int mWidth;
    private int mHeight;
    private int mDefaultResId;
    private OnSuccessListener mListener;

    public BitmapLoadTask(Context context, String url, int width, int height,
                          int defaultResId, OnSuccessListener listener) {
        mContext = context;
        mUrl = url;
        mWidth = width;
        mHeight = height;
        mDefaultResId = defaultResId;
        mListener = listener;
    }

    @Override
    protected Drawable doInBackground(Void... params) {
        Drawable drawable;
        try {
            Bitmap bitmap = Picasso.with(mContext)
                    .load(mUrl)
                    .resize(mWidth, mHeight)
                    .centerCrop()
                    .error(createDefaultImage())
                    .get();
            drawable = new BitmapDrawable(mContext.getResources(), bitmap);
        } catch (Exception e) {
            drawable = createDefaultImage();
        }

        return drawable;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        if (mListener != null && !isCancelled()) {
            mListener.onLoad(drawable);
        }
    }

    private Drawable createDefaultImage() {
        return mContext.getResources().getDrawable(mDefaultResId);
    }
}
