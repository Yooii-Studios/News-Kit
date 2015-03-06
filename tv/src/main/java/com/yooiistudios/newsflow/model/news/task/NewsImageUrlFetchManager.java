package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.task.NewsImageUrlFetchTask;
import com.yooiistudios.newsflow.core.util.IntegerMath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 5.
 *
 * NewsFeedsFetchManager
 *  여러 뉴스 피드들을 fetch 할 경우 그 task 들을 관리함
 */
public class NewsImageUrlFetchManager implements NewsImageUrlFetchTask.OnImageUrlFetchListener {
    private static final int TOP_FETCH_TASK_INDEX = 0;
    private static final int BOTTOM_FETCH_TASK_START_INDEX = 1;

    public interface OnFetchListener {
        public void onFetchTopNewsFeedImageUrl(News news, String url, int newsPosition);
        public void onFetchBottomNewsFeedImageUrl(News news, String url, int newsFeedPosition,
                                                  int newsPosition);
    }

    private static NewsImageUrlFetchManager instance;

    private NewsImageUrlFetchTask mTopNewsImageUrlFetchTask;
    private Map<String, NewsImageUrlFetchTask> mBottomNewsImageUrlFetchTasks;
    private OnFetchListener mListener;

    private NewsImageUrlFetchManager() {
        mBottomNewsImageUrlFetchTasks = new HashMap<>();
    }

    public static NewsImageUrlFetchManager getInstance() {
        if (instance == null) {
            synchronized (NewsImageUrlFetchManager.class) {
                if (instance == null) {
                    instance = new NewsImageUrlFetchManager();
                }
            }
        }
        return instance;
    }

    public void fetch(NewsFeed topNewsFeed, ArrayList<NewsFeed> bottomNewsFeeds,
                      OnFetchListener listener) {
        prepare(listener);

        ArrayList<News> topNewsList = topNewsFeed.getNewsList();
        for (int i = 0; i < topNewsList.size(); i++) {
            mTopNewsImageUrlFetchTask = new NewsImageUrlFetchTask(topNewsList.get(i), this,
                    TOP_FETCH_TASK_INDEX, i);
            mTopNewsImageUrlFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        for (int i = 0; i < bottomNewsFeeds.size(); i++) {
            NewsFeed newsFeed = bottomNewsFeeds.get(i);
            ArrayList<News> newsList = newsFeed.getNewsList();
            for (int j = 0; j < newsList.size(); j++) {
                News news = newsList.get(j);

                int newsFeedTaskId = BOTTOM_FETCH_TASK_START_INDEX + i;
                NewsImageUrlFetchTask task = new NewsImageUrlFetchTask(news, this, newsFeedTaskId, j);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                String uniqueId = NewsIdGenerator.generateKey(newsFeedTaskId, j);
                mBottomNewsImageUrlFetchTasks.put(uniqueId, task);
            }
        }
    }

    private void prepare(OnFetchListener listener) {
        cancelAllTasks();
        prepareVariables(listener);
    }

    private void cancelAllTasks() {
        if (mTopNewsImageUrlFetchTask != null) {
            mTopNewsImageUrlFetchTask.cancel(true);
        }

        for (String key : mBottomNewsImageUrlFetchTasks.keySet()) {
            NewsImageUrlFetchTask task = mBottomNewsImageUrlFetchTasks.get(key);
            task.cancel(true);
        }
    }

    private void prepareVariables(OnFetchListener listener) {
        mBottomNewsImageUrlFetchTasks.clear();
        mListener = listener;
    }

    @Override
    public void onImageUrlFetch(News news, String url, int newsFeedTaskId, int newsPosition) {
        if (newsFeedTaskId == TOP_FETCH_TASK_INDEX) {
            mTopNewsImageUrlFetchTask = null;
            if (mListener != null) {
                mListener.onFetchTopNewsFeedImageUrl(news, url, newsPosition);
            }
        } else {
            int newsFeedPosition = newsFeedTaskId - BOTTOM_FETCH_TASK_START_INDEX;
            String uniqueId = NewsIdGenerator.generateKey(newsFeedTaskId, newsPosition);
            mBottomNewsImageUrlFetchTasks.remove(uniqueId);
            if (mListener != null) {
                mListener.onFetchBottomNewsFeedImageUrl(news, url, newsFeedPosition, newsPosition);
            }
        }
    }

    private static class NewsIdGenerator {
        private static final int DIGIT_PER_ID = 8;
        // MAX_ID_VALUE : 255
        private static final int MAX_ID_VALUE = (int)Math.pow(2, DIGIT_PER_ID) - 1;
        private static final int MIN_ID_VALUE = 0;

        private static String generateKey(int newsFeedPosition, int newsPosition) {
            checkPositionRange(newsFeedPosition);
            checkPositionRange(newsPosition);

            String newsFeedId = IntegerMath.convertToBitPresentation(newsFeedPosition);
            String newsId = IntegerMath.convertToBitPresentation(newsPosition);

            return newsFeedId + newsId;
        }

        private static void checkPositionRange(int position){
            if (position < MIN_ID_VALUE || position > MAX_ID_VALUE) {
                throw new RuntimeException();
            }
        }
    }
}
