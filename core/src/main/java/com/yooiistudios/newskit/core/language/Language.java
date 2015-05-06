package com.yooiistudios.newskit.core.language;

import com.yooiistudios.newskit.core.R;

/**
 * Created by StevenKim in MorningKit from Yooii Studios Co., LTD. on 2013. 12. 4.
 *
 * LanguageType
 *  뉴스 L의 언어를 enum 으로 표현
 *  index = 설정 창에서 순서를 표현
 *  uniqueId = 이 테마의 고유 id를 표시
 */
public enum Language {
    ENGLISH(0, 0, "en", "", "English", R.string.setting_language_english),
    KOREAN(1, 1, "ko", "", "Korean", R.string.setting_language_korean),
    JAPANESE(2, 2, "ja", "", "Japanese", R.string.setting_language_japanese),
    SIMPLIFIED_CHINESE(3, 3, "zh", "CN", "Chinese (Simplified)", R.string.setting_language_simplified_chinese),
    TRADITIONAL_CHINESE(4, 4, "zh", "TW", "Chinese (Traditional)", R.string.setting_language_traditional_chinese),
    GERMAN(5, 5, "de", "", "German", R.string.setting_language_german),
    SPANISH(6, 6, "es", "", "Spanish", R.string.setting_language_spanish),
//    RUSSIAN(7, 7, "ru", "", "Russian", R.string.setting_language_russian),
    // TODO 다른 언어들이 추가되면 인덱스만 수정할 것
    VIETNAM(7, 8, "vi", "", "Vietnam", R.string.setting_language_vietnam);

    private final int index; // 리스트뷰에 표시할 용도의 index
    private final int uniqueId; // SharedPreferences 에 저장될 용도의 unique id
    private final String languageCode;
    private final String region;
    private final String englishNotation;
    private final int localNotationStringId;

    Language(int index, int uniqueId, String code, String region,
             String englishNotation, int localNotationStringId) {
        this.index = index;
        this.uniqueId = uniqueId;
        this.languageCode = code;
        this.region = region;
        this.englishNotation = englishNotation;
        this.localNotationStringId = localNotationStringId;
    }

    public static Language valueOf(int index) {
        for (Language language : Language.values()) {
            if (language.getIndex() == index) {
                return language;
            }
        }
        throw new IndexOutOfBoundsException("Undefined Enumeration Index");
    }

    public static Language valueOfUniqueId(int uniqueId) {
        for (Language language : Language.values()) {
            if (language.getUniqueId() == uniqueId) {
                return language;
            }
        }
        throw new IndexOutOfBoundsException("Undefined Enumeration Index");
    }

    public static Language valueOfCodeAndRegion(String code, String region) {
        for (Language language : Language.values()) {
            if (language.getLanguageCode().equals(code)) {
                if (language.getLanguageCode().equals("zh") || language.getLanguageCode().equals("pt")) {
                    if (language.getRegion().equals(region)) {
                        return language;
                    }
                } else {
                    return language;
                }
            }

        }
        return ENGLISH;
    }

    public int getIndex() { return index; }
    public int getUniqueId() { return uniqueId; }
    public String getLanguageCode() { return languageCode; }
    public String getRegion() { return region; }
    public String getEnglishNotation() { return englishNotation; }
    public int getLocalNotationStringId() { return localNotationStringId; }
}