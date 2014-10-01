package com.yooiistudios.news.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.viewpagerindicator.CirclePageIndicator;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedArchiveUtils;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.model.news.NewsImageRequestQueue;
import com.yooiistudios.news.model.news.task.TopFeedNewsImageUrlFetchTask;
import com.yooiistudios.news.model.news.task.TopNewsFeedFetchTask;
import com.yooiistudios.news.ui.activity.MainActivity;
import com.yooiistudios.news.ui.adapter.MainTopPagerAdapter;
import com.yooiistudios.news.ui.animation.AnimationFactory;
import com.yooiistudios.news.ui.widget.viewpager.MainTopViewPager;
import com.yooiistudios.news.ui.widget.viewpager.SlowSpeedScroller;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.NLLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 19.
 *
 * MainTopViewPager
 *  메인화면 상단 뷰페이저
 */
public class MainTopContainerLayout extends FrameLayout
        implements TopFeedNewsImageUrlFetchTask.OnTopFeedImageUrlFetchListener,
        TopNewsFeedFetchTask.OnFetchListener {
    @InjectView(R.id.main_top_view_pager)                   MainTopViewPager mTopNewsFeedViewPager;
    @InjectView(R.id.main_top_view_pager_wrapper)           FrameLayout mTopNewsFeedViewPagerWrapper;
    @InjectView(R.id.main_top_unavailable_wrapper)          FrameLayout mTopNewsFeedUnavailableWrapper;
    @InjectView(R.id.main_top_page_indicator)               CirclePageIndicator mTopViewPagerIndicator;
    @InjectView(R.id.main_top_news_feed_title_text_view)    TextView mTopNewsFeedTitleTextView;

    private static final String TAG = MainTopContainerLayout.class.getName();

    private TopFeedNewsImageUrlFetchTask mTopImageUrlFetchTask;
    private TopNewsFeedFetchTask mTopNewsFeedFetchTask;
    private HashMap<News, TopFeedNewsImageUrlFetchTask> mTopNewsFeedNewsToImageTaskMap;
    private MainTopPagerAdapter mTopNewsFeedPagerAdapter;

    private OnMainTopLayoutEventListener mOnMainTopLayoutEventListener;

    private NewsFeed mTopNewsFeed;
    private ImageLoader mImageLoader;
    private Activity mActivity;

    // flags for initializing
    private boolean mTopNewsFeedReady = false;
    private boolean mTopNewsFeedFirstImageReady = false;
    private boolean mIsInitialized = false;

    //
    private boolean mIsRefreshingTopNewsFeed = false;

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

        mTopNewsFeedViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.main_top_view_pager_indicator_radius));
    }

    public void autoRefreshTopNewsFeed() {
        if (mTopNewsFeed == null) {
            // 네트워크도 없고 캐시 정보도 없는 경우
            return;
        }
        /*
        if (mTopNewsFeedViewPager.getCurrentItem() + 1 < mTopNewsFeedViewPager.getAdapter().getCount()) {
            mTopNewsFeedViewPager.setCurrentItem(mTopNewsFeedViewPager.getCurrentItem() + 1, true);
        } else {
            mTopNewsFeedViewPager.setCurrentItem(0, true);
        }
        */
    }

    public void init(Activity activity, boolean refresh) {
        if (!(activity instanceof MainActivity)) {
            throw new IllegalArgumentException("activity MUST BE an instance of MainActivity");
        }

        mActivity = activity;
        mOnMainTopLayoutEventListener = (OnMainTopLayoutEventListener)activity;

        Context context = getContext();

        // ViewPager
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            SlowSpeedScroller scroller = new SlowSpeedScroller(context,
                    AnimationFactory.makeViewPagerScrollInterpolator(), true);
            mScroller.set(mTopNewsFeedViewPager, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fetch
        mTopNewsFeed = NewsFeedArchiveUtils.loadTopNewsFeed(context);
        if (refresh) {
            mTopNewsFeedReady = false;
            fetchTopNewsFeed(this);
        } else {
            if (mTopNewsFeed.isValid()) {
                notifyNewTopNewsFeedSet();
                notifyIfInitialized();
            } else {
                mTopNewsFeedReady = false;
                fetchTopNewsFeed(this);
            }
        }

    }

    private void notifyIfInitialized() {
        notifyIfInitialized(false);
    }
    private void notifyIfInitialized(boolean noTopNewsImage) {
        if (mTopNewsFeedReady && (noTopNewsImage || mTopNewsFeedFirstImageReady)) {
            mIsInitialized = true;

            NewsFeedArchiveUtils.saveTopNewsFeed(getContext(), mTopNewsFeed);

            mOnMainTopLayoutEventListener.onMainTopInitialLoad();
        }

    }


    private void fetchTopNewsFeed(TopNewsFeedFetchTask.OnFetchListener listener) {
        fetchTopNewsFeed(listener, true);
    }

    private void fetchTopNewsFeed(TopNewsFeedFetchTask.OnFetchListener listener, boolean shuffle) {
        mTopNewsFeedFetchTask = new TopNewsFeedFetchTask(getContext(), mTopNewsFeed.getNewsFeedUrl(),
                listener, shuffle);
        mTopNewsFeedFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void notifyNewTopNewsFeedSet() {
        // show view pager wrapper
        mTopNewsFeedViewPagerWrapper.setVisibility(View.VISIBLE);
        mTopNewsFeedUnavailableWrapper.setVisibility(View.GONE);
        mTopViewPagerIndicator.setVisibility(View.VISIBLE);

        mTopNewsFeedReady = true;
        ArrayList<News> items = mTopNewsFeed.getNewsList();
        mTopNewsFeedPagerAdapter = new MainTopPagerAdapter(mActivity.getFragmentManager(), mTopNewsFeed);

        mTopNewsFeedViewPager.setAdapter(mTopNewsFeedPagerAdapter);
        mTopViewPagerIndicator.setViewPager(mTopNewsFeedViewPager);
        mTopViewPagerIndicator.setCurrentItem(0);

        if (items.size() > 0) {
            News news = items.get(0);

            if (!news.isImageUrlChecked()) {
                mTopNewsFeedFirstImageReady = false;
                mTopImageUrlFetchTask = new TopFeedNewsImageUrlFetchTask(news, 0, this);
                mTopImageUrlFetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                if (news.getImageUrl() == null) {
                    // no image
                    mTopNewsFeedFirstImageReady = true;
                } else {
                    // 이미지 url은 가져온 상태.
                    applyImage(news.getImageUrl(), 0);
                }
            }
        } else {
            mTopNewsFeedFirstImageReady = true;
        }

        mTopNewsFeedTitleTextView.setText(mTopNewsFeed.getTitle());
    }

    private void showTopNewsFeedUnavailable() {
        // show top unavailable wrapper
        mTopNewsFeedViewPagerWrapper.setVisibility(View.GONE);
        mTopNewsFeedUnavailableWrapper.setVisibility(View.VISIBLE);
        mTopViewPagerIndicator.setVisibility(View.INVISIBLE);

        mTopNewsFeed = null;
        mTopNewsFeedReady = true;

        mTopNewsFeedFirstImageReady = true;
    }

    private void fetchTopNewsFeedImages() {
        if (mTopNewsFeed == null) {
            return;
        }
        mTopNewsFeedNewsToImageTaskMap = new HashMap<News, TopFeedNewsImageUrlFetchTask>();

        ArrayList<News> newsList = mTopNewsFeed.getNewsList();

        for (int i = 0; i < newsList.size(); i++) {
            News news = newsList.get(i);

            if (news.getImageUrl() == null && !news.isImageUrlChecked()) {
                TopFeedNewsImageUrlFetchTask task = new
                        TopFeedNewsImageUrlFetchTask(news, i, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                mTopNewsFeedNewsToImageTaskMap.put(news, task);
            }
        }
    }

    private void cancelTopNewsFeedImageFetchTasks() {
        mTopNewsFeedReady = false;
        for (Map.Entry<News, TopFeedNewsImageUrlFetchTask> entry :
                mTopNewsFeedNewsToImageTaskMap.entrySet()) {
            TopFeedNewsImageUrlFetchTask task = entry.getValue();
            if (task != null) {
                task.cancel(true);
            }
        }
        mTopNewsFeedNewsToImageTaskMap.clear();
    }

    public void refreshNewsFeed() {
        mIsRefreshingTopNewsFeed = true;

        refreshTopNewsFeed(true);
    }

    private void refreshTopNewsFeed(boolean shuffle) {
        NewsFeedUrl topNewsFeedUrl = mTopNewsFeed.getNewsFeedUrl();
        mTopNewsFeed = new NewsFeed();
        mTopNewsFeed.setNewsFeedUrl(topNewsFeedUrl);

        mTopNewsFeed.getNewsList().add(null);
//        for (int i = 0; i < TopNewsFeedFetchTask.FETCH_COUNT; i++) {
//            mTopNewsFeed.getNewsList().add(null);
//        }

        mTopViewPagerIndicator.setCurrentItem(0);
        mTopNewsFeedPagerAdapter = new MainTopPagerAdapter(mActivity.getFragmentManager(), mTopNewsFeed);
        mTopNewsFeedViewPager.setAdapter(mTopNewsFeedPagerAdapter);
        mTopViewPagerIndicator.setViewPager(mTopNewsFeedViewPager);
        mTopNewsFeedTitleTextView.setText("");

        fetchTopNewsFeed(mOnTopNewsFeedRefreshedListener, shuffle);
    }

    private TopNewsFeedFetchTask.OnFetchListener mOnTopNewsFeedRefreshedListener
            = new TopNewsFeedFetchTask.OnFetchListener() {

        @Override
        public void onTopNewsFeedFetchSuccess(NewsFeed newsFeed) {
            mIsRefreshingTopNewsFeed = false;
            mTopNewsFeed = newsFeed;
            notifyNewTopNewsFeedSet();

            configOnRefreshed();
        }

        @Override
        public void onTopNewsFeedFetchFail() {
            mIsRefreshingTopNewsFeed = false;
            showTopNewsFeedUnavailable();

            configOnRefreshed();
        }
    };

    private void configOnRefreshed() {
        NewsFeedArchiveUtils.saveTopNewsFeed(getContext(), mTopNewsFeed);

        mOnMainTopLayoutEventListener.onMainTopRefresh();
    }

    public void configOnNewsFeedReplaced() {
        mTopNewsFeed = NewsFeedArchiveUtils.loadTopNewsFeed(getContext());
        if (mTopNewsFeed.isValid()) {
            notifyNewTopNewsFeedSet();
        } else {
            refreshTopNewsFeed(false);
        }
    }

    public void configOnNewsImageUrlLoadedAt(String imageUrl, int idx) {
        mTopNewsFeedPagerAdapter.getNewsFeed().getNewsList().get(idx).setImageUrl(imageUrl);
        mTopNewsFeedPagerAdapter.notifyImageUrlLoaded(idx);
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public boolean isRefreshingTopNewsFeed() {
        return mIsRefreshingTopNewsFeed;
    }

    @Override
    public void onTopFeedImageUrlFetchSuccess(News news, String url,
                                              final int position) {
        NLLog.i(TAG, "fetch image url success.");
        NLLog.i(TAG, "news link : " + news.getLink());
        NLLog.i(TAG, "image url : " + url);

        news.setImageUrlChecked(true);
        news.setImageUrl(url);

        applyImage(url, position);

        NewsFeedArchiveUtils.saveTopNewsFeed(getContext(), mTopNewsFeed);
    }
    private void applyImage(String url, final int position) {
        mImageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() == null && isImmediate) {
                    return;
                }

                mTopNewsFeedPagerAdapter.notifyImageUrlLoaded(position);

                if (position == 0) {
                    mTopNewsFeedFirstImageReady = true;
                    fetchTopNewsFeedImages();
                    notifyIfInitialized();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (position == 0) {
                    mTopNewsFeedFirstImageReady = true;
                    fetchTopNewsFeedImages();
                    notifyIfInitialized();
                }
            }
        });
    }

    @Override
    public void onTopFeedImageUrlFetchFail(News news, int position) {
        // TODO 여기로 들어올 경우 처리 하자!
        NLLog.i(TAG, "fetch image url failed.");
        news.setImageUrlChecked(true);
        mTopNewsFeedPagerAdapter.notifyImageUrlLoaded(position);

        if (position == 0) {
            mTopNewsFeedFirstImageReady = true;
            fetchTopNewsFeedImages();
            notifyIfInitialized(true);
        }
    }

    /**
     * TopNewsFeedFetch Listener
     */
    @Override
    public void onTopNewsFeedFetchSuccess(NewsFeed newsFeed) {
        NLLog.i(TAG, "onTopNewsFeedFetchSuccess");
//        if (mDialog != null) {
//            mDialog.dismiss();
//        }
        mTopNewsFeed = newsFeed;
        notifyNewTopNewsFeedSet();
//        notifyIfInitialized();
    }

    @Override
    public void onTopNewsFeedFetchFail() {
        NLLog.i(TAG, "onTopNewsFeedFetchFail");
        showTopNewsFeedUnavailable();
        notifyIfInitialized();
    }
}
