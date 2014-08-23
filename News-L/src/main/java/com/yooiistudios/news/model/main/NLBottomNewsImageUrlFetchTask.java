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
public class NLBottomNewsImageUrlFetchTask extends AsyncTask<Void, Void, String> {

    private NLNews mNews;
    private int mPosition;
    private OnBottomImageUrlFetchListener mListener;

    public NLBottomNewsImageUrlFetchTask(NLNews news, int position,
                                         OnBottomImageUrlFetchListener listener) {
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
                mListener.onBottomImageUrlFetchSuccess(mNews, imageUrl, mPosition);
            } else {
                mListener.onBottomImageUrlFetchFail();
            }
        }
    }



    public interface OnBottomImageUrlFetchListener {
        public void onBottomImageUrlFetchSuccess(NLNews news, String url,
                                                 int position);
        public void onBottomImageUrlFetchFail();
    }
}
