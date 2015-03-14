package com.yooiistudios.newsflow.core.news.util;

import com.yooiistudios.newsflow.core.news.newscontent.NewsContent;
import com.yooiistudios.snacktoryandroid.HtmlFetcher;
import com.yooiistudios.snacktoryandroid.JResult;

import java.util.List;

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
            result.setText(getTextWithBreakLine(result));
            fetchResult.newsContent = new NewsContent(result);
//            fetchResult.imageUrl = result.getImageUrl();

            if (result.getOgImages().size() >= 0) {
                fetchResult.imageUrl = result.getOgImages().get(0);
            } else {
                fetchResult.imageUrl = result.getImageUrl();
            }

//            NLLog.now("ogImage size: " + result.getOgImages().size());
//            NLLog.now("srcImage size: " + (result.getImages() != null ? result.getImages().size() : "0"));
        } catch (Exception e) {
            fetchResult.newsContent = NewsContent.createErrorObject();
            fetchResult.imageUrl = "";
        }

        return fetchResult;
    }

    private static String getTextWithBreakLine(JResult result) {
        StringBuilder builder = new StringBuilder();
        List<String> textList = result.getTextList();
        int textLineCount = textList.size();
        for (int i = 0; i < textLineCount; i++) {
            builder.append(textList.get(i));
            if (i < textLineCount - 1) {
                builder.append("\n\n");
            }
        }

        return builder.toString();
    }

    public static class NewsContentFetchResult {
        public NewsContent newsContent;
        public String imageUrl;

//        public List<ImageResult> imageResults;
//        public List<String> ogImages;
    }
}
