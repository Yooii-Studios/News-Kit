package com.yooiistudios.news.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yooiistudios.news.R;
import com.yooiistudios.news.iab.IabManager;
import com.yooiistudios.news.iab.IabManagerListener;
import com.yooiistudios.news.iab.IabProducts;
import com.yooiistudios.news.iab.util.IabResult;
import com.yooiistudios.news.iab.util.Inventory;
import com.yooiistudios.news.ui.adapter.StoreProductItemAdapter;
import com.yooiistudios.news.ui.widget.AutoResize2TextView;
import com.yooiistudios.news.ui.widget.AutoResizeTextView;
import com.yooiistudios.news.util.NLLog;
import com.yooiistudios.news.util.StoreDebugCheckUtils;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class StoreActivity extends ActionBarActivity implements StoreProductItemAdapter.StoreItemOnClickListener, IabManagerListener {
    private static final String TAG = "StoreActivity";
    private IabManager iabManager;

    @InjectView(R.id.store_toolbar) Toolbar mToolbar;

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

    @InjectView(R.id.store_discounted_price_text_view) AutoResize2TextView mDiscountedPriceTextView;
    @InjectView(R.id.store_original_price_text_view) AutoResize2TextView mOriginalPriceTextView;
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
        initIab();
        initToolbar();
        initUI();
        checkDebug();
    }

    private void initIab() {
        iabManager = new IabManager(this, this);
        iabManager.loadWithAllItems();
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
        mDiscountedPriceTextView.setBackgroundColor(Color.TRANSPARENT);
        mOriginalPriceTextView.setBackgroundColor(Color.TRANSPARENT);

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
        if (iabManager != null) {
            if (iabManager.getHelper() == null) return;

            // Pass on the activity result to the helper for handling
            if (!iabManager.getHelper().handleActivityResult(requestCode, requestCode, data)) {
                // not handled, so handle it ourselves (here's where you'd
                // perform any handling of activity results not related to in-app
                // billing...
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iabManager != null) {
            iabManager.dispose();
        }
    }

    public void onPriceButtonClicked(View view) {
        if (StoreDebugCheckUtils.isUsingStore(this)) {
            // process to google play
        } else {
            IabProducts.saveIabProduct(IabProducts.SKU_FULL_VERSION, this);
            initUI();
            initUIOnQueryFinishedDebug();
        }
    }

    @Override
    public void onItemPriceButtonClicked(String sku) {
        Log.i(TAG, "onItemPriceButtonClicked: " + sku);
        if (StoreDebugCheckUtils.isUsingStore(this)) {
            // process to google play
        } else {
            IabProducts.saveIabProduct(sku, this);
            initUI();
        }
    }

    /**
     * Debug
     */
    private void checkDebug() {
        if (NLLog.isDebug) {
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
            if (iabManager != null) {
                iabManager.loadWithAllItems();
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
    public void onIabSetupFinished(IabResult result) {

    }

    @Override
    public void onIabSetupFailed(IabResult result) {
        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
        hideStoreLoading();
    }

    @Override
    public void onQueryFinished(Inventory inventory) {
        initUIOnQueryFinished(inventory);
        hideStoreLoading();
    }

    @Override
    public void onQueryFailed(IabResult result) {
        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
        hideStoreLoading();
    }

    private void initUIOnQueryFinished(Inventory inventory) {
        if (inventory.hasDetails(IabProducts.SKU_FULL_VERSION)) {
            // Full version
            if (inventory.hasPurchase(IabProducts.SKU_FULL_VERSION)) {
                // TODO: 나중에 Purchased 처리 다시 해 주기
                mPriceImageView.setBackgroundResource(R.drawable.store_btn_banner_purchased);
                mDiscountedPriceTextView.setText(R.string.store_purchased);
            } else {
                if (StoreDebugCheckUtils.isUsingStore(this)) {
                    if (inventory.getSkuDetails(IabProducts.SKU_FULL_VERSION_ORIGINAL) != null) {
                        mOriginalPriceTextView.setText(
                                inventory.getSkuDetails(IabProducts.SKU_FULL_VERSION_ORIGINAL).getPrice());
                    } else {
                        mOriginalPriceTextView.setText("$4.99");
                    }
                    mDiscountedPriceTextView.setText(
                            inventory.getSkuDetails(IabProducts.SKU_FULL_VERSION).getPrice());

                    mBannerImageView.setClickable(true);
                    mPriceImageView.setClickable(true);
                } else {
                    initUIOnQueryFinishedDebug();
                }
            }
        }
    }

    private void initUIOnQueryFinishedDebug() {
        List<String> ownedSkus = IabProducts.loadOwnedIabProducts(this);
        if (ownedSkus.contains(IabProducts.SKU_FULL_VERSION)) {
            // TODO: Purchased 처리 나중에 해 주기
            mPriceImageView.setBackgroundResource(R.drawable.store_btn_banner_purchased);
            mDiscountedPriceTextView.setText(R.string.store_purchased);
            mBannerImageView.setClickable(false);
            mPriceImageView.setClickable(false);
        } else {
            mPriceImageView.setBackgroundResource(R.drawable.store_btn_banner_price_selector);
            mOriginalPriceTextView.setText("$4.99");
            mDiscountedPriceTextView.setText("$2.99");
            mBannerImageView.setClickable(true);
            mPriceImageView.setClickable(true);
        }
    }
}
