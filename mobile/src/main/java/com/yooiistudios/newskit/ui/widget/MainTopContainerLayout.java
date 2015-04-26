package com.yooiistudios.newskit.ui.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.ads.AdSize;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.RssFetchable;
import com.yooiistudios.newskit.core.news.database.NewsDb;
import com.yooiistudios.newskit.core.news.util.NewsFeedArchiveUtils;
import com.yooiistudios.newskit.core.ui.animation.activitytransition.ActivityTransitionHelper;
import com.yooiistudios.newskit.core.util.Device;
import com.yooiistudios.newskit.core.util.Display;
import com.yooiistudios.newskit.iab.IabProducts;
import com.yooiistudios.newskit.model.PanelEditMode;
import com.yooiistudios.newskit.model.cache.NewsImageLoader;
import com.yooiistudios.newskit.model.cache.NewsUrlSupplier;
import com.yooiistudios.newskit.model.news.NewsFeedFetchStateMessage;
import com.yooiistudios.newskit.model.news.task.TopFeedNewsImageUrlFetchTask;
import com.yooiistudios.newskit.model.news.task.TopNewsFeedFetchTask;
import com.yooiistudios.newskit.ui.PanelDecoration;
import com.yooiistudios.newskit.ui.activity.MainActivity;
import com.yooiistudios.newskit.ui.activity.NewsFeedDetailActivity;
import com.yooiistudios.newskit.ui.activity.NewsSelectActivity;
import com.yooiistudios.newskit.ui.adapter.MainTopPagerAdapter;
import com.yooiistudios.newskit.ui.animation.AnimationFactory;
import com.yooiistudios.newskit.ui.fragment.MainTopFragment;
import com.yooiistudios.newskit.ui.widget.viewpager.MainTopViewPager;
import com.yooiistudios.newskit.ui.widget.viewpager.ParallexViewPagerIndicator;
import com.yooiistudios.newskit.ui.widget.viewpager.SlowSpeedScroller;
import com.yooiistudios.newskit.util.OnMainPanelEditModeEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.yooiistudios.newskit.ui.activity.MainActivity.INTENT_KEY_TRANSITION_PROPERTY;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 19.
 *
 * MainTopViewPager
 *  메인화면 상단 뷰페이저
 */
