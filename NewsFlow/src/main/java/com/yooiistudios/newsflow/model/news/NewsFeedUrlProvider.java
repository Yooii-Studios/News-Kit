package com.yooiistudios.newsflow.model.news;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 25.
 *
 * NLNewsFeedPreset
 *  디폴드 뉴스 피드 제공자
 */
public class NewsFeedUrlProvider {

    private NewsTopic mTopNewsTopic;
    private ArrayList<NewsTopic> mBottomNewsTopicList;

    private static NewsFeedUrlProvider instance;

    public static NewsFeedUrlProvider getInstance(Context context) {
        if (instance == null) {
            instance = new NewsFeedUrlProvider(context);
        }

        return instance;
    }

    private NewsFeedUrlProvider(Context context) {
        mTopNewsTopic = getNewsTopic(context, "en", null, 4, 2);

        mBottomNewsTopicList = new ArrayList<>();
        mBottomNewsTopicList.add(getNewsTopic(context, "en", null, 3, 1));
        mBottomNewsTopicList.add(getNewsTopic(context, "en", null, 2, 1));
        mBottomNewsTopicList.add(getNewsTopic(context, "jp", null, 2, 1));
        mBottomNewsTopicList.add(getNewsTopic(context, "ko", null, 2, 2));
        mBottomNewsTopicList.add(getNewsTopic(context, "fr", null, 2, 1));
        mBottomNewsTopicList.add(getNewsTopic(context, "en", null, 3, 1));
//        mBottomNewsFeedUrlList.add(new NewsFeedUrl("http://estaticos.elmundo.es/elmundo/rss/portada.xml",
//                NewsFeedUrlType.GENERAL));
    }

    private static NewsTopic getNewsTopic(Context context,
                                          String languageCode, @Nullable String regionCode,
                                          int newsProviderId, int newsTopicId) {
        NewsTopic newsTopic = NewsContentProvider.getInstance(context).getNewsTopic(
                languageCode, regionCode, newsProviderId, newsTopicId);
        if (newsTopic == null) {
            NewsProvider newsProvider =
                    NewsContentProvider.getInstance(context).getNewsProvider(
                            languageCode, regionCode, newsProviderId
                    );

            newsTopic = newsProvider.getDefaultNewsTopic();
            if (newsTopic == null) {
                if (newsProvider.getNewsTopicList().size() == 0) {
                    throw new IllegalStateException("INVALID News provider resource!!!");
                }
                newsTopic = newsProvider.getNewsTopicList().get(0);
            }
        }
        return newsTopic;
    }

    public NewsTopic getTopNewsTopic() {
        return mTopNewsTopic;
    }
    public ArrayList<NewsTopic> getBottomNewsTopicList() {
        return mBottomNewsTopicList;
    }
//    public static int getSelectNewsFeedCountryCount() {
//        return sSelectNewsFeedList.size();
//    }
//    public static ArrayList<NewsFeedUrl> getSelectNewsFeedListAt(int index) {
//        return sSelectNewsFeedList.get(index);
//    }
//
//    private static ArrayList<NewsFeedUrl> getNewsFeedPresetList(NewsLanguage newsLanguage) {
//        ArrayList<NewsFeedUrl> countryNewsList = new ArrayList<NewsFeedUrl>();
//
//        for (NewsFeedUrl preset : NewsFeedUrl.values()) {
//            if (preset.getNewsLanguage().equals(newsLanguage)) {
//                countryNewsList.add(preset);
//            }
//        }
//
//        return countryNewsList;
//    }
}
