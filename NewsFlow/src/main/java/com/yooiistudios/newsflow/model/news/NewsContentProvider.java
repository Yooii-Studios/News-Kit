package com.yooiistudios.newsflow.model.news;

import android.content.Context;
import android.support.annotation.Nullable;

import com.yooiistudios.newsflow.util.NLLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 11.
 *
 * NewsSelectPageUrlProvider
 *  뉴스 선택 화면에서 탭의 국가 및 뉴스피드 목록을 제공함
 */
public class NewsContentProvider {

    private static NewsContentProvider instance;
    private ArrayList<NewsProviderLanguage> mNewsProviderLanguageList;

    public static NewsContentProvider getInstance(Context context) {
        if (instance == null) {
            instance = new NewsContentProvider(context);
        }

        return instance;
    }

    private NewsContentProvider(Context context) {
        mNewsProviderLanguageList = new ArrayList<>();
        for (int i = 0; i < NewsProviderLangType.values().length; i++) {
            // int 를 raw id로 변환
            mNewsProviderLanguageList.add(parseNewsProvidersByResource(context,
                    NewsProviderLangType.valueOf(i).getResourceId()));
        }
    }

    public NewsTopic getNewsTopic(String languageCode, @Nullable String regionCode,
                                         int newsProviderId, int newsTopicId) {
        NewsProvider newsProvider = getNewsProvider(languageCode, regionCode, newsProviderId);
        return newsProvider.getNewsTopic(newsTopicId);
    }

    public NewsProvider getNewsProvider(NewsFeed newsFeed) {
        return getNewsProvider(newsFeed.getTopicLanguageCode(), newsFeed.getTopicRegionCode(),
                newsFeed.getTopicProviderId());
    }


    public NewsProvider getNewsProvider(String languageCodeToCompare,
                                        @Nullable String regionCodeToCompare,
                                        int providerIdToCompare) {
        for (NewsProviderLanguage newsProviderLanguage : mNewsProviderLanguageList) {
            String languageCode = newsProviderLanguage.languageCode;
            String regionCode = newsProviderLanguage.regionCode;
            if (languageCode.equalsIgnoreCase(languageCodeToCompare)
                    && (regionCode == null
                        || regionCodeToCompare == null
                        || regionCode.equalsIgnoreCase(regionCodeToCompare))) {

                for (NewsProvider newsProvider : newsProviderLanguage.newsProviders) {
                    if (newsProvider.id == providerIdToCompare) {
                        return newsProvider;
                    }
                }
            }
        }

        return null;
    }

    public NewsProviderLanguage getNewsRegion(int position) {
        if (position < mNewsProviderLanguageList.size()) {
            return mNewsProviderLanguageList.get(position);
        } else {
            return null;
        }
    }

