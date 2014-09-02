package com.yooiistudios.news.model.news;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.language.Language;
import com.yooiistudios.news.model.language.LanguageType;
import com.yooiistudios.news.util.NLLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dongheyon Jeong on in morning-kit from Yooii Studios Co., LTD. on 2014. 7. 3.
 *
 * MNNewsFeedUtil
 *  뉴스피드에 관한 전반적인 유틸 클래스
 */
public class NewsFeedUtils {
    private static final String KEY_HISTORY = "KEY_HISTORY";
    private static final int MAX_HISTORY_SIZE = 10;
    public static final String PREF_NEWS_FEED = "PREF_NEWS_FEED";

    private static final String NEWS_PROVIDER_YAHOO_JAPAN = "Yahoo!ニュース";

    private NewsFeedUtils() { throw new AssertionError("You MUST not create this class!"); }

    public static NewsFeedUrl getDefaultFeedUrl(Context context) {

        String feedUrl;
        NewsFeedUrlType urlType;

        /*
        NLLanguageType type = NLLanguage.getCurrentLanguageType(context);
        switch(type) {
            case ENGLISH:
                feedUrl = "http://news.google.com/news?cf=all&ned=us&hl=en&output=rss";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
            case KOREAN:
                feedUrl = "http://news.google.com/news?cf=all&ned=kr&hl=ko&output=rss";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
            case JAPANESE:
                feedUrl = "http://rss.dailynews.yahoo.co.jp/fc/rss.xml";
                urlType = NLNewsFeedUrlType.YAHOO;
                break;
            case TRADITIONAL_CHINESE:
                feedUrl = "http://news.google.com/news?cf=all&ned=tw&hl=zh-TW&output=rss";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
            case SIMPLIFIED_CHINESE:
                feedUrl = "http://news.google.com/news?cf=all&ned=cn&hl=zh-CN&output=rss";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
            case RUSSIAN:
                feedUrl = "http://news.google.com/news?cf=all&ned=ru_ru&hl=ru&output=rss";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
            default:
                feedUrl = "";
                urlType = NLNewsFeedUrlType.GOOGLE;
                break;
        }
        */

        // 일단 CNN 주요 뉴스로 고정
        feedUrl = "http://feeds2.feedburner.com/time/topstories";
        urlType = NewsFeedUrlType.GENERAL;

//        feedUrl = "http://sweetpjy.tistory.com/rss";
//        feedUrl = "http://www.cnet.com/rss/iphone-update/";

        return new NewsFeedUrl(feedUrl, urlType);
    }

//    public static String getRssFeedJsonString(RssFeed feed) {
//        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy
//                () {
//                    @Override
//                    public boolean shouldSkipField(FieldAttributes f) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean shouldSkipClass(Class<?> clazz) {
//                        return (clazz == RssItem.class);
//                    }
//                }).serializeNulls().create().toJson(feed);
//    }
//    public static String getRssItemArrayListString(ArrayList<RssItem>
//                                                           itemList) {
//        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy
//                () {
//            @Override
//            public boolean shouldSkipField(FieldAttributes f) {
//                return false;
//            }
//
//            @Override
//            public boolean shouldSkipClass(Class<?> clazz) {
//                return (clazz == RssFeed.class);
//            }
//        }).serializeNulls().create().toJson(itemList);
//    }

    public static void addUrlToHistory(Context context, String url) {
        ArrayList<String> urlList = getUrlHistory(context);

        // if list contains url, remove and add it at 0th index.
        if (urlList.contains(url)) {
            urlList.remove(url);
        }
        // put recent url at 0th index.
        urlList.add(0, url);

        // remove last history if no room.
        if (urlList.size() > MAX_HISTORY_SIZE) {
            urlList.remove(urlList.size()-1);
        }

        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NEWS_FEED, Context.MODE_PRIVATE);

