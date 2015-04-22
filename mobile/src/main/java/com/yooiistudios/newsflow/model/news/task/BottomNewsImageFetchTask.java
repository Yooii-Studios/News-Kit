package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;

import com.android.volley.VolleyError;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.util.NewsFeedImageUrlFetchUtil;
import com.yooiistudios.newsflow.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newsflow.model.cache.NewsImageLoader;
import com.yooiistudios.newsflow.model.cache.NewsUrlSupplier;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLNewsImageUrlFetchTask
 *  뉴스의 이미지 url 을 뽑아내는 태스크
 */
public class BottomNewsImageFetchTask extends AsyncTask<Void, Void, String> {
    public interface OnBottomImageUrlFetchListener {
        void onBottomImageUrlFetchSuccess(News news, String url,
                                          int newsFeedPosition, int newsPosition, int taskType);
        void onFetchImage(News news, int newsFeedPosition, int newsPosition, int taskType);
    }

    private NewsImageLoader mImageLoader;
    private News mNews;
    private int mNewsFeedIndex;
    private int mNewsIndex;
    private int mTaskType;
    private OnBottomImageUrlFetchListener mListener;

    public static final int TASK_INVALID = -1;
    public static final int TASK_INITIAL_LOAD = 0;
    public static final int TASK_REPLACE = 1;
    public static final int TASK_SWIPE_REFRESH = 2;
    public static final int TASK_AUTO_REFRESH = 3;
    public static final int TASK_CACHE = 4;
    public static final int TASK_MATRIX_CHANGED = 5;

    public BottomNewsImageFetchTask(NewsImageLoader imageLoader, News news,
                                    int newsFeedIndex, int newsIndex,
                                    int taskType, OnBottomImageUrlFetchListener listener) {
        mImageLoader = imageLoader;
        mNews = news;
        mNewsFeedIndex = newsFeedIndex;
        mNewsIndex = newsIndex;
        mTaskType = taskType;
        mListener = listener;
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
    protected void onPostExecute(String imageUrl) {
        super.onPostExecute(imageUrl);
        mNews.setImageUrl(imageUrl);
        mNews.setImageUrlChecked(true);
        if (mListener != null) {
            mListener.onBottomImageUrlFetchSuccess(mNews, imageUrl, mNewsFeedIndex, mNewsIndex, mTaskType);

            if (mNews.hasImageUrl()) {
                mImageLoader.get(new NewsUrlSupplier(mNews, mNewsFeedIndex), new CacheImageLoader.ImageListener() {
                    @Override
                    public void onSuccess(CacheImageLoader.ImageResponse response) {
                        mListener.onFetchImage(mNews, mNewsFeedIndex, mNewsIndex, mTaskType);
                    }

                    @Override
                    public void onFail(VolleyError error) {
                        mListener.onFetchImage(mNews, mNewsFeedIndex, mNewsIndex, mTaskType);
                    }
                });
            } else {
                mListener.onFetchImage(mNews, mNewsFeedIndex, mNewsIndex, mTaskType);
            }
        }
    }
}
