package com.yooiistudios.newsflow.ui;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.yooiistudios.newsflow.MainFragment;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.ui.HTML5WebView;
import com.yooiistudios.newsflow.core.util.NLLog;
import com.yooiistudios.newsflow.reference.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * WebFragment
 *  뉴스 링크를 웹에서 볼 수 있는 프래그먼트
 */
public class WebFragment extends Fragment implements HTML5WebView.HTML5WebViewCallback {
    @InjectView(R.id.details_layout) FrameLayout mContainer;
    @InjectView(R.id.news_detail_progress_bar) ProgressBar mProgressBar;

    private HTML5WebView mWebView;
    // FIXME: link 를 주고 받지 않고 추후에는 뉴스 자체를 받을 수 있게 구현하자
    private News mNews;
    private String mLink;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_details_web, container, false);
        ButterKnife.inject(this, root);

        initNews();
        initWebView();
        initProgressBar();

        return root;
    }

    private void initNews() {
//        mNews = getIntent().getExtras().getParcelable(NewsFeedDetailActivity.INTENT_KEY_NEWS);
        mLink = getActivity().getIntent().getExtras().getString(MainFragment.NEWS_ARG_KEY);
    }

    // FIXME: 액티비티의 onAttachFragment 에서 처리하게 변경해주자
    public void setNews(News news) {
        mNews = news;
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

        // http://star.mk.co.kr/v2/view.php?sc=40900001&year=2015&no=215662
//        mWebView.loadUrl(mNews.getLink());
//        webView.loadUrl("http://star.mk.co.kr/v2/view.php?sc=40900001&year=2015&no=215662");
//        webView.loadUrl("http://edition.cnn.com/2015/03/05/world/jihadi-john-terror-network/index.html");
//        mWebView.loadUrl("http://www.nytimes.com/2015/03/06/us/in-ferguson-some-who-are-part-of-problem-are-asked-to-be-part-of-solution.html?hp&action=click&pgtype=Homepage&module=a-lede-package-region&region=top-news&WT.nav=top-news&_r=0");
        if (mLink != null) {
            mWebView.loadUrl(mLink);
        }

        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                NLLog.now("onKey: " + keyCode);
                return false;
            }
        });
    }

    private void initProgressBar() {
        mProgressBar.bringToFront();
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
