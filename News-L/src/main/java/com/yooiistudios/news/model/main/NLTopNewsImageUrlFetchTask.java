package com.yooiistudios.news.model.main;

import android.os.AsyncTask;

import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.model.NLNewsFeedImageUrlFetchUtil;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 18.
 *
 * NLNewsImageUrlFetchTask
 *  뉴스의 이미지 url을 뽑아내는 태스크
 */
public class NLTopNewsImageUrlFetchTask extends AsyncTask<Void, Void, String> {

    private NLNews mNews;
    private OnTopImageUrlFetchListener mListener;

    public NLTopNewsImageUrlFetchTask(NLNews news, OnTopImageUrlFetchListener
            listener) {
        mNews = news;
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
                mListener.onTopImageUrlFetchSuccess(mNews, imageUrl);
            } else {
                mListener.onTopImageUrlFetchFail();
            }
        }
    }



    public interface OnTopImageUrlFetchListener {
        public void onTopImageUrlFetchSuccess(NLNews news, String url);
        public void onTopImageUrlFetchFail();
    }
}
