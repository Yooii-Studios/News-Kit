package com.yooiistudios.newsflow.ui.fragment;

import android.content.Context;
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
//    private String mLink;

//    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private DetailsOverviewRowPresenter mDorPresenter;
    private DetailsOverviewRow mRow;

    private RequestBitmapTask mImageLoadTask;
    private RequestBitmapTask mBackgroundLoadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNews();
        initMetrics();
        initBackground();
        initPresenter();
        initUI();

        Context context = getActivity().getApplicationContext();

        int width = DipToPixel.dpToPixel(getActivity().getApplicationContext(),
                DETAIL_THUMB_WIDTH);
        int height = DipToPixel.dpToPixel(getActivity().getApplicationContext(),
                DETAIL_THUMB_HEIGHT);
        String url = mNews.getImageUrl() != null ? mNews.getImageUrl() : "";
        mImageLoadTask = new RequestBitmapTask(context, url, width, height,
                R.drawable.news_dummy2, new RequestBitmapTask.OnSuccessListener() {
            @Override
            public void onSuccess(Drawable drawable) {
                mRow.setImageDrawable(drawable);
                ((ArrayObjectAdapter)getAdapter()).notifyArrayItemRangeChanged(0, 1);
            }
        });
        mImageLoadTask.execute();


        mBackgroundLoadTask = new RequestBitmapTask(context, url,
                mMetrics.widthPixels, mMetrics.heightPixels,
                R.drawable.default_background, new RequestBitmapTask.OnSuccessListener() {
            @Override
            public void onSuccess(Drawable drawable) {
                mBackgroundManager.setDrawable(drawable);
            }
        });
        mBackgroundLoadTask.execute();
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
        mDorPresenter =
                new DetailsOverviewRowPresenter(new NewsDescriptionPresenter());
    }

    private void initUI() {
        initRow();
        initAdapter();
//        updateBackground(mNews.getImageUrl());
    }

    private void initRow() {
        mRow = new DetailsOverviewRow(mNews);
//        mImageTarget = new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                mRow.setImageBitmap(getActivity(), bitmap);
//                ((ArrayObjectAdapter)getAdapter()).notifyArrayItemRangeChanged(0, 1);
//            }
//
//            @Override
//            public void onBitmapFailed(Drawable errorDrawable) {
//                mRow.setImageDrawable(errorDrawable);
//                ((ArrayObjectAdapter)getAdapter()).notifyArrayItemRangeChanged(0, 1);
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
//                mRow.setImageDrawable(placeHolderDrawable);
//                ((ArrayObjectAdapter)getAdapter()).notifyArrayItemRangeChanged(0, 1);
//            }
//        };

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

    private void initBackground() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(mBackgroundManager);
    }

//    private void updateBackground(String url) {
//        if (url.length() > 0) {
//            Picasso.with(getActivity())
//                    .load(url)
//                    .resize(mMetrics.widthPixels, mMetrics.heightPixels)
//                    .centerCrop()
//                    .error(mDefaultBackground)
//                    .into(mBackgroundTarget);
//        } else {
//            mBackgroundManager.setDrawable(mDefaultBackground);
//        }
//    }

    private static class RequestBitmapTask extends AsyncTask<Void, Void, Drawable> {
        public interface OnSuccessListener {
            public void onSuccess(Drawable drawable);
        }
        private Context mContext;
        private String mUrl;
        private int mWidth;
        private int mHeight;
        private int mDefaultResId;
        private OnSuccessListener mListener;

        public RequestBitmapTask(Context context, String url, int width, int height,
                                 int defaultResId, OnSuccessListener listener) {
            mContext = context;
            mUrl = url;
            mWidth = width;
            mHeight = height;
            mDefaultResId = defaultResId;
            mListener = listener;
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            Drawable drawable;
            try {
                Bitmap bitmap = Picasso.with(mContext)
                        .load(mUrl)
                        .resize(mWidth, mHeight)
                        .centerCrop()
                        .error(createDefaultImage())
                        .get();
                drawable = new BitmapDrawable(mContext.getResources(), bitmap);
            } catch (Exception e) {
                drawable = createDefaultImage();
            }

            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            if (mListener != null && !isCancelled()) {
                mListener.onSuccess(drawable);
            }
        }

        private Drawable createDefaultImage() {
            return mContext.getResources().getDrawable(mDefaultResId);
        }
    }
}
