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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.NewsFeedFetchState;
import com.yooiistudios.newsflow.core.news.TintType;
import com.yooiistudios.newsflow.core.util.Display;
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
                new ResizedImageLoader.ImageListener() {
                    @Override
                    public void onSuccess(ResizedImageLoader.ImageResponse response) {
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

        PanelDecoration.applySmallDummyNewsImageInto(mImageLoader, viewHolder.imageView);
        viewHolder.imageView.setColorFilter(PanelDecoration.getBottomGrayFilterColor(mContext));
        viewHolder.imageView.setTag(TintType.DUMMY_BOTTOM);
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

    private void applyImage(BottomNewsFeedViewHolder viewHolder, ResizedImageLoader.ImageResponse response) {
        Bitmap bitmap = response.bitmap;

        viewHolder.imageView.setImageBitmap(bitmap);

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
    }

    private void setNewsTitleLineCount(BottomNewsFeedViewHolder viewHolder) {
        if (isPortrait()) {
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

    private static class ThumbnailUrlSupplier extends ResizedImageLoader.UrlSupplier {
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
