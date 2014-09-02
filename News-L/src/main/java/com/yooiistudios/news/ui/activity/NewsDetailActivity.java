package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class NewsDetailActivity extends Activity {

    @InjectView(R.id.news_detail_webview) WebView mWebView;
    @InjectView(R.id.news_detail_webview_container) FrameLayout mWebViewContainer;


    private News mNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        ButterKnife.inject(this);

        mNews = getIntent().getExtras().getParcelable(NewsFeedDetailActivity.INTENT_KEY_NEWS);

        initWebView();
//        applySystemWindowsInset(mWebView);
    }
    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new NewsWebViewClient());
        mWebView.loadUrl(mNews.getLink());
    }

    private void applySystemWindowsInset(View containerView) {
        containerView.setFitsSystemWindows(true);
        containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                if (metrics.widthPixels < metrics.heightPixels) {
//                    view.setPadding(0, windowInsets.getSystemWindowInsetTop(), 0,
//                            windowInsets.getSystemWindowInsetBottom());
                    ViewGroup.MarginLayoutParams lp =
                            (ViewGroup.MarginLayoutParams)view.getLayoutParams();
                    lp.topMargin = windowInsets.getSystemWindowInsetTop();
                    lp.bottomMargin = windowInsets.getSystemWindowInsetBottom();
                }
//                else {
//                    view.setPadding(0, 0, windowInsets.getSystemWindowInsetRight(), 0);
//                }
                return windowInsets.consumeSystemWindowInsets();
            }
        });
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
}
