package com.yooiistudios.newsflow.ui.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.yooiistudios.newsflow.NewsApplication;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.RssFetchable;
import com.yooiistudios.newsflow.core.news.util.NewsFeedArchiveUtils;
import com.yooiistudios.newsflow.iab.IabProducts;
import com.yooiistudios.newsflow.model.BackgroundServiceUtils;
import com.yooiistudios.newsflow.model.PanelEditMode;
import com.yooiistudios.newsflow.model.ResizedImageLoader;
import com.yooiistudios.newsflow.model.Settings;
import com.yooiistudios.newsflow.core.news.database.NewsDb;
import com.yooiistudios.newsflow.ui.animation.NewsFeedDetailTransitionUtils;
import com.yooiistudios.newsflow.ui.fragment.SettingFragment;
import com.yooiistudios.newsflow.ui.widget.LoadingAnimationView;
import com.yooiistudios.newsflow.ui.widget.MainAdView;
import com.yooiistudios.newsflow.ui.widget.MainBottomContainerLayout;
import com.yooiistudios.newsflow.ui.widget.MainRefreshLayout;
import com.yooiistudios.newsflow.ui.widget.MainTopContainerLayout;
import com.yooiistudios.newsflow.util.AdDialogFactory;
import com.yooiistudios.newsflow.util.AdUtils;
import com.yooiistudios.newsflow.util.AnalyticsUtils;
import com.yooiistudios.newsflow.util.AppValidationChecker;
import com.yooiistudios.newsflow.core.util.ConnectivityUtils;
import com.yooiistudios.newsflow.core.util.Device;
import com.yooiistudios.newsflow.core.util.Display;
import com.yooiistudios.newsflow.util.FacebookUtils;
import com.yooiistudios.newsflow.core.util.NLLog;
import com.yooiistudios.newsflow.util.OnMainPanelEditModeEventListener;
import com.yooiistudios.newsflow.util.ReviewUtils;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends ActionBarActivity
        implements MainTopContainerLayout.OnMainTopLayoutEventListener,
        MainBottomContainerLayout.OnMainBottomLayoutEventListener,
        OnMainPanelEditModeEventListener {
    public static final String TAG = MainActivity.class.getName();
    public static final String INTENT_KEY_TINT_TYPE = "INTENT_KEY_TINT_TYPE";

    // 뉴스 새로고침시 사용할 인텐트 변수
    public static final String INTENT_KEY_NEWS_FEED_LOCATION = "INTENT_KEY_NEWS_FEED_LOCATION";
    public static final String INTENT_VALUE_TOP_NEWS_FEED = "INTENT_VALUE_TOP_NEWS_FEED";
    public static final String INTENT_VALUE_BOTTOM_NEWS_FEED = "INTENT_VALUE_BOTTOM_NEWS_FEED";
    public static final String INTENT_KEY_BOTTOM_NEWS_FEED_INDEX = "INTENT_KEY_BOTTOM_NEWS_FEED_INDEX";

    // 액티비티 트랜지션시 이미지뷰 애니메이트를 위한 변수를 넘길 인텐트 변수
    public static final String INTENT_KEY_TRANSITION_PROPERTY = "INTENT_KEY_TRANSITION_PROPERTY";

    public static final int RC_NEWS_FEED_DETAIL = 10001;
    public static final int RC_SETTING = 10002;
    public static final int RC_NEWS_FEED_SELECT = 10003;
    private static final int INVALID_WINDOW_INSET = -1;

    /**
     * Auto Refresh Handler
     */
    // auto refresh handler
    private boolean mIsHandlerRunning = false;
    private NewsAutoRefreshHandler mNewsAutoRefreshHandler = new NewsAutoRefreshHandler(this);

    @InjectView(R.id.main_root_layout)              RelativeLayout mRootLayout;
    @InjectView(R.id.main_toolbar)                  Toolbar mToolbar;
    @InjectView(R.id.main_loading_anim_view)        LoadingAnimationView mLoadingAnimationView;
    @InjectView(R.id.main_scroll_view)              ScrollView mScrollView;
    @InjectView(R.id.main_scrolling_content)        View mScrollingContent;
    @InjectView(R.id.main_swipe_refresh_layout)     MainRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.main_top_layout_container)     MainTopContainerLayout mMainTopContainerLayout;
    @InjectView(R.id.main_bottom_layout_container)  MainBottomContainerLayout mMainBottomContainerLayout;

    private MainAdView mBannerAd;
    private ResizedImageLoader mImageLoader;

    // Quit Ad Dialog
    private AdRequest mQuitAdRequest;
    private AdView mQuitAdView;

    private int mSystemWindowInsetBottom = INVALID_WINDOW_INSET;
    private int mSystemWindowInsetRight = INVALID_WINDOW_INSET;

    private static class NewsAutoRefreshHandler extends Handler {
        private WeakReference<MainActivity> mMainActivityRef;

        private NewsAutoRefreshHandler(MainActivity mainActivity) {
            mMainActivityRef = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mMainActivityRef.get();
            if (mainActivity != null) {
                MainTopContainerLayout mainTopContainerLayout =
                        mainActivity.mMainTopContainerLayout;
                MainBottomContainerLayout mainBottomContainerLayout =
                        mainActivity.mMainBottomContainerLayout;

                boolean isInEditingMode = mainTopContainerLayout.isInEditingMode()
                        || mainBottomContainerLayout.isInEditingMode();
                if (isInEditingMode) {
                    mainActivity.stopNewsAutoRefresh();
                } else {
                    mainTopContainerLayout.autoRefreshTopNewsFeed();
                    mainBottomContainerLayout.autoRefreshBottomNewsFeeds();

                    // tick 의 동작 시간을 계산해서 정확히 틱 초마다 UI 갱신을 요청할 수 있게 구현
                    sendEmptyMessageDelayed(0, Settings.getAutoRefreshHandlerDelay(mainActivity));
                }
            }
        }
    }

    /**
     * init
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (NLLog.isDebug()) {
            AppValidationChecker.validationCheck(this);
        }

        // start service on starting app
        NLLog.i("BackgroundServiceUtils", "onCreate");
        BackgroundServiceUtils.startService(getApplicationContext());

        // TODO off-line configuration
        boolean needsRefresh = NewsFeedArchiveUtils.newsNeedsToBeRefreshed(getApplicationContext());
        boolean isOnline = ConnectivityUtils.isNetworkAvailable(getApplicationContext());
        if (needsRefresh && !isOnline) {
            initNetworkUnavailableCoverLayout();
            return;
        }

        init();
    }

    private void init() {
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        // TODO ConcurrentModification 문제 우회를 위해 애니메이션이 끝나기 전 스크롤을 막던지 처리 해야함.
        initToolbar();
        initRefreshLayout();
        initBannerAdView();
        initQuitAdView();
        initImageLoader();
        mMainTopContainerLayout.init(this);
        mMainBottomContainerLayout.init(this);
        bringLoadingContainerToFront();
        showMainContentIfReady();
        requestSystemWindowsBottomInset();

        AdUtils.showPopupAdIfSatisfied(this);
        AnalyticsUtils.startAnalytics((NewsApplication) getApplication(), TAG);
    }

    public ResizedImageLoader getImageLoader() {
        return mImageLoader;
    }

    private void initImageLoader() {
        mImageLoader = ResizedImageLoader.create(this);
    }

    private void initToolbar() {
        mToolbar.bringToFront();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        adjustToolbarTopMargin();
    }

    private void adjustToolbarTopMargin() {
        if (Device.hasLollipop()) {
            int statusBarHeight = Display.getStatusBarHeight(this);
            if (statusBarHeight > 0) {
                ((RelativeLayout.LayoutParams) mToolbar.getLayoutParams()).topMargin = statusBarHeight;
            }
        }
    }

    private void initRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.app_color_accent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mMainTopContainerLayout.isReady() &&
                        !mMainBottomContainerLayout.isRefreshingBottomNewsFeeds()) {
                    stopNewsAutoRefresh();
                    setSwipeRefreshLayoutEnabled(false);

                    mMainTopContainerLayout.refreshNewsFeedOnSwipeDown();
                    mMainBottomContainerLayout.refreshBottomNewsFeeds();
                }
            }
        });

        // 초기 로딩시 swipe refresh 가 되지 않도록 설정
        setSwipeRefreshLayoutEnabled(false);
    }

    private void bringLoadingContainerToFront() {
//        mLoadingContainer.bringToFront();
        mLoadingAnimationView.bringToFront();
    }

    private void initNetworkUnavailableCoverLayout() {
        @SuppressLint("InflateParams")
        View networkUnavailableCoverLayout = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.network_unavailable_cover, null);
        setContentView(networkUnavailableCoverLayout);

        findViewById(R.id.network_unavailable_reload).setOnClickListener(new View.OnClickListener() {
            private int mPressCount = 0;

            @Override
            public void onClick(View v) {
                mPressCount++;
                boolean isNetworkAvailable = ConnectivityUtils.isNetworkAvailable(getApplicationContext());
                if (isNetworkAvailable) {
                    init();
                } else {
                    if (mPressCount > 5) {
                        TextView networkUnavailableMessageTextView
                                = (TextView) findViewById(R.id.network_unavailable_message);
                        int lineCount = networkUnavailableMessageTextView.getLineCount();
                        String messageToAppend = "\n";
                        switch (lineCount % 9) {
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                                messageToAppend += "안돼";
                                break;
                            case 6:
                                messageToAppend += "(｀Д´#)";
                                break;
                            case 7:
                                messageToAppend += "(#｀Д´)";
                                break;
                            case 8:
                                messageToAppend += "(#`・д・)";
                                break;
                            case 0:
                            default:
                                messageToAppend += "(´ﾟДﾟ`)";
                                break;
                        }
                        networkUnavailableMessageTextView.append(messageToAppend);
                    }
                }
            }
        });
    }

    private void initBannerAdView() {
        mBannerAd = new MainAdView(this);
        mBannerAd.setId(R.id.main_banner);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mRootLayout.addView(mBannerAd, lp);
    }

    private void configBannerAdOnInsetChanged() {
        if (isPortrait()) {
            mBannerAd.applyBottomMarginOnPortrait(mSystemWindowInsetBottom);
        } else {
            mBannerAd.applyBottomMarginOnPortrait(0);
        }

        bringLoadingContainerToFront();
    }

//    private void configSwipeRefreshLayoutOnOrientationChanged() {
//        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mSwipeRefreshLayout.getLayoutParams();
//        if (isPortrait()) {
//            lp.addRule(RelativeLayout.ABOVE, View.NO_ID);
//        } else {
//            lp.addRule(RelativeLayout.ABOVE, mBannerAd.getId());
//        }
//    }

    private void initQuitAdView() {
        // make AdView earlier for showing ad fast in the quit dialog
        mQuitAdRequest = new AdRequest.Builder().build();
        mQuitAdView = AdDialogFactory.initAdView(this, mQuitAdRequest);
    }

    private void configOnSystemInsetChanged() {
        // NO_ADS 만 체크해도 풀버전까지 체크됨
        boolean adPurchased = IabProducts.containsSku(getApplicationContext(), IabProducts.SKU_NO_ADS);
        if (adPurchased) {
            if (isPortrait()) {
                mScrollingContent.setPadding(0, 0, 0, mSystemWindowInsetBottom);
            } else {
                mScrollingContent.setPadding(0, 0, mSystemWindowInsetRight, 0);
            }
            mBannerAd.hide();
//            mBannerAd.setVisibility(View.GONE);
        } else {
            configBannerAdOnInsetChanged();
//            configSwipeRefreshLayoutOnOrientationChanged();
            if (isPortrait()) {
//                int adViewHeight = AdSize.SMART_BANNER.getHeightInPixels(getApplicationContext());
                int adViewHeight = getResources().getDimensionPixelSize(R.dimen.admob_smart_banner_height);
                mScrollingContent.setPadding(0, 0, 0, mSystemWindowInsetBottom + adViewHeight);
            } else {
                mScrollingContent.setPadding(0, 0, mSystemWindowInsetRight, 0);
            }
            mBannerAd.show();
//            mBannerAd.setVisibility(View.VISIBLE);

            mBannerAd.resume();
            mQuitAdView.resume();
        }
//        configNavigationTranslucentState();
    }

    private void configNavigationTranslucentState() {
        boolean adPurchased = IabProducts.containsSku(getApplicationContext(), IabProducts.SKU_NO_ADS);
        if (adPurchased && isPortrait()) {
            Display.applyTranslucentNavigationBarAfterLollipop(this);
        } else {
            Display.removeTranslucentNavigationBarAfterLollipop(this);
        }
    }

    public ViewGroup getMainTopContainerLayout() {
        return mMainTopContainerLayout;
    }

    private void setSwipeRefreshLayoutEnabled(boolean enable) {
        if (isPortrait()) {
            boolean readyForRefresh = mMainTopContainerLayout.isReady()
                    && !mMainBottomContainerLayout.isRefreshingBottomNewsFeeds();
            mSwipeRefreshLayout.setEnabled(enable && readyForRefresh);
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }
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
            setSystemWindowInset(0, 0);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestSystemWindowsBottomInsetAfterLollipop() {
        // TODO 아래 코드에 대해
        // TODO 1. 있는 경우 모든 inset 이 0으로 들어옴.
        // TODO 2. 없는 경우 필요한 inset 값이 들어온다.
        // TODO 나중에 최적화? 정리? 해야함
        mScrollingContent.setFitsSystemWindows(true);
        mScrollingContent.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                setSystemWindowInset(
                        windowInsets.getSystemWindowInsetRight(),
                        windowInsets.getSystemWindowInsetBottom());
//                configOnSystemInsetChanged(); // onResume 보다 늦게 호출되기에 최초 한 번은 여기서 확인이 필요
                return windowInsets.consumeSystemWindowInsets();
            }
        });
    }

    private void setSystemWindowInset(int rightInset, int bottomInset) {
        mSystemWindowInsetRight = rightInset;
        mSystemWindowInsetBottom = bottomInset;
        configOnSystemInsetChanged();
    }

    private boolean isSystemWindowInsetInvalid() {
        return mSystemWindowInsetRight == INVALID_WINDOW_INSET
                || mSystemWindowInsetBottom == INVALID_WINDOW_INSET;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRootLayout != null) {
            onConfigurationChanged(getResources().getConfiguration());
            startNewsAutoRefreshIfReady();
        }

        // 화면 켜짐 유지 설정
        SharedPreferences preferences = getSharedPreferences(
                SettingFragment.KEEP_SCREEN_ON_PREFS, Context.MODE_PRIVATE);
        if (preferences.getBoolean(SettingFragment.KEEP_SCREEN_ON_KEY, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        if (mRootLayout != null) {
            mBannerAd.pause();
            mQuitAdView.pause();
            stopNewsAutoRefresh();
            mImageLoader.flushCache();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImageLoader.closeCache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        SubMenu subMenu = menu.addSubMenu(Menu.NONE, R.id.action_newsfeed_overflow, 0, "");
        subMenu.setIcon(R.drawable.ic_menu_moreoverflow_mtrl_alpha);

        subMenu.add(Menu.NONE, R.id.action_store, 0, R.string.store);
        subMenu.add(Menu.NONE, R.id.action_edition, 1, R.string.action_edition);
        subMenu.add(Menu.NONE, R.id.action_info, 2, R.string.action_info);
        subMenu.add(Menu.NONE, R.id.action_settings, 3, R.string.action_settings);
        subMenu.add(Menu.NONE, R.id.action_rate_app, 4, R.string.action_rate_app);
        subMenu.add(Menu.NONE, R.id.action_facebook_like, 5, R.string.action_facebook_like);

        if (NLLog.isDebug()) {
            subMenu.add(Menu.NONE, R.id.action_remove_archive, 6, "Remove archive(Debug)");
            subMenu.add(Menu.NONE, R.id.action_copy_db, 7, "Copy db to sdcard(Debug");
            subMenu.add(Menu.NONE, R.id.action_slow_anim, 8, "Slow Activity Transition(Debug)");
            subMenu.add(Menu.NONE, R.id.action_service_log, 9, "Show service log(Debug)");
        }
        MenuItemCompat.setShowAsAction(subMenu.getItem(), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_store) {
            startActivity(new Intent(MainActivity.this, StoreActivity.class));
            return true;
        } else if (id == R.id.action_edition) {
            toggleEditLayoutVisibility();
            return true;
        } else if (id == R.id.action_info) {
            startActivity(new Intent(MainActivity.this, InfoActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivityForResult(new Intent(MainActivity.this, SettingActivity.class), RC_SETTING);
            return true;
        } else if (id == R.id.action_rate_app) {
            ReviewUtils.showReviewActivity(this);
            return true;
        } else if (id == R.id.action_facebook_like) {
            FacebookUtils.openYooiiPage(this);
            return true;
        // 여기서부터 debug 용
        } else if (id == R.id.action_remove_archive) {
            NewsFeedArchiveUtils.clearArchive(getApplicationContext());
        } else if (id == R.id.action_copy_db) {
            NewsDb.copyDbToExternalStorage(this);
        } else if (id == R.id.action_slow_anim) {
            NewsFeedDetailTransitionUtils.toggleUseScaledDurationDebug(getApplicationContext());
            item.setChecked(!item.isChecked());
        } else if (id == R.id.action_service_log) {
            BackgroundServiceUtils.showDialog(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMainTopInitialLoad() {
        showMainContentIfReady();
        startNewsAutoRefreshIfReady();
    }

    @Override
    public void onMainTopRefresh() {
        configAfterRefreshDone();
    }

    @Override
    public void onMainBottomInitialLoad() {
        showMainContentIfReady();
        startNewsAutoRefreshIfReady();
    }

    @Override
    public void onMainBottomRefresh() {
        configAfterRefreshDone();
    }

    @Override
    public void onMainBottomNewsImageInitiallyAllFetched() {
        startNewsAutoRefreshIfReady();
    }

    @Override
    public void onMainBottomNewsReplaceDone() {
        startNewsAutoRefreshIfReady();
    }

    @Override
    public void onMainBottomMatrixChanged() {
        startNewsAutoRefreshIfReady();
    }

    @Override
    public void onStartNewsFeedDetailActivityFromTopNewsFeed(Intent intent) {
        startNewsFeedDetailWithIntent(intent);
    }

    @Override
    public void onStartNewsFeedDetailActivityFromBottomNewsFeed(Intent intent) {
        startNewsFeedDetailWithIntent(intent);
    }

    private void startNewsFeedDetailWithIntent(Intent intent) {
        startActivityForResult(intent, RC_NEWS_FEED_DETAIL);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            overridePendingTransition(0, 0);
//        }
    }

    @Override
    public void onStartNewsFeedSelectActivityFromTopNewsFeed(Intent intent) {
        startActivityForResult(intent, RC_NEWS_FEED_SELECT);
    }

    @Override
    public void onStartNewsFeedSelectActivityFromBottomNewsFeed(Intent intent) {
        startActivityForResult(intent, RC_NEWS_FEED_SELECT);
    }

    private void startNewsAutoRefreshIfReady() {
        if (mMainTopContainerLayout.isReady()
                && mMainBottomContainerLayout.isInitialized()
                && mMainBottomContainerLayout.isInitializedFirstImages()
                && !mMainBottomContainerLayout.isReplacingBottomNewsFeed()
                && !mMainBottomContainerLayout.isRefreshingBottomNewsFeeds()
                && !mMainBottomContainerLayout.isFetchingAddedBottomNewsFeeds()) {
            startNewsAutoRefresh();
        }
    }

    private void startNewsAutoRefresh() {
        if (mIsHandlerRunning) {
            return;
        }
        mIsHandlerRunning = true;
        mNewsAutoRefreshHandler.sendEmptyMessageDelayed(0, Settings.getAutoRefreshHandlerDelay(this));
    }

    private void stopNewsAutoRefresh() {
        if (!mIsHandlerRunning) {
            return;
        }
        mIsHandlerRunning = false;
        mNewsAutoRefreshHandler.removeMessages(0);
    }

    private void showMainContentIfReady() {
        boolean topReady = mMainTopContainerLayout.isReady();
        boolean bottomReady = mMainBottomContainerLayout.isInitialized();

        if (topReady && bottomReady) {
            NLLog.now("topReady && bottomReady");
            mSwipeRefreshLayout.setRefreshing(false);
            setSwipeRefreshLayoutEnabled(true);

//            NewsFeedArchiveUtils.saveRecentCacheMillisec(getApplicationContext());

            if (!mLoadingAnimationView.isAnimating()) {
                NLLog.now("!mLoadingAnimationView.isAnimating()");
                // onCreate 에서 바로 캐시를 읽어 올 경우
                mLoadingAnimationView.startCircleAnimation();
            } else {
                NLLog.now("mLoadingAnimationView.isAnimating()");
                // Top, Bottom Layout 에서 불리는 경우, 패널 애니메이션 중
                mLoadingAnimationView.stopPanelAnimationAndStartArcAnimation();
            }
        } else {
            NLLog.now("!(topReady && bottomReady)");
            mLoadingAnimationView.startPanelAnimation();
        }
    }

    private void configAfterRefreshDone() {
        if (mMainTopContainerLayout.isReady() &&
                !mMainBottomContainerLayout.isRefreshingBottomNewsFeeds()) {
            // dismiss loading progress bar
            mSwipeRefreshLayout.setRefreshing(false);
            setSwipeRefreshLayoutEnabled(true);
//            NewsFeedArchiveUtils.saveRecentCacheMillisec(getApplicationContext());
            startNewsAutoRefresh();
        }
    }

    private void configOnPortraitOrientation() {
        RelativeLayout.LayoutParams bottomLayoutParams =
                ((RelativeLayout.LayoutParams)mMainBottomContainerLayout.getLayoutParams());

        bottomLayoutParams.addRule(RelativeLayout.BELOW, mMainTopContainerLayout.getId());
        bottomLayoutParams.addRule(RelativeLayout.RIGHT_OF, 0);

        setSwipeRefreshLayoutEnabled(true);

        mScrollView.setEnabled(true);
    }

    private void configOnLandscapeOrientation() {
        RelativeLayout.LayoutParams bottomLayoutParams =
                ((RelativeLayout.LayoutParams)mMainBottomContainerLayout.getLayoutParams());

        bottomLayoutParams.addRule(RelativeLayout.BELOW, 0);
        bottomLayoutParams.addRule(RelativeLayout.RIGHT_OF, mMainTopContainerLayout.getId());

        setSwipeRefreshLayoutEnabled(false);

        mScrollView.setEnabled(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        requestSystemWindowsBottomInset();

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            configOnPortraitOrientation();
        } else {
            configOnLandscapeOrientation();
        }

        mMainTopContainerLayout.configOnOrientationChange();
        mMainBottomContainerLayout.configOnOrientationChange();
    }

    private boolean isPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    public void onEditModeChange(PanelEditMode editMode) {
        if (editMode.equals(PanelEditMode.EDITING)) {
            showEditLayout();
        } else {
            hideEditLayout();
        }
    }

    private void toggleEditLayoutVisibility() {
        if (!isShowingEditLayout()) {
            showEditLayout();
        } else {
            hideEditLayout();
        }
    }

    private void showEditLayout() {
        mMainTopContainerLayout.showEditLayout();
        mMainBottomContainerLayout.showEditLayout();
        stopNewsAutoRefresh();
        setSwipeRefreshLayoutEnabled(false);
        // TODO 애니메이션을 취소시킬때 중간에서 끊길 경우 생각해보기
//        mMainBottomContainerLayout.cancelAutoRefresh();
        mToolbar.setVisibility(View.INVISIBLE);
    }

    private void hideEditLayout() {
        mMainTopContainerLayout.hideEditLayout();
        mMainBottomContainerLayout.hideEditLayout();
        startNewsAutoRefresh();
        setSwipeRefreshLayoutEnabled(true);
        mToolbar.setVisibility(View.VISIBLE);
    }

    private boolean isShowingEditLayout() {
        return mMainTopContainerLayout.isInEditingMode() || mMainBottomContainerLayout.isInEditingMode();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            String newsFeedType;
            switch (requestCode) {
                case RC_NEWS_FEED_DETAIL:
                    newsFeedType = extras.getString(INTENT_KEY_NEWS_FEED_LOCATION, null);

                    if (newsFeedType != null) {
                        if (extras.getBoolean(NewsFeedDetailActivity.INTENT_KEY_NEWSFEED_REPLACED, false)) {
                            configOnNewsFeedReplaced(extras);
                        } else if (extras.getBoolean(NewsFeedDetailActivity.INTENT_KEY_IMAGE_LOADED, false)) {
                            configOnNewImageLoaded(extras);
                        }
                    }
                    break;
                case RC_NEWS_FEED_SELECT:
                    RssFetchable rssFetchable = (RssFetchable)data.getExtras().getSerializable(
                            NewsSelectActivity.KEY_RSS_FETCHABLE);
                    if (rssFetchable != null) {
                        hideEditLayout();

                        newsFeedType = extras.getString(INTENT_KEY_NEWS_FEED_LOCATION, null);
                        if (newsFeedType.equals(INTENT_VALUE_BOTTOM_NEWS_FEED)) {
                            int idx = extras.getInt(INTENT_KEY_BOTTOM_NEWS_FEED_INDEX, -1);
                            if (idx >= 0) {
                                mMainBottomContainerLayout.applyNewsTopicAt(rssFetchable, idx);
                            }
                        } else if (newsFeedType.equals(INTENT_VALUE_TOP_NEWS_FEED)) {
                            mMainTopContainerLayout.applyNewsTopic(rssFetchable);
                        }
                    }
                    break;
                case RC_SETTING:
                    boolean panelMatrixChanged = extras.getBoolean(SettingActivity.PANEL_MATRIX_CHANGED);

                    if (panelMatrixChanged) {
                        mMainBottomContainerLayout.notifyPanelMatrixChanged();
                    }
                    break;
            }
        }
    }

    private void configOnNewImageLoaded(Bundle extras) {
        String imgUrl = extras.getString(
                NewsFeedDetailActivity.INTENT_KEY_IMAGE_URL, null);

        int newsIndex = extras.getInt(News.KEY_CURRENT_NEWS_INDEX, -1);
        String newsFeedType = extras.getString(INTENT_KEY_NEWS_FEED_LOCATION, null);
        if (newsFeedType.equals(INTENT_VALUE_BOTTOM_NEWS_FEED)) {
            int newsFeedIndex = extras.getInt(INTENT_KEY_BOTTOM_NEWS_FEED_INDEX, -1);
            if (newsFeedIndex >= 0 && newsIndex >= 0) {
                mMainBottomContainerLayout.configOnNewsImageUrlLoadedAt(
                        imgUrl, newsFeedIndex, newsIndex);
            }
        } else if (newsFeedType.equals(INTENT_VALUE_TOP_NEWS_FEED)) {
            if (newsIndex >= 0) {
                mMainTopContainerLayout.configOnNewsImageUrlLoadedAt(imgUrl, newsIndex);

                mSwipeRefreshLayout.setRefreshing(false);
                setSwipeRefreshLayoutEnabled(true);

                startNewsAutoRefreshIfReady();
            }
        }
    }

    private void configOnNewsFeedReplaced(Bundle extras) {
        String newsFeedType = extras.getString(INTENT_KEY_NEWS_FEED_LOCATION, null);
        if (newsFeedType.equals(INTENT_VALUE_BOTTOM_NEWS_FEED)) {
            // bottom news feed 의 index 를 가져옴
            int idx = extras.getInt(INTENT_KEY_BOTTOM_NEWS_FEED_INDEX, -1);
            if (idx >= 0) {
                mMainBottomContainerLayout.reloadNewsFeedAt(idx);
            }
        } else if (newsFeedType.equals(INTENT_VALUE_TOP_NEWS_FEED)) {
            // top news feed 가 교체됨
            mMainTopContainerLayout.configOnNewsFeedReplaced();
        }
    }

    @Override
    public void onBackPressed() {
        if (mMainTopContainerLayout.isInEditingMode() || mMainBottomContainerLayout.isInEditingMode()) {
            hideEditLayout();
        }
        else if (!IabProducts.containsSku(this, IabProducts.SKU_NO_ADS)
                && ConnectivityUtils.isNetworkAvailable(getApplicationContext())
                && mRootLayout != null) {
            AlertDialog adDialog = AdDialogFactory.makeAdDialog(MainActivity.this, mQuitAdView);
            if (adDialog != null) {
                adDialog.show();
                // make AdView again for next quit dialog
                // prevent child reference
                mQuitAdView = AdDialogFactory.initAdView(this, mQuitAdRequest);
            } else {
                // just finish activity when dialog is null
                super.onBackPressed();
            }
        } else {
            // just finish activity when no ad item is bought
            super.onBackPressed();
        }
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
}
