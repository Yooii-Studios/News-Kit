package com.yooiistudios.newsflow.model.news;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 19.
 *
 * NewsFeedFetchState
 *  fetch 한 뉴스피드의 상태
 */
public enum NewsFeedFetchState {
    NOT_FETCHED_YET, // Default value. This means that the newsfeed hasn't been fetched yet.
    SUCCESS,
    ERROR_UNKNOWN,
    ERROR_INVALID_URL,
    ERROR_TIMEOUT
}
