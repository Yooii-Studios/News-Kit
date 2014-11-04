package com.yooiistudios.news.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.ui.widget.FloatingActionButton;
import com.yooiistudios.news.util.NLLog;
import com.yooiistudios.news.util.WebUtils;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class NewsDetailActivity extends Activity {
    private static final String TAG = NewsDetailActivity.class.getName();

    @InjectView(R.id.news_detail_root)              RelativeLayout mRootContainer;
    @InjectView(R.id.news_detail_fab)               FloatingActionButton mFab;
//    @InjectView(R.id.news_detail_loading_container) FrameLayout mLoadingLayout;
    @InjectView(R.id.news_detail_progress_bar)      ProgressBar mProgressBar;

    private WebView mWebView;
    private News mNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setConfigCallback((WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        ButterKnife.inject(this);

        mNews = getIntent().getExtras().getParcelable(NewsFeedDetailActivity.INTENT_KEY_NEWS);

        initWebView();

        mProgressBar.bringToFront();

        mFab.bringToFront();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebUtils.openLink(NewsDetailActivity.this, mNews.getLink());
            }
        });
    }

    @Override
    public void onBackPressed() {
        WebBackForwardList list = mWebView.copyBackForwardList();

        if (list.getCurrentIndex() <= 0 && !mWebView.canGoBack()) {
            // 처음 들어온 페이지이거나, history가 없는경우
            super.onBackPressed();
        } else {
            // history가 있는 경우
            // 현재 페이지로 부터 history 수 만큼 뒷 페이지로 이동
            mWebView.goBackOrForward(-(list.getCurrentIndex()));
            // history 삭제
            mWebView.clearHistory();
        }
    }

    private void initWebView() {
        mWebView = new WebView(getApplicationContext());
        mRootContainer.addView(mWebView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        mWebView.setWebViewClient(new NewsWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                getWindow().setTitle(title);
            }
        });

        mWebView.loadUrl(mNews.getLink());

        applySystemWindowsBottomInset(mRootContainer);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void applySystemWindowsBottomInset(View containerView) {
        NLLog.now("applySystemWindowsBottomInset");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            containerView.setFitsSystemWindows(true);
            containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    Resources resources = getResources();
                    DisplayMetrics metrics = resources.getDisplayMetrics();
                    ViewGroup.MarginLayoutParams fabLayoutParams =
                            (RelativeLayout.LayoutParams)mFab.getLayoutParams();
//                ViewGroup.MarginLayoutParams lp =
//                        (ViewGroup.MarginLayoutParams)mRootContainer.getLayoutParams();
                    ViewGroup.MarginLayoutParams webViewLayoutParams =
                            (ViewGroup.MarginLayoutParams) mWebView.getLayoutParams();
                    int progressMarginOffset = resources.getDimensionPixelSize(R.dimen.progress_margin_offset);

                    if (metrics.widthPixels < metrics.heightPixels) {
                        NLLog.now("Portrait");
//                    lp.bottomMargin += windowInsets.getSystemWindowInsetBottom();

//                    webViewLayoutParams.topMargin += windowInsets.getSystemWindowInsetTop();
//                    webViewLayoutParams.bottomMargin += windowInsets.getSystemWindowInsetBottom();
//                    mFab.setPadding(0, 0, 0, windowInsets.getSystemWindowInsetBottom());
                        fabLayoutParams.bottomMargin = windowInsets.getSystemWindowInsetBottom() +
                                resources.getDimensionPixelSize(R.dimen.fab_margin);
//                    mWebView.setPadding(0, windowInsets.getSystemWindowInsetTop(), 0,
//                            windowInsets.getSystemWindowInsetBottom());
                        mProgressBar.setPadding(0,
                                windowInsets.getSystemWindowInsetTop() - progressMarginOffset, 0, 0);
                        view.setPadding(0, windowInsets.getSystemWindowInsetTop(), 0,
                                windowInsets.getSystemWindowInsetBottom());
                    } else {
                        NLLog.now("Landscape");
//                    lp.rightMargin += windowInsets.getSystemWindowInsetRight();
//                    mFab.setPadding(0, 0, windowInsets.getSystemWindowInsetRight(), 0);
                        fabLayoutParams.bottomMargin = windowInsets.getSystemWindowInsetBottom() +
                                resources.getDimensionPixelSize(R.dimen.fab_margin);
//                    webViewLayoutParams.topMargin += windowInsets.getSystemWindowInsetTop();
//                    webViewLayoutParams.rightMargin += windowInsets.getSystemWindowInsetRight();
                        mProgressBar.setPadding(0,
                                windowInsets.getSystemWindowInsetTop() - progressMarginOffset, 0, 0);
                        view.setPadding(0, windowInsets.getSystemWindowInsetTop(),
                                windowInsets.getSystemWindowInsetRight(), 0);
                    }
                    return windowInsets.consumeSystemWindowInsets();
                }
            });
        }
    }

    /*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            int uiOptions = decorView.getSystemUiVisibility();

            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            decorView.setSystemUiVisibility(uiOptions);
        }
    }
    */

/*
    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
*/


    // TODO: WebViewClient 를 WebChromeClient 로 대체해서 progress 를 표시할 수 있는 것이 좋을듯
    // onProgressChanged 에서 progress 가 업데이트됨
    private class NewsWebViewClient extends WebViewClient {
        private boolean mIsRedirected = false;
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            mIsRedirected = true;
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap facIcon) {
            if (!mIsRedirected) {
                //Do something you want when starts loading
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mIsRedirected = false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            if(!mIsRedirected){
//                mLoadingFinished = true;
//            }
//
//            if(mLoadingFinished && !mIsRedirected){
//                //HIDE LOADING IT HAS FINISHED
//                mLoadingLayout.setVisibility(View.INVISIBLE);
//            } else{
//                mIsRedirected = false;
//            }
            if (!mIsRedirected) {
                //Do something you want when finished loading
                mProgressBar.setVisibility(View.INVISIBLE);
            }

        }
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
}
