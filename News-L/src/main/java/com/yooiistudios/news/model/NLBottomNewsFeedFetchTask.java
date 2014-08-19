package com.yooiistudios.news.model;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 */
public class NLBottomNewsFeedFetchTask extends AsyncTask<Void, Void,
        NLNewsFeed> {

    private Context mContext;
    private NLNewsFeedUrl mNewsFeedUrl;
    private OnFetchListener mListener;

    public interface OnFetchListener {
        public void onBottomNewsFeedFetchSuccess(NLNewsFeedUrl url,
                                                 NLNewsFeed newsFeed);
        public void onBottomNewsFeedFetchFail();
    }

    public NLBottomNewsFeedFetchTask(Context context, NLNewsFeedUrl newsFeedUrl,
                                     OnFetchListener listener) {
        mContext = context;
        mNewsFeedUrl = newsFeedUrl;
        mListener = listener;
    }

    @Override
    protected NLNewsFeed doInBackground(Void... voids) {

        return NLNewsFeedFetchUtil.fetch(mContext, mNewsFeedUrl, 10);
    }

    @Override
    protected void onPostExecute(NLNewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (mListener != null) {
            if (newsFeed != null) {
                mListener.onBottomNewsFeedFetchSuccess(mNewsFeedUrl, newsFeed);
            } else {
                mListener.onBottomNewsFeedFetchFail();
            }
        }
    }
}
