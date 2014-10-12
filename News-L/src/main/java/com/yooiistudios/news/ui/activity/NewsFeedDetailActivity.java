package com.yooiistudios.news.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.AlphaForegroundColorSpan;
import com.yooiistudios.news.model.activitytransition.ActivityTransitionHelper;
import com.yooiistudios.news.model.activitytransition.ActivityTransitionImageViewProperty;
import com.yooiistudios.news.model.activitytransition.ActivityTransitionProperty;
import com.yooiistudios.news.model.activitytransition.ActivityTransitionTextViewProperty;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedArchiveUtils;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.model.news.NewsFeedUtils;
import com.yooiistudios.news.model.news.NewsImageRequestQueue;
import com.yooiistudios.news.model.news.TintType;
import com.yooiistudios.news.model.news.task.NewsFeedDetailNewsFeedFetchTask;
import com.yooiistudios.news.model.news.task.NewsFeedDetailNewsImageUrlFetchTask;
import com.yooiistudios.news.ui.adapter.NewsFeedDetailAdapter;
import com.yooiistudios.news.ui.animation.AnimationFactory;
import com.yooiistudios.news.ui.animation.curvemotion.AnimatorPath;
import com.yooiistudios.news.ui.animation.curvemotion.PathEvaluator;
import com.yooiistudios.news.ui.animation.curvemotion.PathPoint;
import com.yooiistudios.news.ui.fragment.NewsSelectFragment;
import com.yooiistudios.news.ui.itemanimator.DetailNewsItemAnimator;
import com.yooiistudios.news.ui.widget.ObservableScrollView;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.NLLog;
import com.yooiistudios.news.util.ScreenUtils;

import java.lang.reflect.Type;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_TRANSITION_PROPERTY;

