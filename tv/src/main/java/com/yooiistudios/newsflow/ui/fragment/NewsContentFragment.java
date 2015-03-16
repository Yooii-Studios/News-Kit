package com.yooiistudios.newsflow.ui.fragment;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.newscontent.NewsContent;
import com.yooiistudios.newsflow.core.ui.animation.activitytransition.ActivityTransitionProperty;
import com.yooiistudios.newsflow.model.BitmapLoadTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
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

    @Getter ActivityTransitionProperty mTransitionProperty;
//    private PicassoImageViewTarget mTopImageTarget;
    private News mNews;
//    private Drawable mDefaultCardImage;
    //    private String mLink;
    private BitmapLoadTask mBitmapLoadTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_news_content,  container, false);
        ButterKnife.inject(this, root);

        initVariables();
        initNews();
        initUIElements();
        initScrollView();

//        DetailsTransitionUtils.runEnterAnimation(this);

        loadImageOnImageViewSizeFix();

        return root;
    }

    private void initVariables() {
//        mTopImageTarget = new PicassoImageViewTarget(mTopImageView);
//        mDefaultCardImage = getActivity().getResources().getDrawable(R.drawable.news_dummy2);
    }

    private void initNews() {
        mNews = getActivity().getIntent().getExtras().getParcelable(NewsFragment.ARG_NEWS_KEY);
    }

    private void initUIElements() {
//        mTitleTextView.setTypeface(TypefaceUtils.getMediumTypeface(getActivity()));
//        mContentTextView.setTypeface(TypefaceUtils.getRegularTypeface(getActivity()));

        NewsContent newsContent = mNews.getNewsContent();
        mTitleTextView.setText(newsContent.getTitle());
        mContentTextView.setText(mNews.getDisplayableNewsContentDescription());
//        news.getDisplayableRssDescription()
    }

    private void initScrollView() {
//        mScrollView.setClipToOutline(true);
//        mContentLayout.setClipToOutline(true);
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

//    private void loadImage() {
//        Picasso.with(getActivity())
//                .load(mNews.getImageUrl())
//                .resize(mTopImageView.getWidth(),
//                        mTopImageView.getHeight())
//                .centerCrop()
//                .error(mDefaultCardImage)
//                .into(mTopImageTarget);
//    }

    private void loadImage() {
        Context context = getActivity().getApplicationContext();
        int width = mTopImageView.getWidth();
        int height = mTopImageView.getHeight();

        mBitmapLoadTask = new BitmapLoadTask(context, mNews.getImageUrl(), width, height,
                R.drawable.news_dummy2, new BitmapLoadTask.OnSuccessListener() {
            @Override
            public void onLoad(Drawable drawable) {
                mTopImageView.setImageDrawable(drawable);
            }
        });
        mBitmapLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
