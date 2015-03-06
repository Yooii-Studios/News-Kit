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

package com.yooiistudios.newsflow.reference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.util.DipToPixel;
import com.yooiistudios.newsflow.ui.widget.PicassoImageCardViewTarget;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";
    private static int CARD_WIDTH = 380;
    private static int CARD_HEIGHT = 180;

    private Context mContext;
    private Drawable mDefaultCardImage;

    public CardPresenter(Context context) {
        super();
        mContext = context;
        mDefaultCardImage = context.getResources().getDrawable(R.drawable.movie);
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(mContext.getResources().getColor(R.color.fastlane_background));
        return new NewsViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        News news = (News)item;

        NewsViewHolder newsViewHolder = (NewsViewHolder)viewHolder;

        applyNewsInfo(newsViewHolder, news);
        applyImage(newsViewHolder, news);
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        // Do nothing.
    }

    private void applyNewsInfo(NewsViewHolder viewHolder, News news) {
        ImageCardView imageCardView = viewHolder.cardView;
        imageCardView.setTitleText(news.getTitle());
        imageCardView.setContentText(news.getDescription());
        imageCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
    }

    private void applyImage(NewsViewHolder newsViewHolder, News news) {
        String imageUrl = news.getImageUrl();
        if (imageUrl != null && imageUrl.length() > 0) {
            Context context = newsViewHolder.cardView.getContext();
            Picasso.with(context)
                    .load(imageUrl)
//                    .resize(Utils.convertDpToPixel(context, CARD_WIDTH),
//                            Utils.convertDpToPixel(context, CARD_HEIGHT))
                    .resize(DipToPixel.dpToPixel(context, CARD_WIDTH),
                            DipToPixel.dpToPixel(context, CARD_HEIGHT))
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(newsViewHolder.picassoTarget);
        }
    }

    private class NewsViewHolder extends Presenter.ViewHolder {
        public ImageCardView cardView;
        public PicassoImageCardViewTarget picassoTarget;

        public NewsViewHolder(View view) {
            super(view);
            cardView = (ImageCardView) view;
            picassoTarget = new PicassoImageCardViewTarget(cardView);
        }

    }
}
