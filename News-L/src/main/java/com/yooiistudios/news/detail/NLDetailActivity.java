package com.yooiistudios.news.detail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.main.NLMainActivity;
import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.model.NLNewsFeed;
import com.yooiistudios.news.util.ImageMemoryCache;

public class NLDetailActivity extends Activity {
    private ImageView mTopImageView;
    private TextView mTopTitleTextView;
    private NLNewsFeed mNewsFeed;
    private NLNews mTopNews;
    private Bitmap mTopImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // retrieve feed from intent
        mNewsFeed = getIntent().getExtras().getParcelable(NLNewsFeed.NEWS_FEED);
        String imageViewName = getIntent().getExtras().getString(NLMainActivity
                .INTENT_KEY_VIEW_NAME_IMAGE, null);
        String titleViewName = getIntent().getExtras().getString(NLMainActivity
                .INTENT_KEY_VIEW_NAME_TITLE, null);

        mTopImageView = (ImageView)findViewById(R.id.newsImage);
        mTopTitleTextView = (TextView)findViewById(R.id.newsTitle);

        mTopImageView.setViewName(imageViewName);
        mTopTitleTextView.setViewName(titleViewName);

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
}
