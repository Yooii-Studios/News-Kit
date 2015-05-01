package com.yooiistudios.newskit.iab.naver;

import com.yooiistudios.newskit.iab.IabProducts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NIAPUtils {
    public static final int NIAP_REQUEST_CODE = 100;
	public static final String NIAP_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC273wBt+dcVclW1WKmorA511mMgAjcYwzPWZyhSE8VOg7K9ixm/gLH/GdWxbmU2y+kqO7Z/Onqu4+opHJmZ3Si3dn8NWdrJXQvXfZMUaFV0vo27t5SF7lPVglpWi4QsQDCK+dHIFNFJIIMTecXFk4kQFdCdKdh5q2PcZHPOw1c7QIDAQAB";

    private static final String NAVER_PRO_VERSION_ORIGINAL = "1000016321";
    private static final String NAVER_PRO_VERSION = "1000016320";
    private static final String NAVER_NO_ADS = "1000016319";
    private static final String NAVER_MORE_PANELS = "1000016318";
    private static final String NAVER_CUSTOM_RSS_URL = "1000016316";

    private static final Map<String, String> naverSkuMap;
    private static final Map<String, String> googleSkuMap;

    private NIAPUtils() { throw new AssertionError("You MUST not create this class!"); }

    static {
        naverSkuMap = new HashMap<>();
        naverSkuMap.put(IabProducts.SKU_PRO_VERSION_ORIGINAL, NAVER_PRO_VERSION_ORIGINAL);
        naverSkuMap.put(IabProducts.SKU_PRO_VERSION, NAVER_PRO_VERSION);
        naverSkuMap.put(IabProducts.SKU_NO_ADS, NAVER_NO_ADS);
        naverSkuMap.put(IabProducts.SKU_MORE_PANELS, NAVER_MORE_PANELS);
        naverSkuMap.put(IabProducts.SKU_CUSTOM_RSS_URL, NAVER_CUSTOM_RSS_URL);

        googleSkuMap = new HashMap<>();
        googleSkuMap.put(NAVER_PRO_VERSION_ORIGINAL, IabProducts.SKU_PRO_VERSION_ORIGINAL);
        googleSkuMap.put(NAVER_PRO_VERSION, IabProducts.SKU_PRO_VERSION);
        googleSkuMap.put(NAVER_NO_ADS, IabProducts.SKU_NO_ADS);
        googleSkuMap.put(NAVER_MORE_PANELS, IabProducts.SKU_MORE_PANELS);
        googleSkuMap.put(NAVER_CUSTOM_RSS_URL, IabProducts.SKU_CUSTOM_RSS_URL);
    }

    public static String convertToGoogleSku(String naverSku) {
        return googleSkuMap.get(naverSku);
    }

    public static String convertToNaverSku(String googleSku) {
        return naverSkuMap.get(googleSku);
    }

    public static ArrayList<String> getAllProducts() {
        // 구글맵의 네이버 프로덕트 id 들
        return new ArrayList<>(googleSkuMap.keySet());
    }
}
