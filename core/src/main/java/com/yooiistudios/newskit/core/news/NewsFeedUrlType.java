package com.yooiistudios.newskit.core.news;

/**
 * Created by Dongheyon Jeong on in morning-kit from Yooii Studios Co., LTD. on 2014. 7. 21.
 *
 * MNNewsFeedUrlType
 *  뉴스피드의 출처를 Type 으로 정리
 */
public enum NewsFeedUrlType {
    GENERAL(0),
    GOOGLE(1),
    YAHOO(2),
    CUSTOM(3);

    private int mUniqueKey;

    private NewsFeedUrlType(int uniqueKey) {
        mUniqueKey = uniqueKey;
    }

    public int getUniqueKey() {
        return mUniqueKey;
    }

    public static NewsFeedUrlType getByUniqueKey(int uniqueKey) {
        for (NewsFeedUrlType urlType : NewsFeedUrlType.values()) {
            if (urlType.getUniqueKey() == uniqueKey) {
                return urlType;
            }
        }

        return GENERAL;
    }
}
