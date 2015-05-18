package com.yooiistudios.newskit.core.news;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 19.
 *
 * NewsFeedFetchState
 *  fetch 한 뉴스피드의 상태
 */
public enum NewsFeedFetchState {
    NOT_FETCHED_YET(0), // Default value. This means that the newsfeed hasn't been fetched yet.
    SUCCESS(1),
    ERROR_UNKNOWN(2),
    ERROR_INVALID_URL(3),
    ERROR_TIMEOUT(4),
    ERROR_NO_NEWS(5);

    private static final NewsFeedFetchState DEFAULT_STATE = NOT_FETCHED_YET;

    private int mKey;

    NewsFeedFetchState(int key) {
        mKey = key;
    }

    public int getKey() {
        return mKey;
    }

    public static NewsFeedFetchState getByKey(int key) {
        for (NewsFeedFetchState state : NewsFeedFetchState.values()) {
            if (state.getKey() == key) {
                return state;
            }
        }

        return DEFAULT_STATE;
    }
}
