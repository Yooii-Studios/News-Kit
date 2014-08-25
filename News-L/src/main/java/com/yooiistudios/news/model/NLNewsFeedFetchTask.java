package com.yooiistudios.news.model;

import android.content.Context;
import android.os.AsyncTask;

import com.yooiistudios.news.model.news.NLNewsFeed;
import com.yooiistudios.news.util.log.NLLog;

/**
 * Created by Dongheyon Jeong on in RSSTest from Yooii Studios Co., LTD. on 2014. 6. 27.
 *
 * MNRssFetchTask
 *  특정 url 에서 뉴스피드 데이터를 가져오는 클래스
 */
public class NLNewsFeedFetchTask extends AsyncTask<Void, Void, NLNewsFeed> {
    private static final String TAG = NLNewsFeedFetchTask.class.getName();
    private Context mContext;
    private NLNewsFeedUrl mFeedUrl;
    private OnFetchListener mOnFetchListener;
    private int mFetchLimit;

    /**
     *
     * @param fetchLimit Number of news to fetch. Should be > 0 and
     *                   < list.size()
     */
    public NLNewsFeedFetchTask(Context context, NLNewsFeedUrl feedUrl,
                               int fetchLimit,
                               OnFetchListener onFetchListener) {
        mContext = context;
        mFeedUrl = feedUrl;
        mFetchLimit = fetchLimit;
        mOnFetchListener = onFetchListener;
    }

    @Override
    protected NLNewsFeed doInBackground(Void... args) {

        return NLNewsFeedFetchUtil.fetch(mContext, mFeedUrl, mFetchLimit);
    }

    @Override
    protected void onPostExecute(NLNewsFeed rssFeed) {
        super.onPostExecute(rssFeed);
        if (isCancelled()) {
            if (mOnFetchListener != null) {
                mOnFetchListener.onCancel();
            }
            return;
        }

        if (rssFeed != null && rssFeed.getNewsList() != null) {
            NLLog.i(TAG, "Fetched news count : " + rssFeed.getNewsList().size());
            // success
            if (mOnFetchListener != null) {
                mOnFetchListener.onSuccess(rssFeed);
            }
        }
        else {
            // error
            if (mOnFetchListener != null) {
                mOnFetchListener.onError();
            }
        }
    }

    public interface OnFetchListener {
        public void onSuccess(NLNewsFeed rssFeed);
        public void onCancel();
        public void onError();
    }
}
