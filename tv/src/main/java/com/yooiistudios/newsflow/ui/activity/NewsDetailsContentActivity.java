package com.yooiistudios.newsflow.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.yooiistudios.newsflow.R;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * NewsDetailsContentActivity
 *  뉴스 내용을 볼 수 있는 액티비티
 */
public class NewsDetailsContentActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_content);
    }
}
