package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;

import com.android.volley.VolleyError;
import com.yooiistudios.newsflow.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.model.cache.NewsImageLoader;
import com.yooiistudios.newsflow.model.cache.NewsUrlSupplier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 10. 16.
 *
 * BottomNewsImageFetchManager
 *
 */
public class BottomNewsImageFetchManager
        implements BottomNewsImageFetchTask.OnBottomImageUrlFetchListener {
    private static BottomNewsImageFetchManager instance;

    private HashMap<News, BottomNewsImageFetchTask> mBottomNewsFeedNewsToImageTaskMap;
    private HashMap<News, Request> mNewsToFetchMap;
    private int mTaskType;
    private int mWhichNews;
    private OnFetchListener mListener;

    public interface OnFetchListener {
        void onBottomNewsImageUrlFetch(News news, String url, int newsFeedPosition, int newsPosition,
                                       int taskType, int whichNews);
        void onBottomNewsImageFetch(int newsFeedPosition, int newsPosition, int whichNews);
        void onBottomNewsImageListFetchDone(int taskType, int whichNews);
    }

    public static BottomNewsImageFetchManager getInstance() {
        if (instance == null) {
            instance = new BottomNewsImageFetchManager();
        }

        return instance;
    }

    private BottomNewsImageFetchManager() {
        mBottomNewsFeedNewsToImageTaskMap = new HashMap<>();
        mNewsToFetchMap = new HashMap<>();
    }

    public void fetchDisplayingImage(NewsImageLoader imageLoader, NewsFeed newsFeed,
                                     OnFetchListener listener, int newsFeedIndex, int taskType) {
        prepare(listener, taskType, News.DISPLAYING_NEWS);
        addDisplayingNews(newsFeed, newsFeedIndex);
        fetch(imageLoader);
    }

    public void fetchDisplayingImages(NewsImageLoader imageLoader, ArrayList<NewsFeed> newsFeedList,
                                      OnFetchListener listener, int taskType) {
        prepare(listener, taskType, News.DISPLAYING_NEWS);
        for (int i = 0 ; i< newsFeedList.size(); i++) {
            NewsFeed newsFeed = newsFeedList.get(i);
            addDisplayingNews(newsFeed, i);
        }
        fetch(imageLoader);
    }

    public void fetchNextImages(NewsImageLoader imageLoader, ArrayList<NewsFeed> newsFeedList,
                                OnFetchListener listener, int taskType) {
        prepare(listener, taskType, News.NEXT_NEWS);
        for (int i = 0; i < newsFeedList.size(); i++) {
            NewsFeed newsFeed = newsFeedList.get(i);
            addNextNews(newsFeed, i);
        }
        fetch(imageLoader);
    }

    private void addDisplayingNews(NewsFeed newsFeed, int newsFeedIndex) {
        if (newsFeed.isDisplayable()) {
            mNewsToFetchMap.put(newsFeed.getDisplayingNews(),
                    new Request(newsFeedIndex, newsFeed.getDisplayingNewsIndex(), false));
        }
    }

    private void addNextNews(NewsFeed newsFeed, int newsFeedIndex) {
        if (newsFeed.isDisplayable()) {
            mNewsToFetchMap.put(newsFeed.getNextNews(),
                    new Request(newsFeedIndex, newsFeed.getNextNewsIndex(), false));
        }
    }

    private void prepare(OnFetchListener listener, int taskType, int whichNews) {
        cancelBottomNewsImageUrlFetchTask();
        mListener = listener;
        mTaskType = taskType;
        mWhichNews = whichNews;
    }

    private void fetch(NewsImageLoader imageLoader) {
        for (Map.Entry<News, Request> entry : mNewsToFetchMap.entrySet()) {
            final News news = entry.getKey();
            final Request request = entry.getValue();
            if (!news.isImageUrlChecked()) {
                BottomNewsImageFetchTask task = new BottomNewsImageFetchTask(imageLoader,
                        news, request.newsFeedPosition, request.newsPosition, mTaskType, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                mBottomNewsFeedNewsToImageTaskMap.put(news, task);
            } else {
                if (news.hasImageUrl()) {
                    imageLoader.get(new NewsUrlSupplier(news, request.newsFeedPosition),
                            new CacheImageLoader.ImageListener() {
                        @Override
                        public void onSuccess(CacheImageLoader.ImageResponse response) {
                            notifyOnImageFetch(news, request.newsFeedPosition, request.newsPosition, mTaskType);
                        }

                        @Override
                        public void onFail(VolleyError error) {
                            notifyOnImageFetch(news, request.newsFeedPosition, request.newsPosition, mTaskType);
                        }
                    });
                } else {
                    notifyOnImageFetch(news, request.newsFeedPosition, request.newsPosition, mTaskType);
                }
            }
        }
    }

    private void notifyOnImageFetch(News news, int newsFeedPosition, int newsPosition, int taskType) {
        Request prevRequest;
        if ((prevRequest = mNewsToFetchMap.get(news)) == null ||
                prevRequest.handled) {
            return;
        }
        if (mListener != null) {
            mListener.onBottomNewsImageFetch(newsFeedPosition, newsPosition, mWhichNews);
        }

        mNewsToFetchMap.put(news, new Request(newsFeedPosition, newsPosition, true));

        boolean allFetched = true;
        for (Map.Entry<News, Request> entry : mNewsToFetchMap.entrySet()) {
            if (!entry.getValue().handled) {
                allFetched = false;
                break;
            }
        }

        if (allFetched) {
            if (mListener != null) {
                mListener.onBottomNewsImageListFetchDone(taskType, mWhichNews);
            }
        }
    }

    public void cancelBottomNewsImageUrlFetchTask() {
        if (mBottomNewsFeedNewsToImageTaskMap != null) {
            for (Map.Entry<News, BottomNewsImageFetchTask> entry :
                    mBottomNewsFeedNewsToImageTaskMap.entrySet()) {
                BottomNewsImageFetchTask task = entry.getValue();
                if (task != null) {
                    task.cancel(true);
                }
            }
            mBottomNewsFeedNewsToImageTaskMap.clear();
        }
        if (mNewsToFetchMap != null) {
            mNewsToFetchMap.clear();
        }

        mListener = null;
        mTaskType = BottomNewsImageFetchTask.TASK_INVALID;
    }

    public void notifyOnImageFetchedManually(News news, String url, int newsFeedPosition, int newsPosition) {
        BottomNewsImageFetchTask task = mBottomNewsFeedNewsToImageTaskMap.get(news);
        if (task != null) {
            task.cancel(true);

            onBottomImageUrlFetchSuccess(news, url, newsFeedPosition, newsPosition, mTaskType);
        }
        notifyOnImageFetch(news, newsFeedPosition, newsPosition, mTaskType);
    }

    @Override
    public void onBottomImageUrlFetchSuccess(News news, String url,
                                             int newsFeedPosition, int newsPosition, int taskType) {
        mBottomNewsFeedNewsToImageTaskMap.remove(news);

        if (mListener != null) {
            mListener.onBottomNewsImageUrlFetch(news, url, newsFeedPosition, newsPosition,
                    taskType, mWhichNews);
        }
    }

    @Override
    public void onFetchImage(News news, int newsFeedPosition, int newsPosition, int taskType) {
        notifyOnImageFetch(news, newsFeedPosition, newsPosition, taskType);
    }

    private static class Request {
        public int newsFeedPosition;
        public int newsPosition;
        public boolean handled;

        public Request(int newsFeedPosition, int newsPosition, boolean handled) {
            this.newsFeedPosition = newsFeedPosition;
            this.newsPosition = newsPosition;
            this.handled = handled;
        }
    }
}
