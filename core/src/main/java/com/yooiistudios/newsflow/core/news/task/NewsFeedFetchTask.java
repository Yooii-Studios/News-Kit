package com.yooiistudios.newsflow.core.news.task;

import android.os.AsyncTask;

import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.NewsTopic;
import com.yooiistudios.newsflow.core.news.RssFetchable;
import com.yooiistudios.newsflow.core.news.util.NewsFeedFetchUtil;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 5.
 *
 * NewsFeedFetchTask
 *  URL 에서 뉴스피드를 가져와 파싱, 자료구조로 저장.
 */
public class NewsFeedFetchTask extends AsyncTask<Void, Void, NewsFeed> {
    private RssFetchable mRssFetchable;
    private OnFetchListener mListener;
    private int mPosition;
    private boolean mShuffle;

    public interface OnFetchListener {
        public void onFetch(NewsFeed newsFeed, int position);
    }

    public NewsFeedFetchTask(RssFetchable rssFetchable, OnFetchListener listener, int position) {
        mRssFetchable = rssFetchable;
        mListener = listener;
        mPosition = position;
    }

//    public NewsFeedFetchTask(RssFetchable rssFetchable, OnFetchListener listener,
//                             boolean shuffle, int fetchLimit) {
//        this(rssFetchable, listener);
//        mShuffle = shuffle;
//        mFetchLimit = fetchLimit;
//    }
//
//    public NewsFeedFetchTask(NewsFeed newsFeed, OnFetchListener listener,
//                             boolean shuffle, int fetchLimit) {
//        this(newsFeed.getNewsFeedUrl(), listener, shuffle, fetchLimit);
//        mNewsFeed = newsFeed;
//    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {
        NewsFeed newsFeed =
                NewsFeedFetchUtil.fetch(mRssFetchable, NewsFeedFetchUtil.FETCH_LIMIT_TV, mShuffle);
        if (mRssFetchable instanceof NewsFeed) {
            newsFeed.setTopicIdInfo((NewsFeed)mRssFetchable);
        } else if (mRssFetchable instanceof NewsTopic) {
            newsFeed.setTopicIdInfo((NewsTopic)mRssFetchable);
        }

        return newsFeed;
    }

    @Override
    protected void onPostExecute(NewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (isCancelled()) {
            return;
        }
        if (mListener != null) {
            mListener.onFetch(newsFeed, mPosition);
        }
    }
}
