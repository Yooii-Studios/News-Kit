package com.yooiistudios.news.model.news;

import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 11.
 */
public class NewsSelectPageUrlProvider {

    private HashMap<String, ArrayList<NewsFeed>> mNewsFeedUrlMap;

    private static NewsSelectPageUrlProvider instance;

    public static NewsSelectPageUrlProvider getInstance() {
        if (instance == null) {
            instance = new NewsSelectPageUrlProvider();
        }

        return instance;
    }

    private NewsSelectPageUrlProvider() {
        mNewsFeedUrlMap = new HashMap<String, ArrayList<NewsFeed>>();
    }


    public ArrayList<NewsFeed> getNewsFeeds(Context context, String languageCode) {
        ArrayList<NewsFeed> list = null;
        if ((list = mNewsFeedUrlMap.get(languageCode)) == null) {
            list = readNewsFeedUrls(context, languageCode);
            mNewsFeedUrlMap.put(languageCode, list);
        }
        return list;
    }
    public ArrayList<NewsFeed> readNewsFeedUrls(Context context, String languageCode) {
        InputStream in = null;
        try {
            in = context.getAssets().open("select_page_news_" + languageCode + ".xml");
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(in, null);

            in.close();
            in = null;

            NodeList newsList = doc.getElementsByTagName("news");

            int totalCnt = newsList.getLength();

            ArrayList<NewsFeed> newsFeedArrayList = new ArrayList<NewsFeed>();
            for (int i=0;i<totalCnt;i++) {
                Element news = (Element) newsList.item(i);

                String newsName = news.getElementsByTagName("name").item(0).getChildNodes().item(0)
                        .getNodeValue();
                String newsUrl = news.getElementsByTagName("url").item(0).getChildNodes().item(0)
                        .getNodeValue();

                NewsFeed newsFeed = new NewsFeed();
                newsFeed.setTitle(newsName);
                newsFeedArrayList.add(newsFeed);
            }

            return newsFeedArrayList;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getLanguageAt(int idx) {
        switch(idx) {
            case 0:
                return "en";
            case 1:
                return "ko";
            default:
                return "en";
        }
    }
    public static int getLanguageCount() {
        return 2;
    }

//    public static int getSelectNewsFeedCountryCount() {
//        return sSelectNewsFeedList.size();
//    }
//    public static ArrayList<NewsFeedUrl> getSelectNewsFeedListAt(int index) {
//        return sSelectNewsFeedList.get(index);
//    }
//
//    private static ArrayList<NewsFeedUrl> getNewsFeedPresetList(NewsLanguage newsLanguage) {
//        ArrayList<NewsFeedUrl> countryNewsList = new ArrayList<NewsFeedUrl>();
//
//        for (NewsFeedUrl preset : NewsFeedUrl.values()) {
//            if (preset.getNewsLanguage().equals(newsLanguage)) {
//                countryNewsList.add(preset);
//            }
//        }
//
//        return countryNewsList;
//    }
}
