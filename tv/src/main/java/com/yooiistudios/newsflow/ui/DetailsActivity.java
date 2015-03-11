package com.yooiistudios.newsflow.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.yooiistudios.newsflow.MainFragment;
import com.yooiistudios.newsflow.R;

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
        setContentView(getLayoutId());
    }

    private int getLayoutId() {
        String contentToShow = getIntent().getExtras().getString(MainFragment.DETAIL_CONTENT_KEY);
        int layoutId;
        if (contentToShow.equals(MainFragment.DETAIL_REFINED_CONTENT)) {
            layoutId = R.layout.activity_details;
        } else {
            layoutId = R.layout.activity_details_web;
        }
        return layoutId;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        // TODO: 프래그먼트에 직접 News 를 세팅해주자
    }
}
