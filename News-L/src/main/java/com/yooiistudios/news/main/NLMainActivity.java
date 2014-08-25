package com.yooiistudios.news.main;

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
import com.yooiistudios.news.NLNewsApplication;
import com.yooiistudios.news.R;
import com.yooiistudios.news.detail.NLDetailActivity;
import com.yooiistudios.news.model.NLNewsFeedFetchTask;
import com.yooiistudios.news.model.NLNewsFeedUrl;
import com.yooiistudios.news.model.main.NLBottomNewsFeedAdapter;
import com.yooiistudios.news.model.main.NLBottomNewsFeedFetchTask;
import com.yooiistudios.news.model.main.NLBottomNewsImageUrlFetchTask;
import com.yooiistudios.news.model.main.NLTopFeedNewsImageUrlFetchTask;
import com.yooiistudios.news.model.main.NLTopNewsFeedFetchTask;
import com.yooiistudios.news.model.main.NLTopNewsFeedPagerAdapter;
import com.yooiistudios.news.model.news.NLNews;
import com.yooiistudios.news.model.news.NLNewsFeed;
import com.yooiistudios.news.model.news.NLNewsFeedArchiveUtils;
import com.yooiistudios.news.store.NLStoreActivity;
import com.yooiistudios.news.ui.itemanimator.SlideInFromBottomItemAnimator;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.log.NLLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class NLMainActivity extends Activity
        implements NLTopFeedNewsImageUrlFetchTask.OnTopFeedImageUrlFetchListener,
        NLTopNewsFeedFetchTask.OnFetchListener,
        NLBottomNewsFeedFetchTask.OnFetchListener,
        NLBottomNewsFeedAdapter.OnItemClickListener,
        NLBottomNewsImageUrlFetchTask.OnBottomImageUrlFetchListener {

    @InjectView(R.id.main_top_view_pager)           ViewPager mTopNewsFeedViewPager;
    @InjectView(R.id.main_top_page_indicator)       CirclePageIndicator mTopViewPagerIndicator;
    @InjectView(R.id.bottomNewsFeedRecyclerView)    RecyclerView mBottomNewsFeedRecyclerView;

    private static final String TAG = NLMainActivity.class.getName();
    public static final String VIEW_NAME_IMAGE_PREFIX = "topImage_";
    public static final String VIEW_NAME_TITLE_PREFIX = "topTitle_";
    public static final String INTENT_KEY_VIEW_NAME_IMAGE = "INTENT_KEY_VIEW_NAME_IMAGE";
    public static final String INTENT_KEY_VIEW_NAME_TITLE = "INTENT_KEY_VIEW_NAME_TITLE";
    private static final int BOTTOM_NEWS_FEED_ANIM_DELAY_UNIT_MILLI = 60;

    private ImageLoader mImageLoader;
//    private ProgressDialog mDialog;

    private NLNewsFeedUrl mTopNewsFeedUrl;
    private NLNewsFeed mTopNewsFeed;
    private ArrayList<NLNewsFeedUrl> mBottomNewsFeedUrlList;
    private ArrayList<NLNewsFeed> mBottomNewsFeedList;

    private NLNewsFeedFetchTask mTopFeedFetchTask;
    private NLTopFeedNewsImageUrlFetchTask mTopImageUrlFetchTask;
    private NLTopNewsFeedFetchTask mTopNewsFeedFetchTask;
    private SparseArray<NLBottomNewsFeedFetchTask>
            mBottomNewsFeedIndexToNewsFetchTaskMap;
    private HashMap<NLNews, NLBottomNewsImageUrlFetchTask>
            mBottomNewsFeedNewsToImageTaskMap;
    private HashMap<NLNews, NLTopFeedNewsImageUrlFetchTask>
            mTopNewsFeedNewsToImageTaskMap;
    private NLBottomNewsFeedAdapter mBottomNewsFeedAdapter;
    private NLTopNewsFeedPagerAdapter mTopNewsFeedPagerAdapter;

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
        mImageLoader = new ImageLoader(((NLNewsApplication)getApplication()).getRequestQueue(),
                ImageMemoryCache.getInstance(getApplicationContext()));

        boolean needsRefresh = NLNewsFeedArchiveUtils.newsNeedsToBeRefreshed(getApplicationContext());

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
        Pair<NLNewsFeedUrl, NLNewsFeed> topFeedPair = NLNewsFeedArchiveUtils.loadTopNews(context);
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
        mTopNewsFeedFetchTask = new NLTopNewsFeedFetchTask(this,
                mTopNewsFeedUrl, this);
        mTopNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void notifyNewTopNewsFeedSet() {
        mTopNewsFeedReady = true;
        ArrayList<NLNews> items = mTopNewsFeed.getNewsList();
        mTopNewsFeedPagerAdapter = new NLTopNewsFeedPagerAdapter(getFragmentManager(), mTopNewsFeed);

        mTopNewsFeedViewPager.setAdapter(mTopNewsFeedPagerAdapter);
        mTopViewPagerIndicator.setViewPager(mTopNewsFeedViewPager);

        if (items.size() > 0) {
            NLNews news = items.get(0);
            mTopNewsFeedFirstImageReady = false;

            mTopImageUrlFetchTask = new NLTopFeedNewsImageUrlFetchTask(news, 0, this);
            mTopImageUrlFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            //TODO 해당 피드의 뉴스가 없을 경우 예외처리. Use dummy image.

            mTopNewsFeedFirstImageReady = true;
            fetchTopNewsFeedImageExceptFirstNews();
        }
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

        Pair<ArrayList<NLNewsFeedUrl>, ArrayList<NLNewsFeed>> bottomNewsPair
                = NLNewsFeedArchiveUtils.loadBottomNews(context);
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

        mBottomNewsFeedList = new ArrayList<NLNewsFeed>();
        for (int i = 0; i < bottomNewsCount; i++) {
            mBottomNewsFeedList.add(null);
        }

        mBottomNewsFeedIndexToNewsFetchTaskMap = new SparseArray<NLBottomNewsFeedFetchTask>();
        for (int i = 0; i < bottomNewsCount; i++) {
            NLNewsFeedUrl url = mBottomNewsFeedUrlList.get(i);
            NLBottomNewsFeedFetchTask task = new NLBottomNewsFeedFetchTask(
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
                HashMap<NLNews, NLBottomNewsImageUrlFetchTask>();

        for (int i = 0; i < mBottomNewsFeedList.size(); i++) {
            NLNewsFeed feed = mBottomNewsFeedList.get(i);
            ArrayList<NLNews> newsList = feed.getNewsList();
            if (newsList.size() > 0) {
                NLNews news = newsList.get(0);
                NLBottomNewsImageUrlFetchTask task = new NLBottomNewsImageUrlFetchTask
                        (news, i, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                mBottomNewsFeedNewsToImageTaskMap.put(news, task);
            }
        }
    }


    private void fetchTopNewsFeedImageExceptFirstNews() {
        mTopNewsFeedNewsToImageTaskMap = new
                HashMap<NLNews, NLTopFeedNewsImageUrlFetchTask>();

        ArrayList<NLNews> newsList = mTopNewsFeed.getNewsList();

        for (int i = 1; i < newsList.size(); i++) {
            NLNews news = newsList.get(i);

            NLTopFeedNewsImageUrlFetchTask task = new
                    NLTopFeedNewsImageUrlFetchTask(news, i, this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mTopNewsFeedNewsToImageTaskMap.put(news, task);
        }
    }
    private void cancelTopNewsFeedImageFetchTasks() {
        mTopNewsFeedReady = false;
        for (Map.Entry<NLNews, NLTopFeedNewsImageUrlFetchTask> entry :
                mTopNewsFeedNewsToImageTaskMap.entrySet()) {
            NLTopFeedNewsImageUrlFetchTask task = entry.getValue();
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
            NLBottomNewsFeedFetchTask task = mBottomNewsFeedIndexToNewsFetchTaskMap
                    .get(i, null);
            if (task != null) {
                task.cancel(true);
            }
        }
        mBottomNewsFeedIndexToNewsFetchTaskMap.clear();

        for (Map.Entry<NLNews, NLBottomNewsImageUrlFetchTask> entry :
                mBottomNewsFeedNewsToImageTaskMap.entrySet()) {
            NLBottomNewsImageUrlFetchTask task = entry.getValue();
            if (task != null) {
                task.cancel(true);
            }
        }
        mBottomNewsFeedNewsToImageTaskMap.clear();
    }
    private void notifyNewBottomNewsFeedListSet(boolean animate) {
        mBottomNewsFeedReady = true;

        if (animate) {
            mBottomNewsFeedAdapter = new NLBottomNewsFeedAdapter
                    (getApplicationContext(), this);
            mBottomNewsFeedRecyclerView.setAdapter(mBottomNewsFeedAdapter);

            for (int i = 0; i < mBottomNewsFeedList.size(); i++) {
                final NLNewsFeed newsFeed = mBottomNewsFeedList.get(i);
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
                NLNewsFeedArchiveUtils.save(getApplicationContext(), mTopNewsFeedUrl,
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
            startActivity(new Intent(NLMainActivity.this, NLStoreActivity.class));
            return true;
        } else if (id == R.id.action_remove_archive) {
            NLNewsFeedArchiveUtils.clearArchive(getApplicationContext());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTopFeedImageUrlFetchSuccess(NLNews news, String url,
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
    public void onTopNewsFeedFetchSuccess(NLNewsFeed newsFeed) {
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
                                             NLNewsFeed newsFeed) {
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
            NLBottomNewsFeedAdapter.NLBottomNewsFeedViewHolder
                    viewHolder, NLNewsFeed newsFeed) {
        NLLog.i(TAG, "onBottomItemClick");
        NLLog.i(TAG, "newsFeed : " + newsFeed.getTitle());

        ImageView imageView = viewHolder.imageView;
        TextView titleView = viewHolder.newsTitleTextView;


        ActivityOptions activityOptions =
                ActivityOptions.makeSceneTransitionAnimation(
                        NLMainActivity.this,
                        new Pair<View, String>(imageView, imageView.getViewName()),
                        new Pair<View, String>(titleView, titleView.getViewName())
                );
//        ActivityOptions activityOptions2 = ActivityOptions.
//                makeSceneTransitionAnimation(NLMainActivity.this,
//                        imageView, imageView.getViewName());

        Intent intent = new Intent(NLMainActivity.this,
                NLDetailActivity.class);
        intent.putExtra(NLNewsFeed.KEY_NEWS_FEED, newsFeed);
        // TODO: 리프레시 구현이 되었을 때 0을 현재 보여지고 있는 인덱스로 교체해야함
        intent.putExtra(NLNews.KEY_NEWS, 0);
        intent.putExtra(INTENT_KEY_VIEW_NAME_IMAGE, imageView.getViewName());
        intent.putExtra(INTENT_KEY_VIEW_NAME_TITLE, titleView.getViewName());
        startActivity(intent, activityOptions.toBundle());
    }

    @Override
    public void onBottomImageUrlFetchSuccess(NLNews news, String url,
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
