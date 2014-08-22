package com.yooiistudios.news.model;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * NLNewsFeedParseHandler
 *  xml 형태의 rss를 파싱하는 클래스
 */
public class NLNewsFeedParseHandler extends DefaultHandler {
    private NLNewsFeed rssFeed;
    private NLNews rssItem;
    private StringBuilder stringBuilder;

    @Override
    public void startDocument() {
        rssFeed = new NLNewsFeed();
    }

    /**
     * Return the parsed RssFeed with it's RssItems
     *
     * @return parsed NLNewsFeed
     */
    public NLNewsFeed getResult() {
        return rssFeed;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        stringBuilder = new StringBuilder();

        if (qName.equals("item") && rssFeed != null) {
            rssItem = new NLNews();
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
            // Parse feed properties

            try {
                if (qName != null && qName.length() > 0) {
                    String methodName = "set" + qName.substring(0, 1).toUpperCase() + qName.substring(1);
                    Method method = ((Object)rssFeed).getClass().getMethod
                            (methodName, String.class);
                    method.invoke(rssFeed, stringBuilder.toString());
                }
            } catch (SecurityException e) {
//                e.printStackTrace();
            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
            } catch (IllegalAccessException e) {
//                e.printStackTrace();
            } catch (InvocationTargetException e) {
//                e.printStackTrace();
            }

        } else if (rssItem != null) {
            // Parse item properties

            try {
                if (qName.equals("content:encoded"))
                    qName = "content";
                String methodName = "set" + qName.substring(0, 1).toUpperCase() + qName.substring(1);
                Method method = ((Object)rssItem).getClass().getMethod
                        (methodName, String.class);
                method.invoke(rssItem, stringBuilder.toString());
            } catch (SecurityException e) {
//                e.printStackTrace();
            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
            } catch (IllegalAccessException e) {
//                e.printStackTrace();
            } catch (InvocationTargetException e) {
//                e.printStackTrace();
            }
        }

    }
}
