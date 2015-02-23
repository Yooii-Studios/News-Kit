package com.yooiistudios.newsflow.model.news;

import com.yooiistudios.newsflow.R;

import lombok.Getter;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 25.
 *
 * NewsProviderLangType
 *  뉴스 선택화면 - 탭 순서를 정하기 위한 enum
 */
public enum NewsProviderLangType {
    ENGLISH(0, R.raw.news_data_en, "English"),
    FRENCH(1, R.raw.news_data_fr, "Français"),
    CHINESE_TW(2, R.raw.news_data_zh_tw, "繁體中文"),
    GERMAN(3, R.raw.news_data_de, "Deutsch"),
    RUSSIAN(4, R.raw.news_data_ru, "Pусский"),
    JAPANESE(5, R.raw.news_data_ja, "日本語"),
    KOREAN(6, R.raw.news_data_ko, "한국어"),
    SWEDISH(7, R.raw.news_data_sv, "Svenska");

    @Getter private int index;
    @Getter private int resourceId;
    @Getter private String title;

    NewsProviderLangType(int index, int resourceId, String title) {
        this.index = index;
        this.resourceId = resourceId;
        this.title = title;
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
}
