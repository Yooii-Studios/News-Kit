package com.yooiistudios.newsflow.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.curation.NewsProvider;
import com.yooiistudios.newsflow.core.news.curation.NewsProviderCountry;
import com.yooiistudios.newsflow.util.TypefaceUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * NewsSelectRecyclerViewAdapter
 *  한 국가의 뉴스 피드 리스트를 보여주는 리사이클러뷰의 어댑터
 */
public class NewsSelectRecyclerAdapter extends
        RecyclerView.Adapter<NewsSelectRecyclerAdapter.NewsSelectViewHolder> {

    private ArrayList<NewsProviderCountry> mNewsProviderCountries;
    private OnSelectionListener mListener;
    private NewsFeed mNewsFeed; // 현재 선택된 뉴스피드 표시용

    public interface OnSelectionListener {
        void onSelectNewsProvider(NewsProvider newsProvider);
        void onSelectNewsProviderCountry(NewsProviderCountry newsProviderCountry);
    }

    public NewsSelectRecyclerAdapter(ArrayList<NewsProviderCountry> newsProviderCountries,
                                     NewsFeed newsFeed) {
        mNewsProviderCountries = newsProviderCountries;
        mNewsFeed = newsFeed;
    }

    @Override
    public NewsSelectViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View v = LayoutInflater.from(context).inflate(
                R.layout.news_select_recycler_view_item, viewGroup, false);
        v.setTag(i);
        return new NewsSelectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NewsSelectViewHolder newsSelectViewHolder, int i) {
        // 국가가 한 곳 뿐이라면 바로 Provider 를 보여 주고, 아니라면 국가를 보여 주기
        // 기획 추가: 현재 뉴스를 체크해서 해당 뉴스 프로바이더 or 토픽의 색을 변경해주기
        if (mNewsProviderCountries.size() != 1) {
            final NewsProviderCountry newsProviderCountry = mNewsProviderCountries.get(i);

            newsSelectViewHolder.feedNameTextView.setText(newsProviderCountry.countryLocalName);

            // TODO: region 비교는 해야하나 아직 모르겠음. 언어와 국가 코드를 같이 비교하기에 region 은 상관없지
            // 않나 생각. 모든 뉴스 적용 후 나중에 확인 필요.(특히 간/번체) 아래 부분도 마찬가지.
            if (mNewsFeed != null && mNewsFeed.getTopicLanguageCode().equals(newsProviderCountry.languageCode) &&
                    mNewsFeed.getTopicCountryCode().equals(newsProviderCountry.countryCode)) {
                Context context = newsSelectViewHolder.feedNameTextView.getContext();
                newsSelectViewHolder.feedNameTextView.setTextColor(
                        context.getResources().getColor(R.color.news_select_color_accent));
            }

            newsSelectViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onSelectNewsProviderCountry(newsProviderCountry);
                }
            });
        } else {
            final NewsProvider newsProvider = mNewsProviderCountries.get(0).newsProviders.get(i);

            newsSelectViewHolder.feedNameTextView.setText(newsProvider.name);

            if (mNewsFeed != null && mNewsFeed.getTopicLanguageCode().equals(newsProvider.languageCode) &&
                    mNewsFeed.getTopicCountryCode().equals(newsProvider.countryCode) &&
                    mNewsFeed.getTopicProviderId() == newsProvider.id) {
                Context context = newsSelectViewHolder.feedNameTextView.getContext();
                newsSelectViewHolder.feedNameTextView.setTextColor(
                        context.getResources().getColor(R.color.news_select_color_accent));
            }

            newsSelectViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  mListener.onSelectNewsProvider(newsProvider);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        // 국가가 한 곳 뿐이라면 바로 Provider 를 보여 주고, 아니라면 국가를 보여 주기
        if (mNewsProviderCountries.size() != 1) {
            return mNewsProviderCountries.size();
        } else {
            return mNewsProviderCountries.get(0).newsProviders.size();
        }
    }

    protected static class NewsSelectViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.news_select_item_feed_name) TextView feedNameTextView;

        public NewsSelectViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            feedNameTextView.setTypeface(TypefaceUtils.getRegularTypeface(itemView.getContext()));
        }
    }

    public void setOnNewsProviderClickListener(OnSelectionListener onNewsProviderClickListener) {
        mListener = onNewsProviderClickListener;
    }
}
