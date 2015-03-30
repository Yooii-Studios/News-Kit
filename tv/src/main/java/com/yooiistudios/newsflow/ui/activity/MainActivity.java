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

package com.yooiistudios.newsflow.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.DefaultNewsFeedProvider;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.NewsFeedUrl;
import com.yooiistudios.newsflow.core.news.NewsFeedUrlType;
import com.yooiistudios.newsflow.core.news.database.NewsDb;
import com.yooiistudios.newsflow.core.news.newscontent.NewsContent;
import com.yooiistudios.newsflow.core.news.util.NewsFeedArchiveUtils;
import com.yooiistudios.newsflow.core.news.util.NewsFeedValidator;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.newsflow.model.debug.MainNewsTopicFetchTester;
import com.yooiistudios.newsflow.model.news.task.NewsContentFetchManager;
import com.yooiistudios.newsflow.model.news.task.NewsFeedsFetchManager;
import com.yooiistudios.newsflow.ui.fragment.MainFragment;

import java.util.ArrayList;

/*
 * MainActivity
 */
public class MainActivity extends Activity
        implements NewsFeedsFetchManager.OnFetchListener,
        NewsContentFetchManager.OnFetchListener {
    public static final int RC_PAIR_ACTIVITY = 1001;
    // 1 Hours * 60 Min * 60 Sec * 1000 millisec = 1 Hours
    private static final long CACHE_EXPIRATION_LIMIT = 1 * 60 * 60 * 1000;
//    private static final long CACHE_EXPIRATION_LIMIT = 1000;
    private static final boolean ENABLE_DEBUG = false;

    private MainNewsTopicFetchTester mTester;

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVariables();
        loadContent();
    }

    private void initVariables() {
        mTester = new MainNewsTopicFetchTester(this);
    }

    private void loadContent() {
        Context context = getApplicationContext();

        if (ENABLE_DEBUG) {
            mTester.testFetch();

            return;
        }

        PanelMatrix panelMatrix = PanelMatrixUtils.getCurrentPanelMatrix(context);

        NewsFeed topNewsFeed = NewsDb.getInstance(context).loadTopNewsFeed(context);
        ArrayList<NewsFeed> bottomNewsFeeds = NewsDb.getInstance(context).loadBottomNewsFeedList(
                context, panelMatrix.getPanelCount());

//        topNewsFeed.isDisplayable()
//        if (!NewsFeedValidator.isDisplayable(topNewsFeed) || !NewsFeedValidator.isDisplayable(bottomNewsFeeds)
        if (!topNewsFeed.isDisplayable() || !NewsFeedValidator.isDisplayable(bottomNewsFeeds)
                || NewsFeedArchiveUtils.newsNeedsToBeRefreshed(context, CACHE_EXPIRATION_LIMIT)) {
            NewsFeed defaultTopNewsFeed = DefaultNewsFeedProvider.getDefaultTopNewsFeed(context);
            ArrayList<NewsFeed> defaultBottomNewsFeeds = DefaultNewsFeedProvider
                    .getDefaultBottomNewsFeedList(getApplicationContext());

            fetch(defaultTopNewsFeed, defaultBottomNewsFeeds);
        } else {
            applyNewsFeeds(topNewsFeed, bottomNewsFeeds);
        }
    }

    public void fetch(NewsFeed topNewsFeed, ArrayList<NewsFeed> bottomNewsFeeds) {
        NewsFeedsFetchManager.getInstance().fetch(topNewsFeed, bottomNewsFeeds, this);
    }

    private MainFragment getMainFragment() {
        return (MainFragment)getFragmentManager().findFragmentById(R.id.main_browse_fragment);
    }

    private void applyNewsFeeds(NewsFeed topNewsFeed, ArrayList<NewsFeed> bottomNewsFeeds) {
        MainFragment fragment = getMainFragment();
        fragment.applyNewsFeeds(topNewsFeed, bottomNewsFeeds);

        NewsContentFetchManager.getInstance().fetch(topNewsFeed, bottomNewsFeeds, this);
    }

    @Override
    protected void onDestroy() {
        NewsFeedsFetchManager.getInstance().cancelAllTasks();
        NewsContentFetchManager.getInstance().cancelAllTasks();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case RC_PAIR_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String topUrl = data.getStringExtra(PairActivity.INTENT_KEY_TOP_URL);
                    ArrayList<String> bottomUrls = data.getStringArrayListExtra(
                            PairActivity.INTENT_KEY_BOTTOM_URL);

                    NewsFeedUrlType urlType = NewsFeedUrlType.CUSTOM;
                    NewsFeed topNewsFeed = new NewsFeed(new NewsFeedUrl(topUrl, urlType));
                    ArrayList<NewsFeed> bottomNewsFeeds = new ArrayList<>();
                    for (String bottomUrl : bottomUrls) {
                        bottomNewsFeeds.add(new NewsFeed(new NewsFeedUrl(bottomUrl, urlType)));
                    }

                    getMainFragment().emptyNewsFeeds();
                    fetch(topNewsFeed, bottomNewsFeeds);
                }
                break;
        }
    }

    @Override
    public void onFetchAllNewsFeeds(NewsFeed topNewsFeed, ArrayList<NewsFeed> bottomNewsFeeds) {
        NewsDb.getInstance(getApplicationContext()).saveTopNewsFeed(topNewsFeed);
        NewsDb.getInstance(getApplicationContext()).saveBottomNewsFeedList(bottomNewsFeeds);
        NewsFeedArchiveUtils.saveRecentCacheMillisec(getApplicationContext());

        if (ENABLE_DEBUG) {
            mTester.onFetchAllNewsFeeds(topNewsFeed, bottomNewsFeeds);
        }

        applyNewsFeeds(topNewsFeed, bottomNewsFeeds);
    }

    @Override
    public void onFetchTopNewsContent(News news, NewsContent newsContent, int newsPosition) {
        MainFragment fragment = getMainFragment();
        fragment.configOnTopNewsContentLoad(news, newsPosition);

        if (ENABLE_DEBUG) {
            mTester.checkAllTopNewsContentFetched();
        }
    }

    @Override
    public void onFetchBottomNewsContent(News news, NewsContent newsContent,
                                         int newsFeedPosition, int newsPosition) {
        MainFragment fragment = getMainFragment();
        fragment.configOnBottomNewsContentLoad(news, newsFeedPosition, newsPosition);

        if (ENABLE_DEBUG) {
            mTester.checkAllBottomNewsContentFetched(newsFeedPosition);
        }
    }
}
