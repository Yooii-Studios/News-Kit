package com.yooiistudios.news.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yooiistudios.news.R;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 19.
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
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.main_bottom_item, parent, false);
        v.setElevation(20);
        return new NLBottomNewsFeedViewHolder(v);
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
