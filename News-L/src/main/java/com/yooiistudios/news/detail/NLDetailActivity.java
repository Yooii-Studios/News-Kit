package com.yooiistudios.news.detail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.common.ImageMemoryCache;
import com.yooiistudios.news.main.NLMainActivity;
import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.model.NLNewsFeed;

public class NLDetailActivity extends Activity {
    private ImageView mTopImageView;
    private NLNewsFeed mNewsFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // retrieve feed from intent
//        mNewsFeed = getIntent().getParcelableExtra(NLNewsFeed.NEWS_FEED);
        mNewsFeed = NLMainActivity.mTopNewsFeed;

        mTopImageView = (ImageView)findViewById(R.id.newsImage);

        if (mNewsFeed.getNewsListContainsImageUrl().size() > 0) {
            loadTopItem();
        }
        else {
            //TODO when NLNewsFeed is invalid.
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTopItem() {
        final ImageMemoryCache cache = ImageMemoryCache.INSTANCE;
        NLNews news = mNewsFeed.getNewsListContainsImageUrl().get(0);
        String imgUrl = news.getMainImageUrl();
        if (imgUrl != null) {
            Bitmap topNewsImage = cache.getBitmapFromUrl(imgUrl);

            if (topNewsImage != null) {
                mTopImageView.setImageBitmap(topNewsImage);
            }
            else {
                //TODO 아직 이미지 못 가져온 경우 처리
//                mHeaderImageView.setImageUrl(item.getPhotoUrl(), mImageLoader);
            }
        }
        else {
            //TODO 이미지 주소가 없을 경우 기본 이미지 보여주기
        }
    }
}
