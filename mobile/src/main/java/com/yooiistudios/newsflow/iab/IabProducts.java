package com.yooiistudios.newsflow.iab;

import android.content.Context;
import android.content.SharedPreferences;

import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newsflow.iab.util.Inventory;
import com.yooiistudios.newsflow.util.StoreDebugCheckUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by StevenKim in Morning Kit from Yooii Studios Co., LTD. on 2014. 1. 9.
 *
 * SKIabProducts
 */
public class IabProducts {
    public static final String SKU_PRO_VERSION_ORIGINAL = "pro_version_original";
    public static final String SKU_PRO_VERSION = "pro_version"; // "pro_version"
    public static final String SKU_NO_ADS = "no_ads"; // "test_no_ad"
    public static final String SKU_MORE_PANELS = "more_news"; // "test_more_news"
//    public static final String SKU_TOPIC_SELECT = "test_topic_select"; // "topic_select"
    public static final String SKU_CUSTOM_RSS_URL = "custom_rss_feed"; // "test_custom_rss_feed"

    private static final String SHARED_PREFERENCES_IAB = "SHARED_PREFERENCES_IAB";
    private static final String SHARED_PREFERENCES_IAB_DEBUG = "SHARED_PREFERENCES_IAB_DEBUG";

    public static List<String> makeProductKeyList() {
        List<String> iabKeyList = new ArrayList<>();
        iabKeyList.add(SKU_PRO_VERSION_ORIGINAL);
        iabKeyList.add(SKU_PRO_VERSION);
        iabKeyList.add(SKU_NO_ADS);
        iabKeyList.add(SKU_MORE_PANELS);
//        iabKeyList.add(SKU_TOPIC_SELECT);
        iabKeyList.add(SKU_CUSTOM_RSS_URL);
        return iabKeyList;
    }

    // 구매완료시 적용
    public static void saveIabProduct(String sku, Context context) {
        SharedPreferences prefs;
        if (StoreDebugCheckUtils.isUsingStore(context)) {
            prefs = context.getSharedPreferences(SHARED_PREFERENCES_IAB, Context.MODE_PRIVATE);
        } else {
            prefs = context.getSharedPreferences(SHARED_PREFERENCES_IAB_DEBUG, Context.MODE_PRIVATE);
        }
        prefs.edit().putBoolean(sku, true).apply();
    }

    // 인앱 정보를 읽어오며 자동으로 적용
    public static void saveIabProducts(Inventory inventory, Context context) {
        List<String> ownedSkus = inventory.getAllOwnedSkus();

        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFERENCES_IAB, Context.MODE_PRIVATE).edit();
        edit.clear(); // 모두 삭제 후 다시 추가
        for (String sku : ownedSkus) {
            edit.putBoolean(sku, true);
        }
        edit.apply();
    }

    public static boolean containsSku(Context context, String sku) {
        return loadOwnedIabProducts(context).contains(sku);
    }

    // 구매된 아이템들을 로드
    public static List<String> loadOwnedIabProducts(Context context) {
        List<String> ownedSkus = new ArrayList<>();

        SharedPreferences prefs;
        if (StoreDebugCheckUtils.isUsingStore(context)) {
            prefs = context.getSharedPreferences(SHARED_PREFERENCES_IAB, Context.MODE_PRIVATE);
        } else {
            prefs = context.getSharedPreferences(SHARED_PREFERENCES_IAB_DEBUG, Context.MODE_PRIVATE);
        }
        if (prefs.getBoolean(SKU_PRO_VERSION, false)) {
            ownedSkus.add(SKU_PRO_VERSION);
            ownedSkus.add(SKU_NO_ADS);
            ownedSkus.add(SKU_MORE_PANELS);
//            ownedSkus.add(SKU_TOPIC_SELECT);
            ownedSkus.add(SKU_CUSTOM_RSS_URL);
        } else {
            if (prefs.getBoolean(SKU_NO_ADS, false)) {
                ownedSkus.add(SKU_NO_ADS);
            }
            if (prefs.getBoolean(SKU_MORE_PANELS, false)) {
                ownedSkus.add(SKU_MORE_PANELS);
            }
//            if (prefs.getBoolean(SKU_TOPIC_SELECT, false)) {
//                ownedSkus.add(SKU_TOPIC_SELECT);
//            }
            if (prefs.getBoolean(SKU_CUSTOM_RSS_URL, false)) {
                ownedSkus.add(SKU_CUSTOM_RSS_URL);
            }
            // 추가: 언락화면에서 리뷰로 사용한 아이템도 체크
//            SharedPreferences unlockPrefs = context.getSharedPreferences(MNUnlockActivity.SHARED_PREFS, Context.MODE_PRIVATE);
//            String reviewUsedProductSku = unlockPrefs.getString(MNUnlockActivity.REVIEW_USED_PRODUCT_SKU, null);
//            if (reviewUsedProductSku != null && ownedSkus.indexOf(reviewUsedProductSku) == -1) {
//                ownedSkus.add(reviewUsedProductSku);
//            }
        }
        return ownedSkus;
    }

    public static boolean isMatrixAvailable(Context context, PanelMatrix panelMatrix) {
        if (IabProducts.containsSku(context, IabProducts.SKU_MORE_PANELS)) {
            return true;
        } else {
            switch(panelMatrix) {
                case TWO_BY_TWO:
                    return true;
                case THREE_BY_TWO:
                case FOUR_BY_TWO:
                default:
                    return false;
            }
        }
    }

    /**
     * For Debug Mode
     */
    public static void resetIabProductsDebug(Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFERENCES_IAB_DEBUG,
                Context.MODE_PRIVATE).edit();
        edit.clear().apply();
    }

    /**
     * For Naver Store Mode
     */
    // 인앱 정보를 읽어오며 자동으로 적용
//    public static void saveIabProducts(List<NaverIabInventoryItem> productList, Context context) {
//        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFERENCES_IAB, Context.MODE_PRIVATE).edit();
//        edit.clear(); // 모두 삭제 후 다시 추가
//        for (NaverIabInventoryItem naverIabInventoryItem : productList) {
//            if (naverIabInventoryItem.isAvailable()) {
//                edit.putBoolean(NaverIabProductUtils.googleSkuMap.get(naverIabInventoryItem.getKey()), true);
//            }
//        }
//        edit.apply();
//    }
}
