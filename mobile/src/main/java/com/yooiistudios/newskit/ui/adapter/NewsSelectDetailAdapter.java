package com.yooiistudios.newskit.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsTopic;
import com.yooiistudios.newskit.core.news.curation.NewsProvider;
import com.yooiistudios.newskit.core.news.curation.NewsProviderCountry;
import com.yooiistudios.newskit.util.TypefaceUtils;

import java.util.ArrayList;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 25.
 *
 * NewsSelectDetailAdapter
 *  뉴스 선택 디테일 프래그먼트에서 활용될 어댑터
 */
public class NewsSelectDetailAdapter extends BaseAdapter {
    private Context mContext;
    private NewsProvider mNewsProvider;
    private NewsProviderCountry mNewsProviderCountry;
    private NewsFeed mCurrentNewsFeed;
    private ArrayList<String> mTitles;

    // 뉴스 토픽용
    public NewsSelectDetailAdapter(Context context, NewsProvider newsProvider,
                                   NewsFeed currentNewsFeed) {
        mContext = context;
        mNewsProvider = newsProvider;
        mCurrentNewsFeed = currentNewsFeed;
        initTopicNamesWithProvider();
    }

    // 뉴스 프로바이더용
    public NewsSelectDetailAdapter(Context context, NewsProviderCountry newsProviderCountry,
                                   NewsFeed currentNewsFeed) {
        mContext = context;
        mNewsProviderCountry = newsProviderCountry;
        mCurrentNewsFeed = currentNewsFeed;
        initTopicNamesWithCountry();
    }

    private void initTopicNamesWithProvider() {
        mTitles = new ArrayList<>();
        for (NewsTopic newsTopic : mNewsProvider.getNewsTopicList()) {
            mTitles.add(newsTopic.title);
        }
    }

    private void initTopicNamesWithCountry() {
        mTitles = new ArrayList<>();
        for (NewsProvider newsProvider : mNewsProviderCountry.newsProviders) {
            mTitles.add(newsProvider.name);
        }
    }

    @Override
    public int getCount() {
        if (mTitles != null) {
            return mTitles.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) LayoutInflater.from(mContext)
                .inflate(R.layout.news_select_detail_simple_item, parent, false);

        textView.setText(mTitles.get(position));
        textView.setTypeface(TypefaceUtils.getRegularTypeface(mContext));

        // 추가: 현재 선택한 뉴스피드와 같은 토픽이거나 언론사일 경우 하이라이트 처리
        if (mNewsProvider != null) {
            if (mNewsProvider.languageCode.equals(mCurrentNewsFeed.getTopicLanguageCode()) &&
                    mNewsProvider.countryCode.equals(mCurrentNewsFeed.getTopicCountryCode()) &&
                    mNewsProvider.id == mCurrentNewsFeed.getTopicProviderId() &&
                            mNewsProvider.getNewsTopicList().get(position).id == mCurrentNewsFeed.getTopicId()) {
                textView.setTextColor(mContext.getResources().getColor(R.color.news_select_color_accent));
            }
        } else if (mNewsProviderCountry != null) {
            if (mNewsProviderCountry.languageCode.equals(mCurrentNewsFeed.getTopicLanguageCode()) &&
                    mNewsProviderCountry.countryCode.equals(mCurrentNewsFeed.getTopicCountryCode()) &&
                    mNewsProviderCountry.newsProviders.get(position).id == mCurrentNewsFeed.getTopicProviderId()) {
                textView.setTextColor(mContext.getResources().getColor(R.color.news_select_color_accent));
            }
        }
        return textView;
    }
}
