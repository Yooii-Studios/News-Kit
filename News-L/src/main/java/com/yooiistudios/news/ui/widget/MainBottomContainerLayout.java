package com.yooiistudios.news.ui.widget;

import android.app.Activity;
import android.app.ActivityOptions;
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
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.antonioleiva.recyclerviewextensions.GridLayoutManager;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedArchiveUtils;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.model.news.TintType;
import com.yooiistudios.news.model.news.task.BottomNewsFeedFetchTask;
import com.yooiistudios.news.model.news.task.BottomNewsImageUrlFetchTask;
import com.yooiistudios.news.ui.activity.MainActivity;
import com.yooiistudios.news.ui.activity.NewsFeedDetailActivity;
import com.yooiistudios.news.ui.adapter.MainBottomAdapter;
import com.yooiistudios.news.ui.animation.AnimationFactory;
import com.yooiistudios.news.ui.itemanimator.SlideInFromBottomItemAnimator;
import com.yooiistudios.news.util.NLLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

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

    private ArrayList<NewsFeed> mBottomNewsFeedList;

    private SparseArray<BottomNewsFeedFetchTask> mBottomNewsFeedIndexToNewsFetchTaskMap;
    private HashMap<News, BottomNewsImageUrlFetchTask> mBottomNewsFeedNewsToImageTaskMap;
    private MainBottomAdapter mBottomNewsFeedAdapter;

    private OnMainBottomLayoutEventListener mOnMainBottomLayoutEventListener;
    private SlideInFromBottomItemAnimator mItemAnimator;
    private Activity mActivity;

    private boolean mIsInitialized = false;

    private boolean mIsRefreshingBottomNewsFeeds = false;

    // interface
    public interface OnMainBottomLayoutEventListener {
        public void onMainBottomInitialLoad();
        public void onMainBottomRefresh();
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
    }

    public void autoRefreshBottomNewsFeeds() {
//        NLLog.now(mBottomNewsFeedRecyclerView.getChildAt(0).getClass().toString());
        for (int i = 0; i < mBottomNewsFeedRecyclerView.getChildCount(); i++) {
            doAutoRefreshBottomNewsFeedAtIndex(i);
        }
    }
    private void doAutoRefreshBottomNewsFeedAtIndex(final int newsFeedIndex) {
        final MainBottomAdapter.BottomNewsFeedViewHolder newsFeedViewHolder =
                new MainBottomAdapter.BottomNewsFeedViewHolder(mBottomNewsFeedRecyclerView.getChildAt(newsFeedIndex));

        AnimationSet hideSet = AnimationFactory.makeBottomHideAnimation();
        hideSet.setAnimationListener(new Animation.AnimationListener() {
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
                        AnimationFactory.makeBottomShowAnimation());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        newsFeedViewHolder.newsTitleTextView.startAnimation(hideSet);
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

    private void fetchBottomNewsFeedListImage() {
        mBottomNewsFeedNewsToImageTaskMap = new
                HashMap<News, BottomNewsImageUrlFetchTask>();

        for (int i = 0; i < mBottomNewsFeedList.size(); i++) {
            NewsFeed feed = mBottomNewsFeedList.get(i);

            ArrayList<News> newsList = feed.getNewsList();
            if (newsList.size() > 0) {
                // IndexOutOfBoundException 방지
                News news = newsList.get(0);

                BottomNewsImageUrlFetchTask task = new BottomNewsImageUrlFetchTask(news, i, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                mBottomNewsFeedNewsToImageTaskMap.put(news, task);
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
                fetchBottomNewsFeedListImage();
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

    @Override
    public void onBottomItemClick(MainBottomAdapter.BottomNewsFeedViewHolder viewHolder,
                                  NewsFeed newsFeed, int position) {
        NLLog.i(TAG, "onBottomItemClick");
        NLLog.i(TAG, "newsFeed : " + newsFeed.getTitle());

        ImageView imageView = viewHolder.imageView;
        TextView titleView = viewHolder.newsTitleTextView;


        ActivityOptions activityOptions =
                ActivityOptions.makeSceneTransitionAnimation(
                        mActivity,
                        new Pair<View, String>(imageView, imageView.getViewName()),
                        new Pair<View, String>(titleView, titleView.getViewName())
                );
//        ActivityOptions activityOptions2 = ActivityOptions.
//                makeSceneTransitionAnimation(NLMainActivity.this,
//                        imageView, imageView.getViewName());

        Intent intent = new Intent(mActivity,
                NewsFeedDetailActivity.class);
        intent.putExtra(NewsFeed.KEY_NEWS_FEED, newsFeed);
        intent.putExtra(News.KEY_CURRENT_NEWS_INDEX, newsFeed.getDisplayingNewsIndex());
        intent.putExtra(MainActivity.INTENT_KEY_VIEW_NAME_IMAGE, imageView.getViewName());
        intent.putExtra(MainActivity.INTENT_KEY_VIEW_NAME_TITLE, titleView.getViewName());

        // 뉴스 새로 선택시
        intent.putExtra(MainActivity.INTENT_KEY_NEWS_FEED_LOCATION,
                MainActivity.INTENT_VALUE_BOTTOM_NEWS_FEED);
        intent.putExtra(MainActivity.INTENT_KEY_BOTTOM_NEWS_FEED_INDEX, position);

        // 미리 이미지뷰에 set해 놓은 태그(TintType)를 인텐트로 보내 적용할 틴트의 종류를 알려줌
        Object tintTag = viewHolder.imageView.getTag();
        TintType tintType = tintTag != null ? (TintType)tintTag : null;
        intent.putExtra(MainActivity.INTENT_KEY_TINT_TYPE, tintType);

        mActivity.startActivityForResult(intent, MainActivity.RC_NEWS_FEED_DETAIL,
                activityOptions.toBundle());
    }

    @Override
    public void onBottomImageUrlFetchSuccess(News news, String url, int position) {
        NLLog.i(TAG, "onBottomImageUrlFetchSuccess");

        news.setImageUrlChecked(true);
        mBottomNewsFeedNewsToImageTaskMap.remove(news);

        if (url != null) {
            news.setImageUrl(url);

            // archive
            NewsFeedArchiveUtils.saveBottomNewsFeedAt(getContext(),
                    mBottomNewsFeedList.get(position), position);


            NLLog.i(TAG, "title : " + news.getTitle() + "'s image url fetch " +
                    "success.\nimage url : " + url);
        }
        if (mBottomNewsFeedAdapter != null && !mItemAnimator.isRunning()) {
            mBottomNewsFeedAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onBottomImageUrlFetchFail(News news, int position) {
        NLLog.i(TAG, "onBottomImageUrlFetchFail");
        news.setImageUrlChecked(true);
        mBottomNewsFeedNewsToImageTaskMap.remove(news);
        if (mBottomNewsFeedAdapter != null && !mItemAnimator.isRunning()) {
            mBottomNewsFeedAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onAnimationsFinished() {
        fetchBottomNewsFeedListImage();
    }
}
