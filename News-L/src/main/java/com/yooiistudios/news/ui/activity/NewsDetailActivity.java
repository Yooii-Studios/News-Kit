package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.news.NewsApplication;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.ui.widget.HTML5WebView;
import com.yooiistudios.news.ui.widget.MovableFloatingActionsMenu;
import com.yooiistudios.news.ui.widget.ObservableWebView;
import com.yooiistudios.news.ui.widget.OldFloatingActionButton;
import com.yooiistudios.news.util.AnalyticsUtils;
import com.yooiistudios.news.util.WebUtils;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class NewsDetailActivity extends Activity
        implements HTML5WebView.HTML5WebViewCallback {
    private static final String TAG = NewsDetailActivity.class.getName();

    @InjectView(R.id.news_detail_content_layout) RelativeLayout mContentContainer;
    @InjectView(R.id.news_detail_progress_bar) ProgressBar mProgressBar;
    @InjectView(R.id.news_detail_fab) OldFloatingActionButton mFab;

    @InjectView(R.id.news_detail_fab_menu) FloatingActionsMenu mFabMenu;
    @InjectView(R.id.news_detail_fab_browser) FloatingActionButton mBrowserFab;
    @InjectView(R.id.news_detail_fab_share) FloatingActionButton mShareFab;
    @InjectView(R.id.news_detail_fab_copy_link) FloatingActionButton mCopyLinkFab;
    @InjectView(R.id.news_detail_fab_facebook) FloatingActionButton mFacebookFab;

    private HTML5WebView mWebView;
    private News mNews;

//    private boolean mIsRedirected = false;
    private long mStartTimeMilli;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setConfigCallback((WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        ButterKnife.inject(this);

        mNews = getIntent().getExtras().getParcelable(NewsFeedDetailActivity.INTENT_KEY_NEWS);

        initWebView();

        mProgressBar.bringToFront();
        mProgressBar.animate().withLayer().yBy(-6.0f * getResources().getDisplayMetrics().density)
                .setDuration(0).start();

        mFab.bringToFront();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebUtils.openLinkInBrowser(NewsDetailActivity.this, mNews.getLink());
            }
        });

        initFabMenu();

        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    @Override
    public void onBackPressed() {
        if (!mFabMenu.isExpanded()) {
            WebBackForwardList list = mWebView.copyBackForwardList();

            if (list.getCurrentIndex() <= 0 && !mWebView.canGoBack()) {
                // 처음 들어온 페이지이거나, history가 없는경우
                super.onBackPressed();
            } else {
                // history 가 있는 경우
                // 현재 페이지로 부터 history 수 만큼 뒷 페이지로 이동
                mWebView.goBackOrForward(-(list.getCurrentIndex()));
                // history 삭제
                mWebView.clearHistory();
            }
        } else {
            mFabMenu.collapse();
        }
    }

    private void initWebView() {
        mWebView = new ObservableWebView(this);
        mContentContainer.addView(mWebView.getLayout(),
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings webSettings = mWebView.getSettings();
//        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
//        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setUseWideViewPort(true);
//        mWebView.setWebViewClient(new NewsWebViewClient());
//        mWebView.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public void onProgressChanged(WebView view, int newProgress) {
//                super.onProgressChanged(view, newProgress);
//                mProgressBar.setProgress(newProgress);
//            }
//
//            @Override
//            public void onReceivedTitle(WebView view, String title) {
//                super.onReceivedTitle(view, title);
//                getWindow().setTitle(title);
//            }
//        });
        mWebView.setHTML5WebViewCallback(this);

        // 웹뷰 퍼포먼스 향상을 위한 코드들
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        mWebView.loadUrl(mNews.getLink());

        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP ||
//                        event.getAction() == MotionEvent.ACTION_CANCEL) {
//
//                }
                if (mFabMenu.isExpanded()) {
                    mFabMenu.collapse();
                }
                return false;
            }
        });
