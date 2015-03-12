package com.yooiistudios.newsflow.core.news.util;

import android.util.Base64;

import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.NewsFeedUrl;
import com.yooiistudios.newsflow.core.news.NewsTopic;
import com.yooiistudios.newsflow.core.news.RssFetchable;
import com.yooiistudios.newsflow.core.util.ObjectConverter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 12.
 *
 * RssFetchableConverter
 *  RssFetchable 객체를 다른 형태로 변형해주는 유틸
 */
public class RssFetchableConverter {
    private RssFetchableConverter() {
        throw new AssertionError("You MUST NOT create the instance of this class!!");
    }

    public static String newsFeedsToBase64String(List<NewsFeed> newsFeeds)
            throws RssFetchableConvertException {
        try {
            JSONArray jsonArray = new JSONArray();
            for (NewsFeed newsFeed : newsFeeds) {
                if (newsFeed.hasTopicInfo()) {
                    NewsTopic topic = newsFeed.createNewsTopicInfo();
                    String encodedTopic = encodeTopic(topic);
                    jsonArray.put(encodedTopic);
                } else {
                    NewsFeedUrl newsFeedUrl = newsFeed.getNewsFeedUrl();
                    String encodedFeedUrl = encodeNewsFeedUrl(newsFeedUrl);
                    jsonArray.put(encodedFeedUrl);
                }
            }
            byte[] dataBytes = jsonArray.toString().getBytes();
            return Base64.encodeToString(dataBytes, Base64.NO_WRAP);
        } catch(IOException e) {
            throw new RssFetchableConvertException("Error occurred while encoding RssFetchable.");
        }
    }

    public static List<RssFetchable> base64StringToRssFetchables(String base64String)
            throws RssFetchableConvertException {
        try {
            String decodedData = decodeData(base64String);
            JSONArray jsonArray = new JSONArray(decodedData);

            List<RssFetchable> fetchables = new ArrayList<>();
            int dataCount = jsonArray.length();
            for (int i = 0; i < dataCount; i++) {
                RssFetchable fetchable = decodeRssFetchable(jsonArray, i);
                fetchables.add(fetchable);
            }
            return fetchables;
        } catch (JSONException|ClassNotFoundException|IOException e) {
            throw new RssFetchableConvertException("Error occurred while decoding data.");
        }
    }

    private static String encodeNewsFeedUrl(NewsFeedUrl newsFeedUrl) throws IOException {
        return Base64.encodeToString(ObjectConverter.toByteArray(newsFeedUrl), Base64.NO_WRAP);
    }

    private static String encodeTopic(NewsTopic topic) throws IOException {
        return Base64.encodeToString(ObjectConverter.toByteArray(topic), Base64.NO_WRAP);
    }

    private static RssFetchable decodeRssFetchable(JSONArray jsonArray, int i) throws JSONException, IOException, ClassNotFoundException {
        String encodedRssFetchable = (String)jsonArray.get(i);
        byte[] bytesData = Base64.decode(encodedRssFetchable, Base64.NO_WRAP);
        return (RssFetchable) ObjectConverter.fromByteArray(bytesData);
    }

    private static String decodeData(String base64String) {
        byte[] dataBytes = Base64.decode(base64String, Base64.NO_WRAP);
        return new String(dataBytes);
    }

    public static class RssFetchableConvertException extends Exception {
        public RssFetchableConvertException() {
        }

        public RssFetchableConvertException(String detailMessage) {
            super(detailMessage);
        }
    }
}
