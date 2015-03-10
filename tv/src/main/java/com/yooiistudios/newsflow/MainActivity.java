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
import android.content.Context;
import android.os.Bundle;

import com.yooiistudios.newsflow.core.news.DefaultNewsFeedProvider;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsContent;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.database.NewsDb;
import com.yooiistudios.newsflow.core.news.util.NewsFeedValidator;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.newsflow.core.util.NLLog;
import com.yooiistudios.newsflow.model.news.task.NewsContentFetchManager;
import com.yooiistudios.newsflow.model.news.task.NewsFeedsFetchManager;
import com.yooiistudios.newsflow.reference.R;

import java.util.ArrayList;

/*
 * MainActivity
 */
public class MainActivity extends Activity
        implements NewsFeedsFetchManager.OnFetchListener,
        NewsContentFetchManager.OnFetchListener {

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadContent();
    }

    private void loadContent() {
        Context context = getApplicationContext();

        PanelMatrix panelMatrix = PanelMatrixUtils.getCurrentPanelMatrix(context);

        NewsFeed topNewsFeed = NewsDb.getInstance(context).loadTopNewsFeed(context);
        ArrayList<NewsFeed> bottomNewsFeeds = NewsDb.getInstance(context).loadBottomNewsFeedList(
                context, panelMatrix.getPanelCount());

        if (NewsFeedValidator.isValid(topNewsFeed) && NewsFeedValidator.isValid(bottomNewsFeeds)) {
            NLLog.i("Archive", "Archive exists. Show.");
            applyNewsFeeds(topNewsFeed, bottomNewsFeeds);
        } else {
            NLLog.i("Archive", "Archive does not exists. Fetch.");
            fetchDefaultNewsFeeds(context);
        }
    }

    private void fetchDefaultNewsFeeds(Context context) {
        NewsFeed defaultTopNewsFeed = DefaultNewsFeedProvider.getDefaultTopNewsFeed(context);
        ArrayList<NewsFeed> defaultBottomNewsFeeds = DefaultNewsFeedProvider
                .getDefaultBottomNewsFeedList(getApplicationContext());

        NewsFeedsFetchManager.getInstance().fetch(defaultTopNewsFeed, defaultBottomNewsFeeds, this);
    }

    private MainFragment getMainFragment() {
        return (MainFragment)getFragmentManager().findFragmentById(R.id.main_browse_fragment);
    }

    @Override
    public void onFetchAllNewsFeeds(NewsFeed topNewsFeed, ArrayList<NewsFeed> bottomNewsFeeds) {
        NewsDb.getInstance(getApplicationContext()).saveTopNewsFeed(topNewsFeed);
        NewsDb.getInstance(getApplicationContext()).saveBottomNewsFeedList(bottomNewsFeeds);

        applyNewsFeeds(topNewsFeed, bottomNewsFeeds);
    }

    private void applyNewsFeeds(NewsFeed topNewsFeed, ArrayList<NewsFeed> bottomNewsFeeds) {
        MainFragment fragment = getMainFragment();
        fragment.applyNewsFeeds(topNewsFeed, bottomNewsFeeds);

        NewsContentFetchManager.getInstance().fetch(topNewsFeed, bottomNewsFeeds, this);
//        NewsImageUrlFetchManager.getInstance().fetch(topNewsFeed, bottomNewsFeeds, this);
    }

//    @Override
//    public void onFetchTopNewsFeedImageUrl(News news, String url, int newsIndex) {
//        MainFragment fragment = getMainFragment();
//        fragment.configOnTopNewsImageUrlLoad(news, url, newsIndex);
//    }
//
//    @Override
//    public void onFetchBottomNewsFeedImageUrl(News news, String url, int newsFeedIndex, int newsIndex) {
//        MainFragment fragment = getMainFragment();
//        fragment.configOnBottomNewsImageUrlLoad(news, url, newsFeedIndex, newsIndex);
//    }

    @Override
    public void onFetchTopNewsContent(News news, NewsContent newsContent, int newsPosition) {
        MainFragment fragment = getMainFragment();
        fragment.configOnTopNewsImageUrlLoad(news, newsContent.getImageUrl(), newsPosition);
    }

    @Override
    public void onFetchBottomNewsContent(News news, NewsContent newsContent,
                                         int newsFeedPosition, int newsPosition) {
        MainFragment fragment = getMainFragment();
        fragment.configOnBottomNewsImageUrlLoad(news, newsContent.getImageUrl(),
                newsFeedPosition, newsPosition);
    }
}
