package com.yooiistudios.news.model.news.task;

import android.os.AsyncTask;
import android.util.Pair;
import android.util.SparseArray;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.util.NLLog;

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
//    private ArrayList<Pair<News, Boolean>> mNewsToFetchImageList;
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
        mBottomNewsFeedNewsToImageTaskMap = new HashMap<News, BottomNewsImageFetchTask>();
        mNewsToFetchMap = new HashMap<News, Pair<Boolean, Integer>>();
    }

    public void fetchAllDisplayingNewsImageList(ImageLoader imageLoader,
                                                ArrayList<NewsFeed> newsFeedList,
                                                OnFetchListener listener, int taskType) {
        SparseArray<NewsFeed> newsFeedToIndexSparseArray = new SparseArray<NewsFeed>();
        for (int i = 0; i < newsFeedList.size(); i++) {
            newsFeedToIndexSparseArray.put(i, newsFeedList.get(i));
        }

        fetch(imageLoader, newsFeedToIndexSparseArray, listener, taskType, false);
    }

    public void fetchDisplayingNewsImageList(ImageLoader imageLoader,
                                             SparseArray<NewsFeed> newsFeedMap,
                                             OnFetchListener listener, int taskType) {
        fetch(imageLoader, newsFeedMap, listener, taskType, false);
    }

    public void fetchDisplayingNewsImage(ImageLoader imageLoader, NewsFeed newsFeed,
                                         OnFetchListener listener, int newsFeedIndex,
                                         int taskType) {

        SparseArray<NewsFeed> list = new SparseArray<NewsFeed>();
        list.put(newsFeedIndex, newsFeed);

        fetch(imageLoader, list, listener, taskType, false);
    }

    public void fetchAllNextNewsImageList(ImageLoader imageLoader, ArrayList<NewsFeed> newsFeedList,
                                          OnFetchListener listener, int taskType) {
        SparseArray<NewsFeed> newsFeedToIndexSparseArray = new SparseArray<NewsFeed>();
        for (int i = 0; i < newsFeedList.size(); i++) {
            newsFeedToIndexSparseArray.put(i, newsFeedList.get(i));
        }
        fetch(imageLoader, newsFeedToIndexSparseArray, listener, taskType, true);
    }

    public void fetchNextNewsImage(ImageLoader imageLoader, NewsFeed newsFeed,
                                   OnFetchListener listener, int newsFeedIndex, int taskType) {

        SparseArray<NewsFeed> list = new SparseArray<NewsFeed>();
        list.put(newsFeedIndex, newsFeed);

        fetch(imageLoader, list, listener, taskType, true);
    }

    private void fetch(final ImageLoader imageLoader, SparseArray<NewsFeed> newsFeedMap,
                       OnFetchListener listener, int taskType, boolean fetchNextNewsImage) {
        //newsFeedMap의 key는 news feed의 인덱스이어야 한다.


        cancelBottomNewsImageUrlFetchTask();
        mListener = listener;
        mTaskType = taskType;

        int newsFeedCount = newsFeedMap.size();

        for (int i = 0; i < newsFeedCount; i++) {
            NewsFeed newsFeed = newsFeedMap.valueAt(i);

            ArrayList<News> newsList = newsFeed.getNewsList();

            int indexToFetch;
            if (fetchNextNewsImage) {
                indexToFetch = newsFeed.getDisplayingNewsIndex();
                if (indexToFetch < newsFeed.getNewsList().size() - 1) {
                    indexToFetch += 1;
                } else {
                    indexToFetch = 0;
                }
            } else {
                indexToFetch = newsFeed.getDisplayingNewsIndex();
            }

            News news = newsList.get(indexToFetch);

            mNewsToFetchMap.put(news, new Pair<Boolean, Integer>(false, newsFeedMap.keyAt(i)));
        }

        for (Map.Entry<News, Pair<Boolean, Integer>> entry : mNewsToFetchMap.entrySet()) {
            final News news = entry.getKey();
            final int newsFeedIndex = entry.getValue().second;
            if (!news.isImageUrlChecked()) {
                BottomNewsImageFetchTask task = new BottomNewsImageFetchTask(imageLoader,
                        news, newsFeedIndex, mTaskType, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                mBottomNewsFeedNewsToImageTaskMap.put(news, task);
            } else {
                if (news.getImageUrl() != null) {
                    NLLog.i("Image fetch", "imageLoader.get. newsFeedIndex : " + newsFeedIndex);
                    imageLoader.get(news.getImageUrl(), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response,
                                               boolean isImmediate) {
                            if (response.getBitmap() == null && isImmediate) {
                                return;
                            }
                            notifyOnImageFetch(news, newsFeedIndex, mTaskType);
                            NLLog.i("Image fetch", "onResponse. newsFeedIndex : " + newsFeedIndex);
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            notifyOnImageFetch(news, newsFeedIndex, mTaskType);
                            NLLog.i("Image fetch", "onErrorResponse. newsFeedIndex : " + newsFeedIndex);
                        }
                    });
                } else {
                    notifyOnImageFetch(news, newsFeedIndex, mTaskType);
                    NLLog.i("Image fetch", "no url. newsFeedIndex : " + newsFeedIndex);
                }
            }
        }
    }

    private void notifyOnImageFetch(News news, int position, int taskType) {
        if (mNewsToFetchMap.get(news) == null) {
            return;
        }
        if (mListener != null) {
            mListener.onBottomNewsImageFetch(position);
        }

        mNewsToFetchMap.put(
                news,
                new Pair<Boolean, Integer>(true, mNewsToFetchMap.get(news).second)
        );

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

    private void cancelBottomNewsImageUrlFetchTask() {
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

    @Override
    public void onBottomImageUrlFetchSuccess(final News news, String url, final int position
            , int taskType) {
        news.setImageUrlChecked(true);
        mBottomNewsFeedNewsToImageTaskMap.remove(news);

        if (mListener != null) {
            mListener.onBottomNewsImageUrlFetch(news, url, position, taskType);
        }
    }

    @Override
    public void onFetchImage(News news, int position, int taskType) {
        notifyOnImageFetch(news, position, taskType);
        NLLog.i("Image fetch", "onFetchImage. newsFeedIndex : " + position);
    }
}
