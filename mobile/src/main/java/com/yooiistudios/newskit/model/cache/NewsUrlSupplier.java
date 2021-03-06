package com.yooiistudios.newskit.model.cache;

import com.yooiistudios.newskit.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newskit.core.news.News;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 4. 4.
 *
 * NewsUrlSupplier
 *  뉴스 이미지를 CacheImageLoader 에서 가져올 때 사용됨
 */
public class NewsUrlSupplier implements CacheImageLoader.UrlSupplier {
    private News mNews;
    private int mNewsFeedPosition;
    private String mGuid;

    public NewsUrlSupplier(News news, int newsFeedPosition) {
        mNews = news;
        mNewsFeedPosition = newsFeedPosition;
        mGuid = news.getGuid();
    }

    public int getNewsFeedPosition() {
        return mNewsFeedPosition;
    }

    public String getGuid() {
        return mGuid;
    }

    public News getNews() {
        return mNews;
    }

    public boolean hasInvalidImageUrl() {
        return mNews.getImageUrlState() == News.IMAGE_URL_STATE_INVALID;
    }

    public void setImageUrlState(int state) {
        mNews.setImageUrlState(state);
    }

    @Override
    public String getUrl() {
        return mNews.getImageUrl();
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + mGuid.hashCode();
        hash = hash * 31 + mNewsFeedPosition;

        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NewsUrlSupplier) {
            NewsUrlSupplier target = (NewsUrlSupplier)o;
            return mNewsFeedPosition == target.mNewsFeedPosition
                    && mGuid.equals(target.mGuid);
        } else {
            return false;
        }
    }
}
