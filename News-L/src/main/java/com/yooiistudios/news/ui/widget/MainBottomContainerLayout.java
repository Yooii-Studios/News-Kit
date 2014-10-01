package com.yooiistudios.news.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.antonioleiva.recyclerviewextensions.GridLayoutManager;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.activitytransition.ActivityTransitionHelper;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedArchiveUtils;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.model.news.NewsImageRequestQueue;
import com.yooiistudios.news.model.news.TintType;
import com.yooiistudios.news.model.news.task.BottomNewsFeedFetchTask;
import com.yooiistudios.news.model.news.task.BottomNewsImageUrlFetchTask;
import com.yooiistudios.news.ui.activity.MainActivity;
import com.yooiistudios.news.ui.activity.NewsFeedDetailActivity;
import com.yooiistudios.news.ui.adapter.MainBottomAdapter;
import com.yooiistudios.news.ui.animation.AnimationFactory;
import com.yooiistudios.news.ui.itemanimator.SlideInFromBottomItemAnimator;
import com.yooiistudios.news.ui.widget.viewpager.SlowSpeedScroller;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.NLLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_BOTTOM_NEWS_FEED_INDEX;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_NEWS_FEED_LOCATION;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_TINT_TYPE;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_TRANSITION_PROPERTY;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_VIEW_NAME_IMAGE;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_VIEW_NAME_TITLE;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_VALUE_BOTTOM_NEWS_FEED;
import static com.yooiistudios.news.ui.activity.MainActivity.RC_NEWS_FEED_DETAIL;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 19.
 *
 * MainBottomContainerLayout
 *  메인화면 하단 레이아웃 컨테이너
 */
