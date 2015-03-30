package com.yooiistudios.newsflow.core.news.util;

import android.util.Pair;

import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.NewsFeedFetchState;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * NewsFeedValidator
 *  뉴스피드가 valid(null 이 아닌지, news 를 갖고 있는지)한지 체크하는 유틸
 */
public class NewsFeedValidator {
    private NewsFeedValidator() {
        throw new AssertionError("You MUST NOT create the instance of "
                + NewsFeedValidator.class.getSimpleName() + "!!");
    }

    public static ArrayList<Pair<NewsFeed, Integer>> getInvalidNewsFeedsPairs(
            ArrayList<NewsFeed> newsFeeds) {
        ArrayList<Pair<NewsFeed, Integer>> newsFeedListToFetch = new ArrayList<>();
        int count = newsFeeds.size();
        for (int i = 0; i < count; i++) {
            NewsFeed newsFeed = newsFeeds.get(i);
            if (!newsFeed.isDisplayable()) {
                newsFeedListToFetch.add(new Pair<>(newsFeed, i));
            }
        }

        return newsFeedListToFetch;
    }

    public static boolean isDisplayable(ArrayList<NewsFeed> newsFeeds) {
        int count = newsFeeds.size();
        for (int i = 0; i < count; i++) {
            NewsFeed newsFeed = newsFeeds.get(i);
            if (!newsFeed.isDisplayable()) {
                return false;
            }
        }

        return true;
    }

    public static boolean isAllFetched(ArrayList<NewsFeed> newsFeeds) {
        int count = newsFeeds.size();
        for (int i = 0; i < count; i++) {
            NewsFeed newsFeed = newsFeeds.get(i);
            if (newsFeed.getNewsFeedFetchState().equals(NewsFeedFetchState.NOT_FETCHED_YET)) {
                return false;
            }
        }

        return true;
    }

//    public static boolean isDisplayable(NewsFeed newsFeed) {
//        return !isInvalid(newsFeed);
//    }
//
//    public static boolean isInvalid(NewsFeed newsFeed) {
//        return !newsFeed.isDisplayable();
////        return newsFeed == null || !newsFeed.containsNews();
//    }
}
