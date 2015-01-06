package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.news.NewsApplication;
import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.fragment.SettingFragment;
import com.yooiistudios.news.util.MNAnalyticsUtils;

/**
 * Created by Wooseong Kim on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * SettingActivity
 *  설정화면 임시 구현 액티비티. 나중에 UI 작업이 필요
 */
public class SettingActivity extends Activity
        implements SettingFragment.OnSettingChangedListener {
    private static final String TAG = SettingActivity.class.getName();
    private static final String SI_PANEL_MATRIX_CHANGED = "SI_PANEL_MATRIX_CHANGED";
    public static final String PANEL_MATRIX_CHANGED = "PANEL_MATRIX_CHANGED";

    private boolean mPanelMatrixChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.setting_container, new SettingFragment())
                    .commit();
        } else {
            mPanelMatrixChanged = savedInstanceState.getBoolean(SI_PANEL_MATRIX_CHANGED);
        }
        MNAnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SI_PANEL_MATRIX_CHANGED, mPanelMatrixChanged);
    }

    @Override
    public void onPanelMatrixChanged(boolean changed) {
        mPanelMatrixChanged = changed;
    }

    @Override
    public void finish() {
        if (mPanelMatrixChanged) {
            getIntent().putExtra(PANEL_MATRIX_CHANGED, true);
            setResult(RESULT_OK, getIntent());
        }
        super.finish();
    }

    @Override
    protected void onStart() {
        // Activity visible to user
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        // Activity no longer visible
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
