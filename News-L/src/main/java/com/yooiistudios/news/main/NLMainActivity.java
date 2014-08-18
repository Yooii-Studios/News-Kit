package com.yooiistudios.news.main;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.yooiistudios.news.R;
import com.yooiistudios.news.common.ImageMemoryCache;
import com.yooiistudios.news.common.log.NLLog;
import com.yooiistudios.news.detail.NLDetailActivity;
import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.model.NLNewsFeed;
import com.yooiistudios.news.model.NLNewsFeedFetchTask;
import com.yooiistudios.news.model.NLNewsFeedUtil;
import com.yooiistudios.news.model.NLNewsImageUrlFetchTask;
import com.yooiistudios.news.store.NLStoreActivity;

import java.util.ArrayList;


public class NLMainActivity extends Activity
        implements NLNewsFeedFetchTask.OnFetchListener,
        NLNewsImageUrlFetchTask.OnImageUrlFetchListener {
    private static final String TAG = NLMainActivity.class.getName();
    private ImageView mTopNewsImageView;
    private TextView mTopNewsTitle;
    public static NLNewsFeed sTopNewsFeed; // 저장 생각하기 귀찮아서 우선 public static으로 선언.
    private ImageLoader mImageLoader;
    private ProgressDialog mDialog;

    private NLNewsFeedFetchTask mTopFeedFetchTask;
    private NLNewsImageUrlFetchTask mTopImageUrlFetchTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageLoader = new ImageLoader(Volley.newRequestQueue(this), ImageMemoryCache.INSTANCE);

        mTopNewsImageView = (ImageView)findViewById(R.id
                .topNewsImageView);
        mTopNewsTitle = (TextView)findViewById(R.id.topNewsTitle);

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

        //load news feed
        Context context = getApplicationContext();
        mDialog = ProgressDialog.show(this,
                "blah..loading", "blah..message");
        mTopFeedFetchTask = new NLNewsFeedFetchTask(context,
                NLNewsFeedUtil.getDefaultFeedUrl(context), 10, this);
        mTopFeedFetchTask.execute();
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
    public void onSuccess(NLNewsFeed rssFeed) {
        NLLog.i(TAG, "fetch success");
        if (mDialog != null) {
            mDialog.dismiss();
        }
        sTopNewsFeed = rssFeed;
        ArrayList<NLNews> items = rssFeed.getNewsList();
//        ArrayList<NLNews> items = rssFeed.getNewsListContainsImageUrl();

        if (items.size() > 0) {
            NLNews news = items.get(0);
            mTopNewsTitle.setText(news.getTitle());

            mTopImageUrlFetchTask = new NLNewsImageUrlFetchTask(news, this);
            mTopImageUrlFetchTask.execute();
        } else {
            //TODO 이미지가 없을 경우 예외처리
        }
    }

    @Override
    public void onCancel() {
        NLLog.i(TAG, "fetch canceled");
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onError() {
        NLLog.i(TAG, "fetch error");
        if (mDialog != null) {
            mDialog.dismiss();
        }
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
}
