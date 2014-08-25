package com.yooiistudios.news.model.main;

import android.os.AsyncTask;

import com.yooiistudios.news.model.news.NLNews;
import com.yooiistudios.news.model.NLNewsFeedImageUrlFetchUtil;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLNewsImageUrlFetchTask
 *  뉴스의 이미지 url을 뽑아내는 태스크
 */
public class NLTopFeedNewsImageUrlFetchTask extends AsyncTask<Void, Void, String> {

    private NLNews mNews;
    private int mPosition;
    private OnTopFeedImageUrlFetchListener mListener;

    public NLTopFeedNewsImageUrlFetchTask(NLNews news, int position,
                                      OnTopFeedImageUrlFetchListener listener) {
        mNews = news;
        mPosition = position;
        mListener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return NLNewsFeedImageUrlFetchUtil.getImageUrl(mNews);
    }

    @Override
    protected void onPostExecute(String imageUrl) {
        super.onPostExecute(imageUrl);

        if (mListener != null) {
            if (imageUrl != null) {
                mListener.onTopFeedImageUrlFetchSuccess(mNews, imageUrl,
                        mPosition);
            } else {
                mListener.onTopFeedImageUrlFetchFail();
            }
        }
    }



    public interface OnTopFeedImageUrlFetchListener {
        public void onTopFeedImageUrlFetchSuccess(NLNews news, String url,
                                                  int position);
        public void onTopFeedImageUrlFetchFail();
    }
}
