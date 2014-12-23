package com.yooiistudios.news.model.news.task;

import android.content.Context;
import android.os.AsyncTask;

import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedFetchUtil;
import com.yooiistudios.news.model.news.NewsTopic;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NewsFeedDetailNewsFeedFetchTask
 *  뉴스 피드 디테일 액티비티의 뉴스를 교체하는 데에 사용됨
 */
public class NewsFeedDetailNewsFeedFetchTask extends AsyncTask<Void, Void, NewsFeed> {

    public static final int FETCH_COUNT = 10;

    private Context mContext;
    private NewsTopic mNewsTopic;
    private OnFetchListener mListener;
    private boolean mShuffle;

    public interface OnFetchListener {
        public void onNewsFeedFetchSuccess(NewsFeed newsFeed);
        public void onNewsFeedFetchFail();
    }

    public NewsFeedDetailNewsFeedFetchTask(Context context, NewsTopic newsTopic,
                                           OnFetchListener listener) {
        this(context, newsTopic, listener, true);
    }
    public NewsFeedDetailNewsFeedFetchTask(Context context, NewsTopic newsTopic,
                                           OnFetchListener listener, boolean shuffle) {
        mContext = context;
        mNewsTopic = newsTopic;
        mListener = listener;
        mShuffle = shuffle;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {
//        if (true) {
//            try {
//                Thread.sleep(1000 * 100);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
        NewsFeed newsFeed = NewsFeedFetchUtil.fetch(mContext, mNewsTopic.getNewsFeedUrl(),
                FETCH_COUNT, mShuffle);
        newsFeed.setTopicLanguageCode(mNewsTopic.getLanguageCode());
        newsFeed.setTopicRegionCode(mNewsTopic.getRegionCode());
        newsFeed.setTopicProviderId(mNewsTopic.getNewsProviderId());
        newsFeed.setTopicId(mNewsTopic.getId());


        return newsFeed;
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
