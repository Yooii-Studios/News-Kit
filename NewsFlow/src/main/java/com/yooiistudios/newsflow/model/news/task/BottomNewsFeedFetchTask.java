package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.newsflow.model.news.NewsFeed;
import com.yooiistudios.newsflow.model.news.NewsFeedFetchState;
import com.yooiistudios.newsflow.model.news.util.NewsFeedFetchUtil;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLBottomNewsFeedFetchTask
 *  메인 화면 하단 뉴스피드 로딩을 담당
 **/
public class BottomNewsFeedFetchTask extends AsyncTask<Void, Void, NewsFeed> {

    private NewsFeed mNewsFeed;
    private OnFetchListener mListener;
    private int mPosition;
    private boolean mShuffle;
    private int mTaskType;

    public static final int TASK_INVALID = -1;
    public static final int TASK_INITIALIZE = 0;
    public static final int TASK_REFRESH = 1;
    public static final int TASK_REPLACE = 2;
    public static final int TASK_CACHE = 3;
    public static final int TASK_MATRIX_CHANGED = 4;

    public interface OnFetchListener {
        public void onBottomNewsFeedFetch(NewsFeed newsFeed, int position, int taskType);
    }

    public BottomNewsFeedFetchTask(NewsFeed newsFeed,
                                   int position, int taskType, OnFetchListener listener) {
        this(newsFeed, position, taskType, listener, true);
    }
    public BottomNewsFeedFetchTask(NewsFeed newsFeed,
                                   int position, int taskType, OnFetchListener listener, boolean shuffle) {
        mNewsFeed = newsFeed;
        mPosition = position;
        mTaskType = taskType;
        mListener = listener;
        mShuffle = shuffle;
    }

    @Override
    protected NewsFeed doInBackground(Void... voids) {
        try {
            NewsFeed newsFeed = NewsFeedFetchUtil.fetch(mNewsFeed.getNewsFeedUrl(), 10, mShuffle);
            newsFeed.setTopicIdInfo(mNewsFeed);

            return newsFeed;
        } catch(MalformedURLException | UnknownHostException e) {
            mNewsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_INVALID_URL);
        } catch(SocketTimeoutException e) {
            mNewsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_TIMEOUT);
        } catch(IOException | SAXException e) {
            mNewsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_UNKNOWN);
        }

        return null;
    }

    @Override
    protected void onPostExecute(NewsFeed newsFeed) {
        super.onPostExecute(newsFeed);
        if (mListener != null) {
            mListener.onBottomNewsFeedFetch(newsFeed != null ? newsFeed : mNewsFeed, mPosition, mTaskType);
        }
    }
}
