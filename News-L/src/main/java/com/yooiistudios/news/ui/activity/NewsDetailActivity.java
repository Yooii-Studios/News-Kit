package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class NewsDetailActivity extends Activity {

    private static final String TAG = NewsDetailActivity.class.getName();

    @InjectView(R.id.news_detail_root)  FrameLayout mRootContainter;
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
        mRootContainter.addView(mWebView);

        initWebView();
    }

    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        mWebView.setWebViewClient(new NewsWebViewClient());
        mWebView.loadUrl(mNews.getLink());
    }

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
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
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
