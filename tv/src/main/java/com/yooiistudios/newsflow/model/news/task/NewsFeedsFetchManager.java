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
    public interface OnFetchListener {
        public void onFetchAll(ArrayList<NewsFeed> newsFeeds);
    }
    private static NewsFeedsFetchManager instance;

    private SparseArray<NewsFeedFetchTask> mTasks;
//    private ArrayList<Pair<NewsFeed, Integer>> mNewsFeedToIndexPairList;
    private SparseArray<NewsFeed> mNewsFeeds;
    private OnFetchListener mListener;

    private NewsFeedsFetchManager() {
        mTasks = new SparseArray<>();
//        mNewsFeedToIndexPairList = new ArrayList<>();
        mNewsFeeds = new SparseArray<>();
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

    public <T extends RssFetchable> void fetch(ArrayList<T> rssFetchables, OnFetchListener listener) {
        prepare(listener);

        for (int i = 0; i < rssFetchables.size(); i++) {
            RssFetchable rssFetchable = rssFetchables.get(i);
            NewsFeedFetchTask task = new NewsFeedFetchTask(rssFetchable, this, i);
            mTasks.put(i, task);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void prepare(OnFetchListener listener) {
        cancelAllTasks();
        prepareVariables(listener);
    }

    private void cancelAllTasks() {
        int taskCount = mTasks.size();
        for (int i = 0; i < taskCount; i++) {
            int key = mTasks.keyAt(i);
            NewsFeedFetchTask task = mTasks.get(key);
            task.cancel(true);
        }
    }

    private void prepareVariables(OnFetchListener listener) {
        mTasks.clear();
//        mNewsFeedToIndexPairList.clear();
        mNewsFeeds.clear();
        mListener = listener;
    }

    @Override
    public void onFetch(NewsFeed newsFeed, int position) {
//        NLLog.now("onFetch : " + newsFeed.toString());
        mNewsFeeds.put(position, newsFeed);
        mTasks.remove(position);

        if (mTasks.size() == 0) {
            ArrayList<NewsFeed> newsFeeds = ArrayUtils.toArrayList(mNewsFeeds);
            mListener.onFetchAll(newsFeeds);
        }
    }
}