public class MainBottomContainerLayout extends FrameLayout
        implements
        BottomNewsFeedFetchTask.OnFetchListener,
        MainBottomAdapter.OnItemClickListener,
        BottomNewsImageUrlFetchTask.OnBottomImageUrlFetchListener,
        RecyclerView.ItemAnimator.ItemAnimatorFinishedListener {
    @InjectView(R.id.bottomNewsFeedRecyclerView)    RecyclerView mBottomNewsFeedRecyclerView;

    private static final String TAG = MainBottomContainerLayout.class.getName();
    private static final int BOTTOM_NEWS_FEED_ANIM_DELAY_UNIT_MILLI = 60;
    private static final int BOTTOM_NEWS_FEED_COLUMN_COUNT = 2;

    private static final int BOTTOM_NEWS_FEED_AUTO_REFRESH_ANIM_DELAY_MILLI = 250;
//    private static final int BOTTOM_NEWS_FEED_AUTO_REFRESH_OFFSET = 400; // 30;

    private ArrayList<NewsFeed> mBottomNewsFeedList;

    private SparseArray<BottomNewsFeedFetchTask> mBottomNewsFeedIndexToNewsFetchTaskMap;
    private HashMap<News, BottomNewsImageUrlFetchTask> mBottomNewsFeedNewsToImageTaskMap;
    private ArrayList<Pair<News, Boolean>> mNewsToFetchImageList;
    private MainBottomAdapter mBottomNewsFeedAdapter;
    private ArrayList<Animation> mAutoRefreshAnimationList;

    private OnMainBottomLayoutEventListener mOnMainBottomLayoutEventListener;
    private SlideInFromBottomItemAnimator mItemAnimator;
    private Activity mActivity;
    private ImageLoader mImageLoader;

    private boolean mIsInitialized = false;
    private boolean mIsInitializedFirstImages = false;

    private boolean mIsRefreshingBottomNewsFeeds = false;

    // interface
    public interface OnMainBottomLayoutEventListener {
        public void onMainBottomInitialLoad();
        public void onMainBottomRefresh();
        public void onMainBottomNewsImageInitiallyAllFetched();
    }

    public MainBottomContainerLayout(Context context) {
        super(context);

        _init(context);
    }

    public MainBottomContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        _init(context);
    }

    public MainBottomContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        _init(context);
    }

    public MainBottomContainerLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                     int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        _init(context);
    }


    private void _init(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.main_bottom_container, this,
                false);
        addView(root);

        ButterKnife.inject(this);

        mImageLoader = new ImageLoader(NewsImageRequestQueue.getInstance(context).getRequestQueue(),
                ImageMemoryCache.getInstance(context));
        mAutoRefreshAnimationList = new ArrayList<Animation>();

        setAnimationCacheEnabled(true);
        setDrawingCacheEnabled(true);
    }

    public void autoRefreshBottomNewsFeeds() {
//        NLLog.now(mBottomNewsFeedRecyclerView.getChildAt(0).getClass().toString());

        mAutoRefreshAnimationList.clear();

        mBottomNewsFeedRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mBottomNewsFeedRecyclerView.getChildCount(); i++) {
                    final int idx = i;
                    mBottomNewsFeedRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doAutoRefreshBottomNewsFeedAtIndex(idx);
                        }
                    }, idx * BOTTOM_NEWS_FEED_AUTO_REFRESH_ANIM_DELAY_MILLI);
                }
            }
        }, SlowSpeedScroller.SWIPE_DURATION);
    }
    private void doAutoRefreshBottomNewsFeedAtIndex(final int newsFeedIndex) {
        final MainBottomAdapter.BottomNewsFeedViewHolder newsFeedViewHolder =
                new MainBottomAdapter.BottomNewsFeedViewHolder(
                        mBottomNewsFeedRecyclerView.getChildAt(newsFeedIndex));

        Animation hideTextSet = AnimationFactory.makeBottomFadeOutAnimation();
        hideTextSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 뉴스 갱신
                NewsFeed newsFeed = mBottomNewsFeedAdapter.getNewsFeedList().get(newsFeedIndex);
                if (newsFeed.getDisplayingNewsIndex() < newsFeed.getNewsList().size() - 1) {
                    newsFeed.setDisplayingNewsIndex(newsFeed.getDisplayingNewsIndex() + 1);
                } else {
                    newsFeed.setDisplayingNewsIndex(0);
                }
                mBottomNewsFeedAdapter.notifyItemChanged(newsFeedIndex);

                // 다시 보여주기
                newsFeedViewHolder.newsTitleTextView.startAnimation(
                        AnimationFactory.makeBottomFadeInAnimation());
                newsFeedViewHolder.imageView.startAnimation(
                        AnimationFactory.makeBottomFadeInAnimation());

                // 모든 애니메이션이 끝난 다음 뉴스 이미지 로드하기 위해 애니메이션들이 다 끝났는지 체크
                mAutoRefreshAnimationList.remove(animation);
                checkAutoRefreshAnimationListDone();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        newsFeedViewHolder.newsTitleTextView.startAnimation(hideTextSet);
        mAutoRefreshAnimationList.add(hideTextSet);
        newsFeedViewHolder.imageView.startAnimation(AnimationFactory.makeBottomFadeOutAnimation());
    }
    private void checkAutoRefreshAnimationListDone() {
        if (mAutoRefreshAnimationList.size() == 0) {
            fetchNextBottomNewsFeedListImageUrl();
        }
    }

    public void init(Activity activity, boolean refresh) {

        if (!(activity instanceof MainActivity)) {
            throw new IllegalArgumentException("activity MUST BE an instance of MainActivity");
        }

        mActivity = activity;
        mOnMainBottomLayoutEventListener = (OnMainBottomLayoutEventListener)activity;

        mIsInitialized = false;

        Context context = getContext();
        //init ui
        mBottomNewsFeedRecyclerView.setHasFixedSize(true);
//        ((ViewGroup)mBottomNewsFeedRecyclerView).setTransitionGroup(false);
        mItemAnimator = new SlideInFromBottomItemAnimator(
                mBottomNewsFeedRecyclerView);
        mBottomNewsFeedRecyclerView.setItemAnimator(mItemAnimator);
        GridLayoutManager layoutManager = new GridLayoutManager(context);
        layoutManager.setColumns(BOTTOM_NEWS_FEED_COLUMN_COUNT);
        mBottomNewsFeedRecyclerView.setLayoutManager(layoutManager);

        mBottomNewsFeedList = NewsFeedArchiveUtils.loadBottomNews(context);
//        mBottomNewsFeedAdapter.resetDisplayingNewsFeedIndices();

        if (refresh) {
            fetchBottomNewsFeedList(this);
        } else {
            boolean isValid = true;
            for (NewsFeed newsFeed : mBottomNewsFeedList) {
                if (!newsFeed.isValid()) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                mIsInitialized = true;
                notifyOnInitialized();
            } else {
                fetchBottomNewsFeedList(this);
            }
        }

        // 메인 하단의 뉴스피드 RecyclerView의 높이를 set
        ViewGroup.LayoutParams recyclerViewLp = mBottomNewsFeedRecyclerView.getLayoutParams();
        recyclerViewLp.height = MainBottomAdapter.measureMaximumHeight(context,
                mBottomNewsFeedList.size(), BOTTOM_NEWS_FEED_COLUMN_COUNT);
    }

    private void notifyOnInitialized() {
        NewsFeedArchiveUtils.saveBottomNewsFeedList(getContext(), mBottomNewsFeedList);
        animateBottomNewsFeedListOnInit();

        mOnMainBottomLayoutEventListener.onMainBottomInitialLoad();
    }

    public boolean isRefreshingBottomNewsFeeds() {
        return mIsRefreshingBottomNewsFeeds;
    }

    private void fetchBottomNewsFeedList(BottomNewsFeedFetchTask.OnFetchListener listener) {
        final int bottomNewsCount = mBottomNewsFeedList.size();

        mBottomNewsFeedIndexToNewsFetchTaskMap = new SparseArray<BottomNewsFeedFetchTask>();
        for (int i = 0; i < bottomNewsCount; i++) {
            NewsFeedUrl url = mBottomNewsFeedList.get(i).getNewsFeedUrl();
            BottomNewsFeedFetchTask task = new BottomNewsFeedFetchTask(
                    getContext(), url, i, listener
            );
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mBottomNewsFeedIndexToNewsFetchTaskMap.put(i, task);
        }
    }

    private void fetchNextBottomNewsFeedListImageUrl() {
        fetchNextBottomNewsFeedListImageUrl(false);
    }
    private void fetchNextBottomNewsFeedListImageUrl(boolean fetchDisplayingNewsImage) {
        mBottomNewsFeedNewsToImageTaskMap = new HashMap<News, BottomNewsImageUrlFetchTask>();
        mNewsToFetchImageList = new ArrayList<Pair<News, Boolean>>();

        int newsFeedCount = mBottomNewsFeedList.size();

        for (int i = 0; i < newsFeedCount; i++) {
            NewsFeed newsFeed = mBottomNewsFeedList.get(i);

            ArrayList<News> newsList = newsFeed.getNewsList();

            int indexToFetch;
            if (fetchDisplayingNewsImage) {
                indexToFetch = newsFeed.getDisplayingNewsIndex();
            } else {
                indexToFetch = newsFeed.getDisplayingNewsIndex();
                if (indexToFetch < newsFeed.getNewsList().size() - 1) {
                    indexToFetch += 1;
                } else {
                    indexToFetch = 0;
                }
            }

//            NLLog.i("indexToFetch", i + "th feed : " + indexToFetch + "th news.");

            News news = newsList.get(indexToFetch);

            mNewsToFetchImageList.add(new Pair<News, Boolean>(news, false));
        }

        for (int i = 0; i < mNewsToFetchImageList.size(); i++) {
            News news = mNewsToFetchImageList.get(i).first;
            if (!news.isImageUrlChecked()) {
                BottomNewsImageUrlFetchTask task = new BottomNewsImageUrlFetchTask(news, i, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                mBottomNewsFeedNewsToImageTaskMap.put(news, task);
            } else {
                if (news.getImageUrl() == null) {
                    notifyOnNewsImageFetched(news, i);
                } else {
                    applyImage(news, i);
                }
            }
        }
    }

    private void cancelBottomNewsFetchTasks() {
        mIsInitialized = false;
        int taskCount = mBottomNewsFeedIndexToNewsFetchTaskMap.size();
        for (int i = 0; i < taskCount; i++) {
            BottomNewsFeedFetchTask task = mBottomNewsFeedIndexToNewsFetchTaskMap
                    .get(i, null);
            if (task != null) {
                task.cancel(true);
            }
        }
        mBottomNewsFeedIndexToNewsFetchTaskMap.clear();

        for (Map.Entry<News, BottomNewsImageUrlFetchTask> entry :
                mBottomNewsFeedNewsToImageTaskMap.entrySet()) {
            BottomNewsImageUrlFetchTask task = entry.getValue();
            if (task != null) {
                task.cancel(true);
            }
        }
        mBottomNewsFeedNewsToImageTaskMap.clear();
        mNewsToFetchImageList.clear();
    }

    private void animateBottomNewsFeedListOnInit() {
        mIsInitialized = true;
        mBottomNewsFeedAdapter = new MainBottomAdapter(getContext(), this);
        mBottomNewsFeedRecyclerView.setAdapter(mBottomNewsFeedAdapter);

        for (int i = 0; i < mBottomNewsFeedList.size(); i++) {
            final NewsFeed newsFeed = mBottomNewsFeedList.get(i);
            final int idx = i;
            mBottomNewsFeedRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBottomNewsFeedAdapter.addNewsFeed(newsFeed);

                    if (idx == (mBottomNewsFeedList.size() - 1)) {
                        mItemAnimator.isRunning(MainBottomContainerLayout.this);
                    }
                }
            }, BOTTOM_NEWS_FEED_ANIM_DELAY_UNIT_MILLI * i + 1);

        }
    }

    public void refreshBottomNewsFeeds() {
        mIsRefreshingBottomNewsFeeds = true;

        ArrayList<NewsFeed> newBottomNewsFeedList = new ArrayList<NewsFeed>();
        for (NewsFeed newsFeed : mBottomNewsFeedList) {
            NewsFeed newNewsFeed = new NewsFeed();
            newNewsFeed.setNewsFeedUrl(newsFeed.getNewsFeedUrl());

            newBottomNewsFeedList.add(newNewsFeed);
        }
        mBottomNewsFeedList = newBottomNewsFeedList;

        // 프로그레스바를 나타내기 위해 NewsFeedUrl만 가지고 있는 뉴스피드를 넣음
        mBottomNewsFeedAdapter.setNewsFeedList(mBottomNewsFeedList);

        fetchBottomNewsFeedList(mOnBottomNewsFeedListRefreshedListener);
    }

    private BottomNewsFeedFetchTask.OnFetchListener mOnBottomNewsFeedListRefreshedListener
            = new BottomNewsFeedFetchTask.OnFetchListener() {

        @Override
        public void onBottomNewsFeedFetchSuccess(int position, NewsFeed newsFeed) {
//            mBottomNewsFeedAdapter.replaceNewsFeedAt(position, newsFeed);
            mBottomNewsFeedList.set(position, newsFeed);
            mBottomNewsFeedIndexToNewsFetchTaskMap.remove(position);

            checkAllBottomNewsFeedFetched();
        }

        @Override
        public void onBottomNewsFeedFetchFail(int position) {
            mBottomNewsFeedIndexToNewsFetchTaskMap.remove(position);

            checkAllBottomNewsFeedFetched();
            // TODO initialize 리스너 참조.
        }

        private void checkAllBottomNewsFeedFetched() {
            int remainingTaskCount = mBottomNewsFeedIndexToNewsFetchTaskMap.size();

            if (remainingTaskCount == 0) {
                mIsRefreshingBottomNewsFeeds = false;

                configOnRefreshed();

                mBottomNewsFeedAdapter.setNewsFeedList(mBottomNewsFeedList);
                fetchNextBottomNewsFeedListImageUrl();
            }
        }
    };

    private void configOnRefreshed() {
        NewsFeedArchiveUtils.saveBottomNewsFeedList(getContext(), mBottomNewsFeedList);

        mOnMainBottomLayoutEventListener.onMainBottomRefresh();
    }

    public void configOnNewsFeedReplacedAt(int idx) {
        //read from cache
        NewsFeed newsFeed = NewsFeedArchiveUtils.loadBottomNewsFeedAt(getContext(),
                idx);

        if (newsFeed.isValid()) {
            mBottomNewsFeedAdapter.replaceNewsFeedAt(idx, newsFeed);

            News news = newsFeed.getNewsList().get(0);
            if (news.getImageUrl() == null) {
                new BottomNewsImageUrlFetchTask(news, idx, this)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            BottomNewsFeedFetchTask task = new BottomNewsFeedFetchTask(
                    getContext(), newsFeed.getNewsFeedUrl(), idx,
                    mOnBottomNewsFeedFetchListener, false);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void configOnNewsImageUrlLoadedAt(String imageUrl, int newsFeedIndex, int newsIndex) {
        mBottomNewsFeedAdapter.getNewsFeedList().get(newsFeedIndex).
                getNewsList().get(newsIndex).setImageUrl(imageUrl);
        mBottomNewsFeedAdapter.notifyItemChanged(newsFeedIndex);
    }

    private BottomNewsFeedFetchTask.OnFetchListener mOnBottomNewsFeedFetchListener
            = new BottomNewsFeedFetchTask.OnFetchListener() {

        @Override
        public void onBottomNewsFeedFetchSuccess(int position, NewsFeed newsFeed) {
            mBottomNewsFeedAdapter.replaceNewsFeedAt(position, newsFeed);
        }

        @Override
        public void onBottomNewsFeedFetchFail(int position) {

        }
    };

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public boolean isInitializedFirstImages() {
        return mIsInitializedFirstImages;
    }

    private void notifyOnNewsImageFetched(News news, int position) {
        if (mBottomNewsFeedAdapter != null && !mItemAnimator.isRunning()) {
            mBottomNewsFeedAdapter.notifyItemChanged(position);
        }
        for (int i = 0; i < mNewsToFetchImageList.size(); i++) {
            Pair<News, Boolean> pair = mNewsToFetchImageList.get(i);
            if (pair.first == news) {
                mNewsToFetchImageList.set(i, new Pair<News, Boolean>(news, true));
                break;
            }
        }

        boolean allFetched = true;
        for (Pair<News, Boolean> pair : mNewsToFetchImageList) {
            if (!pair.second) {
                allFetched = false;
                break;
            }
        }

        if (allFetched) {
            // 모든 이미지가 불려진 경우
            if (!mIsInitializedFirstImages) {
                mIsInitializedFirstImages = true;

                // 콜백 불러주기
                mOnMainBottomLayoutEventListener.onMainBottomNewsImageInitiallyAllFetched();

                fetchNextBottomNewsFeedListImageUrl();
            }
        }
    }

    @Override
    public void onBottomNewsFeedFetchSuccess(int position, NewsFeed newsFeed) {
        NLLog.i(TAG, "onBottomNewsFeedFetchSuccess");
        mBottomNewsFeedIndexToNewsFetchTaskMap.remove(position);
        mBottomNewsFeedList.set(position, newsFeed);

        int remainingTaskCount = mBottomNewsFeedIndexToNewsFetchTaskMap.size();
        if (remainingTaskCount == 0) {
            NLLog.i(TAG, "All task done. Loaded news feed list size : " +
                    mBottomNewsFeedList.size());
            mIsInitialized = true;

            notifyOnInitialized();
        } else {
            NLLog.i(TAG, remainingTaskCount + " remaining tasks.");
        }
    }

    @Override
    public void onBottomNewsFeedFetchFail(int position) {
        NLLog.i(TAG, "onBottomNewsFeedFetchFail");
        // TODO Top news처럼 뉴스 없음 처리하고 notify 해줘야 함
        mIsInitialized = true;

        notifyOnInitialized();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBottomItemClick(MainBottomAdapter.BottomNewsFeedViewHolder viewHolder,
                                  NewsFeed newsFeed, int position) {
        NLLog.i(TAG, "onBottomItemClick");
        NLLog.i(TAG, "newsFeed : " + newsFeed.getTitle());

        ImageView imageView = viewHolder.imageView;
        TextView newsTitleTextView = viewHolder.newsTitleTextView;
        TextView newsFeedTitleTextView = viewHolder.newsFeedTitleTextView;

        Intent intent = new Intent(mActivity,
                NewsFeedDetailActivity.class);
        intent.putExtra(NewsFeed.KEY_NEWS_FEED, newsFeed);
        intent.putExtra(News.KEY_CURRENT_NEWS_INDEX, newsFeed.getDisplayingNewsIndex());
        intent.putExtra(INTENT_KEY_VIEW_NAME_IMAGE, imageView.getViewName());
        intent.putExtra(INTENT_KEY_VIEW_NAME_TITLE, newsTitleTextView.getViewName());

        // 뉴스 새로 선택시
        intent.putExtra(INTENT_KEY_NEWS_FEED_LOCATION,
                INTENT_VALUE_BOTTOM_NEWS_FEED);
        intent.putExtra(INTENT_KEY_BOTTOM_NEWS_FEED_INDEX, position);

        // 미리 이미지뷰에 set해 놓은 태그(TintType)를 인텐트로 보내 적용할 틴트의 종류를 알려줌
        Object tintTag = viewHolder.imageView.getTag();
        TintType tintType = tintTag != null ? (TintType)tintTag : null;
        intent.putExtra(INTENT_KEY_TINT_TYPE, tintType);

        // ActivityOptions를 사용하지 않고 액티비티 트랜지션을 오버라이드해서 직접 애니메이트 하기 위한 변수
        int titleViewPadding = getResources().getDimensionPixelSize(R.dimen.main_bottom_text_padding);
        int feedTitlePadding =
                getResources().getDimensionPixelSize(R.dimen.main_bottom_news_feed_title_padding);

        ActivityTransitionHelper transitionProperty = new ActivityTransitionHelper()
                .addImageView(ActivityTransitionHelper.KEY_IMAGE, imageView)
                .addTextView(ActivityTransitionHelper.KEY_TEXT, newsTitleTextView,
                        titleViewPadding)
                .addTextView(ActivityTransitionHelper.KEY_SUB_TEXT, newsFeedTitleTextView,
                        feedTitlePadding);

        intent.putExtra(INTENT_KEY_TRANSITION_PROPERTY, transitionProperty.toGsonString());

        mActivity.startActivityForResult(intent, RC_NEWS_FEED_DETAIL);

        mActivity.overridePendingTransition(0, 0);
    }

    @Override
    public void onBottomImageUrlFetchSuccess(final News news, String url, final int position) {
        NLLog.i(TAG, "onBottomImageUrlFetchSuccess");

        news.setImageUrlChecked(true);
        mBottomNewsFeedNewsToImageTaskMap.remove(news);

        news.setImageUrl(url);

        // archive
        NewsFeedArchiveUtils.saveBottomNewsFeedAt(getContext(),
                mBottomNewsFeedList.get(position), position);


        NLLog.i(TAG, "title : " + news.getTitle() + "'s image url fetch " +
                "success.\nimage url : " + url);
        applyImage(news, position);
    }

    private void applyImage(final News news, final int position) {
        mImageLoader.get(news.getImageUrl(), new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() == null && isImmediate) {
                    return;
                }
                notifyOnNewsImageFetched(news, position);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                notifyOnNewsImageFetched(news, position);
            }
        });
    }

    @Override
    public void onBottomImageUrlFetchFail(News news, int position) {
        NLLog.i(TAG, "onBottomImageUrlFetchFail");
        news.setImageUrlChecked(true);
        mBottomNewsFeedNewsToImageTaskMap.remove(news);

        notifyOnNewsImageFetched(news, position);
    }

    @Override
    public void onAnimationsFinished() {
        fetchNextBottomNewsFeedListImageUrl(true);
    }
}
