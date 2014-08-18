package com.yooiistudios.news.model;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Dongheyon Jeong on in RSSTest from Yooii Studios Co., LTD. on 2014. 6. 27.
 *
 * MNRssFetchTask
 *  특정 url 에서 뉴스피드 데이터를 가져오는 클래스
 */
public class NLNewsFeedFetchTask extends AsyncTask<NLNewsFeedUrl, Void,
        NLNewsFeed> {
//    private String mRssUrl;
    private Context mContext;
    private NLNewsFeedUrl mFeedUrl;
    private OnFetchListener mOnFetchListener;

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final String ILLEGAL_CHARACTER_OBJ = Character.toString((char) 65532);

    public NLNewsFeedFetchTask(Context context, NLNewsFeedUrl feedUrl,
                               OnFetchListener onFetchListener) {
//        mRssUrl = rssUrl;
        mContext = context;
        mFeedUrl = feedUrl;
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
//        String urlStr = "http://news.google" +
//                ".com/news/url?sa=t&fd=R&ct2=us&usg=AFQjCNFZ1vtEWwtLC8sp3-aAk78i4WmMDA&clid=c3a7d30bb8a4878e06b80cf16b898331&cid=52778582001131&ei=OFXvU_i6CYKfkAWG1gE&url=http://www.usatoday.com/story/news/world/2014/08/16/huge-crowds-greet-pope-at-martyr-beatification/14154907/";
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
            feed = NLNewsFeedParser.read(conn.getInputStream());
            // 로컬 테스트를 위한 코드
//            feed = NLNewsFeedParser.read(mContext.getResources().getAssets().open("feeds.xml"));

            // 피드의 각 뉴스에 대해
            for (NLNews item : feed.getNewsList()) {
                addImageUrl(item);

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

    /**
     * 뉴스의 링크를 사용, 해당 링크 내에서 뉴스를 대표할 만한 이미지의 url을 추출한다.
     * 사용할 수 있는 이미지가 있는 경우 파라미터로 받은 news 인스턴스에 추가하고 아니면 아무것도
     * 하지 않는다.
     * 네트워크를 사용하므로 UI Thread에서는 부르지 말자.
     * @param news NLNews to set ImageUrl.
     */
    // Future use의 가능성이 있기 때문에 메서드로 빼놓음.
    private void addImageUrl(NLNews news) {
        // 뉴스의 링크를 읽음
        String originalLinkSource = null;
        try {
            originalLinkSource = NLNewsFeedUtil.requestHttpGet(
                    news.getLink());

        } catch(Exception e) {
            e.printStackTrace();
        }

        if (originalLinkSource != null) {
            // 링크를 읽었다면 거기서 이미지를 추출.
            // 이미지는 두 장 이상 필요하지 않을것 같아서 우선 한장만 뽑도록 해둠.
            // future use를 생각해 구조는 리스트로 만들어 놓음.
            String imgUrl = NLNewsFeedUtil.getImageUrl(
                    originalLinkSource);
            if (imgUrl != null) {
                news.addImageUrl(imgUrl);
            }
        }
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

        if (rssFeed != null && rssFeed.getRssItems() != null) {
            // success
            if (mOnFetchListener != null) {
                mOnFetchListener.onFetch(rssFeed);
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
        public void onFetch(NLNewsFeed rssFeed);
        public void onCancel();
        public void onError();
    }
}
