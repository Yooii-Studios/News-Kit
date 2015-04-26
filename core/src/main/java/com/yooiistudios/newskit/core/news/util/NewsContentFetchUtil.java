package com.yooiistudios.newskit.core.news.util;

import com.yooiistudios.newskit.core.news.newscontent.NewsContent;
import com.yooiistudios.snacktoryandroid.ArticleTextExtractor;
import com.yooiistudios.snacktoryandroid.HtmlFetcher;
import com.yooiistudios.snacktoryandroid.JResult;

import java.util.List;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 10.
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
            HtmlFetcher htmlFetcher = createHtmlFetcher();

            JResult result = htmlFetcher.fetchAndExtract(url, 30000, true);
            result.setText(getTextWithBreakLine(result));
            fetchResult.newsContent = new NewsContent(result);

            if (result.getOgImages().size() >= 0) {
                fetchResult.imageUrl = result.getOgImages().get(0);
            } else {
                fetchResult.imageUrl = result.getImageUrl();
            }
        } catch (Exception e) {
            fetchResult.newsContent = NewsContent.createErrorObject();
            fetchResult.imageUrl = "";
        }

        return fetchResult;
    }

    private static HtmlFetcher createHtmlFetcher() {
        HtmlFetcher htmlFetcher = new HtmlFetcher();
        ArticleTextExtractor articleTextExtractor = new ArticleTextExtractor();
        // 毎日新聞
        articleTextExtractor.addPositive("NewsBody");
        // 朝日
        articleTextExtractor.addPositive("ArticleText");

        // 구글 - 연예

        // 데일리안
        // http://www.dailian.co.kr/news/view/494007
        // <div id="view_con">
        articleTextExtractor.addPositive("view_con");
        // 경제투데이
        // http://eto.co.kr/news/view.asp?Code=20150315161606500
        // <div class="aticleTxt">
        articleTextExtractor.addPositive("aticleTxt");
        // 한겨레
        // http://www.hani.co.kr/arti/culture/entertainment/682256.html
        // <div class="article-contents">
        articleTextExtractor.addPositive("article-contents");
        // 스포츠 조선
        // <div class='news_text'>
        // http://sports.chosun.com/news/utype.htm?id=201503150100186820012211&ServiceDate=20150315
        articleTextExtractor.addPositive("news_text");
        // 스포츠 투데이
        // <div id='article'>
        // http://stoo.asiae.co.kr/news/view.htm?sec=enter99&idxno=2015031514360840466
//        articleTextExtractor.addPositive("article");

        htmlFetcher.setExtractor(articleTextExtractor);
        return htmlFetcher;
    }

    private static String getTextWithBreakLine(JResult result) {
        StringBuilder builder = new StringBuilder();
        List<String> textList = result.getTextList();
        int textLineCount = textList.size();
        int totalLineTextCount = 0;
        for (int i = 0; i < textLineCount; i++) {
            String line = textList.get(i);
            builder.append(line);
            totalLineTextCount += line.length();
            if (i < textLineCount - 1) {
                builder.append("\n\n");
            }
        }

        String text;

        if (totalLineTextCount == 0) {
            text = result.getText();
        } else {
            text = builder.toString();
        }

        return text;
    }

    public static class NewsContentFetchResult {
        public NewsContent newsContent;
        public String imageUrl;
    }
}
