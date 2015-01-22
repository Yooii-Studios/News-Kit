package com.yooiistudios.news.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.NewsProvider;
import com.yooiistudios.news.util.TypefaceUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * NewsSelectRecyclerViewAdapter
 *  한 국가의 뉴스 피드 리스트를 보여주는 리사이클러뷰의 어댑터
 */
public class NewsSelectRecyclerViewAdapter extends
        RecyclerView.Adapter<NewsSelectRecyclerViewAdapter.NewsSelectViewHolder> {

    private ArrayList<NewsProvider> mNewsProviderList;
    private OnNewsProviderClickListener mOnNewsProviderClickListener;

    public interface OnNewsProviderClickListener {
        public void onNewsProviderClick(NewsProvider newsProvider);
    }

    public NewsSelectRecyclerViewAdapter(ArrayList<NewsProvider> presetList) {
        mNewsProviderList = presetList;
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
        final NewsProvider newsProvider = mNewsProviderList.get(i);

        newsSelectViewHolder.feedNameTextView.setText(newsProvider.getName());
        newsSelectViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnNewsProviderClickListener.onNewsProviderClick(newsProvider);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNewsProviderList.size();
    }

    protected static class NewsSelectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.news_select_item_feed_name) TextView feedNameTextView;

        public NewsSelectViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            feedNameTextView.setTypeface(TypefaceUtils.getRegularTypeface(itemView.getContext()));
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void setOnNewsProviderClickListener(OnNewsProviderClickListener onNewsProviderClickListener) {
        mOnNewsProviderClickListener = onNewsProviderClickListener;
    }
}
