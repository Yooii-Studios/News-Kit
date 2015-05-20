package com.yooiistudios.newskit.model.news;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in morning-kit from Yooii Studios Co., LTD. on 2014. 7. 3.
 *
 * MNNewsFeedUtil
 *  뉴스피드에 관한 전반적인 유틸 클래스
 */
public class CustomRssHistoryUtils {
    private static final String KEY_HISTORY = "key_history";
    private static final int MAX_HISTORY_SIZE = 10;
    public static final String PREF_NEWS_FEED = "pref_news_feed";

//    private static final String NEWS_PROVIDER_YAHOO_JAPAN = "Yahoo!ニュース";

    private CustomRssHistoryUtils() { throw new AssertionError("You MUST not create this class!"); }


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
            urlList.remove(urlList.size() - 1);
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NEWS_FEED, Context.MODE_PRIVATE);

        prefs.edit().putString(KEY_HISTORY, new Gson().toJson(urlList)).apply();
    }

    public static ArrayList<String> getUrlHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NEWS_FEED, Context.MODE_PRIVATE);
        String historyJsonStr = prefs.getString(KEY_HISTORY, null);

        if (historyJsonStr != null) {
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            return new Gson().fromJson(historyJsonStr, type);
        } else {
            return new ArrayList<>();
        }
    }

    public static void removeUrlAtIndex(Context context, int position) {
        ArrayList<String> urlList = getUrlHistory(context);

        if (urlList.size() > position) {
            urlList.remove(position);
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NEWS_FEED, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_HISTORY, new Gson().toJson(urlList)).apply();
    }
}
