package com.yooiistudios.news.model.news.task;

import android.content.Context;
import android.os.AsyncTask;

import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedFetchUtil;
import com.yooiistudios.news.model.news.NewsFeedUrl;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NewsFeedDetailNewsFeedFetchTask
 *  뉴스 피드 디테일 액티비티의 뉴스를 교체하는 데에 사용됨
 */
public class NewsFeedDetailNewsFeedFetchTask extends AsyncTask<Void, Void, NewsFeed> {

    public static final int FETCH_COUNT = 10;

    private Context mContext;
    private NewsFeedUrl mNewsFeedUrl;
    private OnFetchListener mListener;

    public interface OnFetchListener {
        public void onNewsFeedFetchSuccess(NewsFeed newsFeed);
        public void onNewsFeedFetchFail();
    }

    public NewsFeedDetailNewsFeedFetchTask(Context context, NewsFeedUrl newsFeedUrl,
                                           OnFetchListener listener) {
        mContext = context;
        mNewsFeedUrl = newsFeedUrl;
        mListener = listener;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {
        // TODO test code for animation. Remove when done.
        try {
            Thread.sleep(2000);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return NewsFeedFetchUtil.fetch(mContext, mNewsFeedUrl, FETCH_COUNT);
    }

    @Override
    protected void onPostExecute(NewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (mListener != null) {
            if (newsFeed != null) {
                mListener.onNewsFeedFetchSuccess(newsFeed);
            } else {
                mListener.onNewsFeedFetchFail();
            }
        }
    }
}