//        applySystemWindowsBottomInset(mRootContainer);
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void applySystemWindowsBottomInset(View containerView) {
//        NLLog.now("applySystemWindowsBottomInset");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            containerView.setFitsSystemWindows(true);
//            containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
//                @Override
//                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
//                    Resources resources = getResources();
//                    DisplayMetrics metrics = resources.getDisplayMetrics();
//                    ViewGroup.MarginLayoutParams fabLayoutParams =
//                            (RelativeLayout.LayoutParams)mFab.getLayoutParams();
////                ViewGroup.MarginLayoutParams lp =
////                        (ViewGroup.MarginLayoutParams)mRootContainer.getLayoutParams();
//                    ViewGroup.MarginLayoutParams webViewLayoutParams =
//                            (ViewGroup.MarginLayoutParams) mWebView.getLayoutParams();
//                    int progressMarginOffset = resources.getDimensionPixelSize(R.dimen.progress_margin_offset);
//
//                    if (metrics.widthPixels < metrics.heightPixels) {
//                        NLLog.now("Portrait");
//                        fabLayoutParams.bottomMargin = windowInsets.getSystemWindowInsetBottom() +
//                                resources.getDimensionPixelSize(R.dimen.fab_margin);
//                        mProgressBar.setPadding(0,
//                                windowInsets.getSystemWindowInsetTop() - progressMarginOffset, 0, 0);
//                        view.setPadding(0, windowInsets.getSystemWindowInsetTop(), 0,
//                                windowInsets.getSystemWindowInsetBottom());
//                    } else {
//                        NLLog.now("Landscape");
//                        fabLayoutParams.bottomMargin = windowInsets.getSystemWindowInsetBottom() +
//                                resources.getDimensionPixelSize(R.dimen.fab_margin);
//                        mProgressBar.setPadding(0,
//                                windowInsets.getSystemWindowInsetTop() - progressMarginOffset, 0, 0);
//                        view.setPadding(0, windowInsets.getSystemWindowInsetTop(),
//                                windowInsets.getSystemWindowInsetRight(), 0);
//                    }
//                    return windowInsets.consumeSystemWindowInsets();
//                }
//            });
//        }
//    }

    private void initFabMenu() {
//        mFabMenu.attachToWebView(mWebView);
        mFabMenu.getFloatingActionButton().setColorNormalResId(R.color.material_light_blue_500);
        mFabMenu.getFloatingActionButton().setColorPressedResId(R.color.material_light_blue_100);
        mBrowserFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebUtils.openLinkInBrowser(NewsDetailActivity.this, mNews.getLink());
            }
        });
        mShareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebUtils.shareLink(NewsDetailActivity.this, mNews.getLink());
            }
        });
        mCopyLinkFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebUtils.copyLink(NewsDetailActivity.this, mNews.getLink());
            }
        });
        mFacebookFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebUtils.shareLinkToFacebook(NewsDetailActivity.this, mNews.getLink());
            }
        });
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap facIcon) {
        //Do something you want when starts loading
        mProgressBar.setVisibility(View.VISIBLE);
        mStartTimeMilli = System.currentTimeMillis();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        //Do something you want when finished loading
        mProgressBar.setVisibility(View.INVISIBLE);

        long timeTaken = System.currentTimeMillis() - mStartTimeMilli;
        Log.i("webViewPerformance", "time taken : " + timeTaken);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        mProgressBar.setProgress(newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        getWindow().setTitle(title);
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

    // TODO: WebViewClient 를 WebChromeClient 로 대체해서 progress 를 표시할 수 있는 것이 좋을듯
    // onProgressChanged 에서 progress 가 업데이트됨
    private class NewsWebViewClient extends WebViewClient {
        private boolean mIsRedirected = false;
        private long mStartTimeMilli;

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
                mStartTimeMilli = System.currentTimeMillis();
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

                long timeTaken = System.currentTimeMillis() - mStartTimeMilli;
                Log.i("webViewPerformance", "time taken : " + timeTaken);
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
            ignored.printStackTrace();
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
