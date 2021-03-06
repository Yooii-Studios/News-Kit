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

package com.yooiistudios.newskit.tv.ui.presenter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.util.DipToPixel;
import com.yooiistudios.newskit.tv.R;
import com.yooiistudios.newskit.tv.ui.widget.MainImageCardViewTarget;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    // 33 * 16 / 33 * 10 / 16:9 비율
    private static int CARD_WIDTH = 528;
    private static int CARD_HEIGHT = 297;

    private Context mContext;
    private Drawable mErrorCardImage;

    public CardPresenter(Context context) {
        super();
        mContext = context;
        mErrorCardImage = context.getResources().getDrawable(R.drawable.news_dummy);
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent) {
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(mContext.getResources().getColor(R.color.card_background));
        cardView.setInfoAreaBackgroundColor(
                mContext.getResources().getColor(R.color.card_info_background));

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

    }

    private void applyNewsInfo(NewsViewHolder viewHolder, News news) {
        ImageCardView imageCardView = viewHolder.imageCardView;
        imageCardView.setTitleText(news.getTitle());
        imageCardView.setContentText(news.getDisplayableElapsedTimeSincePubDate(mContext));
//        imageCardView.setContentText(news.getNewsContent().getVideoUrl());
        imageCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
    }

    private void applyImage(NewsViewHolder newsViewHolder, News news) {
        if (news.hasImageUrl()) {
            Context context = newsViewHolder.imageCardView.getContext();
            Picasso.with(context)
                    .load(news.getImageUrl())
                    .resize(DipToPixel.dpToPixel(context, CARD_WIDTH),
                            DipToPixel.dpToPixel(context, CARD_HEIGHT))
                    .centerCrop()
                    .into(newsViewHolder.picassoTarget);
        } else if (news.isImageUrlChecked()) {
            newsViewHolder.imageCardView.setMainImage(mErrorCardImage);
        } else {
            newsViewHolder.imageCardView.setMainImage(null);
        }
    }

    public class NewsViewHolder extends Presenter.ViewHolder {
        public ImageCardView imageCardView;
        public MainImageCardViewTarget picassoTarget;

        public NewsViewHolder(View view) {
            super(view);
            imageCardView = (ImageCardView) view;
            picassoTarget = new MainImageCardViewTarget(imageCardView);
        }
    }
}
