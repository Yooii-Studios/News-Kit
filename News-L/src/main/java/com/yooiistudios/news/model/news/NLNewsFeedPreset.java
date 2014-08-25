package com.yooiistudios.news.model.news;

import com.yooiistudios.news.model.NLNewsFeedUrl;
import com.yooiistudios.news.model.NLNewsFeedUrlType;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 25.
 *
 * NLNewsFeedPreset
 *  디폴드 뉴스 피드 제공자
 */
public class NLNewsFeedPreset {

    public static final NLNewsFeedUrl sTopNewsFeesPreset;
    public static final ArrayList<NLNewsFeedUrl> sBottomNewsFeedPresetList;

    private NLNewsFeedPreset() {
        throw new AssertionError("You MUST not create this class!");
    }

    static {
        sTopNewsFeesPreset = new NLNewsFeedUrl("http://feeds2.feedburner" +
                ".com/time/topstories", NLNewsFeedUrlType.GENERAL);

        sBottomNewsFeedPresetList = new ArrayList<NLNewsFeedUrl>();
        sBottomNewsFeedPresetList.add(
                new NLNewsFeedUrl(
                        "http://rss.cnn.com/rss/edition.rss",
                        NLNewsFeedUrlType.GENERAL));
        sBottomNewsFeedPresetList.add(
                new NLNewsFeedUrl(
//                        "http://myhome.chosun.com/rss/www_section_rss.xml",
                        "http://rss.donga.com/total.xml",
                        NLNewsFeedUrlType.GENERAL));
        sBottomNewsFeedPresetList.add(
                new NLNewsFeedUrl(
                        "http://rss.asahi.com/rss/asahi/newsheadlines.rdf",
                        NLNewsFeedUrlType.GENERAL));
        sBottomNewsFeedPresetList.add(
                new NLNewsFeedUrl(
                        "http://www.cnet.com/rss/news/",
                        NLNewsFeedUrlType.GENERAL));
        sBottomNewsFeedPresetList.add(
                new NLNewsFeedUrl(
                        "http://feeds.bbci.co.uk/news/rss.xml?edition=int",
                        NLNewsFeedUrlType.GENERAL));
        sBottomNewsFeedPresetList.add(
                new NLNewsFeedUrl(
                        "http://rss.lemonde.fr/c/205/f/3050/index.rss",
                        NLNewsFeedUrlType.GENERAL));
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
