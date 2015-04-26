package com.yooiistudios.newskit.core.news.task;

import android.os.AsyncTask;

import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.news.newscontent.NewsContent;
import com.yooiistudios.newskit.core.news.util.NewsContentFetchUtil;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 5.
 *
 * NewsImageUrlFetchTask
 *  News 의 link 로부터 내용물을 추출하는 태스크
 */
public class NewsContentFetchTask extends AsyncTask<Void, Void, NewsContentFetchUtil.NewsContentFetchResult> {
    public interface OnContentFetchListener {
        public void onContentFetch(News news, NewsContent newsContent,
                                   int newsFeedPosition, int newsPosition);
    }
    private News mNews;
    private OnContentFetchListener mListener;
    private int mNewsFeedPosition;
    private int mNewsPosition;

    public NewsContentFetchTask(News news, OnContentFetchListener listener,
                                int newsFeedPosition, int newsPosition) {
        mNews = news;
        mListener = listener;
        mNewsFeedPosition = newsFeedPosition;
        mNewsPosition = newsPosition;
    }

    @Override
    protected NewsContentFetchUtil.NewsContentFetchResult doInBackground(Void... voids) {
        return NewsContentFetchUtil.fetch(mNews.getLink());
    }

    @Override
    protected void onPostExecute(NewsContentFetchUtil.NewsContentFetchResult newsContentFetchResult) {
        super.onPostExecute(newsContentFetchResult);
        if (isCancelled()) {
            return;
        }
        mNews.setNewsContent(newsContentFetchResult.newsContent);
        mNews.setImageUrl(newsContentFetchResult.imageUrl);
        mNews.setImageUrlChecked(true);
        if (mListener != null) {
            mListener.onContentFetch(mNews, newsContentFetchResult.newsContent, mNewsFeedPosition, mNewsPosition);
        }
    }
}
