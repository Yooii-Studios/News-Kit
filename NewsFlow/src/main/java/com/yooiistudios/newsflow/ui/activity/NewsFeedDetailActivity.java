package com.yooiistudios.newsflow.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
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
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.newsflow.NewsApplication;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.iab.IabProducts;
import com.yooiistudios.newsflow.model.AlphaForegroundColorSpan;
import com.yooiistudios.newsflow.model.RssFetchable;
import com.yooiistudios.newsflow.model.Settings;
import com.yooiistudios.newsflow.model.database.NewsDb;
import com.yooiistudios.newsflow.model.debug.DebugSettingDialogFactory;
import com.yooiistudios.newsflow.model.debug.DebugSettings;
import com.yooiistudios.newsflow.model.news.News;
import com.yooiistudios.newsflow.model.news.NewsContentProvider;
import com.yooiistudios.newsflow.model.news.NewsFeed;
import com.yooiistudios.newsflow.model.news.NewsImageRequestQueue;
import com.yooiistudios.newsflow.model.news.NewsProvider;
import com.yooiistudios.newsflow.model.news.NewsTopic;
import com.yooiistudios.newsflow.model.news.TintType;
import com.yooiistudios.newsflow.model.news.task.NewsFeedDetailNewsFeedFetchTask;
import com.yooiistudios.newsflow.model.news.task.NewsFeedDetailNewsImageUrlFetchTask;
import com.yooiistudios.newsflow.model.news.util.NewsFeedUtils;
import com.yooiistudios.newsflow.ui.adapter.NewsFeedDetailAdapter;
import com.yooiistudios.newsflow.ui.animation.NewsFeedDetailTransitionUtils;
import com.yooiistudios.newsflow.ui.fragment.NewsSelectFragment;
import com.yooiistudios.newsflow.ui.itemanimator.DetailNewsItemAnimator;
import com.yooiistudios.newsflow.ui.widget.NewsTopicSelectDialogFactory;
import com.yooiistudios.newsflow.ui.widget.ObservableScrollView;
import com.yooiistudios.newsflow.util.AnalyticsUtils;
import com.yooiistudios.newsflow.util.ImageMemoryCache;
import com.yooiistudios.newsflow.util.NLLog;
import com.yooiistudios.newsflow.util.ScreenUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors (prefix = "m")
public class NewsFeedDetailActivity extends ActionBarActivity
        implements NewsFeedDetailAdapter.OnItemClickListener, ObservableScrollView.Callbacks,
        NewsFeedDetailNewsFeedFetchTask.OnFetchListener,
        NewsFeedDetailNewsImageUrlFetchTask.OnImageUrlFetchListener,
        NewsTopicSelectDialogFactory.OnItemClickListener {
    private static final String TAG = NewsFeedDetailActivity.class.getName();

    private static final int BACKGROUND_COLOR = Color.WHITE;

    // Overlay & Shadow
    private static final float TOP_OVERLAY_ALPHA_LIMIT = 0.75f;
    private static final float TOP_OVERLAY_ALPHA_RATIO = 0.0008f;
    private static final float TOP_SCROLL_PARALLAX_RATIO = 0.4f;

    private static final int BOTTOM_NEWS_ANIM_DELAY_UNIT_MILLI = 60;
    private static final int ACTIVITY_ENTER_ANIMATION_DURATION = 600;

    // Auto Scroll
    public static final int START_DELAY = 3000;
    public static final int MIDDLE_DELAY = 1500;
    public static final int DURATION_FOR_EACH_ITEM = 3000;

    public static final String INTENT_KEY_NEWS = "INTENT_KEY_NEWS";
    public static final String INTENT_KEY_NEWSFEED_REPLACED = "INTENT_KEY_NEWSFEED_REPLACED";
    public static final String INTENT_KEY_IMAGE_LOADED = "INTENT_KEY_IMAGE_LOADED";
    public static final String INTENT_KEY_IMAGE_URL = "INTENT_KEY_IMAGE_URL";

    public static final int REQ_SELECT_NEWS_FEED = 13841;

    @Getter @InjectView(R.id.newsfeed_detail_toolbar)               Toolbar mToolbar;
    @Getter @InjectView(R.id.detail_content_layout)                 RelativeLayout mRootLayout;
    @Getter @InjectView(R.id.detail_transition_content_layout)      FrameLayout mTransitionLayout;
    @Getter @InjectView(R.id.detail_toolbar_overlay_view)           View mToolbarOverlayView;
    @Getter @InjectView(R.id.detail_top_gradient_shadow_view)       View mTopGradientShadowView;
    @InjectView(R.id.detail_scrollView)                             ObservableScrollView mScrollView;
    @InjectView(R.id.detail_scroll_content_wrapper)                 RelativeLayout mScrollContentWrapper;
    @InjectView(R.id.newsfeed_detail_swipe_refresh_layout)          SwipeRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.detail_loading_cover)                          View mLoadingCoverView;
    @Getter @InjectView(R.id.newsfeed_detail_reveal_view)           View mRevealView;

    // Top
    @Getter @InjectView(R.id.detail_top_news_image_wrapper)         FrameLayout mTopNewsImageWrapper;
    @InjectView(R.id.detail_top_news_image_ripple_view)             View mTopNewsImageRippleView;
    @Getter @InjectView(R.id.detail_top_news_image_view)            ImageView mTopImageView;
    @Getter @InjectView(R.id.detail_top_news_text_layout)           LinearLayout mTopNewsTextLayout;
    @InjectView(R.id.detail_top_news_text_ripple_layout)            LinearLayout mTopNewsTextRippleLayout;
    @Getter @InjectView(R.id.detail_top_news_title_text_view)       TextView mTopTitleTextView;
    @Getter @InjectView(R.id.detail_top_news_description_text_view) TextView mTopDescriptionTextView;

    // Bottom
    @Getter @InjectView(R.id.detail_bottom_news_recycler_view)      RecyclerView mBottomNewsListRecyclerView;

    ObjectAnimator mAutoScrollDownAnimator;
    ObjectAnimator mAutoScrollUpAnimator;

    private Palette mPalette;

    private ImageLoader mImageLoader;

    private NewsFeed mNewsFeed;
    private News mTopNews;
    private Bitmap mTopImageBitmap;
    private NewsFeedDetailAdapter mAdapter;
    private TintType mTintType;
