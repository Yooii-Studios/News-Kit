package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeedArchiveUtils;
import com.yooiistudios.news.ui.widget.MainBottomContainerLayout;
import com.yooiistudios.news.ui.widget.MainRefreshLayout;
import com.yooiistudios.news.ui.widget.MainTopContainerLayout;
import com.yooiistudios.news.util.FeedbackUtils;
import com.yooiistudios.news.util.NLLog;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity
        implements MainTopContainerLayout.OnMainTopLayoutEventListener,
        MainBottomContainerLayout.OnMainBottomLayoutEventListener {
    @InjectView(R.id.main_loading_container)        ViewGroup mLoadingContainer;
    @InjectView(R.id.main_loading_log)              TextView mLoadingLog;
    @InjectView(R.id.main_scrolling_content)        View mScrollingContent;
    @InjectView(R.id.main_swipe_refresh_layout)     MainRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.main_top_layout_container)     MainTopContainerLayout mMainTopContainerLayout;
    @InjectView(R.id.main_bottom_layout_container)  MainBottomContainerLayout mMainBottomContainerLayout;

    private static final String TAG = MainActivity.class.getName();
    public static final String VIEW_NAME_IMAGE_PREFIX = "topImage_";
    public static final String VIEW_NAME_TITLE_PREFIX = "topTitle_";
    public static final String INTENT_KEY_VIEW_NAME_IMAGE = "INTENT_KEY_VIEW_NAME_IMAGE";
    public static final String INTENT_KEY_VIEW_NAME_TITLE = "INTENT_KEY_VIEW_NAME_TITLE";
    public static final String INTENT_KEY_TINT_TYPE = "INTENT_KEY_TINT_TYPE";

    // 뉴스 새로고침시 사용할 인텐트 변수
    public static final String INTENT_KEY_NEWS_FEED_LOCATION = "INTENT_KEY_NEWS_FEED_LOCATION";
    public static final String INTENT_VALUE_TOP_NEWS_FEED = "INTENT_VALUE_TOP_NEWS_FEED";
    public static final String INTENT_VALUE_BOTTOM_NEWS_FEED = "INTENT_VALUE_BOTTOM_NEWS_FEED";
    public static final String INTENT_KEY_BOTTOM_NEWS_FEED_INDEX =
                                                            "INTENT_KEY_BOTTOM_NEWS_FEED_INDEX";

    // 액티비티 트랜지션시 이미지뷰 애니메이트를 위한 변수를 넘길 인텐트 변수
    public static final String INTENT_KEY_IMAGE_VIEW_LOCATION_LEFT =
            "INTENT_KEY_IMAGE_VIEW_LOCATION_LEFT";
    public static final String INTENT_KEY_IMAGE_VIEW_LOCATION_TOP =
            "INTENT_KEY_IMAGE_VIEW_LOCATION_TOP";
    public static final String INTENT_KEY_IMAGE_VIEW_LOCATION_WIDTH =
            "INTENT_KEY_IMAGE_VIEW_LOCATION_WIDTH";
    public static final String INTENT_KEY_IMAGE_VIEW_LOCATION_HEIGHT =
            "INTENT_KEY_IMAGE_VIEW_LOCATION_HEIGHT";
    public static final String INTENT_KEY_TEXT_VIEW_TEXT =
            "INTENT_KEY_TEXT_VIEW_TEXT";
    public static final String INTENT_KEY_TEXT_VIEW_TEXT_SIZE =
            "INTENT_KEY_TEXT_VIEW_TEXT_SIZE";
    public static final String INTENT_KEY_TEXT_VIEW_TEXT_COLOR =
            "INTENT_KEY_TEXT_VIEW_TEXT_COLOR";
    public static final String INTENT_KEY_TEXT_VIEW_GRAVITY =
            "INTENT_KEY_TEXT_VIEW_GRAVITY";
    public static final String INTENT_KEY_TEXT_VIEW_ELLIPSIZE_ORDINAL =
            "INTENT_KEY_TEXT_VIEW_ELLIPSIZE_ORDINAL";
    public static final String INTENT_KEY_TEXT_VIEW_MAX_LINE =
            "INTENT_KEY_TEXT_VIEW_MAX_LINE";
    public static final String INTENT_KEY_TEXT_VIEW_LEFT =
            "INTENT_KEY_TEXT_VIEW_LEFT";
    public static final String INTENT_KEY_TEXT_VIEW_TOP =
            "INTENT_KEY_TEXT_VIEW_TOP";
    public static final String INTENT_KEY_TEXT_VIEW_WIDTH =
            "INTENT_KEY_TEXT_VIEW_WIDTH";
    public static final String INTENT_KEY_TEXT_VIEW_HEIGHT =
            "INTENT_KEY_TEXT_VIEW_HEIGHT";

    public static final int RC_NEWS_FEED_DETAIL = 10001;

    /**
     * Auto Refresh Handler
     */
    // auto refresh handler
    private static final int AUTO_REFRESH_HANDLER_FIRST_DELAY = 4 * 1000; // finally to be 10 secs
    private static final int AUTO_REFRESH_HANDLER_DELAY = 7 * 1000; // finally to be 10 secs
    private boolean mIsHandlerRunning = false;
    private NewsAutoRefreshHandler mNewsAutoRefreshHandler = new NewsAutoRefreshHandler();

    @Override
    public void onMainTopInitialLoad() {
        NLLog.i(TAG, "onMainTopInitialLoad");
        showMainContentIfReady();
        startNewsAutoRefreshIfReady();
    }

    @Override
    public void onMainTopRefresh() {
        NLLog.i(TAG, "onMainTopRefresh");
        configAfterRefreshDone();
    }

    @Override
    public void onMainBottomInitialLoad() {
        NLLog.i(TAG, "onMainBottomInitialLoad");
        showMainContentIfReady();
        startNewsAutoRefreshIfReady();
    }

    @Override
    public void onMainBottomRefresh() {
        NLLog.i(TAG, "onMainBottomRefresh");
        configAfterRefreshDone();
    }

    @Override
    public void onMainBottomNewsImageInitiallyAllFetched() {
        NLLog.i(TAG, "onMainBottomNewsImageInitiallyAllFetched");
        startNewsAutoRefreshIfReady();
    }

    private class NewsAutoRefreshHandler extends Handler {
        @Override
        public void handleMessage( Message msg ){
            // 갱신
//            NLLog.now("newsAutoRefresh");
            mMainTopContainerLayout.autoRefreshTopNewsFeed();
            mMainBottomContainerLayout.autoRefreshBottomNewsFeeds();

            // tick 의 동작 시간을 계산해서 정확히 틱 초마다 UI 갱신을 요청할 수 있게 구현
            mNewsAutoRefreshHandler.sendEmptyMessageDelayed(0, AUTO_REFRESH_HANDLER_DELAY);
        }
    }

    private void startNewsAutoRefreshIfReady() {
        if (mMainTopContainerLayout.isInitialized()
                && mMainBottomContainerLayout.isInitialized()
                && mMainBottomContainerLayout.isInitializedFirstImages()) {
            startNewsAutoRefresh();
        }
    }

    private void startNewsAutoRefresh() {
        if (mIsHandlerRunning) {
            return;
        }
        mIsHandlerRunning = true;
        // 첫 실행이면 빠른 딜레이 주기
        SharedPreferences prefs = getSharedPreferences("AutoRefreshDelayPrefs", MODE_PRIVATE);
        int delay;
        if (prefs.getBoolean("isNotFirstAutoRefresh", false)) {
            delay = AUTO_REFRESH_HANDLER_FIRST_DELAY;
            prefs.edit().putBoolean("isNotFirstAutoRefresh", true).apply();
        } else {
            delay = AUTO_REFRESH_HANDLER_DELAY;
        }
        mNewsAutoRefreshHandler.sendEmptyMessageDelayed(0, delay);
    }

    private void stopNewsAutoRefresh() {
        if (!mIsHandlerRunning) {
            return;
        }
        mIsHandlerRunning = false;
        mNewsAutoRefreshHandler.removeMessages(0);
    }

    /**
     * Auto Refresh Handler End
     */

    @Override
    protected void onResume() {
        super.onResume();
        startNewsAutoRefreshIfReady();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopNewsAutoRefresh();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        boolean needsRefresh = NewsFeedArchiveUtils.newsNeedsToBeRefreshed(getApplicationContext());

        // TODO off-line configuration
        // TODO ConcurrentModification 문제 우회를 위해 애니메이션이 끝나기 전 스크롤을 막던지 처리 해야함.
        initRefreshLayout();

        mMainTopContainerLayout.init(this, needsRefresh);

        mMainBottomContainerLayout.init(this, needsRefresh);

        showMainContentIfReady();

        applySystemWindowsBottomInset(mScrollingContent);
    }
    //

    private void applySystemWindowsBottomInset(View containerView) {
        containerView.setFitsSystemWindows(true);
        containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                if (metrics.widthPixels < metrics.heightPixels) {
                    view.setPadding(0, 0, 0, windowInsets.getSystemWindowInsetBottom());
                } else {
                    view.setPadding(0, 0, windowInsets.getSystemWindowInsetRight(), 0);
                }
                return windowInsets.consumeSystemWindowInsets();
            }
        });
    }

    private void initRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_color_scheme_1, R.color.refresh_color_scheme_2,
                R.color.refresh_color_scheme_3, R.color.refresh_color_scheme_4);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                NLLog.i(TAG, "onRefresh called from SwipeRefreshLayout");
                if (!mMainTopContainerLayout.isRefreshingTopNewsFeed() &&
                        !mMainBottomContainerLayout.isRefreshingBottomNewsFeeds()) {
                    stopNewsAutoRefresh();
                    mSwipeRefreshLayout.setEnabled(false);

                    mMainTopContainerLayout.refreshNewsFeed();
                    mMainBottomContainerLayout.refreshBottomNewsFeeds();
                }
            }
        });

        // 초기 로딩시 swipe refresh가 되지 않도록 설정
        mSwipeRefreshLayout.setEnabled(false);
    }

    private void showMainContentIfReady() {
        boolean topReady = mMainTopContainerLayout.isInitialized();
        boolean bottomReady = mMainBottomContainerLayout.isInitialized();
        NLLog.i("showMainContentIfReady", "topReady : " + topReady);
        NLLog.i("showMainContentIfReady", "bottomReady : " + bottomReady);

        String loadingStatus = "Top news feed ready : " + topReady
                + "\nBottom news feed ready : " + bottomReady;

        mLoadingLog.setText(loadingStatus);

        if (mLoadingContainer.getVisibility() == View.GONE) {
            return;
        }

        if (topReady && bottomReady) {
            mSwipeRefreshLayout.setRefreshing(false);

            //
            mSwipeRefreshLayout.setEnabled(true);

            NewsFeedArchiveUtils.saveRecentCacheMillisec(getApplicationContext());

            // loaded
            mLoadingContainer.setVisibility(View.GONE);
        }
    }

    private void configAfterRefreshDone() {
        if (!mMainTopContainerLayout.isRefreshingTopNewsFeed() &&
                !mMainBottomContainerLayout.isRefreshingBottomNewsFeeds()) {
            // dismiss loading progress bar
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);
            NewsFeedArchiveUtils.saveRecentCacheMillisec(getApplicationContext());
            startNewsAutoRefresh();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            return true;
        } else if (id == R.id.action_store) {
            startActivity(new Intent(MainActivity.this, StoreActivity.class));
            return true;
        } else if (id == R.id.action_send_feedback) {
            FeedbackUtils.sendFeedback(this);
            return true;
        } else if (id == R.id.action_remove_archive) {
            NewsFeedArchiveUtils.clearArchive(getApplicationContext());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            switch (requestCode) {
                case RC_NEWS_FEED_DETAIL:
                    boolean hasNewsFeedReplaced = extras.getBoolean(
                            NewsFeedDetailActivity.INTENT_KEY_NEWSFEED_REPLACED, false);
                    String newsFeedType = extras.getString(INTENT_KEY_NEWS_FEED_LOCATION, null);
                    boolean newImageLoaded = extras.getBoolean(
                            NewsFeedDetailActivity.INTENT_KEY_IMAGE_LOADED, false);

                    if (newsFeedType == null) {
                        return;
                    }
                    if (hasNewsFeedReplaced) {
                        // 교체된게 top news feed인지 bottom news feed인지 구분
                        if (newsFeedType.equals(INTENT_VALUE_BOTTOM_NEWS_FEED)) {
                            // bottom news feed중 하나가 교체됨

                            // bottom news feed의 index를 가져옴
                            int idx = extras.getInt(INTENT_KEY_BOTTOM_NEWS_FEED_INDEX, -1);
                            if (idx >= 0) {
                                mMainBottomContainerLayout.configOnNewsFeedReplacedAt(idx);
                            }
                        } else if (newsFeedType.equals(INTENT_VALUE_TOP_NEWS_FEED)) {
                            // top news feed가 교체됨
                            mMainTopContainerLayout.configOnNewsFeedReplaced();
                        }
                    } else if (newImageLoaded) {
                        String imgUrl = extras.getString(
                                NewsFeedDetailActivity.INTENT_KEY_IMAGE_URL, null);

                        if (imgUrl == null) {
                            return;
                        }

                        int newsIndex = extras.getInt(News.KEY_CURRENT_NEWS_INDEX, -1);
                        if (newsFeedType.equals(INTENT_VALUE_BOTTOM_NEWS_FEED)) {
                            int newsFeedIndex = extras.getInt(INTENT_KEY_BOTTOM_NEWS_FEED_INDEX, -1);
                            if (newsFeedIndex >= 0 && newsIndex >= 0) {
                                mMainBottomContainerLayout.configOnNewsImageUrlLoadedAt(
                                        imgUrl, newsFeedIndex, newsIndex);
                            }
                        } else if (newsFeedType.equals(INTENT_VALUE_TOP_NEWS_FEED)) {
                            if (newsIndex >= 0) {
                                mMainTopContainerLayout.configOnNewsImageUrlLoadedAt(imgUrl, newsIndex);
                            }
                        }
                    }

                    break;
            }
        }
    }
}
