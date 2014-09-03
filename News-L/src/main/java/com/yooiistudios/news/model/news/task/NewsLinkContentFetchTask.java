package com.yooiistudios.news.model.news.task;

import android.os.AsyncTask;

import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeedUtils;
import com.yooiistudios.news.util.NLLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 2.
 *
 * NewsLinkContentFetchTask
 *  News.getLink()의 컨텐츠를 fetch함
 */
public class NewsLinkContentFetchTask extends AsyncTask<Void, Void, String> {

    private News mNews;
    private OnContentFetchListener mOnContentFetchListener;

    public interface OnContentFetchListener {
        public void onContentFetch(String content);
    }

    public NewsLinkContentFetchTask(News news, OnContentFetchListener listener) {
        mNews = news;
        mOnContentFetchListener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
//        try {
//            CharSequence cs = NewsFeedUtils.requestHttpGet(mNews.getLink());
//
//            Elements elements = Jsoup.parse(cs.toString()).body().select("*");
//
//            StringBuilder stringBuilder = new StringBuilder();
//            for (Element elm : elements) {
//                stringBuilder.append(elm.ownText());
//            }
//
//            String bodyStr = stringBuilder.toString();
//            NLLog.now(bodyStr);
//            return bodyStr;
//        } catch(Exception e) {
//            e.printStackTrace();
//        }

        return "";
//        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (mOnContentFetchListener != null) {
            mOnContentFetchListener.onContentFetch(result);
        }
    }
}
