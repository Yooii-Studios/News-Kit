package com.yooiistudios.newskit.core.news.util;

import android.text.Html;

import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsFeedFetchState;
import com.yooiistudios.newskit.core.news.NewsFeedParser;
import com.yooiistudios.newskit.core.news.NewsFeedUrl;
import com.yooiistudios.newskit.core.news.RssFetchable;

import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
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

            newsFeed = getNewsFeedFromUrl(newsFeedUrl, fetchLimit);

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
        } catch(MalformedURLException e) {
            newsFeed = new NewsFeed(fetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_INVALID_URL);
        } catch(SocketTimeoutException e) {
            newsFeed = new NewsFeed(fetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_TIMEOUT);
        } catch(IOException | SAXException e) {
            // 기존에는 UnknownHostException 을 MalformedURLException 과 함께 잡았지만
            // 인터넷이 안되는 상황에서도 UnknownHostException 이 발생, ERROR_INVALID_URL 로 처리되어
            // 부정확한 데이터를 저장하고 있었음.
            // UnknownHostException 의 경우 IOException 의 sub class 이므로 자동적으로 이 곳에서 처리되게 됨.
            newsFeed = new NewsFeed(fetchable);
            newsFeed.setNewsFeedFetchState(NewsFeedFetchState.ERROR_UNKNOWN);
        }

        return newsFeed;
    }

    private static NewsFeed getNewsFeedFromUrl(NewsFeedUrl newsFeedUrl, int fetchLimit)
            throws IOException, SAXException {
        BufferedInputStream inputStream = null;
        URL url = new URL(newsFeedUrl.getUrl());
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        try {
            conn.setConnectTimeout(TIMEOUT_MILLI);
            conn.setReadTimeout(TIMEOUT_MILLI);
            // 모바일에서는 메인 페이지로 리다이렉트 시켜버리는 페이지(ex. http://www.jpnn.com/index.php?mib=rss&id=215) 대응
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A");
            inputStream = new BufferedInputStream(conn.getInputStream());

            inputStream.mark(Integer.MAX_VALUE);
            String encoding = getEncoding(inputStream);
            inputStream.reset();
            inputStream.mark(0);

            NewsFeed feed = NewsFeedParser.read(inputStream, encoding, fetchLimit);
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

    // TODO: refactor
    private static String getEncoding(InputStream inputStream) {
        // read encoding(Presume that the xml prolog is in the first line)
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 80);
        String prolog = null;
        try {
            char[] buffer = new char[300];
            bufferedReader.read(buffer, 0, 300);
            prolog = new String(buffer);
        } catch (Exception ignored) {}
        String encoding = "";
        if (prolog != null) {
            int prologEndIdx = prolog.indexOf("?>");
            if (prologEndIdx > 0) {
                prolog = prolog.substring(0, prologEndIdx);
            }
            String encodingKey = "encoding";
            int encodingPosition = prolog.indexOf(encodingKey);
            if (encodingPosition >= 0) {
                final char singleQuote = '\'';
                final char doubleQuote = '\"';
                int singleQuoteStartIdx = prolog.indexOf(singleQuote, encodingPosition);
                int doubleQuoteStartIdx = prolog.indexOf(doubleQuote, encodingPosition);

                if (singleQuoteStartIdx != -1 || doubleQuoteStartIdx != -1) {
                    boolean useSingleQuote;
                    if (singleQuoteStartIdx != -1 && doubleQuoteStartIdx != -1) {
                        useSingleQuote = singleQuoteStartIdx < doubleQuoteStartIdx;
                    } else {
                        useSingleQuote = doubleQuoteStartIdx == -1;
                    }
                    int quoteStartIdx = useSingleQuote ? singleQuoteStartIdx : doubleQuoteStartIdx;
                    char quoteToUse = useSingleQuote ? singleQuote : doubleQuote;
                    int quoteEndIdx = prolog.indexOf(quoteToUse, quoteStartIdx + 1);

                    if (isValidIndex(quoteStartIdx, quoteEndIdx)) {
                        encoding = prolog.substring(quoteStartIdx + 1, quoteEndIdx);
                    }
                }

//                int quoteStartIdx = prolog.indexOf('\"', encodingPosition);
//                int quoteEndIdx = prolog.indexOf('\"', quoteStartIdx + 1);
//
//                if (!isValidIndex(quoteStartIdx, quoteEndIdx)) {
//                    quoteStartIdx = prolog.indexOf('\'', encodingPosition);
//                    quoteEndIdx = prolog.indexOf('\'', quoteStartIdx + 1);
//                }
//                if (isValidIndex(quoteStartIdx, quoteEndIdx)) {
//                    encoding = prolog.substring(quoteStartIdx + 1, quoteEndIdx);
//                }
            }
        }

        if (!isValidEncoding(encoding)) {
            encoding = "";
        }

        return encoding;
    }

    private static boolean isValidEncoding(String encoding) {
        boolean isValidEncoding = false;
        try {
            Charset.forName(encoding).newDecoder().onMalformedInput(
                    CodingErrorAction.REPLACE).onUnmappableCharacter(
                    CodingErrorAction.REPLACE);
            isValidEncoding = true;
        } catch (IllegalArgumentException ignored) {}
        return isValidEncoding;
    }

    private static boolean isValidIndex(int quoteStartIdx, int quoteEndIdx) {
        return quoteStartIdx != -1 && quoteEndIdx != -1 && quoteEndIdx > quoteStartIdx;
    }

    private static String getContent(InputStream inputStream, String encoding) throws IOException {
        BufferedReader reader;
        if (encoding.length() > 0) {
            reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
        } else {
            reader = new BufferedReader(new InputStreamReader(inputStream));
        }
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            // TODO: .append("\n") 코드는 테스트시 어느 라인에서 문제가 생긴 건지 체크하기 위한 코드. 지워야 함.
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
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
}
