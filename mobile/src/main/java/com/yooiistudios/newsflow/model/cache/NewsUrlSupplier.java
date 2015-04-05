package com.yooiistudios.newsflow.model.cache;

import com.yooiistudios.newsflow.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newsflow.core.news.News;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 4. 4.
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
