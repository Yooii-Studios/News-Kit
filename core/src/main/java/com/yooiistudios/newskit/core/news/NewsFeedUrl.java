package com.yooiistudios.newskit.core.news;

import java.io.Serializable;

/**
 * Created by Dongheyon Jeong on in morning-kit from Yooii Studios Co., LTD. on 2014. 7. 21.
 *
 * MNNewsFeedUrl
 *  뉴스피드 url 을 가지고 있는 자료구조
 */
public class NewsFeedUrl implements Serializable, RssFetchable {
    private String mUrl;
    private NewsFeedUrlType mType;

    public NewsFeedUrl(String url, NewsFeedUrlType type) {
        mUrl = url;
        mType = type;
    }

    public NewsFeedUrl(String url, int urlTypeUniqueKey) {
        mUrl = url;
        mType = NewsFeedUrlType.getByUniqueKey(urlTypeUniqueKey);
    }

    public NewsFeedUrl(NewsFeedUrl feedUrl) {
        this.mUrl = feedUrl.getUrl();
        this.mType = feedUrl.getType();
    }

    public String getUrl() {
        return mUrl;
    }

    public NewsFeedUrlType getType() {
        return mType;
    }

    @Override
    public NewsFeedUrl getNewsFeedUrl() {
        return this;
    }
}
