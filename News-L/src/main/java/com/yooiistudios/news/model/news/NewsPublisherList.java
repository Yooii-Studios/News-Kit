package com.yooiistudios.news.model.news;

import java.util.ArrayList;

import lombok.Getter;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 25.
 *
 * NewsPublisherList
 *  한 언어의 표기, 코드 및 뉴스 퍼플리셔 리스트를 가지는 클래스
 */
public class NewsPublisherList {
    @Getter String englishLanguageName;
    @Getter String regionalLanguageName;

    @Getter String languageCode;
    @Getter String regionCode;

    @Getter ArrayList<NewsPublisher> newsPublishers;

    NewsPublisherList() {
        newsPublishers = new ArrayList<NewsPublisher>();
    }
}
