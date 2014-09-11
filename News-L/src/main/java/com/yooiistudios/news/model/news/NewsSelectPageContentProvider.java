package com.yooiistudios.news.model.news;

import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 11.
 *
 * NewsSelectPageUrlProvider
 *  뉴스 선택 화면에서 탭의 국가 및 뉴스피드 목록을 제공함
 */
public class NewsSelectPageContentProvider {

    private HashMap<String, ArrayList<NewsFeed>> mNewsFeedUrlMap;
    private ArrayList<NewsSelectPageLanguage> mLanguageList;

    private static NewsSelectPageContentProvider instance;

    public static NewsSelectPageContentProvider getInstance() {
        if (instance == null) {
            instance = new NewsSelectPageContentProvider();
        }

        return instance;
    }

    private NewsSelectPageContentProvider() {
        mNewsFeedUrlMap = new HashMap<String, ArrayList<NewsFeed>>();
    }
    public ArrayList<NewsSelectPageLanguage> getLanguageList(Context context) {
        if (mLanguageList == null) {
            mLanguageList = readLanguageList(context);
        }

        return mLanguageList;
    }

    private ArrayList<NewsSelectPageLanguage> readLanguageList(Context context) {
            InputStream in = null;
            try {
                in = context.getAssets().open("select_page_news_language.xml");
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(in, null);

                in.close();
                in = null;

                NodeList languageList = doc.getElementsByTagName("region");

                int totalCnt = languageList.getLength();

                ArrayList<NewsSelectPageLanguage> languageArrayList
                        = new ArrayList<NewsSelectPageLanguage>();
                for (int i = 0; i < totalCnt; i++) {
                    Element language = (Element) languageList.item(i);

                    String englishNameStr = language.getElementsByTagName("english_name").item(0)
                            .getChildNodes().item(0).getNodeValue();
                    String regionalNameStr = language.getElementsByTagName("regional_name").item(0)
                            .getChildNodes().item(0).getNodeValue();
                    String languageStr = language.getElementsByTagName("language").item(0)
                            .getChildNodes().item(0).getNodeValue();
                    Node countryNode = language.getElementsByTagName("country").item(0)
                            .getChildNodes().item(0);
                    String countryStr = countryNode != null ? countryNode.getNodeValue() : null;

//                    if (countryStr == null || countryStr.length() == 0) {
//                        countryStr = null;
//                    }

                    languageArrayList.add(new NewsSelectPageLanguage(
                            englishNameStr, regionalNameStr, languageStr, countryStr));
                }

                return languageArrayList;
            } catch(Exception e) {
                e.printStackTrace();
            }

        return null;
    }


    public ArrayList<NewsFeed> getNewsFeeds(Context context, NewsSelectPageLanguage language) {
        ArrayList<NewsFeed> list = null;
        String key = language.getLanguageCountryCode();
        if ((list = mNewsFeedUrlMap.get(key)) == null) {
            list = readNewsFeedUrls(context, key);
            mNewsFeedUrlMap.put(key, list);
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

    public NewsSelectPageLanguage getLanguageAt(Context context, int idx) {
        return getLanguageList(context).get(idx);
    }
    public int getLanguageCount(Context context) {
        return getLanguageList(context).size();
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
