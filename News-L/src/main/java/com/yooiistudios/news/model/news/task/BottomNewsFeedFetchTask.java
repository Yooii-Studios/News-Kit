package com.yooiistudios.news.model.news.task;

import android.content.Context;
import android.os.AsyncTask;

import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedFetchUtil;
import com.yooiistudios.news.model.news.NewsFeedUrl;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLBottomNewsFeedFetchTask
 *  메인 화면 하단 뉴스피드 로딩을 담당
 **/
public class BottomNewsFeedFetchTask extends AsyncTask<Void, Void,
        NewsFeed> {

    private Context mContext;
    private NewsFeedUrl mNewsFeedUrl;
    private OnFetchListener mListener;
    private int mPosition;
    private boolean mShuffle;

    public interface OnFetchListener {
        public void onBottomNewsFeedFetchSuccess(int position,
                                                 NewsFeed newsFeed);
        public void onBottomNewsFeedFetchFail(int position);
    }

    public BottomNewsFeedFetchTask(Context context, NewsFeedUrl newsFeedUrl,
                                   int position, OnFetchListener listener) {
        this(context, newsFeedUrl, position, listener, true);
    }
    public BottomNewsFeedFetchTask(Context context, NewsFeedUrl newsFeedUrl,
                                   int position, OnFetchListener listener, boolean shuffle) {
        mContext = context;
        mNewsFeedUrl = newsFeedUrl;
        mPosition = position;
        mListener = listener;
        mShuffle = shuffle;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {

        return NewsFeedFetchUtil.fetch(mContext, mNewsFeedUrl, 15, mShuffle);
    }

    @Override
    protected void onPostExecute(NewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (mListener != null) {
            if (newsFeed != null) {
                mListener.onBottomNewsFeedFetchSuccess(mPosition, newsFeed);
            } else {
                mListener.onBottomNewsFeedFetchFail(mPosition);
            }
        }
    }
}
