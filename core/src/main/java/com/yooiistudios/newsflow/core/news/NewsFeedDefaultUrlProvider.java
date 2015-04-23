package com.yooiistudios.newsflow.core.news;

import android.content.Context;

import com.yooiistudios.newsflow.core.language.DefaultLocale;
import com.yooiistudios.newsflow.core.news.curation.NewsContentProvider;

import java.util.ArrayList;
import java.util.Locale;

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
        mBottomNewsTopicList = new ArrayList<>();

        // TODO: 첫 실행시 기존에 사용하고 있던 Locale 정보를 통해 이사님 추가 기획을 가지고 초기 패널을 구현할 것
        Locale locale = DefaultLocale.loadDefaultLocale(context);

        if (locale.getLanguage().equals("en")) {
            if (locale.getCountry().equals("US")) {
                makeDefaultNewsTopicsUS(newsContentProvider);
            } else if (locale.getCountry().equals("UK")) {

            } else {
                makeDefaultNewsTopicsUS(newsContentProvider);
            }
        } else if (locale.getLanguage().equals("ko")) {
            makeDefaultNewsTopicsKorea(newsContentProvider);
        } else {
            makeDefaultNewsTopicsUS(newsContentProvider);

            /*
            mTopNewsTopic = newsContentProvider.getNewsTopic("en", null, "US", 2, 1);

            mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "UK", 2, 1));
            mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ko", null, "KR", 2, 2));
            mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("fr", null, "FR", 1, 1));
            mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("zh", "tw", "TW", 1, 1));
            mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ja", null, "JP", 1, 1));
            mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "UK", 1, 4));
            mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ko", null, "KR", 1, 1));
            mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 5, 3));
            */
        }
    }

    private void makeDefaultNewsTopicsKorea(NewsContentProvider newsContentProvider) {
        mTopNewsTopic = newsContentProvider.getNewsTopic("ko", null, "KR", 1, 1);

        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ko", null, "KR", 2, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ko", null, "KR", 3, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ko", null, "KR", 4, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 1, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ko", null, "KR", 2, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ko", null, "KR", 3, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("ko", null, "KR", 4, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 1, 2));
    }

    private void makeDefaultNewsTopicsUS(NewsContentProvider newsContentProvider) {
        mTopNewsTopic = newsContentProvider.getNewsTopic("en", null, "US", 1, 1);

        // TODO: 미국의 경우 나중에 이사님께서 뉴스를 다 넣으신 후 멕시코 첫번째 뉴스를 넣어주기
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 2, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 3, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 4, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 5, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 2, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 3, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 4, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 5, 2));
    }

    public NewsTopic getTopNewsTopic() {
        return mTopNewsTopic;
    }
    public ArrayList<NewsTopic> getBottomNewsTopicList() {
        return mBottomNewsTopicList;
    }
}
