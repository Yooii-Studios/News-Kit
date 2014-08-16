package com.yooiistudios.news.model;

import java.io.Serializable;

/**
 * Created by Dongheyon Jeong on in morning-kit from Yooii Studios Co., LTD. on 2014. 7. 21.
 *
 * MNNewsFeedUrl
 *  Url 을 통해
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
