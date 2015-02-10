package com.yooiistudios.news.ui.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.Settings;
import com.yooiistudios.news.model.activitytransition.ActivityTransitionHelper;
import com.yooiistudios.news.model.database.NewsDb;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsImageRequestQueue;
import com.yooiistudios.news.model.news.TintType;
import com.yooiistudios.news.model.news.task.BottomNewsFeedFetchTask;
import com.yooiistudios.news.model.news.task.BottomNewsFeedListFetchManager;
import com.yooiistudios.news.model.news.task.BottomNewsImageFetchManager;
import com.yooiistudios.news.model.news.task.BottomNewsImageFetchTask;
import com.yooiistudios.news.model.news.util.NewsFeedArchiveUtils;
import com.yooiistudios.news.model.panelmatrix.PanelMatrix;
import com.yooiistudios.news.model.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.news.ui.activity.MainActivity;
import com.yooiistudios.news.ui.activity.NewsFeedDetailActivity;
import com.yooiistudios.news.ui.adapter.MainBottomAdapter;
import com.yooiistudios.news.ui.animation.AnimationFactory;
import com.yooiistudios.news.ui.widget.viewpager.SlowSpeedScroller;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.NLLog;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_BOTTOM_NEWS_FEED_INDEX;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_NEWS_FEED_LOCATION;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_TINT_TYPE;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_TRANSITION_PROPERTY;
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
        MainBottomAdapter.OnItemClickListener,
        BottomNewsFeedListFetchManager.OnFetchListener,
        BottomNewsImageFetchManager.OnFetchListener {
    @InjectView(R.id.bottomNewsFeedRecyclerView)    RecyclerView mBottomNewsFeedRecyclerView;

    private static final String TAG = MainBottomContainerLayout.class.getName();
    private static final int COLUMN_COUNT_PORTRAIT = 2;
    private static final int COLUMN_COUNT_LANDSCAPE = 1;

//    private ArrayList<NewsFeed> mBottomNewsFeedList;

    private MainBottomAdapter mBottomNewsFeedAdapter;

    private OnMainBottomLayoutEventListener mOnMainBottomLayoutEventListener;
    private Activity mActivity;
    private ImageLoader mImageLoader;

    private boolean mIsInitialized = false;
    private boolean mIsInitializedFirstImages = false;

    private boolean mIsRefreshingBottomNewsFeeds = false;
    private boolean mIsReplacingBottomNewsFeed = false;
    private boolean mIsFetchingAddedBottomNewsFeeds = false;

    // interface
    public interface OnMainBottomLayoutEventListener {
        public void onMainBottomInitialLoad();
        public void onMainBottomRefresh();
        public void onMainBottomNewsImageInitiallyAllFetched();
        public void onMainBottomNewsReplaceDone();
        public void onMainBottomMatrixChanged();
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
        // 딜레이도 스피드에 따라서 비율적으로 조절해주기
        final int originalRefreshDelay =
                getResources().getInteger(R.integer.bottom_news_feed_auto_refresh_delay_milli);
        final float autoRefreshSpeed = Settings.getAutoRefreshSpeed(getContext());

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
                    }, (long) (idx * (originalRefreshDelay * autoRefreshSpeed)));
                }
            }
        }, SlowSpeedScroller.SWIPE_DURATION); // 탑 스와이프가 진행된 뒤 애니메이션
    }

    private void doAutoRefreshBottomNewsFeedAtIndex(final int newsFeedIndex) {
        NewsFeed newsFeed = mBottomNewsFeedAdapter.getNewsFeedList().get(newsFeedIndex);
        if (newsFeed == null || !newsFeed.containsNews()) {
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
                    BottomNewsImageFetchManager.getInstance().fetchAllNextNewsImageList(
                            mImageLoader, mBottomNewsFeedAdapter.getNewsFeedList(),
                            MainBottomContainerLayout.this,
                            BottomNewsImageFetchTask.TASK_AUTO_REFRESH
                    );
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        newsFeedViewHolder.newsTitleTextView.startAnimation(hideTextSet);
        newsFeedViewHolder.imageView.startAnimation(
                AnimationFactory.makeBottomFadeOutAnimation(getContext()));
    }

    public void init(Activity activity) {
        if (!(activity instanceof MainActivity)) {
            throw new IllegalArgumentException("activity MUST BE an instance of MainActivity");
        }

        mActivity = activity;
        mOnMainBottomLayoutEventListener = (OnMainBottomLayoutEventListener)activity;

        mIsInitialized = false;

        //init ui
        mBottomNewsFeedRecyclerView.setHasFixedSize(true);
//        GridLayoutManager layoutManager = new GridLayoutManager(getContext());
//        layoutManager.setColumns(COLUMN_COUNT_PORTRAIT);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),
                COLUMN_COUNT_PORTRAIT, GridLayoutManager.VERTICAL, false);
//        layoutManager.setOrientation(TwoWayLayoutManager.Orientation.PORTRAIT);
//        layoutManager.setNumColumns(2);
//        layoutManager.setNumRows(2);
        mBottomNewsFeedRecyclerView.setLayoutManager(layoutManager);

        mBottomNewsFeedAdapter = new MainBottomAdapter(getContext(), this);
        mBottomNewsFeedRecyclerView.setAdapter(mBottomNewsFeedAdapter);

        configOnOrientationChange();
        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(getContext());

        ArrayList<NewsFeed> bottomNewsFeedList =
                NewsDb.getInstance(getContext()).loadBottomNewsFeedList(getContext(), currentMatrix.getPanelCount());
        mBottomNewsFeedAdapter.setNewsFeedList(bottomNewsFeedList);
//        mBottomNewsFeedAdapter.setNewsFeedList(NewsFeedArchiveUtils.loadBottomNewsFeedList(getContext()));

        boolean needsRefresh = NewsFeedArchiveUtils.newsNeedsToBeRefreshed(getContext());
        if (needsRefresh) {
            BottomNewsFeedListFetchManager.getInstance().fetchNewsFeedList(
                    mBottomNewsFeedAdapter.getNewsFeedList(), this,
                    BottomNewsFeedFetchTask.TASK_INITIALIZE);
        } else {
            boolean isValid = true;
            ArrayList<Pair<NewsFeed, Integer>> newsFeedListToFetch =
                    new ArrayList<>();
            ArrayList<NewsFeed> list = mBottomNewsFeedAdapter.getNewsFeedList();
            int count = list.size();
            for (int i = 0; i < count; i++) {
                NewsFeed newsFeed = list.get(i);
                if (newsFeed != null && !newsFeed.containsNews()) {
                    isValid = false;
                    newsFeedListToFetch.add(new Pair<>(newsFeed, i));
                }
            }
            if (isValid) {
                notifyOnInitialized();
            } else {
                BottomNewsFeedListFetchManager.getInstance().fetchNewsFeedPairList(
                        newsFeedListToFetch, this,
                        BottomNewsFeedFetchTask.TASK_INITIALIZE);
            }
        }

        adjustSize();
    }

    private void adjustSize() {
        int orientation = getResources().getConfiguration().orientation;
        ViewGroup.LayoutParams recyclerViewLp = mBottomNewsFeedRecyclerView.getLayoutParams();
        Context context = getContext().getApplicationContext();
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 메인 하단의 뉴스피드 RecyclerView의 높이를 set

            recyclerViewLp.height = MainBottomAdapter.measureMaximumHeightOnPortrait(context,
                    mBottomNewsFeedAdapter.getNewsFeedList().size(), COLUMN_COUNT_PORTRAIT);
        } else {
            recyclerViewLp.height = MainBottomAdapter.measureMaximumHeightOnLandscape(context,
                    recyclerViewLp);
        }
        mBottomNewsFeedRecyclerView.setLayoutParams(recyclerViewLp);
    }

    public void notifyPanelMatrixChanged() {
        ArrayList<NewsFeed> currentNewsFeedList = mBottomNewsFeedAdapter.getNewsFeedList();

        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(getContext());

        if (currentNewsFeedList.size() > currentMatrix.getPanelCount()) {
            for (int idx = currentNewsFeedList.size() - 1; idx >= currentMatrix.getPanelCount(); idx--) {
                mBottomNewsFeedAdapter.removeNewsFeedAt(idx);
            }
            mBottomNewsFeedAdapter.notifyDataSetChanged();
        } else if (currentNewsFeedList.size() < currentMatrix.getPanelCount()) {
            ArrayList<NewsFeed> savedNewsFeedList =
                    NewsDb.getInstance(getContext()).loadBottomNewsFeedList(getContext(), currentMatrix.getPanelCount());
//            ArrayList<NewsFeed> savedNewsFeedList =
//                    NewsFeedArchiveUtils.loadBottomNewsFeedList(getContext());
            int maxCount = savedNewsFeedList.size() > currentMatrix.getPanelCount()
                    ? currentMatrix.getPanelCount() : savedNewsFeedList.size();
            ArrayList<Pair<NewsFeed,Integer>> newsFeedToIndexPairListToFetch = new ArrayList<>();
            int currentNewsFeedCount = currentNewsFeedList.size();
            for (int idx = currentNewsFeedCount; idx < maxCount; idx++) {
                NewsFeed newsFeed = savedNewsFeedList.get(idx);
                mBottomNewsFeedAdapter.addNewsFeed(newsFeed);

                if (!newsFeed.containsNews()) {
                    newsFeedToIndexPairListToFetch.add(new Pair<>(newsFeed, idx));
                }
            }

            NewsDb.getInstance(getContext()).saveBottomNewsFeedList(mBottomNewsFeedAdapter.getNewsFeedList());
//            NewsFeedArchiveUtils.saveBottomNewsFeedList(getContext(),
//                    mBottomNewsFeedAdapter.getNewsFeedList());

            mIsFetchingAddedBottomNewsFeeds = true;
            if (newsFeedToIndexPairListToFetch.size() == 0) {
                BottomNewsImageFetchManager.getInstance().fetchDisplayingAndNextImageList(
                        mImageLoader, mBottomNewsFeedAdapter.getNewsFeedList(), this,
                        BottomNewsImageFetchTask.TASK_MATRIX_CHANGED
                );
            } else {
                BottomNewsFeedListFetchManager.getInstance().fetchNewsFeedPairList(
                        newsFeedToIndexPairListToFetch, this,
                        BottomNewsFeedFetchTask.TASK_MATRIX_CHANGED);
            }
        }

        adjustSize();
    }

    private void notifyOnInitialized() {
        mIsInitialized = true;

        mOnMainBottomLayoutEventListener.onMainBottomInitialLoad();

        BottomNewsImageFetchManager.getInstance().fetchAllDisplayingNewsImageList(
                mImageLoader, mBottomNewsFeedAdapter.getNewsFeedList(), this,
                BottomNewsImageFetchTask.TASK_INITIAL_LOAD);
    }

    public boolean isRefreshingBottomNewsFeeds() {
        return mIsRefreshingBottomNewsFeeds;
    }

