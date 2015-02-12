package com.yooiistudios.news.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsImageRequestQueue;
import com.yooiistudios.news.model.news.TintType;
import com.yooiistudios.news.model.news.util.NewsFeedUtils;
import com.yooiistudios.news.ui.widget.RatioFrameLayout;
import com.yooiistudios.news.ui.widget.MainBottomItemLayout;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.ScreenUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 19.
 *
 * MainBottomAdapter
 *  메인 화면 하단 뉴스피드 리스트의 RecyclerView 에 쓰일 어뎁터
 */
public class MainBottomAdapter extends
        RecyclerView.Adapter<MainBottomAdapter.BottomNewsFeedViewHolder> {
    public interface OnBindViewHolderListener {
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i);
    }

    public interface OnItemClickListener {
        public void onBottomItemClick(BottomNewsFeedViewHolder viewHolder, NewsFeed newsFeed, int position);
    }

    public static final int PORTRAIT = 0;
    public static final int LANDSCAPE = 1;
    private static final String TAG = MainBottomAdapter.class.getName();
    private static final String VIEW_NAME_POSTFIX = "_bottom_";
    private static final float HEIGHT_OVER_WIDTH_RATIO = 3.3f / 4.0f;

    private Context mContext;
    private ArrayList<NewsFeed> mNewsFeedList;
    private OnItemClickListener mOnItemClickListener;

    private OnBindViewHolderListener mOnBindViewHolderListener;

    private int mOrientation = PORTRAIT;

    @IntDef(value = {PORTRAIT, LANDSCAPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {}

    public MainBottomAdapter(Context context, OnItemClickListener listener) {
        this(context, listener, PORTRAIT);
    }

    public MainBottomAdapter(Context context, OnItemClickListener listener, @Orientation int orientation) {
        mContext = context;
        mNewsFeedList = new ArrayList<>();
        mOnItemClickListener = listener;
        mOrientation = orientation;
    }

    @Override
    public BottomNewsFeedViewHolder onCreateViewHolder(final ViewGroup parent, int i) {
        Context context = parent.getContext();
        MainBottomItemLayout itemLayout = (MainBottomItemLayout)
                LayoutInflater.from(context).inflate(R.layout.main_bottom_item, parent, false);
//        v.setElevation(DipToPixel.dpToPixel(context,
//                context.getResources().getDimension(
//                        R.dimen.main_bottom_card_view_elevation)
//        ));
//        ((ViewGroup)v).setTransitionGroup(false);
        itemLayout.setOnSupplyTargetAxisLengthListener(new MainBottomItemLayout.OnSupplyTargetAxisLengthListener() {
            @Override
            public int onSupply(@RatioFrameLayout.Axis int axis, @MainBottomItemLayout.Orientation int orientation) {
                if (axis == RatioFrameLayout.AXIS_WIDTH && orientation == MainBottomItemLayout.LANDSCAPE) {
                    ViewGroup.LayoutParams lp = parent.getLayoutParams();
                    return MainBottomAdapter.measureMaximumHeightOnLandscape(mContext, lp)/4;
//                    return parentHeight/4;
                } else {
                    return -1;
                }
            }
        });

        return new BottomNewsFeedViewHolder(itemLayout);
    }

    @Override
    public void onBindViewHolder(final BottomNewsFeedViewHolder viewHolder, final int position) {
        boolean isVertical = mOrientation == PORTRAIT;

        MainBottomItemLayout itemView = (MainBottomItemLayout)viewHolder.itemView;
//        itemView.setBaseAxis(
//                isVertical ? RatioFrameLayout.AXIS_WIDTH : RatioFrameLayout.AXIS_HEIGHT
//        );
        itemView.setBaseAxis(RatioFrameLayout.AXIS_WIDTH);
        itemView.setOrientation(
                isVertical ? MainBottomItemLayout.PORTRAIT : MainBottomItemLayout.LANDSCAPE
        );

        TextView titleView = viewHolder.newsTitleTextView;
        ImageView imageView = viewHolder.imageView;
        TextView newsFeedTitleView = viewHolder.newsFeedTitleTextView;
        NewsFeed newsFeed = mNewsFeedList.get(position);

        viewHolder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.theme_background));
        imageView.setBackgroundColor(NewsFeedUtils.getMainBottomDefaultBackgroundColor());

        if (newsFeed == null || !newsFeed.containsNews()) {
            newsFeedTitleView.setText("");
            titleView.setText(newsFeed != null ? newsFeed.getFetchStateMessage(mContext) : "");
            viewHolder.progressBar.setVisibility(View.INVISIBLE);
            showDummyImage(viewHolder);
            return;
        }

        ArrayList<News> newsList = newsFeed.getNewsList();

        if (newsList == null || newsList.size() == 0) {
            return;
        }

        // 무조건 첫 실행시에는 첫번째 뉴스를 보여주게 변경
        final News displayingNews = newsList.get(mNewsFeedList.get(position).getDisplayingNewsIndex());

        viewHolder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        NewsFeed newsFeed = mNewsFeedList.get(position);

                        if (mOnItemClickListener != null &&
                                newsFeed != null && newsFeed.containsNews()) {
                            mOnItemClickListener.onBottomItemClick(viewHolder, newsFeed, position);
                        }
                    }
                }
        );

        // 뉴스 제목 설정
        String newsTitle = displayingNews.getTitle();
        if (newsTitle != null) {
            titleView.setText(newsTitle);
        }

        String newsFeedTitle = mNewsFeedList.get(position).getTitle();
        if (newsFeedTitle != null) {
            newsFeedTitleView.setText(newsFeedTitle);
        }

        String imageUrl = displayingNews.getImageUrl();

        showLoading(viewHolder);

        if (imageUrl == null) {
            if (displayingNews.isImageUrlChecked()) {
                showDummyImage(viewHolder);
            }

            return;
        }

        RequestQueue requestQueue = NewsImageRequestQueue.getInstance(mContext).getRequestQueue();
        ImageLoader imageLoader = new ImageLoader(requestQueue,
                ImageMemoryCache.getInstance(mContext));

        Object tag = viewHolder.itemView.getTag(R.id.tag_main_bottom_image_request_idx);

        if (tag != null) {
            int previousRequestIdx = (Integer)tag;
            int currentNewsIndex = mNewsFeedList.get(position).getDisplayingNewsIndex();

            if (currentNewsIndex != previousRequestIdx) {
                tag = viewHolder.itemView.getTag(R.id.tag_main_bottom_image_request);
                ((ImageLoader.ImageContainer) tag).cancelRequest();
            }
        }
        ImageLoader.ImageContainer imageContainer =
                imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                final Bitmap bitmap = response.getBitmap();

                if (bitmap == null && isImmediate) {
                    // 비트맵이 null 이지만 인터넷을 통하지 않고 바로 불린 콜백이라면 무시하자
//                    viewHolder.progressBar.setVisibility(View.GONE);
                    return;
                }

