package com.yooiistudios.news.model.news;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 25.
 *
 * NLNewsFeedPreset
 *  디폴드 뉴스 피드 제공자
 */
public class MainNewsFeedUrlProvider {

    private NewsFeedUrl mTopNewsFeedUrl;
    private ArrayList<NewsFeedUrl> mBottomNewsFeedUrlList;

    private static MainNewsFeedUrlProvider instance;

    public static MainNewsFeedUrlProvider getInstance() {
        if (instance == null) {
            instance = new MainNewsFeedUrlProvider();
        }

        return instance;
    }

    private MainNewsFeedUrlProvider() {
        mTopNewsFeedUrl = new NewsFeedUrl("http://feeds2.feedburner.com/time/topstories",
                NewsFeedUrlType.GENERAL);

        mBottomNewsFeedUrlList = new ArrayList<NewsFeedUrl>();
        mBottomNewsFeedUrlList.add(new NewsFeedUrl("http://rss.cnn.com/rss/edition.rss",
                NewsFeedUrlType.GENERAL));
        mBottomNewsFeedUrlList.add(new NewsFeedUrl("http://www.nytimes.com/services/xml/rss/nyt/InternationalHome.xml",
                NewsFeedUrlType.GENERAL));
        mBottomNewsFeedUrlList.add(new NewsFeedUrl("http://rss.rssad.jp/rss/mainichi/flash.rss",
                NewsFeedUrlType.GENERAL));
        mBottomNewsFeedUrlList.add(new NewsFeedUrl("http://myhome.chosun.com/rss/www_section_rss.xml",
                NewsFeedUrlType.GENERAL));
        mBottomNewsFeedUrlList.add(new NewsFeedUrl("http://rss.lemonde.fr/c/205/f/3050/index.rss",
                NewsFeedUrlType.GENERAL));
        mBottomNewsFeedUrlList.add(new NewsFeedUrl("http://estaticos.elmundo.es/elmundo/rss/portada.xml",
                NewsFeedUrlType.GENERAL));
    }

    public NewsFeedUrl getTopNewsFeedUrl() {
        return mTopNewsFeedUrl;
    }
    public ArrayList<NewsFeedUrl> getBottomNewsFeedUrlList() {
        return mBottomNewsFeedUrlList;
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
