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
    public interface OnFetchListener {
        public void onFetchImageUrl(News news, String url, int newsFeedPosition, int newsPosition);
    }

    private static NewsImageUrlFetchManager instance;

    private Map<String, NewsImageUrlFetchTask> mTasks;
//    private SparseArray<News> mNewsList;
    private OnFetchListener mListener;

    private NewsImageUrlFetchManager() {
        mTasks = new HashMap<>();
//        mNewsList = new SparseArray<>();
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

    public void fetch(ArrayList<NewsFeed> newsFeeds, OnFetchListener listener) {
        prepare(listener);

        for (int i = 0; i < newsFeeds.size(); i++) {
            NewsFeed newsFeed = newsFeeds.get(i);
            ArrayList<News> newsList = newsFeed.getNewsList();
            for (int j = 0; j < newsList.size(); j++) {
                News news = newsList.get(j);
                NewsImageUrlFetchTask task = new NewsImageUrlFetchTask(news, this, i, j);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                String uniqueId = NewsIdGenerator.generateKey(i, j);
                mTasks.put(uniqueId, task);
            }
        }
    }

    private void prepare(OnFetchListener listener) {
        cancelAllTasks();
        prepareVariables(listener);
    }

    private void cancelAllTasks() {
        for (String key : mTasks.keySet()) {
            NewsImageUrlFetchTask task = mTasks.get(key);
            task.cancel(true);
        }
    }

    private void prepareVariables(OnFetchListener listener) {
        mTasks.clear();
//        mNewsList.clear();
        mListener = listener;
    }

    @Override
    public void onImageUrlFetch(News news, String url, int newsFeedPosition, int newsPosition) {
        String uniqueId = NewsIdGenerator.generateKey(newsFeedPosition, newsPosition);
        mTasks.remove(uniqueId);
        if (mListener != null) {
            mListener.onFetchImageUrl(news, url, newsFeedPosition, newsPosition);
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

//    @Override
//    public void onFetch(NewsFeed newsFeed, int position) {
////        NLLog.now("onFetch : " + newsFeed.toString());
//        mNewsFeeds.put(position, newsFeed);
//        mTasks.remove(position);
//
//        if (mTasks.size() == 0) {
//            ArrayList<NewsFeed> newsFeeds = ArrayUtils.toArrayList(mNewsFeeds);
//            mListener.onFetchImageUrl(newsFeeds);
//        }
//    }
}
