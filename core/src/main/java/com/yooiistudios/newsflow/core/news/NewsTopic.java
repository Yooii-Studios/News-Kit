package com.yooiistudios.newsflow.core.news;

import java.io.Serializable;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 12. 9.
 *
 * NewsTopic
 *  뉴스 피드에 default 로 제공되어야 하는지의 정보를 추가한 자료구조
 */
public class NewsTopic implements Serializable, RssFetchable {
    public String languageCode;
    public String regionCode;
    public String countryCode;
    public int newsProviderId;
    public int id;

    public String title;
    public NewsFeedUrl newsFeedUrl;
    private boolean mIsDefault;

    public boolean isDefault() { return mIsDefault; }
    public void setDefault(boolean isDefault) { mIsDefault = isDefault; }

    @Override
    public NewsFeedUrl getNewsFeedUrl() {
        return newsFeedUrl;
    }
}
