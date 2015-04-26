package com.yooiistudios.newskit.ui.fragment;

import android.animation.TimeInterpolator;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.news.newscontent.NewsContent;
import com.yooiistudios.newskit.core.ui.animation.activitytransition.ActivityTransitionProperty;
import com.yooiistudios.newskit.model.BitmapLoadTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * NewsDetailsContentFragment
 *  뉴스 링크를 웹에서 볼 수 있는 프래그먼트
 */
@Accessors(prefix = "m")
public class NewsContentFragment extends Fragment {
    public static final String NEWS_LINK_ARG = "news_link_arg";
    public static final int MIN_TEXT_LENGTH = 100;

    @Getter @InjectView(R.id.details_layout) FrameLayout mLayout;
    @Getter @InjectView(R.id.details_scrollview) ScrollView mScrollView;
    @InjectView(R.id.details_content_layout) LinearLayout mContentLayout;
    @InjectView(R.id.details_title_textview) TextView mTitleTextView;
    @InjectView(R.id.details_top_imageview) ImageView mTopImageView;
    @InjectView(R.id.details_content_textview) TextView mContentTextView;
    @InjectView(R.id.details_loading_progress_bar) ProgressBar mProgressBar;

    @Getter ActivityTransitionProperty mTransitionProperty;
    private News mNews;
    private BitmapLoadTask mBitmapLoadTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_news_content,  container, false);
        ButterKnife.inject(this, root);

        initNews();
        initUIElements();

        loadImageOnImageViewSizeFix();

        return root;
    }

    private void initNews() {
        mNews = getActivity().getIntent().getExtras().getParcelable(NewsFragment.ARG_NEWS_KEY);
    }

    private void initUIElements() {
        NewsContent newsContent = mNews.getNewsContent();

        String title = newsContent.getTitle().trim();
        if (title.length() == 0) {
            title = mNews.getTitle();
        }
        setTitle(title);
        setContentText(mNews.getDisplayableNewsContentDescription());
    }

    private void setTitle(String title) {
        mTitleTextView.setText(title);
    }

    private void setContentText(String contentText) {
        mContentTextView.setText(contentText);
    }

    private void loadImageOnImageViewSizeFix() {
        mTopImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mTopImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                loadImage();
                return true;
            }
        });
    }

    private void loadImage() {
        Context context = getActivity().getApplicationContext();
        int width = mTopImageView.getWidth();
        int height = mTopImageView.getHeight();

        mBitmapLoadTask = new BitmapLoadTask(context, mNews.getImageUrl(), width, height,
                R.drawable.news_dummy, new BitmapLoadTask.OnSuccessListener() {
            @Override
            public void onLoad(Drawable drawable) {
                configOnImageLoad(drawable);
            }
        });
        mBitmapLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void configOnImageLoad(Drawable drawable) {
        mProgressBar.setVisibility(View.GONE);
        mTopImageView.setImageDrawable(drawable);


        Context context = getActivity().getApplicationContext();
        TimeInterpolator imageFadeInInterpolator =
                AnimationUtils.loadInterpolator(context, android.R.interpolator.fast_out_slow_in);
        mTopImageView.setAlpha(0.0f);
        mTopImageView.animate().alpha(1.0f).setInterpolator(imageFadeInInterpolator).setDuration(1300);
    }

    // FIXME: 액티비티의 onAttachFragment 에서 처리하게 변경해주자
    public void setNews(News news) {
        mNews = news;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBitmapLoadTask != null) {
            mBitmapLoadTask.cancel(true);
        }
    }
}
