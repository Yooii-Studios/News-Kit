package com.yooiistudios.newsflow.core.news.curation;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 25.
 *
 * NewsProviderLangType
 *  뉴스 선택화면 - 탭 순서를 정하기 위한 enum
 */
public enum NewsProviderLangType {
    ENGLISH(0, com.yooiistudios.newsflow.core.R.raw.news_data_en, "English"),
    FRENCH(1, com.yooiistudios.newsflow.core.R.raw.news_data_fr, "Français"),
    CHINESE_TW(2, com.yooiistudios.newsflow.core.R.raw.news_data_zh_tw, "繁體中文"),
    GERMAN(3, com.yooiistudios.newsflow.core.R.raw.news_data_de, "Deutsch"),
    RUSSIAN(4, com.yooiistudios.newsflow.core.R.raw.news_data_ru, "Pусский"),
    JAPANESE(5, com.yooiistudios.newsflow.core.R.raw.news_data_ja, "日本語"),
    KOREAN(6, com.yooiistudios.newsflow.core.R.raw.news_data_ko, "한국어"),
    SWEDISH(7, com.yooiistudios.newsflow.core.R.raw.news_data_sv, "Svenska");

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
            case 1: return FRENCH;
            case 2: return CHINESE_TW;
            case 3: return GERMAN;
            case 4: return RUSSIAN;
            case 5: return JAPANESE;
            case 6: return KOREAN;
            case 7: return SWEDISH;
            default: return ENGLISH;
        }
    }

    public int getIndex() { return mIndex; }
    public int getResourceId() { return mResourceId; }
    public String getTitle() { return mTitle; }
}
