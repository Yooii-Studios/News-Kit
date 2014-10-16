package com.yooiistudios.news.model.news.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import android.util.SparseArray;

import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedUrl;

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
        mBottomNewsFeedIndexToNewsFetchTaskMap = new SparseArray<BottomNewsFeedFetchTask>();
        mNewsFeedToIndexPairList = new ArrayList<Pair<NewsFeed, Integer>>();
    }

    public void fetchNewsFeed(
            Context context, NewsFeed newsFeed, int index,
            OnFetchListener listener, int taskType) {
        ArrayList<Pair<NewsFeed, Integer>> newsFeedToIndexPairList =
                new ArrayList<Pair<NewsFeed, Integer>>();

        newsFeedToIndexPairList.add(new Pair<NewsFeed, Integer>(newsFeed, index));

        fetch(context, newsFeedToIndexPairList, listener, taskType);
    }
    public void fetchNewsFeedList(
            Context context, ArrayList<NewsFeed> newsFeedList,
            OnFetchListener listener, int taskType) {
        int count = newsFeedList.size();
        ArrayList<Pair<NewsFeed, Integer>> newsFeedToIndexPairList =
                new ArrayList<Pair<NewsFeed, Integer>>();
        for (int i = 0; i < count; i++) {
            newsFeedToIndexPairList.add(new Pair<NewsFeed, Integer>(newsFeedList.get(i), i));
        }

        fetch(context, newsFeedToIndexPairList, listener, taskType);
    }
    public void fetchNewsFeedPairList(
            Context context, ArrayList<Pair<NewsFeed,Integer>> newsFeedToIndexPairList,
            OnFetchListener listener, int taskType) {
        fetch(context, newsFeedToIndexPairList, listener, taskType);
    }

    private void fetch(Context context, ArrayList<Pair<NewsFeed,Integer>> newsFeedToIndexPairList,
                       OnFetchListener listener, int taskType) {
        cancelBottomNewsFetchTasks();
        mListener = listener;
        mTaskType = taskType;

        final int newsFeedCount = newsFeedToIndexPairList.size();

        for (int i = 0; i < newsFeedCount; i++) {
            Pair<NewsFeed, Integer> element = newsFeedToIndexPairList.get(i);
            NewsFeed newsFeed = element.first;
            if (newsFeed == null) {
                continue;
            }

            NewsFeedUrl url = newsFeed.getNewsFeedUrl();
            BottomNewsFeedFetchTask task = new BottomNewsFeedFetchTask(
                    context, url, element.second, mTaskType, this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mBottomNewsFeedIndexToNewsFetchTaskMap.put(i, task);
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

        mListener = null;
        mTaskType = BottomNewsFeedFetchTask.TASK_INVALID;
    }

    @Override
    public void onBottomNewsFeedFetch(NewsFeed newsFeed, int position, int taskType) {
        mBottomNewsFeedIndexToNewsFetchTaskMap.remove(position);
        mNewsFeedToIndexPairList.add(new Pair<NewsFeed, Integer>(newsFeed, position));

        if (mListener != null && mTaskType != BottomNewsFeedFetchTask.TASK_INVALID) {
            mListener.onBottomNewsFeedFetch(newsFeed, position ,taskType);

            int remainingTaskCount = mBottomNewsFeedIndexToNewsFetchTaskMap.size();
            if (remainingTaskCount == 0) {
                mListener.onBottomNewsFeedListFetchDone(mNewsFeedToIndexPairList, mTaskType);
            }
        }
    }
}
