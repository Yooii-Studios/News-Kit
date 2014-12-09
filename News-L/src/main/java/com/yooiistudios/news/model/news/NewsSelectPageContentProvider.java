package com.yooiistudios.news.model.news;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

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

                    NewsTopic newsTopic = new NewsTopic();
                    newsTopic.setTitle(categoryObject.getString("category_name"));

                    String url = categoryObject.getString("url");
                    newsTopic.setNewsFeedUrl(new NewsFeedUrl(url, NewsFeedUrlType.GENERAL));

                    if (categoryObject.has("isDefault")) {
                        newsTopic.setDefault(Boolean.valueOf(categoryObject.getString("isDefault")));
                    }

                    newsPublisher.addNewsTopic(newsTopic);
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
}
