package com.yooiistudios.news.model;

import java.util.ArrayList;

import nl.matshofman.saxrssreader.RssFeed;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * Rss Feed의 Feed를 표현하는 클래스
 */
public class NLNewsFeed extends RssFeed {
    public static final String NEWS_FEED = "NEWS_FEED";

    private ArrayList<NLNews> mNewsItems;

    public NLNewsFeed() {
        mNewsItems = new ArrayList<NLNews>();
    }

    public void addNews(NLNews news) {
        mNewsItems.add(news);
    }

    public void setNewsList(ArrayList<NLNews> newsList) {
        mNewsItems = newsList;
    }

    public ArrayList<NLNews> getNewsList() {
        return mNewsItems;
    }

    /**
     * 이미지 url을 포함하고 있는 뉴스만 반환한다.
     * @return ArrayList of NLNews which has image url.
     */
    public ArrayList<NLNews> getNewsListContainsImageUrl() {
        ArrayList<NLNews> containingList = new ArrayList<NLNews>();

        for (NLNews news : mNewsItems) {
            if (news.getImageUrlList().size() > 0) {
                containingList.add(news);
            }
        }

        return containingList;
    }

}
