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

package com.yooiistudios.newsflow.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.NewsBrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.database.NewsDb;
import com.yooiistudios.newsflow.core.ui.animation.activitytransition.ActivityTransitionProperty;
import com.yooiistudios.newsflow.model.PicassoBackgroundManagerTarget;
import com.yooiistudios.newsflow.ui.activity.MainActivity;
import com.yooiistudios.newsflow.ui.activity.NewsActivity;
import com.yooiistudios.newsflow.ui.activity.PairActivity;
import com.yooiistudios.newsflow.ui.adapter.NewsFeedAdapter;
import com.yooiistudios.newsflow.ui.presenter.CardPresenter;
import com.yooiistudios.newsflow.ui.presenter.SettingItemPresenter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainFragment extends NewsBrowseFragment {
    public static final String ARG_NEWS_KEY = "arg_news_key";
    public static final String ARG_HAS_IMAGE_KEY = "has_image";
    public static final String TRANSITION_PROPERTY_ARG_KEY = "transition_property_arg_key";
    public static final String DETAIL_CONTENT_KEY = "detail_content_key";
    public static final String DETAIL_REFINED_CONTENT = "detail_refined_content";
    public static final String DETAIL_WEB_CONTENT = "detail_web_content";

    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private Drawable mAppBadge;
    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private final Handler mHandler = new Handler();
    private String mBackgroundUrl;
    CardPresenter mCardPresenter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        initVariables(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();
        setupUIElements();
        setupEventListeners();
    }

    private void initVariables(Activity activity) {
        mCardPresenter = new CardPresenter(activity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
    }

    public void applyNewsFeeds(NewsFeed topNewsFeed, ArrayList<NewsFeed> bottomNewsFeeds) {
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        rowsAdapter.add(makeListRow(topNewsFeed, 0));
        for (int i = 0; i < bottomNewsFeeds.size(); i++) {
            NewsFeed newsFeed = bottomNewsFeeds.get(i);

            rowsAdapter.add(makeListRow(newsFeed, i + 1));
        }

        HeaderItem SettingHeader = new HeaderItem(bottomNewsFeeds.size(),
                getString(R.string.settings), null);

        SettingItemPresenter settingItemPresenter = new SettingItemPresenter();
        ArrayObjectAdapter settingRowAdapter = new ArrayObjectAdapter(settingItemPresenter);

        SettingItemPresenter.SettingObject pairObject = new SettingItemPresenter.SettingObject(
                R.string.pair_title,
                R.drawable.ic_tv_link_white_48dp);
        settingRowAdapter.add(pairObject);

        /*
        SettingItemPresenter.SettingObject removeDbObject = new SettingItemPresenter.SettingObject(
                R.string.remove_db);
        settingRowAdapter.add(removeDbObject);
        */
        rowsAdapter.add(new ListRow(SettingHeader, settingRowAdapter));

        setAdapter(rowsAdapter);
    }

    public void emptyNewsFeeds() {
        startHeadersTransition(true);
        onRowSelected(0);
        setAdapter(null);
    }

    private ListRow makeListRow(NewsFeed newsFeed, int i) {
        HeaderItem header = new HeaderItem(i, newsFeed.getTitle(), null);
        NewsFeedAdapter adapter = new NewsFeedAdapter(mCardPresenter, newsFeed);
        return new ListRow(header, adapter);
    }

    public void configOnTopNewsContentLoad(News news, int newsIndex) {
        NewsDb.getInstance(getActivity()).saveNewsContentWithGuid(news);
        NewsDb.getInstance(getActivity()).saveTopNewsImageUrlWithGuid(news.getImageUrl(), news.getGuid());
        applyTopNewsImageUrlAt(newsIndex);
    }

    public void configOnBottomNewsContentLoad(News news, int newsFeedIndex, int newsIndex) {
        NewsDb.getInstance(getActivity()).saveNewsContentWithGuid(news);
        NewsDb.getInstance(getActivity()).saveBottomNewsImageUrlWithGuid(news.getImageUrl(), newsFeedIndex,
                news.getGuid());
        applyBottomNewsImageUrlAt(newsFeedIndex, newsIndex);
    }

    private void applyTopNewsImageUrlAt(int newsIndex) {
        NewsFeedAdapter adapter = getTopNewsFeedAdapter();
        adapter.notifyNewsImageLoadedAt(newsIndex);
    }

    private void applyBottomNewsImageUrlAt(int newsFeedIndex, int newsIndex) {
        NewsFeedAdapter adapter = getBottomNewsFeedAdapter(newsFeedIndex);
        adapter.notifyNewsImageLoadedAt(newsIndex);
    }

    /*
    // FIXME: 동현이 일단 안쓰고 있어서 주석처리
    private NewsFeed getTopNewsFeed() {
        return getTopNewsFeedAdapter().getNewsFeed();
    }

    private NewsFeed getBottomNewsFeedAt(int index) {
        return getBottomNewsFeedAdapter(index).getNewsFeed();
    }
    */

    private NewsFeedAdapter getTopNewsFeedAdapter() {
        return getNewsFeedAdapterAt(0);
    }

    private NewsFeedAdapter getBottomNewsFeedAdapter(int newsFeedIndex) {
        return getNewsFeedAdapterAt(newsFeedIndex + 1);
    }

    private NewsFeedAdapter getNewsFeedAdapterAt(int newsFeedIndex) {
        ListRow row = (ListRow)getAdapter().get(newsFeedIndex);
        return (NewsFeedAdapter)row.getAdapter();
    }

    private void prepareBackgroundManager() {
        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);

        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);

        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
//        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
//        setBadgeDrawable(getResources().getDrawable(R.drawable.app_badge));
        mAppBadge = getResources().getDrawable(R.drawable.app_badge);
        setAppBadge();

        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        setBrandColor(getResources().getColor(R.color.brand_color));

        // set search icon color
//        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));

        setupUIElementsExperiments();
    }

    private void setAppBadge() {
        setBadgeDrawable(mAppBadge);
    }

    private void hideAppBadge() {
        setBadgeDrawable(null);
    }

    private void setupUIElementsExperiments() {
        setBrowseTransitionListener(new BrowseTransitionListener() {
            @Override
            public void onHeadersTransitionStart(boolean withHeaders) {
                super.onHeadersTransitionStart(withHeaders);
                if (isShowingHeaders()) {
                    setAppBadge();
                } else {
                    hideAppBadge();
                    setTitle(getString(R.string.all_news));
                }
            }
        });
    }

    private void setupEventListeners() {
        // 리스너를 달지 않으면 검색창이 보이지 않음
        /*
        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                        .show();
            }
        });
        */

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof News) {
//                ActivityTransitionProperty transitionProperty = createTransitionProperty(itemViewHolder);

                boolean hasImage =
                        ((CardPresenter.NewsViewHolder)itemViewHolder).picassoTarget.hasValidImage();

                Intent intent = new Intent(getActivity(), NewsActivity.class);
                intent.putExtra(ARG_NEWS_KEY, ((News) item));
                intent.putExtra(ARG_HAS_IMAGE_KEY, hasImage);

                // FIXME: 나중에 시간 여유가 되면 애니메이션을 제대로 추가하자
//                ImageCardView imageCardView = (ImageCardView) itemViewHolder.view;
//                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        getActivity(),
//                        imageCardView.getMainImageView(),
//                        NewsActivity.SHARED_ELEMENT_NAME).toBundle();
//                startActivity(intent, bundle);
                startActivity(intent);
            } else if (item instanceof SettingItemPresenter.SettingObject) {
                int titleId = ((SettingItemPresenter.SettingObject) item).getTitleId();
                if (titleId == R.string.pair_title) {
                    clearBackground();
                    startPairActivity(itemViewHolder);
//                } else if (titleId == R.string.copy_db) {
//                    NewsDb.copyDbToExternalStorage(getActivity());
//                } else if (titleId == R.string.remove_db) {
//                    NewsDb.getInstance(getActivity()).clearArchive();
                }
            }
        }
    }

    private void startPairActivity(Presenter.ViewHolder itemViewHolder) {
        ActivityTransitionProperty transitionProperty = createTransitionProperty(itemViewHolder);
        Intent intent = new Intent(getActivity(), PairActivity.class);
        intent.putExtra(TRANSITION_PROPERTY_ARG_KEY, new Gson().toJson(transitionProperty));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        getActivity().startActivityForResult(intent, MainActivity.RC_PAIR_ACTIVITY);
    }

    private ActivityTransitionProperty createTransitionProperty(Presenter.ViewHolder itemViewHolder) {
        ActivityTransitionProperty transitionProperty = new ActivityTransitionProperty();

        int[] screenLocation = new int[2];
        itemViewHolder.view.getLocationOnScreen(screenLocation);

        transitionProperty.setLeft(screenLocation[0]);
        transitionProperty.setTop(screenLocation[1]);
        transitionProperty.setWidth(itemViewHolder.view.getWidth());
        transitionProperty.setHeight(itemViewHolder.view.getHeight());

        return transitionProperty;
    }


    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof News) {
                mBackgroundUrl = ((News) item).getImageUrl();
                startBackgroundTimer();
            }
        }
    }

    /*
    protected void setDefaultBackground(Drawable background) {
        mDefaultBackground = background;
    }

    protected void setDefaultBackground(int resourceId) {
        mDefaultBackground = getResources().getDrawable(resourceId);
    }
    */

    protected void updateBackground() {
        if (mBackgroundUrl != null && mBackgroundUrl.trim().length() > 0) {
            Picasso.with(getActivity())
                    .load(mBackgroundUrl)
                    .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                    .centerCrop()
                    .error(mDefaultBackground)
                    .into(mBackgroundTarget);
        }
    }

    /*
    protected void updateBackground(Drawable drawable) {
        BackgroundManager.getInstance(getActivity()).setDrawable(drawable);
    }
    */

    protected void clearBackground() {
        BackgroundManager.getInstance(getActivity()).setDrawable(mDefaultBackground);
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateBackground();
                }
            });

        }
    }
}
