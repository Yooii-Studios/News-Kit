package com.yooiistudios.news.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedUtils;
import com.yooiistudios.news.model.news.TintType;
import com.yooiistudios.news.ui.activity.MainActivity;
import com.yooiistudios.news.util.cache.ImageMemoryCache;
import com.yooiistudios.news.util.log.NLLog;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 19.
 *
 * NLBottomNewsFeedAdapter
 *  메인 화면 하단 뉴스피드 리스트의 RecyclerView에 쓰일 어뎁터
 */
public class BottomNewsFeedAdapter extends
        RecyclerView.Adapter<BottomNewsFeedAdapter.BottomNewsFeedViewHolder> {
    private static final String TAG = BottomNewsFeedAdapter.class.getName();
    private static final String VIEW_NAME_POSTFIX = "_bottom_";

    private Context mContext;
    private ArrayList<NewsFeed> mNewsFeedList;
    private OnItemClickListener mOnItemClickListener;
    private ArrayList<Integer> mDisplayingNewsFeedIndices;

    public interface OnItemClickListener {
        public void onBottomItemClick(
                BottomNewsFeedViewHolder
                        viewHolder, NewsFeed newsFeed, int position);
    }

    public BottomNewsFeedAdapter(Context context, OnItemClickListener
            listener) {
        mContext = context;
        mNewsFeedList = new ArrayList<NewsFeed>();
        mDisplayingNewsFeedIndices = new ArrayList<Integer>();
        mOnItemClickListener = listener;
    }

    @Override
    public BottomNewsFeedViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int i) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(
                R.layout.main_bottom_item, parent, false);
//        v.setElevation(DipToPixel.dpToPixel(context,
//                context.getResources().getDimension(
//                        R.dimen.main_bottom_card_view_elevation)
//        ));
//        ((ViewGroup)v).setTransitionGroup(false);

        BottomNewsFeedViewHolder viewHolder =
                new BottomNewsFeedViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BottomNewsFeedViewHolder viewHolder,
            final int position) {
        TextView titleView = viewHolder.newsTitleTextView;
        ImageView imageView = viewHolder.imageView;
        TextView newsFeedTitleView = viewHolder.newsFeedTitleTextView;
        ArrayList<News> newsList = mNewsFeedList.get(position).getNewsList();

        if (newsList == null || newsList.size() == 0) {
            showDummyImage(viewHolder);
            return;
        }

        int index = position < mDisplayingNewsFeedIndices.size()
                ? mDisplayingNewsFeedIndices.get(position) : 0;
        final News displayingNews = newsList.get(index);

        titleView.setText(displayingNews.getTitle());
        titleView.setViewName(MainActivity.VIEW_NAME_TITLE_PREFIX +
                VIEW_NAME_POSTFIX + position);

        imageView.setBackgroundColor(NewsFeedUtils.getMainBottomDefaultBackgroundColor());
        imageView.setImageDrawable(new ColorDrawable(NewsFeedUtils.getMainBottomDefaultBackgroundColor()));
        imageView.setViewName(MainActivity.VIEW_NAME_IMAGE_PREFIX + VIEW_NAME_POSTFIX + position);

        newsFeedTitleView.setText(mNewsFeedList.get(position).getTitle());


        viewHolder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        NewsFeed newsFeed = mNewsFeedList.get(position);

                        if (mOnItemClickListener != null && newsFeed != null) {
                            mOnItemClickListener.onBottomItemClick(viewHolder, newsFeed, position);
                        }
                    }
                }
        );

        viewHolder.progressBar.setVisibility(View.VISIBLE);

        String imageUrl;
        if ((imageUrl = displayingNews.getImageUrl()) == null) {
            if (displayingNews.isImageUrlChecked()) {
                showDummyImage(viewHolder);
                viewHolder.progressBar.setVisibility(View.GONE);
                return;
            } else {
                return;
            }
        }

        ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue
                (mContext), ImageMemoryCache.getInstance(mContext));

        imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                NLLog.i(TAG, "onResponse\nposition : " + position);

                Bitmap bitmap = response.getBitmap();

                if (bitmap != null) {
                    viewHolder.imageView.setImageBitmap(bitmap);
                    Palette palette = Palette.generate(bitmap);
                    PaletteItem paletteItem = palette.getDarkVibrantColor();
                    if (paletteItem != null) {
                        int darkVibrantColor = paletteItem.getRgb();
                        int red = Color.red(darkVibrantColor);
                        int green = Color.green(darkVibrantColor);
                        int blue = Color.blue(darkVibrantColor);
                        int alpha = mContext.getResources().getInteger(R.integer.vibrant_color_tint_alpha);
                        viewHolder.imageView.setColorFilter(Color.argb(alpha, red, green, blue));
                        viewHolder.imageView.setTag(TintType.PALETTE);
                    } else {
                        viewHolder.imageView.setColorFilter(NewsFeedUtils.getGrayFilterColor());
                        viewHolder.imageView.setTag(TintType.GRAYSCALE);
                    }
                    viewHolder.progressBar.setVisibility(View.GONE);
                } else {
                    if (!displayingNews.isImageUrlChecked()) {
                        // 뉴스의 이미지 url이 있는지 체크가 안된 경우는 아직 기다려야 함.
                        viewHolder.progressBar.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                showDummyImage(viewHolder);
            }
        });
    }
    private void showDummyImage(BottomNewsFeedViewHolder viewHolder) {
        Bitmap dummyImage = NewsFeedUtils.getDummyNewsImage(mContext);
        viewHolder.imageView.setImageBitmap(dummyImage);
        viewHolder.imageView.setColorFilter(NewsFeedUtils.getDummyImageFilterColor());
        viewHolder.imageView.setTag(TintType.DUMMY);
    }

    @Override
    public int getItemCount() {
        return mNewsFeedList.size();
    }

    public void addNewsFeed(NewsFeed newsFeed) {
        mNewsFeedList.add(newsFeed);
        mDisplayingNewsFeedIndices.add(0);
        notifyItemInserted(mNewsFeedList.size() - 1);
    }

    public void setImageUrlAt(String imageUrl, int position) {
    }

//    @Override
//    public void onClick(View view) {
//        int position = ((Integer)view.getTag(KEY_INDEX));
//        NLBottomNewsFeedViewHolder viewHolder = (NLBottomNewsFeedViewHolder)
//                view.getTag(KEY_VIEW_HOLDER);
//        NLNewsFeed newsFeed = mNewsFeedList.get(position);
//
//        if (mOnItemClickListener != null) {
//            mOnItemClickListener.onItemClick(viewHolder, newsFeed);
//        }
//    }

    public static class BottomNewsFeedViewHolder extends RecyclerView
            .ViewHolder {

        public TextView newsTitleTextView;
        public ImageView imageView;
        public ProgressBar progressBar;
        public TextView newsFeedTitleTextView;

        public BottomNewsFeedViewHolder(View itemView) {
            super(itemView);
            newsTitleTextView = (TextView) itemView.findViewById(R.id.main_bottom_item_title);
            imageView = (ImageView) itemView.findViewById(R.id.main_bottom_item_image_view);
            progressBar = (ProgressBar) itemView.findViewById(R.id.main_bottom_item_progress);
            newsFeedTitleTextView = (TextView) itemView.findViewById(R.id.main_bottom_news_feed_title);
        }

    }
}
