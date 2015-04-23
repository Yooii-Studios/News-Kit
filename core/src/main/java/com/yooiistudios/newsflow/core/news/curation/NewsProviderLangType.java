package com.yooiistudios.newsflow.core.news.curation;

import com.yooiistudios.newsflow.core.R;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 25.
 *
 * NewsProviderLangType
 *  뉴스 선택화면 - 탭 순서를 정하기 위한 enum
 */
public enum NewsProviderLangType {
    ENGLISH(0, R.raw.news_data_en, "English"),
    SPANISH(1, R.raw.news_data_es, "Español"),
    FRENCH(2, R.raw.news_data_fr, "Français"),
    CHINESE_CN(3, R.raw.news_data_zh_cn, "简体中文"),
    CHINESE_TW(4, R.raw.news_data_zh_tw, "繁體中文"),
    GERMAN(5, R.raw.news_data_de, "Deutsch"),
    DUTCH(6, R.raw.news_data_nl, "Nederlands"),
    PORTUGUESE(7, R.raw.news_data_pt, "Português"),
    RUSSIAN(8, R.raw.news_data_ru, "Pусский"),
    JAPANESE(9, R.raw.news_data_ja, "日本語"),
    KOREAN(10, R.raw.news_data_ko, "한국어"),
    TURKISH(11, R.raw.news_data_tr, "Türk"),
    ITALIAN(12, R.raw.news_data_it, "Italiano"),
    NORWEGIAN(13, R.raw.news_data_no, "Norsk"),
    VIETNAMESE(14, R.raw.news_data_vi, "Tiếng Việt"),
    SWEDISH(15, R.raw.news_data_sv, "Svenska"),
    DANISH(16, R.raw.news_data_da, "Dansk"),
    FINNISH(17, R.raw.news_data_fi, "Suomi"),
    INDONESIAN(18, R.raw.news_data_id, "Indonesia"),
    MALAY(19, R.raw.news_data_ms, "Malay"),
    THAI(20, R.raw.news_data_th, "ไทย");

    private int mIndex;
    private int mResourceId;
    private String mTitle;

    NewsProviderLangType(int index, int resourceId, String title) {
        mIndex = index;
        mResourceId = resourceId;
        mTitle = title;
    }

    public static NewsProviderLangType valueOf(int index) {
        switch (index) {
            case 0: return ENGLISH;
            case 1: return SPANISH;
            case 2: return FRENCH;
            case 3: return CHINESE_CN;
            case 4: return CHINESE_TW;
            case 5: return GERMAN;
            case 6: return DUTCH;
            case 7: return PORTUGUESE;
            case 8: return RUSSIAN;
            case 9: return JAPANESE;
            case 10: return KOREAN;
            case 11: return TURKISH;
            case 12: return ITALIAN;
            case 13: return NORWEGIAN;
            case 14: return VIETNAMESE;
            case 15: return SWEDISH;
            case 16: return DANISH;
            case 17: return FINNISH;
            case 18: return INDONESIAN;
            case 19: return MALAY;
            case 20: return THAI;
            default: return ENGLISH;
        }
    }

    public int getIndex() { return mIndex; }
    public int getResourceId() { return mResourceId; }
    public String getTitle() { return mTitle; }
}
