package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.task.NewsLinkContentFetchTask;
import com.yooiistudios.news.util.NLLog;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class NewsDetailActivity extends Activity
    implements NewsLinkContentFetchTask.OnContentFetchListener {

    private static final String TAG = NewsDetailActivity.class.getName();

//    @InjectView(R.id.news_detail_webview)           WebView mWebView;
    @InjectView(R.id.news_detail_content)           TextView mContentTextView;
//    @InjectView(R.id.news_detail_webview_container) FrameLayout mWebViewContainer;


    private News mNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        ButterKnife.inject(this);

        mNews = getIntent().getExtras().getParcelable(NewsFeedDetailActivity.INTENT_KEY_NEWS);

        mContentTextView.setText(mNews.getDescription());

        new NewsLinkContentFetchTask(mNews, this).execute();

    }
//    private void initWebView() {
//        WebSettings webSettings = mWebView.getSettings();
//        webSettings.setBuiltInZoomControls(true);
//        webSettings.setJavaScriptEnabled(true);
//        mWebView.setWebViewClient(new NewsWebViewClient());
//        mWebView.loadUrl(mNews.getLink());
//    }

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

    @Override
    public void onContentFetch(String content) {
        mContentTextView.setText("\n\n\n\nfetch done\n\n");

        mContentTextView.append(Html.fromHtml(content));

        NLLog.i(TAG, content);
    }

//    private class NewsWebViewClient extends WebViewClient {
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            view.loadUrl(url);
//            return true;
//        }
//    }
}
