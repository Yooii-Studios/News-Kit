package com.yooiistudios.news.model.main;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.yooiistudios.news.R;
import com.yooiistudios.news.main.NLMainActivity;
import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.model.NLNewsFeed;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.dp.DipToPixel;
import com.yooiistudios.news.util.log.NLLog;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 19.
 *
 * NLBottomNewsFeedAdapter
 *  메인 화면 하단 뉴스피드 리스트의 RecyclerView에 쓰일 어뎁터
 */
public class NLBottomNewsFeedAdapter extends
        RecyclerView.Adapter<NLBottomNewsFeedAdapter
                .NLBottomNewsFeedViewHolder> {
    private static final String TAG = NLBottomNewsFeedAdapter.class.getName();
    private static final String VIEW_NAME_POSTFIX = "_bottom_";

    private Context mContext;
    private ArrayList<NLNewsFeed> mNewsFeed;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(
                NLBottomNewsFeedAdapter.NLBottomNewsFeedViewHolder
                        viewHolder, NLNewsFeed newsFeed);
    }

    public NLBottomNewsFeedAdapter(Context context, OnItemClickListener
                                   listener) {
        mContext = context;
        mNewsFeed = new ArrayList<NLNewsFeed>();
        mOnItemClickListener = listener;
    }

    @Override
    public NLBottomNewsFeedViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int i) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(
                R.layout.main_bottom_item, parent, false);
        v.setElevation(DipToPixel.dpToPixel(context,
                context.getResources().getDimension(
                        R.dimen.main_bottom_card_view_elevation)
        ));
//        ((ViewGroup)v).setTransitionGroup(false);

        NLBottomNewsFeedViewHolder viewHolder =
                new NLBottomNewsFeedViewHolder(v);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final NLBottomNewsFeedViewHolder viewHolder,
            final int position) {
        TextView titleView = viewHolder.feedName;
        ImageView imageView = viewHolder.imageView;

        titleView.setText(mNewsFeed.get(position).getTitle());
        titleView.setViewName(NLMainActivity.VIEW_NAME_TITLE_PREFIX +
                VIEW_NAME_POSTFIX + position);

        imageView.setBackground(
                new ColorDrawable(Color.TRANSPARENT));
        imageView.setViewName(NLMainActivity.VIEW_NAME_IMAGE_PREFIX +
                VIEW_NAME_POSTFIX + position);

        ArrayList<NLNews> newsList = mNewsFeed.get(position).getNewsList();
        String imageUrl;
        if (newsList.size() > 0 &&
                (imageUrl = newsList.get(0).getImageUrl()) != null) {

            ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue
                    (mContext), ImageMemoryCache.INSTANCE);

            imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    NLLog.i(TAG, "onResponse\nposition : " + position);
                    viewHolder.imageView.setImageBitmap(response.getBitmap());
//                    viewHolder.imageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }

        viewHolder.itemView.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NLNewsFeed newsFeed = mNewsFeed.get(position);

                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(viewHolder, newsFeed);
                    }
                }
            }
        );
    }

    @Override
    public int getItemCount() {
        return mNewsFeed.size();
    }

    public void addNewsFeed(NLNewsFeed newsFeed) {
        mNewsFeed.add(newsFeed);
        notifyItemInserted(mNewsFeed.size() - 1);
    }
    public void setImageUrlAt(String imageUrl, int position) {
    }

//    @Override
//    public void onClick(View view) {
//        int position = ((Integer)view.getTag(KEY_INDEX));
//        NLBottomNewsFeedViewHolder viewHolder = (NLBottomNewsFeedViewHolder)
//                view.getTag(KEY_VIEW_HOLDER);
//        NLNewsFeed newsFeed = mNewsFeed.get(position);
//
//        if (mOnItemClickListener != null) {
//            mOnItemClickListener.onItemClick(viewHolder, newsFeed);
//        }
//    }

    public static class NLBottomNewsFeedViewHolder extends RecyclerView
            .ViewHolder {

        public TextView feedName;
        public ImageView imageView;

        public NLBottomNewsFeedViewHolder(View itemView) {
            super(itemView);
            feedName = (TextView)itemView.findViewById(R.id.title);
            imageView = (ImageView)itemView.findViewById(R.id.image);
        }

    }
}