//    private void fetchNextBottomNewsFeedListImageUrl(int taskType) {
//        fetchNextBottomNewsFeedListImageUrl(taskType, false);
//    }
//
//    private void fetchNextBottomNewsFeedListImageUrl(int taskType,
//                                                     boolean fetchDisplayingNewsImage) {
//        fetchNextBottomNewsFeedListImageUrl(mBottomNewsFeedAdapter.getNewsFeedList(), taskType,
//                fetchDisplayingNewsImage);
//    }

    public void animateBottomNewsFeedListOnInit() {
        mBottomNewsFeedRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mBottomNewsFeedRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                int recyclerViewHeight = mBottomNewsFeedRecyclerView.getHeight();
                int childCount = mBottomNewsFeedRecyclerView.getChildCount();

                for (int i = 0; i < childCount; i++) {
                    View child = mBottomNewsFeedRecyclerView.getChildAt(i);
                    child.setTranslationY((float) (recyclerViewHeight * 1.5));
                    int startDelay = getResources().getInteger(R.integer.bottom_news_feed_init_move_up_anim_delay) * (i+1);
                    int duration = getResources().getInteger(R.integer.bottom_news_feed_init_move_up_anim_duration);
                    child.animate()
                            .translationY(0)
                            .setStartDelay(startDelay)
                            .setDuration(duration)
                            .setInterpolator(
                                    AnimationFactory.makeDefaultReversePathInterpolator())
                            .start();
                }
                return true;
            }
        });
    }

    public void refreshBottomNewsFeeds() {
        mIsRefreshingBottomNewsFeeds = true;

        ArrayList<NewsFeed> newBottomNewsFeedList = new ArrayList<>();
        for (NewsFeed newsFeed : mBottomNewsFeedAdapter.getNewsFeedList()) {
            NewsFeed newNewsFeed = new NewsFeed();
            newNewsFeed.setNewsFeedUrl(newsFeed.getNewsFeedUrl());

            newBottomNewsFeedList.add(newNewsFeed);
        }

        // 프로그레스바를 나타내기 위해 NewsFeedUrl만 가지고 있는 뉴스피드를 넣음
        mBottomNewsFeedAdapter.setNewsFeedList(newBottomNewsFeedList);

        BottomNewsFeedListFetchManager.getInstance().fetchNewsFeedList(
                mBottomNewsFeedAdapter.getNewsFeedList(), this,
                BottomNewsFeedFetchTask.TASK_REFRESH);
    }

    public void reloadNewsFeedAt(int idx) {
        //read from cache
        NewsFeed newsFeed = NewsDb.getInstance(getContext()).loadBottomNewsFeedAt(getContext(), idx, false);
//        NewsFeed newsFeed = NewsFeedArchiveUtils.loadBottomNewsFeedAt(getContext(), idx);

        mBottomNewsFeedAdapter.replaceNewsFeedAt(idx, newsFeed);

        mIsReplacingBottomNewsFeed = true;
        if (newsFeed.containsNews()) {
            BottomNewsImageFetchManager.getInstance().fetchDisplayingAndNextImage(
                    mImageLoader, newsFeed, this, idx, BottomNewsImageFetchTask.TASK_REPLACE
            );
        } else {
            BottomNewsFeedListFetchManager.getInstance().fetchNewsFeed(
                    newsFeed, idx, this, BottomNewsFeedFetchTask.TASK_REPLACE);
        }
    }

    public void configOnNewsImageUrlLoadedAt(String imageUrl, int newsFeedIndex, int newsIndex) {
        News news = mBottomNewsFeedAdapter.getNewsFeedList().get(newsFeedIndex).
                getNewsList().get(newsIndex);

        BottomNewsImageFetchManager.getInstance().notifyOnImageFetchedManually(news, imageUrl,
                newsIndex);

        news.setImageUrl(imageUrl);
        mBottomNewsFeedAdapter.notifyItemChanged(newsFeedIndex);
    }

    public void configOnOrientationChange() {
        int orientation = getResources().getConfiguration().orientation;
        GridLayoutManager layoutManager =
                (GridLayoutManager)mBottomNewsFeedRecyclerView.getLayoutManager();

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager.setSpanCount(COLUMN_COUNT_PORTRAIT);

            mBottomNewsFeedAdapter.setOrientation(MainBottomAdapter.PORTRAIT);
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutManager.setSpanCount(COLUMN_COUNT_LANDSCAPE);

            mBottomNewsFeedAdapter.setOrientation(MainBottomAdapter.LANDSCAPE);
        }
        adjustSize();
        mBottomNewsFeedAdapter.notifyDataSetChanged();

        invalidate();
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public boolean isInitializedFirstImages() {
        return mIsInitializedFirstImages;
    }

    public boolean isReplacingBottomNewsFeed() {
        return mIsReplacingBottomNewsFeed;
    }

    public boolean isFetchingAddedBottomNewsFeeds() {
        return mIsFetchingAddedBottomNewsFeeds;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActivity.overridePendingTransition(0, 0);
        }
    }

    /**
     * 뉴스피드 하나를 fetch한 경우 불리는 콜백
     * @param newsFeed 파싱된 뉴스피드 객체
     * @param index 뉴스피드의 인덱스
     * @param taskType BottomNewsFeedFetchTask 참조
     */
    @Override
    public void onBottomNewsFeedFetch(NewsFeed newsFeed, int index, int taskType) {
        mBottomNewsFeedAdapter.replaceNewsFeedAt(index, newsFeed);
    }

    /**
     * 한 태스크(초기화, 당겨서 새로고침 등)가 모두 끝난 경우 불리는 태스크
     * @param newsFeedPairList 인덱스에 매핑된 뉴스피드 리스트
     * @param taskType BottomNewsFeedFetchTask 참조
     */
    @Override
    public void onBottomNewsFeedListFetchDone(ArrayList<Pair<NewsFeed, Integer>> newsFeedPairList,
                                              int taskType) {
        switch(taskType) {
            case BottomNewsFeedFetchTask.TASK_INITIALIZE:
                mBottomNewsFeedAdapter.notifyDataSetChanged();

                NewsDb.getInstance(getContext()).saveBottomNewsFeedList(mBottomNewsFeedAdapter.getNewsFeedList());
//                NewsFeedArchiveUtils.saveBottomNewsFeedList(getContext(),
//                        mBottomNewsFeedAdapter.getNewsFeedList());
                if (!mIsInitialized) {
                    notifyOnInitialized();
                }
                break;
            case BottomNewsFeedFetchTask.TASK_REFRESH:
                NewsDb.getInstance(getContext()).saveBottomNewsFeedList(mBottomNewsFeedAdapter.getNewsFeedList());
//                NewsFeedArchiveUtils.saveBottomNewsFeedList(getContext(),
//                        mBottomNewsFeedAdapter.getNewsFeedList());

                BottomNewsImageFetchManager.getInstance().fetchDisplayingAndNextImageList(
                        mImageLoader, mBottomNewsFeedAdapter.getNewsFeedList(), this,
                        BottomNewsImageFetchTask.TASK_SWIPE_REFRESH
                );
                break;
            case BottomNewsFeedFetchTask.TASK_REPLACE:
                if (newsFeedPairList.size() == 1) {
                    Pair<NewsFeed, Integer> newsFeedPair = newsFeedPairList.get(0);

                    NewsFeed newsFeed = newsFeedPair.first;
                    NewsDb.getInstance(getContext()).saveBottomNewsFeedAt(newsFeed, newsFeedPair.second);
//                    NewsFeedArchiveUtils.saveBottomNewsFeedAt(getContext(), newsFeed,
//                            newsFeedPair.second);

                    BottomNewsImageFetchManager.getInstance().fetchDisplayingAndNextImage(
                            mImageLoader, newsFeed, this, newsFeedPair.second,
                            BottomNewsImageFetchTask.TASK_REPLACE
                    );
                }
                break;
            case BottomNewsFeedFetchTask.TASK_MATRIX_CHANGED:
//                ArrayList<NewsFeed>
                for (Pair<NewsFeed, Integer> newsFeedToIndexPair : newsFeedPairList) {
                    NewsFeed newsFeed = newsFeedToIndexPair.first;
                    NewsDb.getInstance(getContext()).saveBottomNewsFeedAt(newsFeed, newsFeedToIndexPair.second);
//                    NewsFeedArchiveUtils.saveBottomNewsFeedAt(getContext(), newsFeed,
//                            newsFeedToIndexPair.second);
                }

                BottomNewsImageFetchManager.getInstance().fetchDisplayingAndNextImageList(
                        mImageLoader, mBottomNewsFeedAdapter.getNewsFeedList(), this,
                        BottomNewsImageFetchTask.TASK_MATRIX_CHANGED
                );
                break;
            default:
                break;
        }
    }

    @Override
    public void onBottomNewsImageUrlFetch(News news, String url, int index, int taskType) {
        news.setImageUrlChecked(true);
        if (url != null) {
            news.setImageUrl(url);

            // archive
            NewsFeed newsFeed = mBottomNewsFeedAdapter.getNewsFeedList().get(index);
            NewsDb.getInstance(getContext()).saveBottomNewsFeedAt(newsFeed, index);
//            NewsFeedArchiveUtils.saveBottomNewsFeedAt(getContext(),
//                    mBottomNewsFeedAdapter.getNewsFeedList().get(index), index);
        } else {
            // 이미지 url이 없는 경우. 바로 notify 해서 더미 이미지 보여줌.
            mBottomNewsFeedAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void onBottomNewsImageFetch(int position) {
        mBottomNewsFeedAdapter.notifyItemChanged(position);
    }

    @Override
    public void onBottomNewsImageListFetchDone(int taskType) {
        // 모든 이미지가 불려진 경우
        switch(taskType) {
            case BottomNewsImageFetchTask.TASK_INITIAL_LOAD:
                if (!mIsInitializedFirstImages) {
                    mIsInitializedFirstImages = true;

                    // 콜백 불러주기
                    mOnMainBottomLayoutEventListener.onMainBottomNewsImageInitiallyAllFetched();

                    BottomNewsImageFetchManager.getInstance().fetchAllNextNewsImageList(
                            mImageLoader, mBottomNewsFeedAdapter.getNewsFeedList(), this, taskType
                    );
                }
                break;
            case BottomNewsImageFetchTask.TASK_SWIPE_REFRESH:
                if (mIsRefreshingBottomNewsFeeds) {
                    mIsRefreshingBottomNewsFeeds = false;

                    mOnMainBottomLayoutEventListener.onMainBottomRefresh();

                    BottomNewsImageFetchManager.getInstance().fetchAllNextNewsImageList(
                            mImageLoader, mBottomNewsFeedAdapter.getNewsFeedList(), this,
                            BottomNewsImageFetchTask.TASK_SWIPE_REFRESH
                    );
                }
                break;
            case BottomNewsImageFetchTask.TASK_REPLACE:
                if (mIsReplacingBottomNewsFeed) {
                    mIsReplacingBottomNewsFeed = false;

                    mOnMainBottomLayoutEventListener.onMainBottomNewsReplaceDone();

                    BottomNewsImageFetchManager.getInstance().fetchAllNextNewsImageList(
                            mImageLoader, mBottomNewsFeedAdapter.getNewsFeedList(), this,
                            BottomNewsImageFetchTask.TASK_REPLACE
                    );
                }
                break;
            case BottomNewsImageFetchTask.TASK_MATRIX_CHANGED:
                if (mIsFetchingAddedBottomNewsFeeds) {
                    mIsFetchingAddedBottomNewsFeeds = false;

                    mOnMainBottomLayoutEventListener.onMainBottomMatrixChanged();
                    BottomNewsImageFetchManager.getInstance().fetchAllNextNewsImageList(
                            mImageLoader, mBottomNewsFeedAdapter.getNewsFeedList(), this,
                            BottomNewsImageFetchTask.TASK_MATRIX_CHANGED
                    );
                }
                break;
        }
    }
}