//                if (viewHolder.imageView.getAnimation() != null) {
//                    viewHolder.progressBar.setVisibility(View.GONE);
//                }

                if (bitmap != null) {
                    viewHolder.progressBar.setVisibility(View.GONE);

                    viewHolder.imageView.setImageBitmap(bitmap);
//                    setOnClickListener(viewHolder, position);

                    // apply palette
                    Palette palette = Palette.generate(bitmap);
                    int darkVibrantColor = palette.getDarkVibrantColor(Color.TRANSPARENT);
                    if (darkVibrantColor != Color.TRANSPARENT) {
//                        int darkVibrantColor = paletteItem.getRgb();
                        int red = Color.red(darkVibrantColor);
                        int green = Color.green(darkVibrantColor);
                        int blue = Color.blue(darkVibrantColor);
                        int alpha = mContext.getResources().getInteger(R.integer.vibrant_color_tint_alpha);
                        viewHolder.imageView.setColorFilter(Color.argb(alpha, red, green, blue));
                        viewHolder.imageView.setTag(TintType.PALETTE);
                    } else {
                        viewHolder.imageView.setColorFilter(NewsFeedUtils.getGrayFilterColor());
                        viewHolder.imageView.setTag(TintType.GRAY_SCALE);
                    }
                } else {
                    if (!displayingNews.isImageUrlChecked()) {
                        // 뉴스의 이미지 url 이 있는지 체크가 안된 경우는 아직 기다려야 함.
                        showLoading(viewHolder);
                    } else {
                        showDummyImage(viewHolder);
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                showDummyImage(viewHolder);
            }
        });
        viewHolder.itemView.setTag(R.id.tag_main_bottom_image_request, imageContainer);
        viewHolder.itemView.setTag(R.id.tag_main_bottom_image_request_idx,
                mNewsFeedList.get(position).getDisplayingNewsIndex());

        if (mOnBindViewHolderListener != null) {
            mOnBindViewHolderListener.onBindViewHolder(viewHolder, position);
        }
    }

    private void showLoading(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        viewHolder.imageView.setImageDrawable(null);
        viewHolder.imageView.setColorFilter(null);
//        viewHolder.itemView.setOnClickListener(null);
    }

    private void showDummyImage(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.progressBar.setVisibility(View.GONE);
        viewHolder.imageView.setImageBitmap(NewsFeedUtils.getDummyNewsImage(mContext));
        viewHolder.imageView.setColorFilter(NewsFeedUtils.getDummyImageFilterColor());
        viewHolder.imageView.setTag(TintType.DUMMY);
//        setOnClickListener(viewHolder, position);
    }