public class MainTopContainerLayout extends FrameLayout
        implements TopFeedNewsImageUrlFetchTask.OnTopFeedImageUrlFetchListener,
        TopNewsFeedFetchTask.OnFetchListener,
        MainTopPagerAdapter.OnItemClickListener,
        View.OnLongClickListener {
    @InjectView(R.id.main_top_content_wrapper)                  FrameLayout mContentWrapper;
    @InjectView(R.id.main_top_view_pager_wrapper)               FrameLayout mViewPagerWrapper;
    @InjectView(R.id.main_top_view_pager)                       MainTopViewPager mViewPager;
    @InjectView(R.id.main_top_view_pager_indicator)             ParallexViewPagerIndicator mViewPagerIndicator;
    @InjectView(R.id.main_top_news_feed_title_text_view)        TextView mNewsFeedTitleTextView;
    @InjectView(R.id.main_top_unavailable_wrapper)              FrameLayout mUnavailableWrapper;
    @InjectView(R.id.main_top_unavailable_background_imageview) ImageView mUnavailableBackgroundImageView;
    @InjectView(R.id.main_top_unavailable_icon_imageview)       ImageView mUnavailableIconImageView;
    @InjectView(R.id.main_top_unavailable_textview)             TextView mUnavailableTextView;
    @InjectView(R.id.main_top_edit_layout)                      FrameLayout mEditLayout;
    @InjectView(R.id.main_top_replace_newsfeed)                 View mReplaceButton;

//    private static final String TAG = MainTopContainerLayout.class.getName();

    private TopNewsFeedFetchTask mTopNewsFeedFetchTask;
    private HashMap<News, TopFeedNewsImageUrlFetchTask> mImageTaskMap;
    private MainTopPagerAdapter mTopNewsFeedPagerAdapter;

    private OnMainTopLayoutEventListener mOnEventListener;
    private OnMainPanelEditModeEventListener mEditModeListener;

    private NewsImageLoader mImageLoader;
    private MainActivity mActivity;

    private PanelEditMode mEditMode = PanelEditMode.DEFAULT;

    // flags for initializing
    private boolean mIsReady = false;

    // interface
    public interface OnMainTopLayoutEventListener {
        void onMainTopInitialLoad();
        void onTopNewsFeedFetch();
        void onMainTopRefresh();
        void onStartNewsFeedDetailActivityFromTopNewsFeed(Intent intent);
        void onStartNewsFeedSelectActivityFromTopNewsFeed(Intent intent);
        void onTouchTopEditLayout();
    }

    // constructors
    public MainTopContainerLayout(Context context) {
        super(context);

        _init(context);
    }

    public MainTopContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        _init(context);
    }

    public MainTopContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        _init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MainTopContainerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        _init(context);
    }

    private void _init(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.main_top_container, this, false);
        addView(root);

        ButterKnife.inject(this);

        initEditLayer();
        setOnLongClickListener(this);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.main_top_view_pager_page_margin));
    }

    private void initImageLoader() {
        mImageLoader = mActivity.getImageLoader();
    }

    private void initEditLayer() {
        hideEditLayout();
        adjustEditLayoutPosition();
        mEditLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnEventListener.onTouchTopEditLayout();
            }
        });
        mReplaceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, NewsSelectActivity.class);
                intent = makeNewsSelectIntent(intent);
                mOnEventListener.onStartNewsFeedSelectActivityFromTopNewsFeed(intent);
            }
        });
    }

    private void adjustEditLayoutPosition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarHeight = Display.getStatusBarHeight(getContext().getApplicationContext());
            mEditLayout.setPadding(0, statusBarHeight, 0, 0);
        }
    }

    public NewsFeed getNewsFeed() {
        return mTopNewsFeedPagerAdapter.getNewsFeed();
    }

    public void autoRefreshTopNewsFeed() {
        NewsFeed newsFeed = mTopNewsFeedPagerAdapter.getNewsFeed();
        if (newsFeed == null || !newsFeed.containsNews()) {
            // 네트워크도 없고 캐시 정보도 없는 경우
            return;
        }
        if (mViewPager.getCurrentItem() + 1 < mViewPager.getAdapter().getCount()) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
        } else {
            mViewPager.setCurrentItem(0, true);
        }
    }

    public void init(Activity activity) {
        if (!(activity instanceof MainActivity)) {
            throw new IllegalArgumentException("activity MUST BE an instance of "
                    + MainActivity.class.getSimpleName());
        }

        mActivity = (MainActivity)activity;
        mOnEventListener = (OnMainTopLayoutEventListener)activity;
        mEditModeListener = (OnMainPanelEditModeEventListener)activity;

        Context context = getContext();

        initImageLoader();
        initViewPager();

        // Fetch
        NewsFeed topNewsFeed = NewsDb.getInstance(context).loadTopNewsFeed(context);
        mTopNewsFeedPagerAdapter.setNewsFeed(topNewsFeed);

        boolean needsRefresh = NewsFeedArchiveUtils.newsNeedsToBeRefreshed(context);
        if (needsRefresh) {
            mIsReady = false;
            fetchTopNewsFeed(TopNewsFeedFetchTask.TaskType.INITIALIZE);
        } else {
            if (mTopNewsFeedPagerAdapter.getNewsFeed().isDisplayable()) {
                notifyNewTopNewsFeedSet();
                fetchFirstNewsImage(TopFeedNewsImageUrlFetchTask.TaskType.INITIALIZE);
            } else {
                mIsReady = false;
                fetchTopNewsFeed(TopNewsFeedFetchTask.TaskType.INITIALIZE);
            }
        }

    }

    private void initViewPager() {
        // ViewPager
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            android.view.animation.Interpolator interpolator =
                    (android.view.animation.Interpolator)
                        AnimationFactory.makeViewPagerScrollInterpolator();
            SlowSpeedScroller scroller = new SlowSpeedScroller(getContext(), interpolator, true);
            mScroller.set(mViewPager, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mTopNewsFeedPagerAdapter = new MainTopPagerAdapter(mActivity.getFragmentManager());
    }

    public boolean isReady() {
        return mIsReady;
    }

    private void notifyOnReady(TopNewsFeedFetchTask.TaskType taskType) {
        mIsReady = true;

        switch (taskType) {
            case INITIALIZE:
                mOnEventListener.onMainTopInitialLoad();
                break;
            case SWIPE_REFRESH:
            case REPLACE:
                mOnEventListener.onMainTopRefresh();
                break;
        }
    }

    private void notifyOnReady(TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        mIsReady = true;

        switch (taskType) {
            case INITIALIZE:
                mOnEventListener.onMainTopInitialLoad();
                break;
            case REFRESH:
            case REPLACE:
                mOnEventListener.onMainTopRefresh();
                break;
        }
    }

    public void showEditLayout() {
        setEditMode(PanelEditMode.EDITING);
        adjustEditLayoutVisibility();
    }

    public void hideEditLayout() {
        setEditMode(PanelEditMode.NONE);
        adjustEditLayoutVisibility();
    }

    @Override
    public boolean onLongClick(View v) {
        mEditModeListener.onEditModeChange(PanelEditMode.EDITING);
        return true;
    }

    private void setEditMode(PanelEditMode editMode) {
        mEditMode = editMode;
    }

    private void adjustEditLayoutVisibility() {
        if (isInEditingMode()) {
            mEditLayout.setVisibility(View.VISIBLE);
        } else {
            mEditLayout.setVisibility(View.GONE);
        }
    }

    public boolean isInEditingMode() {
        return mEditMode.equals(PanelEditMode.EDITING);
    }

    private void fetchTopNewsFeed(TopNewsFeedFetchTask.TaskType taskType) {
        fetchTopNewsFeed(taskType, true);
    }

    private void fetchTopNewsFeed(TopNewsFeedFetchTask.TaskType taskType, boolean shuffle) {
        mTopNewsFeedFetchTask = new TopNewsFeedFetchTask(mTopNewsFeedPagerAdapter.getNewsFeed(),
                this, taskType, shuffle);
        mTopNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void applyNewsTopic(RssFetchable rssFetchable) {
        mTopNewsFeedFetchTask = new TopNewsFeedFetchTask(rssFetchable, this,
                TopNewsFeedFetchTask.TaskType.REPLACE, true);
        mTopNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void notifyNewTopNewsFeedSet() {
        // show view pager wrapper
        mViewPagerWrapper.setVisibility(View.VISIBLE);
//        mUnavailableWrapper.setBackground(null);
        mUnavailableWrapper.setVisibility(View.GONE);

        mViewPager.setAdapter(mTopNewsFeedPagerAdapter);

        // init 안에 VISIBLE 을 안 넣은 이유는 탑 뉴스를 교체하고 돌아와서
        // onResume 이 불리거나 할 경우에 보이는 것을 방지하기 위함
        mViewPagerIndicator.setVisibility(View.VISIBLE);
        initViewPagerIndicator();

        mNewsFeedTitleTextView.setText(mTopNewsFeedPagerAdapter.getNewsFeed().getTitle());
    }

    private void initViewPagerIndicator() {
        if (mViewPager.getAdapter() != null) {
            mViewPagerIndicator.initialize(mTopNewsFeedPagerAdapter.getCount(), mViewPager);
            mViewPagerIndicator.setPage(mViewPager.getCurrentItem());
        }
    }

    private void fetchFirstNewsImage(TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        ArrayList<News> items = mTopNewsFeedPagerAdapter.getNewsFeed().getNewsList();
        if (items.size() > 0) {
            News news = items.get(0);

            if (!news.isImageUrlChecked()) {
                mIsReady = false;
                TopFeedNewsImageUrlFetchTask imageUrlFetchTask =
                        new TopFeedNewsImageUrlFetchTask(news, 0, taskType, this);
                imageUrlFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                if (!news.hasImageUrl()) {
                    // no image
                    notifyOnReady(taskType);
                } else {
                    // 이미지 url 은 가져온 상태.
                    applyImage(news, 0, taskType);
                }
            }
        } else {
            notifyOnReady(taskType);
        }
    }

    private void showUnavailableStatus(String message) {
        // show top unavailable wrapper
        mViewPagerWrapper.setVisibility(View.GONE);
        mViewPagerIndicator.setVisibility(View.INVISIBLE);

        mUnavailableWrapper.setVisibility(View.VISIBLE);

        Context context = getContext().getApplicationContext();
        mUnavailableIconImageView.setImageResource(R.drawable.ic_rss_url_failed_large);
        PanelDecoration.applyRssUrlFailedBackgroundInto(
                context, mImageLoader, mUnavailableBackgroundImageView, null);
        mUnavailableTextView.setText(message);

        adjustUnavailableIconImageViewTopMargin();
    }

    private void adjustUnavailableIconImageViewTopMargin() {
        if (Device.hasLollipop()) {
            int statusBarHeight = Display.getStatusBarHeight(getContext());
            if (statusBarHeight > 0) {
                mUnavailableWrapper.setPadding(0, statusBarHeight, 0, 0);
            }
        }
    }

    private void fetchTopNewsFeedImages(TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        if (mTopNewsFeedPagerAdapter.getNewsFeed() == null) {
            return;
        }
        mImageTaskMap = new HashMap<>();

        ArrayList<News> newsList = mTopNewsFeedPagerAdapter.getNewsFeed().getNewsList();

        for (int i = 0; i < newsList.size(); i++) {
            News news = newsList.get(i);

            if (!news.hasImageUrl() && !news.isImageUrlChecked()) {
                TopFeedNewsImageUrlFetchTask task =
                        new TopFeedNewsImageUrlFetchTask(news, i, taskType, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                mImageTaskMap.put(news, task);
            }
        }
    }

    public void refreshNewsFeedOnSwipeDown() {
        refreshTopNewsFeed(TopNewsFeedFetchTask.TaskType.SWIPE_REFRESH, true);
    }

    private void refreshTopNewsFeed(TopNewsFeedFetchTask.TaskType taskType, boolean shuffle) {
        mIsReady = false;

        NewsFeed newsFeed = mTopNewsFeedPagerAdapter.getNewsFeed();
        newsFeed.clearFetchedInfo();
        // 뉴스가 로드되기 전 프로그레스 바를 보여주기 위해 임의로 null 을 하나 추가함
        newsFeed.addNews(null);

        mTopNewsFeedPagerAdapter = new MainTopPagerAdapter(mActivity.getFragmentManager());
        mTopNewsFeedPagerAdapter.setNewsFeed(newsFeed);
        mViewPager.setAdapter(mTopNewsFeedPagerAdapter);

        // 리프레시를 할 경우에는 로딩이 되기 전까지 안보이게 하기
        mViewPagerIndicator.setVisibility(View.INVISIBLE);
        mNewsFeedTitleTextView.setText("");

        fetchTopNewsFeed(taskType, shuffle);
    }

    public void configOnNewsFeedReplaced() {
        mIsReady = false;
        NewsFeed topNewsFeed = NewsDb.getInstance(getContext()).loadTopNewsFeed(getContext(), false);
        mTopNewsFeedPagerAdapter.setNewsFeed(topNewsFeed);
        if (mTopNewsFeedPagerAdapter.getNewsFeed().containsNews()) {
            notifyNewTopNewsFeedSet();
            fetchFirstNewsImage(TopFeedNewsImageUrlFetchTask.TaskType.REPLACE);
        } else {
            refreshTopNewsFeed(TopNewsFeedFetchTask.TaskType.REPLACE, false);
        }
    }

    public void configOnNewsImageUrlLoadedAt(String imageUrl, int idx) {
        News news = mTopNewsFeedPagerAdapter.getNewsFeed().getNewsList().get(idx);
        news.setImageUrl(imageUrl);
        mTopNewsFeedPagerAdapter.notifyImageUrlLoaded(idx);

        mIsReady = true;
    }

    private void applyImage(News news, final int position,
                            final TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        mImageLoader.get(new NewsUrlSupplier(news, NewsFeed.INDEX_TOP),
                new CacheImageLoader.ImageListener() {
                    @Override
                    public void onSuccess(CacheImageLoader.ImageResponse response) {
                        mTopNewsFeedPagerAdapter.notifyImageUrlLoaded(position);

                        if (position == 0) {
                            notifyOnReady(taskType);

                            // fetch other images
                            fetchTopNewsFeedImages(taskType);
                        }
                    }

                    @Override
                    public void onFail(VolleyError error) {
                        mTopNewsFeedPagerAdapter.notifyImageUrlLoaded(position);

                        if (position == 0) {
                            notifyOnReady(taskType);

                            // fetch other images
                            fetchTopNewsFeedImages(taskType);
                        }
                    }
                });
    }

    public void configOnOrientationChange() {
        if (Device.isPortrait(getContext())) {
            configOnPortraitOrientation();
        } else if (Device.isLandscape(getContext())) {
            configOnLandscapeOrientation();
        }

        // 애니메이션 도중 회전시 프래그먼트 밑 이미지뷰가 어긋나는 현상(ViewPager 내부 버그) 막기 위해 다시 어댑터를 새로 대입
        // 하지만 Sync 로 교체를 하기에 회전시 화면전환 속도가 1/2 정도로 꽤 떨어지는 점을 알아두자
        int currentItem = mViewPager.getCurrentItem();
        if (mTopNewsFeedPagerAdapter != null) {
            mViewPager.setAdapter(mTopNewsFeedPagerAdapter);
            mViewPager.setCurrentItem(currentItem);
        }
        initViewPagerIndicator();
        invalidate();
    }

    private void configOnPortraitOrientation() {
        adjustLayoutParamsOnPortrait();
        adjustContentWrapperLayoutParamsOnPortrait();
    }

    private void configOnLandscapeOrientation() {
        adjustLayoutParamsOnLandscape();
        adjustContentWrapperLayoutParamsOnLandscape();
    }

    private void adjustLayoutParamsOnPortrait() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = getResources().getDimensionPixelSize(R.dimen.main_top_view_pager_height);

        setPadding(0, 0, 0, 0);
    }

    private void adjustContentWrapperLayoutParamsOnPortrait() {
        ViewGroup.LayoutParams contentWrapperLp = mContentWrapper.getLayoutParams();
        contentWrapperLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    private void adjustLayoutParamsOnLandscape() {
        ViewGroup.LayoutParams lp = getLayoutParams();

        // 유진님 기준에 따라 세로모드의 bottomHeight 만큼을 width 로 잡아줄 것
        float portraitHeight = getResources().getDimension(R.dimen.main_top_view_pager_height);
        lp.width = (int)(Display.getDisplaySize(getContext()).x - portraitHeight);
        lp.height = Display.getDisplaySize(getContext()).y;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            lp.height -= Display.getStatusBarHeight(getContext().getApplicationContext());
        }

        Context context = getContext().getApplicationContext();

        // 바텀과 상하단 맞추기 위함
        int margin = context.getResources().getDimensionPixelSize(
                R.dimen.main_bottom_margin_small);

        boolean adPurchased = IabProducts.containsSku(context, IabProducts.SKU_NO_ADS);
        if (!adPurchased) {
            // indicator 간격 만큼 하단 띄우기 위함
            int adHeight = AdSize.SMART_BANNER.getHeightInPixels(context);
            setPadding(0, margin * 2, 0, adHeight + margin * 2);
        } else {
            setPadding(0, margin * 2, 0, margin * 2);
        }
    }

    private void adjustContentWrapperLayoutParamsOnLandscape() {
        ViewGroup.LayoutParams indicatorLp = mViewPagerIndicator.getLayoutParams();
        int indicatorHeight = indicatorLp.height;

        ViewGroup.LayoutParams contentWrapperLp = mContentWrapper.getLayoutParams();
        contentWrapperLp.height = getLayoutParams().height - indicatorHeight;

        if (indicatorLp instanceof MarginLayoutParams) {
            MarginLayoutParams indicatorMarginLp = (MarginLayoutParams)indicatorLp;
            contentWrapperLp.height -= indicatorMarginLp.topMargin;
        }

        Context context = getContext().getApplicationContext();
        if (!IabProducts.containsSku(context, IabProducts.SKU_NO_ADS)) {
            int adViewHeight = AdSize.SMART_BANNER.getHeightInPixels(context);
            contentWrapperLp.height -= adViewHeight;
        }
    }

    /**
     * TopNewsFeedFetch Listener
     */
    @Override
    public void onTopNewsFeedFetch(NewsFeed newsFeed, TopNewsFeedFetchTask.TaskType taskType) {
        Context context = getContext().getApplicationContext();

        mTopNewsFeedPagerAdapter.setNewsFeed(newsFeed);
        NewsDb.getInstance(context).saveTopNewsFeed(newsFeed);
        NewsFeedArchiveUtils.saveRecentCacheMillisec(context);

        TopFeedNewsImageUrlFetchTask.TaskType imageFetchTaskType;
        if (taskType.equals(TopNewsFeedFetchTask.TaskType.INITIALIZE)) {
            mActivity.onTopNewsFeedFetch();
            imageFetchTaskType = TopFeedNewsImageUrlFetchTask.TaskType.INITIALIZE;
        } else {
            imageFetchTaskType =
                    taskType.equals(TopNewsFeedFetchTask.TaskType.SWIPE_REFRESH)
                        ? TopFeedNewsImageUrlFetchTask.TaskType.REFRESH
                        : TopFeedNewsImageUrlFetchTask.TaskType.REPLACE;
        }

        if (newsFeed.containsNews()) {
            notifyNewTopNewsFeedSet();
            fetchFirstNewsImage(imageFetchTaskType);
        } else {
            showUnavailableStatus(NewsFeedFetchStateMessage.getMessage(context, newsFeed));
            notifyOnReady(taskType);
        }
    }

    @Override
    public void onTopFeedImageUrlFetch(News news, String url, final int position,
                                       TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        if (url != null) {
            applyImage(news, position, taskType);
            NewsDb.getInstance(getContext()).saveTopNewsImageUrlWithGuid(url, news.getGuid());
        } else {
            mTopNewsFeedPagerAdapter.notifyImageUrlLoaded(position);

            if (position == 0) {
                notifyOnReady(taskType);
                fetchTopNewsFeedImages(taskType);
            }
        }
    }

    @Override
    public void onTopItemClick(MainTopFragment.ItemViewHolder viewHolder, NewsFeed newsFeed, int position) {
        if (!newsFeed.containsNews()) {
            return;
        }

        Intent intent = new Intent(mActivity, NewsFeedDetailActivity.class);
        intent = putNewsFeedInfoToIntent(intent, newsFeed, position);
        intent = makeNewsSelectIntent(intent);
//        intent = putImageTintTypeToIntent(intent, viewHolder);
        intent = putActivityTransitionInfo(intent, viewHolder);

        mOnEventListener.onStartNewsFeedDetailActivityFromTopNewsFeed(intent);
    }

    private Intent makeNewsSelectIntent(Intent intent) {
        intent.putExtra(MainActivity.INTENT_KEY_NEWS_FEED_LOCATION, MainActivity.INTENT_VALUE_TOP_NEWS_FEED);
        intent.putExtra(NewsFeed.KEY_NEWS_FEED, mTopNewsFeedPagerAdapter.getNewsFeed());

        return intent;
    }

//    private Intent putImageTintTypeToIntent(Intent intent, MainNewsFeedFragment.ItemViewHolder viewHolder) {
//        Object tintTag = viewHolder.imageView.getTag();
//        TintType tintType = tintTag != null ? (TintType)tintTag : null;
//        intent.putExtra(MainActivity.INTENT_KEY_TINT_TYPE, tintType);
//
//        return intent;
//    }

    private Intent putActivityTransitionInfo(Intent intent, MainTopFragment.ItemViewHolder viewHolder) {
        // ActivityOptions 를 사용하지 않고 액티비티 트랜지션을 오버라이드해서 직접 애니메이트 하기 위한 변수
        int titleViewPadding =
                getResources().getDimensionPixelSize(R.dimen.main_top_view_pager_title_padding);
        int feedTitlePadding =
                getResources().getDimensionPixelSize(R.dimen.main_top_news_feed_title_padding);

        ActivityTransitionHelper transitionProperty = new ActivityTransitionHelper()
                .addImageView(ActivityTransitionHelper.KEY_IMAGE, viewHolder.imageView)
                .addTextView(ActivityTransitionHelper.KEY_TEXT, viewHolder.titleTextView,
                        titleViewPadding)
                .addTextView(ActivityTransitionHelper.KEY_SUB_TEXT, mNewsFeedTitleTextView,
                        feedTitlePadding);

        intent.putExtra(INTENT_KEY_TRANSITION_PROPERTY, transitionProperty.toGsonString());

        return intent;
    }

    private Intent putNewsFeedInfoToIntent(Intent intent, NewsFeed newsFeed, int position) {
        intent.putExtra(NewsFeed.KEY_NEWS_FEED, newsFeed);
        intent.putExtra(News.KEY_CURRENT_NEWS_INDEX, position);

        return intent;
    }
}
