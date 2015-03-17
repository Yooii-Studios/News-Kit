package com.yooiistudios.newsflow.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.util.DisplayMetrics;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.util.DipToPixel;
import com.yooiistudios.newsflow.model.BitmapLoadTask;
import com.yooiistudios.newsflow.ui.activity.NewsContentActivity;
import com.yooiistudios.newsflow.ui.activity.NewsWebActivity;
import com.yooiistudios.newsflow.ui.presenter.NewsDescriptionPresenter;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * DetailsFragment
 *  뉴스 링크를 웹에서 볼 수 있는 프래그먼트
 */
public class NewsFragment extends DetailsFragment {
    private static final int ACTION_OPEN_LINK = 1;
    private static final int ACTION_SEE_CONTENT = 2;

    private static final int DETAIL_THUMB_WIDTH = 273;
    private static final int DETAIL_THUMB_HEIGHT = 273;

    public static final String ARG_NEWS_KEY = "arg_news_key";

    private BackgroundManager mBackgroundManager;
    private News mNews;

    private DisplayMetrics mMetrics;
    private DetailsOverviewRowPresenter mDorPresenter;
    private DetailsOverviewRow mRow;

    private BitmapLoadTask mImageLoadTask;
    private BitmapLoadTask mBackgroundLoadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNews();
        initMetrics();
        initBackground();
        initPresenter();
        initUI();

        loadImages();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mImageLoadTask != null) {
            mImageLoadTask.cancel(true);
        }
        if (mBackgroundLoadTask != null) {
            mBackgroundLoadTask.cancel(true);
        }
    }

    private void initNews() {
        mNews = getActivity().getIntent().getExtras().getParcelable(MainFragment.ARG_NEWS_KEY);
    }

    private void initMetrics() {
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void initPresenter() {
        mDorPresenter = new DetailsOverviewRowPresenter(new NewsDescriptionPresenter());
        // set detail background and style
        mDorPresenter.setBackgroundColor(getResources().getColor(R.color.detail_background));
        mDorPresenter.setStyleLarge(true);
        mDorPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_OPEN_LINK) {
                    Intent intent = new Intent(getActivity(), NewsWebActivity.class);
                    intent.putExtra(ARG_NEWS_KEY, mNews);
                    startActivity(intent);
                } else if (action.getId() == ACTION_SEE_CONTENT){
                    Intent intent = new Intent(getActivity(), NewsContentActivity.class);
                    intent.putExtra(ARG_NEWS_KEY, mNews);
                    startActivity(intent);
                }
            }
        });
    }

    private void initUI() {
        initRow();
        initAdapter();
    }

    private void loadImages() {
        loadHeroImage();
        loadBackgroundImage();
    }

    private void initRow() {
        mRow = new DetailsOverviewRow(mNews);

        mRow.addAction(new Action(ACTION_OPEN_LINK,
                getResources().getString(R.string.open_link), null));
        if (mNews.getDisplayableNewsContentDescription().length() > NewsContentFragment.MIN_TEXT_LENGTH) {
            mRow.addAction(new Action(ACTION_SEE_CONTENT,
                    getResources().getString(R.string.see_content), null));
        }
    }

    private void initAdapter() {
        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(DetailsOverviewRow.class, mDorPresenter);

        ArrayObjectAdapter adapter = new ArrayObjectAdapter(presenterSelector);
        adapter.add(mRow);
        setAdapter(adapter);
    }

    private void initBackground() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
    }

    private void loadHeroImage() {
        Context context = getActivity().getApplicationContext();
        int width = DipToPixel.dpToPixel(context, DETAIL_THUMB_WIDTH);
        int height = DipToPixel.dpToPixel(context, DETAIL_THUMB_HEIGHT);

        mImageLoadTask = new BitmapLoadTask(context, mNews.getImageUrl(), width, height,
                R.drawable.news_dummy, new BitmapLoadTask.OnSuccessListener() {
            @Override
            public void onLoad(Drawable drawable) {
                mRow.setImageDrawable(drawable);
                ((ArrayObjectAdapter)getAdapter()).notifyArrayItemRangeChanged(0, 1);
            }
        });
        mImageLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadBackgroundImage() {
        Context context = getActivity().getApplicationContext();

        mBackgroundLoadTask = new BitmapLoadTask(context, mNews.getImageUrl(),
                mMetrics.widthPixels, mMetrics.heightPixels,
                R.drawable.default_background, new BitmapLoadTask.OnSuccessListener() {
            @Override
            public void onLoad(Drawable drawable) {
                mBackgroundManager.setDrawable(drawable);
            }
        });
        mBackgroundLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
