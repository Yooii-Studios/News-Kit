package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.newsflow.model.RssFetchable;
import com.yooiistudios.newsflow.model.news.NewsFeed;
import com.yooiistudios.newsflow.model.news.NewsFeedFetchState;
import com.yooiistudios.newsflow.model.news.NewsTopic;
import com.yooiistudios.newsflow.model.news.util.NewsFeedFetchUtil;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLTopNewsFeedFetchTask
 *  메인 액티비티의 탑 뉴스를 가져오는 클래스
 */
public class TopNewsFeedFetchTask extends AsyncTask<Void, Void, NewsFeed> {

    public enum TaskType { INITIALIZE, SWIPE_REFRESH, REPLACE, CACHE }

    public static final int FETCH_COUNT = 10;

    private RssFetchable mRssFetchable;
    private NewsFeed mNewsFeed;
    private OnFetchListener mListener;
    private TaskType mTaskType;
    private boolean mShuffle;

//    public static final int TASK_INITIALIZE = 0;
//    public static final int TASK_SWIPE_REFRESH = 1;
//    public static final int TASK_REPLACE = 2;

    public interface OnFetchListener {
        public void onTopNewsFeedFetch(NewsFeed newsFeed, TaskType taskType);
    }

    public TopNewsFeedFetchTask(RssFetchable rssFetchable,
                                OnFetchListener listener, TaskType taskType, boolean shuffle) {
        mRssFetchable = rssFetchable;
        mListener = listener;
        mTaskType = taskType;
        mShuffle = shuffle;
    }

    public TopNewsFeedFetchTask(NewsFeed newsFeed,
                                OnFetchListener listener, TaskType taskType, boolean shuffle) {
        this(newsFeed.getNewsFeedUrl(), listener, taskType, shuffle);
        mNewsFeed = newsFeed;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {
        try {
            NewsFeed newsFeed =
                    NewsFeedFetchUtil.fetch(mRssFetchable, FETCH_COUNT, mShuffle);
            if (mNewsFeed != null) {
                newsFeed.setTopicIdInfo(mNewsFeed);
            } else if (mRssFetchable instanceof NewsTopic) {
                newsFeed.setTopicIdInfo((NewsTopic)mRssFetchable);
            }

            return newsFeed;
        } catch(MalformedURLException | UnknownHostException e) {
            NewsFeed newsFeed = new NewsFeed(mRssFetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_INVALID_URL);

            return newsFeed;
        } catch(SocketTimeoutException e) {
            NewsFeed newsFeed = new NewsFeed(mRssFetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_TIMEOUT);

            return newsFeed;
        } catch(IOException | SAXException e) {
            NewsFeed newsFeed = new NewsFeed(mRssFetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_UNKNOWN);

            return newsFeed;
        }
    }

    @Override
    protected void onPostExecute(NewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (mListener != null) {
            mListener.onTopNewsFeedFetch(newsFeed, mTaskType);
        }
    }
}
