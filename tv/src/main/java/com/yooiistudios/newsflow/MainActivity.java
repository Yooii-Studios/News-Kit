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

package com.yooiistudios.newsflow;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.widget.ImageCardView;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yooiistudios.newsflow.core.news.DefaultNewsFeedProvider;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.model.news.task.NewsFeedsFetchManager;
import com.yooiistudios.newsflow.model.news.task.NewsImageUrlFetchManager;
import com.yooiistudios.newsflow.reference.R;
import com.yooiistudios.newsflow.reference.Utils;
import com.yooiistudios.newsflow.ui.widget.PicassoImageCardViewTarget;

import java.util.ArrayList;

/*
 * MainActivity
 */
public class MainActivity extends Activity
        implements NewsFeedsFetchManager.OnFetchListener,
        NewsImageUrlFetchManager.OnFetchListener {
    private TextView mLogView;
    private ImageCardView mImageView;
    private PicassoImageCardViewTarget mImageCardViewTarget;

    private ArrayList<NewsFeed> mNewsFeeds;
    private Drawable mDefaultCardImage;

    private boolean mIsImageShowing;

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initVariables();
        initLogView();

        loadContent();
    }

    private void initViews() {
        mLogView = (TextView)findViewById(R.id.textView);
        mImageView = (ImageCardView)findViewById(R.id.imageView);
        mImageCardViewTarget = new PicassoImageCardViewTarget(mImageView);
    }

    private void initVariables() {
        mDefaultCardImage = getResources().getDrawable(R.drawable.movie);
    }

    private void initLogView() {
        mLogView.setMovementMethod(new ScrollingMovementMethod());
    }

    private void loadContent() {
        ArrayList<NewsFeed> feeds = DefaultNewsFeedProvider.getDefaultBottomNewsFeedList(
                getApplicationContext());
        NewsFeedsFetchManager.getInstance().fetch(feeds, this);
    }

    @Override
    public void onFetchAllNewsFeeds(ArrayList<NewsFeed> newsFeeds) {
        mNewsFeeds = newsFeeds;

        StringBuilder builder = new StringBuilder();
        for (NewsFeed newsFeed : mNewsFeeds) {
            builder.append(newsFeed.toString()).append("\n\n");
        }
        mLogView.setText(builder.toString());

        NewsImageUrlFetchManager.getInstance().fetch(mNewsFeeds, this);
    }

    @Override
    public void onFetchImageUrl(News news, String url, int newsFeedPosition, int newsPosition) {
        NewsFeed newsFeed = mNewsFeeds.get(newsFeedPosition);
        newsFeed.getNewsList().get(0).setImageUrl(url);

        if (!mIsImageShowing && url != null && url.length() > 0) {
            Picasso.with(this)
                    .load(url)
                    .resize(Utils.convertDpToPixel(getApplicationContext(), 300),
                            Utils.convertDpToPixel(getApplicationContext(), 300))
                    .error(mDefaultCardImage)
                    .into(mImageCardViewTarget);
            mIsImageShowing = true;
        }
//        mImageView
    }
}
