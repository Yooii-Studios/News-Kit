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
        void onFetchImageUrl(News news, String url, int newsFeedPosition);
    }
    private News mNews;
    private OnImageUrlFetchListener mListener;
    private int mNewsFeedPosition;

    public NewsImageUrlFetchTask(News news, OnImageUrlFetchListener listener,
                                 int newsFeedPosition) {
        mNews = news;
        mListener = listener;
        mNewsFeedPosition = newsFeedPosition;
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (mNews.hasImageUrl()) {
            return mNews.getImageUrl();
        } else {
            return NewsFeedImageUrlFetchUtil.getImageUrl(mNews);
        }
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
            mListener.onFetchImageUrl(mNews, url, mNewsFeedPosition);
        }
    }
}
