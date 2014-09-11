package com.yooiistudios.news.model.news;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 11.
 *
 * NewsSelectPageLanguage
 *  뉴스 선택 화면의 언어 저장을 위한 클래스
 */
public class NewsSelectPageLanguage {
    private String mEnglishName;
    private String mRegionalName;
    private String mLanguageCode;
    private String mCountryCode;

    public NewsSelectPageLanguage(String englishName, String regionalName,
                                  String languageCode, String countryCode) {
        mEnglishName = englishName;
        mRegionalName = regionalName;
        mLanguageCode = languageCode;
        mCountryCode = countryCode;
    }

    public String getEnglishName() {
        return mEnglishName;
    }

    public String getRegionalName() {
        return mRegionalName;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public String getLanguageCountryCode() {
        if (mCountryCode == null) {
            return mLanguageCode;
        }

        return mLanguageCode + "_" + mCountryCode;
    }
}
