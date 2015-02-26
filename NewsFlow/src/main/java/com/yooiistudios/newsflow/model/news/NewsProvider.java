package com.yooiistudios.newsflow.model.news;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 13.
 *
 * NewsProvider
 *  뉴스 출판사(ex. 이름 : 구글뉴스, 뉴스피드 리스트 : Top stories, World, Business, ...)
 */
public class NewsProvider {
    public String name;
    private ArrayList<NewsTopic> mNewsTopicList = new ArrayList<>();

    // identifiers
    public String languageCode;
    public String regionCode;
    public String countryCode;
    public int id;

    public ArrayList<NewsTopic> getNewsTopicList() { return mNewsTopicList; }

    public void addNewsTopic(NewsTopic newsTopic) {
        mNewsTopicList.add(newsTopic);
    }

    public NewsTopic findNewsTopicById(int newsTopicId) {
        for (NewsTopic newsTopic : mNewsTopicList) {
            if (newsTopic.id == newsTopicId) {
                return newsTopic;
            }
        }
        return getDefaultNewsTopic();
    }

    private NewsTopic getDefaultNewsTopic() {
        for (NewsTopic newsTopic : mNewsTopicList) {
            if (newsTopic.isDefault()) {
                return newsTopic;
            }
        }
        return getNewsTopicList().get(0);
    }
}
