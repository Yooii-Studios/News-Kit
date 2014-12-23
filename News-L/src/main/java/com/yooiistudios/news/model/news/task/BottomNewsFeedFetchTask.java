package com.yooiistudios.news.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedFetchUtil;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLBottomNewsFeedFetchTask
 *  메인 화면 하단 뉴스피드 로딩을 담당
 **/
public class BottomNewsFeedFetchTask extends AsyncTask<Void, Void,
        NewsFeed> {

    private NewsFeed mNewsFeed;
    private OnFetchListener mListener;
    private int mPosition;
    private boolean mShuffle;
    private int mTaskType;

    public static final int TASK_INVALID = -1;
    public static final int TASK_INITIALIZE = 0;
    public static final int TASK_REFRESH = 1;
    public static final int TASK_REPLACE = 2;
    public static final int TASK_CACHE = 3;
    public static final int TASK_MATRIX_CHANGED = 4;

    public interface OnFetchListener {
        public void onBottomNewsFeedFetch(NewsFeed newsFeed, int position, int taskType);
    }

    public BottomNewsFeedFetchTask(NewsFeed newsFeed,
                                   int position, int taskType, OnFetchListener listener) {
        this(newsFeed, position, taskType, listener, true);
    }
    public BottomNewsFeedFetchTask(NewsFeed newsFeed,
                                   int position, int taskType, OnFetchListener listener, boolean shuffle) {
        mNewsFeed = newsFeed;
        mPosition = position;
        mTaskType = taskType;
        mListener = listener;
        mShuffle = shuffle;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {
        NewsFeed newsFeed = NewsFeedFetchUtil.fetch(mNewsFeed.getNewsFeedUrl(), 10, mShuffle);
        if (newsFeed != null) {
            newsFeed.setTopicIdInfo(mNewsFeed);
        }

        return newsFeed;
    }

    @Override
    protected void onPostExecute(NewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (mListener != null) {
            mListener.onBottomNewsFeedFetch(newsFeed, mPosition, mTaskType);
        }
    }
}
