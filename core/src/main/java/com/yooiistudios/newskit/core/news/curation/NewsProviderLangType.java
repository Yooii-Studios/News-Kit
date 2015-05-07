package com.yooiistudios.newskit.core.news.curation;

import com.yooiistudios.newskit.core.R;

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
    RUSSIAN(6, R.raw.news_data_ru, "Pусский"),
    PORTUGUESE(7, R.raw.news_data_pt, "Português"),
    JAPANESE(8, R.raw.news_data_ja, "日本語"),
    KOREAN(9, R.raw.news_data_ko, "한국어"),
    TURKISH(10, R.raw.news_data_tr, "Türk"),
    ITALIAN(11, R.raw.news_data_it, "Italiano"),
    VIETNAMESE(12, R.raw.news_data_vi, "Tiếng Việt"),
    SWEDISH(13, R.raw.news_data_sv, "Svenska"),
    NORWEGIAN(14, R.raw.news_data_nb, "Norsk Bokmål"),
    FINNISH(15, R.raw.news_data_fi, "Suomi"),
    DANISH(16, R.raw.news_data_da, "Dansk"),
    DUTCH(17, R.raw.news_data_nl, "Nederlands"),
    THAI(18, R.raw.news_data_th, "ไทย"),
    MALAY(19, R.raw.news_data_ms, "Malay"),
    INDONESIAN(20, R.raw.news_data_in, "Indonesia"),
    ARABIC(21, R.raw.news_data_ar, "العربية"),
    POLISH(22, R.raw.news_data_pl, "Język polski"),
    GREEK(23, R.raw.news_data_el, "Ελληνικά"),
    CZECH(24, R.raw.news_data_cs, "Čeština"),
    BULGARIAN(25, R.raw.news_data_bg, "Български"),
    BELARUSIAN(26, R.raw.news_data_be, "беларускі"),
    CROATIAN(27, R.raw.news_data_hr, "Hrvatski"),
    HUNGARIAN(28, R.raw.news_data_hu, "Magyar"),
    KAZAKH(29, R.raw.news_data_kk, "Қазақ"),
    ROMANIAN(30, R.raw.news_data_ro, "Român"),
    SERBIAN(31, R.raw.news_data_sr, "Cрпски"),
    SLOVAK(32, R.raw.news_data_sk, "Slovenčina"),
    UKRAINIAN(33, R.raw.news_data_uk, "Украї́нська"),
    PERSIAN(34, R.raw.news_data_fa, "فارسی");

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
            case 0:  return ENGLISH;
            case 1:  return SPANISH;
            case 2:  return FRENCH;
            case 3:  return CHINESE_CN;
            case 4:  return CHINESE_TW;
            case 5:  return GERMAN;
            case 6:  return RUSSIAN;
            case 7:  return PORTUGUESE;
            case 8:  return JAPANESE;
            case 9:  return KOREAN;
            case 10: return TURKISH;
            case 11: return ITALIAN;
            case 12: return VIETNAMESE;
            case 13: return SWEDISH;
            case 14: return NORWEGIAN;
            case 15: return FINNISH;
            case 16: return DANISH;
            case 17: return DUTCH;
            case 18: return THAI;
            case 19: return MALAY;
            case 20: return INDONESIAN;
            case 21: return ARABIC;
            case 22: return POLISH;
            case 23: return GREEK;
            case 24: return CZECH;
            case 25: return BULGARIAN;
            case 26: return BELARUSIAN;
            case 27: return CROATIAN;
            case 28: return HUNGARIAN;
            case 29: return KAZAKH;
            case 30: return ROMANIAN;
            case 31: return SERBIAN;
            case 32: return SLOVAK;
            case 33: return UKRAINIAN;
            case 34: return PERSIAN;
            default: return ENGLISH;
        }
    }

    public int getIndex() { return mIndex; }
    public int getResourceId() { return mResourceId; }
    public String getTitle() { return mTitle; }
}
