package com.yooiistudios.news.model.news;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 13.
 *
 * NewsPublisher
 *  뉴스 출판사(ex. 이름 : 구글뉴스, 뉴스피드 리스트 : Top stories, World, Business, ...)
 */
public class NewsPublisher {
    private String mName;
    private ArrayList<NewsFeed> mNewsFeedList;

    public NewsPublisher() {
        mNewsFeedList = new ArrayList<NewsFeed>();
    }

    public void setName(String name) {
        mName = name;
    }

    public void addNewsFeed(NewsFeed newsFeed) {
        mNewsFeedList.add(newsFeed);
    }

    public String getName() {
        return mName;
    }

    public ArrayList<NewsFeed> getNewsFeedList() {
        return mNewsFeedList;
    }
}