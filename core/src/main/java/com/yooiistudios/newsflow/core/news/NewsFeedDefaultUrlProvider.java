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
 *  영어의 경우에는 5번째 뉴스에 멕시코 1번째 뉴스(구글)을 넣고, 그 외의 언어는 전부 미국의 1번째 뉴스를 넣을 것
 *  그리고 각 나라의 2, 3, 4, 5번째 언론사가 없을 경우 미국의 같은 인덱스의 언론사를 넣을 것.
 *
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

        // TODO: 첫 실행시 기존에 사용하고 있던 Locale 정보를 통해 이사님 추가 기획을 가지고 초기 패널을 구현할 것. 전부 다 구현되면 TODO 삭제
        Locale locale = DefaultLocale.loadDefaultLocale(context);

        if (locale.getLanguage().equals("en")) {
            if (locale.getCountry().equals("US")) {
                makeDefaultNewsTopicsUS(newsContentProvider);
            } else if (locale.getCountry().equals("GB")) {
                makeDefaultNewsTopicsUK(newsContentProvider);
            } else {
                makeDefaultNewsTopicsUS(newsContentProvider);
            }
        } else if (locale.getLanguage().equals("ko")) {
            makeDefaultNewsTopicsKorea(newsContentProvider);
        } else {
            makeDefaultNewsTopicsUS(newsContentProvider);
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

        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 2, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 3, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 4, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("es", null, "MX", 1, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 2, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 3, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 4, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("es", null, "MX", 1, 2));
    }

    private void makeDefaultNewsTopicsUK(NewsContentProvider newsContentProvider) {
        mTopNewsTopic = newsContentProvider.getNewsTopic("en", null, "US", 1, 1);

        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "GB", 2, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "GB", 3, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "GB", 4, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 1, 1));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "GB", 2, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "GB", 3, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "GB", 4, 2));
        mBottomNewsTopicList.add(newsContentProvider.getNewsTopic("en", null, "US", 1, 2));
    }

    public NewsTopic getTopNewsTopic() {
        return mTopNewsTopic;
    }
    public ArrayList<NewsTopic> getBottomNewsTopicList() {
        return mBottomNewsTopicList;
    }
}
