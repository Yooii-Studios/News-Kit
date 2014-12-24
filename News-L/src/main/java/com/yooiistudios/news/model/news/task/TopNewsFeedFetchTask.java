package com.yooiistudios.news.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedFetchUtil;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLTopNewsFeedFetchTask
 *  메인 액티비티의 탑 뉴스를 가져오는 클래스
 */
public class TopNewsFeedFetchTask extends AsyncTask<Void, Void, NewsFeed> {

    public enum TaskType { INITIALIZE, SWIPE_REFRESH, REPLACE, CACHE }

    public static final int FETCH_COUNT = 10;

    private NewsFeed mNewsFeed;
    private OnFetchListener mListener;
    private TaskType mTaskType;
    private boolean mShuffle;

//    public static final int TASK_INITIALIZE = 0;
//    public static final int TASK_SWIPE_REFRESH = 1;
//    public static final int TASK_REPLACE = 2;

    public interface OnFetchListener {
        public void onTopNewsFeedFetch(NewsFeed newsFeed, TaskType taskType);
    }

    public TopNewsFeedFetchTask(NewsFeed newsFeed,
                                OnFetchListener listener, TaskType taskType, boolean shuffle) {
        mNewsFeed = newsFeed;
        mListener = listener;
        mTaskType = taskType;
        mShuffle = shuffle;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {
        NewsFeed newsFeed =
                NewsFeedFetchUtil.fetch(mNewsFeed.getNewsFeedUrl(), FETCH_COUNT, mShuffle);
        newsFeed.setTopicIdInfo(mNewsFeed);
        return newsFeed;
    }

    @Override
    protected void onPostExecute(NewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (mListener != null) {
            mListener.onTopNewsFeedFetch(newsFeed, mTaskType);
        }
    }
}
