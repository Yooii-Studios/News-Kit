package com.yooiistudios.newsflow.ui.adapter;

import android.support.v17.leanback.widget.ObjectAdapter;

import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.reference.CardPresenter;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * NewsFeedAdapter
 *  메인화면 어뎁터
 */
public class NewsFeedAdapter extends ObjectAdapter {
    private ArrayList<News> mNewsList;

    public NewsFeedAdapter(CardPresenter presenter, NewsFeed newsFeed) {
        super(presenter);
        mNewsList = newsFeed.getNewsList();
    }

    @Override
    public int size() {
        return mNewsList.size();
    }

    @Override
    public Object get(int position) {
        return mNewsList.get(position);
    }

    public void applyNewsImageAt(String imageUrl, int newsIndex) {
        mNewsList.get(newsIndex).setImageUrl(imageUrl);
        notifyItemRangeChanged(newsIndex, 1);
    }
}
