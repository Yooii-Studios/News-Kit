package com.yooiistudios.newskit.core.news.curation;

import android.util.SparseArray;

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

    // index 에 해당하는 NewsProviderLangType 를 찾기 위해 매번 switch, if-else, for 등으로
    // 체크하지 않고 초기 클래스가 로드될 경우 한번만 전체 타입을 검사, 맵으로 만들어 둠으로써
    // 가독성 및 퍼포먼스 향상
    private static final SparseArray<NewsProviderLangType> TYPES;

    private int mIndex;
    private int mResourceId;
    private String mTitle;

    static {
        TYPES = new SparseArray<>();

        for (NewsProviderLangType type : NewsProviderLangType.values()) {
            TYPES.put(type.getIndex(), type);
        }
    }

    NewsProviderLangType(int index, int resourceId, String title) {
        mIndex = index;
        mResourceId = resourceId;
        mTitle = title;
    }

    public static NewsProviderLangType valueOf(int index) {
        return TYPES.get(index, ENGLISH);
    }

    public int getIndex() { return mIndex; }
    public int getResourceId() { return mResourceId; }
    public String getTitle() { return mTitle; }
}
