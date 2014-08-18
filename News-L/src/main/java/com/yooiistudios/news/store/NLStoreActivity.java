package com.yooiistudios.news.store;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yooiistudios.news.R;
import com.yooiistudios.news.common.encryption.SKMd5Utils;
import com.yooiistudios.news.common.log.NLLog;
import com.yooiistudios.news.store.iab.SKIabManager;
import com.yooiistudios.news.store.iab.SKIabManagerListener;
import com.yooiistudios.news.store.iab.SKIabProducts;
import com.yooiistudios.news.store.iab.util.IabHelper;
import com.yooiistudios.news.store.iab.util.IabResult;
import com.yooiistudios.news.store.iab.util.Inventory;
import com.yooiistudios.news.store.iab.util.Purchase;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NLStoreActivity extends Activity implements SKIabManagerListener, IabHelper.OnIabPurchaseFinishedListener {

    private static final String TAG = "NLStoreActivity";
    private SKIabManager iabManager;

    @InjectView(R.id.store_progressBar) ProgressBar progressBar;
    @InjectView(R.id.store_loading_view) View loadingView;

    @InjectView(R.id.store_full_version_text_view)  TextView fullVersionTextView;
    @InjectView(R.id.store_full_version_button)     Button fullVersionButton;

    @InjectView(R.id.store_no_ad_text_view)         TextView noAdTextView;
    @InjectView(R.id.store_no_ad_button)            Button noAdButton;

    @InjectView(R.id.store_more_news_text_view)     TextView moreNewsTextView;
    @InjectView(R.id.store_more_news_button)        Button moreNewsButton;

    @InjectView(R.id.store_feature1_text_view)      TextView feature1TextView;
    @InjectView(R.id.store_feature1_button)         Button feature1Button;

    // For Test
    @InjectView(R.id.store_reset_button)            Button resetButton;
    @InjectView(R.id.store_debug_button)            Button debugButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_test);
        ButterKnife.inject(this);

        showStoreLoading();

        initActionBar();
        initIab();
        initUI();
        checkDebug();
    }

    /**
     * Init
     */
    private void initActionBar() {

    }

    private void initIab() {
        iabManager = new SKIabManager(this, this);
        iabManager.loadWithAllItems();
    }

    private void initUIAfterQuery(Inventory inventory) {
        if (inventory.hasDetails(SKIabProducts.SKU_FULL_VERSION) &&
                inventory.hasPurchase(SKIabProducts.SKU_FULL_VERSION)) {
            fullVersionButton.setText("Purchased");
            fullVersionButton.setClickable(false);
            fullVersionButton.setEnabled(false);
        } else if (inventory.hasDetails(SKIabProducts.SKU_MORE_NEWS) &&
                inventory.hasPurchase(SKIabProducts.SKU_MORE_NEWS)) {
            moreNewsButton.setText("Purchased");
            moreNewsButton.setClickable(false);
            moreNewsButton.setEnabled(false);
        } else if (inventory.hasDetails(SKIabProducts.SKU_NO_ADS) &&
                inventory.hasPurchase(SKIabProducts.SKU_NO_ADS)) {
            noAdButton.setText("Purchased");
            noAdButton.setClickable(false);
            noAdButton.setEnabled(false);
        } else if (inventory.hasDetails(SKIabProducts.SKU_TEMP_FEATURE) &&
                inventory.hasPurchase(SKIabProducts.SKU_TEMP_FEATURE)) {
            feature1Button.setText("Purchased");
            feature1Button.setClickable(false);
            feature1Button.setEnabled(false);
        }
    }

    private void initUI() {

    }

    private void checkDebug() {
        if (NLLog.isDebug) {
            resetButton.setVisibility(View.VISIBLE);
            debugButton.setVisibility(View.VISIBLE);
            if (NLStoreDebugChecker.isUsingStore(this)) {
//                if (IS_STORE_FOR_NAVER) {
//                    debugButton.setText("Naver Store");
//                } else {
                    debugButton.setText("Google Store");
//                }
            } else {
                debugButton.setText("Debug");
            }
        } else {
            resetButton.setVisibility(View.GONE);
            debugButton.setVisibility(View.GONE);
            NLStoreDebugChecker.setUsingStore(true, this);
        }
    }

    private void updateUIOnPurchase(Purchase info) {
        if (info.getSku().equals(SKIabProducts.SKU_FULL_VERSION)) {
            fullVersionButton.setText("Purchased");
            fullVersionButton.setClickable(false);
            fullVersionButton.setEnabled(false);
        } else if (info.getSku().equals(SKIabProducts.SKU_MORE_NEWS)) {
            moreNewsButton.setText("Purchased");
            moreNewsButton.setClickable(false);
            moreNewsButton.setEnabled(false);
        } else if (info.getSku().equals(SKIabProducts.SKU_NO_ADS)) {
            noAdButton.setText("Purchased");
            noAdButton.setClickable(false);
            noAdButton.setEnabled(false);
        } else if (info.getSku().equals(SKIabProducts.SKU_TEMP_FEATURE)) {
            feature1Button.setText("Purchased");
            feature1Button.setClickable(false);
            feature1Button.setEnabled(false);
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
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
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

    /**
     * IAB Listener
     */
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
        initUIAfterQuery(inventory);
        hideStoreLoading();
    }

    @Override
    public void onQueryFailed(IabResult result) {
        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
        hideStoreLoading();
    }

    /**
     * IabHelper.OnIabPurchaseFinishedListener
     * @param result The result of the purchase.
     * @param info The purchase information (null if purchase failed)
     */
    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {
        // 구매된 리스트를 확인해 SharedPreferences 에 적용
        if (result.isSuccess()) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();

            // 창하님 조언으로 수정: payload 는 sku 의 md5 해시값으로 비교해 해킹을 방지
            // 또한 orderId 는 무조건 37자리여야 한다고 하심. 프리덤같은 가짜 결제는 자릿수가 짧게 온다고 하심
            if (info != null && info.getDeveloperPayload().equals(SKMd5Utils.getMd5String(info.getSku()))
                    && info.getOrderId().length() == 37) {
                // 프레퍼런스에 저장
                SKIabProducts.saveIabProduct(info.getSku(), this);
                updateUIOnPurchase(info);
            } else if (info == null) {
                showComplain("No purchase info");
            } else {
                showComplain("Payload problem");
                if (!info.getPackageName().equals(SKMd5Utils.getMd5String(info.getSku()))) {
                    Log.e(TAG, "payload not equals to md5 hash of sku");
                } else if (info.getOrderId().length() != 37) {
                    Log.e(TAG, "length of orderId is not 37");
                }
            }
        } else {
            showComplain("Purchase Failed");
        }
    }

    private void showComplain(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    /**
     * Test
     */
    public void onResetButtonClicked(View view) {
        // 디버그 상태에서 구매했던 아이템들을 리셋
        if (NLStoreDebugChecker.isUsingStore(this)) {
            if (iabManager != null) {
                iabManager.loadWithAllItems();
            }
            initUI();
        } else {
            SKIabProducts.resetIabProductsDebug(this);
            initUI();
        }
    }

    public void onDebugButtonClicked(View view) {
        if (NLStoreDebugChecker.isUsingStore(this)) {
            debugButton.setText("Debug");
            NLStoreDebugChecker.setUsingStore(false, this);
        } else {
            debugButton.setText("Google Store");
            NLStoreDebugChecker.setUsingStore(true, this);
        }
    }

    /**
     * Buy
     */
    public void onFullVersionButtonClicked(View view) {
        if (NLStoreDebugChecker.isUsingStore(this)) {
            iabManager.processPurchase(SKIabProducts.SKU_FULL_VERSION, this);
        } else {
            SKIabProducts.saveIabProduct(SKIabProducts.SKU_FULL_VERSION, this);
            initUI();
        }
    }

    public void onNoAdButtonClicked(View view) {
        if (NLStoreDebugChecker.isUsingStore(this)) {
            iabManager.processPurchase(SKIabProducts.SKU_NO_ADS, this);
        } else {
            SKIabProducts.saveIabProduct(SKIabProducts.SKU_NO_ADS, this);
            initUI();
        }
    }

    public void onMoreNewsButtonClicked(View view) {
        if (NLStoreDebugChecker.isUsingStore(this)) {
            iabManager.processPurchase(SKIabProducts.SKU_MORE_NEWS, this);
        } else {
            SKIabProducts.saveIabProduct(SKIabProducts.SKU_MORE_NEWS, this);
            initUI();
        }
    }

    public void onFeature1ButtonClicked(View view) {
        if (NLStoreDebugChecker.isUsingStore(this)) {
            iabManager.processPurchase(SKIabProducts.SKU_TEMP_FEATURE, this);
        } else {
            SKIabProducts.saveIabProduct(SKIabProducts.SKU_TEMP_FEATURE, this);
            initUI();
        }
    }
}
