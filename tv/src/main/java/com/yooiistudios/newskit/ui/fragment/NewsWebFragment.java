package com.yooiistudios.newskit.ui.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.ui.HTML5WebView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * WebFragment
 *  뉴스 링크를 웹에서 볼 수 있는 프래그먼트
 */
public class NewsWebFragment extends Fragment implements HTML5WebView.HTML5WebViewCallback {
    @InjectView(R.id.details_layout) FrameLayout mContainer;
    @InjectView(R.id.news_detail_progress_bar) ProgressBar mProgressBar;
    private static final int SCROLL_LENGTH = 70;

    private HTML5WebView mWebView;
    private News mNews;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_news_web, container, false);
        ButterKnife.inject(this, root);

        initNews();
        initWebView();
        initProgressBar();

        return root;
    }

    private void initNews() {
        mNews = getActivity().getIntent().getExtras().getParcelable(NewsFragment.ARG_NEWS_KEY);
    }

    private void initWebView() {
        mWebView = new HTML5WebView(getActivity());
        mContainer.addView(mWebView.getLayout(), new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        mWebView.setHTML5WebViewCallback(this);

        // 웹뷰 퍼포먼스 향상을 위한 코드들
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 특정 버전 밑에서는 돌아가서 적었음
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        if (mNews.getLink() != null) {
            mWebView.loadUrl(mNews.getLink());
        }
    }

    private void initProgressBar() {
        mProgressBar.bringToFront();
    }

    public void scrollDownWebView() {
//        int currentScroll = mWebView.getScrollY();
//        int toScroll = currentScroll + SCROLL_LENGTH;
//        if (toScroll >= mWebView.getContentHeight()) {
//            toScroll = mWebView.getContentHeight();
//        }
//        mWebView.scrollTo(0, toScroll);
        int scrollBy = SCROLL_LENGTH;
        if (mWebView.getScrollY() + scrollBy > mWebView.getContentHeight()) {
            scrollBy = mWebView.getContentHeight() - mWebView.getScrollY();
        }
        mWebView.scrollBy(0, scrollBy);
    }

    public void scrollUpWebView() {
        int currentScroll = mWebView.getScrollY();
        int toScroll = currentScroll - SCROLL_LENGTH;
        if (toScroll <= 0) {
            toScroll = 0;
        }
        mWebView.scrollTo(0, toScroll);
    }

    @Override
    public void onDestroy() {
//        mWebView.stopLoading();
        mContainer.removeView(mWebView);
        super.onDestroy();
    }

    /**
     * WebView Callbacks
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap facIcon) {
        // Do something you want when starts loading
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // Do something you want when finished loading
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        mProgressBar.setProgress(newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {

    }
}
