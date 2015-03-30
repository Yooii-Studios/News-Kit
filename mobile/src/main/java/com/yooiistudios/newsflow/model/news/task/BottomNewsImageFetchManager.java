package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;
import android.util.Pair;
import android.util.SparseArray;

import com.android.volley.VolleyError;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.model.ResizedImageLoader;

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
    private HashMap<News, Pair<Boolean, Integer>> mNewsToFetchMap;
    private int mTaskType;
    private OnFetchListener mListener;

    public interface OnFetchListener {
        public void onBottomNewsImageUrlFetch(News news, String url, int index, int taskType);
        public void onBottomNewsImageFetch(int position);
        public void onBottomNewsImageListFetchDone(int taskType);
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

    public void fetchAllDisplayingNewsImageList(ResizedImageLoader imageLoader,
                                                ArrayList<NewsFeed> newsFeedList,
                                                OnFetchListener listener, int taskType) {
        SparseArray<NewsFeed> newsFeedToIndexSparseArray = new SparseArray<>();
        for (int i = 0; i < newsFeedList.size(); i++) {
            newsFeedToIndexSparseArray.put(i, newsFeedList.get(i));
        }

        fetch(imageLoader, newsFeedToIndexSparseArray, listener, taskType, false);
    }

    public void fetchAllNextNewsImageList(ResizedImageLoader imageLoader, ArrayList<NewsFeed> newsFeedList,
                                          OnFetchListener listener, int taskType) {
        SparseArray<NewsFeed> newsFeedToIndexSparseArray = new SparseArray<>();
        for (int i = 0; i < newsFeedList.size(); i++) {
            newsFeedToIndexSparseArray.put(i, newsFeedList.get(i));
        }
        fetch(imageLoader, newsFeedToIndexSparseArray, listener, taskType, true);
    }

    public void fetchNextNewsImage(ResizedImageLoader imageLoader, NewsFeed newsFeed,
                                   OnFetchListener listener, int newsFeedIndex, int taskType) {

        SparseArray<NewsFeed> list = new SparseArray<>();
        list.put(newsFeedIndex, newsFeed);

        fetch(imageLoader, list, listener, taskType, true);
    }

    public void fetchDisplayingAndNextImage(ResizedImageLoader imageLoader, NewsFeed newsFeed,
                                            OnFetchListener listener, int newsFeedIndex,
                                            int taskType) {
        prepare(listener, taskType);

        ArrayList<News> newsList;
        if (newsFeed == null || (newsList = newsFeed.getNewsList()).size() == 0) {
            return;
        }

        mNewsToFetchMap.put(newsList.get(newsFeed.getDisplayingNewsIndex()),
                new Pair<>(false, newsFeedIndex));
        mNewsToFetchMap.put(newsList.get(newsFeed.getNextNewsIndex()),
                new Pair<>(false, newsFeedIndex));

        _fetch(imageLoader);
    }


    public void fetchDisplayingAndNextImageList(ResizedImageLoader imageLoader,
                                                ArrayList<NewsFeed> newsFeedList,
                                                OnFetchListener listener, int taskType) {
        prepare(listener, taskType);

        ArrayList<News> newsList;

        for (int i = 0 ; i< newsFeedList.size(); i++) {
            NewsFeed newsFeed = newsFeedList.get(i);
            if (newsFeed == null || (newsList = newsFeed.getNewsList()).size() == 0) {
                continue;
            }

            mNewsToFetchMap.put(newsList.get(newsFeed.getDisplayingNewsIndex()), new Pair<>(false, i));
            mNewsToFetchMap.put(newsList.get(newsFeed.getNextNewsIndex()), new Pair<>(false, i));
        }

        _fetch(imageLoader);
    }

    private void fetch(ResizedImageLoader imageLoader, SparseArray<NewsFeed> newsFeedMap,
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

            mNewsToFetchMap.put(news, new Pair<>(false, newsFeedMap.keyAt(i)));
        }

        _fetch(imageLoader);
    }

    private void prepare(OnFetchListener listener, int taskType) {
        cancelBottomNewsImageUrlFetchTask();
        mListener = listener;
        mTaskType = taskType;
    }

    private void _fetch(ResizedImageLoader imageLoader) {
        for (Map.Entry<News, Pair<Boolean, Integer>> entry : mNewsToFetchMap.entrySet()) {
            final News news = entry.getKey();
            final int newsFeedIndex = entry.getValue().second;
            if (!news.isImageUrlChecked()) {
                BottomNewsImageFetchTask task = new BottomNewsImageFetchTask(imageLoader,
                        news, newsFeedIndex, mTaskType, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                mBottomNewsFeedNewsToImageTaskMap.put(news, task);
            } else {
                if (news.hasImageUrl()) {
                    imageLoader.get(news.getImageUrl(), new ResizedImageLoader.ImageListener() {
                        @Override
                        public void onSuccess(ResizedImageLoader.ImageResponse response) {
                            notifyOnImageFetch(news, newsFeedIndex, mTaskType);
                        }

                        @Override
                        public void onFail(VolleyError error) {
                            notifyOnImageFetch(news, newsFeedIndex, mTaskType);
                        }
                    });
                } else {
                    notifyOnImageFetch(news, newsFeedIndex, mTaskType);
                }
            }
        }
    }

    private void notifyOnImageFetch(News news, int position, int taskType) {
        Pair<Boolean, Integer> newsFetchedToIndexPair;
        if ((newsFetchedToIndexPair = mNewsToFetchMap.get(news)) == null ||
                newsFetchedToIndexPair.first) {
            return;
        }
        if (mListener != null) {
            mListener.onBottomNewsImageFetch(position);
        }

        mNewsToFetchMap.put(news, new Pair<>(true, newsFetchedToIndexPair.second));

        boolean allFetched = true;
        for (Map.Entry<News, Pair<Boolean, Integer>> entry : mNewsToFetchMap.entrySet()) {
            if (!entry.getValue().first) {
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

    public void notifyOnImageFetchedManually(News news, String url, int position) {
        BottomNewsImageFetchTask task = mBottomNewsFeedNewsToImageTaskMap.get(news);
        if (task != null) {
            task.cancel(true);

            onBottomImageUrlFetchSuccess(news, url, position, mTaskType);
        }
        notifyOnImageFetch(news, position, mTaskType);
    }

    @Override
    public void onBottomImageUrlFetchSuccess(final News news, String url, final int position
            , int taskType) {
//        news.setImageUrlChecked(true);
        mBottomNewsFeedNewsToImageTaskMap.remove(news);

        if (mListener != null) {
            mListener.onBottomNewsImageUrlFetch(news, url, position, taskType);
        }
    }

    @Override
    public void onFetchImage(News news, int position, int taskType) {
        notifyOnImageFetch(news, position, taskType);
//        NLLog.i("Image fetch", "onFetchImage. newsFeedIndex : " + position);
    }
}
