package com.yooiistudios.news.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeedUtils;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 2.
 *
 * NewsLinkContentFetchTask
 *  News.getLink()의 컨텐츠를 fetch함
 */
public class NewsLinkContentFetchTask extends AsyncTask<Void, Void, String> {

    private News mNews;
    private OnContentFetchListener mOnContentFetchListener;

    public interface OnContentFetchListener {
        public void onContentFetch(String content);
    }

    public NewsLinkContentFetchTask(News news, OnContentFetchListener listener) {
        mNews = news;
        mOnContentFetchListener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return NewsFeedUtils.requestHttpGet(mNews.getLink());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
//        CharSequence
//
//        if (mOnContentFetchListener != null) {
//            mOnContentFetchListener.onContentFetch(content);
//        }
    }
}
