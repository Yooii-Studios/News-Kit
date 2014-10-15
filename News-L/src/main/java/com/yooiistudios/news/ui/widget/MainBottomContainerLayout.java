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
import android.view.ViewTreeObserver;
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
        BottomNewsImageUrlFetchTask.OnBottomImageUrlFetchListener {
    @InjectView(R.id.bottomNewsFeedRecyclerView)    RecyclerView mBottomNewsFeedRecyclerView;

    private static final String TAG = MainBottomContainerLayout.class.getName();
    private static final int BOTTOM_NEWS_FEED_COLUMN_COUNT = 2;

//    private ArrayList<NewsFeed> mBottomNewsFeedList;

    private SparseArray<BottomNewsFeedFetchTask> mBottomNewsFeedIndexToNewsFetchTaskMap;
    private HashMap<News, BottomNewsImageUrlFetchTask> mBottomNewsFeedNewsToImageTaskMap;
    private ArrayList<Pair<News, Boolean>> mNewsToFetchImageList;
    private MainBottomAdapter mBottomNewsFeedAdapter;

    private OnMainBottomLayoutEventListener mOnMainBottomLayoutEventListener;
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

        setAnimationCacheEnabled(true);
        setDrawingCacheEnabled(true);
    }

    public void autoRefreshBottomNewsFeeds() {
        mBottomNewsFeedRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int childCount = mBottomNewsFeedRecyclerView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    final int idx = i;
                    mBottomNewsFeedRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doAutoRefreshBottomNewsFeedAtIndex(idx);
                        }
                    }, idx * getResources().getInteger(R.integer.bottom_news_feed_auto_refresh_delay_milli));
                }
            }
        }, SlowSpeedScroller.SWIPE_DURATION);
    }
    private void doAutoRefreshBottomNewsFeedAtIndex(final int newsFeedIndex) {
        NewsFeed newsFeed = mBottomNewsFeedAdapter.getNewsFeedList().get(newsFeedIndex);
        if (newsFeed == null) {
            return;
        }

        final MainBottomAdapter.BottomNewsFeedViewHolder newsFeedViewHolder =
                new MainBottomAdapter.BottomNewsFeedViewHolder(
                        mBottomNewsFeedRecyclerView.getChildAt(newsFeedIndex));

        Animation hideTextSet = AnimationFactory.makeBottomFadeOutAnimation(getContext());
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
                        AnimationFactory.makeBottomFadeInAnimation(getContext()));
                newsFeedViewHolder.imageView.startAnimation(
                        AnimationFactory.makeBottomFadeInAnimation(getContext()));

                // 모든 애니메이션이 끝난 다음 뉴스 이미지 로드하기 위해 애니메이션들이 다 끝났는지 체크
                if (newsFeedIndex == mBottomNewsFeedRecyclerView.getChildCount() - 1) {
                    fetchNextBottomNewsFeedListImageUrl();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        newsFeedViewHolder.newsTitleTextView.startAnimation(hideTextSet);
        newsFeedViewHolder.imageView.startAnimation(AnimationFactory.makeBottomFadeOutAnimation(getContext()));
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
        GridLayoutManager layoutManager = new GridLayoutManager(context);
        layoutManager.setColumns(BOTTOM_NEWS_FEED_COLUMN_COUNT);
        mBottomNewsFeedRecyclerView.setLayoutManager(layoutManager);

        mBottomNewsFeedAdapter = new MainBottomAdapter(getContext(), this);
        mBottomNewsFeedRecyclerView.setAdapter(mBottomNewsFeedAdapter);

        mBottomNewsFeedAdapter.setNewsFeedList(NewsFeedArchiveUtils.loadBottomNews(context));

        if (refresh) {
            fetchBottomNewsFeedList(BottomNewsFeedFetchTask.TASK_INITIALIZE);
        } else {
            boolean isValid = true;
            ArrayList<Pair<NewsFeed, Integer>> newsFeedListToFetch =
                    new ArrayList<Pair<NewsFeed, Integer>>();
            ArrayList<NewsFeed> list = mBottomNewsFeedAdapter.getNewsFeedList();
            int count = list.size();
            for (int i = 0; i < count; i++) {
                NewsFeed newsFeed = list.get(i);
                if (newsFeed != null && !newsFeed.isValid()) {
                    isValid = false;
                    newsFeedListToFetch.add(new Pair<NewsFeed, Integer>(newsFeed, i));
                }
            }
            if (isValid) {
                notifyOnInitialized();
            } else {
                fetchBottomNewsFeedList(newsFeedListToFetch,
                        BottomNewsFeedFetchTask.TASK_INITIALIZE);
            }
        }

        // 메인 하단의 뉴스피드 RecyclerView의 높이를 set
        ViewGroup.LayoutParams recyclerViewLp = mBottomNewsFeedRecyclerView.getLayoutParams();
        recyclerViewLp.height = MainBottomAdapter.measureMaximumHeight(context,
                mBottomNewsFeedAdapter.getNewsFeedList().size(), BOTTOM_NEWS_FEED_COLUMN_COUNT);
    }

    private void notifyOnInitialized() {
        mIsInitialized = true;

        mOnMainBottomLayoutEventListener.onMainBottomInitialLoad();
    }

    public boolean isRefreshingBottomNewsFeeds() {
        return mIsRefreshingBottomNewsFeeds;
    }

    private void fetchBottomNewsFeedList(int taskType) {
        ArrayList<NewsFeed> newsFeedListToFetch = mBottomNewsFeedAdapter.getNewsFeedList();
        int count = newsFeedListToFetch.size();
        ArrayList<Pair<NewsFeed, Integer>> list = new ArrayList<Pair<NewsFeed, Integer>>();
        for (int i = 0; i < count; i++) {
            list.add(new Pair<NewsFeed, Integer>(newsFeedListToFetch.get(i), i));
        }
        fetchBottomNewsFeedList(list, taskType, true);
    }
    private void fetchBottomNewsFeedList(ArrayList<Pair<NewsFeed, Integer>> newsFeedList,
                                         int taskType) {
        fetchBottomNewsFeedList(newsFeedList, taskType, true);
    }
    private void fetchBottomNewsFeedList(ArrayList<Pair<NewsFeed, Integer>> newsFeedToIndexPairList,
                                         int taskType, boolean shuffle) {
        final int newsFeedCount = newsFeedToIndexPairList.size();

        mBottomNewsFeedIndexToNewsFetchTaskMap = new SparseArray<BottomNewsFeedFetchTask>();
        for (int i = 0; i < newsFeedCount; i++) {
            Pair<NewsFeed, Integer> element = newsFeedToIndexPairList.get(i);
            NewsFeed newsFeed = element.first;
            if (newsFeed == null) {
                continue;
            }

            NewsFeedUrl url = newsFeed.getNewsFeedUrl();
            BottomNewsFeedFetchTask task = new BottomNewsFeedFetchTask(
                    getContext(), url, element.second, taskType, this, shuffle);
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

        int newsFeedCount = mBottomNewsFeedAdapter.getNewsFeedList().size();

        for (int i = 0; i < newsFeedCount; i++) {
            NewsFeed newsFeed = mBottomNewsFeedAdapter.getNewsFeedList().get(i);

            if (newsFeed == null) {
                continue;
            }

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
        int taskCount = mBottomNewsFeedIndexToNewsFetchTaskMap.size();
        for (int i = 0; i < taskCount; i++) {
            mBottomNewsFeedIndexToNewsFetchTaskMap.valueAt(i).cancel(true);
        }
        mBottomNewsFeedIndexToNewsFetchTaskMap.clear();

        cancelBottomNewsImageUrlFetchTask();
    }

    private void cancelBottomNewsImageUrlFetchTask() {
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

    public void animateBottomNewsFeedListOnInit() {
        mBottomNewsFeedRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mBottomNewsFeedRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                int recyclerViewHeight = mBottomNewsFeedRecyclerView.getHeight();
                int childCount = mBottomNewsFeedRecyclerView.getChildCount();

                for (int i = 0; i < childCount; i++) {
                    View child = mBottomNewsFeedRecyclerView.getChildAt(i);

                    child.setTranslationY(recyclerViewHeight);
                    child.animate()
                            .translationY(0)
                            .setStartDelay(getResources().getInteger(R.integer.bottom_news_feed_anim_delay_unit_milli) * i + 1)
                            .setDuration(500)
                            .setInterpolator(AnimationFactory.makeDefaultPathInterpolator())
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    fetchNextBottomNewsFeedListImageUrl(true);
                                }
                            })
                            .start();
                }

                return true;
            }
        });
    }

    public void refreshBottomNewsFeeds() {
        mIsRefreshingBottomNewsFeeds = true;

        ArrayList<NewsFeed> newBottomNewsFeedList = new ArrayList<NewsFeed>();
        for (NewsFeed newsFeed : mBottomNewsFeedAdapter.getNewsFeedList()) {
            NewsFeed newNewsFeed = new NewsFeed();
            newNewsFeed.setNewsFeedUrl(newsFeed.getNewsFeedUrl());

            newBottomNewsFeedList.add(newNewsFeed);
        }

        // 프로그레스바를 나타내기 위해 NewsFeedUrl만 가지고 있는 뉴스피드를 넣음
        mBottomNewsFeedAdapter.setNewsFeedList(newBottomNewsFeedList);

        fetchBottomNewsFeedList(BottomNewsFeedFetchTask.TASK_REFRESHING);
    }

    public void reloadNewsFeedAt(int idx) {
        //read from cache
        NewsFeed newsFeed = NewsFeedArchiveUtils.loadBottomNewsFeedAt(getContext(),
                idx);

        if (newsFeed.isValid()) {
            mBottomNewsFeedAdapter.replaceNewsFeedAt(idx, newsFeed);

            News news = newsFeed.getNewsList().get(newsFeed.getDisplayingNewsIndex());
            if (news.getImageUrl() == null) {
                new BottomNewsImageUrlFetchTask(news, idx, this)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            ArrayList<Pair<NewsFeed, Integer>> list = new ArrayList<Pair<NewsFeed, Integer>>();
            list.add(new Pair<NewsFeed, Integer>(newsFeed, idx));
            fetchBottomNewsFeedList(list, BottomNewsFeedFetchTask.TASK_REPLACING, false);
        }
    }

    public void configOnNewsImageUrlLoadedAt(String imageUrl, int newsFeedIndex, int newsIndex) {
        mBottomNewsFeedAdapter.getNewsFeedList().get(newsFeedIndex).
                getNewsList().get(newsIndex).setImageUrl(imageUrl);
        mBottomNewsFeedAdapter.notifyItemChanged(newsFeedIndex);
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public boolean isInitializedFirstImages() {
        return mIsInitializedFirstImages;
    }

    private void notifyOnNewsImageFetched(News news, int position) {
        mBottomNewsFeedAdapter.notifyItemChanged(position);
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
    public void onBottomNewsFeedFetch(NewsFeed newsFeed, int position, int taskType) {
        NLLog.i(TAG, "onBottomNewsFeedFetch");
        mBottomNewsFeedIndexToNewsFetchTaskMap.remove(position);
        mBottomNewsFeedAdapter.replaceNewsFeedAt(position, newsFeed);

        int remainingTaskCount = mBottomNewsFeedIndexToNewsFetchTaskMap.size();

        switch(taskType) {
            case BottomNewsFeedFetchTask.TASK_INITIALIZE:
                if (remainingTaskCount == 0) {
                    mBottomNewsFeedAdapter.notifyDataSetChanged();

                    NewsFeedArchiveUtils.saveBottomNewsFeedList(getContext(),
                            mBottomNewsFeedAdapter.getNewsFeedList());
                    if (!mIsInitialized) {
                        notifyOnInitialized();
                    }
                }
                break;
            case BottomNewsFeedFetchTask.TASK_REFRESHING:
                if (remainingTaskCount == 0) {
                    mIsRefreshingBottomNewsFeeds = false;

                    NewsFeedArchiveUtils.saveBottomNewsFeedList(getContext(),
                            mBottomNewsFeedAdapter.getNewsFeedList());

                    mOnMainBottomLayoutEventListener.onMainBottomRefresh();

                    fetchNextBottomNewsFeedListImageUrl();
                }
                break;
            case BottomNewsFeedFetchTask.TASK_REPLACING:
                NewsFeedArchiveUtils.saveBottomNewsFeedAt(getContext(), newsFeed, position);
                break;
            default:
                break;
        }

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
        int titleViewPadding =
                getResources().getDimensionPixelSize(R.dimen.main_bottom_text_padding);
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
                mBottomNewsFeedAdapter.getNewsFeedList().get(position), position);


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
}