public class NewsFeedDetailActivity extends Activity
        implements NewsFeedDetailAdapter.OnItemClickListener, ObservableScrollView.Callbacks,
        NewsFeedDetailNewsFeedFetchTask.OnFetchListener,
        NewsFeedDetailNewsImageUrlFetchTask.OnImageUrlFetchListener {
    @InjectView(R.id.detail_content_layout)                 FrameLayout mRootLayout;
    @InjectView(R.id.detail_transition_content_layout)      FrameLayout mTransitionLayout;
    @InjectView(R.id.detail_actionbar_overlay_view)         View mActionBarOverlayView;
    @InjectView(R.id.detail_top_overlay_view)               View mTopOverlayView;
    @InjectView(R.id.detail_scrollView)                     ObservableScrollView mScrollView;
    @InjectView(R.id.news_detail_swipe_refresh_layout)      SwipeRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.detail_loading_cover)                  View mLoadingCoverView;

    // Top
    @InjectView(R.id.detail_top_news_image_wrapper)         FrameLayout mTopNewsImageWrapper;
    @InjectView(R.id.detail_top_news_image_ripple_view)     View mTopNewsImageRippleView;
    @InjectView(R.id.detail_top_news_image_view)            ImageView mTopImageView;
    @InjectView(R.id.detail_top_news_text_layout)           LinearLayout mTopNewsTextLayout;
    @InjectView(R.id.detail_top_news_text_ripple_layout)    LinearLayout mTopNewsTextRippleLayout;
    @InjectView(R.id.detail_top_news_title_text_view)       TextView mTopTitleTextView;
    @InjectView(R.id.detail_top_news_description_text_view) TextView mTopDescriptionTextView;
    // Bottom
    @InjectView(R.id.detail_bottom_news_recycler_view)      RecyclerView mBottomNewsListRecyclerView;

    private static final int BOTTOM_NEWS_ANIM_DELAY_UNIT_MILLI = 60;
    private static final int ACTIVITY_ENTER_ANIMATION_DURATION = 600;
    private static final String TAG = NewsFeedDetailActivity.class.getName();
    public static final String INTENT_KEY_NEWS = "INTENT_KEY_NEWS";
    public static final String INTENT_KEY_NEWSFEED_REPLACED = "INTENT_KEY_NEWSFEED_REPLACED";
    public static final String INTENT_KEY_IMAGE_LOADED = "INTENT_KEY_IMAGE_LOADED";
    public static final String INTENT_KEY_IMAGE_URL = "INTENT_KEY_IMAGE_URL";

    public static final int REQ_SELECT_NEWS_FEED = 13841;

    private static final int BACKGROUND_COLOR = Color.WHITE;

    private Palette mPalette;

    private ImageLoader mImageLoader;

    private NewsFeed mNewsFeed;
    private News mTopNews;
    private Bitmap mTopImageBitmap;
    private NewsFeedDetailAdapter mAdapter;
    private TintType mTintType;
    private ColorDrawable mRootLayoutBackground;
    private ColorDrawable mRecyclerViewBackground;
    private BitmapDrawable mActionBarHomeIcon;
    private BitmapDrawable mActionBarOverflowIcon;
    private SpannableString mActionBarTitle;
    private AlphaForegroundColorSpan mColorSpan;
    private int mActionTextColor;

    // 액티비티 전환 애니메이션 관련 변수
    private ActivityTransitionImageViewProperty mTransImageViewProperty;
    private ActivityTransitionTextViewProperty mTransTitleViewProperty;
    private ActivityTransitionTextViewProperty mTransFeedTitleViewProperty;

    private Rect mRootClipBound;

    private int mThumbnailLeftDelta;
    private int mThumbnailTopDelta;
    private int mThumbnailLeftTarget;
    private int mThumbnailTopTarget;
    private float mThumbnailScaleRatio;

    private TextView mNewsTitleThumbnailTextView;
    private TextView mNewsFeedTitleThumbnailTextView;

    // 액티비티 트랜지션 속도 관련 변수
    public static int sAnimatorScale = 1;
    private long mExitAnimationDuration = 100;
    private long mImageFilterAnimationDuration;
    private long mImageScaleAnimationDuration;
    private long mRootViewHorizontalScaleAnimationDuration;
    private long mRootViewVerticalScaleAnimationDuration;
    private long mRootViewTranslationAnimationDuration;
    private long mThumbnailTextAnimationDuration;
    private long mActionBarIconAnimationDuration;
    private long mActionBarBgAnimationDuration;

    private int mWindowInsetEnd;

    private boolean mIsRefreshing = false;
    private boolean mHasAnimatedColorFilter = false;
    private boolean mIsAnimatingActivityTransitionAnimation = false;

    private boolean mHasNewsFeedReplaced = false;
    private boolean mIsLoadingImageOnInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
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
        String imageViewName = getIntent().getExtras().getString(MainActivity
                .INTENT_KEY_VIEW_NAME_IMAGE, null);

        // set view name to animate
        mTopImageView.setViewName(imageViewName);

        applySystemWindowsBottomInset(R.id.detail_scroll_content_wrapper);
        initRootLayout();
        initActionBar();
        initSwipeRefreshView();
        initCustomScrollView();
        initTopNews();
        initBottomNewsList();
        initLoadingCoverView();

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (savedInstanceState == null && mTopImageView.getDrawable() != null) {
            ViewTreeObserver observer = mRootLayout.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    mRootLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                    initEnterExitAnimationVariable();

                    mNewsTitleThumbnailTextView = new TextView(NewsFeedDetailActivity.this);
                    mNewsFeedTitleThumbnailTextView = new TextView(NewsFeedDetailActivity.this);

//                    View view = new View(NewsFeedDetailActivity.this);
//                    view.setBackgroundColor(Color.BLACK);
//                    addThumbnailView(view, mTransImageViewProperty);
                    addThumbnailTextView(mNewsTitleThumbnailTextView, mTransTitleViewProperty);
                    addThumbnailTextView(mNewsFeedTitleThumbnailTextView,
                            mTransFeedTitleViewProperty);

                    runEnterAnimation();

                    return true;
                }
            });
        } else {
            showLoadingCover();
        }
    }

    private void initRootLayout() {
        mRootLayoutBackground = new ColorDrawable(BACKGROUND_COLOR);
        mRootLayout.setBackground(mRootLayoutBackground);
    }

    private void initEnterExitAnimationVariable() {
        // 액티비티 전환 관련 변수
        Bundle extras = getIntent().getExtras();

        String transitionPropertyStr = extras.getString(INTENT_KEY_TRANSITION_PROPERTY);
        Type type = new TypeToken<ActivityTransitionHelper>(){}.getType();
        ActivityTransitionHelper transitionProperty =
                new Gson().fromJson(transitionPropertyStr, type);
        mTransImageViewProperty =
                transitionProperty.getImageViewProperty(ActivityTransitionHelper.KEY_IMAGE);
        mTransTitleViewProperty =
                transitionProperty.getTextViewProperty(ActivityTransitionHelper.KEY_TEXT);
        mTransFeedTitleViewProperty =
                transitionProperty.getTextViewProperty(ActivityTransitionHelper.KEY_SUB_TEXT);

        // 썸네일 위치, 목표 위치 계산
        int[] screenLocation = new int[2];
        mTopNewsImageWrapper.getLocationOnScreen(screenLocation);
        int left = screenLocation[0];
        int top = screenLocation[1];
        mThumbnailLeftDelta = mTransImageViewProperty.getLeft() - left;
        mThumbnailTopDelta = mTransImageViewProperty.getTop() - top;

        float widthRatio = mTopNewsImageWrapper.getWidth()/(float)mTransImageViewProperty.getWidth();
        float heightRatio = mTopNewsImageWrapper.getHeight()/(float)mTransImageViewProperty.getHeight();
        boolean fitWidth = widthRatio > heightRatio;
        mThumbnailScaleRatio = fitWidth ? widthRatio : heightRatio;
        final int targetWidth = (int)(mTransImageViewProperty.getWidth() * mThumbnailScaleRatio);
        mThumbnailLeftTarget = fitWidth
                ? left : left - (targetWidth - mTopNewsImageWrapper.getWidth())/2;
        mThumbnailTopTarget = top;

        // 애니메이션 속도 관련 변수
        mImageFilterAnimationDuration = getResources().getInteger(
                R.integer.news_feed_detail_image_filter_duration_milli) * sAnimatorScale;
        mImageScaleAnimationDuration = getResources().getInteger(
                R.integer.news_feed_detail_image_scale_duration_milli) * sAnimatorScale;
        mRootViewHorizontalScaleAnimationDuration = getResources().getInteger(
                R.integer.news_feed_detail_root_horizontal_scale_duration_milli) * sAnimatorScale;
        mRootViewVerticalScaleAnimationDuration = getResources().getInteger(
                R.integer.news_feed_detail_root_vertical_scale_duration_milli) * sAnimatorScale;
        mRootViewTranslationAnimationDuration = getResources().getInteger(
                R.integer.news_feed_detail_root_translation_duration_milli) * sAnimatorScale;
        mThumbnailTextAnimationDuration = getResources().getInteger(
                R.integer.news_feed_detail_thumbnail_text_duration_milli) * sAnimatorScale;
        mActionBarIconAnimationDuration = getResources().getInteger(
                R.integer.news_feed_detail_action_bar_content_duration_milli) * sAnimatorScale;
        mActionBarBgAnimationDuration = getResources().getInteger(
                R.integer.news_feed_detail_action_bar_bg_duration_milli) * sAnimatorScale;
    }

    private void addThumbnailTextView(TextView textView,
                                      ActivityTransitionTextViewProperty textViewProperty) {
        int padding = textViewProperty.getPadding();

        // 뉴스 타이틀 썸네일 뷰 추가
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(textViewProperty.getText());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,textViewProperty.getTextSize());
        textView.setTextColor(textViewProperty.getTextColor());
        textView.setGravity(textViewProperty.getGravity());
        textView.setEllipsize(
                TextUtils.TruncateAt.values()[textViewProperty.getEllipsizeOrdinal()]);
        textView.setMaxLines(textViewProperty.getMaxLine());

        addThumbnailView(textView, textViewProperty);
    }

    private void addThumbnailView(View view, ActivityTransitionProperty property) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                property.getWidth(), property.getHeight());
        lp.leftMargin = property.getLeft();
        lp.topMargin = property.getTop();
        mTransitionLayout.addView(view, lp);
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    public void runEnterAnimation() {
        mIsAnimatingActivityTransitionAnimation = true;

        final PathInterpolator commonInterpolator = new PathInterpolator(.4f, .0f, 1.f, .2f);

        // 애니메이션 실행 전 텍스트뷰, 리사이클러뷰, 배경 안보이게 해두기
        mTopNewsTextLayout.setAlpha(0);
        mBottomNewsListRecyclerView.setAlpha(0);
//        mRootLayoutBackground.setAlpha(0);

        // 루트 뷰, 이미지뷰의 위치 곡선 정의
        AnimatorPath imageTranslationPath = new AnimatorPath();
        imageTranslationPath.moveTo(mThumbnailLeftDelta, mThumbnailTopDelta);
//        imageTranslationPath.curveTo(
//                mThumbnailLeftDelta/2, mThumbnailTopDelta, 0, mThumbnailTopDelta/2,
//                mThumbnailLeftTarget, mThumbnailTopTarget);
        imageTranslationPath.lineTo(mThumbnailLeftTarget, mThumbnailTopTarget);

        ObjectAnimator rootClipBoundTranslationAnimator = ObjectAnimator.ofObject(NewsFeedDetailActivity.this,
                "rootClipBoundTranslation", new PathEvaluator(), imageTranslationPath.getPoints().toArray());
        rootClipBoundTranslationAnimator.setInterpolator(AnimationFactory.makeNewsFeedImageAndRootTransitionInterpolator(getApplicationContext()));
        rootClipBoundTranslationAnimator.setDuration(mRootViewTranslationAnimationDuration);
        rootClipBoundTranslationAnimator.start();

//        PropertyValuesHolder rootClipBoundTranslationPvh = PropertyValuesHolder.ofObject(
//                "RootClipBoundTranslation", new PathEvaluator(), imageTranslationPath.getPoints().toArray());

//        PropertyValuesHolder rootClipBoundTranslationPvh = PropertyValuesHolder.ofObject(
//                "RootClipBoundTranslation", new TypeEvaluator<Point>() {
//                    @Override
//                    public Point evaluate(float v, Point startPoint, Point endPoint) {
//                        return new Point((int)(startPoint.x * (1 - v) + endPoint.x * v),
//                                (int)(startPoint.y * (1 - v) + endPoint.y * v));
//                    }
//                },
//                new Point(mThumbnailLeftDelta, mThumbnailTopDelta),
//                new Point(mThumbnailLeftTarget,
//                        mThumbnailTopTarget + (int)(mTopImageView.getHeight() * 0.1)));

        // 루트 뷰의 외곽 크기 조절
        mRootClipBound = new Rect(
                mTransImageViewProperty.getLeft(),
                mTransImageViewProperty.getTop(),
                mTransImageViewProperty.getLeft() + mTransImageViewProperty.getWidth(),
                mTransImageViewProperty.getTop() + mTransImageViewProperty.getHeight());
        mRootLayout.setClipToOutline(true);
        mRootLayout.setClipBounds(mRootClipBound);

        ObjectAnimator rootClipBoundHorizontalSizeAnimator =
                ObjectAnimator.ofObject(NewsFeedDetailActivity.this, "rootWidthClipBoundSize", new TypeEvaluator<Float>() {
                    @Override
                    public Float evaluate(float v, Float startScale, Float endScale) {
                        return startScale * (1 - v) + endScale * v;
                    }
                }, 1.f, mThumbnailScaleRatio);
        rootClipBoundHorizontalSizeAnimator.setInterpolator(AnimationFactory.makeNewsFeedRootBoundHorizontalInterpolator(getApplicationContext()));
        rootClipBoundHorizontalSizeAnimator.setDuration(mRootViewHorizontalScaleAnimationDuration); // 530
        rootClipBoundHorizontalSizeAnimator.start();

        ObjectAnimator rootClipBoundVerticalSizeAnimator =
                ObjectAnimator.ofInt(NewsFeedDetailActivity.this, "rootVerticalClipBoundSize",
                        mTransImageViewProperty.getHeight(), mRootLayout.getHeight());
        rootClipBoundVerticalSizeAnimator.setInterpolator(AnimationFactory.makeNewsFeedRootBoundVerticalInterpolator(getApplicationContext()));
        rootClipBoundVerticalSizeAnimator.setDuration(mRootViewVerticalScaleAnimationDuration); //
        // 530
        rootClipBoundVerticalSizeAnimator.start();


//        PropertyValuesHolder rootClipBoundSizePvh = PropertyValuesHolder.ofObject(
//                "RootClipBoundSize", new TypeEvaluator<PointF>() {
//                    @Override
//                    public PointF evaluate(float v, PointF startScale, PointF endScale) {
//                        return new PointF(startScale.x * (1 - v) + endScale.x * v,
//                                startScale.y * (1 - v) + endScale.y * v);
//                    }
//                },
//                new PointF(1.0f, 1.0f),
//                new PointF(mThumbnailScaleRatio, mThumbnailScaleRatio * 0.6f));

        // 곡선 이동 PropertyValuesHolder 준비
        mTopNewsImageWrapper.setPivotX(0);
        mTopNewsImageWrapper.setPivotY(0);
        ObjectAnimator imageWrapperTranslationAnimator = ObjectAnimator.ofObject(
                NewsFeedDetailActivity.this, "ImageWrapperTranslation", new PathEvaluator(),
                imageTranslationPath.getPoints().toArray());
        imageWrapperTranslationAnimator.setInterpolator(AnimationFactory.makeNewsFeedImageAndRootTransitionInterpolator(getApplicationContext()));
        imageWrapperTranslationAnimator.setDuration(mRootViewTranslationAnimationDuration);
        imageWrapperTranslationAnimator.start();

        // 크기 변경 PropertyValuesHolder 준비
        ViewGroup.LayoutParams lp = mTopNewsImageWrapper.getLayoutParams();
        lp.width = mTransImageViewProperty.getWidth();
        lp.height = mTransImageViewProperty.getHeight();
        mTopNewsImageWrapper.setLayoutParams(lp);

        ObjectAnimator imageWrapperSizeAnimator = ObjectAnimator.ofFloat(
                NewsFeedDetailActivity.this, "ImageWrapperSize", 1.0f, mThumbnailScaleRatio);
        imageWrapperSizeAnimator.setInterpolator(
                AnimationFactory.makeNewsFeedImageScaleInterpolator(getApplicationContext()));
        imageWrapperSizeAnimator.setDuration(mImageScaleAnimationDuration);
        imageWrapperSizeAnimator.start();

        // 준비해 놓은 PropertyValuesHolder들 실행
//        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(NewsFeedDetailActivity.this,
//                imageWrapperTranslationPvh, imageWrapperSizePvh
//        );
//        animator.setInterpolator(AnimationFactory.makeNewsFeedImageAndRootTransitionInterpolator());
//        animator.setDuration(mImageAnimationDuration);
        imageWrapperSizeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mIsAnimatingActivityTransitionAnimation = false;

                // 액션바 텍스트 페이드인
                ObjectAnimator actionBarTitleAnimator = ObjectAnimator.ofFloat(
                        NewsFeedDetailActivity.this, "ActionBarTitleAlpha", 0.0f, 1.0f);
                actionBarTitleAnimator.setDuration(mActionBarIconAnimationDuration);
                actionBarTitleAnimator.setInterpolator(commonInterpolator);
                actionBarTitleAnimator.start();

                // 액션바 아이콘 페이드인
                ObjectAnimator actionBarHomeIconAnimator =
                        ObjectAnimator.ofInt(mActionBarHomeIcon, "alpha", 0, 255);
                actionBarHomeIconAnimator.setDuration(mActionBarIconAnimationDuration);
                actionBarHomeIconAnimator.setInterpolator(commonInterpolator);
                actionBarHomeIconAnimator.start();

                ObjectAnimator actionBarOverflowIconAnimator =
                        ObjectAnimator.ofInt(mActionBarOverflowIcon, "alpha", 0, 255);
                actionBarOverflowIconAnimator.setDuration(mActionBarIconAnimationDuration);
                actionBarOverflowIconAnimator.setInterpolator(commonInterpolator);
                actionBarOverflowIconAnimator.start();

                animateTopOverlayFadeIn();
            }
        });
        /*
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private boolean mHasAnimated = false;

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float elapsedValue = valueAnimator.getAnimatedFraction();

                if (elapsedValue > 0.7) {
                    if (mHasAnimated) {
                        return;
                    }
                    mHasAnimated = true;

                    mTopNewsTextLayout.setAlpha(1);
                    mBottomNewsListRecyclerView.setAlpha(1);
                    mRootLayoutBackground.setAlpha(1);

                    Rect rootLayoutOriginalBound = new Rect(
                            mRootLayout.getLeft(), mRootLayout.getTop(),
                            mRootLayout.getRight(), mRootLayout.getBottom());

                    ObjectAnimator clipBoundAnimator = ObjectAnimator.ofObject(
                            NewsFeedDetailActivity.this, "RootLayoutClipBound",
                            new TypeEvaluator<Rect>() {
                                @Override
                                public Rect evaluate(float v, Rect startRect, Rect endRect) {
                                    Rect rect = new Rect();
                                    rect.left = (int) (startRect.left * (1 - v) + endRect.left * v);
                                    rect.top = (int) (startRect.top * (1 - v) + endRect.top * v);
                                    rect.right = (int) (startRect.right * (1 - v) + endRect.right * v);
                                    rect.bottom = (int) (startRect.bottom * (1 - v) + endRect.bottom * v);
                                    return rect;
                                }
                            }, mRootClipBound, rootLayoutOriginalBound);
                    clipBoundAnimator.setInterpolator(
                            AnimationFactory.makeNewsFeedRootBoundHorizontalInterpolator());
                    clipBoundAnimator.setDuration((int)(mDebugAnimDuration * 1.5));
                    clipBoundAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                            mIsAnimatingActivityTransitionAnimation = false;
                        }
                    });
                    clipBoundAnimator.start();
                }
            }
        });
        */
