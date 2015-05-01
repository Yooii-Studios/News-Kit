package com.yooiistudios.newskit.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.newskit.NewsApplication;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.IabInfo;
import com.yooiistudios.newskit.core.debug.DebugSettings;
import com.yooiistudios.newskit.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newskit.core.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.newskit.iab.GoogleIabManager;
import com.yooiistudios.newskit.iab.IabListener;
import com.yooiistudios.newskit.iab.IabManager;
import com.yooiistudios.newskit.iab.IabProducts;
import com.yooiistudios.newskit.iab.NaverIabManager;
import com.yooiistudios.newskit.ui.adapter.StoreProductItemAdapter;
import com.yooiistudios.newskit.ui.widget.AutoResizeTextView;
import com.yooiistudios.newskit.util.AnalyticsUtils;
import com.yooiistudios.newskit.util.StoreDebugCheckUtils;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class StoreActivity extends ActionBarActivity implements IabListener,
        StoreProductItemAdapter.StoreItemOnClickListener {
    private static final String TAG = StoreActivity.class.getName();
//    private GoogleIabManager mGoogleIabManager;
//    private NaverIabManager mNaverIabManager;
    private IabManager mIabManager;

    @InjectView(R.id.store_toolbar) Toolbar mToolbar;

    @InjectView(R.id.store_banner_layout_wrapper) LinearLayout mBannerLayoutWrapper;
    @InjectView(R.id.store_banner_image_view) ImageView mBannerImageView;
    @InjectView(R.id.store_title_text_view_1) TextView mTitleTextView1;
    @InjectView(R.id.store_title_text_view_2) TextView mTitleTextView2;
    @InjectView(R.id.store_description_text_view_1) TextView mDescriptionTextView1;
    @InjectView(R.id.store_description_text_view_2) TextView mDescriptionTextView2;

    @InjectView(R.id.store_icon_banner_rss_image_view) ImageView mRssImageView;
    @InjectView(R.id.store_icon_banner_more_panels_image_view) ImageView mMorePanelsImageView;
    @InjectView(R.id.store_icon_banner_topic_image_view) ImageView mTopicImageView;
    @InjectView(R.id.store_icon_banner_no_ads_image_view) ImageView mNoAdsImageView;
    @InjectView(R.id.store_icon_banner_discount_image_view) ImageView mDiscountImageView;

    @InjectView(R.id.store_discounted_price_text_view) AutoResizeTextView mDiscountedPriceTextView;
    @InjectView(R.id.store_original_price_text_view) AutoResizeTextView mOriginalPriceTextView;
    @InjectView(R.id.store_thank_you_text_view) AutoResizeTextView mThankYouTextView;
    @InjectView(R.id.store_purchase_text_view) AutoResizeTextView mPurchasedTextView;
    @InjectView(R.id.store_price_image_view) ImageView mPriceImageView;
    @InjectView(R.id.store_product_list_view) ListView mProductListView;

    @InjectView(R.id.store_progressBar) ProgressBar progressBar;
    @InjectView(R.id.store_loading_view) View loadingView;

    @InjectView(R.id.store_debug_button) Button mDebugButton;
    @InjectView(R.id.store_reset_button) Button mResetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        ButterKnife.inject(this);
        showStoreLoading();
        initLayout();
        initIab();
        initToolbar();
        initUI();
        checkDebug();
        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    private void initLayout() {
        ((ViewManager)mBannerLayoutWrapper.getParent()).removeView(mBannerLayoutWrapper);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mProductListView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.BELOW, R.id.store_toolbar);
        mBannerLayoutWrapper.setLayoutParams(new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mProductListView.addHeaderView(mBannerLayoutWrapper);
    }

    private void initIab() {
        if (isUsingGoogleStore()) {
            mIabManager = new GoogleIabManager(this, this);
        } else if (isUsingNaverStore()) {
            mIabManager = new NaverIabManager(this, this);
        }
        mIabManager.setup();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(getResources().getDimension(R.dimen.store_toolbar_elevation));
        }
    }

    private void initUI() {
        initBannerLayout();
        initItemListView();
    }

    private void initBannerLayout() {
        setPointColoredTextView(mDescriptionTextView2, getString(R.string.store_description_text_2),
                getString(R.string.store_description_text_2_highlight));
        mOriginalPriceTextView.setPaintFlags(mOriginalPriceTextView.getPaintFlags() |
                Paint.STRIKE_THRU_TEXT_FLAG);
        // release
        /*
        int[] attrs = new int[] { android.R.attr.selectableItemBackground };

        // Obtain the styled attributes. 'themedContext' is a context with a
        // theme, typically the current Activity (i.e. 'this')
        TypedArray ta = obtainStyledAttributes(attrs);

        // Now get the value of the 'listItemBackground' attribute that was
        // set in the theme used in 'themedContext'. The parameter is the index
        // of the attribute in the 'attrs' array. The returned Drawable
        // is what you are after
        Drawable drawableFromTheme = ta.getDrawable(0);

        // Finally free resources used by TypedArray
        ta.recycle();

        // setBackground(Drawable) requires API LEVEL 16,
        // otherwise you have to use deprecated setBackgroundDrawable(Drawable) method.
        mTitleTextView1.setBackground(drawableFromTheme);
        */

        mTitleTextView1.setBackgroundColor(Color.TRANSPARENT);
        mTitleTextView2.setBackgroundColor(Color.TRANSPARENT);
        mDescriptionTextView1.setBackgroundColor(Color.TRANSPARENT);
        mDescriptionTextView2.setBackgroundColor(Color.TRANSPARENT);
        mOriginalPriceTextView.setBackgroundColor(Color.TRANSPARENT);
        mDiscountedPriceTextView.setBackgroundColor(Color.TRANSPARENT);
        mPurchasedTextView.setBackgroundColor(Color.TRANSPARENT);
        mThankYouTextView.setBackgroundColor(Color.TRANSPARENT);

        mRssImageView.setBackgroundColor(Color.TRANSPARENT);
        mMorePanelsImageView.setBackgroundColor(Color.TRANSPARENT);
        mTopicImageView.setBackgroundColor(Color.TRANSPARENT);
        mNoAdsImageView.setBackgroundColor(Color.TRANSPARENT);
        mDiscountImageView.setBackgroundColor(Color.TRANSPARENT);
    }

    private void initItemListView() {
        mProductListView.setAdapter(new StoreProductItemAdapter(this, null, this));
        mProductListView.setBackgroundColor(Color.WHITE);
    }

    private void setPointColoredTextView(TextView textView, String descriptionString, String pointedString) {
        if (textView != null) {
            SpannableString spannableString = new SpannableString(descriptionString);

            int pointedStringIndex = descriptionString.indexOf(pointedString);
            if (pointedStringIndex != -1) {
                spannableString.setSpan(
                        new ForegroundColorSpan(getResources().getColor(R.color.store_no_ads_color)),
                        pointedStringIndex, pointedStringIndex + pointedString.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(spannableString);
            }
        }
    }

    private boolean isUsingGoogleStore() {
        return IabInfo.STORE_TYPE == IabProducts.StoreType.GOOGLE;
    }

    private boolean isUsingNaverStore() {
        return IabInfo.STORE_TYPE == IabProducts.StoreType.NAVER;
    }

    /**
     * Loading
     */
    private void showStoreLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }
    }

    private void hideStoreLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(ProgressBar.GONE);
        }
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handled = false;
        if (mIabManager != null) {
            handled = !mIabManager.isHelperDisposed()
                    && mIabManager.handleActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIabManager != null) {
            mIabManager.dispose();
        }
    }

    public void onPriceButtonClicked(View view) {
        if (StoreDebugCheckUtils.isUsingStore(this)) {
            purchase(IabProducts.SKU_PRO_VERSION);
        } else {
            IabProducts.saveIabProduct(IabProducts.SKU_PRO_VERSION, this);
            initUI();
            initUIOnQueryFinishedDebug();
            // 풀버전 구매시 2X3으로 변경해줌
            PanelMatrixUtils.setCurrentPanelMatrix(PanelMatrix.THREE_BY_TWO, this);
        }
    }

    @Override
    public void onItemPriceButtonClicked(String googleSku) {
        if (StoreDebugCheckUtils.isUsingStore(this)) {
            purchase(googleSku);
        } else {
            IabProducts.saveIabProduct(googleSku, this);
            initUI();
        }
    }

    private void purchase(String googleSku) {
        mIabManager.purchase(googleSku);
    }

    /**
     * Debug
     */
    private void checkDebug() {
        if (DebugSettings.isDebugBuild()) {
            mResetButton.setVisibility(View.VISIBLE);
            mDebugButton.setVisibility(View.VISIBLE);
            if (StoreDebugCheckUtils.isUsingStore(this)) {
                mDebugButton.setText("Google Play");
            } else {
                mDebugButton.setText("Debug");
            }
        } else {
            mResetButton.setVisibility(View.GONE);
            mDebugButton.setVisibility(View.GONE);
            StoreDebugCheckUtils.setUsingStore(true, this);
        }
    }

    public void onResetButtonClicked(View view) {
        // 디버그 상태에서 구매했던 아이템들을 리셋
        if (StoreDebugCheckUtils.isUsingStore(this)) {
            if (mIabManager != null) {
                mIabManager.setup();
            }
            initUI();
        } else {
            IabProducts.resetIabProductsDebug(this);
            initUI();
            initUIOnQueryFinishedDebug();
        }
    }

    public void onDebugButtonClicked(View view) {
        if (StoreDebugCheckUtils.isUsingStore(this)) {
            mDebugButton.setText("Debug");
            StoreDebugCheckUtils.setUsingStore(false, this);
        } else {
            mDebugButton.setText("Google Play");
            StoreDebugCheckUtils.setUsingStore(true, this);
        }
    }

    @Override
    public void onIabSetupFinished() {}

    @Override
    public void onIabSetupFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        hideStoreLoading();
    }

    @Override
    public void onQueryFinished(Map<String, String> googleSkuToPriceMap) {
        initUIOnQueryFinished(googleSkuToPriceMap);
        hideStoreLoading();
    }

    @Override
    public void onQueryFailed(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hideStoreLoading();
    }

    @Override
    public void onIabPurchaseFinished(String googleSku) {
        updateUIOnPurchase(googleSku);
    }

    @Override
    public void onIabPurchaseFailed(String message) {
        showComplain("Purchase Failed: " + message);
    }

    private void showComplain(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initUIOnQueryFinished(Map<String, String> googleSkuToPriceMap) {
        if (StoreDebugCheckUtils.isUsingStore(this)) {
            Context context = getApplicationContext();
            List<String> ownedProducts = IabProducts.loadOwnedIabProducts(context);
            // Full version
            if (ownedProducts.contains(IabProducts.SKU_PRO_VERSION)) {
                disableProVersion();
            } else {
//                if (inventory.getSkuDetails(IabProducts.SKU_PRO_VERSION_ORIGINAL) != null) {
                mOriginalPriceTextView.setText(
                        googleSkuToPriceMap.get(IabProducts.SKU_PRO_VERSION_ORIGINAL));
                // IabManager 에서 inventory 가 모든 sku 의 정보를 가지고 있는지 체크하므로 필요 없을 것이라 예상
//                } else {
//                    mOriginalPriceTextView.setText("$4.99");
//                }
                mDiscountedPriceTextView.setText(
                        googleSkuToPriceMap.get(IabProducts.SKU_PRO_VERSION));

                mBannerImageView.setClickable(true);
                mBannerImageView.setBackgroundResource(R.drawable.store_bg_banner_selector);
                mPriceImageView.setClickable(true);
            }
            mProductListView.setAdapter(new StoreProductItemAdapter(context, googleSkuToPriceMap, this));
        } else {
            initUIOnQueryFinishedDebug();
        }
    }

    private void updateUIOnPurchase(String sku) {
        if (sku.equals(IabProducts.SKU_PRO_VERSION)) {
            // Full version
            disableProVersion();
            // 풀버전 구매시 2X3으로 변경해줌
            PanelMatrixUtils.setCurrentPanelMatrix(PanelMatrix.THREE_BY_TWO, this);
        }
        HeaderViewListAdapter adapter = (HeaderViewListAdapter)mProductListView.getAdapter();
        ((StoreProductItemAdapter)adapter.getWrappedAdapter()).updateOnPurchase();
    }

    private void initUIOnQueryFinishedDebug() {
        List<String> ownedSkus = IabProducts.loadOwnedIabProducts(this);
        if (ownedSkus.contains(IabProducts.SKU_PRO_VERSION)) {
            disableProVersion();
        } else {
            enableProVersion();
        }
    }

    private void enableProVersion() {
        mPriceImageView.setBackgroundResource(R.drawable.store_btn_banner_price_selector);
        mOriginalPriceTextView.setVisibility(View.VISIBLE);
        mDiscountedPriceTextView.setVisibility(View.VISIBLE);
        mPurchasedTextView.setVisibility(View.INVISIBLE);
        mThankYouTextView.setVisibility(View.INVISIBLE);
        mBannerImageView.setClickable(true);
        mBannerImageView.setBackgroundResource(R.drawable.store_bg_banner_selector);
        mPriceImageView.setClickable(true);
    }

    private void disableProVersion() {
        mPriceImageView.setBackgroundResource(R.drawable.store_btn_banner_purchased);
        mOriginalPriceTextView.setVisibility(View.INVISIBLE);
        mDiscountedPriceTextView.setVisibility(View.INVISIBLE);
        mPurchasedTextView.setVisibility(View.VISIBLE);
        mThankYouTextView.setVisibility(View.VISIBLE);
        mBannerImageView.setClickable(false);
        mBannerImageView.setBackgroundResource(R.drawable.store_bg_banner);
        mPriceImageView.setClickable(false);
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