        prefs.edit().putString(KEY_HISTORY, new Gson().toJson(urlList)).apply();
    }

    public static ArrayList<String> getUrlHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NEWS_FEED, Context.MODE_PRIVATE);
        String historyJsonStr = prefs.getString(KEY_HISTORY, null);

        if (historyJsonStr != null) {
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            return new Gson().fromJson(historyJsonStr, type);
        } else {
            return new ArrayList<String>();
        }
    }

    /**
     *
     * @param news RssItem
     * @param type NLNewsFeedUrlType
     * @return retval
     * retval[0] : title.
     * retval[1] : publisher or null if there's no publisher info.
     *
     */
    public static String[] getTitleAndPublisherName(News news,
                                          NewsFeedUrlType type) {
        String title = news.getTitle();
        String newTitle;
        String publisher;
        switch (type) {
            case GOOGLE:
                final String delim = " - ";
                int idx = title.lastIndexOf(delim);

                int titleStartIdx = 0;
                int pubStartIdx = idx + delim.length();
                int pubEndIdx = title.length();

                if (idx >= 0 &&
                        idx >= titleStartIdx &&
                        pubEndIdx >= pubStartIdx) {
                // title.length() >= delim.length()
                    newTitle = title.substring(titleStartIdx, idx);
                    publisher = "- " + title.substring(pubStartIdx, pubEndIdx);
                } else {
                    newTitle = title;
                    publisher = null;
                }
                break;

            case YAHOO:
                newTitle = title;
                publisher = NEWS_PROVIDER_YAHOO_JAPAN;
                break;

            case GENERAL:
            default:
                newTitle = title;
                publisher = null;
                break;
        }

        return new String[]{newTitle, publisher};
    }

    public static String getFeedTitle(Context context) {
        LanguageType currentLanguageType = Language.getCurrentLanguageType(context);

        String provider;

        if (currentLanguageType.equals(LanguageType.JAPANESE)) {
            provider = NEWS_PROVIDER_YAHOO_JAPAN;
        }
        else {
            provider = null;
        }

        return provider;
    }


    public static ArrayList<String> getImgSrcList(String str) {
//        Pattern nonValidPattern = Pattern
//                .compile("<img[^>]*src=[\"']?([^>\"']+)[\"']?[^>]*>");
        Pattern nonValidPattern = Pattern
                .compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");


        ArrayList<String> result = new ArrayList<String>();
        Matcher matcher = nonValidPattern.matcher(str);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }


    /**
     * Html 페이지를 대표하는 이미지를 추출한다.
     * @param source Html in plain String.
     * @return One image url which represents the page. May be null if no
     * appropriate image url.
     */
    public static String getImageUrl(String source) {
        long startMilli;
        long endMilli;

        startMilli = System.currentTimeMillis();
        Document doc = Jsoup.parse(source);
        endMilli = System.currentTimeMillis();
        NLLog.i("getImageUrl", "Jsoup.parse(source) : " +
                (endMilli - startMilli));

        // og:image
        startMilli = System.currentTimeMillis();
        Elements ogImgElems = doc.select("meta[property=og:image]");
        endMilli = System.currentTimeMillis();
        NLLog.i("getImageUrl", "doc.select(\"meta[property=og:image]\") : " +
                (endMilli - startMilli));

        String imgUrl = null;

        if (ogImgElems.size() > 0) {
            imgUrl = ogImgElems.get(0).attr("content");
        } else {
            // 워드프레스처럼 entry-content 클래스를 쓰는 경우의 예외처리
            Elements elms = doc.getElementsByClass("entry-content");

            if (elms.size() > 0) {
                Elements imgElms = elms.get(0).getElementsByTag("img");

                if (imgElms.size() > 0) {
                    imgUrl = imgElms.get(0).attr("src");
                }

            }

            // TODO 기타 예외처리가 더 들어가야 할듯..
        }

        return imgUrl;
    }
    public static String requestHttpGet(String url) throws Exception {
        // HttpClient 생성
        HttpClient httpclient = new DefaultHttpClient();

        // HttpGet생성
        HttpGet httpget = new HttpGet(url);

        System.out.println("executing request " + httpget.getURI());
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
        httpget.abort();
        httpclient.getConnectionManager().shutdown();

        return null;
    }

    public static String requestHttpGet_(String url) throws Exception {
        // HttpClient 생성
        HttpClient httpclient = new DefaultHttpClient();
        try {
            // HttpGet생성
            HttpGet httpget = new HttpGet(url);

            System.out.println("executing request " + httpget.getURI());
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));

                String line = "";
                while ((line = rd.readLine()) != null) {
                    if (line.contains("og:image")) {
                        return line;
                    } else if (line.contains("</head>")) {
                        return null;
                    }
                }
            }
            httpget.abort();
//            System.out.println("----------------------------------------");
            httpclient.getConnectionManager().shutdown();

            return null;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return null;
    }

    /**
     * 일반적인 Get Method를 이용한 Http request를 날린다.
     * @param url Url to send request
     * @return Html in plain String.
     * @throws Exception
     */
    public static String requestHttpGet__(String url) throws Exception {
        long startMilli;
        long endMilli;

        startMilli = System.currentTimeMillis();
        HttpURLConnection con =
                (HttpURLConnection)new URL(url).openConnection();
        endMilli = System.currentTimeMillis();
        NLLog.i("performance", "open connection : " +
                (endMilli - startMilli));
        con.setRequestMethod("GET");

        startMilli = System.currentTimeMillis();
        int responseCode = con.getResponseCode();
        endMilli = System.currentTimeMillis();
        NLLog.i("performance", "getting response code : " +
                (endMilli - startMilli));

        if (responseCode != 200) {
            return null;
        }

        startMilli = System.currentTimeMillis();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder responseBuilder = new StringBuilder();
        endMilli = System.currentTimeMillis();
        NLLog.i("performance", "make buffered reader with input stream: " +
                (endMilli - startMilli));

        startMilli = System.currentTimeMillis();
        while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine);
        }
        in.close();
        endMilli = System.currentTimeMillis();
        NLLog.i("performance", "read with while loop : " +
                (endMilli - startMilli));

        startMilli = System.currentTimeMillis();
        String responseStr = responseBuilder.toString();
        endMilli = System.currentTimeMillis();
        NLLog.i("performance", "reponseBuilder to responseStr : " +
                (endMilli - startMilli));

        return responseStr;
    }

    public static Bitmap getDummyNewsImage(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.news_dummy);
    }

    /**
     * Color used to Main Top news image and Dummy image
     * @return
     */
    public static int getGrayFilterColor() {
        return Color.argb(127, 16, 16, 16);
    }
    public static int getDummyImageFilterColor() {
        return NewsFeedUtils.getGrayFilterColor();
    }

    public static int getMainBottomDefaultBackgroundColor() {
        return Color.argb(200, 16, 16, 16);
    }
}