//    private void setOnClickListener(final BottomNewsFeedViewHolder viewHolder, final int position) {
//        viewHolder.itemView.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        NewsFeed newsFeed = mNewsFeedList.get(position);
//
//                        if (mOnItemClickListener != null && newsFeed.containsNews()) {
//                            mOnItemClickListener.onBottomItemClick(viewHolder, newsFeed, position);
//                        }
//                    }
//                }
//        );
//    }

    public static int measureMaximumHeightOnPortrait(Context context, int itemCount, int columnCount) {
        // get display width
        Point displaySize = ScreenUtils.getDisplaySize(context);
        int displayWidth = displaySize.x;

        // main_bottom_margin_small : item padding = recyclerView margin
        float recyclerViewMargin = context.getResources().
                getDimension(R.dimen.main_bottom_margin_small);

        float rowWidth = (displayWidth - (recyclerViewMargin * 2)) / columnCount;
        float rowHeight = getRowHeight(rowWidth);

        int rowCount = itemCount / columnCount;
        if (itemCount % columnCount != 0) {
            rowCount += 1;
        }

        return Math.round(rowHeight * rowCount);
    }

    public static int measureMaximumHeightOnLandscape(Context context, ViewGroup.LayoutParams lp) {
        int height = ScreenUtils.getDisplaySize(context).y;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // 롤리팝 이상 디바이스에서만 투명 스테이터스바가 적용된다.
            height -= ScreenUtils.calculateStatusBarHeight(context);
        }
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams)lp;
            height -= (marginLayoutParams.topMargin + marginLayoutParams.bottomMargin);
        }

        return height;
    }

    public static float getRowHeight(float width) {
        return width * (HEIGHT_OVER_WIDTH_RATIO);
    }

    public static float getRowWidth(float height) {
        return height * (1 / HEIGHT_OVER_WIDTH_RATIO);
    }

    @Override
    public int getItemCount() {
        return mNewsFeedList.size();
    }

    public void addNewsFeed(NewsFeed newsFeed) {
        mNewsFeedList.add(newsFeed);
        notifyItemInserted(mNewsFeedList.size() - 1);
    }

    public void addNewsFeedAt(NewsFeed newsFeed, int idx) {
        mNewsFeedList.add(idx, newsFeed);
        notifyItemInserted(idx);
    }

//    public void addNewsFeedList(List<NewsFeed> newsFeedListToAdd) {
//        int notifyStartIdx = mNewsFeedList.size();
//        mNewsFeedList.addAll(newsFeedListToAdd);
//        notifyItemRangeInserted(notifyStartIdx, newsFeedListToAdd.size());
//    }

    public boolean contains(NewsFeed newsFeed) {
        return mNewsFeedList.contains(newsFeed);
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

    public void removeNewsFeedAt(int idx) {
        mNewsFeedList.remove(idx);
    }

    public ArrayList<NewsFeed> getNewsFeedList() {
        return mNewsFeedList;
    }

    public void setOrientation(@Orientation int orientation) {
        mOrientation = orientation;
    }

    public void setOnBindViewHolderListener(OnBindViewHolderListener onBindViewHolderListener) {
        mOnBindViewHolderListener = onBindViewHolderListener;
    }

    public static class BottomNewsFeedViewHolder extends RecyclerView.ViewHolder {
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
