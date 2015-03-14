package com.yooiistudios.newsflow.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.util.DipToPixel;
import com.yooiistudios.newsflow.model.PicassoBackgroundManagerTarget;
import com.yooiistudios.newsflow.ui.activity.NewsContentActivity;
import com.yooiistudios.newsflow.ui.activity.NewsWebActivity;
import com.yooiistudios.newsflow.ui.presenter.NewsDescriptionPresenter;

import java.io.IOException;

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

    private News mNews;
    private boolean mHasImage;
//    private String mLink;

    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private DetailsOverviewRowPresenter mDorPresenter;
    private DetailRowBuilderTask mDetailRowBuilderTask;
    private DetailsOverviewRow mRow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNews();
        initIntentVariables();
        initMetrics();
        initBackground();
        initPresenter();
        initUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        mDetailRowBuilderTask.cancel(true);
    }

    private void initNews() {
        mNews = getActivity().getIntent().getExtras().getParcelable(MainFragment.ARG_NEWS_KEY);
    }

    private void initIntentVariables() {
        mHasImage = getActivity().getIntent().getExtras().getBoolean(MainFragment.ARG_HAS_IMAGE_KEY);
    }

    private void initMetrics() {
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void initPresenter() {
        mDorPresenter =
                new DetailsOverviewRowPresenter(new NewsDescriptionPresenter());
        mDetailRowBuilderTask = (DetailRowBuilderTask) new DetailRowBuilderTask()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // FIXME: 나중에 시간 여유가 되면 애니메이션을 제대로 수정해주자
//        mDorPresenter.setSharedElementEnterTransition(getActivity(),
//                NewsActivity.SHARED_ELEMENT_NAME);
    }

    private void initUI() {
        initRow();
        initAdapter();
        updateBackground(mNews.getImageUrl());
    }

    private void initRow() {
        mRow = new DetailsOverviewRow(mNews);

        mRow.addAction(new Action(ACTION_OPEN_LINK,
                getResources().getString(R.string.open_link), null));
        mRow.addAction(new Action(ACTION_SEE_CONTENT,
                getResources().getString(R.string.see_content),
                null));
    }

    private void initAdapter() {
        ClassPresenterSelector ps = new ClassPresenterSelector();
        // set detail background and style
        mDorPresenter.setBackgroundColor(getResources().getColor(R.color.detail_background));
        mDorPresenter.setStyleLarge(true);
        mDorPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_OPEN_LINK) {
//                        Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
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

        ps.addClassPresenter(DetailsOverviewRow.class, mDorPresenter);

        ArrayObjectAdapter adapter = new ArrayObjectAdapter(ps);
        adapter.add(mRow);
        setAdapter(adapter);
    }

    private Bitmap getDefaultImage() {
        Drawable drawable = getActivity().getResources().getDrawable(R.drawable.news_dummy2);
        return ((BitmapDrawable)drawable).getBitmap();
    }

    private Bitmap getImageFromUrl() {
        Bitmap poster;
        try {
            poster = Picasso.with(getActivity())
                    .load(mNews.getImageUrl())
                    .resize(DipToPixel.dpToPixel(getActivity().getApplicationContext(),
                                    DETAIL_THUMB_WIDTH),
                            DipToPixel.dpToPixel(getActivity().getApplicationContext(),
                                    DETAIL_THUMB_HEIGHT))
                    .centerCrop()
                    .error(getActivity().getResources().getDrawable(R.drawable
                            .news_dummy2))
                    .get();
        } catch (IOException ignored) {
            poster = getDefaultImage();
        }
        return poster;
    }

    private void initBackground() {
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);

        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);
    }

    private class DetailRowBuilderTask extends AsyncTask<Void, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap poster;
            if (mHasImage) {
                poster = getImageFromUrl();
            } else {
                poster = getDefaultImage();
            }
            return poster;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                return;
            }
            mRow.setImageBitmap(getActivity(), bitmap);
            setAdapter(getAdapter());

//            ClassPresenterSelector ps = new ClassPresenterSelector();
//            // set detail background and style
//            mDorPresenter.setBackgroundColor(getResources().getColor(R.color.detail_background));
//            mDorPresenter.setStyleLarge(true);
//            mDorPresenter.setOnActionClickedListener(new OnActionClickedListener() {
//                @Override
//                public void onActionClicked(Action action) {
//                    if (action.getId() == ACTION_OPEN_LINK) {
////                        Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(getActivity(), NewsWebActivity.class);
//                        intent.putExtra(ARG_NEWS_KEY, mNews);
//                        startActivity(intent);
//                    } else if (action.getId() == ACTION_SEE_CONTENT){
//                        Intent intent = new Intent(getActivity(), NewsContentActivity.class);
//                        intent.putExtra(ARG_NEWS_KEY, mNews);
//                        startActivity(intent);
//                    }
//                }
//            });
//
//            ps.addClassPresenter(DetailsOverviewRow.class, mDorPresenter);
//
//            ArrayObjectAdapter adapter = new ArrayObjectAdapter(ps);
//            adapter.add(detailRow);
//            setAdapter(adapter);
        }
    }

    private void updateBackground(String url) {
        Picasso.with(getActivity())
                .load(url)
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .centerCrop()
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
    }
}