//    @Getter private ColorDrawable mRootLayoutBackground;
    private ColorDrawable mRecyclerViewBackground;
    @Getter private BitmapDrawable mToolbarHomeIcon;
    @Getter private BitmapDrawable mToolbarOverflowIcon;
    @Getter private SpannableString mToolbarTitle;
    @Getter private AlphaForegroundColorSpan mToolbarTitleColorSpan;
    private int mToolbarTextColor;

    private NewsFeedDetailNewsFeedFetchTask mNewsFeedFetchTask;
    private NewsFeedDetailNewsImageUrlFetchTask mTopNewsImageFetchTask;


    private int mWindowInsetEnd;

    private boolean mIsRefreshing = false;
    private boolean mIsAnimatingActivityTransitionAnimation = false;

    @InjectView(R.id.newsfeed_detail_ad_upper_view) View mAdUpperView;
    @InjectView(R.id.newsfeed_detail_adView) AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_newsfeed_detail);
        ButterKnife.inject(this);

        Context context = getApplicationContext();
        mImageLoader = new ImageLoader(NewsImageRequestQueue.getInstance(context).getRequestQueue(),
                ImageMemoryCache.getInstance(context));

        // retrieve feed from intent
        mNewsFeed = getIntent().getExtras().getParcelable(NewsFeed.KEY_NEWS_FEED);
        Object tintTypeObj = getIntent().getExtras().getSerializable(MainActivity.INTENT_KEY_TINT_TYPE);
        mTintType = tintTypeObj != null ? (TintType)tintTypeObj : null;

        int topNewsIndex = getIntent().getExtras().getInt(News.KEY_CURRENT_NEWS_INDEX);
        if (topNewsIndex < mNewsFeed.getNewsList().size()) {
            mTopNews = mNewsFeed.getNewsList().remove(topNewsIndex);
        }

        applySystemWindowsBottomInset();
