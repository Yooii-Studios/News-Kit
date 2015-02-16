package com.yooiistudios.newsflow.model.news;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 13.
 *
 * NewsProvider
 *  뉴스 출판사(ex. 이름 : 구글뉴스, 뉴스피드 리스트 : Top stories, World, Business, ...)
 */
public class NewsProvider {
    private String mName;
    private ArrayList<NewsTopic> mNewsTopicList;

    // identifiers
    private String mLanguageCode;
    private String mRegionCode;
    private int mId;

    public NewsProvider() {
        mNewsTopicList = new ArrayList<>();
    }

    public void setName(String name) {
        mName = name;
    }

    public void addNewsTopic(NewsTopic newsTopic) {
        mNewsTopicList.add(newsTopic);
    }

    public String getName() {
        return mName;
    }

    public ArrayList<NewsTopic> getNewsTopicList() {
        return mNewsTopicList;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public void setLanguageCode(String languageCode) {
        mLanguageCode = languageCode;
    }

    public String getRegionCode() {
        return mRegionCode;
    }

    public void setRegionCode(String regionCode) {
        mRegionCode = regionCode;
    }

    public NewsTopic getNewsTopic(int newsTopicId) {
        for (NewsTopic newsTopic : mNewsTopicList) {
            if (newsTopic.getId() == newsTopicId) {
                return newsTopic;
            }
        }
        return null;
    }

    public NewsTopic getDefaultNewsTopic() {
        for (NewsTopic newsTopic : mNewsTopicList) {
            if (newsTopic.isDefault()) {
                return newsTopic;
            }
        }
        return null;
    }
}
