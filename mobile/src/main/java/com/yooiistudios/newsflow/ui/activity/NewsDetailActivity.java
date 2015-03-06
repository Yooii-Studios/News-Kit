package com.yooiistudios.newsflow.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.newsflow.NewsApplication;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.ui.HTML5WebView;
import com.yooiistudios.newsflow.ui.widget.ObservableWebView;
import com.yooiistudios.newsflow.ui.widget.WebFloatingActionButton;
import com.yooiistudios.newsflow.util.AnalyticsUtils;
import com.yooiistudios.newsflow.util.WebUtils;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class NewsDetailActivity extends Activity implements HTML5WebView.HTML5WebViewCallback {
    private static final String TAG = NewsDetailActivity.class.getName();

    @InjectView(R.id.news_detail_content_layout) RelativeLayout mContentContainer;
    @InjectView(R.id.news_detail_progress_bar) ProgressBar mProgressBar;
    @InjectView(R.id.news_detail_fab) WebFloatingActionButton mFab;

    private News mNews;
    private ObservableWebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setConfigCallback((WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        ButterKnife.inject(this);

        initNews();
        initWebView();
        initProgressBar();
        initFabMenu();

        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    private void initNews() {
        mNews = getIntent().getExtras().getParcelable(NewsFeedDetailActivity.INTENT_KEY_NEWS);
    }

    private void initWebView() {
        mWebView = new ObservableWebView(this);
        mContentContainer.addView(mWebView.getLayout(),
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        mWebView.setHTML5WebViewCallback(this);

        // 웹뷰 퍼포먼스 향상을 위한 코드들
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        mWebView.loadUrl(mNews.getLink());
    }

    private void initProgressBar() {
        mProgressBar.bringToFront();
    }

    private void initFabMenu() {
        mFab.attachToWebView(mWebView);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebUtils.shareLink(NewsDetailActivity.this, mNews.getLink());
            }
        });
    }

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
        getWindow().setTitle(title);
    }

    private void setConfigCallback(WindowManager windowManager) {
        try {
            Field field = WebView.class.getDeclaredField("mWebViewCore");
            field = field.getType().getDeclaredField("mBrowserFrame");
            field = field.getType().getDeclaredField("sConfigCallback");
            field.setAccessible(true);
            Object configCallback = field.get(null);

            if (null == configCallback) {
                return;
            }

            field = field.getType().getDeclaredField("mWindowManager");
            field.setAccessible(true);
            field.set(configCallback, windowManager);
        } catch(Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        WebBackForwardList list = mWebView.copyBackForwardList();

        if (list.getCurrentIndex() <= 0 && !mWebView.canGoBack()) {
            // 처음 들어온 페이지이거나, history 가 없는경우
            super.onBackPressed();
        } else {
            // history 가 있는 경우
            // 현재 페이지로 부터 history 수 만큼 뒷 페이지로 이동
            mWebView.goBackOrForward(-(list.getCurrentIndex()));
            // history 삭제
            mWebView.clearHistory();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }

        setConfigCallback(null);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        // Activity visible to user
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        // Activity no longer visible
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
