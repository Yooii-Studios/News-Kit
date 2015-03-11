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

package com.yooiistudios.newsflow.ui.presenter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.util.DipToPixel;
import com.yooiistudios.newsflow.ui.widget.PicassoImageCardViewTarget;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static int CARD_WIDTH = 380;
    private static int CARD_HEIGHT = 180;

    private Context mContext;
    private Drawable mErrorCardImage;

    public CardPresenter(Context context) {
        super();
        mContext = context;
        mErrorCardImage = context.getResources().getDrawable(R.drawable.ic_launcher);
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent) {
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(mContext.getResources().getColor(R.color.detail_background_dark));

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
//        imageCardView.setContentText(news.getDescription());
        String url = news.getImageUrl();
        String message = url != null ? url : "no url";
        imageCardView.setContentText(url);
//        imageCardView.setContentText("53 min before");
        imageCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
    }

    private void applyImage(NewsViewHolder newsViewHolder, News news) {
        String imageUrl = news.getImageUrl();
        if (isImageUrlValid(imageUrl)) {
            Context context = newsViewHolder.imageCardView.getContext();
            Picasso.with(context)
                    .load(imageUrl)
//                    .resize(Utils.convertDpToPixel(context, CARD_WIDTH),
//                            Utils.convertDpToPixel(context, CARD_HEIGHT))
                    .resize(DipToPixel.dpToPixel(context, CARD_WIDTH),
                            DipToPixel.dpToPixel(context, CARD_HEIGHT))
                    .centerCrop()
                    .error(mErrorCardImage)
                    .into(newsViewHolder.picassoTarget);
        } else {
            newsViewHolder.imageCardView.setMainImage(null);
        }
    }

    private boolean isImageUrlValid(String imageUrl) {
        return imageUrl != null && imageUrl.length() > 0;
    }

    private class NewsViewHolder extends Presenter.ViewHolder {
        public ImageCardView imageCardView;
        public PicassoImageCardViewTarget picassoTarget;

        public NewsViewHolder(View view) {
            super(view);
            imageCardView = (ImageCardView) view;
            picassoTarget = new PicassoImageCardViewTarget(imageCardView);
        }
    }
}
