package com.yooiistudios.news.detail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.main.NLMainActivity;
import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.model.NLNewsFeed;
import com.yooiistudios.news.model.detail.NLDetailBottomNewsAdapter;
import com.yooiistudios.news.ui.itemanimator.SlideInFromBottomItemAnimator;
import com.yooiistudios.news.util.ImageMemoryCache;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NLDetailActivity extends Activity
        implements NLDetailBottomNewsAdapter.OnItemClickListener {
    @InjectView(R.id.topNewsImage) ImageView mTopImageView;
    @InjectView(R.id.topNewsTitle) TextView mTopTitleTextView;
    @InjectView(R.id.bottomNewsListRecyclerView)
    RecyclerView mBottomNewsListRecyclerView;

    private static final int BOTTOM_NEWS_ANIM_DELAY_UNIT_MILLI = 60;

    private NLNewsFeed mNewsFeed;
    private NLNews mTopNews;
    private Bitmap mTopImageBitmap;
    private NLDetailBottomNewsAdapter mAdapter;
    private ArrayList<NLNews> mBottomNewsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.inject(this);

        // retrieve feed from intent
        mNewsFeed = getIntent().getExtras().getParcelable(NLNewsFeed.NEWS_FEED);
        String imageViewName = getIntent().getExtras().getString(NLMainActivity
                .INTENT_KEY_VIEW_NAME_IMAGE, null);
        String titleViewName = getIntent().getExtras().getString(NLMainActivity
                .INTENT_KEY_VIEW_NAME_TITLE, null);

        // set view name to animate
        mTopImageView.setViewName(imageViewName);
        mTopTitleTextView.setViewName(titleViewName);

        initTopNews();
        initBottomNewsList();
    }

    private void initTopNews() {
        mTopNews = mNewsFeed.getNewsListContainsImageUrl().get(0);

        if (mNewsFeed.getNewsListContainsImageUrl().size() > 0) {
            loadTopItem();
            if (mTopImageBitmap != null) {
                colorize(mTopImageBitmap);
            } else {
                // TODO 이미지가 없을 경우 색상 처리
            }
        } else {
            //TODO when NLNewsFeed is invalid.
        }
    }
    private void initBottomNewsList() {
        //init ui
        mBottomNewsListRecyclerView.setHasFixedSize(true);
        mBottomNewsListRecyclerView.setItemAnimator(
                new SlideInFromBottomItemAnimator(mBottomNewsListRecyclerView));
        LinearLayoutManager layoutManager = new LinearLayoutManager
                (getApplicationContext());
        mBottomNewsListRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new NLDetailBottomNewsAdapter(getApplicationContext(), this);

        mBottomNewsListRecyclerView.setAdapter(mAdapter);

        // make bottom news array list. EXCLUDE top news.
        mBottomNewsList = new ArrayList<NLNews>(mNewsFeed.getNewsList());
        mBottomNewsList.remove(mTopNews);

        for (int i = 0; i < mBottomNewsList.size(); i++) {
            final NLNews news = mBottomNewsList.get(i);
            mBottomNewsListRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.addNews(news);
                }
            }, BOTTOM_NEWS_ANIM_DELAY_UNIT_MILLI*i + 1);
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

        // set title
        mTopTitleTextView.setText(mTopNews.getTitle());

        // set image
        String imgUrl = mTopNews.getImageUrl();
        if (imgUrl != null) {
            mTopImageBitmap = cache.getBitmapFromUrl(imgUrl);

            if (mTopImageBitmap != null) {
                mTopImageView.setImageBitmap(mTopImageBitmap);
            }
            else {
                //TODO 아직 이미지 못 가져온 경우 처리

                // mTopImageBitmap
//                mHeaderImageView.setImageUrl(item.getPhotoUrl(), mImageLoader);
            }
        }
        else {
            // mTopImageBitmap
            //TODO 이미지 주소가 없을 경우 기본 이미지 보여주기
        }
    }

    private void colorize(Bitmap photo) {
        Palette palette = Palette.generate(photo);
        applyPalette(palette);
    }

    private void applyPalette(Palette palette) {
        // TODO 공식 문서가 release된 후 palette.get~ 메서드가 null을 반환할 가능성이 있는지 체크
        PaletteItem darkMutedColor =  palette.getDarkMutedColor();
        PaletteItem vibrantColor = palette.getVibrantColor();

        if (darkMutedColor != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(darkMutedColor.getRgb()));
        }

        if (vibrantColor != null) {
            mTopTitleTextView.setTextColor(vibrantColor.getRgb());
        }
    }

    @Override
    public void onItemClick(NLDetailBottomNewsAdapter.NLDetailBottomNewsViewHolder viewHolder, NLNews news) {

    }
}
