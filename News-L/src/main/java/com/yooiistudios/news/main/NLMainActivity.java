package com.yooiistudios.news.main;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.antonioleiva.recyclerviewextensions.GridLayoutManager;
import com.yooiistudios.news.R;
import com.yooiistudios.news.detail.NLDetailActivity;
import com.yooiistudios.news.model.NLBottomNewsFeedAdapter;
import com.yooiistudios.news.model.NLBottomNewsFeedFetchTask;
import com.yooiistudios.news.model.NLBottomNewsImageUrlFetchTask;
import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.model.NLNewsFeed;
import com.yooiistudios.news.model.NLNewsFeedFetchTask;
import com.yooiistudios.news.model.NLNewsFeedUrl;
import com.yooiistudios.news.model.NLNewsFeedUrlType;
import com.yooiistudios.news.model.NLTopNewsFeedFetchTask;
import com.yooiistudios.news.model.NLTopNewsImageUrlFetchTask;
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
        implements NLTopNewsImageUrlFetchTask.OnTopImageUrlFetchListener,
        NLTopNewsFeedFetchTask.OnFetchListener,
        NLBottomNewsFeedFetchTask.OnFetchListener,
        NLBottomNewsFeedAdapter.OnItemClickListener,
        NLBottomNewsImageUrlFetchTask.OnBottomImageUrlFetchListener {

    @InjectView(R.id.topNewsImageView) ImageView mTopNewsImageView;
    @InjectView(R.id.topNewsTitle) TextView mTopNewsTitle;
    @InjectView(R.id.bottomNewsFeedRecyclerView)
    RecyclerView mBottomNewsFeedRecyclerView;

    private static final String TAG = NLMainActivity.class.getName();
    public static final String VIEW_NAME_IMAGE_PREFIX = "topImage";
    public static final String VIEW_NAME_TITLE_PREFIX = "topTitle";
    public static final String INTENT_KEY_VIEW_NAME_IMAGE = "INTENT_KEY_VIEW_NAME_IMAGE";
    public static final String INTENT_KEY_VIEW_NAME_TITLE = "INTENT_KEY_VIEW_NAME_TITLE";

    public NLNewsFeed mTopNewsFeed; // 저장 생각하기 귀찮아서 우선 public static으로 선언.
    private ImageLoader mImageLoader;
    private ProgressDialog mDialog;

    private NLNewsFeedFetchTask mTopFeedFetchTask;
    private NLTopNewsImageUrlFetchTask mTopImageUrlFetchTask;
    private NLTopNewsFeedFetchTask mTopNewsFeedFetchTask;
    private ArrayList<NLNewsFeedUrl> mBottomNewsFeedUrlList;
    private SparseArray<NLBottomNewsFeedFetchTask>
            mBottomNewsFeedIndexToNewsFetchTaskMap;
    private HashMap<NLNews, NLBottomNewsImageUrlFetchTask>
            mBottomNewsFeedNewsToImageTaskMap;
    private ArrayList<NLNewsFeed> mBottomNewsFeedList;
    private NLBottomNewsFeedAdapter mBottomNewsFeedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mImageLoader = new ImageLoader(Volley.newRequestQueue(this), ImageMemoryCache.INSTANCE);

        // TODO off-line configuration
        initTopNewsFeed();
        initBottomNewsFeed();

        //load news feed
//        Context context = getApplicationContext();
//        mTopFeedFetchTask = new NLNewsFeedFetchTask(context,
//                NLNewsFeedUtils.getDefaultFeedUrl(context), 10, this);
//        mTopFeedFetchTask.execute();
    }

    private void initTopNewsFeed() {
        initTopNewsImageView();
        mTopNewsTitle.setViewName(VIEW_NAME_TITLE_PREFIX);

        // Dialog
        mDialog = ProgressDialog.show(this, "blah..loading", "blah..message");

        // Fetch
        mTopNewsFeedFetchTask =
                new NLTopNewsFeedFetchTask(this, new NLNewsFeedUrl(
                        "http://feeds2.feedburner.com/time/topstories",
                        NLNewsFeedUrlType.GENERAL), this);
        mTopNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
    private void initBottomNewsFeed() {
        //init ui
        mBottomNewsFeedRecyclerView.setHasFixedSize(true);
//        ((ViewGroup)mBottomNewsFeedRecyclerView).setTransitionGroup(false);
        mBottomNewsFeedRecyclerView.setItemAnimator(
                new SlideInFromBottomItemAnimator(mBottomNewsFeedRecyclerView));
        GridLayoutManager layoutManager = new GridLayoutManager(
                getApplicationContext());
        layoutManager.setColumns(2);
//        LinearLayoutManager layoutManager = new LinearLayoutManager
//                (getApplicationContext());
        mBottomNewsFeedRecyclerView.setLayoutManager(layoutManager);

        mBottomNewsFeedList = new ArrayList<NLNewsFeed>();
        mBottomNewsFeedUrlList = new ArrayList<NLNewsFeedUrl>();

        final int bottomNewsCount = 6;

        for (int i = 0; i < bottomNewsCount; i++) {
            mBottomNewsFeedUrlList.add(new NLNewsFeedUrl(
                    "http://feeds2.feedburner.com/time/topstories",
                    NLNewsFeedUrlType.GENERAL));
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

    private void initTopNewsImageView() {
        mTopNewsImageView.setViewName(VIEW_NAME_IMAGE_PREFIX);
        mTopNewsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Top News가 unavailable할 경우 예외처리

//                ActivityOptions options2 = ActivityOptions.
//                        makeSceneTransitionAnimation(NLMainActivity.this,
//                                Pair.create(mTopNewsImageView, "topImage"),
//                                Pair.create(mTopNewsTitle, "topTitle"));
//                ActivityOptions options2 = ActivityOptions.
//                        makeSceneTransitionAnimation(NLMainActivity.this,
//                                mTopNewsImageView, "");

//                ActivityOptions activityOptions =
//                        ActivityOptions.makeSceneTransitionAnimation(
//                                NLMainActivity.this,
//                                new Pair<View, String>(mTopNewsImageView,
//                                        mTopNewsImageView.getViewName()),
//                                new Pair<View, String>(mTopNewsTitle,
//                                        mTopNewsTitle.getViewName())
//                        );
                ActivityOptions activityOptions2 = ActivityOptions.
                        makeSceneTransitionAnimation(NLMainActivity.this,
                                mTopNewsImageView, mTopNewsImageView.getViewName());

                Intent intent = new Intent(NLMainActivity.this,
                        NLDetailActivity.class);
                intent.putExtra(NLNewsFeed.NEWS_FEED, mTopNewsFeed);
                intent.putExtra(INTENT_KEY_VIEW_NAME_IMAGE, mTopNewsImageView.getViewName());
                intent.putExtra(INTENT_KEY_VIEW_NAME_TITLE, mTopNewsTitle.getViewName());

                startActivity(intent, activityOptions2.toBundle());
            }
        });
    }

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

    private void cancelBottomNewsFetchTasks() {
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
    private void showNewBottomNewsFeedList(ArrayList<NLNewsFeed>
                                                   newNewsFeedList) {
        mBottomNewsFeedAdapter = new NLBottomNewsFeedAdapter
                (getApplicationContext(), this);
        mBottomNewsFeedRecyclerView.setAdapter(mBottomNewsFeedAdapter);

        for (int i = 0; i < newNewsFeedList.size(); i++) {
            final NLNewsFeed newsFeed = newNewsFeedList.get(i);
            mBottomNewsFeedRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBottomNewsFeedAdapter.addNewsFeed(newsFeed);
                }
            }, 60*i + 1);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTopImageUrlFetchSuccess(NLNews news, String url) {
        NLLog.i(TAG, "fetch image url success.");
        if (url != null) {
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
                    mTopNewsImageView.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
    }

    @Override
    public void onTopImageUrlFetchFail() {
        NLLog.i(TAG, "fetch image url failed.");
    }

    /**
     * TopNewsFeedFetch Listener
     */
    @Override
    public void onTopNewsFeedFetchSuccess(NLNewsFeed newsFeed) {
        NLLog.i(TAG, "onTopNewsFeedFetchSuccess");
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mTopNewsFeed = newsFeed;
        ArrayList<NLNews> items = newsFeed.getNewsList();
//        ArrayList<NLNews> items = rssFeed.getNewsListContainsImageUrl();

        if (items.size() > 0) {
            NLNews news = items.get(0);
            mTopNewsTitle.setText(news.getTitle());

            mTopImageUrlFetchTask = new NLTopNewsImageUrlFetchTask(news, this);
            mTopImageUrlFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            //TODO 이미지가 없을 경우 예외처리
        }
    }

    @Override
    public void onTopNewsFeedFetchFail() {
        NLLog.i(TAG, "onTopNewsFeedFetchFail");
        if (mDialog != null) {
            mDialog.dismiss();
        }
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

            fetchBottomNewsFeedListImage();

            showNewBottomNewsFeedList(mBottomNewsFeedList);
        } else {
            NLLog.i(TAG, remainingTaskCount + " remaining tasks.");
        }
    }

    @Override
    public void onBottomNewsFeedFetchFail() {
        NLLog.i(TAG, "onBottomNewsFeedFetchFail");
    }

    @Override
    public void onItemClick(
            NLBottomNewsFeedAdapter.NLBottomNewsFeedViewHolder
                    viewHolder, NLNewsFeed newsFeed) {
        NLLog.i(TAG, "onItemClick");
        NLLog.i(TAG, "newsFeed : " + newsFeed.getTitle());

        ImageView imageView = viewHolder.imageView;
        TextView titleView = viewHolder.feedName;


//        ActivityOptions activityOptions =
//                ActivityOptions.makeSceneTransitionAnimation(
//                        NLMainActivity.this,
//                        new Pair<View, String>(imageView, imageView.getViewName()),
//                        new Pair<View, String>(titleView, titleView.getViewName())
//                );
        ActivityOptions activityOptions2 = ActivityOptions.
                makeSceneTransitionAnimation(NLMainActivity.this,
                        imageView, imageView.getViewName());

        Intent intent = new Intent(NLMainActivity.this,
                NLDetailActivity.class);
        intent.putExtra(NLNewsFeed.NEWS_FEED, newsFeed);
        intent.putExtra(INTENT_KEY_VIEW_NAME_IMAGE, imageView.getViewName());
        intent.putExtra(INTENT_KEY_VIEW_NAME_TITLE, titleView.getViewName());
        startActivity(intent, activityOptions2.toBundle());
    }

    @Override
    public void onBottomImageUrlFetchSuccess(NLNews news, String url,
                                             int position) {
        NLLog.i(TAG, "onTopImageUrlFetchSuccess");
        if (url != null) {
            news.setImageUrl(url);
            mBottomNewsFeedAdapter.notifyItemChanged(position);

            mBottomNewsFeedNewsToImageTaskMap.remove(news);

            NLLog.i(TAG, "title : " + news.getTitle() + "'s image url fetch " +
                    "success.\nimage url : " + url);
        }
    }

    @Override
    public void onBottomImageUrlFetchFail() {
        NLLog.i(TAG, "onTopImageUrlFetchFail");
    }
}
