package com.yooiistudios.newsflow.core.news;

import com.yooiistudios.newsflow.core.news.NewsFeedUrl;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 12. 23.
 *
 * RssFetchable
 *  뉴스 피드를 웹에서 읽어올 수 있는 자료구조를 표시함
 */
public interface RssFetchable {
    public NewsFeedUrl getNewsFeedUrl();
}
