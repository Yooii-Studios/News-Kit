package com.yooiistudios.news.model;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLTopNewsFeedFetchTask
 *  메인 액티비티의 탑 뉴스를 가져오는 클래스
 */
public class NLTopNewsFeedFetchTask extends AsyncTask<Void, Void, NLNewsFeed> {

    private Context mContext;
    private NLNewsFeedUrl mNewsFeedUrl;
    private OnFetchListener mListener;

    public interface OnFetchListener {
        public void onTopNewsFeedFetchSuccess(NLNewsFeed newsFeed);
        public void onTopNewsFeedFetchFail();
    }

    public NLTopNewsFeedFetchTask(Context context, NLNewsFeedUrl newsFeedUrl,
                                  OnFetchListener listener) {
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
                mListener.onTopNewsFeedFetchSuccess(newsFeed);
            } else {
                mListener.onTopNewsFeedFetchFail();
            }
        }
    }
}
