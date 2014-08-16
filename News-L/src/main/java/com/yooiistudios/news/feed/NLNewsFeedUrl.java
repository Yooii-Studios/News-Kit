package com.yooiistudios.news.feed;

import java.io.Serializable;

/**
 * Created by Dongheyon Jeong on in morning-kit from Yooii Studios Co., LTD. on 2014. 7. 21.
 *
 * MNNewsFeedUrl
 *  뉴스피드 url 을 가지고 있는 자료구조
 */
public class NLNewsFeedUrl implements Serializable {
    private String mUrl;
    private NLNewsFeedUrlType mType;

    public NLNewsFeedUrl(String url, NLNewsFeedUrlType type) {
        mUrl = url;
        mType = type;
    }
    public NLNewsFeedUrl(NLNewsFeedUrl feedUrl) {
        this.mUrl = feedUrl.getUrl();
        this.mType = feedUrl.getType();
    }

    public String getUrl() {
        return mUrl;
    }
    public NLNewsFeedUrlType getType() {
        return mType;
    }
}
