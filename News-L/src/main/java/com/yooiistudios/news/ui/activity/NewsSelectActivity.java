package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.adapter.NewsSelectPagerAdapter;
import com.yooiistudios.news.ui.widget.viewpager.SlidingTabLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewsSelectActivity extends Activity {

    @InjectView(R.id.news_select_top_view_pager)    ViewPager mViewPager;
    @InjectView(R.id.news_select_sliding_tabs)      SlidingTabLayout mSlidingTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_select);
        ButterKnife.inject(this);

        mViewPager.setAdapter(new NewsSelectPagerAdapter(getFragmentManager()));
//        mViewPager.setOnPageChangeListener(mSimpleOnPageChangeListener);

        mSlidingTabLayout.setViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.news_select, menu);
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

//    private SimpleOnPageChangeListener mSimpleOnPageChangeListener
//            = new SimpleOnPageChangeListener() {
//
//        @Override
//        public void onPageSelected(int position) {
//
//        }
//    };
}
