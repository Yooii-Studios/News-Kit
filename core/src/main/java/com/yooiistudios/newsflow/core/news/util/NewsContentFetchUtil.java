package com.yooiistudios.newsflow.core.news.util;

import com.yooiistudios.newsflow.core.news.newscontent.NewsContent;
import com.yooiistudios.snacktoryandroid.HtmlFetcher;
import com.yooiistudios.snacktoryandroid.JResult;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 10.
 *
 * NewsContentFetchUtil
 *  Snacktory library 를 사용해 html 의 내용물을 파싱함
 */
public class NewsContentFetchUtil {
    private NewsContentFetchUtil() {
        throw new AssertionError("You MUST NOT create the instance of this class!!");
    }

    public static NewsContentFetchResult fetch(String url) {
        NewsContentFetchResult fetchResult = new NewsContentFetchResult();
        try {
            JResult result = new HtmlFetcher().fetchAndExtract(url, 30000, true);
            fetchResult.newsContent = new NewsContent(result);
            fetchResult.imageUrl = result.getImageUrl();
        } catch (Exception e) {
            fetchResult.newsContent = NewsContent.createErrorObject();
            fetchResult.imageUrl = "";
        }

        return fetchResult;
    }

    public static class NewsContentFetchResult {
        public NewsContent newsContent;
        public String imageUrl;
    }
}
