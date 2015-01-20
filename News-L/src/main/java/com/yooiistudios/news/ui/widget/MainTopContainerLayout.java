package com.yooiistudios.news.ui.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.activitytransition.ActivityTransitionHelper;
import com.yooiistudios.news.model.database.NewsDb;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.util.NewsFeedArchiveUtils;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.model.news.NewsImageRequestQueue;
import com.yooiistudios.news.model.news.TintType;
import com.yooiistudios.news.model.news.task.TopFeedNewsImageUrlFetchTask;
import com.yooiistudios.news.model.news.task.TopNewsFeedFetchTask;
import com.yooiistudios.news.ui.activity.MainActivity;
import com.yooiistudios.news.ui.activity.NewsFeedDetailActivity;
import com.yooiistudios.news.ui.adapter.MainTopPagerAdapter;
import com.yooiistudios.news.ui.animation.AnimationFactory;
import com.yooiistudios.news.ui.fragment.MainNewsFeedFragment;
import com.yooiistudios.news.ui.widget.viewpager.MainTopViewPager;
import com.yooiistudios.news.ui.widget.viewpager.ParallexViewPagerIndicator;
import com.yooiistudios.news.ui.widget.viewpager.SlowSpeedScroller;
import com.yooiistudios.news.util.ImageMemoryCache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_TRANSITION_PROPERTY;
import static com.yooiistudios.news.ui.activity.MainActivity.RC_NEWS_FEED_DETAIL;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 19.
 *
 * MainTopViewPager
 *  메인화면 상단 뷰페이저
 */
