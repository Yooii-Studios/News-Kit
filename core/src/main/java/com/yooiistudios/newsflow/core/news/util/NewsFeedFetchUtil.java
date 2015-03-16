package com.yooiistudios.newsflow.core.news.util;

import android.text.Html;

import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.NewsFeedFetchState;
import com.yooiistudios.newsflow.core.news.NewsFeedParser;
import com.yooiistudios.newsflow.core.news.NewsFeedUrl;
import com.yooiistudios.newsflow.core.news.RssFetchable;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
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
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final String ILLEGAL_CHARACTER_OBJ = Character.toString((char) 65532);

    private static final int TIMEOUT_MILLI = 5000;

    public static NewsFeed fetch(RssFetchable fetchable, int fetchLimit, boolean shuffle) {
        NewsFeed newsFeed;
        try {
            NewsFeedUrl newsFeedUrl = fetchable.getNewsFeedUrl();

            newsFeed = getNewsFeedFromUrl(newsFeedUrl);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.SUCCESS);

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
            refactorDescription(newsFeed);
        } catch(MalformedURLException| UnknownHostException e) {
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
        InputStream inputStream = getInputStreamFromNewsFeedUrl(newsFeedUrl);
        NewsFeed feed = NewsFeedParser.read(inputStream);
        feed.setNewsFeedUrl(newsFeedUrl);
        inputStream.close();
        return feed;
    }

    private static void shuffleNewsList(NewsFeed feed) {
        Collections.shuffle(feed.getNewsList(), new Random(System.nanoTime()));
    }

    private static void refactorDescription(NewsFeed feed) {
        for (News item : feed.getNewsList()) {
            String desc = item.getDescription();
            if (desc != null) {
                item.setOriginalDescription(desc);

                String strippedDesc = Html.fromHtml(desc.substring(0,
                        desc.length())).toString();

                int length = strippedDesc.length() > MAX_DESCRIPTION_LENGTH ?
                        MAX_DESCRIPTION_LENGTH : strippedDesc.length();
                String refinedDesc = new StringBuilder(strippedDesc).substring
                        (0, length).replaceAll(ILLEGAL_CHARACTER_OBJ, "")
                        .replaceAll("\n", " ");
                item.setDescription(refinedDesc);
            }
        }
    }

    private static void trimNewsList(NewsFeed feed, int fetchLimit) {
        ArrayList<News> trimmedNewsList =
                new ArrayList<>(feed.getNewsList().subList(0, fetchLimit));
        feed.setNewsList(trimmedNewsList);
    }

    private static boolean shouldTrimNewsList(NewsFeed feed, int fetchLimit) {
        return fetchLimit > 0 && fetchLimit < feed.getNewsList().size();
    }

    private static InputStream getInputStreamFromNewsFeedUrl(NewsFeedUrl newsFeedUrl) throws IOException {
        URL url = new URL(newsFeedUrl.getUrl());
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(TIMEOUT_MILLI);
        conn.setReadTimeout(TIMEOUT_MILLI);
        return conn.getInputStream();
    }
}
