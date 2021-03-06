package com.yooiistudios.newskit.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.newskit.NewsApplication;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsTopic;
import com.yooiistudios.newskit.core.news.RssFetchable;
import com.yooiistudios.newskit.core.news.curation.NewsContentProvider;
import com.yooiistudios.newskit.core.news.curation.NewsProvider;
import com.yooiistudios.newskit.core.news.database.NewsDb;
import com.yooiistudios.newskit.core.news.util.NewsFeedFetchUtil;
import com.yooiistudios.newskit.core.util.Device;
import com.yooiistudios.newskit.core.util.Display;
import com.yooiistudios.newskit.iab.IabProducts;
import com.yooiistudios.newskit.model.AlphaForegroundColorSpan;
import com.yooiistudios.newskit.model.NewsFeedDetailSettings;
import com.yooiistudios.newskit.model.cache.NewsImageLoader;
import com.yooiistudios.newskit.model.cache.NewsUrlSupplier;
import com.yooiistudios.newskit.model.news.task.NewsFeedDetailNewsFeedFetchTask;
import com.yooiistudios.newskit.model.news.task.NewsFeedDetailNewsImageUrlFetchTask;
import com.yooiistudios.newskit.ui.PanelDecoration;
import com.yooiistudios.newskit.ui.adapter.NewsFeedDetailAdapter;
import com.yooiistudios.newskit.ui.animation.NewsFeedDetailTransitionUtils;
import com.yooiistudios.newskit.ui.fragment.dialog.NewsFeedDetailSettingDialogFragment;
import com.yooiistudios.newskit.ui.itemanimator.DetailNewsItemAnimator;
import com.yooiistudios.newskit.ui.widget.NewsTopicSelectDialogFactory;
import com.yooiistudios.newskit.ui.widget.ObservableScrollView;
import com.yooiistudios.newskit.util.AnalyticsUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors (prefix = "m")
public class NewsFeedDetailActivity extends AppCompatActivity
        implements NewsFeedDetailAdapter.OnItemClickListener, ObservableScrollView.Callbacks,
        NewsFeedDetailNewsFeedFetchTask.OnFetchListener,
        NewsFeedDetailNewsImageUrlFetchTask.OnImageUrlFetchListener,
        NewsTopicSelectDialogFactory.OnItemClickListener,
        NewsFeedDetailTransitionUtils.OnAnimationEndListener,
        NewsFeedDetailSettingDialogFragment.OnActionListener {
    private static final String TAG = NewsFeedDetailActivity.class.getName();
    private static final int INVALID_WINDOW_INSET = -1;

    // Overlay & Shadow
    private static final float TOP_OVERLAY_ALPHA_LIMIT = 0.75f;
    private static final float TOP_OVERLAY_ALPHA_RATIO = 0.0008f;
    private static final float TOP_SCROLL_PARALLAX_RATIO = 0.4f;

//    private static final int BOTTOM_NEWS_ANIM_DELAY_UNIT_MILLI = 60;
//    private static final int ACTIVITY_ENTER_ANIMATION_DURATION = 600;

    // Auto Scroll
    public static final int START_DELAY = 3000;
    public static final int MIDDLE_DELAY = 1500;
    public static final int DURATION_FOR_EACH_ITEM = 3000;

    public static final String INTENT_KEY_NEWS = "INTENT_KEY_NEWS";
    public static final String INTENT_KEY_NEWSFEED_REPLACED = "INTENT_KEY_NEWSFEED_REPLACED";
    public static final String INTENT_KEY_IMAGE_LOADED = "INTENT_KEY_IMAGE_LOADED";
    public static final String INTENT_KEY_IMAGE_URL = "INTENT_KEY_IMAGE_URL";

    public static final int REQ_SELECT_NEWS_FEED = 13841;

    @Getter @InjectView(R.id.newsfeed_detail_toolbar)                        Toolbar mToolbar;
    @Getter @InjectView(R.id.newsfeed_detail_content_layout)                 RelativeLayout mRootLayout;
    @Getter @InjectView(R.id.newsfeed_detail_transition_content_layout)      FrameLayout mTransitionLayout;
    @Getter @InjectView(R.id.newsfeed_detail_toolbar_overlay_view)           View mToolbarOverlayView;
    @Getter @InjectView(R.id.newsfeed_detail_top_gradient_shadow_view)       View mTopGradientShadowView;
    @InjectView(R.id.newsfeed_detail_scroll_view)                            ObservableScrollView mScrollView;
    @InjectView(R.id.newsfeed_detail_scroll_content_wrapper)                 RelativeLayout mScrollContentWrapper;
    @InjectView(R.id.newsfeed_detail_swipe_refresh_layout)                   SwipeRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.newsfeed_detail_loading_cover)                          View mLoadingCoverView;
    @Getter @InjectView(R.id.newsfeed_detail_reveal_view)                    View mRevealView;

    // Top
    @Getter @InjectView(R.id.newsfeed_detail_top_news_image_wrapper)         FrameLayout mTopNewsImageWrapper;
    @InjectView(R.id.newsfeed_detail_top_news_image_ripple_view)             View mTopNewsImageRippleView;
    @Getter @InjectView(R.id.newsfeed_detail_top_news_image_view)            ImageView mTopImageView;
    @Getter @InjectView(R.id.newsfeed_detail_top_news_text_layout)           LinearLayout mTopNewsTextLayout;
    @InjectView(R.id.newsfeed_detail_top_news_text_ripple_layout)            LinearLayout mTopNewsTextRippleLayout;
    @Getter @InjectView(R.id.newsfeed_detail_top_news_title_text_view)       TextView mTopTitleTextView;
    @Getter @InjectView(R.id.newsfeed_detail_top_news_description_text_view) TextView mTopDescriptionTextView;

    // Bottom
    @Getter @InjectView(R.id.newsfeed_detail_bottom_news_recycler_view)      RecyclerView mBottomRecyclerView;

    ObjectAnimator mAutoScrollDownAnimator;
    ObjectAnimator mAutoScrollUpAnimator;

    private NewsImageLoader mImageLoader;

    private NewsFeed mNewsFeed;
    private News mTopNews;
    private Bitmap mTopImageBitmap;
    private NewsFeedDetailAdapter mAdapter;
    private int mFilterColor;
    private ColorDrawable mRecyclerViewBackground;
    private Drawable mToolbarHomeIcon;
    private BitmapDrawable mToolbarOverflowIcon;
    @Getter private SpannableString mToolbarTitle;
    @Getter private AlphaForegroundColorSpan mToolbarTitleColorSpan;
    private NewsFeedDetailTransitionUtils mTransitionUtils;

    private NewsFeedDetailNewsFeedFetchTask mNewsFeedFetchTask;
    private NewsFeedDetailNewsImageUrlFetchTask mTopNewsImageFetchTask;

    private int mSystemWindowInsetBottom = INVALID_WINDOW_INSET;

    private boolean mIsRefreshing = false;

    @InjectView(R.id.newsfeed_detail_ad_upper_view) View mAdUpperView;
    @InjectView(R.id.newsfeed_detail_adView) AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news_feed_detail);
        ButterKnife.inject(this);

        // retrieve feed from intent
        mImageLoader = NewsImageLoader.create(this);

        initViewsSize();
        requestSystemWindowsBottomInset();
        initRevealView();
        initToolbar();
        initSwipeRefreshView();
        initCustomScrollView();
        initNewsFeed();
        initTopNewsContent();
        initBottomNewsList();
        initLoadingCoverView();
        initAdView();

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (savedInstanceState == null) {
            if (mTopImageView.getDrawable() != null) {
                mTransitionUtils = NewsFeedDetailTransitionUtils.runEnterAnimation(this);
            } else {
                configBeforeRefresh();
//                showLoadingCover();
            }
        } else {
            adjustShadowGradientViews();
        }
        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
        AnalyticsUtils.trackActivityOrientation((NewsApplication) getApplication(), TAG,
                getResources().getConfiguration().orientation);
    }

    private void initNewsFeed() {
        NewsFeed newsFeed = getIntent().getExtras().getParcelable(NewsFeed.KEY_NEWS_FEED);
        Parcel parcel = Parcel.obtain();
        newsFeed.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        mNewsFeed = NewsFeed.CREATOR.createFromParcel(parcel);
        parcel.recycle();
    }

    private void initRevealView() {
        mRevealView.setBackgroundColor(Color.WHITE);
    }

    private void initAdView() {
        // NO_ADS 만 체크해도 풀버전까지 체크됨
        if (IabProducts.containsSku(getApplicationContext(), IabProducts.SKU_NO_ADS)) {
            mAdView.setVisibility(View.GONE);
            mAdUpperView.setVisibility(View.GONE);
        } else {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setBackgroundColor(
                            getResources().getColor(R.color.material_grey_900));
                    mAdUpperView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void configOnSystemInsetChanged() {
        // NO_ADS 만 체크해도 풀버전까지 체크됨
        boolean adPurchased = IabProducts.containsSku(getApplicationContext(), IabProducts.SKU_NO_ADS);
        if (adPurchased) {
            mScrollContentWrapper.setPadding(0, 0, 0, mSystemWindowInsetBottom);
            mAdUpperView.setVisibility(View.GONE);
            mAdView.setVisibility(View.GONE);
        } else {
            int adViewHeight = AdSize.SMART_BANNER.getHeightInPixels(getApplicationContext());
            if (Device.isPortrait(this)) {
                mScrollContentWrapper.setPadding(0, 0, 0, mSystemWindowInsetBottom + adViewHeight);
            } else {
                mScrollContentWrapper.setPadding(0, 0, 0, adViewHeight);
            }
            mAdView.setVisibility(View.VISIBLE);
            mAdView.resume();

            RelativeLayout.LayoutParams adViewLp =
                    (RelativeLayout.LayoutParams)mAdView.getLayoutParams();
            adViewLp.bottomMargin = mSystemWindowInsetBottom;
        }
    }

    private void initLoadingCoverView() {
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);

        mLoadingCoverView.getLayoutParams().height = displaySize.y;
        mLoadingCoverView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    private void initToolbar() {
        mToolbar.bringToFront();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initToolbarGradientView();
        initToolbarIcon();
        initToolbarTitle();
        initToolbarTopMargin();
        initToolbarHeight();
    }

    private void initToolbarTopMargin() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarHeight = Display.getStatusBarHeight(this);
            if (statusBarHeight > 0) {
                ((RelativeLayout.LayoutParams) mToolbar.getLayoutParams()).topMargin = statusBarHeight;
            }
        }
    }

    private void initToolbarTitle() {
        mToolbarTitleColorSpan = new AlphaForegroundColorSpan(
                getResources().getColor(R.color.material_white_primary_text));

        initToolbarTextAppearance();
        applyToolbarTitle();
    }

    private void applyToolbarTitle() {
        if (getSupportActionBar() != null && mNewsFeed != null) {
            mToolbarTitle = new SpannableString(mNewsFeed.getTitle());
            mToolbarTitle.setSpan(mToolbarTitleColorSpan, 0, mToolbarTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getSupportActionBar().setTitle(mToolbarTitle);
        }
    }

    private void initToolbarIcon() {
        if (getSupportActionBar() != null) {
            mToolbarHomeIcon = mToolbar.getNavigationIcon();

            Bitmap overflowIconBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_menu_moreoverflow_mtrl_alpha);
            mToolbarOverflowIcon = new BitmapDrawable(getResources(), overflowIconBitmap);
        }
    }

    private void initToolbarGradientView() {
        // 기존의 계산된 ActionBar 높이와 Toolbar 의 실제가 높이가 달라 측정해서 적용하게 변경
        final int statusBarSize = Display.getStatusBarHeight(this);
        mToolbar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mToolbar.getViewTreeObserver().removeOnPreDrawListener(this);
                Context context = NewsFeedDetailActivity.this;
                int ratio;
                int gradientHeight;
                if (Device.hasLollipop()) {
                    if (Display.isTablet(context) && Device.isPortrait(context)) {
                        ratio = 3;
                    } else {
                        ratio = 2;
                    }
                    gradientHeight = (mToolbar.getHeight() + statusBarSize) * ratio;
                    mToolbarOverlayView.getLayoutParams().height = mToolbar.getHeight() + statusBarSize;
                } else {
                    if (Display.isTablet(context) && Device.isPortrait(context)) {
                        ratio = 4;
                    } else {
                        ratio = 3;
                    }
                    gradientHeight = mToolbar.getHeight() * ratio;
                    mToolbarOverlayView.getLayoutParams().height = mToolbar.getHeight();
                }
                mTopGradientShadowView.getLayoutParams().height = gradientHeight;

                return false;
            }
        });
    }

    private void initSwipeRefreshView() {
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.app_color_accent);
    }

    private void initCustomScrollView() {
        mScrollView.addCallbacks(this);
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mIsRefreshing;
            }
        });
    }

    private void initTopNewsContent() {
        mTopNewsImageRippleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewsFeedDetailActivity.this, NewsDetailActivity.class);
                intent.putExtra(INTENT_KEY_NEWS, mTopNews);

                startActivity(intent);
            }
        });

        mTopNewsTextRippleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewsFeedDetailActivity.this, NewsDetailActivity.class);
                intent.putExtra(INTENT_KEY_NEWS, mTopNews);

                startActivity(intent);
            }
        });

        initTopNews();
        notifyTopNewsChanged();
    }

    private void initTopNews() {
        int topNewsIndex = getIntent().getExtras().getInt(News.KEY_CURRENT_NEWS_INDEX);
        if (topNewsIndex < mNewsFeed.getNewsList().size()) {
            mTopNews = mNewsFeed.getNewsList().remove(topNewsIndex);
        }
    }

    private void initBottomNewsList() {
        mBottomRecyclerView.setHasFixedSize(true);
        mBottomRecyclerView.setItemAnimator(
                new DetailNewsItemAnimator(mBottomRecyclerView));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mBottomRecyclerView.setLayoutManager(layoutManager);

        mRecyclerViewBackground = new ColorDrawable(Color.WHITE);
        mBottomRecyclerView.setBackground(mRecyclerViewBackground);

        notifyBottomNewsChanged();
    }

    private void notifyBottomNewsChanged() {
        mAdapter = new NewsFeedDetailAdapter(this);
        mBottomRecyclerView.setAdapter(mAdapter);
        mAdapter.setNewsFeed(mNewsFeed.getNewsList());

        adjustBottomRecyclerHeight();
    }

    private void adjustBottomRecyclerHeight() {
        applyMaxBottomRecyclerViewHeight();
        applyActualRecyclerViewHeightOnPreDraw();
    }

    private void applyActualRecyclerViewHeightOnPreDraw() {
        ViewTreeObserver observer = mBottomRecyclerView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mBottomRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                applyActualRecyclerViewHeight();
                return true;
            }
        });
    }

    private void applyActualRecyclerViewHeight() {
        int totalHeight = 0;
        int childCount = mBottomRecyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            totalHeight += mBottomRecyclerView.getChildAt(i).getHeight();
        }

        ViewGroup.LayoutParams lp = mBottomRecyclerView.getLayoutParams();
        lp.height = totalHeight;
        mBottomRecyclerView.setLayoutParams(lp);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRevealView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mImageLoader.flushCache();

        if (mNewsFeedFetchTask != null) {
            mNewsFeedFetchTask.cancel(true);
        }
        if (mTopNewsImageFetchTask != null) {
            mTopNewsImageFetchTask.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: Options 번역 필요
        SubMenu subMenu = menu.addSubMenu(Menu.NONE, R.id.action_newsfeed_overflow, 0, "Options");
        subMenu.setIcon(mToolbarOverflowIcon);

        if (!mNewsFeed.isCustomRss()) {
            subMenu.add(Menu.NONE, R.id.action_select_topic, 0,
                    getString(R.string.newsfeed_select_news_section));
        }

        /*
        String autoScrollString = getString(R.string.newsfeed_auto_scroll) + " ";
        if (Settings.isNewsFeedAutoScroll(this)) {
            autoScrollString += getString(R.string.off);
        } else {
            autoScrollString += getString(R.string.on);
        }

        subMenu.add(Menu.NONE, R.id.action_auto_scroll, 1, autoScrollString);
        */

        subMenu.add(Menu.NONE, R.id.action_newsfeed_detail_setting, 1,
                getString(R.string.action_settings));

        MenuItemCompat.setShowAsAction(subMenu.getItem(), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                AnalyticsUtils.trackNewsFeedDetailQuitAction((NewsApplication) getApplication(), TAG,
                        "Home Button");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }
                return true;

            case R.id.action_select_topic:
                NewsProvider newsProvider =
                        NewsContentProvider.getInstance(this).getNewsProvider(mNewsFeed);
                if (newsProvider != null) {
                    NewsTopicSelectDialogFactory.makeDialog(this, newsProvider, this).show();

                    // 무조건 자동스크롤은 멈춰주기 - 교체하고 돌아올 경우 꼬인다
                    stopAutoScroll();
                }
                return true;

            /*
            case R.id.action_auto_scroll:
                boolean isAutoScroll = NewsFeedDetailSettings.isNewsFeedAutoScroll(this);
                isAutoScroll = !isAutoScroll;
                NewsFeedDetailSettings.setNewsFeedAutoScroll(this, isAutoScroll);

                String autoScrollString = getString(R.string.newsfeed_auto_scroll) + " ";
                if (isAutoScroll) {
                    autoScrollString += getString(R.string.off);
                } else {
                    autoScrollString += getString(R.string.on);
                }
                item.setTitle(autoScrollString);

                if (isAutoScroll) {
                    startAutoScroll();
                } else {
                    stopAutoScroll();
                }
                return true;
            */

            case R.id.action_newsfeed_detail_setting:
                showSettingFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingFragment() {
        stopAutoScroll();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = NewsFeedDetailSettingDialogFragment.newInstance();
        newFragment.show(ft, "dialog");
    }

    @Override
    public void onDismissSettingDialog() {
        if (NewsFeedDetailSettings.isNewsFeedAutoScroll(this)) {
            startAutoScroll();
        }
    }

    private void applyMaxBottomRecyclerViewHeight() {
        int maxRowHeight = NewsFeedDetailAdapter.measureMaximumRowHeight(getApplicationContext());

        int newsListCount = mNewsFeed.getNewsList().size();
        ViewGroup.LayoutParams lp = mBottomRecyclerView.getLayoutParams();
        lp.height = maxRowHeight * newsListCount;
        mBottomRecyclerView.setLayoutParams(lp);
    }

    private void notifyTopNewsChanged() {
        // set action bar title
        applyToolbarTitle();
        applyTopNewsText();
        invalidateImage();
    }

    private void applyTopNewsText() {
        // set title
        mTopTitleTextView.setText(mTopNews.getTitle());

        // set description
        String description = mTopNews.getDescription();
        if (description != null && description.trim().length() > 0) {
            mTopDescriptionTextView.setVisibility(View.VISIBLE);
            mTopDescriptionTextView.setText(description.trim());

            // 타이틀 아래 패딩 조절
            mTopTitleTextView.setPadding(mTopTitleTextView.getPaddingLeft(),
                    mTopTitleTextView.getPaddingTop(), mTopTitleTextView.getPaddingRight(), 0);
        } else {
            mTopDescriptionTextView.setVisibility(View.GONE);
        }

        mTopNewsTextLayout.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void invalidateImage() {
        String imgUrl = mTopNews.getImageUrl();
        getIntent().putExtra(INTENT_KEY_IMAGE_LOADED, true);
        getIntent().putExtra(INTENT_KEY_IMAGE_URL, imgUrl);
        setResult(RESULT_OK, getIntent());

        if (mTopNews.hasImageUrl()) {
            int newsFeedIndex;
            if (isFromTopNewsFeed()) {
                newsFeedIndex = NewsFeed.INDEX_TOP;
            } else {
                newsFeedIndex = getIntent().getExtras().getInt(
                        MainActivity.INTENT_KEY_BOTTOM_NEWS_FEED_INDEX);
            }
            mImageLoader.get(new NewsUrlSupplier(mTopNews, newsFeedIndex),
                    new CacheImageLoader.ImageListener() {
                        @Override
                        public void onSuccess(CacheImageLoader.ImageResponse response) {
                            setImage(response);
                            configAfterRefreshDone();
                        }

                        @Override
                        public void onFail(VolleyError error) {
                            applyDummyImage();
                            configAfterRefreshDone();
                        }
                    });
        } else if (mTopNews.isImageUrlChecked()) {
            applyDummyImage();
            configAfterRefreshDone();
        } else {
            configBeforeRefresh();
            mTopNewsImageFetchTask = new NewsFeedDetailNewsImageUrlFetchTask(mTopNews, this);
            mTopNewsImageFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void fetchNewsFeed(RssFetchable fetchable) {
        int fetchLimit = isFromTopNewsFeed()
                ? NewsFeedFetchUtil.FETCH_LIMIT_TOP
                : NewsFeedFetchUtil.FETCH_LIMIT_BOTTOM;
        mNewsFeedFetchTask = new NewsFeedDetailNewsFeedFetchTask(fetchable, this, fetchLimit, false);
        mNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        configBeforeRefresh();
    }

    private void configBeforeRefresh() {
        mIsRefreshing = true;
        NewsFeedDetailTransitionUtils.animateTopOverlayFadeOut(this);
        showLoadingCover();
    }

    private void configAfterRefreshDone() {
        mIsRefreshing = false;
        NewsFeedDetailTransitionUtils.animateTopOverlayFadeIn(this);
        hideLoadingCover();
    }

    private void showLoadingCover() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mLoadingCoverView.setVisibility(View.VISIBLE);
        mLoadingCoverView.animate().alpha(1.0f);
    }

    private void hideLoadingCover() {
        mSwipeRefreshLayout.setRefreshing(false);

        mLoadingCoverView.animate().alpha(0.0f).withEndAction(new Runnable() {
            @Override
            public void run() {
                mLoadingCoverView.setVisibility(View.GONE);
            }
        });
    }

    private void setImage(CacheImageLoader.ImageResponse response) {
        setImageBitmap(response.bitmap);
        applyFilterColor(response.paletteColor);
    }

    private void applyDummyImage() {
        Bitmap dummyImage = PanelDecoration.getDummyImage(getApplicationContext(), mImageLoader);
        setImageBitmap(dummyImage);
        applyDummyFilterColor();
    }

    private void applyFilterColor(CacheImageLoader.PaletteColor paletteColor) {
        int filterColor;
        if (isFromTopNewsFeed()) {
            filterColor = PanelDecoration.getDefaultTopPaletteColor();
        } else {
            if (paletteColor.isCustom()) {
                filterColor = PanelDecoration.getRandomPaletteColorWithAlpha(
                        getApplicationContext(), paletteColor.getPaletteColor());
            } else {
                filterColor = PanelDecoration.getPaletteColorWithAlpha(
                        getApplicationContext(), paletteColor.getPaletteColor());
            }
        }
        setFilterColor(filterColor);
    }

    private void applyDummyFilterColor() {
        int filterColor;
        if (isFromTopNewsFeed()) {
            filterColor = PanelDecoration.getTopDummyImageFilterColor();
        } else {
            filterColor = PanelDecoration.getBottomDummyImageFilterColor(getApplicationContext());
        }
        setFilterColor(filterColor);
    }

    private void setImageBitmap(Bitmap bitmap) {
        mTopImageBitmap = bitmap;
        mTopImageView.setImageBitmap(mTopImageBitmap);
    }

    private void setFilterColor(int color) {
        mFilterColor = color;
        applyPalette();
    }

    public boolean isFromTopNewsFeed() {
        String newsLocation = getIntent().getExtras().getString(
                MainActivity.INTENT_KEY_NEWS_FEED_LOCATION, MainActivity.INTENT_VALUE_TOP_NEWS_FEED);

        return newsLocation.equals(MainActivity.INTENT_VALUE_TOP_NEWS_FEED);
    }

    private void applyPalette() {
        mTopTitleTextView.setTextColor(Color.WHITE);
        mTopDescriptionTextView.setTextColor(getResources().getColor(R.color.material_white_secondary_text));

        int filterColor = getFilterColor();

        int red = Color.red(filterColor);
        int green = Color.green(filterColor);
        int blue = Color.blue(filterColor);

        int colorWithoutAlpha = Color.rgb(red, green, blue);
//        mTopContentLayout.setBackground(new ColorDrawable(colorWithoutAlpha));
        mTopNewsTextLayout.setBackground(new ColorDrawable(colorWithoutAlpha));
        mRevealView.setBackgroundColor(colorWithoutAlpha);
    }

    public int getFilterColor() {
        return mFilterColor;
    }

    @Override
    public void onItemClick(NewsFeedDetailAdapter.ViewHolder viewHolder, News news) {
        Intent intent = new Intent(this, NewsDetailActivity.class);
        intent.putExtra(INTENT_KEY_NEWS, news);

        startActivity(intent);
    }

    private void requestSystemWindowsBottomInset() {
        if (Device.hasLollipop()) {
            configNavigationTranslucentState();
            if (isSystemWindowInsetInvalid()) {
                requestSystemWindowsBottomInsetAfterLollipop();
            } else {
                configOnSystemInsetChanged();
            }
        } else {
            setSystemWindowInset(0);
        }
    }

    private boolean isSystemWindowInsetInvalid() {
        return mSystemWindowInsetBottom == INVALID_WINDOW_INSET;
    }

    private void configNavigationTranslucentState() {
        boolean adPurchased = IabProducts.containsSku(getApplicationContext(), IabProducts.SKU_NO_ADS);
        if (adPurchased) {
            Display.applyTranslucentNavigationBarAfterLollipop(this);
        } else {
            Display.removeTranslucentNavigationBarAfterLollipop(this);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestSystemWindowsBottomInsetAfterLollipop() {
        mScrollContentWrapper.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                setSystemWindowInset(windowInsets.getSystemWindowInsetBottom());
                return windowInsets.consumeSystemWindowInsets();
            }
        });
    }

    private void setSystemWindowInset(int bottomInset) {
        mSystemWindowInsetBottom = bottomInset;
        configOnSystemInsetChanged();
    }

    /**
     * Custom Scrolling
     */
    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        // Reposition the header bar -- it's normally anchored to the top of the content,
        // but locks to the top of the screen on scroll
        int scrollY = mScrollView.getScrollY();

        // Move background photo (parallax effect)
        if (mScrollView.getScrollY() >= 0) {
            mTopNewsImageWrapper.setTranslationY(scrollY * TOP_SCROLL_PARALLAX_RATIO);

            mToolbarOverlayView.setAlpha(getToolbarOverlayAlpha());
            mTopGradientShadowView.setAlpha(getTopGradientShadowViewAlpha());
        } else {
            mTopNewsImageWrapper.setTranslationY(0);
            if (mToolbarOverlayView.getAlpha() != 0) {
                mToolbarOverlayView.setAlpha(getToolbarOverlayAlpha());
            }
            if (mTopGradientShadowView.getAlpha() != 1.f) {
                mTopGradientShadowView.setAlpha(getTopGradientShadowViewAlpha());
            }
        }
    }

    public float getToolbarOverlayAlpha() {
        int scrollY = mScrollView.getScrollY();

        float toolbarAlpha;
        if (scrollY >= 0) {
            if (scrollY * TOP_OVERLAY_ALPHA_RATIO < TOP_OVERLAY_ALPHA_LIMIT) {
                toolbarAlpha = scrollY * TOP_OVERLAY_ALPHA_RATIO;
            } else {
                toolbarAlpha = TOP_OVERLAY_ALPHA_LIMIT;
            }
        } else {
            toolbarAlpha = 0;
        }

        return toolbarAlpha;
    }

    public float getTopGradientShadowViewAlpha() {
        int scrollY = mScrollView.getScrollY();
        float alpha;
        if (scrollY >= 0) {
            alpha = 1.f - scrollY * TOP_OVERLAY_ALPHA_RATIO;
        } else {
            alpha = 1.0f;
        }
        return alpha >= 0 ? alpha : 0;
    }

    @Override
    public void onScrollStarted() {
        stopAutoScroll();
    }

    @Override
    public void onSwipeLeft() {
        finish();
    }

    @Override
    public void onNewsFeedFetchSuccess(NewsFeed newsFeed) {
        mNewsFeed = newsFeed;

        getIntent().putExtra(INTENT_KEY_NEWSFEED_REPLACED, true);
        setResult(RESULT_OK, getIntent());

        // cache
        archiveNewsFeed(newsFeed);

        if (mNewsFeed.getNewsList().size() > 0) {
            mTopNews = mNewsFeed.getNewsList().remove(0);
        }

        notifyTopNewsChanged();
        notifyBottomNewsChanged();

        mTopNewsImageFetchTask = new NewsFeedDetailNewsImageUrlFetchTask(mTopNews, this);
        mTopNewsImageFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onNewsFeedFetchFail() {
        configAfterRefreshDone();
    }

    @Override
    public void onImageUrlFetchSuccess(News news, String url) {
//        news.setImageUrl(url);
//        news.setImageUrlChecked(true);

        // 아카이빙을 위해 임시로 top news 를 news feed 에 추가.
        mNewsFeed.addNewsAt(0, news);
        archiveNewsFeed(mNewsFeed);
        mNewsFeed.removeNewsAt(0);

        invalidateImage();
    }

    @Override
    public void onImageUrlFetchFail(News news) {
        applyDummyImage();
        configAfterRefreshDone();
    }

    @Override
    public void onSelectNewsTopic(Dialog dialog, NewsProvider newsProvider, int position) {
        dialog.dismiss();

        mScrollView.smoothScrollTo(0, 0);

        NewsTopic selectedTopic = newsProvider.getNewsTopicList().get(position);
        replaceNewsFeed(selectedTopic);
    }

    private void archiveNewsFeed(NewsFeed newsFeed) {
        Context context = getApplicationContext();
        if (isFromTopNewsFeed()) {
            NewsDb.getInstance(context).saveTopNewsFeed(newsFeed);
        } else {
            int idx = getIntent().getExtras().getInt(
                    MainActivity.INTENT_KEY_BOTTOM_NEWS_FEED_INDEX);
            NewsDb.getInstance(context).saveBottomNewsFeedAt(newsFeed, idx);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch(requestCode) {
                case REQ_SELECT_NEWS_FEED:
                    RssFetchable rssFetchable = (RssFetchable)data.getExtras().getSerializable(
                            NewsSelectActivity.KEY_RSS_FETCHABLE);
                    replaceNewsFeed(rssFetchable);
                    break;
            }
        }
    }

    private void replaceNewsFeed(RssFetchable fetchable) {
        fetchNewsFeed(fetchable);
    }

    private void startAutoScroll() {
        stopAutoScroll();

        // 1초 기다렸다가 아래로 스크롤, 스크롤 된 뒤는 다시 위로 올라옴
        // 중간 터치가 있을 때에는 onScrollChanged 애니메이션을 중지
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int deviceHeight = displayMetrics.heightPixels;

        final int maxY = mScrollContentWrapper.getHeight() - deviceHeight - mSystemWindowInsetBottom;

        int startDelay = NewsFeedDetailSettings.getStartDelaySecond(this) * 1000;
        int durationForOneItem = NewsFeedDetailSettings.getDurationForEachItem(this);
        final float speedRatio = NewsFeedDetailSettings.getSpeedRatio(this);
        durationForOneItem *= speedRatio;
        final int defaultDuration = mBottomRecyclerView.getChildCount() * durationForOneItem;
        final int middleDelay = NewsFeedDetailSettings.getMidDelay(this);
        int downScrollDuration = defaultDuration;

        // 아래 스크롤은 시작 위치에 따라 시간이 달라질 수 있음
        if (mScrollView.getScrollY() != 0) {
            downScrollDuration = (int) (downScrollDuration * (((float)maxY - mScrollView.getScrollY()) / maxY));
        }

        try {
            mAutoScrollDownAnimator = ObjectAnimator.ofInt(mScrollView, "scrollY", mScrollView.getScrollY(), maxY);
            mAutoScrollDownAnimator.setStartDelay(startDelay);
            mAutoScrollDownAnimator.setDuration(downScrollDuration);
            mAutoScrollDownAnimator.setInterpolator(new LinearInterpolator(this, null));
            mAutoScrollDownAnimator.start();

            mAutoScrollDownAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mAutoScrollUpAnimator = ObjectAnimator.ofInt(mScrollView, "scrollY", maxY, 0);
                    mAutoScrollUpAnimator.setStartDelay(middleDelay);
                    mAutoScrollUpAnimator.setDuration(defaultDuration);
                    mAutoScrollUpAnimator.setInterpolator(
                            new LinearInterpolator(NewsFeedDetailActivity.this, null));
                    mAutoScrollUpAnimator.start();
                }
            });
        } catch (IllegalArgumentException e) {
            Crashlytics.getInstance().core.log("mBottomRecyclerView.getChildCount(): " + mBottomRecyclerView.getChildCount());
            Crashlytics.getInstance().core.log("durationForOneItem: " + durationForOneItem);
            Crashlytics.getInstance().core.log("defaultDuration: " + defaultDuration);
            Crashlytics.getInstance().core.log("maxY: " + maxY);
            Crashlytics.getInstance().core.log("mScrollView.getScrollY(): " + mScrollView.getScrollY());
            Crashlytics.getInstance().core.logException(e);
        }
    }

    private void stopAutoScroll() {
        if (mAutoScrollDownAnimator != null) {
            mAutoScrollDownAnimator.cancel();
        }
        if (mAutoScrollUpAnimator != null) {
            mAutoScrollUpAnimator.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        stopAutoScroll();
        mImageLoader.closeCache();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        // Activity visible to user
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        // Activity no longer visible
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnalyticsUtils.trackNewsFeedDetailQuitAction((NewsApplication) getApplication(), TAG,
                "Back Button");
    }

//    @Override
//    public void onRecyclerScaleAnimationEnd() {
//        adjustBottomRecyclerHeight();
//        startScrollIfAutoScrollOn();
//    }

    @Override
    public void onTransitionEnd() {
        adjustBottomRecyclerHeight();
        startScrollIfAutoScrollOn();
    }

    private void startScrollIfAutoScrollOn() {
        if (NewsFeedDetailSettings.isNewsFeedAutoScroll(this)) {
            // 부모인 래퍼가 자식보다 프리드로우 리스너가 먼저 불리기에
            // 자식이 그려질 때 명시적으로 뷰트리옵저버에서 따로 살펴봐야 제대로 된 높이를 계산가능
            mScrollContentWrapper.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mScrollContentWrapper.getViewTreeObserver().removeOnPreDrawListener(this);
                    startAutoScroll();
                    return true;
                }
            });
        }
    }

    private void initToolbarTextAppearance() {
        if (Device.isPortrait(this)) {
            mToolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Title);
        } else {
            mToolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Subhead);
        }
    }

    private void initViewsSize() {
        Point deviceSize = Display.getDisplaySize(this);

        // width
        mToolbar.getLayoutParams().width = deviceSize.x;
        mTopNewsImageWrapper.getLayoutParams().width = deviceSize.x;
        mTopNewsTextLayout.getLayoutParams().width = deviceSize.x;
        mBottomRecyclerView.getLayoutParams().width = deviceSize.x;

        // height
        int imageWrapperHeight;
        if (Device.isPortrait(this)) {
            imageWrapperHeight = getResources().getDimensionPixelSize(
                    R.dimen.detail_top_image_view_height_port);
        } else {
            imageWrapperHeight = getResources().getDimensionPixelSize(
                    R.dimen.detail_top_image_view_height_land);
        }
        if (Device.hasLollipop()) {
            imageWrapperHeight += Display.getStatusBarHeight(this);
        }
        mTopNewsImageWrapper.getLayoutParams().height = imageWrapperHeight;
    }

    private void initToolbarHeight() {
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        mToolbar.getLayoutParams().height = toolbarHeight;
    }

    private void adjustShadowGradientViews() {
        mScrollView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mScrollView.getViewTreeObserver().removeOnPreDrawListener(this);

                onScrollChanged(0, 0); // 회전시 툴바와 그라디언트 알파값 조절을 위함
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        boolean isAnimating = mTransitionUtils != null && mTransitionUtils.isAnimating();
        return isAnimating || super.dispatchTouchEvent(event);
    }
}
