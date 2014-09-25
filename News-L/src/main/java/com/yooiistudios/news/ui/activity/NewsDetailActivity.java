package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.ui.widget.FloatingActionButton;
import com.yooiistudios.news.util.WebUtils;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class NewsDetailActivity extends Activity {

    private static final String TAG = NewsDetailActivity.class.getName();

    @InjectView(R.id.news_detail_root)              FrameLayout mRootContainer;
    @InjectView(R.id.news_detail_fab)               FloatingActionButton mFab;
    @InjectView(R.id.news_detail_loading_container) FrameLayout mLoadingLayout;

    private WebView mWebView;

    private News mNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setConfigCallback((WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        ButterKnife.inject(this);

        mNews = getIntent().getExtras().getParcelable(NewsFeedDetailActivity.INTENT_KEY_NEWS);

        mWebView = new MWebView(getApplicationContext());
        mRootContainer.addView(mWebView);

        initWebView();

        mLoadingLayout.bringToFront();
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
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        mWebView.setWebViewClient(new NewsWebViewClient());
        mWebView.loadUrl(mNews.getLink());

        applySystemWindowsBottomInset(mRootContainer);
    }

    private void applySystemWindowsBottomInset(View containerView) {
        containerView.setFitsSystemWindows(true);
        containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                ViewGroup.MarginLayoutParams lp = (FrameLayout.LayoutParams)mFab.getLayoutParams();
                if (metrics.widthPixels < metrics.heightPixels) {
                    lp.bottomMargin += windowInsets.getSystemWindowInsetBottom();
//                    mFab.setPadding(0, 0, 0, windowInsets.getSystemWindowInsetBottom());
                } else {
                    lp.rightMargin += windowInsets.getSystemWindowInsetRight();
//                    mFab.setPadding(0, 0, windowInsets.getSystemWindowInsetRight(), 0);
                }
                return windowInsets.consumeSystemWindowInsets();
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class NewsWebViewClient extends WebViewClient {
        private boolean mLoadingFinished = true;
        private boolean mRedirect = false;
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!mLoadingFinished) {
                mRedirect = true;
            }

            mLoadingFinished = false;

            view.loadUrl(url);
            return true;
        }

//        @Override
//        public void onPageFinished(WebView view, String url) {
//            super.onPageFinished(view, url);
//            NLLog.i(TAG, "\n\n=========");
//            NLLog.i(TAG, "onPageFinished");
//            NLLog.i(TAG, "url : " + url);
//            NLLog.i(TAG, "=========\n");
//        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap facIcon) {
            mLoadingFinished = false;
            //SHOW LOADING IF IT ISNT ALREADY VISIBLE
            if (mLoadingLayout.getVisibility() != View.VISIBLE) {
                mLoadingLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(!mRedirect){
                mLoadingFinished = true;
            }

            if(mLoadingFinished && !mRedirect){
                //HIDE LOADING IT HAS FINISHED
                mLoadingLayout.setVisibility(View.INVISIBLE);
            } else{
                mRedirect = false;
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
        } catch(Exception e) {
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

    private class MWebView extends WebView {

        public MWebView(Context context) {
            super(context);
        }

        @Override
        public void onDetachedFromWindow(){
            super.onDetachedFromWindow();
            setVisible(false);
        }
    }
}
