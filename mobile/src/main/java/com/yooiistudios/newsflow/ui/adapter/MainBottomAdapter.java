package com.yooiistudios.newsflow.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
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
    private static final String TAG = MainBottomAdapter.class.getName();
    private static final String VIEW_NAME_POSTFIX = "_bottom_";

    private Context mContext;
    private ArrayList<NewsFeed> mNewsFeedList;
    private OnItemClickListener mOnItemClickListener;
    private CacheImageLoader mImageLoader;

    private OnBindMainBottomViewHolderListener mOnBindMainBottomViewHolderListener;

    private PanelEditMode mEditMode = PanelEditMode.DEFAULT;

    private int mOrientation = PORTRAIT;

    @IntDef(value = {PORTRAIT, LANDSCAPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {}

    public MainBottomAdapter(Context context, CacheImageLoader imageLoader,
                             OnItemClickListener listener) {
        this(context, imageLoader, listener, PORTRAIT);
    }

    public MainBottomAdapter(Context context, CacheImageLoader imageLoader,
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
                    ViewGroup.LayoutParams lp = parent.getLayoutParams();
                    double ratio = mNewsFeedList.size() <= 4 ? 0.25 : 0.23;
                    double height = MainBottomItemLayout.measureMaximumHeightOnLandscape(mContext, lp) * ratio;
                    return (int) Math.floor(height);
//                    return MainBottomAdapter.measureMaximumHeightOnLandscape(mContext, lp) * ratio;
                } else {
                    return -1;
                }
            }
        });
        BottomNewsFeedViewHolder viewHolder = new BottomNewsFeedViewHolder(itemLayout);
        initEditLayer(viewHolder);

        // TODO 필요한지 체크해야함
//        viewHolder.itemView.setBackgroundColor(
//                mContext.getResources().getColor(R.color.material_grey_black_1000));
//        imageView.setBackgroundColor(PanelDecoration.getMainBottomDefaultBackgroundColor());

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
            setCurrentNewsTitle(viewHolder, position);
            setNewsTitleLineCount(viewHolder);

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
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
//                    showLoading(viewHolder);
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
        ThumbnailUrlSupplier urlSupplier = createThumbnailUrlSupplier(position);

