package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsContent;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.task.NewsContentFetchTask;
import com.yooiistudios.newsflow.core.news.util.NewsIdGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 10.
 *
 * NewsContentFetchManager
 *  NewsContent 객체 fetch 를 관리하는 매니저 유틸
 */
public class NewsContentFetchManager implements NewsContentFetchTask.OnContentFetchListener {
    public interface OnFetchListener {
        public void onFetchTopNewsContent(News news, NewsContent newsContent, int newsPosition);
        public void onFetchBottomNewsContent(News news, NewsContent newsContent,
                                             int newsFeedPosition, int newsPosition);
    }

    private static final int TOP_FETCH_TASK_INDEX = 0;
    private static final int BOTTOM_FETCH_TASK_START_INDEX = 1;

    private static NewsContentFetchManager instance;

    private NewsContentFetchTask mTopNewsContentFetchTask;
    private Map<String, NewsContentFetchTask> mBottomNewsContentFetchTasks;
    private OnFetchListener mListener;

    private NewsContentFetchManager() {
        mBottomNewsContentFetchTasks = new HashMap<>();
    }

    public static NewsContentFetchManager getInstance() {
        if (instance == null) {
            synchronized (NewsContentFetchManager.class) {
                if (instance == null) {
                    instance = new NewsContentFetchManager();
                }
            }
        }
        return instance;
    }

    private void prepare(OnFetchListener listener) {
        cancelAllTasks();
        prepareVariables(listener);
    }

    private void cancelAllTasks() {
        if (mTopNewsContentFetchTask != null) {
            mTopNewsContentFetchTask.cancel(true);
        }

        for (String key : mBottomNewsContentFetchTasks.keySet()) {
            NewsContentFetchTask task = mBottomNewsContentFetchTasks.get(key);
            task.cancel(true);
        }
    }

    private void prepareVariables(OnFetchListener listener) {
        mBottomNewsContentFetchTasks.clear();
        mListener = listener;
    }

    public void fetch(NewsFeed topNewsFeed, ArrayList<NewsFeed> bottomNewsFeeds,
                      OnFetchListener listener) {
        prepare(listener);

        ArrayList<News> topNewsList = topNewsFeed.getNewsList();
        for (int i = 0; i < topNewsList.size(); i++) {
            mTopNewsContentFetchTask = new NewsContentFetchTask(topNewsList.get(i), this,
                    TOP_FETCH_TASK_INDEX, i);
            mTopNewsContentFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        for (int i = 0; i < bottomNewsFeeds.size(); i++) {
            NewsFeed newsFeed = bottomNewsFeeds.get(i);
            ArrayList<News> newsList = newsFeed.getNewsList();
            for (int j = 0; j < newsList.size(); j++) {
                News news = newsList.get(j);

                int newsFeedTaskId = BOTTOM_FETCH_TASK_START_INDEX + i;
                NewsContentFetchTask task = new NewsContentFetchTask(news, this, newsFeedTaskId, j);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                String uniqueId = NewsIdGenerator.generateKey(newsFeedTaskId, j);
                mBottomNewsContentFetchTasks.put(uniqueId, task);
            }
        }
    }

    @Override
    public void onContentFetch(News news, NewsContent newsContent, int newsFeedTaskId, int newsPosition) {
        if (newsFeedTaskId == TOP_FETCH_TASK_INDEX) {
            mTopNewsContentFetchTask = null;
            if (mListener != null) {
                mListener.onFetchTopNewsContent(news, newsContent, newsPosition);
            }
        } else {
            int newsFeedPosition = newsFeedTaskId - BOTTOM_FETCH_TASK_START_INDEX;
            String uniqueId = NewsIdGenerator.generateKey(newsFeedTaskId, newsPosition);
            mBottomNewsContentFetchTasks.remove(uniqueId);
            if (mListener != null) {
                mListener.onFetchBottomNewsContent(news, newsContent, newsFeedPosition, newsPosition);
            }
        }
    }
}
