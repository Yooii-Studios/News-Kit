package com.yooiistudios.news.feed;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;

/**
 * Created by Dongheyon Jeong on in RSSTest from Yooii Studios Co., LTD. on 2014. 6. 27.
 *
 * MNRssFetchTask
 *  특정 url 에서 뉴스피드 데이터를 가져오는 클래스
 */
public class NLNewsFeedFetchTask extends AsyncTask<NLNewsFeedUrl, Void, RssFeed> {
//    private String mRssUrl;
    private Context mContext;
    private NLNewsFeedUrl mFeedUrl;
    private OnFetchListener mOnFetchListener;

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final String ILLEGAL_CHARACTER_OBJ = Character.toString((char) 65532);

    public NLNewsFeedFetchTask(Context context, NLNewsFeedUrl feedUrl,
                               OnFetchListener onFetchListener) {
//        mRssUrl = rssUrl;
        mFeedUrl = feedUrl;
        mOnFetchListener = onFetchListener;
    }

    @Override
    protected RssFeed doInBackground(NLNewsFeedUrl... args) {

//        if (args == null || args.length <= 0) {
//            //error
//            return null;
//        }
//        MNNewsFeedUrl feedUrl = args[0];

        if (!mFeedUrl.getType().equals(NLNewsFeedUrlType.CUSTOM)) {
            // 디폴트 세팅을 사용할 경우 패널단에서 언어설정을 감지 못하므로 무조건 현재 언어의
            // 디폴트 url을 가져온다.
            mFeedUrl = NLNewsFeedUtil.getDefaultFeedUrl(mContext);
        }

        RssFeed feed = null;
        try {
            URL url = new URL(mFeedUrl.getUrl());
//            InputStream is = url.openStream();
            URLConnection conn = url.openConnection();

            feed = RssReader.read(conn.getInputStream());

            for (RssItem item : feed.getRssItems()) {
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
    protected void onPostExecute(RssFeed rssFeed) {
        super.onPostExecute(rssFeed);
        if (isCancelled()) {
            if (mOnFetchListener != null) {
                mOnFetchListener.onCancel();
            }
            return;
        }

        if (rssFeed != null && rssFeed.getRssItems() != null) {
            // success
            if (mOnFetchListener != null) {
                mOnFetchListener.onFetch(rssFeed);
            }

            // flurry
//            Map<String, String> params = new HashMap<String, String>();
//            params.put(MNFlurry.NEWS,
//                    mFeedUrl.getType().equals(NLNewsFeedUrlType.CUSTOM) ?
//                    "Custom RSS" : "Default News");
//            FlurryAgent.logEvent(MNFlurry.PANEL, params);

//            ArrayList<RssItem> rssItems = rssFeed.getRssItems();
//            for (RssItem rssItem : rssItems) {
//                Log.i("RSS Reader", rssItem.getTitle());
//            }
        }
        else {
            // error
            if (mOnFetchListener != null) {
                mOnFetchListener.onError();
            }
        }
    }

    public interface OnFetchListener {
        public void onFetch(RssFeed rssFeed);
        public void onCancel();
        public void onError();
    }
}
