package com.yooiistudios.news.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.util.dp.DipToPixel;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 19.
 *
 * NLBottomNewsFeedAdapter
 *  메인 화면 하단 뉴스피드 리스트의 RecyclerView에 쓰일 어뎁터
 */
public class NLBottomNewsFeedAdapter extends RecyclerView.Adapter<NLBottomNewsFeedAdapter.NLBottomNewsFeedViewHolder> {

    private ArrayList<NLNewsFeed> mNewsFeed;

    public NLBottomNewsFeedAdapter() {
        mNewsFeed = new ArrayList<NLNewsFeed>();
    }

    public NLBottomNewsFeedAdapter(ArrayList<NLNewsFeed> newsFeed) {
        mNewsFeed = newsFeed;
    }

    @Override
    public NLBottomNewsFeedViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int i) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(
                R.layout.main_bottom_item, parent, false);
        v.setElevation(DipToPixel.dpToPixel(context,
                context.getResources().getDimension(
                        R.dimen.main_bottom_cardView_elevation)));

        NLBottomNewsFeedViewHolder viewHolder =
                new NLBottomNewsFeedViewHolder(v);

//        viewHolder.feedName.setBackgroundColor(context.getResources().getColor(
//                R.color.theme_default_accent));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(
            NLBottomNewsFeedViewHolder bottomNewsFeedViewHolder, int position) {
        bottomNewsFeedViewHolder.feedName.setText(
                mNewsFeed.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mNewsFeed.size();
    }

    public void addNewsFeed(NLNewsFeed newsFeed) {
        mNewsFeed.add(newsFeed);
        notifyItemInserted(mNewsFeed.size() - 1);
    }

    protected static class NLBottomNewsFeedViewHolder extends RecyclerView
            .ViewHolder {

        TextView feedName;

        public NLBottomNewsFeedViewHolder(View itemView) {
            super(itemView);
            feedName = (TextView)itemView.findViewById(R.id.feedName);
        }

    }
}
