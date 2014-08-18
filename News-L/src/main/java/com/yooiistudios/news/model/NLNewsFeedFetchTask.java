package com.yooiistudios.news.model;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;

import com.yooiistudios.news.common.log.NLLog;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Dongheyon Jeong on in RSSTest from Yooii Studios Co., LTD. on 2014. 6. 27.
 *
 * MNRssFetchTask
 *  특정 url 에서 뉴스피드 데이터를 가져오는 클래스
 */
public class NLNewsFeedFetchTask extends AsyncTask<NLNewsFeedUrl, Void,
        NLNewsFeed> {
    private static final String TAG = NLNewsFeedFetchTask.class.getName();
//    private String mRssUrl;
    private Context mContext;
    private NLNewsFeedUrl mFeedUrl;
    private OnFetchListener mOnFetchListener;
    private int mFetchLimit;

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final String ILLEGAL_CHARACTER_OBJ = Character.toString((char) 65532);

    /**
     *
     * @param context
     * @param feedUrl
     * @param fetchLimit Number of news to fetch. Should be > 0 and
     *                   < list.size()
     * @param onFetchListener
     */
    public NLNewsFeedFetchTask(Context context, NLNewsFeedUrl feedUrl,
                               int fetchLimit,
                               OnFetchListener onFetchListener) {
//        mRssUrl = rssUrl;
        mContext = context;
        mFeedUrl = feedUrl;
        mFetchLimit = fetchLimit;
        mOnFetchListener = onFetchListener;
    }

    @Override
    protected NLNewsFeed doInBackground(NLNewsFeedUrl... args) {
        if (!mFeedUrl.getType().equals(NLNewsFeedUrlType.GENERAL)) {
            // 디폴트 세팅을 사용할 경우 패널단에서 언어설정을 감지 못하므로 무조건 현재 언어의
            // 디폴트 url을 가져온다.
            mFeedUrl = NLNewsFeedUtil.getDefaultFeedUrl(mContext);
        }

        // 테스트용 Rss url 세팅
//        urlStr = "http://kwang82.hankyung.com/2014/08/100_5.html";
//        urlStr = "http://deepseadk.egloos.com/1828225";
//        urlStr = "http://www.usatoday.com/story/news/world/2014/08/16/huge-crowds-greet-pope-at-martyr-beatification/14154907/";
//
//        mFeedUrl = new NLNewsFeedUrl(urlStr, NLNewsFeedUrlType.CUSTOM);

        NLNewsFeed feed = null;
        try {
            // 피드 주소로 커넥션 열기
            URL url = new URL(mFeedUrl.getUrl());
            URLConnection conn = url.openConnection();

            // RSS 파싱

            long startMilli;
            long endMilli;

            startMilli = System.currentTimeMillis();
            feed = NLNewsFeedParser.read(conn.getInputStream());
            endMilli = System.currentTimeMillis();
            NLLog.i("performance", "NLNewsFeedParser.read" +
                    (endMilli - startMilli));
            // 퍼포먼스 개선 여지 있음.
            // 로컬 테스트를 위한 코드
//            feed = NLNewsFeedParser.read(mContext.getResources().getAssets().open("feeds.xml"));

            // shuffle and trim size
            Collections.shuffle(feed.getNewsList(), new Random(System.nanoTime()));
            if (mFetchLimit > 0 && mFetchLimit < feed.getNewsList().size()) {
                ArrayList<NLNews> trimmedNewsList =
                        new ArrayList<NLNews>(feed.getNewsList().subList(0,
                                mFetchLimit));
                feed.setNewsList(trimmedNewsList);
            }

            // 피드의 각 뉴스에 대해
            for (NLNews item : feed.getNewsList()) {

                // 피드의 본문에서 텍스트만 걸러내는 작업
                String desc = item.getDescription();
                if (desc != null) {

                    String strippedDesc = Html.fromHtml(desc.substring(0,
                            desc.length())).toString();

                    int length = strippedDesc.length() > MAX_DESCRIPTION_LENGTH ?
                            MAX_DESCRIPTION_LENGTH : strippedDesc.length();
                    String refinedDesc = new StringBuilder(strippedDesc).substring
                            (0, length).replaceAll(ILLEGAL_CHARACTER_OBJ, "")
                            .replaceAll("\n", " ");
                    item.setDescription(refinedDesc);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return feed;
    }

    @Override
    protected void onPostExecute(NLNewsFeed rssFeed) {
        super.onPostExecute(rssFeed);
        if (isCancelled()) {
            if (mOnFetchListener != null) {
                mOnFetchListener.onCancel();
            }
            return;
        }

        if (rssFeed != null && rssFeed.getNewsList() != null) {
            NLLog.i(TAG, "Fetched news count : " + rssFeed.getNewsList().size());
            // success
            if (mOnFetchListener != null) {
                mOnFetchListener.onSuccess(rssFeed);
            }
        }
        else {
            // error
            if (mOnFetchListener != null) {
                mOnFetchListener.onError();
            }
        }
    }

    public interface OnFetchListener {
        public void onSuccess(NLNewsFeed rssFeed);
        public void onCancel();
        public void onError();
    }
}
