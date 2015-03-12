package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;
import android.util.SparseArray;

import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.RssFetchable;
import com.yooiistudios.newsflow.core.news.task.NewsFeedFetchTask;
import com.yooiistudios.newsflow.core.util.ArrayUtils;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 5.
 *
 * NewsFeedsFetchManager
 *  여러 뉴스 피드들을 fetch 할 경우 그 task 들을 관리함
 */
public class NewsFeedsFetchManager implements NewsFeedFetchTask.OnFetchListener {
    private static final int TOP_FETCH_TASK_INDEX = 0;
    private static final int BOTTOM_FETCH_TASK_START_INDEX = 1;
    public interface OnFetchListener {
        public void onFetchAllNewsFeeds(NewsFeed topNewsFeed, ArrayList<NewsFeed> bottomNewsFeeds);
    }
    private static NewsFeedsFetchManager instance;

    private NewsFeedFetchTask mTopNewsFeedFetchTask;
    private SparseArray<NewsFeedFetchTask> mBottomNewsFeedsFetchTasks;
    private NewsFeed mFetchedTopNewsFeed;
    private SparseArray<NewsFeed> mFetchedBottomNewsFeeds;
    private OnFetchListener mListener;

    private NewsFeedsFetchManager() {
        mBottomNewsFeedsFetchTasks = new SparseArray<>();
        mFetchedBottomNewsFeeds = new SparseArray<>();
    }

    public static NewsFeedsFetchManager getInstance() {
        if (instance == null) {
            synchronized (NewsFeedsFetchManager.class) {
                if (instance == null) {
                    instance = new NewsFeedsFetchManager();
                }
            }
        }
        return instance;
    }

    public <T extends RssFetchable> void fetch(RssFetchable topFetchable,
                                               ArrayList<T> bottomFetchables,
                                               OnFetchListener listener) {
        prepare(listener);

        fetchTopNewsFeed(topFetchable);
        fetchBottomNewsFeeds(bottomFetchables);
    }

    private void fetchTopNewsFeed(RssFetchable topFetchable) {
        mTopNewsFeedFetchTask = new NewsFeedFetchTask(topFetchable, this, TOP_FETCH_TASK_INDEX);
        mTopNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private <T extends RssFetchable> void fetchBottomNewsFeeds(ArrayList<T> bottomFetchables) {
        for (int i = 0; i < bottomFetchables.size(); i++) {
            RssFetchable rssFetchable = bottomFetchables.get(i);
            int taskId = i + BOTTOM_FETCH_TASK_START_INDEX;
            NewsFeedFetchTask task = new NewsFeedFetchTask(rssFetchable, this, taskId);
            mBottomNewsFeedsFetchTasks.put(taskId, task);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void prepare(OnFetchListener listener) {
        cancelAllTasks();
        prepareVariables(listener);
    }

    private void cancelAllTasks() {
        cancelTopFetchTask();
        cancelBottomFetchTasks();
    }

    private void cancelTopFetchTask() {
        if (mTopNewsFeedFetchTask != null) {
            mTopNewsFeedFetchTask.cancel(true);
        }
    }

    private void cancelBottomFetchTasks() {
        int bottomTaskCount = mBottomNewsFeedsFetchTasks.size();
        for (int i = 0; i < bottomTaskCount; i++) {
            int key = mBottomNewsFeedsFetchTasks.keyAt(i);
            NewsFeedFetchTask task = mBottomNewsFeedsFetchTasks.get(key);
            task.cancel(true);
        }
    }

    private void prepareVariables(OnFetchListener listener) {
        mBottomNewsFeedsFetchTasks.clear();
        mFetchedBottomNewsFeeds.clear();
        mListener = listener;
    }

    @Override
    public void onFetch(NewsFeed newsFeed, int position) {
        configOnFetch(newsFeed, position);

        if (allFetched()) {
            ArrayList<NewsFeed> bottomNewsFeeds = ArrayUtils.toArrayList(mFetchedBottomNewsFeeds);
            mListener.onFetchAllNewsFeeds(mFetchedTopNewsFeed, bottomNewsFeeds);
        }
    }

    private boolean allFetched() {
        return mTopNewsFeedFetchTask == null && mBottomNewsFeedsFetchTasks.size() == 0;
    }

    private void configOnFetch(NewsFeed newsFeed, int position) {
        if (position == TOP_FETCH_TASK_INDEX) {
            mFetchedTopNewsFeed = newsFeed;
            mTopNewsFeedFetchTask = null;
        } else {
            mFetchedBottomNewsFeeds.put(position, newsFeed);
            mBottomNewsFeedsFetchTasks.remove(position);
        }
    }
}