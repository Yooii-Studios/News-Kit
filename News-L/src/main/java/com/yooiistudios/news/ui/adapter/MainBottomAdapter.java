package com.yooiistudios.news.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.NLLog;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 19.
 *
 * MainBottomAdapter
 *  메인 화면 하단 뉴스피드 리스트의 RecyclerView에 쓰일 어뎁터
 */
public class MainBottomAdapter extends
        RecyclerView.Adapter<MainBottomAdapter.BottomNewsFeedViewHolder> {
    private static final String TAG = MainBottomAdapter.class.getName();
    private static final String VIEW_NAME_POSTFIX = "_bottom_";

    private Context mContext;
    private ArrayList<NewsFeed> mNewsFeedList;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onBottomItemClick(BottomNewsFeedViewHolder viewHolder, NewsFeed newsFeed, int position);
    }

    public MainBottomAdapter(Context context, OnItemClickListener
            listener) {
        mContext = context;
        mNewsFeedList = new ArrayList<NewsFeed>();
        mOnItemClickListener = listener;
    }

    @Override
    public BottomNewsFeedViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.main_bottom_item, parent, false);
//        v.setElevation(DipToPixel.dpToPixel(context,
//                context.getResources().getDimension(
//                        R.dimen.main_bottom_card_view_elevation)
//        ));
//        ((ViewGroup)v).setTransitionGroup(false);

        return new BottomNewsFeedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final BottomNewsFeedViewHolder viewHolder, final int position) {
        TextView titleView = viewHolder.newsTitleTextView;
        ImageView imageView = viewHolder.imageView;
        TextView newsFeedTitleView = viewHolder.newsFeedTitleTextView;
        ArrayList<News> newsList = mNewsFeedList.get(position).getNewsList();

        viewHolder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.theme_background));
        imageView.setBackgroundColor(NewsFeedUtils.getMainBottomDefaultBackgroundColor());

        if (newsList == null || newsList.size() == 0) {
            return;
        }

        // 무조건 첫 실행시에는 첫번째 뉴스를 보여주게 변경
        final News displayingNews = newsList.get(0);

        String newsTitle = displayingNews.getTitle();
        if (newsTitle != null) {
            titleView.setText(newsTitle);
        }
        titleView.setViewName(MainActivity.VIEW_NAME_TITLE_PREFIX +
                VIEW_NAME_POSTFIX + position);

//        imageView.setImageDrawable(new ColorDrawable(NewsFeedUtils.getMainBottomDefaultBackgroundColor()));
        imageView.setViewName(MainActivity.VIEW_NAME_IMAGE_PREFIX + VIEW_NAME_POSTFIX + position);

        String newsFeedTitle = mNewsFeedList.get(position).getTitle();
        if (newsFeedTitle != null) {
            newsFeedTitleView.setText(newsFeedTitle);
        }

        viewHolder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        NewsFeed newsFeed = mNewsFeedList.get(position);

                        if (mOnItemClickListener != null && newsFeed.isValid()) {
                            mOnItemClickListener.onBottomItemClick(viewHolder, newsFeed, position);
                        }
                    }
                }
        );

        viewHolder.progressBar.setVisibility(View.VISIBLE);

        String imageUrl = displayingNews.getImageUrl();
        NLLog.i("main bottom image", "position : " + position);
        NLLog.i("main bottom image", "imageUrl : " + imageUrl);
        NLLog.i("main bottom image", "displayingNews.isImageUrlChecked() : " + displayingNews.isImageUrlChecked());
        if (imageUrl == null) {
            if (displayingNews.isImageUrlChecked()) {
                showDummyImage(viewHolder);
                viewHolder.progressBar.setVisibility(View.GONE);
                return;
            } else {
                viewHolder.imageView.setImageDrawable(null);
                viewHolder.imageView.setColorFilter(null);
                return;
            }
        }

        ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue
                (mContext), ImageMemoryCache.getInstance(mContext));

        imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                NLLog.i("main bottom image", "onResponse\nposition : " + position + ", " +
                        "isImmediate : " + isImmediate);

                Bitmap bitmap = response.getBitmap();

                if (bitmap == null && isImmediate) {
                    // 비트맵이 null이지만 인터넷을 통하지 않고 바로 불린 콜백이라면 무시하자
                    return;
                }

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

    public static int measureMaximumHeight(Context context, int itemCount, int columnCount) {
        NLLog.i(TAG, "itemCount : " + itemCount);
        NLLog.i(TAG, "columnCount : " + columnCount);

        // get display width
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point displaySize = new Point();
        display.getSize(displaySize);
        int displayWidth = displaySize.x;

        // main_bottom_margin_small : item padding = recyclerView margin
        float recyclerViewMargin = context.getResources().
                getDimension(R.dimen.main_bottom_margin_small);

        float rowWidth = (displayWidth - (recyclerViewMargin * 2)) / columnCount;

        float rowHeight = getRowHeight(rowWidth);

        NLLog.i(TAG, "rowHeight : " + rowHeight);

        int rowCount = itemCount / columnCount;
        if (itemCount % columnCount != 0) {
            rowCount += 1;
        }

        return Math.round(rowHeight * rowCount);
    }

    public static float getRowHeight(float width) {
        return width * 3.3f / 4.0f;
    }

    @Override
    public int getItemCount() {
        return mNewsFeedList.size();
    }

    public void addNewsFeed(NewsFeed newsFeed) {
        mNewsFeedList.add(newsFeed);
        notifyItemInserted(mNewsFeedList.size() - 1);
    }

    public void replaceNewsFeedAt(int idx, NewsFeed newsFeed) {
        if (idx < mNewsFeedList.size()) {
            mNewsFeedList.set(idx, newsFeed);
            notifyItemChanged(idx);
        }
    }

    public void setNewsFeedList(ArrayList<NewsFeed> newsFeedList) {
        mNewsFeedList = newsFeedList;
        notifyDataSetChanged();
    }

    public static class BottomNewsFeedViewHolder extends RecyclerView
            .ViewHolder {

        public TextView newsTitleTextView;
        public ImageView imageView;
        public ProgressBar progressBar;
        public TextView newsFeedTitleTextView;
        public int displayingNewsIndex;

        public BottomNewsFeedViewHolder(View itemView) {
            super(itemView);
            displayingNewsIndex = 0;
            newsTitleTextView = (TextView) itemView.findViewById(R.id.main_bottom_item_title);
            imageView = (ImageView) itemView.findViewById(R.id.main_bottom_item_image_view);
            progressBar = (ProgressBar) itemView.findViewById(R.id.main_bottom_item_progress);
            newsFeedTitleTextView = (TextView) itemView.findViewById(R.id.main_bottom_news_feed_title);
        }
    }
}
