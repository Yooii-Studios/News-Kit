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
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.yooiistudios.news.R;
import com.yooiistudios.news.common.ImageMemoryCache;
import com.yooiistudios.news.detail.NLDetailActivity;
import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.model.NLNewsFeed;
import com.yooiistudios.news.model.NLNewsFeedFetchTask;
import com.yooiistudios.news.model.NLNewsFeedUtil;
import com.yooiistudios.news.store.NLStoreActivity;

import java.util.ArrayList;


public class NLMainActivity extends Activity {
    private NetworkImageView mTopNewsImageView;
    private TextView mTopNewsTitle;

    // 저장 생각하기 귀찮아서 우선 public static으로 선언.
    public static NLNewsFeed sTopNewsFeed;

    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageLoader = new ImageLoader(Volley.newRequestQueue(this), ImageMemoryCache.INSTANCE);

        mTopNewsImageView = (NetworkImageView)findViewById(R.id
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
        final ProgressDialog dialog = ProgressDialog.show(this,
                "blah..loading", "blah..message");
        NLNewsFeedFetchTask fetchTask = new NLNewsFeedFetchTask(context,
                NLNewsFeedUtil.getDefaultFeedUrl(context),
                new NLNewsFeedFetchTask.OnFetchListener() {
                    @Override
                    public void onFetch(NLNewsFeed rssFeed) {
                        dialog.dismiss();
                        sTopNewsFeed = rssFeed;
//                        ArrayList<NLNews> items = rssFeed.getNewsList();
                        ArrayList<NLNews> items = rssFeed.getNewsListContainsImageUrl();

                        if (items.size() > 0) {
                            NLNews news = items.get(0);
                            String imgUrl = news.getMainImageUrl();
                            if (imgUrl != null) {
                                mTopNewsImageView.setImageUrl(imgUrl, mImageLoader);
                            }
                            mTopNewsTitle.setText(news.getTitle());
                        } else {
                            //TODO 이미지가 없을 경우 예외처리
                        }
                    }

                    @Override
                    public void onCancel() {
                        dialog.dismiss();
                    }

                    @Override
                    public void onError() {
                        dialog.dismiss();
                    }
                });
        fetchTask.execute();
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
}
