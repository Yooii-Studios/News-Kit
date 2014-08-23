package com.yooiistudios.news.model.detail;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.util.dp.DipToPixel;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 19.
 *
 * NLBottomNewsFeedAdapter
 *  메인 화면 하단 뉴스피드 리스트의 RecyclerView에 쓰일 어뎁터
 */
public class NLDetailBottomNewsAdapter extends
        RecyclerView.Adapter<NLDetailBottomNewsAdapter.NLDetailBottomNewsViewHolder> {
    private static final String TAG = NLDetailBottomNewsAdapter.class.getName();

    private Context mContext;
    private ArrayList<NLNews> mNewsList;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(NLDetailBottomNewsViewHolder viewHolder, NLNews news);
    }

    public NLDetailBottomNewsAdapter(Context context
            , OnItemClickListener listener) {
        mContext = context;
        mNewsList = new ArrayList<NLNews>();
        mOnItemClickListener = listener;
    }

    @Override
    public NLDetailBottomNewsViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int i) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(
                R.layout.detail_bottom_item, parent, false);
        v.setElevation(DipToPixel.dpToPixel(context,
                context.getResources().getDimension(
                        R.dimen.main_bottom_card_view_elevation)
        ));

        return new NLDetailBottomNewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final NLDetailBottomNewsViewHolder viewHolder,
            final int position) {
        TextView titleView = viewHolder.newsTitle;
        if (titleView != null) {
            titleView.setText(mNewsList.get(position).getTitle());
            titleView.setTextColor(Color.BLACK);
        }

        TextView descriptionView = viewHolder.newsDescription;
        if (descriptionView != null) {
            descriptionView.setText(mNewsList.get(position).getDescription());
            descriptionView.setTextColor(Color.GRAY);
        }

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

    public static class NLDetailBottomNewsViewHolder extends RecyclerView
            .ViewHolder {

        protected TextView newsTitle;
        protected TextView newsDescription;

        public NLDetailBottomNewsViewHolder(View itemView) {
            super(itemView);
            newsTitle = (TextView)itemView.findViewById(R.id.detail_bottom_news_item_title);
            newsDescription = (TextView) itemView.findViewById(R.id.detail_bottom_news_item_description);
        }

    }
}
