package com.yooiistudios.newskit.tv.model.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.yooiistudios.newskit.tv.R;
import com.yooiistudios.newskit.tv.model.ui.fragment.NewsWebFragment;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * NewsDetailsWebActivity
 *  뉴스를 웹으로 보는 액티비티
 */
public class NewsWebActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_web);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        NewsWebFragment webFragment = (NewsWebFragment)
                getFragmentManager().findFragmentById(R.id.news_detail_fragment);
        switch(event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                webFragment.scrollDownWebView();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                webFragment.scrollUpWebView();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}