//        mImageLoader.getThumbnail(displayingNews.getImageUrl(),
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

    private ThumbnailUrlSupplier createThumbnailUrlSupplier(int position) {
        NewsFeed newsFeed = getNewsFeedAt(position);
        String url = newsFeed.getDisplayingNews().getImageUrl();
        return new ThumbnailUrlSupplier(url, position);
    }

    private NewsFeed getNewsFeedAt(int position) {
        return mNewsFeedList.get(position);
    }

    private void resetViewStates(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.newsContentWrapper.setVisibility(View.VISIBLE);
        viewHolder.itemView.setOnClickListener(null);
        viewHolder.progressBar.setVisibility(View.GONE);
        adjustEditLayoutVisibility(viewHolder);
        viewHolder.statusLayout.setVisibility(View.GONE);
    }

    private void showNewsContent(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.newsContentWrapper.setVisibility(View.VISIBLE);

        viewHolder.progressBar.setVisibility(View.GONE);
        viewHolder.statusLayout.setVisibility(View.GONE);
        viewHolder.statusLayout.setBackground(null);
        viewHolder.statusImageView.setImageDrawable(null);
    }

    private void showLoading(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.newsContentWrapper.setVisibility(View.GONE);
        viewHolder.imageView.setImageDrawable(null);
        viewHolder.statusLayout.setVisibility(View.GONE);
        viewHolder.statusLayout.setBackground(null);
        viewHolder.statusImageView.setImageDrawable(null);

        viewHolder.progressBar.setVisibility(View.VISIBLE);
    }

    private void showDummyImage(BottomNewsFeedViewHolder viewHolder) {
        viewHolder.statusLayout.setVisibility(View.GONE);
        viewHolder.statusLayout.setBackground(null);
        viewHolder.statusImageView.setImageDrawable(null);
        viewHolder.progressBar.setVisibility(View.GONE);

        PanelDecoration.applySmallDummyNewsImageInto(mContext, mImageLoader, viewHolder.imageView);
//        viewHolder.imageView.setColorFilter(PanelDecoration.getDefaultBottomPaletteColor(mContext));
        viewHolder.imageView.setColorFilter(PanelDecoration.getBottomDummyImageFilterColor(mContext));
//        viewHolder.imageView.setTag(TintType.DUMMY_BOTTOM);
    }

    private void showErrorStatus(BottomNewsFeedViewHolder viewHolder, int position) {
        NewsFeed newsFeed = getNewsFeedAt(position);
        viewHolder.newsContentWrapper.setVisibility(View.GONE);
        viewHolder.imageView.setImageDrawable(null);

        viewHolder.progressBar.setVisibility(View.GONE);

        viewHolder.statusLayout.setVisibility(View.VISIBLE);
        viewHolder.statusLayout.setBackground(mContext.getResources()
                .getDrawable(R.drawable.img_rss_url_fail));
        viewHolder.statusImageView.setImageDrawable(mContext.getResources()
                .getDrawable(R.drawable.ic_rss_url_failed_small));
        String errorMessage = NewsFeedFetchStateMessage.getMessage(mContext, newsFeed);
        viewHolder.statusTextView.setText(errorMessage);

        // 방향에 따라 orientation 과 마진 설정 필요
        Resources resources = mContext.getResources();
        LinearLayout.LayoutParams textViewParams =
                (LinearLayout.LayoutParams) viewHolder.statusTextView.getLayoutParams();
        LinearLayout.LayoutParams imageViewParams =
                (LinearLayout.LayoutParams) viewHolder.statusImageView.getLayoutParams();

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

    private void cancelPreviousImageRequest(BottomNewsFeedViewHolder viewHolder) {
        cancelPreviousImageRequestIfNecessary(viewHolder, null);
    }

    private void cancelPreviousImageRequestIfNecessary(BottomNewsFeedViewHolder viewHolder,
                                                       int position) {
        ThumbnailUrlSupplier currentUrlSupplier = createThumbnailUrlSupplier(position);
        cancelPreviousImageRequestIfNecessary(viewHolder, currentUrlSupplier);
    }

    private void cancelPreviousImageRequestIfNecessary(BottomNewsFeedViewHolder viewHolder,
                                                       ThumbnailUrlSupplier currentImageUrlSupplier) {
        Object urlSupplierTag = viewHolder.itemView.getTag(R.id.tag_main_bottom_image_url_supplier);

        if (urlSupplierTag != null && urlSupplierTag instanceof ThumbnailUrlSupplier) {
            ThumbnailUrlSupplier urlSupplier = (ThumbnailUrlSupplier) urlSupplierTag;
            if (currentImageUrlSupplier == null || !currentImageUrlSupplier.equals(urlSupplier)) {
                mImageLoader.cancelRequest(urlSupplier);
            }
        }
    }

    private void applyImage(BottomNewsFeedViewHolder viewHolder, CacheImageLoader.ImageResponse response) {
        Bitmap bitmap = response.bitmap;

        viewHolder.imageView.setImageBitmap(bitmap);

        int vibrantColor = response.paletteColor.getVibrantColor();
        if (response.paletteColor.hasValidVibrantColor()) {
//            int red = Color.red(vibrantColor);
//            int green = Color.green(vibrantColor);
//            int blue = Color.blue(vibrantColor);
//            int alpha = mContext.getResources().getInteger(R.integer.vibrant_color_tint_alpha);

            int filterColor = PanelDecoration.getPaletteColorWithAlpha(mContext, vibrantColor);
            viewHolder.imageView.setColorFilter(filterColor);
        } else {
            viewHolder.imageView.setColorFilter(PanelDecoration.getDefaultBottomPaletteColor(mContext));
        }
    }

    private void setNewsTitleLineCount(BottomNewsFeedViewHolder viewHolder) {
        if (Device.isPortrait(mContext)) {
            // TODO 리소스의 maxLines 와 통일시켜야 함
            viewHolder.newsTitleTextView.setMaxLines(3);
            viewHolder.newsTitleTextView.setSingleLine(false);
        } else {
            viewHolder.newsTitleTextView.setSingleLine(true);
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
        viewHolder.newsFeedTitleTextView.setText(getNewsFeedAt(position).getTitle());
    }

    private void setCurrentNewsTitle(BottomNewsFeedViewHolder viewHolder, int position) {
        News displayingNews = getNewsFeedAt(position).getDisplayingNews();
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
        public View newsContentWrapper;
        public TextView newsTitleTextView;
        public TextView newsFeedTitleTextView;
        public ImageView imageView;
        public ProgressBar progressBar;
        public FrameLayout editLayout;
        public View changeNewsfeedButton;
        public LinearLayout statusLayout;
        public ImageView statusImageView;
        public TextView statusTextView;

        public BottomNewsFeedViewHolder(View itemView) {
            super(itemView);
            newsContentWrapper = itemView.findViewById(R.id.main_bottom_content);
            newsTitleTextView = (TextView) itemView.findViewById(R.id.main_bottom_item_title);
            newsFeedTitleTextView = (TextView) itemView.findViewById(R.id.main_bottom_news_feed_title);
            imageView = (ImageView) itemView.findViewById(R.id.main_bottom_item_image_view);
            progressBar = (ProgressBar) itemView.findViewById(R.id.main_bottom_item_progress);
            editLayout = (FrameLayout)itemView.findViewById(R.id.main_bottom_edit_layout);
            changeNewsfeedButton = itemView.findViewById(R.id.main_bottom_replace_newsfeed);
            statusLayout = (LinearLayout) itemView.findViewById(R.id.main_bottom_status_layout);
            statusImageView = (ImageView) itemView.findViewById(R.id.main_bottom_status_imageview);
            statusTextView = (TextView) itemView.findViewById(R.id.main_bottom_status_textview);
        }
    }

    private static class ThumbnailUrlSupplier extends CacheImageLoader.UrlSupplier {
        private final String mUrl;
        private final int mNewsFeedPosition;

        public ThumbnailUrlSupplier(String url, int newsFeedPosition) {
            this.mUrl = url;
            this.mNewsFeedPosition = newsFeedPosition;
        }

        public int getNewsFeedPosition() {
            return mNewsFeedPosition;
        }

        @Override
        public String getUrl() {
            return mUrl;
        }

        @Override
        public int hashCode() {
            return getUrl().hashCode() * mNewsFeedPosition;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ThumbnailUrlSupplier) {
                ThumbnailUrlSupplier supplier = (ThumbnailUrlSupplier) o;
                return getUrl().equals(supplier.getUrl())
                        && getNewsFeedPosition() == supplier.getNewsFeedPosition();
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return super.toString() + "\nposition: " + mNewsFeedPosition;
        }
    }
}
