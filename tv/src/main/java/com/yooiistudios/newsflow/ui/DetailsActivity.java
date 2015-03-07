package com.yooiistudios.newsflow.ui;

import android.app.Activity;
import android.os.Bundle;

import com.yooiistudios.newsflow.reference.R;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * NewsDetailActivity
 *  뉴스 내용을 볼 수 있는 액티비티
 */
public class DetailsActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
    }
}
