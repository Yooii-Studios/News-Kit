package com.yooiistudios.newsflow.ui.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdSize;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.database.NewsDb;
import com.yooiistudios.newsflow.core.news.util.NewsFeedArchiveUtils;
import com.yooiistudios.newsflow.core.news.util.NewsFeedValidator;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.newsflow.core.ui.animation.activitytransition.ActivityTransitionHelper;
import com.yooiistudios.newsflow.core.util.Device;
import com.yooiistudios.newsflow.core.util.Display;
import com.yooiistudios.newsflow.iab.IabProducts;
import com.yooiistudios.newsflow.model.PanelEditMode;
import com.yooiistudios.newsflow.model.cache.NewsImageLoader;
import com.yooiistudios.newsflow.model.news.task.BottomNewsFeedFetchTask;
import com.yooiistudios.newsflow.model.news.task.BottomNewsFeedListFetchManager;
import com.yooiistudios.newsflow.model.news.task.BottomNewsImageFetchManager;
import com.yooiistudios.newsflow.model.news.task.BottomNewsImageFetchTask;
import com.yooiistudios.newsflow.ui.activity.MainActivity;
import com.yooiistudios.newsflow.ui.activity.NewsFeedDetailActivity;
import com.yooiistudios.newsflow.ui.activity.NewsSelectActivity;
import com.yooiistudios.newsflow.ui.activity.StoreActivity;
import com.yooiistudios.newsflow.ui.adapter.MainBottomAdapter;
import com.yooiistudios.newsflow.ui.animation.AnimationFactory;
import com.yooiistudios.newsflow.ui.widget.viewpager.SlowSpeedScroller;
import com.yooiistudios.newsflow.util.OnMainPanelEditModeEventListener;
import com.yooiistudios.serialanimator.animator.SerialAnimator;
import com.yooiistudios.serialanimator.animator.SerialValueAnimator;
import com.yooiistudios.serialanimator.property.ViewProperty;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.yooiistudios.newsflow.ui.activity.MainActivity.INTENT_KEY_BOTTOM_NEWS_FEED_INDEX;
import static com.yooiistudios.newsflow.ui.activity.MainActivity.INTENT_KEY_NEWS_FEED_LOCATION;
import static com.yooiistudios.newsflow.ui.activity.MainActivity.INTENT_KEY_TRANSITION_PROPERTY;
import static com.yooiistudios.newsflow.ui.activity.MainActivity.INTENT_VALUE_BOTTOM_NEWS_FEED;

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
        BottomNewsImageFetchManager.OnFetchListener,
        SerialAnimator.TransitionProperty.TransitionSupplier<ValueAnimator>,
        ViewProperty.AnimationListener,
        MainBottomAdapter.OnBindMainBottomViewHolderListener {

    @InjectView(R.id.bottom_news_feed_recycler_view) RecyclerView mBottomNewsFeedRecyclerView;
    @InjectView(R.id.bottom_news_feed_more_panel_textview) TextView mMorePanelTextView;

    private static final int COLUMN_COUNT_PORTRAIT = 2;
    private static final int COLUMN_COUNT_LANDSCAPE = 1;

    private MainBottomAdapter mAdapter;

    private OnMainBottomLayoutEventListener mOnMainBottomLayoutEventListener;
    private OnMainPanelEditModeEventListener mOnMainPanelEditModeEventListener;
    private MainActivity mActivity;
    private NewsImageLoader mImageLoader;
    private SerialValueAnimator mAutoAnimator;

//    private boolean mIsInitialized = false;
//    private boolean mIsInitializedFirstImages = false;
//
//    private boolean mIsRefreshingBottomNewsFeeds = false;
//    private boolean mIsReplacingBottomNewsFeed = false;
//    private boolean mIsFetchingAddedBottomNewsFeeds = false;

    // interface
    public interface OnMainBottomLayoutEventListener {
        void onMainBottomInitialLoad();
        void onMainBottomRefresh();
        void onMainBottomNewsImageInitiallyAllFetched();
        void onMainBottomNewsReplaceDone();
        void onMainBottomMatrixChanged();
        void onStartNewsFeedDetailActivityFromBottomNewsFeed(Intent intent);
        void onStartNewsFeedSelectActivityFromBottomNewsFeed(Intent intent);
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

        setAnimationCacheEnabled(true);
        setDrawingCacheEnabled(true);
        initAnimator();
    }

    private void initImageLoader() {
        mImageLoader = mActivity.getImageLoader();
    }

    public void init(Activity activity) {
        if (!(activity instanceof MainActivity)) {
            throw new IllegalArgumentException("activity MUST BE an instance of MainActivity");
        }

        mActivity = (MainActivity)activity;
        mOnMainBottomLayoutEventListener = (OnMainBottomLayoutEventListener)activity;
        mOnMainPanelEditModeEventListener = (OnMainPanelEditModeEventListener)activity;

        initImageLoader();
        initUI();
        initAdapter();
        initMorePanelTextView();

        configOnOrientationChange();
        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(getContext());

        ArrayList<NewsFeed> bottomNewsFeedList =
                NewsDb.getInstance(getContext()).loadBottomNewsFeedList(getContext(), currentMatrix.getPanelCount());
        mAdapter.setNewsFeedList(bottomNewsFeedList);

        if (NewsFeedArchiveUtils.newsNeedsToBeRefreshed(getContext())) {
            BottomNewsFeedListFetchManager.getInstance().fetchNewsFeedList(
                    mAdapter.getNewsFeedList(), this,
                    BottomNewsFeedFetchTask.TASK_INITIALIZE);
        } else {
            ArrayList<NewsFeed> newsFeeds = mAdapter.getNewsFeedList();
            if (!NewsFeedValidator.containsNewsFeedToFetch(newsFeeds)) {
                notifyOnInitialized();
            } else {
                ArrayList<Pair<NewsFeed, Integer>> newsFeedListToFetch =
                        NewsFeedValidator.getInvalidNewsFeedsPairs(newsFeeds);
                BottomNewsFeedListFetchManager.getInstance().fetchNewsFeedPairList(
                        newsFeedListToFetch, this,
                        BottomNewsFeedFetchTask.TASK_INITIALIZE);
            }
        }

        adjustSize();
        mAdapter.setOnBindMainBottomViewHolderListener(this);
    }

    private void initAdapter() {
        mAdapter = new MainBottomAdapter(mActivity, mImageLoader, this);
        mBottomNewsFeedRecyclerView.setAdapter(mAdapter);
    }

    private void initUI() {
        //init ui
        mBottomNewsFeedRecyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),
                COLUMN_COUNT_PORTRAIT, GridLayoutManager.VERTICAL, false);
        mBottomNewsFeedRecyclerView.setLayoutManager(layoutManager);
    }

    private void initMorePanelTextView() {
        mMorePanelTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(getContext(), StoreActivity.class));
            }
        });
    }

    private void adjustSize() {
        RelativeLayout.LayoutParams recyclerViewParams =
                (RelativeLayout.LayoutParams) mBottomNewsFeedRecyclerView.getLayoutParams();
        Context context = getContext().getApplicationContext();

        // 우상하단 간격을 딱 맞추기 위함
        int margin = context.getResources().getDimensionPixelSize(
                R.dimen.main_bottom_margin_small);

        if (Device.isPortrait(getContext())) {
            // 메인 하단의 뉴스피드 RecyclerView 의 높이를 set
            recyclerViewParams.height = MainBottomItemLayout.measureParentHeightOnPortrait(context,
                    mAdapter.getNewsFeedList().size(), COLUMN_COUNT_PORTRAIT);
            mBottomNewsFeedRecyclerView.setPadding(0, 0, 0, 0);

            recyclerViewParams.setMargins(margin, margin, margin, margin);
        } else {
            recyclerViewParams.height = Display.getDisplayHeightWithoutStatusBar(context);

            boolean adPurchased = IabProducts.containsSku(context, IabProducts.SKU_NO_ADS);
            if (!adPurchased) {
                int adHeight = AdSize.SMART_BANNER.getHeightInPixels(context);
                mBottomNewsFeedRecyclerView.setPadding(0, margin, 0, adHeight + margin);
            } else {
                mBottomNewsFeedRecyclerView.setPadding(0, margin, 0, margin);
            }

            // 왼쪽, 오른쪽은 기본 마진, 상하단은 마진은 0으로 처리하고 패딩으로 더 늘려줄 것
            recyclerViewParams.setMargins(margin, 0, margin, 0);
        }
        mBottomNewsFeedRecyclerView.setLayoutParams(recyclerViewParams);
    }

    private void adjustMorePanelTextView() {
        if (Device.isPortrait(getContext()) &&
                !IabProducts.containsSku(getContext(), IabProducts.SKU_MORE_PANELS)) {
            mMorePanelTextView.setVisibility(View.VISIBLE);

            // 스마트 배너 높이 만큼으로 높이를 잡아줌
            mMorePanelTextView.getLayoutParams().height =
                    AdSize.SMART_BANNER.getHeightInPixels(getContext());
        } else {
            mMorePanelTextView.setVisibility(View.GONE);
            mMorePanelTextView.getLayoutParams().height = 0;
        }
    }

    private void initAnimator() {
        mAutoAnimator = new SerialValueAnimator();

        SerialValueAnimator.ValueAnimatorProperty transitionProperty
                = new SerialValueAnimator.ValueAnimatorProperty(
                this,
                SlowSpeedScroller.SWIPE_DURATION,
                AnimationFactory.getBottomDuration(getContext()));
        mAutoAnimator.setTransitionProperty(transitionProperty);

        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(getContext());
        mAutoAnimator.applyMockViewProperties(getContext(), this, currentMatrix.getPanelCount());
    }

    public void autoRefreshBottomNewsFeeds() {
        mAutoAnimator.animate();
    }

    public void cancelAutoRefresh() {
        mAutoAnimator.cancelAndResetAllTransitions();
    }

    public boolean isAllNewsFeedsReady() {
        if (mAdapter == null) {
            return false;
        }
        for (NewsFeed newsFeed : mAdapter.getNewsFeedList()) {
            if (newsFeed.isNotFetchedYet()) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllImagesReady() {
        if (mAdapter == null) {
            return false;
        }
        for (NewsFeed newsFeed : mAdapter.getNewsFeedList()) {
            if (newsFeed.isNotFetchedYet()) {
                return false;
            }
            if (!newsFeed.isDisplayable()) {
                continue;
            }
            News news = newsFeed.getDisplayingNews();
            if (news.hasImageUrl() &&
                    news.getImageUrlState() == News.IMAGE_URL_STATE_NO_INFO) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onAnimationEnd(ViewProperty viewProperty) {
        int transitionIndex = viewProperty.getTransitionInfo().index;

        // TODO 인덱스 0에 대한 명세 필요
        if (transitionIndex == 0) {
            int newsFeedIndex = viewProperty.getViewIndex();
            increaseDisplayingNewsIndexAt(newsFeedIndex);
            fetchNextNewsImageAt(newsFeedIndex);
        }
    }

    private void increaseDisplayingNewsIndexAt(int newsFeedIndex) {
        ArrayList<NewsFeed> newsFeeds = mAdapter.getNewsFeedList();
        NewsFeed newsFeed = newsFeeds.get(newsFeedIndex);
        newsFeed.increaseDisplayingNewsIndex();
        mAdapter.notifyItemChanged(newsFeedIndex);
    }

    private void fetchNextNewsImageAt(int newsFeedIndex) {
        ArrayList<NewsFeed> newsFeeds = mAdapter.getNewsFeedList();
        // TODO 뉴스 각각의 애니메이션이 끝난 경우 각자 뉴스 이미지를 가져오도록 변경해야함
        if (newsFeedIndex == newsFeeds.size() - 1) {
            BottomNewsImageFetchManager.getInstance().fetchNextImages(
                    mImageLoader, newsFeeds,
                    MainBottomContainerLayout.this,
                    BottomNewsImageFetchTask.TASK_AUTO_REFRESH
            );
        }
    }

    @NonNull
    @Override
    public List<ValueAnimator> onSupplyTransitionList(View targetView) {
        List<ValueAnimator> animators = new ArrayList<>();
        ValueAnimator fadeOutAnimator =
                AnimationFactory.makeBottomFadeOutAnimator(getContext(), targetView);
        ValueAnimator fadeInAnimator =
                AnimationFactory.makeBottomFadeInAnimator(getContext(), targetView);
        animators.add(fadeOutAnimator);
        animators.add(fadeInAnimator);

        return animators;
    }

    @Override
    public void onBindViewHolder(MainBottomAdapter.BottomNewsFeedViewHolder viewHolder, int i) {
        ViewProperty property =
                new ViewProperty.Builder()
                        .setView(viewHolder.contentWrapper)
                        .setViewIndex(i)
                        .setAnimationListener(this)
                        .build();
        mAutoAnimator.putViewPropertyIfRoom(property, i);
    }

    public void notifyPanelMatrixChanged() {
        ArrayList<NewsFeed> currentNewsFeedList = mAdapter.getNewsFeedList();

        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(getContext());

        if (currentNewsFeedList.size() > currentMatrix.getPanelCount()) {
            configOnPanelCountDecreased();
        } else if (currentNewsFeedList.size() < currentMatrix.getPanelCount()) {
            configOnPanelCountIncreased();
        }

        adjustSize();
    }

    private void configOnPanelCountDecreased() {
        ArrayList<NewsFeed> currentNewsFeedList = mAdapter.getNewsFeedList();
        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(getContext());

        for (int idx = currentNewsFeedList.size() - 1; idx >= currentMatrix.getPanelCount(); idx--) {
            mAdapter.removeNewsFeedAt(idx);
            mAutoAnimator.removeViewPropertyByKey(idx);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void configOnPanelCountIncreased() {
        ArrayList<NewsFeed> currentNewsFeedList = mAdapter.getNewsFeedList();
        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(getContext());

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
            mAdapter.addNewsFeed(newsFeed);

            if (!newsFeed.containsNews()) {
                newsFeedToIndexPairListToFetch.add(new Pair<>(newsFeed, idx));
            }
        }

        NewsDb.getInstance(getContext()).saveBottomNewsFeedList(mAdapter.getNewsFeedList());
//            NewsFeedArchiveUtils.saveBottomNewsFeedList(getContext(),
//                    mAdapter.getNewsFeedList());

        if (newsFeedToIndexPairListToFetch.size() == 0) {
            BottomNewsImageFetchManager.getInstance().fetchDisplayingImages(
                    mImageLoader, mAdapter.getNewsFeedList(), this,
                    BottomNewsImageFetchTask.TASK_MATRIX_CHANGED
            );
        } else {
            BottomNewsFeedListFetchManager.getInstance().fetchNewsFeedPairList(
                    newsFeedToIndexPairListToFetch, this,
                    BottomNewsFeedFetchTask.TASK_MATRIX_CHANGED);
        }
    }

    private void notifyOnInitialized() {
        mOnMainBottomLayoutEventListener.onMainBottomInitialLoad();

        BottomNewsImageFetchManager.getInstance().fetchDisplayingImages(
                mImageLoader, mAdapter.getNewsFeedList(), this,
                BottomNewsImageFetchTask.TASK_INITIAL_LOAD);
    }

//    private void fetchNextBottomNewsFeedListImageUrl(int taskType) {
//        fetchNextBottomNewsFeedListImageUrl(taskType, false);
//    }
//
//    private void fetchNextBottomNewsFeedListImageUrl(int taskType,
//                                                     boolean fetchDisplayingNewsImage) {
//        fetchNextBottomNewsFeedListImageUrl(mAdapter.getNewsFeedList(), taskType,
//                fetchDisplayingNewsImage);
//    }

    public void refreshBottomNewsFeeds() {
        for (NewsFeed newsFeed : mAdapter.getNewsFeedList()) {
            newsFeed.clearFetchedInfo();
        }
        mAdapter.notifyDataSetChanged();

        BottomNewsFeedListFetchManager.getInstance().fetchNewsFeedList(
                mAdapter.getNewsFeedList(), this,
                BottomNewsFeedFetchTask.TASK_REFRESH);
    }

    public void reloadNewsFeedAt(int idx) {
        //read from cache
        NewsFeed newsFeed = NewsDb.getInstance(getContext()).loadBottomNewsFeedAt(getContext(), idx, false);
//        NewsFeed newsFeed = NewsFeedArchiveUtils.loadBottomNewsFeedAt(getContext(), idx);

        mAdapter.replaceNewsFeedAt(idx, newsFeed);

        if (newsFeed.containsNews()) {
            BottomNewsImageFetchManager.getInstance().fetchDisplayingImage(
                    mImageLoader, newsFeed, this, idx, BottomNewsImageFetchTask.TASK_REPLACE
            );
        } else {
            BottomNewsFeedListFetchManager.getInstance().fetchNewsFeed(
                    newsFeed, idx, this, BottomNewsFeedFetchTask.TASK_REPLACE);
        }
    }

    public void configOnNewsImageUrlLoadedAt(String imageUrl, int newsFeedIndex, int newsIndex) {
        News news = mAdapter.getNewsFeedList().get(newsFeedIndex).
                getNewsList().get(newsIndex);

        BottomNewsImageFetchManager.getInstance().notifyOnImageFetchedManually(news, imageUrl,
                newsFeedIndex, newsIndex);

        news.setImageUrl(imageUrl);
        mAdapter.notifyItemChanged(newsFeedIndex);
    }

    public void configOnOrientationChange() {
        GridLayoutManager layoutManager =
                (GridLayoutManager)mBottomNewsFeedRecyclerView.getLayoutManager();

        if (Device.isPortrait(getContext())) {
            layoutManager.setSpanCount(COLUMN_COUNT_PORTRAIT);
            mBottomNewsFeedRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

            mAdapter.setOrientation(MainBottomAdapter.PORTRAIT);
        } else if (Device.isLandscape(getContext())) {
            layoutManager.setSpanCount(COLUMN_COUNT_LANDSCAPE);
            mBottomNewsFeedRecyclerView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);

            mAdapter.setOrientation(MainBottomAdapter.LANDSCAPE);
        }
        adjustSize();
        adjustMorePanelTextView();

        layoutManager.scrollToPositionWithOffset(0, 0);

        mAdapter.notifyDataSetChanged();


        invalidate();
    }

    public boolean isInEditingMode() {
        return mAdapter.isInEditingMode();
    }

    public void showEditLayout() {
        mAdapter.setEditMode(PanelEditMode.EDITING);
        mAdapter.notifyDataSetChanged();
    }

    public void hideEditLayout() {
        mAdapter.setEditMode(PanelEditMode.NONE);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickEditButton(int position) {
        Intent intent = new Intent(mActivity, NewsSelectActivity.class);
        intent = putNewsFeedLocationInfoToIntent(intent, position);
        mOnMainBottomLayoutEventListener.onStartNewsFeedSelectActivityFromBottomNewsFeed(intent);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onClick(MainBottomAdapter.BottomNewsFeedViewHolder viewHolder,
                        NewsFeed newsFeed, int position) {
        Intent intent = makeIntentForNewsFeedDetail(viewHolder, newsFeed, position);
        mOnMainBottomLayoutEventListener.onStartNewsFeedDetailActivityFromBottomNewsFeed(intent);
    }

    @Override
    public void onLongClick() {
        mOnMainPanelEditModeEventListener.onEditModeChange(PanelEditMode.EDITING);
    }

    @Override
    public void onTouchBottomEditLayout() {
        mOnMainPanelEditModeEventListener.onTouchBottomEditLayout();
    }

    private Intent makeIntentForNewsFeedDetail(MainBottomAdapter.BottomNewsFeedViewHolder viewHolder,
                                               NewsFeed newsFeed, int position) {
        Intent intent = new Intent(mActivity, NewsFeedDetailActivity.class);
        intent = putNewsFeedInfoToIntent(intent, newsFeed);
        intent = putNewsFeedLocationInfoToIntent(intent, position);
//        intent = putImageTintTypeToIntent(intent, viewHolder);
        intent = putActivityTransitionInfo(intent, viewHolder);
        return intent;
    }

    private Intent putNewsFeedInfoToIntent(Intent intent, NewsFeed newsFeed) {
        intent.putExtra(NewsFeed.KEY_NEWS_FEED, newsFeed);
        intent.putExtra(News.KEY_CURRENT_NEWS_INDEX, newsFeed.getDisplayingNewsIndex());

        return intent;
    }

    private Intent putNewsFeedLocationInfoToIntent(Intent intent, int position) {
        intent.putExtra(INTENT_KEY_NEWS_FEED_LOCATION,
                INTENT_VALUE_BOTTOM_NEWS_FEED);
        intent.putExtra(INTENT_KEY_BOTTOM_NEWS_FEED_INDEX, position);
        intent.putExtra(NewsFeed.KEY_NEWS_FEED, mAdapter.getNewsFeedList().get(position));

        return intent;
    }

//    // 미리 이미지뷰에 set 해 놓은 태그(TintType)를 인텐트로 보내 적용할 틴트의 종류를 알려줌
//    private Intent putImageTintTypeToIntent(Intent intent, MainBottomAdapter.BottomNewsFeedViewHolder viewHolder) {
//        Object tintTag = viewHolder.imageView.getTag();
//        TintType tintType = tintTag != null ? (TintType)tintTag : null;
//        intent.putExtra(INTENT_KEY_FROM, tintType);
//
//        return intent;
//    }

    private Intent putActivityTransitionInfo(Intent intent,
                                             MainBottomAdapter.BottomNewsFeedViewHolder viewHolder) {
        int titleViewPadding =
                getResources().getDimensionPixelSize(R.dimen.main_bottom_text_padding);
        int feedTitlePadding =
                getResources().getDimensionPixelSize(R.dimen.main_bottom_news_feed_title_padding);

        ActivityTransitionHelper transitionProperty = new ActivityTransitionHelper()
                .addImageView(ActivityTransitionHelper.KEY_IMAGE, viewHolder.imageView)
                .addTextView(ActivityTransitionHelper.KEY_TEXT, viewHolder.newsTitleTextView,
                        titleViewPadding)
                .addTextView(ActivityTransitionHelper.KEY_SUB_TEXT,
                        viewHolder.newsFeedTitleTextView,
                        feedTitlePadding);

        intent.putExtra(INTENT_KEY_TRANSITION_PROPERTY, transitionProperty.toGsonString());

        return intent;
    }

    /**
     * 뉴스피드 하나를 fetch 한 경우 불리는 콜백
     * @param newsFeed 파싱된 뉴스피드 객체
     * @param index 뉴스피드의 인덱스
     * @param taskType BottomNewsFeedFetchTask 참조
     */
    @Override
    public void onBottomNewsFeedFetch(NewsFeed newsFeed, int index, int taskType) {
        mAdapter.replaceNewsFeedAt(index, newsFeed);
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
                NewsFeedArchiveUtils.saveRecentCacheMillisec(getContext().getApplicationContext());
                mAdapter.notifyDataSetChanged();

                NewsDb.getInstance(getContext()).saveBottomNewsFeedList(mAdapter.getNewsFeedList());
                notifyOnInitialized();
                break;
            case BottomNewsFeedFetchTask.TASK_REFRESH:
                NewsFeedArchiveUtils.saveRecentCacheMillisec(getContext().getApplicationContext());
                NewsDb.getInstance(getContext()).saveBottomNewsFeedList(mAdapter.getNewsFeedList());

                BottomNewsImageFetchManager.getInstance().fetchDisplayingImages(
                        mImageLoader, mAdapter.getNewsFeedList(), this,
                        BottomNewsImageFetchTask.TASK_SWIPE_REFRESH
                );
                break;
            case BottomNewsFeedFetchTask.TASK_REPLACE:
                if (newsFeedPairList.size() == 1) {
                    Pair<NewsFeed, Integer> newsFeedPair = newsFeedPairList.get(0);

                    NewsFeed newsFeed = newsFeedPair.first;
                    NewsDb.getInstance(getContext()).saveBottomNewsFeedAt(newsFeed, newsFeedPair.second);

                    BottomNewsImageFetchManager.getInstance().fetchDisplayingImage(
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

                BottomNewsImageFetchManager.getInstance().fetchDisplayingImages(
                        mImageLoader, mAdapter.getNewsFeedList(), this,
                        BottomNewsImageFetchTask.TASK_MATRIX_CHANGED
                );
                break;
            default:
                break;
        }
    }

    @Override
    public void onBottomNewsImageUrlFetch(News news, String url, int newsFeedPosition,
                                          int newsPosition, int taskType, int whichNews) {
        NewsDb.getInstance(getContext()).saveBottomNewsImageUrlWithGuid(url, newsFeedPosition,
                news.getGuid());
        if (url == null && whichNews == News.DISPLAYING_NEWS) {
            // 이미지 url 이 없는 경우. 바로 notify 해서 더미 이미지 보여줌.
            mAdapter.notifyItemChanged(newsFeedPosition);
        }
    }

    @Override
    public void onBottomNewsImageFetch(int newsFeedPosition, int newsPosition, int whichNews) {
        if (whichNews == News.DISPLAYING_NEWS) {
            mAdapter.notifyItemChanged(newsFeedPosition);
        }
    }

    @Override
    public void onBottomNewsImageListFetchDone(int taskType, int whichNews) {
        if (whichNews == News.NEXT_NEWS) {
            return;
        }
        switch(taskType) {
            case BottomNewsImageFetchTask.TASK_INITIAL_LOAD:
                mOnMainBottomLayoutEventListener.onMainBottomNewsImageInitiallyAllFetched();
                break;
            case BottomNewsImageFetchTask.TASK_SWIPE_REFRESH:
                mOnMainBottomLayoutEventListener.onMainBottomRefresh();
                break;
            case BottomNewsImageFetchTask.TASK_REPLACE:
                mOnMainBottomLayoutEventListener.onMainBottomNewsReplaceDone();
                break;
            case BottomNewsImageFetchTask.TASK_MATRIX_CHANGED:
                mOnMainBottomLayoutEventListener.onMainBottomMatrixChanged();
                break;
        }
        BottomNewsImageFetchManager.getInstance().fetchNextImages(
                mImageLoader, mAdapter.getNewsFeedList(), this, taskType
        );
    }
}