//        initRootLayout();
        initRevealView();
        initToolbar();
        initSwipeRefreshView();
        initCustomScrollView();
        initTopNews();
        initBottomNewsList();
        initLoadingCoverView();
        initAdView();

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (savedInstanceState == null) {
            if (mTopImageView.getDrawable() != null) {
                NewsFeedDetailTransitionUtils.runEnterAnimation(this);
//                startTransitionAnimation();
            } else {
                showLoadingCover();
            }
        }
        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    private void initRevealView() {
        mRevealView.setBackgroundColor(BACKGROUND_COLOR);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mNewsFeedFetchTask != null) {
            mNewsFeedFetchTask.cancel(true);
        }
        if (mTopNewsImageFetchTask != null) {
            mTopNewsImageFetchTask.cancel(true);
        }
    }

//    private void initRootLayout() {
//        mRootLayoutBackground = new ColorDrawable(BACKGROUND_COLOR);
//        mRootLayout.setBackground(mRootLayoutBackground);
//    }

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
//                    mAdView.bringToFront();
                    mAdUpperView.setVisibility(View.VISIBLE);
//                    mAdUpperView.bringToFront();
                }
            });
        }
    }

    private void checkAdView() {
        // NO_ADS 만 체크해도 풀버전까지 체크됨
        if (IabProducts.containsSku(getApplicationContext(), IabProducts.SKU_NO_ADS)) {
            mScrollContentWrapper.setPadding(0, 0, 0, mWindowInsetEnd);
            mAdUpperView.setVisibility(View.GONE);
            mAdView.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        } else {
            int adViewHeight = getResources().getDimensionPixelSize(R.dimen.admob_smart_banner_height);
            mScrollContentWrapper.setPadding(0, 0, 0, mWindowInsetEnd + adViewHeight);
            mAdUpperView.setVisibility(View.VISIBLE);
            mAdView.setVisibility(View.VISIBLE);
            mAdView.resume();

            RelativeLayout.LayoutParams adViewLp =
                    (RelativeLayout.LayoutParams)mAdView.getLayoutParams();
            adViewLp.bottomMargin = mWindowInsetEnd;

            // 네비게이션바에 색상 입히기
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
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

    @Override
    public void finish() {
        if (mTopImageView.getDrawable() == null) {
            super.finish();
        } else {
            if (true || !mIsAnimatingActivityTransitionAnimation) {
                super.finish();
//                runExitAnimation();
            }
        }
    }

    private void initToolbar() {
        mToolbar.bringToFront();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adjustToolbarTopMargin();
        initToolbarGradientView();
        initToolbarIcon();
        initToolbarTitle();
    }

    private void adjustToolbarTopMargin() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarHeight = ScreenUtils.getStatusBarHeight(this);
            if (statusBarHeight > 0) {
                ((RelativeLayout.LayoutParams) mToolbar.getLayoutParams()).topMargin = statusBarHeight;
            }
        }
    }

    private void initToolbarTitle() {
        mToolbarTitleColorSpan = new AlphaForegroundColorSpan(
                getResources().getColor(R.color.material_white_primary_text));

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
            Bitmap upIconBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_ab_up_white);
            mToolbarHomeIcon = new BitmapDrawable(getResources(), upIconBitmap);
            getSupportActionBar().setHomeAsUpIndicator(mToolbarHomeIcon);

            Bitmap overflowIconBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_menu_moreoverflow_mtrl_alpha);
            mToolbarOverflowIcon = new BitmapDrawable(getResources(), overflowIconBitmap);
        }
    }

    private void initToolbarGradientView() {
        // 기존의 계산된 ActionBar 높이와 Toolbar 의 실제가 높이가 달라 측정해서 적용하게 변경
        final int statusBarSize = ScreenUtils.getStatusBarHeight(this);
        mToolbar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mToolbar.getViewTreeObserver().removeOnPreDrawListener(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mTopGradientShadowView.getLayoutParams().height = (mToolbar.getHeight() + statusBarSize) * 2;
                    mToolbarOverlayView.getLayoutParams().height = mToolbar.getHeight() + statusBarSize;
                } else {
                    mTopGradientShadowView.getLayoutParams().height = mToolbar.getHeight() * 2;
                    mToolbarOverlayView.getLayoutParams().height = mToolbar.getHeight();
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAdView();
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
                return mIsRefreshing || mIsAnimatingActivityTransitionAnimation;
            }
        });
    }

    private void initTopNews() {
//        mTopTitleTextView.setAlpha(0);
//        mTopDescriptionTextView.setAlpha(0);

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

        if (mTopNews != null) {
            notifyTopNewsChanged();
        } else {
            // TODO: when NewsFeed is invalid.
        }
    }

    private void initBottomNewsList() {
        //init ui

        final RecyclerView.ItemAnimator itemAnimator;

        mBottomNewsListRecyclerView.setHasFixedSize(true);
        mBottomNewsListRecyclerView.setItemAnimator(
                new DetailNewsItemAnimator(mBottomNewsListRecyclerView));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mBottomNewsListRecyclerView.setLayoutManager(layoutManager);

        mRecyclerViewBackground = new ColorDrawable(BACKGROUND_COLOR);
        mBottomNewsListRecyclerView.setBackground(mRecyclerViewBackground);

        notifyBottomNewsChanged();
    }

    private void notifyBottomNewsChanged() {
        mAdapter = new NewsFeedDetailAdapter(this);

        mBottomNewsListRecyclerView.setAdapter(mAdapter);

        // make bottom news array list. EXCLUDE top news.
//        mBottomNewsList = new ArrayList<NLNews>(mNewsFeed.getNewsList());

        mAdapter.setNewsFeed(mNewsFeed.getNewsList());

//        final int newsCount = mNewsFeed.getNewsList().size();
//        for (int i = 0; i < newsCount; i++) {
//            News news = mNewsFeed.getNewsList().get(i);
//            mAdapter.addNews(news);
//        }
        applyMaxBottomRecyclerViewHeight();

        ViewTreeObserver observer = mBottomNewsListRecyclerView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mBottomNewsListRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                applyActualRecyclerViewHeight();
                return true;
            }
        });
    }

    private void applyActualRecyclerViewHeight() {
        int totalHeight = 0;
        int childCount = mBottomNewsListRecyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            totalHeight += mBottomNewsListRecyclerView.getChildAt(i).getHeight();
        }

        mBottomNewsListRecyclerView.getLayoutParams().height = totalHeight;
        mAdapter.notifyDataSetChanged();
        if (Settings.isNewsFeedAutoScroll(this)) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SubMenu subMenu = menu.addSubMenu(Menu.NONE, R.id.action_newsfeed_overflow, 0, "");
        subMenu.setIcon(mToolbarOverflowIcon);

        String autoScrollString = getString(R.string.newsfeed_auto_scroll) + " ";
        if (Settings.isNewsFeedAutoScroll(this)) {
            autoScrollString += getString(R.string.off);
        } else {
            autoScrollString += getString(R.string.on);
        }

        if (NLLog.isDebug()) {
            subMenu.add(Menu.NONE, R.id.action_select_topic, 0, "Select topics(Debug)");
            subMenu.add(Menu.NONE, R.id.action_auto_scroll_setting_debug, 2, "Auto Scroll Setting(Debug)");
        }
        subMenu.add(Menu.NONE, R.id.action_auto_scroll, 1, autoScrollString);

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

            case R.id.action_replace_newsfeed:
                startActivityForResult(new Intent(NewsFeedDetailActivity.this, NewsSelectActivity.class),
                        REQ_SELECT_NEWS_FEED);
                // 교체화면으로 가면 무조건 자동스크롤은 멈춰주기 - 교체하고 돌아올 경우 꼬인다
                stopAutoScroll();
                return true;

            case R.id.action_auto_scroll:
                boolean isAutoScroll = Settings.isNewsFeedAutoScroll(this);
                isAutoScroll = !isAutoScroll;
                Settings.setNewsFeedAutoScroll(this, isAutoScroll);

                String autoScrollString = getString(R.string.newsfeed_auto_scroll) + " ";
                if (isAutoScroll) {
                    autoScrollString += getString(R.string.off);
                } else {
                    autoScrollString += getString(R.string.on);
                }
                item.setTitle(autoScrollString);

                if (isAutoScroll) {
                    stopAutoScroll();
                    startAutoScroll();
                }
                return true;

            case R.id.action_auto_scroll_setting_debug:
                DebugSettingDialogFactory.showAutoScrollSettingDialog(this,
                        new DebugSettingDialogFactory.DebugSettingListener() {
                            @Override
                            public void autoScrollSettingSaved() {
                                stopAutoScroll();
                                startAutoScroll();
                            }
                        });
                return true;

            case R.id.action_select_topic:
                NewsProvider newsProvider =
                        NewsContentProvider.getInstance(this).getNewsProvider(mNewsFeed);
                if (newsProvider != null) {
                    NewsTopicSelectDialogFactory.makeDialog(this, newsProvider, this).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applyMaxBottomRecyclerViewHeight() {
        int maxRowHeight = NewsFeedDetailAdapter.measureMaximumRowHeight(getApplicationContext());
//        NLLog.now("maxRowHeight : " + maxRowHeight);

        int newsListCount = mNewsFeed.getNewsList().size();
        mBottomNewsListRecyclerView.getLayoutParams().height =
                maxRowHeight * newsListCount;
    }

    private void notifyTopNewsChanged() {
        // set action bar title
        applyToolbarTitle();

        // set title
        mTopTitleTextView.setText(mTopNews.getTitle());

        // set description
        if (mTopNews.getDescription() == null) {
            mTopDescriptionTextView.setVisibility(View.GONE);
        } else {
            mTopDescriptionTextView.setText(mTopNews.getDescription());

            // 타이틀 아래 패딩 조절
            mTopTitleTextView.setPadding(mTopTitleTextView.getPaddingLeft(),
                    mTopTitleTextView.getPaddingTop(), mTopTitleTextView.getPaddingRight(), 0);
        }

        applyImage();
    }

    private void applyImage() {
        // set image
        String imgUrl = mTopNews.getImageUrl();
        getIntent().putExtra(INTENT_KEY_IMAGE_LOADED, true);
        getIntent().putExtra(INTENT_KEY_IMAGE_URL, imgUrl);
        setResult(RESULT_OK, getIntent());

        if (imgUrl != null) {
            mImageLoader.get(imgUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bitmap = response.getBitmap();

                    if (bitmap == null && isImmediate) {
                        // 비트맵이 null 이지만 인터넷을 통하지 않고 바로 불린 콜백이라면 무시하자
                        return;
                    }

                    if (bitmap != null) {
                        setTopNewsImageBitmap(bitmap);

//                        animateTopItems();
                    }
                    configAfterRefreshDone();
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    applyDummyTopNewsImage();

//                    animateTopItems();

                    configAfterRefreshDone();
                }
            });
        } else if (mTopNews.isImageUrlChecked()) {
            applyDummyTopNewsImage();

//            animateTopItems();
        } else {
            showLoadingCover();
//            animateTopItems();
            mTopNewsImageFetchTask = new NewsFeedDetailNewsImageUrlFetchTask(mTopNews, this);
            mTopNewsImageFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void fetchNewsFeed(RssFetchable fetchable) {
        mNewsFeedFetchTask = new NewsFeedDetailNewsFeedFetchTask(fetchable, this, false);
        mNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        configBeforeRefresh();
    }

    private void configBeforeRefresh() {
        mIsRefreshing = true;
        mSwipeRefreshLayout.setRefreshing(true);
        NewsFeedDetailTransitionUtils.animateTopOverlayFadeOut(this);
        showLoadingCover();
    }

    private void configAfterRefreshDone() {
        mIsRefreshing = false;
        mSwipeRefreshLayout.setRefreshing(false);
        NewsFeedDetailTransitionUtils.animateTopOverlayFadeIn(this);
        hideLoadingCover();
    }

    private void showLoadingCover() {
        mLoadingCoverView.setVisibility(View.VISIBLE);
    }

    private void hideLoadingCover() {
        mLoadingCoverView.setVisibility(View.GONE);
    }

    private void applyDummyTopNewsImage() {
        _setTopNewsImageBitmap(NewsFeedUtils.getDummyNewsImage(getApplicationContext()),
                TintType.DUMMY);
    }

    private void setTopNewsImageBitmap(Bitmap bitmap) {
        _setTopNewsImageBitmap(bitmap, null);
    }

    private void _setTopNewsImageBitmap(Bitmap bitmap, TintType tintType) {
        mTopImageBitmap = bitmap;
        mTopImageView.setImageBitmap(mTopImageBitmap);

        mPalette = Palette.generate(mTopImageBitmap);


        if (tintType != null) {
            mTintType = tintType;
        } else if (mTintType == null) {
            // 이미지가 set 되기 전에 이 액티비티로 들어온 경우 mTintType == null이다.
            // 그러므로 이 상황에서 이미지가 set 된다면
            // 1. 메인 상단에서 들어온 경우 : TintType.GRAY_SCALE
            // 2. 메인 하단에서 들어온 경우 :
            // 2.1 palette에서 색을 꺼내 사용할 수 있는 경우 : TintType.PALETTE
            // 2.2 palette에서 색을 꺼내 사용할 수 없는 경우 : TintType.GRAY_SCALE(default)

            // 상단 뉴스피드인지 하단 뉴스피드인지 구분
            String newsLocation = getIntent().getExtras().getString(
                    MainActivity.INTENT_KEY_NEWS_FEED_LOCATION, null);

            if (newsLocation != null) {
                switch (newsLocation) {
                    case MainActivity.INTENT_VALUE_TOP_NEWS_FEED:
                        // 메인 상단에서 온 경우
                        mTintType = TintType.GRAY_SCALE;
                        break;
                    case MainActivity.INTENT_VALUE_BOTTOM_NEWS_FEED:
                        // 메인 하단에서 온 경우
                        int filterColor = getTopImageFilterColorPaletteItem();
                        if (filterColor != Color.TRANSPARENT) {
                            mTintType = TintType.PALETTE;
                        } else {
                            mTintType = TintType.GRAY_SCALE;
                        }
                        break;
                    default:
                        mTintType = TintType.GRAY_SCALE;
                        break;
                }
            } else {
                mTintType = TintType.GRAY_SCALE;
            }
        }
        applyPalette();
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
    }

    public int getFilterColor() {
        int color;
        int alpha;
        TintType tintType = mTintType != null ? mTintType : TintType.GRAY_SCALE;

        switch(tintType) {
            case DUMMY:
                color = NewsFeedUtils.getDummyImageFilterColor();
                alpha = Color.alpha(color);
                break;
            case PALETTE:
                int filterColor = getTopImageFilterColorPaletteItem();
                if (filterColor != Color.TRANSPARENT) {
                    color = filterColor;
                    alpha = getResources().getInteger(R.integer.vibrant_color_tint_alpha);
                    break;
                }
                // darkVibrantColor == null 이라면 아래의 구문으로 넘어간다.
            case GRAY_SCALE:
            default:
                color = NewsFeedUtils.getGrayFilterColor();
                alpha = Color.alpha(color);
                break;
        }

        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int getTopImageFilterColorPaletteItem() {
        return mPalette.getVibrantSwatch().getRgb();
//        return mPalette.getDarkVibrantColor(Color.TRANSPARENT);
    }

    @Override
    public void onItemClick(NewsFeedDetailAdapter.ViewHolder viewHolder, News news) {
//        NLLog.now("detail bottom onItemClick");

        Intent intent = new Intent(this, NewsDetailActivity.class);
        intent.putExtra(INTENT_KEY_NEWS, news);

        startActivity(intent);
//        NLWebUtils.openLink(this, news.getLink());
    }

    private void applySystemWindowsBottomInset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            applySystemWindowsBottomInsetAfterLollipop();
        } else {
            mWindowInsetEnd = 0;
            checkAdView();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void applySystemWindowsBottomInsetAfterLollipop() {
        mScrollContentWrapper.setFitsSystemWindows(true);
        mScrollContentWrapper.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                if (metrics.widthPixels < metrics.heightPixels) {
                    mWindowInsetEnd = windowInsets.getSystemWindowInsetBottom();
                    view.setPadding(0, 0, 0, mWindowInsetEnd);
                } else {
                    mWindowInsetEnd = windowInsets.getSystemWindowInsetRight();
                    view.setPadding(0, 0, mWindowInsetEnd, 0);
                }
                checkAdView();
                return windowInsets.consumeSystemWindowInsets();
            }
        });
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

            float ratio = 0.0008f;
            mToolbarOverlayView.setAlpha(scrollY * ratio);
            if (scrollY * ratio >= TOP_OVERLAY_ALPHA_LIMIT) {
                mToolbarOverlayView.setAlpha(TOP_OVERLAY_ALPHA_LIMIT);
                mTopGradientShadowView.setAlpha(0);
            } else {
                mTopGradientShadowView.setAlpha(1.f - scrollY * ratio);
            }
        } else {
            mTopNewsImageWrapper.setTranslationY(0);
            if (mToolbarOverlayView.getAlpha() != 0) {
                mToolbarOverlayView.setAlpha(0);
            }
            if (mTopGradientShadowView.getAlpha() != 1.f) {
                mTopGradientShadowView.setAlpha(1.f);
            }
        }
    }

    @Override
    public void onScrollStarted() {
        stopAutoScroll();
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
        news.setImageUrl(url);
        news.setImageUrlChecked(true);

        // 아카이빙을 위해 임시로 top news 를 news feed 에 추가.
        mNewsFeed.addNewsAt(0, news);
        archiveNewsFeed(mNewsFeed);
        mNewsFeed.removeNewsAt(0);

        applyImage();
    }

    @Override
    public void onImageUrlFetchFail(News news) {
        configAfterRefreshDone();

        news.setImageUrlChecked(true);

        applyImage();
    }

    @Override
    public void onSelectNewsTopic(Dialog dialog, NewsProvider newsProvider, int position) {
        dialog.dismiss();

        NewsTopic selectedTopic = newsProvider.getNewsTopicList().get(position);
        replaceNewsFeed(selectedTopic);
    }

    private void archiveNewsFeed(NewsFeed newsFeed) {
        // 이전 intent 를 사용, 상단 뉴스피드인지 하단 뉴스피드인지 구분
        String newsLocation = getIntent().getExtras().getString(
                MainActivity.INTENT_KEY_NEWS_FEED_LOCATION, null);

        // 저장
        if (newsLocation != null) {
            Context context = getApplicationContext();
            if (newsLocation.equals(MainActivity.INTENT_VALUE_TOP_NEWS_FEED)) {
                NewsDb.getInstance(context).saveTopNewsFeed(newsFeed);
//                NewsFeedArchiveUtils.saveTopNewsFeed(context, newsFeed);
            } else if (newsLocation.equals(MainActivity.INTENT_VALUE_BOTTOM_NEWS_FEED)) {
                int idx = getIntent().getExtras().getInt(
                        MainActivity.INTENT_KEY_BOTTOM_NEWS_FEED_INDEX);
                NewsDb.getInstance(context).saveBottomNewsFeedAt(newsFeed, idx);
//                NewsFeedArchiveUtils.saveBottomNewsFeedAt(context, newsFeed, idx);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch(requestCode) {
                case REQ_SELECT_NEWS_FEED:
                    RssFetchable rssFetchable = (RssFetchable)data.getExtras().getSerializable(
                            NewsSelectFragment.KEY_SELECTED_RSS_FETCHABLE);
                    replaceNewsFeed(rssFetchable);
                    break;
            }
        }
//        NLLog.now("onActivityResult-req:" + requestCode + "/result:" + resultCode);
    }

    private void replaceNewsFeed(RssFetchable fetchable) {
//        archiveNewsFeed(new NewsFeed(fetchable));
        fetchNewsFeed(fetchable);
    }

    private void startAutoScroll() {
        // 1초 기다렸다가 아래로 스크롤, 스크롤 된 뒤는 다시 위로 올라옴
        // 중간 터치가 있을 때에는 onScrollChanged 애니메이션을 중지
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int deviceHeight = displayMetrics.heightPixels;

        final int maxY = mScrollContentWrapper.getHeight() - deviceHeight - mWindowInsetEnd;

        int startDelay = DebugSettings.getStartDelay(this);
        final int durationForOneItem = DebugSettings.getDurationForEachItem(this);
        final int defaultDuration = mBottomNewsListRecyclerView.getChildCount() * durationForOneItem;
        final int middleDelay = DebugSettings.getMidDelay(this);
        int downScrollDuration = defaultDuration;

        // 아래 스크롤은 시작 위치에 따라 시간이 달라질 수 있음
        if (mScrollView.getScrollY() != 0) {
            downScrollDuration = (int) (downScrollDuration * (((float)maxY - mScrollView.getScrollY()) / maxY));
        }

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
}
