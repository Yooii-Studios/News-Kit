package com.yooiistudios.news.model.news;

import android.content.Context;
import android.text.Html;

import com.yooiistudios.news.util.log.NLLog;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 */
public class NewsFeedFetchUtil {
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final String ILLEGAL_CHARACTER_OBJ = Character.toString((char) 65532);

    public static NewsFeed fetch(Context context, NewsFeedUrl feedUrl,
                                   int fetchLimit) {

        if (!feedUrl.getType().equals(NewsFeedUrlType.GENERAL)) {
            // 디폴트 세팅을 사용할 경우 패널단에서 언어설정을 감지 못하므로 무조건 현재 언어의
            // 디폴트 url을 가져온다.
            feedUrl = NewsFeedUtils.getDefaultFeedUrl(context);
        }

        NewsFeed feed = null;
        try {
            // 피드 주소로 커넥션 열기
            URL url = new URL(feedUrl.getUrl());
            URLConnection conn = url.openConnection();

            // RSS 파싱

            long startMilli;
            long endMilli;

            startMilli = System.currentTimeMillis();
            feed = NewsFeedParser.read(conn.getInputStream());
            endMilli = System.currentTimeMillis();
            NLLog.i("performance", "NLNewsFeedParser.read" +
                    (endMilli - startMilli));
            // 퍼포먼스 개선 여지 있음.
            // 로컬 테스트를 위한 코드
//            feed = NLNewsFeedParser.read(mContext.getResources().getAssets().open("feeds.xml"));

            // shuffle and trim size
            Collections.shuffle(feed.getNewsList(), new Random(System.nanoTime()));
            if (fetchLimit > 0 && fetchLimit < feed.getNewsList().size()) {
                ArrayList<News> trimmedNewsList =
                        new ArrayList<News>(feed.getNewsList().subList(0,
                                fetchLimit));
                feed.setNewsList(trimmedNewsList);
            }

            // 피드의 각 뉴스에 대해
            for (News item : feed.getNewsList()) {

                // 피드의 본문에서 텍스트만 걸러내는 작업
                String desc = item.getDescription();
                if (desc != null) {

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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return feed;
    }
}
