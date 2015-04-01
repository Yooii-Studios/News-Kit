package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;

import com.android.volley.VolleyError;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.util.NewsFeedImageUrlFetchUtil;
import com.yooiistudios.newsflow.core.cache.volley.CacheImageLoader;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLNewsImageUrlFetchTask
 *  뉴스의 이미지 url 을 뽑아내는 태스크
 */
public class BottomNewsImageFetchTask extends AsyncTask<Void, Void, String> {

//    private ImageLoader mImageLoader;
    private CacheImageLoader mImageLoader;
    private News mNews;
    private int mPosition;
    private int mTaskType;
    private OnBottomImageUrlFetchListener mListener;

    public static final int TASK_INVALID = -1;
    public static final int TASK_INITIAL_LOAD = 0;
    public static final int TASK_REPLACE = 1;
    public static final int TASK_SWIPE_REFRESH = 2;
    public static final int TASK_AUTO_REFRESH = 3;
    public static final int TASK_CACHE = 4;
    public static final int TASK_MATRIX_CHANGED = 5;

    public BottomNewsImageFetchTask(CacheImageLoader imageLoader, News news, int position,
                                    int taskType, OnBottomImageUrlFetchListener listener) {
        mImageLoader = imageLoader;
        mNews = news;
        mPosition = position;
        mTaskType = taskType;
        mListener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return NewsFeedImageUrlFetchUtil.getImageUrl(mNews);
    }

    @Override
    protected void onPostExecute(String imageUrl) {
        super.onPostExecute(imageUrl);
        mNews.setImageUrl(imageUrl);
        mNews.setImageUrlChecked(true);
        if (mListener != null) {
            mListener.onBottomImageUrlFetchSuccess(mNews, imageUrl, mPosition, mTaskType);

            if (mNews.hasImageUrl()) {
                mImageLoader.get(mNews.getImageUrl(), new CacheImageLoader.ImageListener() {
                    @Override
                    public void onSuccess(CacheImageLoader.ImageResponse response) {
                        mListener.onFetchImage(mNews, mPosition, mTaskType);
                    }

                    @Override
                    public void onFail(VolleyError error) {
                        mListener.onFetchImage(mNews, mPosition, mTaskType);
                    }
                });
            } else {
                mListener.onFetchImage(mNews, mPosition, mTaskType);
            }
        }
    }



    public interface OnBottomImageUrlFetchListener {
        public void onBottomImageUrlFetchSuccess(News news, String url,
                                                 int position, int taskType);
        public void onFetchImage(News news, int position, int taskType);
    }
}
