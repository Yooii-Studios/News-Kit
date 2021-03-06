package com.yooiistudios.newskit.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsFeedFetchState;
import com.yooiistudios.newskit.core.news.NewsFeedUrl;
import com.yooiistudios.newskit.core.news.NewsTopic;
import com.yooiistudios.newskit.core.news.RssFetchable;
import com.yooiistudios.newskit.core.news.util.NewsFeedFetchUtil;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NewsFeedDetailNewsFeedFetchTask
 *  뉴스 피드 디테일 액티비티의 뉴스를 교체하는 데에 사용됨
 */
public class NewsFeedDetailNewsFeedFetchTask extends AsyncTask<Void, Void, NewsFeed> {
    private RssFetchable mFetchable;
    private OnFetchListener mListener;
    private int mFetchCount;
    private boolean mShuffle;

    public interface OnFetchListener {
        void onNewsFeedFetchSuccess(NewsFeed newsFeed);
        void onNewsFeedFetchFail();
    }

    public NewsFeedDetailNewsFeedFetchTask(RssFetchable fetchable, OnFetchListener listener,
                                           int fetchCount, boolean shuffle) {
        mFetchable = fetchable;
        mListener = listener;
        mFetchCount = fetchCount;
        mShuffle = shuffle;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {
        NewsFeedUrl newsFeedUrl = mFetchable.getNewsFeedUrl();
        NewsFeed newsFeed = NewsFeedFetchUtil.fetch(newsFeedUrl, mFetchCount, mShuffle);

        if (mFetchable instanceof NewsTopic) {
            newsFeed.setTopicIdInfo(((NewsTopic) mFetchable));
        }

        return newsFeed;
    }

    @Override
    protected void onPostExecute(NewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (mListener != null) {
            if (newsFeed.containsNews() && newsFeed.getNewsFeedFetchState().equals(NewsFeedFetchState.SUCCESS)) {
                mListener.onNewsFeedFetchSuccess(newsFeed);
            } else {
                mListener.onNewsFeedFetchFail();
            }
        }
    }
}
