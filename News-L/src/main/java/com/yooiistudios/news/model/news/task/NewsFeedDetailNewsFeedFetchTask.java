package com.yooiistudios.news.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedFetchState;
import com.yooiistudios.news.model.news.util.NewsFeedFetchUtil;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.model.news.NewsTopic;
import com.yooiistudios.news.util.RssFetchable;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NewsFeedDetailNewsFeedFetchTask
 *  뉴스 피드 디테일 액티비티의 뉴스를 교체하는 데에 사용됨
 */
public class NewsFeedDetailNewsFeedFetchTask extends AsyncTask<Void, Void, NewsFeed> {

    public static final int FETCH_COUNT = 10;

    private RssFetchable mFetchable;
    private OnFetchListener mListener;
    private boolean mShuffle;

    public interface OnFetchListener {
        public void onNewsFeedFetchSuccess(NewsFeed newsFeed);
        public void onNewsFeedFetchFail();
    }

    public NewsFeedDetailNewsFeedFetchTask(RssFetchable fetchable,
                                           OnFetchListener listener, boolean shuffle) {
        mFetchable = fetchable;
        mListener = listener;
        mShuffle = shuffle;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {
        NewsFeedUrl newsFeedUrl = null;
        if (mFetchable instanceof NewsTopic) {
            newsFeedUrl = ((NewsTopic)mFetchable).getNewsFeedUrl();
        } else if (mFetchable instanceof NewsFeedUrl) {
            newsFeedUrl = (NewsFeedUrl) mFetchable;
        }
        try {
            NewsFeed newsFeed = NewsFeedFetchUtil.fetch(newsFeedUrl, FETCH_COUNT, mShuffle);

            if (mFetchable instanceof NewsTopic) {
                newsFeed.setTopicIdInfo(((NewsTopic) mFetchable));
            }

            return newsFeed;
        } catch(MalformedURLException | UnknownHostException e) {
            NewsFeed newsFeed = new NewsFeed(mFetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_INVALID_URL);

            return newsFeed;
        } catch(SocketTimeoutException e) {
            NewsFeed newsFeed = new NewsFeed(mFetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_TIMEOUT);

            return newsFeed;
        } catch(IOException | SAXException e) {
            NewsFeed newsFeed = new NewsFeed(mFetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_UNKNOWN);

            return newsFeed;
        }
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
