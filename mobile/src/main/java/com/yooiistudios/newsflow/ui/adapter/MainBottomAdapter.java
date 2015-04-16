package com.yooiistudios.newsflow.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.NewsFeedFetchState;
import com.yooiistudios.newsflow.core.util.Device;
import com.yooiistudios.newsflow.model.PanelEditMode;
import com.yooiistudios.newsflow.model.cache.NewsImageLoader;
import com.yooiistudios.newsflow.model.cache.NewsUrlSupplier;
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
        void onBindViewHolder(BottomNewsFeedViewHolder viewHolder, int i);
    }

    public interface OnItemClickListener {
        void onClick(BottomNewsFeedViewHolder viewHolder, NewsFeed newsFeed, int position);
        void onLongClick();
        void onClickEditButton(int position);
    }

    public static final int PORTRAIT = 0;
    public static final int LANDSCAPE = 1;
//    private static final String TAG = MainBottomAdapter.class.getName();
//    private static final String VIEW_NAME_POSTFIX = "_bottom_";

    private Context mContext;
    private ArrayList<NewsFeed> mNewsFeedList;
    private OnItemClickListener mOnItemClickListener;
    private NewsImageLoader mImageLoader;

    private OnBindMainBottomViewHolderListener mOnBindMainBottomViewHolderListener;

    private PanelEditMode mEditMode = PanelEditMode.DEFAULT;

    private int mOrientation = PORTRAIT;

    @IntDef(value = {PORTRAIT, LANDSCAPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {}

    public MainBottomAdapter(Context context, NewsImageLoader imageLoader,
                             OnItemClickListener listener) {
        this(context, imageLoader, listener, PORTRAIT);
    }

    public MainBottomAdapter(Context context, NewsImageLoader imageLoader,
                             OnItemClickListener listener, @Orientation int orientation) {
        mContext = context.getApplicationContext();
        mNewsFeedList = new ArrayList<>();
        mOnItemClickListener = listener;
        mImageLoader = imageLoader;
        mOrientation = orientation;
    }

    @Override
    public void onViewRecycled(BottomNewsFeedViewHolder viewHolder) {
        super.onViewRecycled(viewHolder);
        cancelPreviousImageRequest(viewHolder);
    }

    @Override
    public BottomNewsFeedViewHolder onCreateViewHolder(final ViewGroup parent, int i) {
        Context context = parent.getContext();
        MainBottomItemLayout itemLayout = (MainBottomItemLayout)
                LayoutInflater.from(context).inflate(R.layout.main_bottom_item, parent, false);
        itemLayout.setOnSupplyTargetAxisLengthListener(new MainBottomItemLayout.OnSupplyTargetAxisLengthListener() {
            @Override
            public int onSupply(@RatioFrameLayout.Axis int axis, @MainBottomItemLayout.Orientation int orientation) {
                if (axis == RatioFrameLayout.AXIS_WIDTH &&
                         orientation == MainBottomItemLayout.LANDSCAPE) {
                    // 4개 이상일 경우 넥5 기준으로 5번째 아이템 제목 밑 공간이 조금 보일 정도로 ratio 를 잡아줌
                    float ratio = mNewsFeedList.size() <= 4 ? 0.25f : 0.215f;
                    float parentHeight = MainBottomItemLayout.measureParentHeightOnLandscape(parent);
                    return (int) Math.floor(parentHeight * ratio);
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
        notifyOnBindViewHolder(viewHolder, position);
        resetViewStates(viewHolder);
        initItemView(viewHolder);
        initEditLayerListener(viewHolder, position);

        final NewsFeed newsFeed = getNewsFeedAt(position);

        if (newsFeed.isDisplayable()) {
            showNewsContent(viewHolder);
            setNewsFeedTitle(viewHolder, position);
            setNewsTitleLineCount(viewHolder);
            setCurrentNewsTitle(viewHolder, position);

            initOnClickListener(viewHolder, position, newsFeed);

            News displayingNews = newsFeed.getDisplayingNews();
            if (displayingNews.hasImageUrl()) {
                cancelPreviousImageRequestIfNecessary(viewHolder, position);
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                loadImage(viewHolder, position);
//                showDummyImage(viewHolder);
            } else {
                cancelPreviousImageRequest(viewHolder);
                if (displayingNews.isImageUrlChecked()) {
                    showDummyImage(viewHolder);
                } else {
//                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    showLoading(viewHolder);
                    viewHolder.newsContentWrapper.setVisibility(View.VISIBLE);
                }
            }
        } else {
            cancelPreviousImageRequest(viewHolder);

            NewsFeedFetchState fetchState = newsFeed.getNewsFeedFetchState();
            if (fetchState.equals(NewsFeedFetchState.NOT_FETCHED_YET)) {
                showLoading(viewHolder);
            } else {
                showErrorStatus(viewHolder, position);
            }
        }
    }

    private void loadImage(final BottomNewsFeedViewHolder viewHolder, int position) {
        NewsUrlSupplier urlSupplier = createThumbnailUrlSupplier(position);

        mImageLoader.getThumbnail(urlSupplier,
                new CacheImageLoader.ImageListener() {
                    @Override
                    public void onSuccess(CacheImageLoader.ImageResponse response) {
                        viewHolder.itemView.setTag(R.id.tag_main_bottom_image_url_supplier, null);
                        showNewsContent(viewHolder);
                        applyImage(viewHolder, response);
                    }

                    @Override
                    public void onFail(VolleyError error) {
                        showDummyImage(viewHolder);
                    }
                });
        viewHolder.itemView.setTag(R.id.tag_main_bottom_image_url_supplier, urlSupplier);
    }

    private NewsUrlSupplier createThumbnailUrlSupplier(int position) {
        NewsFeed newsFeed = getNewsFeedAt(position);
        News news = newsFeed.getDisplayingNews();
        return new NewsUrlSupplier(news, position);
    }

    private NewsFeed getNewsFeedAt(int position) {
        return mNewsFeedList.get(position);
    }

    private void resetViewStates(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.newsContentWrapper.setVisibility(View.VISIBLE);
        viewHolder.itemView.setOnClickListener(null);
        viewHolder.progressBar.setVisibility(View.GONE);
        adjustEditLayoutVisibility(viewHolder);

        setErrorLayoutGone(viewHolder);
    }

    private void showNewsContent(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.newsContentWrapper.setVisibility(View.VISIBLE);
        viewHolder.progressBar.setVisibility(View.GONE);
        setErrorLayoutGone(viewHolder);
    }

    private void showLoading(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.newsContentWrapper.setVisibility(View.GONE);
        viewHolder.imageView.setImageDrawable(null);
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        setErrorLayoutGone(viewHolder);
    }

    private void showDummyImage(BottomNewsFeedViewHolder viewHolder) {
        setErrorLayoutGone(viewHolder);

        viewHolder.progressBar.setVisibility(View.GONE);

        PanelDecoration.applySmallDummyNewsImageInto(mContext, mImageLoader, viewHolder.imageView);
        viewHolder.imageView.setColorFilter(PanelDecoration.getBottomDummyImageFilterColor(mContext),
                PorterDuff.Mode.SRC_OVER);
    }

    private void showErrorStatus(BottomNewsFeedViewHolder viewHolder, int position) {
        viewHolder.newsContentWrapper.setVisibility(View.GONE);
        viewHolder.imageView.setImageDrawable(null);
        viewHolder.progressBar.setVisibility(View.GONE);

        setErrorLayoutVisible(viewHolder);

        NewsFeed newsFeed = getNewsFeedAt(position);
        String errorMessage = NewsFeedFetchStateMessage.getMessage(mContext, newsFeed);
        viewHolder.statusTextView.setText(errorMessage);

        // 방향에 따라 orientation 과 마진 설정 필요
        Resources resources = mContext.getResources();
        LinearLayout.LayoutParams textViewParams =
                (LinearLayout.LayoutParams) viewHolder.statusTextView.getLayoutParams();
        LinearLayout.LayoutParams imageViewParams =
                (LinearLayout.LayoutParams) viewHolder.statusIconImageView.getLayoutParams();

        if (Device.isPortrait(mContext)) {
            viewHolder.statusLayout.setOrientation(LinearLayout.VERTICAL);
            textViewParams.leftMargin = 0;
            textViewParams.topMargin = resources.getDimensionPixelSize(R.dimen.base_margin_middle);

            int imageViewSize = resources.getDimensionPixelSize(
                    R.dimen.main_bottom_status_image_view_size_portrait);
            imageViewParams.width = imageViewSize;
            imageViewParams.height = imageViewSize;
        } else if (Device.isLandscape(mContext)) {
            viewHolder.statusLayout.setOrientation(LinearLayout.HORIZONTAL);
            textViewParams.leftMargin = resources.getDimensionPixelSize(R.dimen.base_margin);
            textViewParams.topMargin = 0;

            int imageViewSize = resources.getDimensionPixelSize(
                    R.dimen.main_bottom_status_image_view_size_landscape);
            imageViewParams.width = imageViewSize;
            imageViewParams.height = imageViewSize;
        }
    }

    private void setErrorLayoutVisible(BottomNewsFeedViewHolder viewHolder) {
//        viewHolder.statusLayout.setVisibility(View.VISIBLE);
//        viewHolder.statusLayout.setBackgroundResource(R.drawable.img_rss_url_failed_small);
        viewHolder.statusWrapper.setVisibility(View.VISIBLE);
        PanelDecoration.applyRssUrlFailedSmallBackgroundInto(mContext, mImageLoader, viewHolder.statusBackgroundImageView);
        viewHolder.statusIconImageView.setImageResource(R.drawable.ic_rss_url_failed_small);
    }

    private void setErrorLayoutGone(BottomNewsFeedViewHolder viewHolder) {
//        viewHolder.statusLayout.setVisibility(View.GONE);
//        viewHolder.statusLayout.setBackground(null);
        viewHolder.statusWrapper.setVisibility(View.GONE);
        viewHolder.statusBackgroundImageView.setImageBitmap(null);
        viewHolder.statusIconImageView.setImageDrawable(null);
    }

    private void cancelPreviousImageRequest(BottomNewsFeedViewHolder viewHolder) {
        cancelPreviousImageRequestIfNecessary(viewHolder, null);
    }

    private void cancelPreviousImageRequestIfNecessary(BottomNewsFeedViewHolder viewHolder,
                                                       int position) {
        NewsUrlSupplier currentUrlSupplier = createThumbnailUrlSupplier(position);
        cancelPreviousImageRequestIfNecessary(viewHolder, currentUrlSupplier);
    }

    private void cancelPreviousImageRequestIfNecessary(BottomNewsFeedViewHolder viewHolder,
                                                       NewsUrlSupplier currentImageUrlSupplier) {
        Object urlSupplierTag = viewHolder.itemView.getTag(R.id.tag_main_bottom_image_url_supplier);

        if (urlSupplierTag != null && urlSupplierTag instanceof NewsUrlSupplier) {
            NewsUrlSupplier urlSupplier = (NewsUrlSupplier) urlSupplierTag;
            if (currentImageUrlSupplier == null || !currentImageUrlSupplier.equals(urlSupplier)) {
                mImageLoader.cancelRequest(urlSupplier);
            }
        }
    }

    private void applyImage(BottomNewsFeedViewHolder viewHolder, CacheImageLoader.ImageResponse response) {
        Bitmap bitmap = response.bitmap;

        viewHolder.imageView.setImageBitmap(bitmap);

        int paletteColor = response.paletteColor.getPaletteColor();
        if (response.paletteColor.isCustom()) {
            int filterColor = PanelDecoration.getRandomPaletteColorWithAlpha(mContext, paletteColor);
            viewHolder.imageView.setColorFilter(filterColor, PorterDuff.Mode.SRC_OVER);
        } else {
            int filterColor = PanelDecoration.getPaletteColorWithAlpha(mContext, paletteColor);
            viewHolder.imageView.setColorFilter(filterColor, PorterDuff.Mode.SRC_OVER);
        }
    }

    private void setNewsTitleLineCount(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.newsTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
        if (Device.isPortrait(mContext)) {
            viewHolder.newsTitleTextView.setMaxLines(3);
        } else {
            viewHolder.newsTitleTextView.setMaxLines(1);
        }
    }

    private void notifyOnBindViewHolder(BottomNewsFeedViewHolder viewHolder, int position) {
        if (mOnBindMainBottomViewHolderListener != null) {
            mOnBindMainBottomViewHolderListener.onBindViewHolder(viewHolder, position);
        }
    }

    private void initItemView(BottomNewsFeedViewHolder viewHolder) {
        MainBottomItemLayout itemView = (MainBottomItemLayout)viewHolder.itemView;
        itemView.setBaseAxis(RatioFrameLayout.AXIS_WIDTH);
        itemView.setOrientation(
                usePortraitLayout() ? MainBottomItemLayout.PORTRAIT : MainBottomItemLayout.LANDSCAPE
        );
    }

    private void initEditLayerListener(BottomNewsFeedViewHolder viewHolder, final int position) {
        // TODO 바깥쪽 클릭할 경우 editMode 해제하는 기능 추가해야 함
        viewHolder.changeNewsfeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onClickEditButton(position);
            }
        });
    }

    private void setNewsFeedTitle(BottomNewsFeedViewHolder viewHolder, int position) {
        if (Device.isPortrait(mContext)) {
            viewHolder.newsFeedTitleTextView.setGravity(Gravity.END);
        } else {
            viewHolder.newsFeedTitleTextView.setGravity(Gravity.START);
        }
        viewHolder.newsFeedTitleTextView.setText(getNewsFeedAt(position).getTitle());
    }

    private void setCurrentNewsTitle(BottomNewsFeedViewHolder viewHolder, int position) {
        News displayingNews = getNewsFeedAt(position).getDisplayingNews();
        if (Device.isPortrait(mContext)) {
            viewHolder.newsTitleTextView.setGravity(Gravity.CENTER);
        } else {
            viewHolder.newsTitleTextView.setGravity(Gravity.START);
        }
        viewHolder.newsTitleTextView.setText(displayingNews.getTitle());
    }

    private void initOnClickListener(final BottomNewsFeedViewHolder viewHolder, final int position,
                                     final NewsFeed newsFeed) {
        viewHolder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemClickListener.onClick(viewHolder, newsFeed, position);
                    }
                }
        );
    }

    private boolean usePortraitLayout() {
        return mOrientation == PORTRAIT;
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

    @Override
    public int getItemCount() {
        return mNewsFeedList.size();
    }

    public void addNewsFeed(NewsFeed newsFeed) {
        mNewsFeedList.add(newsFeed);
        notifyItemInserted(mNewsFeedList.size() - 1);
    }


    /*
    public void addNewsFeedAt(NewsFeed newsFeed, int idx) {
        mNewsFeedList.add(idx, newsFeed);
        notifyItemInserted(idx);
    }

//    public void addNewsFeedList(List<NewsFeed> newsFeedListToAdd) {
//        int notifyStartIdx = mNewsFeedList.size();
//        mNewsFeedList.addAll(newsFeedListToAdd);
//        notifyItemRangeInserted(notifyStartIdx, newsFeedListToAdd.size());
//    }
    */

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
        public View contentWrapper;
        public View newsContentWrapper;
        public TextView newsTitleTextView;
        public TextView newsFeedTitleTextView;
        public ImageView imageView;
        public ProgressBar progressBar;
        public FrameLayout editLayout;
        public View changeNewsfeedButton;
        public FrameLayout statusWrapper;
        public LinearLayout statusLayout;
        public ImageView statusBackgroundImageView;
        public ImageView statusIconImageView;
        public TextView statusTextView;

        public BottomNewsFeedViewHolder(View itemView) {
            super(itemView);
            contentWrapper = itemView.findViewById(R.id.main_bottom_item_anim_content);
            newsContentWrapper = itemView.findViewById(R.id.main_bottom_item_news_content);
            newsTitleTextView = (TextView) itemView.findViewById(R.id.main_bottom_item_title);
            newsFeedTitleTextView = (TextView) itemView.findViewById(R.id.main_bottom_news_feed_title);
            imageView = (ImageView) itemView.findViewById(R.id.main_bottom_item_image_view);
            progressBar = (ProgressBar) itemView.findViewById(R.id.main_bottom_item_progress);
            editLayout = (FrameLayout)itemView.findViewById(R.id.main_bottom_edit_layout);
            changeNewsfeedButton = itemView.findViewById(R.id.main_bottom_replace_newsfeed);
            statusWrapper = (FrameLayout) itemView.findViewById(R.id.main_bottom_status_wrapper);
            statusLayout = (LinearLayout) itemView.findViewById(R.id.main_bottom_status_layout);
            statusBackgroundImageView = (ImageView) itemView.findViewById(R.id.main_bottom_status_background_imageview);
            statusIconImageView = (ImageView) itemView.findViewById(R.id.main_bottom_status_icon_imageview);
            statusTextView = (TextView) itemView.findViewById(R.id.main_bottom_status_textview);
        }
    }
}
