package com.yooiistudios.news.model.news;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

    public static NewsPublisherList getNewsProviders(Context context, int position) {
        // int를 raw id로 변환
        int resourceId = NewsPublisherLangType.valueOf(position).getResourceId();

        // raw id 에서 json 스트링을 만들고 JSONObject 로 변환
        try {
            InputStream file;
            file = context.getResources().openRawResource(resourceId);

            BufferedReader reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
            char[] tC = new char[file.available()];
            reader.read(tC);

            String content = new String(tC);

            // 변환한 JSONObject 를 NewsPublishers 로 변환
            JSONObject newsData = new JSONObject(content);

            // lang info
            NewsPublisherList newsPublisherList = new NewsPublisherList();
            newsPublisherList.englishLanguageName = newsData.getString("lang_name_english");
            newsPublisherList.regionalLanguageName = newsData.getString("lang_name_regional");
            newsPublisherList.languageCode = newsData.getString("lang_code");
            String regionCode = newsData.getString("region_code");
            if (!regionCode.equals("null")) {
                newsPublisherList.regionCode = regionCode;
            }

            // publishers
            JSONArray providersArray = newsData.getJSONArray("news_publishers");
            for (int i = 0; i < providersArray.length(); i++) {
                JSONObject newsProviderObject = providersArray.getJSONObject(i);

                NewsPublisher newsPublisher = new NewsPublisher();
                newsPublisher.setName(newsProviderObject.getString("publisher_name"));

                JSONArray categoriesArray = newsProviderObject.getJSONArray("categories");
                for (int j = 0; j < categoriesArray.length(); j++) {
                    JSONObject categoryObject = categoriesArray.getJSONObject(j);

                    NewsFeed newsFeed = new NewsFeed();
                    newsFeed.setTitle(categoryObject.getString("category_name"));

                    String url = categoryObject.getString("url");
                    newsFeed.setNewsFeedUrl(new NewsFeedUrl(url, NewsFeedUrlType.GENERAL));

                    newsPublisher.addNewsFeed(newsFeed);
                }

                newsPublisherList.newsPublishers.add(newsPublisher);
            }

            /*
            // Test
            NLLog.now("lang: " + newsPublisherList.englishLanguageName);
            NLLog.now("lang_region: " + newsPublisherList.regionalLanguageName);
            NLLog.now("lang_code: " + newsPublisherList.languageCode);
            NLLog.now("region_code: " + newsPublisherList.languageCode);
            NLLog.now("newsProviders");

            for (NewsPublisher newsPublisher : newsPublisherList.newsPublishers) {
                NLLog.now("provider name: " + newsPublisher.getName());
                for (NewsFeed newsFeed : newsPublisher.getNewsFeedList()) {
                    NLLog.now("newsFeed name: " + newsFeed.getTitle());
                    NLLog.now("newsFeed url: " + newsFeed.getNewsFeedUrl().getUrl());
                }
            }
            */

            return newsPublisherList;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*
    private HashMap<String, ArrayList<NewsPublisher>> mNewsProviderMap;
    private ArrayList<NewsSelectPageLanguage> mLanguageList;

    private static NewsSelectPageContentProvider instance;

    public static NewsSelectPageContentProvider getInstance() {
        if (instance == null) {
            instance = new NewsSelectPageContentProvider();
        }

        return instance;
    }

    private NewsSelectPageContentProvider() {
        mNewsProviderMap = new HashMap<String, ArrayList<NewsPublisher>>();
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


    public ArrayList<NewsPublisher> getNewsFeeds(Context context, NewsSelectPageLanguage language) {
        ArrayList<NewsPublisher> list = null;
        String key = language.getLanguageCountryCode();
        if ((list = mNewsProviderMap.get(key)) == null) {
            list = readNewsFeedUrls(context, key);
            mNewsProviderMap.put(key, list);
        }
        return list;
    }
    public ArrayList<NewsPublisher> readNewsFeedUrls(Context context, String languageCode) {
        InputStream in = null;
        try {
            in = context.getAssets().open("select_page_news_" + languageCode + ".xml");
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(in, null);

            in.close();
            in = null;

            NodeList newsProviderList = doc.getElementsByTagName("news_provider");

            int newsProviderCnt = newsProviderList.getLength();

            ArrayList<NewsPublisher> newsPublisherArrayList = new ArrayList<NewsPublisher>();
            for (int i=0;i<newsProviderCnt;i++) {
                Element newsPublisherElement = (Element) newsProviderList.item(i);

                String newsProviderName = newsPublisherElement.getElementsByTagName("name")
                        .item(0).getChildNodes().item(0).getNodeValue();

                NodeList newsList = newsPublisherElement.getElementsByTagName("news");

                int newsCount = newsList.getLength();

                NewsPublisher newsPublisher = new NewsPublisher();
                newsPublisher.setName(newsProviderName);

                for (int j = 0; j < newsCount; j++) {
                    Element newsFeedElement = (Element) newsList.item(j);
                    String newsName = newsFeedElement.getElementsByTagName("name")
                            .item(0).getChildNodes().item(0).getNodeValue();
                    String newsUrl = newsFeedElement.getElementsByTagName("url")
                            .item(0).getChildNodes().item(0).getNodeValue();

                    NewsFeed newsFeed = new NewsFeed();
                    newsFeed.setTitle(newsName);
                    newsFeed.setNewsFeedUrl(new NewsFeedUrl(newsUrl, NewsFeedUrlType.GENERAL));

                    newsPublisher.addNewsFeed(newsFeed);
                }
                newsPublisherArrayList.add(newsPublisher);
            }

            return newsPublisherArrayList;
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

    */
}
