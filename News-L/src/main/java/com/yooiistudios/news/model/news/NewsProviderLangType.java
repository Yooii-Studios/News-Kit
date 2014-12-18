package com.yooiistudios.news.model.news;

import com.yooiistudios.news.R;

import lombok.Getter;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 25.
 *
 * NewsProviderLangType
 *  뉴스 선택화면 - 탭 순서를 정하기 위한 enum
 */
public enum NewsProviderLangType {
    ENGLISH(0, R.raw.news_provider_en, "English"),
    KOREAN(1, R.raw.news_provider_ko, "한국어"),
    JAPANESE(2, R.raw.news_provider_jp, "日本語"),
    FRENCH(3, R.raw.news_provider_fr, "Français");

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
            case 1: return KOREAN;
            case 2: return JAPANESE;
            case 3: return FRENCH;
            default: return ENGLISH;
        }
    }
}
