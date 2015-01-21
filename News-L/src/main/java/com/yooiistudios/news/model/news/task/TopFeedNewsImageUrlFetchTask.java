package com.yooiistudios.news.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.util.NewsFeedImageUrlFetchUtil;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLNewsImageUrlFetchTask
 *  뉴스의 이미지 url 을 뽑아내는 태스크
 */
public class TopFeedNewsImageUrlFetchTask extends AsyncTask<Void, Void, String> {

    public enum TaskType { INITIALIZE, REFRESH, REPLACE, CACHE }

    private News mNews;
    private int mPosition;
    private TaskType mTaskType;
    private OnTopFeedImageUrlFetchListener mListener;

    public TopFeedNewsImageUrlFetchTask(News news, int position, TaskType taskType,
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
        public void onTopFeedImageUrlFetch(News news, String url, int position, TaskType taskType);
    }
}
