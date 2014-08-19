package com.yooiistudios.news.main;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.android.volley.toolbox.Volley;
import com.antonioleiva.recyclerviewextensions.GridLayoutManager;
import com.yooiistudios.news.R;
import com.yooiistudios.news.detail.NLDetailActivity;
import com.yooiistudios.news.model.NLBottomNewsFeedAdapter;
import com.yooiistudios.news.model.NLBottomNewsFeedFetchTask;
import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.model.NLNewsFeed;
import com.yooiistudios.news.model.NLNewsFeedFetchTask;
import com.yooiistudios.news.model.NLNewsFeedUrl;
import com.yooiistudios.news.model.NLNewsFeedUrlType;
import com.yooiistudios.news.model.NLNewsImageUrlFetchTask;
import com.yooiistudios.news.model.NLTopNewsFeedFetchTask;
import com.yooiistudios.news.store.NLStoreActivity;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.log.NLLog;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class NLMainActivity extends Activity
        implements NLNewsImageUrlFetchTask.OnImageUrlFetchListener,
        NLTopNewsFeedFetchTask.OnFetchListener,
        NLBottomNewsFeedFetchTask.OnFetchListener {

    @InjectView(R.id.topNewsImageView) ImageView mTopNewsImageView;
    @InjectView(R.id.topNewsTitle) TextView mTopNewsTitle;
    @InjectView(R.id.bottomNewsFeedRecyclerView)
    RecyclerView mBottomNewsFeedRecyclerView;

    private static final String TAG = NLMainActivity.class.getName();
    public static NLNewsFeed sTopNewsFeed; // 저장 생각하기 귀찮아서 우선 public static으로 선언.
    private ImageLoader mImageLoader;
    private ProgressDialog mDialog;

    private NLNewsFeedFetchTask mTopFeedFetchTask;
    private NLNewsImageUrlFetchTask mTopImageUrlFetchTask;
    private NLTopNewsFeedFetchTask mTopNewsFeedFetchTask;
    private ArrayList<NLNewsFeedUrl> mBottomNewsFeedUrlList;
    private SparseArray<NLBottomNewsFeedFetchTask>
            mBottomNewsFeedIndexToTaskMap;
    private ArrayList<NLNewsFeed> mBottomNewsFeedList;
    private NLBottomNewsFeedAdapter mBottomNewsFeedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mImageLoader = new ImageLoader(Volley.newRequestQueue(this), ImageMemoryCache.INSTANCE);

        initTopNewsImageView();
        initTopNewsFeed();
        initBottomNewsFeed();

        //load news feed
//        Context context = getApplicationContext();
//        mTopFeedFetchTask = new NLNewsFeedFetchTask(context,
//                NLNewsFeedUtils.getDefaultFeedUrl(context), 10, this);
//        mTopFeedFetchTask.execute();
    }

    private void initTopNewsFeed() {
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
        GridLayoutManager layoutManager = new GridLayoutManager(
                getApplicationContext());
        layoutManager.setColumns(2);
//        mBottomNewsFeedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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

        mBottomNewsFeedIndexToTaskMap = new SparseArray<NLBottomNewsFeedFetchTask>();
        for (int i = 0; i < bottomNewsCount; i++) {
            NLNewsFeedUrl url = mBottomNewsFeedUrlList.get(i);
            NLBottomNewsFeedFetchTask task = new NLBottomNewsFeedFetchTask(
                    getApplicationContext(), url, i, this
            );
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mBottomNewsFeedIndexToTaskMap.put(i, task);
        }

    }

    private void initTopNewsImageView() {
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

                ActivityOptions activityOptions =
                        ActivityOptions.makeSceneTransitionAnimation(
                                NLMainActivity.this,
                                new Pair<View, String>(mTopNewsImageView,
                                        "topImage"),
                                new Pair<View, String>(mTopNewsTitle,
                                        "topTitle")
                        );
                ActivityOptions activityOptions2 = ActivityOptions.
                        makeSceneTransitionAnimation(NLMainActivity.this,
                                mTopNewsTitle, "topTitle");

                Intent intent = new Intent(NLMainActivity.this,
                        NLDetailActivity.class);
//                intent.putExtra(NLNewsFeed.NEWS_FEED, sTopNewsFeed);
                startActivity(intent, activityOptions.toBundle());
            }
        });
    }

    private void cancelBottomNewsFetchTasks() {
        int taskCount = mBottomNewsFeedIndexToTaskMap.size();
        for (int i = 0; i < taskCount; i++) {
            NLBottomNewsFeedFetchTask task = mBottomNewsFeedIndexToTaskMap
                    .get(i, null);
            if (task != null) {
                task.cancel(true);
            }
        }
        mBottomNewsFeedIndexToTaskMap.clear();
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
    public void onImageUrlFetchSuccess(NLNews news, String url) {
        NLLog.i(TAG, "fetch image url success.");
        String imgUrl = news.getMainImageUrl();
        if (imgUrl != null) {
            final long startMilli;

            startMilli = System.currentTimeMillis();
//            mTopNewsImageView.setImageUrl(imgUrl, mImageLoader);
            mImageLoader.get(imgUrl, new ImageLoader.ImageListener() {
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
    public void onImageUrlFetchFail() {
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
        sTopNewsFeed = newsFeed;
        ArrayList<NLNews> items = newsFeed.getNewsList();
//        ArrayList<NLNews> items = rssFeed.getNewsListContainsImageUrl();

        if (items.size() > 0) {
            NLNews news = items.get(0);
            mTopNewsTitle.setText(news.getTitle());

            mTopImageUrlFetchTask = new NLNewsImageUrlFetchTask(news, this);
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
        mBottomNewsFeedIndexToTaskMap.remove(position);
        mBottomNewsFeedList.set(position, newsFeed);

        int remainingTaskCount = mBottomNewsFeedIndexToTaskMap.size();
        if (remainingTaskCount == 0) {
            NLLog.i(TAG, "All task done. Loaded news feed list size : " +
                    mBottomNewsFeedList.size());

            mBottomNewsFeedAdapter = new NLBottomNewsFeedAdapter(
                    mBottomNewsFeedList);
            mBottomNewsFeedRecyclerView.setAdapter(mBottomNewsFeedAdapter);
        } else {
            NLLog.i(TAG, remainingTaskCount + " remaining tasks.");
        }
    }

    @Override
    public void onBottomNewsFeedFetchFail() {
        NLLog.i(TAG, "onBottomNewsFeedFetchFail");
    }
}
