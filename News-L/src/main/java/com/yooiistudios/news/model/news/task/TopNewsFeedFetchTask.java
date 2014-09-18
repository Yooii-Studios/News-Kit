package com.yooiistudios.news.model.news.task;

import android.content.Context;
import android.os.AsyncTask;

import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedFetchUtil;
import com.yooiistudios.news.model.news.NewsFeedUrl;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLTopNewsFeedFetchTask
 *  메인 액티비티의 탑 뉴스를 가져오는 클래스
 */
public class TopNewsFeedFetchTask extends AsyncTask<Void, Void, NewsFeed> {

    public static final int FETCH_COUNT = 10;

    private Context mContext;
    private NewsFeedUrl mNewsFeedUrl;
    private OnFetchListener mListener;
    private boolean mShuffle;

    public interface OnFetchListener {
        public void onTopNewsFeedFetchSuccess(NewsFeed newsFeed);
        public void onTopNewsFeedFetchFail();
    }

    public TopNewsFeedFetchTask(Context context, NewsFeedUrl newsFeedUrl,
                                OnFetchListener listener) {
        this(context, newsFeedUrl, listener, true);
    }
    public TopNewsFeedFetchTask(Context context, NewsFeedUrl newsFeedUrl,
                                OnFetchListener listener, boolean shuffle) {
        mContext = context;
        mNewsFeedUrl = newsFeedUrl;
        mListener = listener;
        mShuffle = shuffle;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {

        return NewsFeedFetchUtil.fetch(mContext, mNewsFeedUrl, FETCH_COUNT, mShuffle);
    }

    @Override
    protected void onPostExecute(NewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (mListener != null) {
            if (newsFeed != null) {
                mListener.onTopNewsFeedFetchSuccess(newsFeed);
            } else {
                mListener.onTopNewsFeedFetchFail();
            }
        }
    }
}
