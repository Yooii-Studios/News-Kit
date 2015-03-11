package com.yooiistudios.newsflow.ui.fragment;

import android.graphics.Bitmap;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.model.PicassoBackgroundManagerTarget;
import com.yooiistudios.newsflow.reference.Utils;
import com.yooiistudios.newsflow.ui.activity.NewsDetailsActivity;
import com.yooiistudios.newsflow.ui.presenter.NewsDetailsDescriptionPresenter;

import java.io.IOException;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * DetailsFragment
 *  뉴스 링크를 웹에서 볼 수 있는 프래그먼트
 */
public class NewsDetailsFragment extends DetailsFragment {
    private static final int ACTION_OPEN_LINK = 1;
    private static final int ACTION_SEE_CONTENT = 2;

    private static final int DETAIL_THUMB_WIDTH = 273;
    private static final int DETAIL_THUMB_HEIGHT = 273;

    private News mNews;
//    private String mLink;

    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private DetailsOverviewRowPresenter mDorPresenter;
    private DetailRowBuilderTask mDetailRowBuilderTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNews();
        initMetrics();
        initBackground();
        initPresenter();
        updateBackground(mNews.getImageUrl());
    }

    private void initNews() {
        mNews = getActivity().getIntent().getExtras().getParcelable(MainFragment.NEWS_ARG_KEY);
    }

    private void initMetrics() {
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void initPresenter() {
        mDorPresenter =
                new DetailsOverviewRowPresenter(new NewsDetailsDescriptionPresenter());

        mDetailRowBuilderTask = (DetailRowBuilderTask) new DetailRowBuilderTask().execute();
        mDorPresenter.setSharedElementEnterTransition(getActivity(),
                NewsDetailsActivity.SHARED_ELEMENT_NAME);
    }

    private void initBackground() {
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);

        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);
    }

    private class DetailRowBuilderTask extends AsyncTask<Void, Integer, DetailsOverviewRow> {

        @Override
        protected DetailsOverviewRow doInBackground(Void... params) {
            DetailsOverviewRow row = new DetailsOverviewRow(mNews);
            try {
                Bitmap poster = Picasso.with(getActivity())
                        .load(mNews.getImageUrl())
                        .resize(Utils.convertDpToPixel(getActivity().getApplicationContext(),
                                        DETAIL_THUMB_WIDTH),
                                Utils.convertDpToPixel(getActivity().getApplicationContext(),
                                        DETAIL_THUMB_HEIGHT))
                        .centerCrop()
                        .get();
                row.setImageBitmap(getActivity(), poster);
            } catch (IOException ignored) {
            }

            row.addAction(new Action(ACTION_OPEN_LINK,
                    getResources().getString(R.string.open_link), null));
            row.addAction(new Action(ACTION_SEE_CONTENT,
                    getResources().getString(R.string.see_content),
                    null));
            return row;
        }

        @Override
        protected void onPostExecute(DetailsOverviewRow detailRow) {
            ClassPresenterSelector ps = new ClassPresenterSelector();
            // set detail background and style
            mDorPresenter.setBackgroundColor(getResources().getColor(R.color.detail_background));
            mDorPresenter.setStyleLarge(true);
            mDorPresenter.setOnActionClickedListener(new OnActionClickedListener() {
                @Override
                public void onActionClicked(Action action) {
                    if (action.getId() == ACTION_OPEN_LINK) {
//                        Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
//                        intent.putExtra(getResources().getString(R.string.movie), mSelectedMovie);
//                        intent.putExtra(getResources().getString(R.string.should_start), true);
//                        startActivity(intent);
                        Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                    } else if (action.getId() == ACTION_SEE_CONTENT){
                        Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ps.addClassPresenter(DetailsOverviewRow.class, mDorPresenter);

            ArrayObjectAdapter adapter = new ArrayObjectAdapter(ps);
            adapter.add(detailRow);
            setAdapter(adapter);
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
