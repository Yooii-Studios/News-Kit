package com.yooiistudios.news.model.news.task;

import android.os.AsyncTask;
import android.util.Pair;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;

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
        implements BottomNewsImageUrlFetchTask.OnBottomImageUrlFetchListener {

    private static BottomNewsImageFetchManager instance;

    private HashMap<News, BottomNewsImageUrlFetchTask> mBottomNewsFeedNewsToImageTaskMap;
    private ArrayList<Pair<News, Boolean>> mNewsToFetchImageList;
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
        mBottomNewsFeedNewsToImageTaskMap = new HashMap<News, BottomNewsImageUrlFetchTask>();
        mNewsToFetchImageList = new ArrayList<Pair<News, Boolean>>();
    }

    public void fetchDisplayingNewsImageList(ImageLoader imageLoader,
                                             ArrayList<NewsFeed> newsFeedList,
                                             OnFetchListener listener, int taskType) {
        fetch(imageLoader, newsFeedList, listener, taskType, false);
    }

    public void fetchDisplayingNewsImage(ImageLoader imageLoader, NewsFeed newsFeed,
                                         OnFetchListener listener, int taskType) {

        ArrayList<NewsFeed> list = new ArrayList<NewsFeed>();
        list.add(newsFeed);

        fetch(imageLoader, list, listener, taskType, false);
    }

    public void fetchNextNewsImageList(ImageLoader imageLoader, ArrayList<NewsFeed> newsFeedList,
                                       OnFetchListener listener, int taskType) {
        fetch(imageLoader, newsFeedList, listener, taskType, true);
    }

    private void fetch(final ImageLoader imageLoader, ArrayList<NewsFeed> newsFeedList,
                       OnFetchListener listener, int taskType, boolean fetchNextNewsImage) {
        cancelBottomNewsImageUrlFetchTask();
        mListener = listener;
        mTaskType = taskType;

        int newsFeedCount = newsFeedList.size();

        for (int i = 0; i < newsFeedCount; i++) {
            NewsFeed newsFeed = newsFeedList.get(i);

            if (newsFeed == null) {
                continue;
            }

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

            mNewsToFetchImageList.add(new Pair<News, Boolean>(news, false));
        }

        for (int i = 0; i < mNewsToFetchImageList.size(); i++) {
            final News news = mNewsToFetchImageList.get(i).first;
            if (!news.isImageUrlChecked()) {
                BottomNewsImageUrlFetchTask task = new BottomNewsImageUrlFetchTask(imageLoader,
                        news, i, mTaskType, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                mBottomNewsFeedNewsToImageTaskMap.put(news, task);
            } else {
                if (news.getImageUrl() != null) {
                    imageLoader.get(news.getImageUrl(), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response,
                                               boolean isImmediate) {
                            if (response.getBitmap() == null && isImmediate) {
                                return;
                            }
                            checkAllImageFetched(news, mTaskType);
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            checkAllImageFetched(news, mTaskType);
                        }
                    });
                } else {
                    checkAllImageFetched(news, mTaskType);
                }
            }
        }
    }

    private void checkAllImageFetched(News news, int taskType) {
        for (int i = 0; i < mNewsToFetchImageList.size(); i++) {
            Pair<News, Boolean> pair = mNewsToFetchImageList.get(i);
            if (pair.first == news) {
                mNewsToFetchImageList.set(i, new Pair<News, Boolean>(news, true));
                break;
            }
        }

        boolean allFetched = true;
        for (Pair<News, Boolean> pair : mNewsToFetchImageList) {
            if (!pair.second) {
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
            for (Map.Entry<News, BottomNewsImageUrlFetchTask> entry :
                    mBottomNewsFeedNewsToImageTaskMap.entrySet()) {
                BottomNewsImageUrlFetchTask task = entry.getValue();
                if (task != null) {
                    task.cancel(true);
                }
            }
            mBottomNewsFeedNewsToImageTaskMap.clear();
        }
        if (mNewsToFetchImageList != null) {
            mNewsToFetchImageList.clear();
        }

        mListener = null;
        mTaskType = BottomNewsImageUrlFetchTask.TASK_INVALID;
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
        checkAllImageFetched(news, taskType);

        if (mListener != null) {
            mListener.onBottomNewsImageFetch(position);
        }
    }
}
