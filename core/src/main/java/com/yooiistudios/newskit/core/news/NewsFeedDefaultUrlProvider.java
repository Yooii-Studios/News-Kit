package com.yooiistudios.newskit.core.news;

import android.content.Context;

import com.yooiistudios.newskit.core.language.DefaultLocale;
import com.yooiistudios.newskit.core.news.curation.NewsContentProvider;

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

        Locale locale = DefaultLocale.loadDefaultLocale(context);

        if (locale.getCountry().equals("US")) {
            makeDefaultNewsTopics(newsContentProvider, "en", null, "US",
                    "es", null, "MX");
        } else if (locale.getLanguage().equals("zh") && locale.getCountry().equals("CN")){
            makeDefaultNewsTopics(newsContentProvider, locale.getLanguage(), "CN",
                    locale.getCountry(), "en", null, "US");
        } else if (locale.getLanguage().equals("zh") && locale.getCountry().equals("TW")){
            makeDefaultNewsTopics(newsContentProvider, locale.getLanguage(), "TW",
                    locale.getCountry(), "en", null, "US");
//        } else if (locale.getLanguage().equals("zh") && locale.getCountry().equals("HK")){
//            makeDefaultNewsTopics(newsContentProvider, locale.getLanguage(), "HK",
//                    locale.getCountry(), "en", null, "US");
        } else {
            // 기본은 Default Locale 값 + 영어
            // regionCode 는 중국어를 위해 null 을 넣지 않고 country 로 대체
            makeDefaultNewsTopics(newsContentProvider, locale.getLanguage(), null,
                    locale.getCountry(), "en", null, "US");
        }
    }

    public NewsTopic getTopNewsTopic() {
        return mTopNewsTopic;
    }
    public ArrayList<NewsTopic> getBottomNewsTopicList() {
        return mBottomNewsTopicList;
    }

    private void makeDefaultNewsTopics(NewsContentProvider newsContentProvider,
                                       String defaultLanguageCode, String defaultRegionCode,
                                       String defaultCountryCode, String subLanguageCode,
                                       String subRegionCode, String subCountryCode) {
        // A1
        mTopNewsTopic = newsContentProvider.getNewsTopic(defaultLanguageCode, defaultRegionCode,
                defaultCountryCode, 1, 1);

        // B1, C1, D1, E1(sub 언어 구글), B2, C2, D2, E2(sub 언어 구글 2)
        for (int i = 1; i <= 8; i++) {
            if (i % 4 != 0) {
                // B1, C1, D1, B2, C2, D2 (Default Language)
                mBottomNewsTopicList.add(newsContentProvider.getNewsTopic(defaultLanguageCode,
                        defaultRegionCode, defaultCountryCode, i % 4 + 1, i / 4 + 1));
            } else {
                // E1, E2 (Sub Language)
                if (defaultLanguageCode.equals("zh") && defaultCountryCode.equals("CN")) {
                    mBottomNewsTopicList.add(newsContentProvider.getNewsTopic(
                            subLanguageCode, subRegionCode, subCountryCode, 2, i / 4));
                } else {
                    mBottomNewsTopicList.add(newsContentProvider.getNewsTopic(
                            subLanguageCode, subRegionCode, subCountryCode, 1, i / 4));
                }
            }
        }
    }
}
