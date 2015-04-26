package com.yooiistudios.newskit.model.news.task;

import android.os.AsyncTask;
import android.util.Pair;
import android.util.SparseArray;

import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.RssFetchable;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 10. 16.
 *
 * BottomNewsFeedListFetchManager
 *  메인화면 하단 뉴스피드들을 불러오고 관리, 콜백을 부르는 매니저 클래스
 */
public class BottomNewsFeedListFetchManager
        implements BottomNewsFeedFetchTask.OnFetchListener {
    private static BottomNewsFeedListFetchManager instance;

    private SparseArray<BottomNewsFeedFetchTask> mBottomNewsFeedIndexToNewsFetchTaskMap;
    private ArrayList<Pair<NewsFeed, Integer>> mNewsFeedToIndexPairList;
    private OnFetchListener mListener;
    private int mTaskType;

    public interface OnFetchListener {
        public void onBottomNewsFeedFetch(NewsFeed newsFeed, int index, int taskType);
        public void onBottomNewsFeedListFetchDone(ArrayList<Pair<NewsFeed, Integer>> newsFeedList,
                                                  int taskType);
    }

    public static BottomNewsFeedListFetchManager getInstance() {
        if (instance == null) {
            instance = new BottomNewsFeedListFetchManager();
        }

        return instance;
    }

    private BottomNewsFeedListFetchManager() {
        mBottomNewsFeedIndexToNewsFetchTaskMap = new SparseArray<>();
        mNewsFeedToIndexPairList = new ArrayList<>();
    }

    public void fetchNewsFeed(NewsFeed newsFeed, int index,
                              OnFetchListener listener, int taskType) {
        ArrayList<Pair<NewsFeed, Integer>> newsFeedToIndexPairList =
                new ArrayList<>();

        newsFeedToIndexPairList.add(new Pair<>(newsFeed, index));

        fetchNewsFeeds(newsFeedToIndexPairList, listener, taskType);
    }


    public void fetchRssFetchables(RssFetchable RssFetchable, int index,
                                   OnFetchListener listener, int taskType) {
        ArrayList<Pair<RssFetchable, Integer>> rssFetchableToIndexPairList =
                new ArrayList<>();

        rssFetchableToIndexPairList.add(new Pair<>(RssFetchable, index));

        fetchNewsTopics(rssFetchableToIndexPairList, listener, taskType);
    }

    public void fetchNewsFeedList(ArrayList<NewsFeed> newsFeedList,
                                  OnFetchListener listener, int taskType) {
        int count = newsFeedList.size();
        ArrayList<Pair<NewsFeed, Integer>> newsFeedToIndexPairList =
                new ArrayList<>();
        for (int i = 0; i < count; i++) {
            newsFeedToIndexPairList.add(new Pair<>(newsFeedList.get(i), i));
        }

        fetchNewsFeeds(newsFeedToIndexPairList, listener, taskType);
    }

    public void fetchNewsFeedPairList(
            ArrayList<Pair<NewsFeed, Integer>> newsFeedToIndexPairList,
            OnFetchListener listener, int taskType) {
        fetchNewsFeeds(newsFeedToIndexPairList, listener, taskType);
    }

    private void fetchNewsFeeds(ArrayList<Pair<NewsFeed, Integer>> newsFeedToIndexPairList,
                                OnFetchListener listener, int taskType) {
        cancelBottomNewsFetchTasks();
        mListener = listener;
        mTaskType = taskType;

        for (Pair<NewsFeed, Integer> element : newsFeedToIndexPairList) {
            NewsFeed newsFeed = element.first;
            if (newsFeed == null) {
                continue;
            }

            BottomNewsFeedFetchTask task = new BottomNewsFeedFetchTask(
                    newsFeed, element.second, mTaskType, this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mBottomNewsFeedIndexToNewsFetchTaskMap.put(element.second, task);
        }
    }

    private void fetchNewsTopics(ArrayList<Pair<RssFetchable, Integer>> rssFetchableToIndexPairList,
                       OnFetchListener listener, int taskType) {
        cancelBottomNewsFetchTasks();
        mListener = listener;
        mTaskType = taskType;

        for (Pair<RssFetchable, Integer> element : rssFetchableToIndexPairList) {
            RssFetchable rssFetchable = element.first;
            if (rssFetchable == null) {
                continue;
            }

            BottomNewsFeedFetchTask task = new BottomNewsFeedFetchTask(
                    rssFetchable, element.second, mTaskType, this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mBottomNewsFeedIndexToNewsFetchTaskMap.put(element.second, task);
        }
    }

    private void cancelBottomNewsFetchTasks() {
        if (mBottomNewsFeedIndexToNewsFetchTaskMap != null) {
            int taskCount = mBottomNewsFeedIndexToNewsFetchTaskMap.size();
            for (int i = 0; i < taskCount; i++) {
                mBottomNewsFeedIndexToNewsFetchTaskMap.valueAt(i).cancel(true);
            }
            mBottomNewsFeedIndexToNewsFetchTaskMap.clear();
        }

        if (mNewsFeedToIndexPairList != null) {
            mNewsFeedToIndexPairList.clear();
        }

        // cancel loading image
        BottomNewsImageFetchManager.getInstance().cancelBottomNewsImageUrlFetchTask();

        mListener = null;
        mTaskType = BottomNewsFeedFetchTask.TASK_INVALID;
    }

    @Override
    public void onBottomNewsFeedFetch(NewsFeed newsFeed, int position, int taskType) {
        mBottomNewsFeedIndexToNewsFetchTaskMap.delete(position);
        mNewsFeedToIndexPairList.add(new Pair<>(newsFeed, position));

        if (mListener != null && mTaskType != BottomNewsFeedFetchTask.TASK_INVALID) {
            mListener.onBottomNewsFeedFetch(newsFeed, position ,taskType);

            int remainingTaskCount = mBottomNewsFeedIndexToNewsFetchTaskMap.size();
            if (remainingTaskCount == 0) {
                mListener.onBottomNewsFeedListFetchDone(mNewsFeedToIndexPairList, mTaskType);
            }
        }
    }
}