//        animator.start();

        // 이미지뷰 컬러 필터 애니메이션
        animateTopImageViewColorFilter();

        // 상단 뉴스의 텍스트뷰 위치, 알파 애니메이션
//        Point displaySize = new Point();
//        getWindowManager().getDefaultDisplay().getSize(displaySize);
//        final int translationY = displaySize.y - mTopNewsTextLayout.getTop();
//        mTopNewsTextLayout.setAlpha(0);
//        mTopNewsTextLayout.setTranslationY(translationY);
//        mTopNewsTextLayout.animate().
//                alpha(1.0f).
//                translationY(0).
//                setDuration(mDebugAnimDuration).
//                setInterpolator(pathInterpolator).
//                start();

        // 하단 리사이클러뷰 아이템들 위치 애니메이션
//        ViewTreeObserver observer = mBottomNewsListRecyclerView.getViewTreeObserver();
//        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                mBottomNewsListRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
//
//                // animate bottom recycler view
//                for (int i = 0; i < mBottomNewsListRecyclerView.getChildCount(); i++) {
//                    View child = mBottomNewsListRecyclerView.getChildAt(i);
//
//                    child.setTranslationY(child.getTop() + child.getBottom());
//                    child.animate().
//                            translationY(0).
//                            setStartDelay(i*100).
//                            setDuration(mDebugAnimDuration).
//                            setInterpolator(pathInterpolator).
//                            start();
//                }
//
//                return true;
//            }
//        });

        // 배경 색상 페이드인 애니메이션
