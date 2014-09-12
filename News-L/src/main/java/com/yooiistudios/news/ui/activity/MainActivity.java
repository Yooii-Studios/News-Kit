package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.antonioleiva.recyclerviewextensions.GridLayoutManager;
import com.viewpagerindicator.CirclePageIndicator;
import com.yooiistudios.news.NewsApplication;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedArchiveUtils;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.model.news.TintType;
import com.yooiistudios.news.model.news.task.BottomNewsFeedFetchTask;
import com.yooiistudios.news.model.news.task.BottomNewsImageUrlFetchTask;
import com.yooiistudios.news.model.news.task.TopFeedNewsImageUrlFetchTask;
import com.yooiistudios.news.model.news.task.TopNewsFeedFetchTask;
import com.yooiistudios.news.ui.adapter.MainBottomAdapter;
import com.yooiistudios.news.ui.adapter.MainTopPagerAdapter;
import com.yooiistudios.news.ui.itemanimator.SlideInFromBottomItemAnimator;
import com.yooiistudios.news.ui.widget.MainRefreshLayout;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.NLLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity
        implements TopFeedNewsImageUrlFetchTask.OnTopFeedImageUrlFetchListener,
        TopNewsFeedFetchTask.OnFetchListener,
        BottomNewsFeedFetchTask.OnFetchListener,
        MainBottomAdapter.OnItemClickListener,
        BottomNewsImageUrlFetchTask.OnBottomImageUrlFetchListener,
        RecyclerView.ItemAnimator.ItemAnimatorFinishedListener {

    @InjectView(R.id.main_top_view_pager)           ViewPager mTopNewsFeedViewPager;
    @InjectView(R.id.main_top_view_pager_wrapper)   FrameLayout mTopNewsFeedViewPagerWrapper;
    @InjectView(R.id.main_top_unavailable_wrapper)  FrameLayout mTopNewsFeedUnavailableWrapper;
    @InjectView(R.id.main_top_page_indicator)       CirclePageIndicator mTopViewPagerIndicator;
    @InjectView(R.id.main_top_news_feed_title_text_view) TextView mTopNewsFeedTitleTextView;
    @InjectView(R.id.bottomNewsFeedRecyclerView)    RecyclerView mBottomNewsFeedRecyclerView;
    @InjectView(R.id.main_loading_container)        ViewGroup mLoadingContainer;
    @InjectView(R.id.main_loading_log)              TextView mLoadingLog;
    @InjectView(R.id.main_scrolling_content)        View mScrollingContent;
    @InjectView(R.id.main_swipe_refresh_layout)     MainRefreshLayout mSwipeRefreshLayout;

    private static final String TAG = MainActivity.class.getName();
    public static final String VIEW_NAME_IMAGE_PREFIX = "topImage_";
    public static final String VIEW_NAME_TITLE_PREFIX = "topTitle_";
    public static final String INTENT_KEY_VIEW_NAME_IMAGE = "INTENT_KEY_VIEW_NAME_IMAGE";
    public static final String INTENT_KEY_VIEW_NAME_TITLE = "INTENT_KEY_VIEW_NAME_TITLE";
    public static final String INTENT_KEY_TINT_TYPE = "INTENT_KEY_TINT_TYPE";
    private static final int BOTTOM_NEWS_FEED_ANIM_DELAY_UNIT_MILLI = 60;
    private static final int BOTTOM_NEWS_FEED_COLUMN_COUNT = 2;

    private ImageLoader mImageLoader;

    private NewsFeed mTopNewsFeed;
    private ArrayList<NewsFeed> mBottomNewsFeedList;

    private TopFeedNewsImageUrlFetchTask mTopImageUrlFetchTask;
    private TopNewsFeedFetchTask mTopNewsFeedFetchTask;
    private SparseArray<BottomNewsFeedFetchTask>
            mBottomNewsFeedIndexToNewsFetchTaskMap;
    private HashMap<News, BottomNewsImageUrlFetchTask>
            mBottomNewsFeedNewsToImageTaskMap;
    private HashMap<News, TopFeedNewsImageUrlFetchTask>
            mTopNewsFeedNewsToImageTaskMap;
    private MainBottomAdapter mBottomNewsFeedAdapter;
    private MainTopPagerAdapter mTopNewsFeedPagerAdapter;
    private ArrayList<Integer> mDisplayingBottomNewsFeedIndices;

    private SlideInFromBottomItemAnimator mItemAnimator;

    private boolean mTopNewsFeedReady = false;
    private boolean mTopNewsFeedFirstImageReady = false;
    private boolean mBottomNewsFeedReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

//        mImageLoader = new ImageLoader(Volley.newRequestQueue(this), ImageMemoryCache.INSTANCE);
        mImageLoader = new ImageLoader(((NewsApplication)getApplication()).getRequestQueue(),
                ImageMemoryCache.getInstance(getApplicationContext()));

        boolean needsRefresh = NewsFeedArchiveUtils.newsNeedsToBeRefreshed(getApplicationContext());

        // TODO off-line configuration
        // TODO ConcurrentModification 문제 우회를 위해 애니메이션이 끝나기 전 스크롤을 막던지 처리 해야함.
        initRefreshLayout();
        initTopNewsFeed(needsRefresh);
        initBottomNewsFeed(needsRefresh);
        showMainContentIfReady();

        applySystemWindowsBottomInset(mScrollingContent);
    }
    //

    private void applySystemWindowsBottomInset(View containerView) {
        containerView.setFitsSystemWindows(true);
        containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                if (metrics.widthPixels < metrics.heightPixels) {
                    view.setPadding(0, 0, 0, windowInsets.getSystemWindowInsetBottom());
                } else {
                    view.setPadding(0, 0, windowInsets.getSystemWindowInsetRight(), 0);
                }
                return windowInsets.consumeSystemWindowInsets();
            }
        });
    }

    private void initRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_color_scheme_1, R.color.refresh_color_scheme_2,
                R.color.refresh_color_scheme_3, R.color.refresh_color_scheme_4);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                NLLog.i(TAG, "onRefresh called from SwipeRefreshLayout");
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    // 기존 뉴스 삭제 후 뉴스피드 새로 로딩
                    NewsFeedArchiveUtils.clearArchive(getApplicationContext());
                    initTopNewsFeed(false);
                    initBottomNewsFeed(false);
                }
            }
        });
    }

    private void initTopNewsFeed(boolean refresh) {
        // Dialog
//        mDialog = ProgressDialog.show(this, getString(R.string.splash_loading_title),
//                getString(R.string.splash_loading_description));

        Context context = getApplicationContext();

        // Fetch
        mTopNewsFeed = NewsFeedArchiveUtils.loadTopNewsFeed(context);
        if (refresh) {
            fetchTopNewsFeed();
        } else {
            if (mTopNewsFeed.isValid()) {
                notifyNewTopNewsFeedSet();
            } else {
                fetchTopNewsFeed();
            }
        }

    }
    private void fetchTopNewsFeed() {
        mTopNewsFeedReady = false;
        mTopNewsFeedFetchTask = new TopNewsFeedFetchTask(this, mTopNewsFeed.getNewsFeedUrl(), this);
        mTopNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void notifyNewTopNewsFeedSet() {
        // show view pager wrapper
        mTopNewsFeedViewPagerWrapper.setVisibility(View.VISIBLE);
        mTopNewsFeedUnavailableWrapper.setVisibility(View.GONE);
        mTopViewPagerIndicator.setVisibility(View.VISIBLE);

        mTopNewsFeedReady = true;
        ArrayList<News> items = mTopNewsFeed.getNewsList();
        mTopNewsFeedPagerAdapter = new MainTopPagerAdapter(getFragmentManager(), mTopNewsFeed);

        mTopNewsFeedViewPager.setAdapter(mTopNewsFeedPagerAdapter);
        mTopViewPagerIndicator.setViewPager(mTopNewsFeedViewPager);

        if (items.size() > 0) {
            News news = items.get(0);
            mTopNewsFeedFirstImageReady = false;

            mTopImageUrlFetchTask = new TopFeedNewsImageUrlFetchTask(news, 0, this);
            mTopImageUrlFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mTopNewsFeedFirstImageReady = true;
            fetchTopNewsFeedImageExceptFirstNews();
        }

        mTopNewsFeedTitleTextView.setText(mTopNewsFeed.getTitle());
    }
    private void showTopNewsFeedUnavailable() {
        // show top unavailable wrapper
        mTopNewsFeedViewPagerWrapper.setVisibility(View.GONE);
        mTopNewsFeedUnavailableWrapper.setVisibility(View.VISIBLE);
        mTopViewPagerIndicator.setVisibility(View.INVISIBLE);

        mTopNewsFeed = null;
        mTopNewsFeedReady = true;

        mTopNewsFeedFirstImageReady = true;
    }

    private void initBottomNewsFeed(boolean refresh) {
        mBottomNewsFeedReady = false;

        Context context = getApplicationContext();
        //init ui
        mBottomNewsFeedRecyclerView.setHasFixedSize(true);
//        ((ViewGroup)mBottomNewsFeedRecyclerView).setTransitionGroup(false);
        mItemAnimator = new SlideInFromBottomItemAnimator(
                mBottomNewsFeedRecyclerView);
        mBottomNewsFeedRecyclerView.setItemAnimator(mItemAnimator);
        GridLayoutManager layoutManager = new GridLayoutManager(context);
        layoutManager.setColumns(BOTTOM_NEWS_FEED_COLUMN_COUNT);
        mBottomNewsFeedRecyclerView.setLayoutManager(layoutManager);

        mBottomNewsFeedList = NewsFeedArchiveUtils.loadBottomNews(context);
        mDisplayingBottomNewsFeedIndices = new ArrayList<Integer>();

        // 메인 하단 뉴스피드들의 현재 뉴스 인덱스를 0으로 초기화
        for (int i = 0; i < mBottomNewsFeedList.size(); i++) {
            mDisplayingBottomNewsFeedIndices.add(0);
        }

        if (refresh) {
            fetchBottomNewsFeedList();
        } else {
            boolean isValid = true;
            for (NewsFeed newsFeed : mBottomNewsFeedList) {
                if (!newsFeed.isValid()) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                notifyNewBottomNewsFeedListSet(false);
//                fetchBottomNewsFeedListImage();
            } else {
                fetchBottomNewsFeedList();
            }
        }

        // 메인 하단의 뉴스피드 RecyclerView의 높이를 set
        ViewGroup.LayoutParams recyclerViewLp = mBottomNewsFeedRecyclerView.getLayoutParams();
        recyclerViewLp.height = MainBottomAdapter.measureMaximumHeight(getApplicationContext(),
                mBottomNewsFeedList.size(), BOTTOM_NEWS_FEED_COLUMN_COUNT);

    }
    private void fetchBottomNewsFeedList() {
        final int bottomNewsCount = mBottomNewsFeedList.size();

        mBottomNewsFeedIndexToNewsFetchTaskMap = new SparseArray<BottomNewsFeedFetchTask>();
        for (int i = 0; i < bottomNewsCount; i++) {
            NewsFeedUrl url = mBottomNewsFeedList.get(i).getNewsFeedUrl();
            BottomNewsFeedFetchTask task = new BottomNewsFeedFetchTask(
                    getApplicationContext(), url, i, this
            );
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mBottomNewsFeedIndexToNewsFetchTaskMap.put(i, task);
        }
    }

    private void fetchBottomNewsFeedListImage() {
        mBottomNewsFeedNewsToImageTaskMap = new
                HashMap<News, BottomNewsImageUrlFetchTask>();

        for (int i = 0; i < mBottomNewsFeedList.size(); i++) {
            NewsFeed feed = mBottomNewsFeedList.get(i);

            // IndexOutOfBoundException 방지
            int newsIndex = i < mDisplayingBottomNewsFeedIndices.size() ?
                    mDisplayingBottomNewsFeedIndices.get(i) : 0;

            ArrayList<News> newsList = feed.getNewsList();
            if (newsList.size() > 0) {
                // IndexOutOfBoundException 방지
                News news = newsIndex < newsList.size() ? newsList.get(newsIndex) : newsList.get(0);

                BottomNewsImageUrlFetchTask task = new BottomNewsImageUrlFetchTask(news, i, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                mBottomNewsFeedNewsToImageTaskMap.put(news, task);
            }
        }
    }

    private void fetchTopNewsFeedImageExceptFirstNews() {
        if (mTopNewsFeed == null) {
            return;
        }
        mTopNewsFeedNewsToImageTaskMap = new
                HashMap<News, TopFeedNewsImageUrlFetchTask>();

        ArrayList<News> newsList = mTopNewsFeed.getNewsList();

        for (int i = 1; i < newsList.size(); i++) {
            News news = newsList.get(i);

            TopFeedNewsImageUrlFetchTask task = new
                    TopFeedNewsImageUrlFetchTask(news, i, this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mTopNewsFeedNewsToImageTaskMap.put(news, task);
        }
    }
    private void cancelTopNewsFeedImageFetchTasks() {
        mTopNewsFeedReady = false;
        for (Map.Entry<News, TopFeedNewsImageUrlFetchTask> entry :
                mTopNewsFeedNewsToImageTaskMap.entrySet()) {
            TopFeedNewsImageUrlFetchTask task = entry.getValue();
            if (task != null) {
                task.cancel(true);
            }
        }
        mTopNewsFeedNewsToImageTaskMap.clear();
    }

    private void cancelBottomNewsFetchTasks() {
        mBottomNewsFeedReady = false;
        int taskCount = mBottomNewsFeedIndexToNewsFetchTaskMap.size();
        for (int i = 0; i < taskCount; i++) {
            BottomNewsFeedFetchTask task = mBottomNewsFeedIndexToNewsFetchTaskMap
                    .get(i, null);
            if (task != null) {
                task.cancel(true);
            }
        }
        mBottomNewsFeedIndexToNewsFetchTaskMap.clear();

        for (Map.Entry<News, BottomNewsImageUrlFetchTask> entry :
                mBottomNewsFeedNewsToImageTaskMap.entrySet()) {
            BottomNewsImageUrlFetchTask task = entry.getValue();
            if (task != null) {
                task.cancel(true);
            }
        }
        mBottomNewsFeedNewsToImageTaskMap.clear();
    }
    private void notifyNewBottomNewsFeedListSet(boolean animate) {
        mBottomNewsFeedReady = true;

        if (animate) {
            mBottomNewsFeedAdapter = new MainBottomAdapter
                    (getApplicationContext(), this);
            mBottomNewsFeedRecyclerView.setAdapter(mBottomNewsFeedAdapter);

            for (int i = 0; i < mBottomNewsFeedList.size(); i++) {
                final NewsFeed newsFeed = mBottomNewsFeedList.get(i);
                final int idx = i;
                mBottomNewsFeedRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBottomNewsFeedAdapter.addNewsFeed(newsFeed);

                        if (idx == (mBottomNewsFeedList.size() - 1)) {
                            mItemAnimator.isRunning(MainActivity.this);
                        }
                    }
                }, BOTTOM_NEWS_FEED_ANIM_DELAY_UNIT_MILLI * i + 1);

            }
        }
    }

    private void showMainContentIfReady() {
        showMainContentIfReady(false);
    }
    private void showMainContentIfReady(boolean noTopNewsImage) {
        NLLog.i("showMainContentIfReady", "mTopNewsFeedReady : " + mTopNewsFeedReady);
        NLLog.i("showMainContentIfReady", "mBottomNewsFeedReady : " + mBottomNewsFeedReady);
        NLLog.i("showMainContentIfReady", "mTopNewsFeedFirstImageReady : " + mTopNewsFeedFirstImageReady);
        NLLog.i("showMainContentIfReady", "noTopNewsImage : " + noTopNewsImage);

        String loadingStatus = "Top news ready : " + mTopNewsFeedReady
                + "\nTop news first image ready : "
                + (noTopNewsImage ? "NO IMAGE!!!" : mTopNewsFeedFirstImageReady)
                + "\nBottom news feed ready : " + mBottomNewsFeedReady;

        mLoadingLog.setText(loadingStatus);

        if (mLoadingContainer.getVisibility() == View.GONE) {
            return;
        }

        if (mTopNewsFeedReady && mBottomNewsFeedReady) {
            if (noTopNewsImage || mTopNewsFeedFirstImageReady) {
                mSwipeRefreshLayout.setRefreshing(false);

                NewsFeedArchiveUtils.save(getApplicationContext(), mTopNewsFeed,
                        mBottomNewsFeedList);
                notifyNewBottomNewsFeedListSet(true);

                // loaded
                mLoadingContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_store) {
            startActivity(new Intent(MainActivity.this, StoreActivity.class));
            return true;
        } else if (id == R.id.action_remove_archive) {
            NewsFeedArchiveUtils.clearArchive(getApplicationContext());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTopFeedImageUrlFetchSuccess(News news, String url,
                                              final int position) {
        NLLog.i(TAG, "fetch image url success.");
        NLLog.i(TAG, "news link : " + news.getLink());
        NLLog.i(TAG, "image url : " + url);
        if (url == null) {
            fetchTopNewsFeedImageExceptFirstNews();
            showMainContentIfReady(true);
        }
        else {
            news.setImageUrl(url);

            final long startMilli;

            startMilli = System.currentTimeMillis();
            mImageLoader.get(url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    long endMilli;
                    endMilli = System.currentTimeMillis();
                    NLLog.i("performance", "mImageLoader.get : " +
                            (endMilli - startMilli));
                    NLLog.i(TAG, "onResponse\nposition : " + position);

                    mTopNewsFeedPagerAdapter.notifyImageLoaded(position);

                    if (position == 0) {
                        mTopNewsFeedFirstImageReady = true;
                        fetchTopNewsFeedImageExceptFirstNews();
                        showMainContentIfReady();
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
    }

    @Override
    public void onTopFeedImageUrlFetchFail() {
        // TODO 여기로 들어올 경우 처리 하자!
        NLLog.i(TAG, "fetch image url failed.");
    }

    /**
     * TopNewsFeedFetch Listener
     */
    @Override
    public void onTopNewsFeedFetchSuccess(NewsFeed newsFeed) {
        NLLog.i(TAG, "onTopNewsFeedFetchSuccess");
//        if (mDialog != null) {
//            mDialog.dismiss();
//        }
        mTopNewsFeed = newsFeed;
        notifyNewTopNewsFeedSet();
        showMainContentIfReady();
    }

    @Override
    public void onTopNewsFeedFetchFail() {
        NLLog.i(TAG, "onTopNewsFeedFetchFail");
        showTopNewsFeedUnavailable();
        showMainContentIfReady();
    }

    @Override
    public void onBottomNewsFeedFetchSuccess(int position, NewsFeed newsFeed) {
        NLLog.i(TAG, "onBottomNewsFeedFetchSuccess");
        mBottomNewsFeedIndexToNewsFetchTaskMap.remove(position);
        mBottomNewsFeedList.set(position, newsFeed);

        int remainingTaskCount = mBottomNewsFeedIndexToNewsFetchTaskMap.size();
        if (remainingTaskCount == 0) {
            NLLog.i(TAG, "All task done. Loaded news feed list size : " +
                    mBottomNewsFeedList.size());
            mBottomNewsFeedReady = true;

            notifyNewBottomNewsFeedListSet(false);
            showMainContentIfReady();
        } else {
            NLLog.i(TAG, remainingTaskCount + " remaining tasks.");
        }
    }

    @Override
    public void onBottomNewsFeedFetchFail() {
        NLLog.i(TAG, "onBottomNewsFeedFetchFail");
        // TODO Top news처럼 뉴스 없음 처리하고 notify 해줘야 함
    }

    @Override
    public void onBottomItemClick(MainBottomAdapter.BottomNewsFeedViewHolder viewHolder,
                                  NewsFeed newsFeed, int position) {
        NLLog.i(TAG, "onBottomItemClick");
        NLLog.i(TAG, "newsFeed : " + newsFeed.getTitle());

        ImageView imageView = viewHolder.imageView;
        TextView titleView = viewHolder.newsTitleTextView;


        ActivityOptions activityOptions =
                ActivityOptions.makeSceneTransitionAnimation(
                        MainActivity.this,
                        new Pair<View, String>(imageView, imageView.getViewName()),
                        new Pair<View, String>(titleView, titleView.getViewName())
                );
//        ActivityOptions activityOptions2 = ActivityOptions.
//                makeSceneTransitionAnimation(NLMainActivity.this,
//                        imageView, imageView.getViewName());

        Intent intent = new Intent(MainActivity.this,
                NewsFeedDetailActivity.class);
        intent.putExtra(NewsFeed.KEY_NEWS_FEED, newsFeed);
        intent.putExtra(News.KEY_NEWS, mDisplayingBottomNewsFeedIndices.get(position));
        intent.putExtra(INTENT_KEY_VIEW_NAME_IMAGE, imageView.getViewName());
        intent.putExtra(INTENT_KEY_VIEW_NAME_TITLE, titleView.getViewName());

        // 미리 이미지뷰에 set해 놓은 태그(TintType)를 인텐트로 보내 적용할 틴트의 종류를 알려줌
        Object tintTag = viewHolder.imageView.getTag();
        TintType tintType = tintTag != null ? (TintType)tintTag : null;
        intent.putExtra(INTENT_KEY_TINT_TYPE, tintType);

        startActivity(intent, activityOptions.toBundle());
    }

    @Override
    public void onBottomImageUrlFetchSuccess(News news, String url,
                                             int position) {
        NLLog.i(TAG, "onBottomImageUrlFetchSuccess");
        news.setImageUrlChecked(true);
        if (url != null) {
            news.setImageUrl(url);
            if (mBottomNewsFeedAdapter != null && !mItemAnimator.isRunning()) {
                mBottomNewsFeedAdapter.notifyItemChanged(position);
            }

            mBottomNewsFeedNewsToImageTaskMap.remove(news);

            NLLog.i(TAG, "title : " + news.getTitle() + "'s image url fetch " +
                    "success.\nimage url : " + url);
        }
    }

    @Override
    public void onBottomImageUrlFetchFail() {
        NLLog.i(TAG, "onBottomImageUrlFetchFail");
    }

    @Override
    public void onAnimationsFinished() {
        fetchBottomNewsFeedListImage();
    }
}
