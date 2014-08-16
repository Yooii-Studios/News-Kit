package com.yooiistudios.news.feed;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yooiistudios.news.setting.language.NLLanguage;
import com.yooiistudios.news.setting.language.NLLanguageType;

import java.lang.reflect.Type;
import java.util.ArrayList;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;

/**
 * Created by Dongheyon Jeong on in morning-kit from Yooii Studios Co., LTD. on 2014. 7. 3.
 *
 * MNNewsFeedUtil
 *  뉴스피드에 관한 전반적인 유틸 클래스
 */
public class NLNewsFeedUtil {
    private static final String KEY_HISTORY = "KEY_HISTORY";
    private static final int MAX_HISTORY_SIZE = 10;
    public static final String PREF_NEWS_FEED = "PREF_NEWS_FEED";

    private static final String NEWS_PROVIDER_YAHOO_JAPAN = "Yahoo!ニュース";

    private NLNewsFeedUtil() { throw new AssertionError("You MUST not create this class!"); }

    public static NLNewsFeedUrl getDefaultFeedUrl(Context context) {
        NLLanguageType type = NLLanguage.getCurrentLanguageType(context);

        String feedUrl;
        NLNewsFeedUrlType urlType;

        switch(type) {
            case ENGLISH:
                feedUrl = "http://news.google.com/news?cf=all&ned=us&hl=en&output=rss";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
            case KOREAN:
                feedUrl = "http://news.google.com/news?cf=all&ned=kr&hl=ko&output=rss";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
            case JAPANESE:
                feedUrl = "http://rss.dailynews.yahoo.co.jp/fc/rss.xml";
                urlType = NLNewsFeedUrlType.YAHOO;
                break;
            case TRADITIONAL_CHINESE:
                feedUrl = "http://news.google.com/news?cf=all&ned=tw&hl=zh-TW&output=rss";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
            case SIMPLIFIED_CHINESE:
                feedUrl = "http://news.google.com/news?cf=all&ned=cn&hl=zh-CN&output=rss";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
            case RUSSIAN:
                feedUrl = "http://news.google.com/news?cf=all&ned=ru_ru&hl=ru&output=rss";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
            default:
                feedUrl = "";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
        }
//        feedUrl = "http://sweetpjy.tistory.com/rss";
//        feedUrl = "http://www.cnet.com/rss/iphone-update/";

        return new NLNewsFeedUrl(feedUrl, urlType);
    }

    public static String getRssFeedJsonString(RssFeed feed) {
        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy
                () {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return (clazz == RssItem.class);
                    }
                }).serializeNulls().create().toJson(feed);
    }
    public static String getRssItemArrayListString(ArrayList<RssItem>
                                                           itemList) {
        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy
                () {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return (clazz == RssFeed.class);
            }
        }).serializeNulls().create().toJson(itemList);
    }

    public static void addUrlToHistory(Context context, String url) {
        ArrayList<String> urlList = getUrlHistory(context);

        // if list contains url, remove and add it at 0th index.
        if (urlList.contains(url)) {
            urlList.remove(url);
        }
        // put recent url at 0th index.
        urlList.add(0, url);

        // remove last history if no room.
        if (urlList.size() > MAX_HISTORY_SIZE) {
            urlList.remove(urlList.size()-1);
        }

        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NEWS_FEED, Context.MODE_PRIVATE);

        prefs.edit().putString(KEY_HISTORY, new Gson().toJson(urlList)).apply();
    }

    public static ArrayList<String> getUrlHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NEWS_FEED, Context.MODE_PRIVATE);
        String historyJsonStr = prefs.getString(KEY_HISTORY, null);

        if (historyJsonStr != null) {
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            return new Gson().fromJson(historyJsonStr, type);
        }
        else {
            return new ArrayList<String>();
        }
    }

    /**
     *
     * @param news RssItem
     * @param type NLNewsFeedUrlType
     * @return retval
     * retval[0] : title.
     * retval[1] : publisher or null if there's no publisher info.
     *
     */
    public static String[] getTitleAndPublisherName(RssItem news,
                                          NLNewsFeedUrlType type) {
        String title = news.getTitle();
        String newTitle;
        String publisher;
        switch (type) {
            case GOOGLE:
                final String delim = " - ";
                int idx = title.lastIndexOf(delim);

                int titleStartIdx = 0;
                int titleEndIdx = idx;
                int pubStartIdx = idx+delim.length();
                int pubEndIdx = title.length();

                if (idx >= 0 &&
                        titleEndIdx >= titleStartIdx &&
                        pubEndIdx >= pubStartIdx) {
                // title.length() >= delim.length()
                    newTitle = title.substring(titleStartIdx, titleEndIdx);
                    publisher = "- " + title.substring(pubStartIdx, pubEndIdx);
                }
                else {
                    newTitle = title;
                    publisher = null;
                }

                break;
            case YAHOO:
                newTitle = title;
                publisher = NEWS_PROVIDER_YAHOO_JAPAN;
                break;
            case CUSTOM:
            default:
                newTitle = title;
                publisher = null;
                break;
        }

        return new String[]{newTitle, publisher};
    }

    public static String getFeedTitle(Context context) {
        NLLanguageType currentLanguageType = NLLanguage.getCurrentLanguageType(context);

        String provider;

        if (currentLanguageType.equals(NLLanguageType.JAPANESE)) {
            provider = NEWS_PROVIDER_YAHOO_JAPAN;
        }
        else {
            provider = null;
        }

        return provider;
    }
}
