package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.yooiistudios.news.model.news.task.BottomNewsFeedFetchTask;
import com.yooiistudios.news.model.news.task.BottomNewsImageUrlFetchTask;
import com.yooiistudios.news.model.news.task.TopFeedNewsImageUrlFetchTask;
import com.yooiistudios.news.model.news.task.TopNewsFeedFetchTask;
import com.yooiistudios.news.ui.adapter.BottomNewsFeedAdapter;
import com.yooiistudios.news.ui.adapter.TopNewsFeedPagerAdapter;
import com.yooiistudios.news.ui.itemanimator.SlideInFromBottomItemAnimator;
import com.yooiistudios.news.util.cache.ImageMemoryCache;
import com.yooiistudios.news.util.log.NLLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity
        implements TopFeedNewsImageUrlFetchTask.OnTopFeedImageUrlFetchListener,
        TopNewsFeedFetchTask.OnFetchListener,
        BottomNewsFeedFetchTask.OnFetchListener,
        BottomNewsFeedAdapter.OnItemClickListener,
        BottomNewsImageUrlFetchTask.OnBottomImageUrlFetchListener {

    @InjectView(R.id.main_top_view_pager)           ViewPager mTopNewsFeedViewPager;
    @InjectView(R.id.main_top_page_indicator)       CirclePageIndicator mTopViewPagerIndicator;
    @InjectView(R.id.main_top_news_feed_title_text_view) TextView mTopNewsFeedTitleTextView;
    @InjectView(R.id.bottomNewsFeedRecyclerView)    RecyclerView mBottomNewsFeedRecyclerView;

    private static final String TAG = MainActivity.class.getName();
    public static final String VIEW_NAME_IMAGE_PREFIX = "topImage_";
    public static final String VIEW_NAME_TITLE_PREFIX = "topTitle_";
    public static final String INTENT_KEY_VIEW_NAME_IMAGE = "INTENT_KEY_VIEW_NAME_IMAGE";
    public static final String INTENT_KEY_VIEW_NAME_TITLE = "INTENT_KEY_VIEW_NAME_TITLE";
    private static final int BOTTOM_NEWS_FEED_ANIM_DELAY_UNIT_MILLI = 60;

    private ImageLoader mImageLoader;
//    private ProgressDialog mDialog;

    private NewsFeedUrl mTopNewsFeedUrl;
    private NewsFeed mTopNewsFeed;
    private ArrayList<NewsFeedUrl> mBottomNewsFeedUrlList;
    private ArrayList<NewsFeed> mBottomNewsFeedList;

    private TopFeedNewsImageUrlFetchTask mTopImageUrlFetchTask;
    private TopNewsFeedFetchTask mTopNewsFeedFetchTask;
    private SparseArray<BottomNewsFeedFetchTask>
            mBottomNewsFeedIndexToNewsFetchTaskMap;
    private HashMap<News, BottomNewsImageUrlFetchTask>
            mBottomNewsFeedNewsToImageTaskMap;
    private HashMap<News, TopFeedNewsImageUrlFetchTask>
            mTopNewsFeedNewsToImageTaskMap;
    private BottomNewsFeedAdapter mBottomNewsFeedAdapter;
    private TopNewsFeedPagerAdapter mTopNewsFeedPagerAdapter;

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
        initTopNewsFeed(needsRefresh);
        initBottomNewsFeed(needsRefresh);
        showMainContentIfReady();
    }

    private void initTopNewsFeed(boolean refresh) {
        // Dialog
//        mDialog = ProgressDialog.show(this, getString(R.string.splash_loading_title),
//                getString(R.string.splash_loading_description));

        Context context = getApplicationContext();

        // Fetch
        Pair<NewsFeedUrl, NewsFeed> topFeedPair = NewsFeedArchiveUtils.loadTopNews(context);
        mTopNewsFeedUrl = topFeedPair.first;
        if (refresh) {
            fetchTopNewsFeed();
        } else {
            if ((mTopNewsFeed = topFeedPair.second) != null) {
                notifyNewTopNewsFeedSet();
            } else {
                fetchTopNewsFeed();
            }
        }

    }
    private void fetchTopNewsFeed() {
        mTopNewsFeedReady = false;
        mTopNewsFeedFetchTask = new TopNewsFeedFetchTask(this,
                mTopNewsFeedUrl, this);
        mTopNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void notifyNewTopNewsFeedSet() {
        mTopNewsFeedReady = true;
        ArrayList<News> items = mTopNewsFeed.getNewsList();
        mTopNewsFeedPagerAdapter = new TopNewsFeedPagerAdapter(getFragmentManager(), mTopNewsFeed);

        mTopNewsFeedViewPager.setAdapter(mTopNewsFeedPagerAdapter);
        mTopViewPagerIndicator.setViewPager(mTopNewsFeedViewPager);

        if (items.size() > 0) {
            News news = items.get(0);
            mTopNewsFeedFirstImageReady = false;

            mTopImageUrlFetchTask = new TopFeedNewsImageUrlFetchTask(news, 0, this);
            mTopImageUrlFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            //TODO 해당 피드의 뉴스가 없을 경우 예외처리. Use dummy image.

            mTopNewsFeedFirstImageReady = true;
            fetchTopNewsFeedImageExceptFirstNews();
        }

        mTopNewsFeedTitleTextView.setText(mTopNewsFeed.getTitle());
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
        layoutManager.setColumns(2);
//        LinearLayoutManager layoutManager = new LinearLayoutManager
//                (getApplicationContext());
        mBottomNewsFeedRecyclerView.setLayoutManager(layoutManager);

        Pair<ArrayList<NewsFeedUrl>, ArrayList<NewsFeed>> bottomNewsPair
                = NewsFeedArchiveUtils.loadBottomNews(context);
        mBottomNewsFeedUrlList = bottomNewsPair.first;

        if (refresh) {
            fetchBottomNewsFeedList();
        } else {
            if ((mBottomNewsFeedList = bottomNewsPair.second) != null) {
                notifyNewBottomNewsFeedListSet(false);
//                fetchBottomNewsFeedListImage();
            } else {
                fetchBottomNewsFeedList();
            }
        }

    }
    private void fetchBottomNewsFeedList() {
        final int bottomNewsCount = mBottomNewsFeedUrlList.size();

        mBottomNewsFeedList = new ArrayList<NewsFeed>();
        for (int i = 0; i < bottomNewsCount; i++) {
            mBottomNewsFeedList.add(null);
        }

        mBottomNewsFeedIndexToNewsFetchTaskMap = new SparseArray<BottomNewsFeedFetchTask>();
        for (int i = 0; i < bottomNewsCount; i++) {
            NewsFeedUrl url = mBottomNewsFeedUrlList.get(i);
            BottomNewsFeedFetchTask task = new BottomNewsFeedFetchTask(
                    getApplicationContext(), url, i, this
            );
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mBottomNewsFeedIndexToNewsFetchTaskMap.put(i, task);
        }
    }

//    private void initTopNewsImageView() {
//        mTopNewsImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //TODO Top News가 unavailable할 경우 예외처리
//
////                ActivityOptions options2 = ActivityOptions.
////                        makeSceneTransitionAnimation(NLMainActivity.this,
////                                Pair.create(mTopNewsImageView, "topImage"),
////                                Pair.create(mTopNewsTitle, "topTitle"));
////                ActivityOptions options2 = ActivityOptions.
////                        makeSceneTransitionAnimation(NLMainActivity.this,
////                                mTopNewsImageView, "");
//
//                ActivityOptions activityOptions =
//                        ActivityOptions.makeSceneTransitionAnimation(
//                                NLMainActivity.this,
//                                new Pair<View, String>(mTopNewsImageView,
//                                        mTopNewsImageView.getViewName()),
//                                new Pair<View, String>(mTopNewsTitle,
//                                        mTopNewsTitle.getViewName())
//                        );
////                ActivityOptions activityOptions2 = ActivityOptions.
////                        makeSceneTransitionAnimation(NLMainActivity.this,
////                                mTopNewsImageView, mTopNewsImageView.getViewName());
//
//                Intent intent = new Intent(NLMainActivity.this,
//                        NLDetailActivity.class);
//                intent.putExtra(NLNewsFeed.KEY_NEWS_FEED, mTopNewsFeed);
//                intent.putExtra(INTENT_KEY_VIEW_NAME_IMAGE, mTopNewsImageView.getViewName());
//                intent.putExtra(INTENT_KEY_VIEW_NAME_TITLE, mTopNewsTitle.getViewName());
//
//                startActivity(intent, activityOptions.toBundle());
//            }
//        });
//    }

    private void fetchBottomNewsFeedListImage() {
        mBottomNewsFeedNewsToImageTaskMap = new
                HashMap<News, BottomNewsImageUrlFetchTask>();

        for (int i = 0; i < mBottomNewsFeedList.size(); i++) {
            NewsFeed feed = mBottomNewsFeedList.get(i);
            ArrayList<News> newsList = feed.getNewsList();
            if (newsList.size() > 0) {
                News news = newsList.get(0);
                BottomNewsImageUrlFetchTask task = new BottomNewsImageUrlFetchTask
                        (news, i, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                mBottomNewsFeedNewsToImageTaskMap.put(news, task);
            }
        }
    }


    private void fetchTopNewsFeedImageExceptFirstNews() {
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
            mBottomNewsFeedAdapter = new BottomNewsFeedAdapter
                    (getApplicationContext(), this);
            mBottomNewsFeedRecyclerView.setAdapter(mBottomNewsFeedAdapter);

            for (int i = 0; i < mBottomNewsFeedList.size(); i++) {
                final NewsFeed newsFeed = mBottomNewsFeedList.get(i);
                mBottomNewsFeedRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBottomNewsFeedAdapter.addNewsFeed(newsFeed);
                    }
                }, BOTTOM_NEWS_FEED_ANIM_DELAY_UNIT_MILLI * i + 1);
            }
            mItemAnimator.isRunning(new RecyclerView.ItemAnimator.ItemAnimatorFinishedListener() {
                @Override
                public void onAnimationsFinished() {
                    fetchBottomNewsFeedListImage();
//                    mItemAnimator.animateAdd()
                }
            });
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

        if (findViewById(R.id.loading_container).getVisibility() == View.GONE) {
            return;
        }

        if (mTopNewsFeedReady && mBottomNewsFeedReady) {
            if (noTopNewsImage || mTopNewsFeedFirstImageReady) {
                NewsFeedArchiveUtils.save(getApplicationContext(), mTopNewsFeedUrl,
                        mTopNewsFeed, mBottomNewsFeedUrlList, mBottomNewsFeedList);
                notifyNewBottomNewsFeedListSet(true);

                // loaded
                findViewById(R.id.loading_container).setVisibility(View.GONE);
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

                    mTopNewsFeedPagerAdapter.notifyImageLoaded(
                            getApplicationContext(), position);

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
        // TODO Handle when top news fetch failed.
//        if (mDialog != null) {
//            mDialog.dismiss();
//        }
    }

    @Override
    public void onBottomNewsFeedFetchSuccess(int position,
                                             NewsFeed newsFeed) {
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
    }

    @Override
    public void onBottomItemClick(
            BottomNewsFeedAdapter.NLBottomNewsFeedViewHolder
                    viewHolder, NewsFeed newsFeed) {
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
                DetailActivity.class);
        intent.putExtra(NewsFeed.KEY_NEWS_FEED, newsFeed);
        // TODO: 리프레시 구현이 되었을 때 0을 현재 보여지고 있는 인덱스로 교체해야함
        intent.putExtra(News.KEY_NEWS, 0);
        intent.putExtra(INTENT_KEY_VIEW_NAME_IMAGE, imageView.getViewName());
        intent.putExtra(INTENT_KEY_VIEW_NAME_TITLE, titleView.getViewName());
        startActivity(intent, activityOptions.toBundle());
    }

    @Override
    public void onBottomImageUrlFetchSuccess(News news, String url,
                                             int position) {
        NLLog.i(TAG, "onBottomImageUrlFetchSuccess");
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
}
