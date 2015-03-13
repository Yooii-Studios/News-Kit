package com.yooiistudios.newsflow.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.yooiistudios.newsflow.R;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
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
}
