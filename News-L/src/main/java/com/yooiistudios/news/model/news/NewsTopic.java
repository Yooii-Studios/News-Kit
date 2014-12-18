package com.yooiistudios.news.model.news;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 12. 9.
 *
 * NewsTopic
 *  뉴스 피드에 default로 제공되어야 하는지의 정보를 추가한 클래스
 */
public class NewsTopic extends NewsFeed {

    // identifiers
    private String mLanguageCode;
    private String mRegionCode;
    private int mNewsProviderId;
    private int mId;

    private boolean mIsDefault;

    public boolean isDefault() {
        return mIsDefault;
    }

    public void setDefault(boolean isDefault) {
        mIsDefault = isDefault;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public void setLanguageCode(String languageCode) {
        mLanguageCode = languageCode;
    }

    public String getRegionCode() {
        return mRegionCode;
    }

    public void setRegionCode(String regionCode) {
        mRegionCode = regionCode;
    }

    public int getNewsProviderId() {
        return mNewsProviderId;
    }

    public void setNewsProviderId(int newsProviderId) {
        mNewsProviderId = newsProviderId;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }
}