//        mRootLayoutBackground.setAlpha(0);
//        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mRootLayoutBackground, "alpha", 0, 255);
//        bgAnim.setDuration(mDebugAnimDuration);
//        bgAnim.setInterpolator(pathInterpolator);
//        bgAnim.start();
//
//        mRecyclerViewBackground.setAlpha(0);
//        ObjectAnimator recyclerBgAnim = ObjectAnimator.ofInt(mRecyclerViewBackground, "alpha", 0,
//                255);
//        recyclerBgAnim.setDuration(mDebugAnimDuration);
//        recyclerBgAnim.setInterpolator(pathInterpolator);
//        recyclerBgAnim.start();

        // 뉴스 타이틀 썸네일 텍스트뷰 애니메이션
        mNewsTitleThumbnailTextView.setAlpha(0.0f);
//        ViewPropertyAnimator thumbnailAlphaAnimator = mNewsTitleThumbnailTextView.animate();
//        thumbnailAlphaAnimator.alpha(0.0f);
//        thumbnailAlphaAnimator.setDuration(mThumbnailTextAnimationDuration);
//        thumbnailAlphaAnimator.setInterpolator(commonInterpolator);
//        thumbnailAlphaAnimator.start();

        // 뉴스 피드 타이틀 썸네일 텍스트뷰 애니메이션
        mNewsFeedTitleThumbnailTextView.setAlpha(0.0f);