    private static NewsProviderLanguage parseNewsProvidersByResource(Context context, int resourceId) {
        NLLog.now("parseNewsProvidersByResource");

        // raw id 에서 json 스트링을 만들고 JSONObject 로 변환
        try {
            InputStream file;
            file = context.getResources().openRawResource(resourceId);

            BufferedReader reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
            char[] buffer = new char[file.available()];
            reader.read(buffer);

            // 변환한 JSONObject 를 NewsProviders 로 변환
            JSONObject newsData = new JSONObject(new String(buffer));

            // NewsProviderLanguages
            NewsProviderLanguage newsProviderLanguage = new NewsProviderLanguage();
            newsProviderLanguage.englishLanguageName = newsData.getString("lang_name_english");
            newsProviderLanguage.regionalLanguageName = newsData.getString("lang_name_regional");
            newsProviderLanguage.languageCode = newsData.getString("lang_code");
            String regionCode = newsData.getString("region_code");
            if (!regionCode.equals("")) {
                newsProviderLanguage.regionCode = regionCode;
            }

            // NewsProviderCountries
            JSONArray newsProviderCountryArray = newsData.getJSONArray("countries");
            newsProviderLanguage.newsProviderCountries = parseNewsProviderCountries(
                    newsProviderCountryArray, newsProviderLanguage);

            /*
            JSONArray providersArray = newsData.getJSONArray("news_providers");
            for (int i = 0; i < providersArray.length(); i++) {
                JSONObject newsProviderObject = providersArray.getJSONObject(i);

                NewsProvider newsProvider = new NewsProvider();
                newsProvider.name = newsProviderObject.getString("provider_name");
                newsProvider.id = newsProviderObject.getInt("provider_id");

                newsProvider.languageCode = newsProviderLanguage.languageCode;
                newsProvider.regionCode = newsProviderLanguage.regionCode;

                JSONArray topicArray = newsProviderObject.getJSONArray("topics");
                for (int j = 0; j < topicArray.length(); j++) {
                    JSONObject topicObject = topicArray.getJSONObject(j);

                    NewsTopic newsTopic = new NewsTopic();
                    newsTopic.title = topicObject.getString("topic_name");
                    newsTopic.id = topicObject.getInt("topic_id");

                    newsTopic.languageCode = newsProviderLanguage.languageCode;
                    newsTopic.regionCode = newsProviderLanguage.regionCode;
                    newsTopic.newsProviderId = newsProvider.id;

                    String url = topicObject.getString("url");
                    newsTopic.newsFeedUrl = new NewsFeedUrl(url, NewsFeedUrlType.GENERAL);

                    if (topicObject.has("isDefault")) {
                        newsTopic.setDefault(Boolean.valueOf(topicObject.getString("isDefault")));
                    }

                    newsProvider.addNewsTopic(newsTopic);
                }

                newsProviderLanguage.newsProviders.add(newsProvider);
            }
            */

            // Test
            NLLog.now("lang: " + newsProviderLanguage.englishLanguageName);
            NLLog.now("lang_region: " + newsProviderLanguage.regionalLanguageName);
            NLLog.now("lang_code: " + newsProviderLanguage.languageCode);
            NLLog.now("region_code: " + newsProviderLanguage.languageCode);
            NLLog.now("newsProviders");

            for (NewsProviderCountry newsProviderCountry : newsProviderLanguage.newsProviderCountries) {
                NLLog.now("newsProviderCountry name: " + newsProviderCountry.countryLocalName);
                NLLog.now("newsProviderCountry code: " + newsProviderCountry.countryCode);

                for (NewsProvider newsProvider : newsProviderCountry.newsProviders) {
                    NLLog.now("provider name: " + newsProvider.name);

                    for (NewsTopic topic : newsProvider.getNewsTopicList()) {
                        NLLog.now("topic    title    : " + topic.title);
                        NLLog.now("topic    language    : " + topic.languageCode);
                        NLLog.now("topic    region      : " + topic.regionCode);
                        NLLog.now("topic    providerId  : " + topic.newsProviderId);
                        NLLog.now("topic    id          : " + topic.id);
                    }
                }
            }
            return newsProviderLanguage;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<NewsProviderCountry> parseNewsProviderCountries(
            JSONArray newsProviderCountryArray, NewsProviderLanguage newsProviderLanguage) throws JSONException {

        ArrayList<NewsProviderCountry> newsProviderCountries = new ArrayList<>();

        for (int i = 0; i < newsProviderCountryArray.length(); i++) {
            JSONObject newsProviderCountryObject = newsProviderCountryArray.getJSONObject(i);
            NLLog.now(newsProviderCountryObject.toString());
            NewsProviderCountry newsProviderCountry = new NewsProviderCountry();
            newsProviderCountry.languageCode = newsProviderLanguage.languageCode;
            newsProviderCountry.regionCode = newsProviderLanguage.regionCode;
            newsProviderCountry.countryLocalName = newsProviderCountryObject.getString("country_name");
            newsProviderCountry.countryCode = newsProviderCountryObject.getString("country_code");
            newsProviderCountry.newsProviders = parseNewsProviders(
                    newsProviderCountryObject.getJSONArray("news_providers"), newsProviderLanguage);

            newsProviderCountries.add(newsProviderCountry);
        }
        return newsProviderCountries;
    }

    private static ArrayList<NewsProvider> parseNewsProviders(JSONArray newsProviderArray,
                                  NewsProviderLanguage newsProviderLanguage) throws JSONException {

        ArrayList<NewsProvider> newsProviders = new ArrayList<>();

        for (int i = 0; i < newsProviderArray.length(); i++) {
            JSONObject newsProviderObject = newsProviderArray.getJSONObject(i);

            NewsProvider newsProvider = new NewsProvider();
            newsProvider.languageCode = newsProviderLanguage.languageCode;
            newsProvider.regionCode = newsProviderLanguage.regionCode;

            newsProvider.id = newsProviderObject.getInt("provider_id");
            newsProvider.name = newsProviderObject.getString("provider_name");

            // News Topics
            JSONArray topicArray = newsProviderObject.getJSONArray("topics");
            for (int j = 0; j < topicArray.length(); j++) {
                JSONObject topicObject = topicArray.getJSONObject(j);

                NewsTopic newsTopic = new NewsTopic();
                newsTopic.title = topicObject.getString("topic_name");
                newsTopic.id = topicObject.getInt("topic_id");
                newsTopic.languageCode = newsProviderLanguage.languageCode;
                newsTopic.regionCode = newsProviderLanguage.regionCode;
                newsTopic.newsProviderId = newsProvider.id;

                String url = topicObject.getString("url");
                newsTopic.newsFeedUrl = new NewsFeedUrl(url, NewsFeedUrlType.GENERAL);

                if (topicObject.has("isDefault")) {
                    newsTopic.setDefault(Boolean.valueOf(topicObject.getString("isDefault")));
                }
                newsProvider.addNewsTopic(newsTopic);
            }
            newsProviders.add(newsProvider);
        }
        return newsProviders;
    }
}
