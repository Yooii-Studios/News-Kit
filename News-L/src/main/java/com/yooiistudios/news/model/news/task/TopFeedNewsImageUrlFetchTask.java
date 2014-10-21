package com.yooiistudios.news.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeedImageUrlFetchUtil;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLNewsImageUrlFetchTask
 *  뉴스의 이미지 url을 뽑아내는 태스크
 */
public class TopFeedNewsImageUrlFetchTask extends AsyncTask<Void, Void, String> {

    private News mNews;
    private int mPosition;
    private int mTaskType;
    private OnTopFeedImageUrlFetchListener mListener;

    public static final int TASK_INITIALIZE = 0;
    public static final int TASK_REFRESH = 1;
    public static final int TASK_REPLACE = 2;

    public TopFeedNewsImageUrlFetchTask(News news, int position, int taskType,
                                        OnTopFeedImageUrlFetchListener listener) {
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

        if (mListener != null) {
            mListener.onTopFeedImageUrlFetch(mNews, imageUrl, mPosition, mTaskType);
        }
    }



    public interface OnTopFeedImageUrlFetchListener {
        public void onTopFeedImageUrlFetch(News news, String url, int position, int taskType);
    }
}
