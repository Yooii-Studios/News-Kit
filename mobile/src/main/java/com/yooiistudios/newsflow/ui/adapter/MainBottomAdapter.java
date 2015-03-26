package com.yooiistudios.newsflow.ui.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.TintType;
import com.yooiistudios.newsflow.core.util.Display;
import com.yooiistudios.newsflow.core.util.Timestamp;
import com.yooiistudios.newsflow.model.PanelEditMode;
import com.yooiistudios.newsflow.model.ResizedImageLoader;
import com.yooiistudios.newsflow.model.news.NewsFeedFetchStateMessage;
import com.yooiistudios.newsflow.ui.PanelDecoration;
import com.yooiistudios.newsflow.ui.widget.MainBottomItemLayout;
import com.yooiistudios.newsflow.ui.widget.RatioFrameLayout;

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
    public interface OnBindMainBottomViewHolderListener {
        public void onBindViewHolder(BottomNewsFeedViewHolder viewHolder, int i);
    }

    public interface OnItemClickListener {
        public void onClick(BottomNewsFeedViewHolder viewHolder, NewsFeed newsFeed, int position);
        public void onLongClick();
        public void onClickEditButton(int position);
    }

    public static final int PORTRAIT = 0;
    public static final int LANDSCAPE = 1;
    private static final String TAG = MainBottomAdapter.class.getName();
    private static final String VIEW_NAME_POSTFIX = "_bottom_";
    private static final float HEIGHT_OVER_WIDTH_RATIO = 3.3f / 4.0f;

    private Context mContext;
    private ArrayList<NewsFeed> mNewsFeedList;
    private OnItemClickListener mOnItemClickListener;
    private ResizedImageLoader mImageLoader;

    private OnBindMainBottomViewHolderListener mOnBindMainBottomViewHolderListener;

    private PanelEditMode mEditMode = PanelEditMode.DEFAULT;

    private int mOrientation = PORTRAIT;

    @IntDef(value = {PORTRAIT, LANDSCAPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {}

    public MainBottomAdapter(Context context, ResizedImageLoader imageLoader,
                             OnItemClickListener listener) {
        this(context, imageLoader, listener, PORTRAIT);
    }

    public MainBottomAdapter(Context context, ResizedImageLoader imageLoader,
                             OnItemClickListener listener, @Orientation int orientation) {
        mContext = context.getApplicationContext();
        mNewsFeedList = new ArrayList<>();
        mOnItemClickListener = listener;
        mImageLoader = imageLoader;
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
                if (axis == RatioFrameLayout.AXIS_WIDTH &&
                        orientation == MainBottomItemLayout.LANDSCAPE) {
                    ViewGroup.LayoutParams lp = parent.getLayoutParams();
                    double ratio = mNewsFeedList.size() <= 4 ? 0.25 : 0.23;
                    double height = MainBottomAdapter.measureMaximumHeightOnLandscape(mContext, lp) * ratio;
                    return (int) Math.floor(height);
//                    return MainBottomAdapter.measureMaximumHeightOnLandscape(mContext, lp) * ratio;
                } else {
                    return -1;
                }
            }
        });
        BottomNewsFeedViewHolder viewHolder = new BottomNewsFeedViewHolder(itemLayout);
        initEditLayer(viewHolder);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BottomNewsFeedViewHolder viewHolder, final int position) {
        if (mOnBindMainBottomViewHolderListener != null) {
            mOnBindMainBottomViewHolderListener.onBindViewHolder(viewHolder, position);
        }
        adjustEditLayoutVisibility(viewHolder);
        configEditLayer(viewHolder, position);

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
        titleView.setSingleLine(isLandscape());
        ImageView imageView = viewHolder.imageView;
        TextView newsFeedTitleView = viewHolder.newsFeedTitleTextView;
        NewsFeed newsFeed = mNewsFeedList.get(position);

        viewHolder.itemView.setBackgroundColor(
                mContext.getResources().getColor(R.color.material_grey_black_1000));
        imageView.setBackgroundColor(PanelDecoration.getMainBottomDefaultBackgroundColor());

        if (newsFeed == null || !newsFeed.containsNews()) {
            newsFeedTitleView.setText("");
            titleView.setText(newsFeed != null ?
                    NewsFeedFetchStateMessage.getMessage(mContext, newsFeed) : "");
            showUnknownErrorImage(viewHolder);
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
                            mOnItemClickListener.onClick(viewHolder, newsFeed, position);
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

        if (!displayingNews.hasImageUrl()) {
            if (displayingNews.isImageUrlChecked()) {
                showDummyImage(viewHolder);
            }

            return;
        }

        Object tag = viewHolder.itemView.getTag(R.id.tag_main_bottom_image_request_idx);

       if (tag != null) {
            int previousRequestIdx = (Integer)tag;
            int currentNewsIndex = mNewsFeedList.get(position).getDisplayingNewsIndex();

            if (currentNewsIndex != previousRequestIdx) {
                tag = viewHolder.itemView.getTag(R.id.tag_main_bottom_image_request);
                if (tag != null) {
                    ((ImageLoader.ImageContainer) tag).cancelRequest();
                }
            }
        }
        ImageLoader.ImageContainer imageContainer =
                mImageLoader.getThumbnail(imageUrl, new ResizedImageLoader.ImageListener() {
                    @Override
                    public void onSuccess(ResizedImageLoader.ImageResponse response) {
                        Bitmap bitmap = response.bitmap;
                        viewHolder.progressBar.setVisibility(View.GONE);

                        viewHolder.imageView.setImageBitmap(bitmap);

                        // apply palette
//                            Palette palette = Palette.generate(bitmap);
//                            int vibrantColor = palette.getVibrantColor(Color.TRANSPARENT);
                        int vibrantColor = response.vibrantColor;
                        if (vibrantColor != Color.TRANSPARENT) {
                            int red = Color.red(vibrantColor);
                            int green = Color.green(vibrantColor);
                            int blue = Color.blue(vibrantColor);
                            int alpha = mContext.getResources().getInteger(R.integer.vibrant_color_tint_alpha);
                            viewHolder.imageView.setColorFilter(Color.argb(alpha, red, green, blue));
                            viewHolder.imageView.setTag(TintType.PALETTE);
                        } else {
                            viewHolder.imageView.setColorFilter(PanelDecoration.getBottomGrayFilterColor(mContext));
                            viewHolder.imageView.setTag(TintType.GRAY_SCALE_BOTTOM);
                        }
//                        } else {
//                            if (!displayingNews.isImageUrlChecked()) {
//                                // 뉴스의 이미지 url 이 있는지 체크가 안된 경우는 아직 기다려야 함.
//                                showLoading(viewHolder);
//                            } else {
//                                showDummyImage(viewHolder);
//                            }
                    }

                    @Override
                    public void onFail(VolleyError error) {
                        showDummyImage(viewHolder);
                    }
                });
        viewHolder.itemView.setTag(R.id.tag_main_bottom_image_request, imageContainer);
        viewHolder.itemView.setTag(R.id.tag_main_bottom_image_request_idx,
                mNewsFeedList.get(position).getDisplayingNewsIndex());
    }

    private void configEditLayer(BottomNewsFeedViewHolder viewHolder, final int position) {
        viewHolder.changeNewsfeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onClickEditButton(position);
            }
        });
    }

    private void showLoading(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.progressBar.setVisibility(View.VISIBLE);
//        viewHolder.progressBar.setVisibility(View.GONE);
        viewHolder.imageView.setImageDrawable(null);
        viewHolder.imageView.setColorFilter(null);
//        viewHolder.itemView.setOnClickListener(null);
    }

    private void showDummyImage(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.progressBar.setVisibility(View.GONE);
//        viewHolder.imageView.setImageBitmap(PanelDecoration.getDummyNewsImage(mContext));
        PanelDecoration.applyDummyNewsImageInto(mContext, viewHolder.imageView);
        viewHolder.imageView.setColorFilter(PanelDecoration.getBottomGrayFilterColor(mContext));
        viewHolder.imageView.setTag(TintType.DUMMY_BOTTOM);
//        setOnClickListener(viewHolder, position);
    }

    private void showUnknownErrorImage(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.progressBar.setVisibility(View.INVISIBLE);
        viewHolder.imageView.setImageDrawable(mContext.getResources()
                .getDrawable(R.drawable.img_rss_url_fail));
        viewHolder.imageView.setColorFilter(PanelDecoration.getBottomRssNotFoundImgFilterColor(mContext));
    }

    private void initEditLayer(BottomNewsFeedViewHolder viewHolder) {
        setEditMode(mEditMode);
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnItemClickListener.onLongClick();
                return true;
            }
        });
    }

    public void setEditMode(PanelEditMode editMode) {
        mEditMode = editMode;
    }

    private void adjustEditLayoutVisibility(BottomNewsFeedViewHolder viewHolder) {
        if (isInEditingMode()) {
            viewHolder.editLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.editLayout.setVisibility(View.GONE);
        }
    }

    public boolean isInEditingMode() {
        return mEditMode.equals(PanelEditMode.EDITING);
    }

    public static int measureMaximumHeightOnPortrait(Context context, int itemCount, int columnCount) {
        // get display width
        Point displaySize = Display.getDisplaySize(context);
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
        int height = Display.getDisplaySize(context).y;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // 롤리팝 이상 디바이스에서만 투명 스테이터스바가 적용된다.
            height -= Display.getStatusBarHeight(context);
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

    private boolean isPortrait() {
        return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private boolean isLandscape() {
        return !isPortrait();
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

    public void setOnBindMainBottomViewHolderListener(OnBindMainBottomViewHolderListener
                                                              onBindMainBottomViewHolderListener) {
        mOnBindMainBottomViewHolderListener = onBindMainBottomViewHolderListener;
    }

    public static class BottomNewsFeedViewHolder extends RecyclerView.ViewHolder {
        public TextView newsTitleTextView;
        public ImageView imageView;
        public ProgressBar progressBar;
        public TextView newsFeedTitleTextView;
        public FrameLayout editLayout;
        public View changeNewsfeedButton;

        public BottomNewsFeedViewHolder(View itemView) {
            super(itemView);
            newsTitleTextView = (TextView) itemView.findViewById(R.id.main_bottom_item_title);
            imageView = (ImageView) itemView.findViewById(R.id.main_bottom_item_image_view);
            progressBar = (ProgressBar) itemView.findViewById(R.id.main_bottom_item_progress);
            newsFeedTitleTextView = (TextView) itemView.findViewById(R.id.main_bottom_news_feed_title);
            editLayout = (FrameLayout)itemView.findViewById(R.id.main_bottom_edit_layout);
            changeNewsfeedButton = itemView.findViewById(R.id.main_bottom_replace_newsfeed);
        }
    }
}
