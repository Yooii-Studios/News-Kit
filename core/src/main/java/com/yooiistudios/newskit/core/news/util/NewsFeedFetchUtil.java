package com.yooiistudios.newskit.core.news.util;

import android.text.Html;

import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsFeedFetchState;
import com.yooiistudios.newskit.core.news.NewsFeedParser;
import com.yooiistudios.newskit.core.news.NewsFeedUrl;
import com.yooiistudios.newskit.core.news.RssFetchable;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NewsFeedFetchUtil
 *  url 을 받아 뉴스피드를 파싱해 가져오는 유틸
 */
public class NewsFeedFetchUtil {
    // 추후 변경사항에 대비해 남겨둠
    public static final int FETCH_LIMIT_TOP = 20;
    public static final int FETCH_LIMIT_BOTTOM = 20;
    public static final int FETCH_LIMIT_TV = 20;
    public static final int INVALID_FETCH_LIMIT = -1;

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final String ILLEGAL_CHARACTER_OBJ = Character.toString((char) 65532);

    private static final int TIMEOUT_MILLI = 5000;

//    public static NewsFeed fetch(RssFetchable fetchable, boolean shuffle) {
//        return fetch(fetchable, DEFAULT_FETCH_LIMIT, shuffle);
//    }

    public static NewsFeed fetch(RssFetchable fetchable, int fetchLimit, boolean shuffle) {
        NewsFeed newsFeed;
        try {
            NewsFeedUrl newsFeedUrl = fetchable.getNewsFeedUrl();

            newsFeed = getNewsFeedFromUrl(newsFeedUrl);

            if (newsFeed.containsNews()) {
                newsFeed.setNewsFeedFetchState(NewsFeedFetchState.SUCCESS);
                cleanUpNewsContents(newsFeed);

                if (shuffle) {
                    shuffleNewsList(newsFeed);
                }
                if (shouldTrimNewsList(newsFeed, fetchLimit)) {
                    trimNewsList(newsFeed, fetchLimit);
                }
                for(News news : newsFeed.getNewsList()) {
                    if (news.getGuid() == null) {
                        news.setGuid(news.getLink());
                    }
                }
            } else {
                newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_NO_NEWS);
            }
        } catch(MalformedURLException | UnknownHostException e) {
            newsFeed = new NewsFeed(fetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_INVALID_URL);
        } catch(SocketTimeoutException e) {
            newsFeed = new NewsFeed(fetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_TIMEOUT);
        } catch(IOException | SAXException e) {
            newsFeed = new NewsFeed(fetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_UNKNOWN);
        }

        return newsFeed;
    }

    private static NewsFeed getNewsFeedFromUrl(NewsFeedUrl newsFeedUrl) throws IOException, SAXException {
        InputStream inputStream = null;
        URL url = new URL(newsFeedUrl.getUrl());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        try {
            conn.setConnectTimeout(TIMEOUT_MILLI);
            conn.setReadTimeout(TIMEOUT_MILLI);
            inputStream = conn.getInputStream();
//            inputStream = getInputStreamFromNewsFeedUrl(newsFeedUrl);
            NewsFeed feed = NewsFeedParser.read(inputStream);
            feed.setNewsFeedUrl(newsFeedUrl);

            return feed;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void cleanUpNewsContents(NewsFeed newsFeed) {
        for (News news : newsFeed.getNewsList()) {
            // 뉴스 내용에 앞뒤로 공간이 있을 경우가 있어 첫 로딩 시 trim 을 적용해줌
            news.setTitle(news.getTitle().trim());
            cleanUpNewsDescription(news);

            news.setLink(news.getLink().replaceAll("(\\r|\\n|\\t)", "").trim());
        }
    }

    private static void shuffleNewsList(NewsFeed feed) {
        Collections.shuffle(feed.getNewsList(), new Random(System.nanoTime()));
    }

    private static boolean shouldTrimNewsList(NewsFeed feed, int fetchLimit) {
        return fetchLimit != INVALID_FETCH_LIMIT
                && fetchLimit > 0
                && fetchLimit < feed.getNewsList().size();
    }

    private static void trimNewsList(NewsFeed feed, int fetchLimit) {
        ArrayList<News> trimmedNewsList =
                new ArrayList<>(feed.getNewsList().subList(0, fetchLimit));
        feed.setNewsList(trimmedNewsList);
    }

    private static void cleanUpNewsDescription(News news) {
        String desc = news.getDescription();
        if (desc != null) {
            news.setOriginalDescription(desc);

            String strippedDesc = Html.fromHtml(desc.substring(0,
                    desc.length())).toString();

            int length = strippedDesc.length() > MAX_DESCRIPTION_LENGTH ?
                    MAX_DESCRIPTION_LENGTH : strippedDesc.length();
            String refinedDesc = new StringBuilder(strippedDesc).substring
                    (0, length).replaceAll(ILLEGAL_CHARACTER_OBJ, "")
                    .replaceAll("\n", " ").trim();
            news.setDescription(refinedDesc);
        }
    }

//    private static InputStream getInputStreamFromNewsFeedUrl(NewsFeedUrl newsFeedUrl) throws IOException {
//        URL url = new URL(newsFeedUrl.getUrl());
//        URLConnection conn = url.openConnection();
//        conn.setConnectTimeout(TIMEOUT_MILLI);
//        conn.setReadTimeout(TIMEOUT_MILLI);
//        return conn.getInputStream();
//    }
}
