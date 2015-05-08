package com.yooiistudios.newskit.core.news;

import com.yooiistudios.newskit.core.debug.DebugSettings;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.reflect.Method;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * NLNewsFeedParseHandler
 *  xml 형태의 rss 를 파싱하는 클래스
 */
public class NewsFeedParseHandler extends DefaultHandler {
    private NewsFeed rssFeed;
    private News rssItem;
    private StringBuilder stringBuilder;
    private int mNewsCountLimit;

    public NewsFeedParseHandler(int newsCountLimit) {
        mNewsCountLimit = newsCountLimit;
    }

    @Override
    public void startDocument() {
        rssFeed = new NewsFeed();
    }

    /**
     * Return the parsed RssFeed with it's RssItems
     *
     * @return parsed NLNewsFeed
     */
    public NewsFeed getResult() {
        return rssFeed;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        stringBuilder = new StringBuilder();

        if (qName.equals("item") && rssFeed != null) {
            /*
            // NewsTopic validation 테스트 중 퍼포먼스 개선을 위해 일정 갯수 이상 가져온 경우 무시하는 기능.
            checkNewsCountLimitDebug();
            */
            rssItem = new News();
            rssFeed.addNews(rssItem);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        stringBuilder.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (rssFeed != null && rssItem == null) {
            try {
                if (qName != null && qName.length() > 0) {
                    String methodName = "set" + qName.substring(0, 1).toUpperCase() + qName.substring(1);
                    Method method = ((Object)rssFeed).getClass().getMethod
                            (methodName, String.class);
                    method.invoke(rssFeed, stringBuilder.toString());
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }

        } else if (rssItem != null) {
            try {
                if (qName.equals("content:encoded"))
                    qName = "content";
                String methodName = "set" + qName.substring(0, 1).toUpperCase() + qName.substring(1);
                Method method = ((Object)rssItem).getClass().getMethod
                        (methodName, String.class);
                method.invoke(rssItem, stringBuilder.toString());
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }

    private void checkNewsCountLimitDebug() throws SAXException {
        if (DebugSettings.isDebugBuild() && rssFeed.getNewsList().size() > mNewsCountLimit - 1) {
            throw new SAXException(new BreakParsingException());
        }
    }

    public static class BreakParsingException extends Exception {}
}
