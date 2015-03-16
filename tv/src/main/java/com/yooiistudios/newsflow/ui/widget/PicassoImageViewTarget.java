/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.yooiistudios.newsflow.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Picasso target for updating default_background images
 */
public class PicassoImageViewTarget implements Target {
    private Context mContext;
    private ImageView mImageView;

    public PicassoImageViewTarget(ImageView imageView) {
        mImageView = imageView;
        mContext = imageView.getContext();
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
        mImageView.setImageDrawable(bitmapDrawable);
    }

    @Override
    public void onBitmapFailed(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }

    @Override
    public void onPrepareLoad(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }
}
