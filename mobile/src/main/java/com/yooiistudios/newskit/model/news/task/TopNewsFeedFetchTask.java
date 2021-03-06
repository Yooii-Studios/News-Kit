package com.yooiistudios.newskit.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsTopic;
import com.yooiistudios.newskit.core.news.RssFetchable;
import com.yooiistudios.newskit.core.news.util.NewsFeedFetchUtil;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLTopNewsFeedFetchTask
 *  메인 액티비티의 탑 뉴스를 가져오는 클래스
 */
public class TopNewsFeedFetchTask extends AsyncTask<Void, Void, NewsFeed> {
    public enum TaskType { INITIALIZE, SWIPE_REFRESH, REPLACE, CACHE }

    private RssFetchable mRssFetchable;
    private NewsFeed mNewsFeed;
    private OnFetchListener mListener;
    private TaskType mTaskType;
    private boolean mShuffle;

    public interface OnFetchListener {
        void onTopNewsFeedFetch(NewsFeed newsFeed, TaskType taskType);
    }

    public TopNewsFeedFetchTask(RssFetchable rssFetchable,
                                OnFetchListener listener, TaskType taskType, boolean shuffle) {
        mRssFetchable = rssFetchable;
        mListener = listener;
        mTaskType = taskType;
        mShuffle = shuffle;
    }

    public TopNewsFeedFetchTask(NewsFeed newsFeed,
                                OnFetchListener listener, TaskType taskType, boolean shuffle) {
        this(newsFeed.getNewsFeedUrl(), listener, taskType, shuffle);
        mNewsFeed = newsFeed;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {
        NewsFeed newsFeed =
                NewsFeedFetchUtil.fetch(mRssFetchable, NewsFeedFetchUtil.FETCH_LIMIT_TOP, mShuffle);
        if (mNewsFeed != null) {
            newsFeed.setTopicIdInfo(mNewsFeed);
        } else if (mRssFetchable instanceof NewsTopic) {
            newsFeed.setTopicIdInfo((NewsTopic)mRssFetchable);
        }

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
