package com.yooiistudios.news.model.news;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 25.
 *
 * NLNewsFeedPreset
 *  디폴드 뉴스 피드 제공자
 */
public class NewsFeedPreset {

    public static final NewsFeedUrl sTopNewsFeesPreset;
    public static final ArrayList<NewsFeedUrl> sBottomNewsFeedPresetList;

    private NewsFeedPreset() {
        throw new AssertionError("You MUST not create this class!");
    }

    static {
        sTopNewsFeesPreset = new NewsFeedUrl("http://feeds2.feedburner" +
                ".com/time/topstories", NewsFeedUrlType.GENERAL);

        sBottomNewsFeedPresetList = new ArrayList<NewsFeedUrl>();
        sBottomNewsFeedPresetList.add(
                new NewsFeedUrl(
                        "http://rss.cnn.com/rss/edition.rss",
                        NewsFeedUrlType.GENERAL));
        sBottomNewsFeedPresetList.add(
                new NewsFeedUrl(
//                        "http://feeds.bbci.co.uk/news/rss.xml?edition=int",
                        "http://www.nytimes.com/services/xml/rss/nyt/InternationalHome.xml",
                        NewsFeedUrlType.GENERAL));
        sBottomNewsFeedPresetList.add(
                new NewsFeedUrl(
//                        "http://adv.yomiuri.co.jp/index.rss",
                        "http://rss.rssad.jp/rss/mainichi/flash.rss",
                        NewsFeedUrlType.GENERAL));
        sBottomNewsFeedPresetList.add(
                new NewsFeedUrl(
                        "http://www.cnet.com/rss/news/",
                        NewsFeedUrlType.GENERAL));
        sBottomNewsFeedPresetList.add(
                new NewsFeedUrl(
                        "http://rss.lemonde.fr/c/205/f/3050/index.rss",
                        NewsFeedUrlType.GENERAL));
        sBottomNewsFeedPresetList.add(
                new NewsFeedUrl(
//                        "http://myhome.chosun.com/rss/www_section_rss.xml",
//                        "http://rss.donga.com/total.xml",
//                        "http://www.thetimes.co.uk/tto/news/rss",
                        "http://estaticos.elmundo.es/elmundo/rss/portada.xml",
                        NewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://rss.cnn.com/rss/edition.rss",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://www.cnet.com/rss/news/",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://feeds.bbci.co.uk/news/rss.xml?edition=int",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://www.newyorker.com/feed/news",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://estaticos.elmundo.es/elmundo/rss/portada.xml",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://rss.lemonde.fr/c/205/f/3050/index.rss",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://rss.cnn.com/rss/edition.rss",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://www.cnet.com/rss/news/",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://feeds.bbci.co.uk/news/rss.xml?edition=int",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://www.newyorker.com/feed/news",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://estaticos.elmundo.es/elmundo/rss/portada.xml",
//                        NLNewsFeedUrlType.GENERAL));
//        sBottomNewsFeedPresetList.add(
//                new NLNewsFeedUrl(
//                        "http://rss.lemonde.fr/c/205/f/3050/index.rss",
//                        NLNewsFeedUrlType.GENERAL));
    }
}
