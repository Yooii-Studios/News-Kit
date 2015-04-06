package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;
import android.util.SparseArray;

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
    private OnFetchListener mListener;

    public interface OnFetchListener {
        void onBottomNewsImageUrlFetch(News news, String url, int newsFeedPosition, int newsPosition, int taskType);
        void onBottomNewsImageFetch(int newsFeedPosition, int newsPosition);
        void onBottomNewsImageListFetchDone(int taskType);
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

    public void fetchAllDisplayingNewsImageList(NewsImageLoader imageLoader,
                                                ArrayList<NewsFeed> newsFeedList,
                                                OnFetchListener listener, int taskType) {
        SparseArray<NewsFeed> newsFeedToIndexSparseArray = new SparseArray<>();
        for (int i = 0; i < newsFeedList.size(); i++) {
            newsFeedToIndexSparseArray.put(i, newsFeedList.get(i));
        }

        fetch(imageLoader, newsFeedToIndexSparseArray, listener, taskType, false);
    }

    public void fetchAllNextNewsImageList(NewsImageLoader imageLoader, ArrayList<NewsFeed> newsFeedList,
                                          OnFetchListener listener, int taskType) {
        SparseArray<NewsFeed> newsFeedToIndexSparseArray = new SparseArray<>();
        for (int i = 0; i < newsFeedList.size(); i++) {
            newsFeedToIndexSparseArray.put(i, newsFeedList.get(i));
        }
        fetch(imageLoader, newsFeedToIndexSparseArray, listener, taskType, true);
    }

    public void fetchNextNewsImage(NewsImageLoader imageLoader, NewsFeed newsFeed,
                                   OnFetchListener listener, int newsFeedIndex, int taskType) {

        SparseArray<NewsFeed> list = new SparseArray<>();
        list.put(newsFeedIndex, newsFeed);

        fetch(imageLoader, list, listener, taskType, true);
    }

    public void fetchDisplayingAndNextImage(NewsImageLoader imageLoader, NewsFeed newsFeed,
                                            OnFetchListener listener, int newsFeedIndex,
                                            int taskType) {
        prepare(listener, taskType);

        ArrayList<News> newsList;
        if (newsFeed == null || (newsList = newsFeed.getNewsList()).size() == 0) {
            return;
        }

        mNewsToFetchMap.put(newsFeed.getDisplayingNews(),
                new Request(newsFeedIndex, newsFeed.getDisplayingNewsIndex(), false));
        mNewsToFetchMap.put(newsList.get(newsFeed.getNextNewsIndex()),
                new Request(newsFeedIndex, newsFeed.getNextNewsIndex(), false));

        _fetch(imageLoader);
    }


    public void fetchDisplayingAndNextImageList(NewsImageLoader imageLoader,
                                                ArrayList<NewsFeed> newsFeedList,
                                                OnFetchListener listener, int taskType) {
        prepare(listener, taskType);

        ArrayList<News> newsList;

        for (int i = 0 ; i< newsFeedList.size(); i++) {
            NewsFeed newsFeed = newsFeedList.get(i);
            if (newsFeed == null || (newsList = newsFeed.getNewsList()).size() == 0) {
                continue;
            }

            mNewsToFetchMap.put(newsFeed.getDisplayingNews(),
                    new Request(i, newsFeed.getDisplayingNewsIndex(), false));
            mNewsToFetchMap.put(newsList.get(newsFeed.getNextNewsIndex()),
                    new Request(i, newsFeed.getNextNewsIndex(), false));
        }

        _fetch(imageLoader);
    }

    private void fetch(NewsImageLoader imageLoader, SparseArray<NewsFeed> newsFeedMap,
                       OnFetchListener listener, int taskType, boolean fetchNextNewsImage) {
        //newsFeedMap의 key는 news feed의 인덱스이어야 한다.
        prepare(listener, taskType);

        int newsFeedCount = newsFeedMap.size();
        for (int i = 0; i < newsFeedCount; i++) {
            NewsFeed newsFeed = newsFeedMap.valueAt(i);

            if (newsFeed == null) {
                continue;
            }
            ArrayList<News> newsList = newsFeed.getNewsList();

            if (newsList.size() == 0) {
                continue;
            }

            int indexToFetch;
            if (fetchNextNewsImage) {
                indexToFetch = newsFeed.getNextNewsIndex();
            } else {
                indexToFetch = newsFeed.getDisplayingNewsIndex();
            }

            News news = newsList.get(indexToFetch);

            mNewsToFetchMap.put(news, new Request(newsFeedMap.keyAt(i), indexToFetch, false));
        }

        _fetch(imageLoader);
    }

    private void prepare(OnFetchListener listener, int taskType) {
        cancelBottomNewsImageUrlFetchTask();
        mListener = listener;
        mTaskType = taskType;
    }

    private void _fetch(NewsImageLoader imageLoader) {
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
//        Pair<Boolean, Integer> newsFetchedToIndexPair;
        if ((prevRequest = mNewsToFetchMap.get(news)) == null ||
                prevRequest.handled) {
            return;
        }
        if (mListener != null) {
            mListener.onBottomNewsImageFetch(newsFeedPosition, newsPosition);
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
                mListener.onBottomNewsImageListFetchDone(taskType);
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
//        news.setImageUrlChecked(true);
        mBottomNewsFeedNewsToImageTaskMap.remove(news);

        if (mListener != null) {
            mListener.onBottomNewsImageUrlFetch(news, url, newsFeedPosition, newsPosition,
                    taskType);
        }
    }

    @Override
    public void onFetchImage(News news, int newsFeedPosition, int newsPosition, int taskType) {
        notifyOnImageFetch(news, newsFeedPosition, newsPosition, taskType);
//        NLLog.i("Image fetch", "onFetchImage. newsFeedIndex : " + position);
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
