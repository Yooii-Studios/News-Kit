package com.yooiistudios.newsflow.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.newsflow.NewsApplication;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.debug.DebugSettings;
import com.yooiistudios.newsflow.iab.IabProducts;
import com.yooiistudios.newsflow.ui.fragment.SettingFragment;
import com.yooiistudios.newsflow.util.AnalyticsUtils;
import com.yooiistudios.newsflow.util.StoreDebugCheckUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wooseong Kim on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * SettingActivity
 *  설정화면 액티비티
 */
public class SettingActivity extends ActionBarActivity
        implements SettingFragment.OnSettingChangedListener {
    private static final String TAG = SettingActivity.class.getName();
    public static final String PANEL_MATRIX_CHANGED = "PANEL_MATRIX_CHANGED";

    private boolean mIsPanelMatrixChanged = false;

    @InjectView(R.id.setting_toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.inject(this);
        // OS에 의해서 kill 당할 경우 복구하지 말고 메인 액티비티를 새로 띄워줌
        if (savedInstanceState != null) {
            finish();
        } else {
            initToolbar();
            initFullVersionButtonDebug();
            getFragmentManager().beginTransaction()
                    .add(R.id.setting_container, new SettingFragment())
                    .commit();
        }
        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    private void initFullVersionButtonDebug() {
        // Google play service 가 없는 에뮬레이터에서 구동할 경우 스토어에 들어가지 못하기 때문에 디버그 옵션으로 추가.
        View debugBuyFullVersionButton = findViewById(R.id.setting_debug_buy_pro_version);
        View debugResetAllPurchaseButton = findViewById(R.id.setting_debug_reset_all_purchase);
        if (DebugSettings.isDebugBuild()) {
            debugBuyFullVersionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StoreDebugCheckUtils.setUsingStore(false, getApplicationContext());
                    IabProducts.saveIabProduct(IabProducts.SKU_PRO_VERSION, getApplicationContext());
                }
            });
            debugResetAllPurchaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StoreDebugCheckUtils.setUsingStore(false, getApplicationContext());
                    IabProducts.resetIabProductsDebug(getApplicationContext());
                }
            });
        } else {
            debugBuyFullVersionButton.setVisibility(View.GONE);
            debugResetAllPurchaseButton.setVisibility(View.GONE);
        }
    }

    private void initToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(getResources()
                    .getDimensionPixelSize(R.dimen.news_select_elevation));
        }
        mToolbar.bringToFront();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // sans-serif-medium, 20sp
        mToolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title);

        // typeface 는 따로 설정 필요
        SpannableString titleString = new SpannableString(mToolbar.getTitle());
        titleString.setSpan(new TypefaceSpan(getString(R.string.noto_sans_medium)), 0,
                titleString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mToolbar.setTitle(titleString);
    }

    @Override
    public void onPanelMatrixSelect(boolean isChanged) {
        mIsPanelMatrixChanged = isChanged;
    }

    @Override
    public void finish() {
        if (mIsPanelMatrixChanged) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                AnalyticsUtils.trackSettingsQuitAction((NewsApplication) getApplication(), TAG,
                        "Home Button");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnalyticsUtils.trackSettingsQuitAction((NewsApplication) getApplication(), TAG, "Back Button");
    }
}
