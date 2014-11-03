package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.fragment.SettingFragment;

/**
 * Created by Wooseong Kim on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * SettingActivity
 *  설정화면 임시 구현 액티비티. 나중에 UI 작업이 필요
 */
public class SettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.setting_container, new SettingFragment())
                    .commit();
        }
    }

}