//        ViewPropertyAnimator feedTitleThumbnailAlphaAnimator
//                = mNewsFeedTitleThumbnailTextView.animate();
//        feedTitleThumbnailAlphaAnimator.alpha(0.0f);
//        feedTitleThumbnailAlphaAnimator.setDuration(mThumbnailTextAnimationDuration);
//        feedTitleThumbnailAlphaAnimator.setInterpolator(commonInterpolator);
//        feedTitleThumbnailAlphaAnimator.start();

        // 액션바 내용물 우선 숨겨두도록
        mActionBarHomeIcon.setAlpha(0);
        mColorSpan.setAlpha(0.0f);
        mActionBarOverflowIcon.setAlpha(0);

        //　액션바 배경, 오버레이 페이드 인
        saveTopOverlayAlphaState();
        mTopOverlayView.setAlpha(0);
        mActionBarOverlayView.setAlpha(0);
    }

    private void saveTopOverlayAlphaState() {
        mTopOverlayView.setTag(mTopOverlayView.getAlpha());
        mActionBarOverlayView.setTag(mActionBarOverlayView.getAlpha());
    }

    /**
     * runEnterAnimation 에서 루트뷰 clip bound 위치 조절에 사용될 메서드.
     * @param newLoc 계산된 위치
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setRootClipBoundTranslation(PathPoint newLoc) {
        mRootClipBound.left = (int)newLoc.mX;
        mRootClipBound.top = (int)newLoc.mY;
        mRootLayout.setClipBounds(mRootClipBound);
    }

    /**
     * runEnterAnimation 에서 루트뷰 clip bound 스케일링에 사용될 메서드.
     * @param scale 계산된 사이즈
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setRootClipBoundSize(PointF scale) {
        mRootClipBound.right = mRootClipBound.left +
                (int)(mTransImageViewProperty.getWidth() * scale.x);
        mRootClipBound.bottom = mRootClipBound.top +
                (int)(mTransImageViewProperty.getHeight() * scale.y);
        mRootLayout.setClipBounds(mRootClipBound);
    }

    public void setRootVerticalClipBoundSize(int height) {
        mRootClipBound.bottom = mRootClipBound.top + height;
        mRootLayout.setClipBounds(mRootClipBound);
    }

    public void setRootWidthClipBoundSize(float scale) {
        mRootClipBound.right = mRootClipBound.left +
                (int)(mTransImageViewProperty.getWidth() * scale);
        mRootLayout.setClipBounds(mRootClipBound);
    }

    public void setRootHeightClipBoundSize(float scale) {
        mRootClipBound.bottom = mRootClipBound.top +
                (int)(mTransImageViewProperty.getHeight() * scale);
        NLLog.now("mRootClipBound.top : " + mRootClipBound.top);
        NLLog.now("mRootClipBound.bottom : " + mRootClipBound.bottom);
        NLLog.now("mRootClipBound.height() : " + (mRootClipBound.bottom - mRootClipBound.top));
        mRootLayout.setClipBounds(mRootClipBound);
    }

    /**
     * runEnterAnimation 에서 이미지뷰 wrapper 스케일링에 사용될 메서드.
     * @param scale 계산된 사이즈
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setImageWrapperSize(float scale) {
        mTopNewsImageWrapper.setScaleX(scale);
        mTopNewsImageWrapper.setScaleY(scale);
    }

    /**
     * runEnterAnimation 에서 이미지뷰 wrapper 위치 이동에 사용될 메서드.
     * @param newLoc 계산된 위치
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setImageWrapperTranslation(PathPoint newLoc) {
        mTopNewsImageWrapper.setTranslationX((int)newLoc.mX);
        mTopNewsImageWrapper.setTranslationY((int) newLoc.mY);
    }

    public void setRootLayoutClipBound(Rect clipBound) {
        mRootLayout.setClipBounds(clipBound);
    }

    private void animateTopImageViewColorFilter() {
        if (mHasAnimatedColorFilter) {
            return;
        }
        ObjectAnimator color = ObjectAnimator.ofArgb(mTopImageView.getColorFilter(), "color", 0);
        color.addUpdateListener(new ColorFilterListener(mTopImageView));
        color.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mHasAnimatedColorFilter = true;
            }

        });
        color.setDuration(mImageFilterAnimationDuration).start();
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

    private void darkenHeroImage() {
        int filterColor = getFilterColor();

        int red = Color.red(filterColor);
        int green = Color.green(filterColor);
        int blue = Color.blue(filterColor);

        ObjectAnimator color = ObjectAnimator.ofArgb(mTopImageView.getColorFilter(), "color",
                Color.argb(Color.alpha(filterColor), red, green, blue));

        color.addUpdateListener(new ColorFilterListener(mTopImageView));
        color.setDuration(mImageFilterAnimationDuration);
        color.start();
    }

    private void runExitAnimation() {
        mIsAnimatingActivityTransitionAnimation = true;

        final PathInterpolator pathInterpolator = AnimationFactory.makeNewsFeedReverseTransitionInterpolator();

        // 곡선 이동 PropertyValuesHolder 준비
        AnimatorPath path = new AnimatorPath();
        path.moveTo(mThumbnailLeftTarget, mThumbnailTopTarget);
        path.curveTo(0, mThumbnailTopDelta/2, mThumbnailLeftDelta/2, mThumbnailTopDelta,
                mThumbnailLeftDelta, mThumbnailTopDelta);

        PropertyValuesHolder imageWrapperTranslationPvh = PropertyValuesHolder.ofObject(
                "ImageWrapperTranslation", new PathEvaluator(), path.getPoints().toArray());

        // 크기 변경 PropertyValuesHolder 준비
        ViewGroup.LayoutParams lp = mTopNewsImageWrapper.getLayoutParams();
        lp.width = mTransImageViewProperty.getWidth();
        lp.height = mTransImageViewProperty.getHeight();
        mTopNewsImageWrapper.setLayoutParams(lp);

        PropertyValuesHolder imageWrapperSizePvh = PropertyValuesHolder.ofFloat(
                "ImageWrapperSize", mThumbnailScaleRatio, 1.0f);

        // 준비해 놓은 PropertyValuesHolder들 실행
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(NewsFeedDetailActivity.this,
                imageWrapperTranslationPvh, imageWrapperSizePvh
        );
        animator.setInterpolator(pathInterpolator);
        animator.setDuration(mExitAnimationDuration);
        animator.start();

        // 이미지뷰 컬러 필터 애니메이션
        darkenHeroImage();

        // 상단 뉴스의 텍스트뷰 위치, 알파 애니메이션
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        final int translationY = displaySize.y - mTopNewsTextLayout.getTop();
        mTopNewsTextLayout.animate().
                alpha(0.0f).
                translationYBy(translationY).
                setDuration(mExitAnimationDuration).
                setInterpolator(pathInterpolator).
                start();

        // 하단 리사이클러뷰 위치 애니메이션
        mBottomNewsListRecyclerView.animate().
                translationYBy(displaySize.y - mBottomNewsListRecyclerView.getTop() + mWindowInsetEnd).
                setDuration(mExitAnimationDuration).
                setInterpolator(pathInterpolator).
                withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        NewsFeedDetailActivity.super.finish();
                        overridePendingTransition(0, 0);
                    }
                }).
                start();

        // 배경 색상 페이드인 애니메이션
//        mRootLayoutBackground.setAlpha(1);
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mRootLayoutBackground, "alpha", 255, 0);
        bgAnim.setDuration(mExitAnimationDuration);
        bgAnim.setInterpolator(pathInterpolator);
        bgAnim.start();

//        mRecyclerViewBackground.setAlpha(1);
        ObjectAnimator recyclerBgAnim = ObjectAnimator.ofInt(mRecyclerViewBackground, "alpha", 255,
                0);
        recyclerBgAnim.setDuration(mExitAnimationDuration);
        recyclerBgAnim.setInterpolator(pathInterpolator);
        recyclerBgAnim.start();

        // 뉴스 타이틀 썸네일 텍스트뷰 애니메이션
        mNewsTitleThumbnailTextView.setAlpha(0.0f);
        ViewPropertyAnimator thumbnailAlphaAnimator = mNewsTitleThumbnailTextView.animate();
        thumbnailAlphaAnimator.alpha(1.0f);
        thumbnailAlphaAnimator.setDuration(mExitAnimationDuration/2);
        thumbnailAlphaAnimator.setInterpolator(pathInterpolator);
        thumbnailAlphaAnimator.start();

        // 뉴스 피드 타이틀 썸네일 텍스트뷰 애니메이션
        mNewsFeedTitleThumbnailTextView.setAlpha(0.0f);
        ViewPropertyAnimator feedTitleThumbnailAlphaAnimator =
                mNewsFeedTitleThumbnailTextView.animate();
        feedTitleThumbnailAlphaAnimator.alpha(1.0f);
        feedTitleThumbnailAlphaAnimator.setDuration(mExitAnimationDuration/2);
        feedTitleThumbnailAlphaAnimator.setInterpolator(pathInterpolator);
        feedTitleThumbnailAlphaAnimator.start();

        // 액션바 홈버튼 페이드인
//        mActionBarHomeIcon.setAlpha(1);
        ObjectAnimator actionBarHomeIconAnimator =
                ObjectAnimator.ofInt(mActionBarHomeIcon, "alpha", 255, 0);
        actionBarHomeIconAnimator.setDuration(mExitAnimationDuration);
        actionBarHomeIconAnimator.setInterpolator(pathInterpolator);
        actionBarHomeIconAnimator.start();

        // 액션바 텍스트 페이드인
//        mColorSpan.setAlpha(1.0f);
        ObjectAnimator actionBarTitleAnimator = ObjectAnimator.ofFloat(
                NewsFeedDetailActivity.this, "ActionBarTitleAlpha", 1.0f, 0.0f);
        actionBarTitleAnimator.setDuration(mExitAnimationDuration);
        actionBarTitleAnimator.setInterpolator(pathInterpolator);
        actionBarTitleAnimator.start();

        // 액션바 오버플로우 페이드인
//        mActionBarOverflowIcon.setAlpha(1);
        ObjectAnimator actionBarOverflowIconAnimator =
                ObjectAnimator.ofInt(mActionBarOverflowIcon, "alpha", 255, 0);
        actionBarOverflowIconAnimator.setDuration(mExitAnimationDuration);
        actionBarOverflowIconAnimator.setInterpolator(pathInterpolator);
        actionBarOverflowIconAnimator.start();

        //　액션바 배경, 오버레이 페이드 인
        animateTopOverlayFadeOut();
    }

    @Override
    public void finish() {
        if (mTopImageView.getDrawable() == null) {
            super.finish();
        } else {
            if (true || !mIsAnimatingActivityTransitionAnimation) {
                runExitAnimation();
            }
        }
    }

    private void initActionBar() {

        initActionBarGradientView();
        initActionBarIcon();
        initActionBarTitle();
        //@drawable/ic_ab_up_white
    }

    private void initActionBarTitle() {
        TypedArray typedArray = getTheme().obtainStyledAttributes(
                R.style.MainThemeActionBarTitleTextStyle, new int[]{android.R.attr.textColor});
        mActionTextColor = typedArray.getColor(0, Color.WHITE);
        typedArray.recycle();

        mColorSpan = new AlphaForegroundColorSpan(mActionTextColor);

        applyActionBarTitle();
    }

    private void applyActionBarTitle() {
        if (getActionBar() != null && mNewsFeed != null) {
            mActionBarTitle = new SpannableString(mNewsFeed.getTitle());
            mActionBarTitle.setSpan(mColorSpan, 0, mActionBarTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getActionBar().setTitle(mActionBarTitle);
        }
    }

    /**
     * runEnterAnimation 에서 액션바 타이틀 알파값 애니메이션에 사용될 메서드.
     * @param value 계산된 알파값
     */
    @SuppressWarnings("UnusedDeclaration")
    private void setActionBarTitleAlpha(float value) {
        mColorSpan.setAlpha(value);
        mActionBarTitle.setSpan(mColorSpan, 0, mActionBarTitle.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (getActionBar() != null) {
            getActionBar().setTitle(mActionBarTitle);
        }
    }

    private void initActionBarIcon() {
        if (getActionBar() != null) {
            Bitmap upIconBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_ab_up_white);
            mActionBarHomeIcon = new BitmapDrawable(getResources(), upIconBitmap);
            getActionBar().setHomeAsUpIndicator(mActionBarHomeIcon);

            Bitmap overflowIconBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_menu_moreoverflow_mtrl_alpha);
            mActionBarOverflowIcon = new BitmapDrawable(getResources(), overflowIconBitmap);
        }
    }

    private void initActionBarGradientView() {
        int actionBarSize = ScreenUtils.calculateActionBarSize(this);
        int statusBarSize = 0;

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarSize = getResources().getDimensionPixelSize(resourceId);
        }

        if (actionBarSize != 0) {
            mTopOverlayView.getLayoutParams().height = (actionBarSize + statusBarSize) * 2;
            mActionBarOverlayView.getLayoutParams().height = actionBarSize + statusBarSize;
        }
    }

    private void initSwipeRefreshView() {
        mSwipeRefreshLayout.setEnabled(false);
//        mSwipeRefreshLayout.setRefreshing(true);

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_color_scheme_1, R.color.refresh_color_scheme_2,
                R.color.refresh_color_scheme_3, R.color.refresh_color_scheme_4);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
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
        mTopTitleTextView.setAlpha(0);
        mTopDescriptionTextView.setAlpha(0);

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
            //TODO when NLNewsFeed is invalid.
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
        mAdapter = new NewsFeedDetailAdapter(this, this);

        mBottomNewsListRecyclerView.setAdapter(mAdapter);

        // make bottom news array list. EXCLUDE top news.
