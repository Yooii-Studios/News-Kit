package com.yooiistudios.newsflow.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yooiistudios.newsflow.MainFragment;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.reference.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * DetailsFragment
 *  뉴스 링크를 웹에서 볼 수 있는 프래그먼트
 */
public class DetailsFragment extends Fragment { // implements HTML5WebView.HTML5WebViewCallback {
    @InjectView(R.id.details_scrollview) ScrollView mScrollView;
    @InjectView(R.id.details_content_layout) LinearLayout mContentLayout;
    @InjectView(R.id.details_title_textview) TextView mTitleTextView;
    @InjectView(R.id.details_top_imageview) ImageView mTopImageView;
    @InjectView(R.id.details_content_textview) TextView mContentTextView;

    // FIXME: link 를 주고 받지 않고 추후에는 뉴스 자체를 받을 수 있게 구현하자
    private News mNews;
    private String mLink;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_details,  container, false);
        ButterKnife.inject(this, root);

        initUIElements();
        initScrollView();
        initNews();

        return root;
    }

    private void initUIElements() {
//        mTitleTextView.setTypeface(TypefaceUtils.getMediumTypeface(getActivity()));
//        mContentTextView.setTypeface(TypefaceUtils.getRegularTypeface(getActivity()));
    }

    private void initScrollView() {
//        mScrollView.setClipToOutline(true);
//        mContentLayout.setClipToOutline(true);
    }

    private void initNews() {
//        mNews = getIntent().getExtras().getParcelable(NewsFeedDetailActivity.INTENT_KEY_NEWS);
        mLink = getActivity().getIntent().getExtras().getString(MainFragment.NEWS_ARG_KEY);
    }

    // FIXME: 액티비티의 onAttachFragment 에서 처리하게 변경해주자
    public void setNews(News news) {
        mNews = news;
    }
}
