package com.yooiistudios.news.model.main;

import android.content.Context;
import android.os.AsyncTask;

import com.yooiistudios.news.model.NLNewsFeed;
import com.yooiistudios.news.model.NLNewsFeedFetchUtil;
import com.yooiistudios.news.model.NLNewsFeedUrl;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLBottomNewsFeedFetchTask
 *  메인 화면 하단 뉴스피드 로딩을 담당
 **/
public class NLBottomNewsFeedFetchTask extends AsyncTask<Void, Void,
        NLNewsFeed> {

    private Context mContext;
    private NLNewsFeedUrl mNewsFeedUrl;
    private OnFetchListener mListener;
    private int mPosition;

    public interface OnFetchListener {
        public void onBottomNewsFeedFetchSuccess(int position,
                                                 NLNewsFeed newsFeed);
        public void onBottomNewsFeedFetchFail();
    }

    public NLBottomNewsFeedFetchTask(Context context, NLNewsFeedUrl newsFeedUrl,
                                     int position, OnFetchListener listener) {
        mContext = context;
        mNewsFeedUrl = newsFeedUrl;
        mPosition = position;
        mListener = listener;
    }

    @Override
    protected NLNewsFeed doInBackground(Void... voids) {

        return NLNewsFeedFetchUtil.fetch(mContext, mNewsFeedUrl, 15);
    }

    @Override
    protected void onPostExecute(NLNewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (mListener != null) {
            if (newsFeed != null) {
                mListener.onBottomNewsFeedFetchSuccess(mPosition, newsFeed);
            } else {
                mListener.onBottomNewsFeedFetchFail();
            }
        }
    }
}
