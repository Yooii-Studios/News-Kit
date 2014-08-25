package com.yooiistudios.news.model.detail;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.NLNews;
import com.yooiistudios.news.util.dp.DipToPixel;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 19.
 *
 * NLBottomNewsFeedAdapter
 *  메인 화면 하단 뉴스피드 리스트의 RecyclerView에 쓰일 어뎁터
 */
public class NLDetailNewsAdapter extends
        RecyclerView.Adapter<NLDetailNewsAdapter.ViewHolder> {
    private static final String TAG = NLDetailNewsAdapter.class.getName();

    private Context mContext;
    private ArrayList<NLNews> mNewsList;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(ViewHolder viewHolder, NLNews news);
    }

    public NLDetailNewsAdapter(Context context
            , OnItemClickListener listener) {
        mContext = context;
        mNewsList = new ArrayList<NLNews>();
        mOnItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int i) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(
                R.layout.detail_bottom_item, parent, false);
        v.setElevation(DipToPixel.dpToPixel(context,
                context.getResources().getDimension(
                        R.dimen.main_bottom_card_view_elevation)
        ));

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder,
            final int position) {
        TextView titleTextView = viewHolder.newsTitle;
        if (titleTextView != null) {
            titleTextView.setText(mNewsList.get(position).getTitle());
            titleTextView.setTextColor(Color.BLACK);

            // 아래 패딩 조절
            if (mNewsList.get(position).getDescription() != null) {
                titleTextView.setPadding(titleTextView.getPaddingLeft(),
                        titleTextView.getPaddingTop(), titleTextView.getPaddingRight(), 0);
            }
        }

        TextView descriptionTextView = viewHolder.newsDescription;
        if (descriptionTextView != null && mNewsList.get(position).getDescription() != null) {
            String description = mNewsList.get(position).getDescription();
            if (description != null) {
                descriptionTextView.setText(mNewsList.get(position).getDescription());
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
                    NLNews news = mNewsList.get(position);

                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(viewHolder, news);
                    }
                }
            }
        );
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }

    public void addNews(NLNews news) {
        mNewsList.add(news);
        notifyItemInserted(mNewsList.size() - 1);
    }

    public static class ViewHolder extends RecyclerView
            .ViewHolder {

        protected TextView newsTitle;
        protected TextView newsDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            newsTitle = (TextView)itemView.findViewById(R.id.detail_bottom_news_item_title);
            newsDescription = (TextView) itemView.findViewById(R.id.detail_bottom_news_item_description);
        }

    }
}
