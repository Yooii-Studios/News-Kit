package com.yooiistudios.newsflow.core.news;

import android.content.Context;

import com.yooiistudios.newsflow.core.news.curation.NewsContentProvider;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 25.
 *
 * NewsFeedUrlProvider
 *  디폴드 뉴스 피드 제공자
 */
public class NewsFeedDefaultUrlProvider {
    private NewsTopic mTopNewsTopic;
    private ArrayList<NewsTopic> mBottomNewsTopicList;

    private static NewsFeedDefaultUrlProvider instance;

    public static NewsFeedDefaultUrlProvider getInstance(Context context) {
        if (instance == null) {
            instance = new NewsFeedDefaultUrlProvider(context);
        }
        return instance;
    }

    private NewsFeedDefaultUrlProvider(Context context) {
        NewsContentProvider newsContentProvider = NewsContentProvider.getInstance(context);

        // TODO: 출시 전 로직 정상화 후 다시 초기화 뉴스를 기획 참고해 지정해줄 것
        mTopNewsTopic = newsContentProvider.getNewsTopic("en", null, "us", 2, 1);

        mBottomNewsTopicList = new ArrayList<>();
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "uk", 2, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ko", null, "kr", 2, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("fr", null, "fr", 1, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("zh", "tw", "hk", 1, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ja", null, "jp", 1, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "uk", 1, 4));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ko", null, "kr", 1, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "us", 4, 2));
    }

    public NewsTopic getTopNewsTopic() {
        return mTopNewsTopic;
    }
    public ArrayList<NewsTopic> getBottomNewsTopicList() {
        return mBottomNewsTopicList;
    }
}
