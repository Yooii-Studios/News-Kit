package com.yooiistudios.news.store.iab;

//import android.support.v7.app.ActionBarActivity;

import android.app.Activity;

import com.yooiistudios.news.common.encryption.SKMd5Utils;
import com.yooiistudios.news.common.log.NLLog;
import com.yooiistudios.news.store.iab.util.IabHelper;
import com.yooiistudios.news.store.iab.util.IabResult;
import com.yooiistudios.news.store.iab.util.Inventory;

import java.util.List;

import lombok.Getter;

/**
 * Created by StevenKim in Morning Kit from Yooii Studios Co., LTD. on 2014. 1. 9.
 *
 * SKIabManager
 *  In-App-Billing과 관련된 로직을 래핑한 클래스
 */
public class SKIabManager {
    public static final int IAB_REQUEST_CODE = 10002;
//    public static final String DEVELOPER_PAYLOAD= "SKIabManager_Payload";
    private static final String TAG = "SKIabManager";
    private SKIabManagerListener iapManagerListener;
    @Getter private IabHelper helper;
    private Activity activity;
    private String base64EncodedPublicKey;

    private SKIabManager() {}
    public SKIabManager(Activity activity, SKIabManagerListener iapManagerListener) {
        this.activity = activity;
        this.iapManagerListener = iapManagerListener;
        this.base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm4R/S6QfFgGTlnX/jZSdSj8svHYhcp+im7VI3uLfXA5oHZPYdXbpE6DeEnrKEbVGYU1FEMKNaBTRRw9ogk+F+BmNU+1IdUEqygQfIzNohvmuTEKXCTBMSP3iaImTamH8bTkJhCRaBO8cz13FfhDgPeio6NmGhz9oB0RSFVlp4ZFC4oVM2if7BYWBWF3xS3VhiiURNuXOvyGPynUPb+EoK9pn/3LGIjbGCxdRciARkBW3GMhsqc29grTlenXtWiFM7T/V3h2rKJUyUPV6pj8Nb0OcW9fv9Y1NhoFitbTxgoz+FBQ/E3fEsch9Bvjv9AIni9d2vQ0DKWbclFankVb1jQIDAQAB";
    }

    public void loadWithAllItems() {
        load(false);
    }

    public void loadWithOnlyOwnedItems() {
        load(true);
    }

    private void load(final boolean isOwnItemsOnly) {
        // compute your public key and store it in base64EncodedPublicKey
        helper = new IabHelper(activity, base64EncodedPublicKey);
//        helper.enableDebugLogging(true); // You shoud off this when you publish

        helper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    NLLog.e(TAG, "Problem setting up In-app Billing: " +
                            result);
                    iapManagerListener.onIabSetupFailed(result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (helper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                iapManagerListener.onIabSetupFinished(result);

                if (isOwnItemsOnly) {
                    queryOwnItemsInformation();
                } else {
                    queryAllItemsInformation();
                }
            }
        });
    }

    private void queryAllItemsInformation() {
        List<String> iabProductsSkuList = SKIabProducts.makeProductKeyList();
        helper.queryInventoryAsync(true, iabProductsSkuList, new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                // Have we been disposed of in the meantime? If so, quit.
                if (helper == null) return;

                // Is it a failure?
                if (result.isFailure()) {
                    iapManagerListener.onQueryFailed(result);
                } else {
                    SKIabProducts.saveIabProducts(inv, activity); // 구매한 상품은 저장
                    iapManagerListener.onQueryFinished(inv);
                }
            }
        });
    }

    private void queryOwnItemsInformation() {
        helper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                // Have we been disposed of in the meantime? If so, quit.
                if (helper == null) return;

                // Is it a failure?
                if (result.isFailure()) {
                    iapManagerListener.onQueryFailed(result);
                } else {
                    iapManagerListener.onQueryFinished(inv);
                }
            }
        });
    }

    public void dispose() {
        if (helper != null) {
            helper.dispose();
            helper = null;
        }
    }

    public void processPurchase(String sku, IabHelper.OnIabPurchaseFinishedListener onIabPurchaseFinishedListener) {
        try {
            // 페이로드를 특정 스트링으로 했었는데, 창하님의 조언으로는 sku의 md5 값과 맞추는 것이 그나마 해킹 확률이 줄어 들 것이라고 말하심
            helper.launchPurchaseFlow(activity, sku, IabHelper.ITEM_TYPE_INAPP, IAB_REQUEST_CODE, onIabPurchaseFinishedListener, SKMd5Utils.getMd5String(sku));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
