package com.yooiistudios.news.model.news;

import com.yooiistudios.news.model.RssFetchable;

import java.io.Serializable;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 12. 9.
 *
 * NewsTopic
 *  뉴스 피드에 default 로 제공되어야 하는지의 정보를 추가한 클래스
 */
public class NewsTopic implements Serializable, RssFetchable {

    // identifiers
    private String mLanguageCode;
    private String mRegionCode;
    private int mNewsProviderId;
    private int mId;

    private String mTitle;
    private NewsFeedUrl mNewsFeedUrl;
    private boolean mIsDefault;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public NewsFeedUrl getNewsFeedUrl() {
        return mNewsFeedUrl;
    }

    public void setNewsFeedUrl(NewsFeedUrl newsFeedUrl) {
        mNewsFeedUrl = newsFeedUrl;
    }

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