public class MainTopContainerLayout extends FrameLayout
        implements TopFeedNewsImageUrlFetchTask.OnTopFeedImageUrlFetchListener,
        TopNewsFeedFetchTask.OnFetchListener,
        MainTopPagerAdapter.OnItemClickListener {
    @InjectView(R.id.main_top_view_pager)                   MainTopViewPager mTopNewsFeedViewPager;
    @InjectView(R.id.main_top_view_pager_wrapper)           FrameLayout mTopNewsFeedViewPagerWrapper;
    @InjectView(R.id.main_top_unavailable_wrapper)          FrameLayout mTopNewsFeedUnavailableWrapper;
    @InjectView(R.id.main_top_view_pager_indicator)         ParallexViewPagerIndicator mTopViewPagerIndicator;
    @InjectView(R.id.main_top_news_feed_title_text_view)    TextView mTopNewsFeedTitleTextView;
    @InjectView(R.id.main_top_unavailable_description)      TextView mTopNewsFeedUnavailableDescription;

    private static final String TAG = MainTopContainerLayout.class.getName();

    private TopFeedNewsImageUrlFetchTask mTopImageUrlFetchTask;
    private TopNewsFeedFetchTask mTopNewsFeedFetchTask;
    private HashMap<News, TopFeedNewsImageUrlFetchTask> mTopNewsFeedNewsToImageTaskMap;
    private MainTopPagerAdapter mTopNewsFeedPagerAdapter;

    private OnMainTopLayoutEventListener mOnMainTopLayoutEventListener;

//    private NewsFeed mTopNewsFeed;
    private ImageLoader mImageLoader;
    private Activity mActivity;

    // flags for initializing
    private boolean mIsReady = false;

    // interface
    public interface OnMainTopLayoutEventListener {
        public void onMainTopInitialLoad();
        public void onMainTopRefresh();
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

        mImageLoader = new ImageLoader(NewsImageRequestQueue.getInstance(context).getRequestQueue(),
                ImageMemoryCache.getInstance(context));

        mTopNewsFeedViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.main_top_view_pager_page_margin));
    }

    public void animateOnInit() {
//        final View view = findViewById(R.id.main_top_root);
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);

                // get display height
                Point displaySize = new Point();
                mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int displayHeight = displaySize.y;

                // calculate translation Y for animation.
                int translationY = displayHeight - getTop();

                // animate
                setTranslationY(translationY);

                int duration = getResources().getInteger(R.integer.bottom_news_feed_init_move_up_anim_duration);
                animate()
                    .setDuration(duration)
                    .translationY(0)
                    .setInterpolator(AnimationFactory.makeDefaultReversePathInterpolator())
                    .start();

                return false;
            }
        });
    }

    public void autoRefreshTopNewsFeed() {
        NewsFeed newsFeed = mTopNewsFeedPagerAdapter.getNewsFeed();
        if (newsFeed == null || !newsFeed.isValid()) {
            // 네트워크도 없고 캐시 정보도 없는 경우
            return;
        }
        if (mTopNewsFeedViewPager.getCurrentItem() + 1 < mTopNewsFeedViewPager.getAdapter().getCount()) {
            mTopNewsFeedViewPager.setCurrentItem(mTopNewsFeedViewPager.getCurrentItem() + 1, true);
        } else {
            mTopNewsFeedViewPager.setCurrentItem(0, true);
        }
    }

    public void init(Activity activity) {
        if (!(activity instanceof MainActivity)) {
            throw new IllegalArgumentException("activity MUST BE an instance of MainActivity");
        }

        mActivity = activity;
        mOnMainTopLayoutEventListener = (OnMainTopLayoutEventListener)activity;

        Context context = getContext();

        initViewPager(context);

        // Fetch
//        showTopNewsFeedUnavailable(newsFeed.getFetchStateMessage(context));
        NewsFeed topNewsFeed = NewsDb.getInstance(context).loadTopNewsFeed(context);
        mTopNewsFeedPagerAdapter.setNewsFeed(topNewsFeed);
//        mTopNewsFeedPagerAdapter.setNewsFeed(NewsFeedArchiveUtils.loadTopNewsFeed(context));

        boolean needsRefresh = NewsFeedArchiveUtils.newsNeedsToBeRefreshed(context);
        if (needsRefresh) {
            mIsReady = false;
            fetchTopNewsFeed(TopNewsFeedFetchTask.TaskType.INITIALIZE);
        } else {
            if (mTopNewsFeedPagerAdapter.getNewsFeed().isValid()) {
                notifyNewTopNewsFeedSet();
                fetchFirstNewsImage(TopFeedNewsImageUrlFetchTask.TaskType.INITIALIZE);
//                notifyIfInitialized();
            } else {
                mIsReady = false;
                fetchTopNewsFeed(TopNewsFeedFetchTask.TaskType.INITIALIZE);
            }
        }

    }

    private void initViewPager(Context context) {
        // ViewPager
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            android.view.animation.Interpolator interpolator =
                    (android.view.animation.Interpolator)
                        AnimationFactory.makeViewPagerScrollInterpolator();
            SlowSpeedScroller scroller = new SlowSpeedScroller(context, interpolator, true);
            mScroller.set(mTopNewsFeedViewPager, scroller);
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
                mOnMainTopLayoutEventListener.onMainTopInitialLoad();
                break;
            case SWIPE_REFRESH:
            case REPLACE:
                mOnMainTopLayoutEventListener.onMainTopRefresh();
                break;
        }
    }

    private void notifyOnReady(TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        mIsReady = true;

        switch (taskType) {
            case INITIALIZE:
                mOnMainTopLayoutEventListener.onMainTopInitialLoad();
                break;
            case REFRESH:
            case REPLACE:
                mOnMainTopLayoutEventListener.onMainTopRefresh();
                break;
        }
    }

    private void fetchTopNewsFeed(TopNewsFeedFetchTask.TaskType taskType) {
        fetchTopNewsFeed(taskType, true);
    }

    private void fetchTopNewsFeed(TopNewsFeedFetchTask.TaskType taskType, boolean shuffle) {
        mTopNewsFeedFetchTask = new TopNewsFeedFetchTask(mTopNewsFeedPagerAdapter.getNewsFeed(),
                this, taskType, shuffle);
        mTopNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void notifyNewTopNewsFeedSet() {
        // show view pager wrapper
        mTopNewsFeedViewPagerWrapper.setVisibility(View.VISIBLE);
        mTopNewsFeedUnavailableWrapper.setVisibility(View.GONE);
        mTopViewPagerIndicator.setVisibility(View.VISIBLE);

        mTopNewsFeedViewPager.setAdapter(mTopNewsFeedPagerAdapter);
        mTopViewPagerIndicator.initialize(mTopNewsFeedPagerAdapter.getCount(), mTopNewsFeedViewPager);

        mTopNewsFeedTitleTextView.setText(mTopNewsFeedPagerAdapter.getNewsFeed().getTitle());
    }

    private void fetchFirstNewsImage(TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        ArrayList<News> items = mTopNewsFeedPagerAdapter.getNewsFeed().getNewsList();
        if (items.size() > 0) {
            News news = items.get(0);

            if (!news.isImageUrlChecked()) {
                mIsReady = false;
                mTopImageUrlFetchTask = new TopFeedNewsImageUrlFetchTask(news, 0,
                        taskType, this);
                mTopImageUrlFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                if (news.getImageUrl() == null) {
                    // no image
                    notifyOnReady(taskType);
                } else {
                    // 이미지 url은 가져온 상태.
                    applyImage(news.getImageUrl(), 0, taskType);
                }
            }
        } else {
            notifyOnReady(taskType);
        }
    }

    private void showTopNewsFeedUnavailable(String message) {
        // show top unavailable wrapper
        mTopNewsFeedViewPagerWrapper.setVisibility(View.GONE);
        mTopNewsFeedUnavailableWrapper.setVisibility(View.VISIBLE);
        mTopViewPagerIndicator.setVisibility(View.INVISIBLE);

        mTopNewsFeedUnavailableDescription.setText(message);
    }

    private void fetchTopNewsFeedImages(TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        if (mTopNewsFeedPagerAdapter.getNewsFeed() == null) {
            return;
        }
        mTopNewsFeedNewsToImageTaskMap = new HashMap<News, TopFeedNewsImageUrlFetchTask>();

        ArrayList<News> newsList = mTopNewsFeedPagerAdapter.getNewsFeed().getNewsList();

        for (int i = 0; i < newsList.size(); i++) {
            News news = newsList.get(i);

            if (news.getImageUrl() == null && !news.isImageUrlChecked()) {
                TopFeedNewsImageUrlFetchTask task =
                        new TopFeedNewsImageUrlFetchTask(news, i, taskType, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                mTopNewsFeedNewsToImageTaskMap.put(news, task);
            }
        }
    }

//    private void cancelTopNewsFeedImageFetchTasks() {
//        mTopNewsFeedReady = false;
//        for (Map.Entry<News, TopFeedNewsImageUrlFetchTask> entry :
//                mTopNewsFeedNewsToImageTaskMap.entrySet()) {
//            TopFeedNewsImageUrlFetchTask task = entry.getValue();
//            if (task != null) {
//                task.cancel(true);
//            }
//        }
//        mTopNewsFeedNewsToImageTaskMap.clear();
//    }

    public void refreshNewsFeedOnSwipeDown() {
        refreshTopNewsFeed(TopNewsFeedFetchTask.TaskType.SWIPE_REFRESH, true);
    }

    private void refreshTopNewsFeed(TopNewsFeedFetchTask.TaskType taskType, boolean shuffle) {
        mIsReady = false;

        NewsFeedUrl topNewsFeedUrl = mTopNewsFeedPagerAdapter.getNewsFeed().getNewsFeedUrl();
        NewsFeed topNewsFeed = new NewsFeed();
        topNewsFeed.setNewsFeedUrl(topNewsFeedUrl);
        topNewsFeed.getNewsList().add(null);

//        mTopViewPagerIndicator.setCurrentItem(0);
        mTopNewsFeedPagerAdapter = new MainTopPagerAdapter(mActivity.getFragmentManager());
        mTopNewsFeedPagerAdapter.setNewsFeed(topNewsFeed);
        mTopNewsFeedViewPager.setAdapter(mTopNewsFeedPagerAdapter);
        mTopViewPagerIndicator.initialize(mTopNewsFeedPagerAdapter.getCount(), mTopNewsFeedViewPager);
        mTopNewsFeedTitleTextView.setText("");

        fetchTopNewsFeed(taskType, shuffle);
    }

    public void configOnNewsFeedReplaced() {
        mIsReady = false;
        NewsFeed topNewsFeed = NewsDb.getInstance(getContext()).loadTopNewsFeed(getContext(), false);
        mTopNewsFeedPagerAdapter.setNewsFeed(topNewsFeed);
//        mTopNewsFeedPagerAdapter.setNewsFeed(NewsFeedArchiveUtils.loadTopNewsFeed(getContext(), false));
        if (mTopNewsFeedPagerAdapter.getNewsFeed().isValid()) {
            notifyNewTopNewsFeedSet();
            fetchFirstNewsImage(TopFeedNewsImageUrlFetchTask.TaskType.REPLACE);
        } else {
            refreshTopNewsFeed(TopNewsFeedFetchTask.TaskType.REPLACE, false);
        }
    }

    public void configOnNewsImageUrlLoadedAt(String imageUrl, int idx) {
        News news = mTopNewsFeedPagerAdapter.getNewsFeed().getNewsList().get(idx);
        news.setImageUrl(imageUrl);
        news.setImageUrlChecked(true);
        mTopNewsFeedPagerAdapter.notifyImageUrlLoaded(idx);

        mIsReady = true;
    }

    private void applyImage(String url, final int position,
                            final TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        mImageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() == null && isImmediate) {
                    return;
                }

                mTopNewsFeedPagerAdapter.notifyImageUrlLoaded(position);

                if (position == 0) {
                    notifyOnReady(taskType);

                    // fetch other images
                    fetchTopNewsFeedImages(taskType);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (position == 0) {
                    notifyOnReady(taskType);

                    // fetch other images
                    fetchTopNewsFeedImages(taskType);
                }
            }
        });
    }

    /**
     * TopNewsFeedFetch Listener
     */
    @Override
    public void onTopNewsFeedFetch(NewsFeed newsFeed, TopNewsFeedFetchTask.TaskType taskType) {
        Context context = getContext().getApplicationContext();

        mTopNewsFeedPagerAdapter.setNewsFeed(newsFeed);
        NewsDb.getInstance(context).saveTopNewsFeed(newsFeed);

//        NewsFeedArchiveUtils.saveTopNewsFeed(getContext(), newsFeed);

        if (taskType.equals(TopNewsFeedFetchTask.TaskType.INITIALIZE)) {
            if (newsFeed.isValid()) {
                notifyNewTopNewsFeedSet();
                fetchFirstNewsImage(TopFeedNewsImageUrlFetchTask.TaskType.INITIALIZE);
            } else {
                showTopNewsFeedUnavailable(newsFeed.getFetchStateMessage(context));
                notifyOnReady(taskType);
            }
        } else {
            TopFeedNewsImageUrlFetchTask.TaskType imageFetchTaskType =
                    taskType.equals(TopNewsFeedFetchTask.TaskType.SWIPE_REFRESH)
                        ? TopFeedNewsImageUrlFetchTask.TaskType.REFRESH
                        : TopFeedNewsImageUrlFetchTask.TaskType.REPLACE;

            if (newsFeed.isValid()) {
                notifyNewTopNewsFeedSet();
                fetchFirstNewsImage(imageFetchTaskType);
            } else {
                showTopNewsFeedUnavailable(newsFeed.getFetchStateMessage(context));
                notifyOnReady(taskType);
            }
        }
    }

    @Override
    public void onTopFeedImageUrlFetch(News news, String url, final int position,
                                       TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        news.setImageUrlChecked(true);

        if (url != null) {
            news.setImageUrl(url);
            applyImage(url, position, taskType);
            NewsDb.getInstance(getContext()).saveTopNewsFeed(mTopNewsFeedPagerAdapter.getNewsFeed());
//            NewsFeedArchiveUtils.saveTopNewsFeed(getContext(), mTopNewsFeedPagerAdapter.getNewsFeed());
        } else {
            mTopNewsFeedPagerAdapter.notifyImageUrlLoaded(position);

            if (position == 0) {
                notifyOnReady(taskType);
                fetchTopNewsFeedImages(taskType);
            }
        }
    }

    @Override
    public void onTopItemClick(MainNewsFeedFragment.ItemViewHolder viewHolder, NewsFeed newsFeed, int position) {
        if (!newsFeed.isValid()) {
            return;
        }

        Intent intent = new Intent(mActivity, NewsFeedDetailActivity.class);
        intent.putExtra(NewsFeed.KEY_NEWS_FEED, newsFeed);
        intent.putExtra(News.KEY_CURRENT_NEWS_INDEX, position);

        // 뉴스 새로 선택시
        intent.putExtra(MainActivity.INTENT_KEY_NEWS_FEED_LOCATION, MainActivity.INTENT_VALUE_TOP_NEWS_FEED);

        Object tintTag = viewHolder.getImageView().getTag();
        TintType tintType = tintTag != null ? (TintType)tintTag : null;
        intent.putExtra(MainActivity.INTENT_KEY_TINT_TYPE, tintType);


        // ActivityOptions를 사용하지 않고 액티비티 트랜지션을 오버라이드해서 직접 애니메이트 하기 위한 변수
        int titleViewPadding =
                getResources().getDimensionPixelSize(R.dimen.main_top_view_pager_title_padding);
        int feedTitlePadding =
                getResources().getDimensionPixelSize(R.dimen.main_top_news_feed_title_padding);

        ActivityTransitionHelper transitionProperty = new ActivityTransitionHelper()
                .addImageView(ActivityTransitionHelper.KEY_IMAGE, viewHolder.getImageView())
                .addTextView(ActivityTransitionHelper.KEY_TEXT, viewHolder.getTitleTextView(),
                        titleViewPadding)
                .addTextView(ActivityTransitionHelper.KEY_SUB_TEXT, mTopNewsFeedTitleTextView,
                        feedTitlePadding);

        intent.putExtra(INTENT_KEY_TRANSITION_PROPERTY, transitionProperty.toGsonString());

        mActivity.startActivityForResult(intent, RC_NEWS_FEED_DETAIL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActivity.overridePendingTransition(0, 0);
        }
    }
}
