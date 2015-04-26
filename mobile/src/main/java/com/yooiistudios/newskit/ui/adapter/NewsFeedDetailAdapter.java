package com.yooiistudios.newskit.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.util.DipToPixel;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 19.
 *
 * NewsFeedDetailAdapter
 *  뉴스 피드 디테일 화면 하단 뉴스 리스트의 RecyclerView 에 쓰일 어뎁터
 */
public class NewsFeedDetailAdapter extends
        RecyclerView.Adapter<NewsFeedDetailAdapter.ViewHolder> {
    private ArrayList<News> mNewsList;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(ViewHolder viewHolder, News news);
    }

    public NewsFeedDetailAdapter(OnItemClickListener onItemClickListener) {
        mNewsList = new ArrayList<>();
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        Context context = parent.getContext();

        return createViewHolder(context, parent);
    }

    public static ViewHolder createViewHolder(Context context, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(
                R.layout.detail_bottom_item, parent, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.setElevation(DipToPixel.dpToPixel(context,
                    context.getResources().getDimension(
                            R.dimen.main_bottom_card_view_elevation)
            ));
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final News news = mNewsList.get(position);

        configView(viewHolder, news, mOnItemClickListener);
    }

    public static void configView(final ViewHolder viewHolder, final News news,
                                  final OnItemClickListener listener) {
        TextView titleTextView = viewHolder.newsTitleTextView;
        if (titleTextView != null) {
//            titleTextView.setTypeface(TypefaceUtils.getMediumTypeface(context));
            titleTextView.setText(news.getTitle());

            // 아래 패딩 조절
            if (news.getDescription() != null) {
                titleTextView.setPadding(titleTextView.getPaddingLeft(),
                        titleTextView.getPaddingTop(), titleTextView.getPaddingRight(), 0);
            }
        }

        TextView descriptionTextView = viewHolder.newsDescriptionTextView;
        if (descriptionTextView != null) {
//            descriptionTextView.setTypeface(TypefaceUtils.getMediumTypeface(context));
            String description = news.getDescription();
            if (description != null && description.trim().length() > 0) {
                descriptionTextView.setVisibility(View.VISIBLE);
                descriptionTextView.setText(news.getDescription().trim());
            } else {
                descriptionTextView.setVisibility(View.GONE);
            }
        }

        viewHolder.rippleView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.onItemClick(viewHolder, news);
                        }
                    }
                }
        );
    }

    public static int measureMaximumRowHeight(Context context) {
        ViewHolder viewHolder = NewsFeedDetailAdapter.createViewHolder(context, null);

        int titleMaxLine = context.getResources()
                .getInteger(R.integer.news_feed_detail_title_max_line);

        int descriptionMaxLine = context.getResources()
                .getInteger(R.integer.news_feed_detail_description_text_max_line);

        String title = "T";
        for (int i = 0; i < titleMaxLine; i++) {
            if (i != (titleMaxLine - 1)) {
                title += "\nT";
            }
        }
        String description = "D";
        for (int i = 0; i < descriptionMaxLine; i++) {
            if (i != (descriptionMaxLine - 1)) {
                description += "\nD";
            }
        }

        News news = new News();
        news.setTitle(title);
        news.setDescription(description);

        NewsFeedDetailAdapter.configView(viewHolder, news, null);

        viewHolder.itemView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        viewHolder.itemView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return viewHolder.itemView.getMeasuredHeight();
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }

    public void setNewsFeed(ArrayList<News> newsList) {
        mNewsList = newsList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView newsTitleTextView;
        protected TextView newsDescriptionTextView;
        protected View rippleView;

        public ViewHolder(View itemView) {
            super(itemView);
            newsTitleTextView = (TextView) itemView.findViewById(R.id.detail_bottom_news_item_title);
            newsDescriptionTextView = (TextView) itemView.findViewById(R.id.detail_bottom_news_item_description);
            rippleView = itemView.findViewById(R.id.detail_bottom_news_item_ripple_view);
        }
    }
}
