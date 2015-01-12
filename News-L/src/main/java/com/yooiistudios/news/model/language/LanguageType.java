package com.yooiistudios.news.model.language;

import com.yooiistudios.news.R;

/**
 * Created by StevenKim in MorningKit from Yooii Studios Co., LTD. on 2013. 12. 4.
 *
 * NLLanguageType
 *  뉴스 L의 언어를 enum 으로 표현
 *  index = 설정 창에서 순서를 표현
 *  uniqueId = 이 테마의 고유 id를 표시
 */
public enum LanguageType {
    ENGLISH(0, 0, "en", "",
            "English", R.string.setting_language_english),
    KOREAN(1, 1, "ko", "",
            "Korean", R.string.setting_language_korean),
    JAPANESE(2, 2, "ja", "",
            "Japanese", R.string.setting_language_japanese),
    SIMPLIFIED_CHINESE(3, 3, "zh", "CN",
            "Chinese (Simplified)", R.string.setting_language_simplified_chinese),
    TRADITIONAL_CHINESE(4, 4, "zh", "TW",
            "Chinese (Traditional)", R.string.setting_language_traditional_chinese),
    RUSSIAN(5, 5, "ru", "",
            "Russian", R.string.setting_language_russian);

    private final int index; // 리스트뷰에 표시할 용도의 index
    private final int uniqueId; // SharedPreferences 에 저장될 용도의 unique id
    private final String code;
    private final String region;
    private final String englishNotation;
    private final int localNotationStringId;

    private LanguageType(int index, int uniqueId, String code, String region,
                         String englishNotation, int localNotationStringId) {
        this.index = index;
        this.uniqueId = uniqueId;
        this.code = code;
        this.region = region;
        this.englishNotation = englishNotation;
        this.localNotationStringId = localNotationStringId;
    }

    public static LanguageType valueOf(int index) {
        for (LanguageType languageType : LanguageType.values()) {
            if (languageType.getIndex() == index) {
                return languageType;
            }
        }
        throw new IndexOutOfBoundsException("Undefined Enumeration Index");
    }

    public static LanguageType valueOfUniqueId(int uniqueId) {
        for (LanguageType languageType : LanguageType.values()) {
            if (languageType.getUniqueId() == uniqueId) {
                return languageType;
            }
        }
        throw new IndexOutOfBoundsException("Undefined Enumeration Index");
    }

    public static LanguageType valueOfCodeAndRegion(String code, String region) {
        for (LanguageType languageType : LanguageType.values()) {
            if (languageType.getCode().equals(code)) {
                if (languageType.getCode().equals("zh") || languageType.getCode().equals("pt")) {
                    if (languageType.getRegion().equals(region)) {
                        return languageType;
                    }
                } else {
                    return languageType;
                }
            }

        }
        return ENGLISH;
    }

    public int getIndex() { return index; }
    public int getUniqueId() { return uniqueId; }
    public String getCode() { return code; }
    public String getRegion() { return region; }
    public String getEnglishNotation() { return englishNotation; }
    public int getLocalNotationStringId() { return localNotationStringId; }
}