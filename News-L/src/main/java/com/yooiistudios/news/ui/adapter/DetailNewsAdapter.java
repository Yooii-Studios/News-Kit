package com.yooiistudios.news.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.util.dp.DipToPixel;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 19.
 *
 * NLBottomNewsFeedAdapter
 *  메인 화면 하단 뉴스피드 리스트의 RecyclerView에 쓰일 어뎁터
 */
public class DetailNewsAdapter extends
        RecyclerView.Adapter<DetailNewsAdapter.ViewHolder> {
    private static final String TAG = DetailNewsAdapter.class.getName();

    private Context mContext;
    private ArrayList<News> mNewsList;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(ViewHolder viewHolder, News news);
    }

    public DetailNewsAdapter(Context context, OnItemClickListener onItemClickListener) {
        mContext = context;
        mNewsList = new ArrayList<News>();
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
        v.setElevation(DipToPixel.dpToPixel(context,
                context.getResources().getDimension(
                        R.dimen.main_bottom_card_view_elevation)
        ));

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
            titleTextView.setText(news.getTitle());
            titleTextView.setTextColor(Color.BLACK);

            // 아래 패딩 조절
            if (news.getDescription() != null) {
                titleTextView.setPadding(titleTextView.getPaddingLeft(),
                        titleTextView.getPaddingTop(), titleTextView.getPaddingRight(), 0);
            }
        }

        TextView descriptionTextView = viewHolder.newsDescriptionTextView;
        if (descriptionTextView != null) {
            String description = news.getDescription();
            if (description != null) {
                descriptionTextView.setText(news.getDescription());
                descriptionTextView.setTextColor(Color.GRAY);
            } else {
                descriptionTextView.setVisibility(View.GONE);
            }
        }

        viewHolder.itemView.setClickable(true);
        viewHolder.itemView.setFocusable(true);
        viewHolder.itemView.setOnClickListener(
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
        ViewHolder viewHolder = DetailNewsAdapter.createViewHolder(context, null);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(R.style.DetailTextStyle,
                new int[]{android.R.attr.maxLines});
        int maxLines = typedArray.getInt(0, -1);

        String title = "";
        for (int i = 0; i < maxLines; i++) {
            title += "title";

            if (i != (maxLines - 1)) {
                title += "\n";
            }
        }
        String description = "";
        for (int i = 0; i < maxLines; i++) {
            description += "description";

            if (i != (maxLines - 1)) {
                description += "\n";
            }
        }

        News news = new News();
        news.setTitle(title);
        news.setDescription(description);

        DetailNewsAdapter.configView(viewHolder, news, null);

        viewHolder.itemView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return viewHolder.itemView.getMeasuredHeight();
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }

    public void addNews(News news) {
        mNewsList.add(news);
        notifyItemInserted(mNewsList.size() - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView newsTitleTextView;
        protected TextView newsDescriptionTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            newsTitleTextView = (TextView)itemView.findViewById(R.id.detail_bottom_news_item_title);
            newsDescriptionTextView = (TextView) itemView.findViewById(R.id.detail_bottom_news_item_description);
        }
    }
}
