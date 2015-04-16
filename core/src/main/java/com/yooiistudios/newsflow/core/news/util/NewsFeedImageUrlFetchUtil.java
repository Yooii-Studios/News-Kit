package com.yooiistudios.newsflow.core.news.util;

import com.yooiistudios.newsflow.core.news.News;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 21.
 *
 * NLNewsFeedImageUrlFetchUtil
 *  뉴스에서 이미지를 가져옴
 */
public class NewsFeedImageUrlFetchUtil {
    private static final int TIMEOUT_MILLI = 5000;
    /**
     * 뉴스의 링크를 사용, 해당 링크 내에서 뉴스를 대표할 만한 이미지의 url 을 추출한다.
     * 사용할 수 있는 이미지가 있는 경우 파라미터로 받은 news 인스턴스에 추가하고 아니면 아무것도
     * 하지 않는다.
     * 네트워크를 사용하므로 UI Thread 에서는 부르지 말자.
     * @param news NLNews to set ImageUrl. May be null if there's no image src.
     */
    // Future use 의 가능성이 있기 때문에 메서드로 빼놓음.
    public static String getImageUrl(News news) {
        // 뉴스의 링크를 읽음
        String originalLinkSource = null;
        try {
            originalLinkSource = requestHttpGet_(news.getLink());

        } catch(Exception e) {
//            e.printStackTrace();
        }

        String imgUrl = "";
        if (originalLinkSource != null) {
            // 링크를 읽었다면 거기서 이미지를 추출.
            // 이미지는 두 장 이상 필요하지 않을것 같아서 우선 한장만 뽑도록 해둠.
            // future use 를 생각해 구조는 리스트로 만들어 놓음.
            imgUrl = getImageUrlFromRssContent(originalLinkSource);
        }

        return imgUrl;
    }


    /**
     * Html 페이지를 대표하는 이미지를 추출한다.
     * @param source Html in plain String.
     * @return One image url which represents the page.
     */
    private static String getImageUrlFromRssContent(String source) {
        Document doc = Jsoup.parse(source);

        // og:image
        Elements ogImgElements = doc.select("meta[property=og:image]");

        String imgUrl = "";

        if (ogImgElements.size() > 0) {
            imgUrl = ogImgElements.get(0).attr("content");
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

    private static String requestHttpGet_(String url) throws Exception {
        // HttpClient 생성
        HttpClient httpclient = new DefaultHttpClient();
        HttpParams params = httpclient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_MILLI);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT_MILLI);
        try {
            // HttpGet 생성
            HttpGet httpget = new HttpGet(url);

            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));

                String line;
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return null;
    }
}