//        mBottomNewsList = new ArrayList<NLNews>(mNewsFeed.getNewsList());

        final int newsCount = mNewsFeed.getNewsList().size();
        for (int i = 0; i < newsCount; i++) {
            News news = mNewsFeed.getNewsList().get(i);
            mAdapter.addNews(news);
//            final int idx = i;
//            mBottomNewsListRecyclerView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mAdapter.addNews(news);
//
//                    if (idx == (mNewsFeed.getNewsList().size() - 1)) {
//                        mBottomNewsListRecyclerView.getItemAnimator()
//                                .isRunning(NewsFeedDetailActivity.this);
//                    }
//                }
//            }, BOTTOM_NEWS_ANIM_DELAY_UNIT_MILLI * i + 1);
        }
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

//        getMenuInflater().inflate(R.menu.news_feed, menu);
        SubMenu subMenu = menu.addSubMenu(Menu.NONE, R.id.action_newsfeed_overflow, Menu.NONE, "");
        subMenu.setIcon(mActionBarOverflowIcon);
        subMenu.add(Menu.NONE, R.id.action_replace_newsfeed, Menu.NONE, R.string.action_newsfeed);
        MenuItemCompat.setShowAsAction(subMenu.getItem(), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finishAfterTransition();
                return true;

            case R.id.action_replace_newsfeed:
                startActivityForResult(new Intent(NewsFeedDetailActivity.this, NewsSelectActivity.class),
                        REQ_SELECT_NEWS_FEED);
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
        applyActionBarTitle();

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
        if (imgUrl != null) {
            getIntent().putExtra(INTENT_KEY_IMAGE_LOADED, true);
            getIntent().putExtra(INTENT_KEY_IMAGE_URL, imgUrl);
            setResult(RESULT_OK, getIntent());

            mImageLoader.get(imgUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bitmap = response.getBitmap();

                    if (bitmap == null && isImmediate) {
                        // 비트맵이 null이지만 인터넷을 통하지 않고 바로 불린 콜백이라면 무시하자
                        return;
                    }

                    if (bitmap != null) {
                        setTopNewsImageBitmap(bitmap);

                        animateTopItems();
                    }
                    hideLoadingCover();

                    if (mIsLoadingImageOnInit) {
                        mIsLoadingImageOnInit = false;
                        animateTopImageViewColorFilter();
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    applyDummyTopNewsImage();

                    animateTopItems();

                    hideLoadingCover();

                    if (mIsLoadingImageOnInit) {
                        mIsLoadingImageOnInit = false;
                        animateTopImageViewColorFilter();
                    }
                }
            });
        } else if (mTopNews.isImageUrlChecked()) {
            applyDummyTopNewsImage();

            animateTopItems();
        } else {
            showLoadingCover();
            animateTopItems();
            new NewsFeedDetailNewsImageUrlFetchTask(mTopNews, this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            mIsLoadingImageOnInit = true;
        }
    }

    private void fetchNewsFeed(NewsFeedUrl newsFeedUrl) {
        new NewsFeedDetailNewsFeedFetchTask(getApplicationContext(), newsFeedUrl, this, false)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        showLoadingCover();
        configBeforeRefresh();
    }

    private void configBeforeRefresh() {
        mIsRefreshing = true;
        mSwipeRefreshLayout.setRefreshing(true);
        animateTopOverlayFadeOut();
    }
    private void configAfterRefreshDone() {
        mSwipeRefreshLayout.setRefreshing(false);
        animateTopOverlayFadeIn();
    }
    private void showLoadingCover() {
        mLoadingCoverView.setVisibility(View.VISIBLE);
    }
    private void hideLoadingCover() {
        mLoadingCoverView.setVisibility(View.GONE);
    }

    private void animateTopOverlayFadeIn() {
        if (mTopOverlayView.getTag() == null || mActionBarOverlayView.getTag() == null
                || mTopOverlayView.getAlpha() > 0 || mActionBarOverlayView.getAlpha() > 0) {
            return;
        }
        mTopOverlayView.animate()
                .setDuration(mActionBarBgAnimationDuration)
                .alpha((Float)mTopOverlayView.getTag())
                .setInterpolator(new DecelerateInterpolator());
        mActionBarOverlayView.animate()
                .setDuration(mActionBarBgAnimationDuration)
                .alpha((Float) mActionBarOverlayView.getTag())
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animator) {}
                    @Override public void onAnimationCancel(Animator animator) {}
                    @Override public void onAnimationRepeat(Animator animator) {}

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mIsRefreshing = false;
                    }

                });
    }

    private void animateTopOverlayFadeOut() {
        saveTopOverlayAlphaState();
        mTopOverlayView.animate()
                .setDuration(mActionBarBgAnimationDuration)
                .alpha(0f)
                .setInterpolator(new DecelerateInterpolator());
        mActionBarOverlayView.animate()
                .setDuration(mActionBarBgAnimationDuration)
                .alpha(0f)
                .setInterpolator(new DecelerateInterpolator());
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
            // 1. 메인 상단에서 들어온 경우 : TintType.GRAYSCALE
            // 2. 메인 하단에서 들어온 경우 :
            // 2.1 palette에서 색을 꺼내 사용할 수 있는 경우 : TintType.PALETTE
            // 2.2 palette에서 색을 꺼내 사용할 수 없는 경우 : TintType.GRAYSCALE(default)

            // 상단 뉴스피드인지 하단 뉴스피드인지 구분
            String newsLocation = getIntent().getExtras().getString(
                    MainActivity.INTENT_KEY_NEWS_FEED_LOCATION, null);

            if (newsLocation != null) {
                if (newsLocation.equals(MainActivity.INTENT_VALUE_TOP_NEWS_FEED)) {
                    // 메인 상단에서 온 경우
                    mTintType = TintType.GRAYSCALE;
                } else if (newsLocation.equals(MainActivity.INTENT_VALUE_BOTTOM_NEWS_FEED)) {
                    // 메인 하단에서 온 경우
                    PaletteItem paletteItem = getTopImageFilterColorPaletteItem();
                    if (paletteItem != null) {
                        mTintType = TintType.PALETTE;
                    } else {
                        mTintType = TintType.GRAYSCALE;
                    }
                } else {
                    mTintType = TintType.GRAYSCALE;
                }
            } else {
                mTintType = TintType.GRAYSCALE;
            }
        }
        applyPalette(!mHasAnimatedColorFilter);
    }

    private void animateTopItems() {
        mTopTitleTextView.animate()
                .setStartDelay(450)
                .setDuration(650)
                .alpha(1f)
                .setInterpolator(new DecelerateInterpolator());
        mTopDescriptionTextView.animate()
                .setStartDelay(450)
                .setDuration(650)
                .alpha(1f)
                .setInterpolator(new DecelerateInterpolator());
    }

    private void applyPalette(boolean applyColorFilter) {
        PaletteItem lightVibrantColor = mPalette.getLightVibrantColor();

        mTopTitleTextView.setTextColor(Color.WHITE);

        if (lightVibrantColor != null) {
            mTopDescriptionTextView.setTextColor(lightVibrantColor.getRgb());
        }

        int filterColor = getFilterColor();

        int red = Color.red(filterColor);
        int green = Color.green(filterColor);
        int blue = Color.blue(filterColor);

        int colorWithoutAlpha = Color.rgb(red, green, blue);
//        mTopContentLayout.setBackground(new ColorDrawable(colorWithoutAlpha));
        mTopNewsTextLayout.setBackground(new ColorDrawable(colorWithoutAlpha));

        if (applyColorFilter) {
            mTopImageView.setColorFilter(Color.argb(Color.alpha(filterColor), red, green, blue));
        }
    }

    private int getFilterColor() {
        int color;
        int alpha;
        TintType tintType = mTintType != null ? mTintType : TintType.GRAYSCALE;

        switch(tintType) {
            case DUMMY:
                color = NewsFeedUtils.getDummyImageFilterColor();
                alpha = Color.alpha(color);
                break;
            case PALETTE:
                PaletteItem darkVibrantColor = getTopImageFilterColorPaletteItem();
                if (darkVibrantColor != null) {
                    color = darkVibrantColor.getRgb();
                    alpha = getResources().getInteger(R.integer.vibrant_color_tint_alpha);
                    break;
                }
                // darkVibrantColor == null 이라면 아래의 구문으로 넘어간다.
            case GRAYSCALE:
            default:
                color = NewsFeedUtils.getGrayFilterColor();
                alpha = Color.alpha(color);
                break;
        }

        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private PaletteItem getTopImageFilterColorPaletteItem() {
        PaletteItem darkVibrantColor = mPalette.getDarkVibrantColor();
        if (darkVibrantColor != null) {
            return darkVibrantColor;
        } else {
            return null;
        }
    }

    @Override
    public void onItemClick(NewsFeedDetailAdapter.ViewHolder viewHolder, News news) {
//        NLLog.now("detail bottom onItemClick");

        Intent intent = new Intent(this, NewsDetailActivity.class);
        intent.putExtra(INTENT_KEY_NEWS, news);

        startActivity(intent);
//        NLWebUtils.openLink(this, news.getLink());
    }

    private void applySystemWindowsBottomInset(int container) {
        View containerView = findViewById(container);
        containerView.setFitsSystemWindows(true);
        containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
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
        if (scrollY >= 0) {
            mTopNewsImageWrapper.setTranslationY(scrollY * 0.4f);

            mActionBarOverlayView.setAlpha(scrollY * 0.0005f);
            if (mActionBarOverlayView.getAlpha() >= 0.6f) {
                mActionBarOverlayView.setAlpha(0.6f);
            }
        } else {
            mTopNewsImageWrapper.setTranslationY(0);
            if (mActionBarOverlayView.getAlpha() != 0) {
                mActionBarOverlayView.setAlpha(0);
            }
        }
    }

    @Override
    public void onNewsFeedFetchSuccess(NewsFeed newsFeed) {
        mNewsFeed = newsFeed;

        // cache
        archiveNewsFeed(newsFeed);

        if (mNewsFeed.getNewsList().size() > 0) {
            mTopNews = mNewsFeed.getNewsList().remove(0);
        }

        notifyTopNewsChanged();
        notifyBottomNewsChanged();

        new NewsFeedDetailNewsImageUrlFetchTask(mTopNews, this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onNewsFeedFetchFail() {
        configAfterRefreshDone();
    }

    @Override
    public void onImageUrlFetchSuccess(News news, String url) {
        configAfterRefreshDone();

        news.setImageUrl(url);
        news.setImageUrlChecked(true);

        // 아카이빙을 위해 임시로 top news를 news feed에 추가.
        mNewsFeed.addNewsAt(0, news);
        archiveNewsFeed(mNewsFeed);
        mNewsFeed.removeNewsAt(0);

        applyImage();
    }

    @Override
    public void onImageUrlFetchFail(News news) {
        configAfterRefreshDone();
        hideLoadingCover();

        news.setImageUrlChecked(true);

        applyImage();
    }

    private static class ColorFilterListener implements ValueAnimator.AnimatorUpdateListener {
        private final ImageView mHeroImageView;

        public ColorFilterListener(ImageView hero) {
            mHeroImageView = hero;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mHeroImageView.getDrawable().setColorFilter(mHeroImageView.getColorFilter());
        }
    }

    private void archiveNewsFeed(NewsFeed newsFeed) {
        // 이전 intent를 사용, 상단 뉴스피드인지 하단 뉴스피드인지 구분
        String newsLocation = getIntent().getExtras().getString(
                MainActivity.INTENT_KEY_NEWS_FEED_LOCATION, null);

        // 저장
        if (newsLocation != null) {
            Context context = getApplicationContext();
            if (newsLocation.equals(MainActivity.INTENT_VALUE_TOP_NEWS_FEED)) {
                NewsFeedArchiveUtils.saveTopNewsFeed(context, newsFeed);
            } else if (newsLocation.equals(MainActivity.INTENT_VALUE_BOTTOM_NEWS_FEED)) {
                int idx = getIntent().getExtras().getInt(
                        MainActivity.INTENT_KEY_BOTTOM_NEWS_FEED_INDEX);
                NewsFeedArchiveUtils.saveBottomNewsFeedAt(context, newsFeed, idx);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch(requestCode) {
                case REQ_SELECT_NEWS_FEED:
                    NewsFeed newsFeed = data.getExtras().getParcelable(
                            NewsSelectFragment.KEY_SELECTED_NEWS_FEED);

                    archiveNewsFeed(newsFeed);

                    fetchNewsFeed(newsFeed.getNewsFeedUrl());

                    getIntent().putExtra(INTENT_KEY_NEWSFEED_REPLACED, true);
                    setResult(RESULT_OK, getIntent());

                    break;
            }
        }
//        NLLog.now("onActivityResult-req:" + requestCode + "/result:" + resultCode);
    }
}
