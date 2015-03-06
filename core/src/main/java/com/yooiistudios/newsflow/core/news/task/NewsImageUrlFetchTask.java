package com.yooiistudios.newsflow.core.news.task;

import android.os.AsyncTask;

import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.util.NewsFeedImageUrlFetchUtil;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 5.
 *
 * NewsImageUrlFetchTask
 *
 */
public class NewsImageUrlFetchTask extends AsyncTask<Void, Void, String> {
    public interface OnImageUrlFetchListener {
        public void onImageUrlFetch(News news, String url, int newsFeedPosition, int newsPosition);
    }
    private News mNews;
    private OnImageUrlFetchListener mListener;
    private int mNewsFeedPosition;
    private int mNewsPosition;

    public NewsImageUrlFetchTask(News news, OnImageUrlFetchListener listener,
                                 int newsFeedPosition, int newsPosition) {
        mNews = news;
        mListener = listener;
        mNewsFeedPosition = newsFeedPosition;
        mNewsPosition = newsPosition;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return NewsFeedImageUrlFetchUtil.getImageUrl(mNews);
    }

    @Override
    protected void onPostExecute(String url) {
        super.onPostExecute(url);
        if (isCancelled()) {
            return;
        }
        mNews.setImageUrl(url);
        mNews.setImageUrlChecked(true);
        if (mListener != null) {
            mListener.onImageUrlFetch(mNews, url, mNewsFeedPosition, mNewsPosition);
        }
    }
}
