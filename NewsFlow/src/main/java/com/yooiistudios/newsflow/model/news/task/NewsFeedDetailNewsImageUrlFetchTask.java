package com.yooiistudios.newsflow.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.newsflow.model.news.News;
import com.yooiistudios.newsflow.model.news.util.NewsFeedImageUrlFetchUtil;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NewsFeedDetailNewsImageUrlFetchTask
 *  뉴스의 이미지 url 을 뽑아내는 태스크
 */
public class NewsFeedDetailNewsImageUrlFetchTask extends AsyncTask<Void, Void, String> {

    private News mNews;
    private OnImageUrlFetchListener mListener;

    public NewsFeedDetailNewsImageUrlFetchTask(News news, OnImageUrlFetchListener listener) {
        mNews = news;
        mListener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
//        if (true) {
//            try {
//                Thread.sleep(1000 * 100);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
        return NewsFeedImageUrlFetchUtil.getImageUrl(mNews);
    }

    @Override
    protected void onPostExecute(String imageUrl) {
        super.onPostExecute(imageUrl);

        if (mListener != null) {
            if (imageUrl != null) {
                mListener.onImageUrlFetchSuccess(mNews, imageUrl);
            } else {
                mListener.onImageUrlFetchFail(mNews);
            }
        }
    }



    public interface OnImageUrlFetchListener {
        public void onImageUrlFetchSuccess(News news, String url);
        public void onImageUrlFetchFail(News news);
    }
}
