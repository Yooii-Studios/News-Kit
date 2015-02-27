package com.yooiistudios.newsflow.ui.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.model.AlphaForegroundColorSpan;
import com.yooiistudios.newsflow.model.activitytransition.ActivityTransitionHelper;
import com.yooiistudios.newsflow.model.activitytransition.ActivityTransitionImageViewProperty;
import com.yooiistudios.newsflow.model.activitytransition.ActivityTransitionProperty;
import com.yooiistudios.newsflow.model.activitytransition.ActivityTransitionTextViewProperty;
import com.yooiistudios.newsflow.ui.activity.NewsFeedDetailActivity;
import com.yooiistudios.newsflow.util.Device;
import com.yooiistudios.newsflow.util.ScreenUtils;
import com.yooiistudios.serialanimator.AnimatorListenerImpl;

import java.lang.reflect.Type;

import io.codetail.animation.SupportAnimator;

import static com.yooiistudios.newsflow.ui.activity.MainActivity.INTENT_KEY_TRANSITION_PROPERTY;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 24.
 *
 * NewsFeedDetailTransitionUtils
 *  NewsFeedDetailActivity 의 액티비티 트랜지션 애니메이션을 래핑한 클래스
 */
public class NewsFeedDetailTransitionUtils {
    private static final String SHARED_PREFERENCES_NEWSFEED_DETAIL_TRANSITION
            = "shared_preferences_newsfeed_detail_transition";
    private static final String KEY_USE_SCALED_DURATION = "key_use_scale_duration";

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
    private float mThumbnailWidthScaleRatio;
    private float mThumbnailHeightScaleRatio;

    private boolean mAnimatingTopTitleFadeIn = false;
    private boolean mAnimatingTopDescriptionFadeIn = false;
    private Rect mTopTextLayoutSizeRect = new Rect();
    private Rect mTopTextLayoutLocalVisibleRect;
    private Rect mRecyclerLocalVisibleRect;
    private Rect mTopTitleLocalVisibleRect;
    private Rect mTopDescriptionLocalVisibleRect;

    private TextView mNewsTitleThumbnailTextView;
    private TextView mNewsFeedTitleThumbnailTextView;

    private long mDebugTempDuration;
    private long mRevealAnimationDuration;
    private long mImageFilterAnimationDuration;
    private long mImageScaleAnimationDuration;
    private long mImageTranslationAnimationDuration;
    private long mThumbnailTextAnimationDuration;
    private long mToolbarAnimationDuration;
    private long mToolbarBgAnimationDuration;

    private Toolbar mToolbar;
    private RelativeLayout mRootLayout;
    private FrameLayout mTransitionLayout;
    private View mToolbarOverlayView;
    private View mTopGradientShadowView;
    private View mRevealView;

    // Top
    private FrameLayout mTopNewsImageWrapper;
    private ImageView mTopImageView;
    private LinearLayout mTopTextLayout;
    private TextView mTopTitleTextView;
    private TextView mTopDescriptionTextView;

    // Bottom
    private RecyclerView mBottomNewsListRecyclerView;

    private NewsFeedDetailActivity mActivity;
    private SpannableString mToolbarTitle;
    private AlphaForegroundColorSpan mToolbarTitleColorSpan;

    private NewsFeedDetailTransitionUtils(NewsFeedDetailActivity activity) {
        initViewsAndVariables(activity);
    }

    public static void runEnterAnimation(NewsFeedDetailActivity activity) {
        new NewsFeedDetailTransitionUtils(activity).requestActivityTransition();
    }

    public static void animateTopOverlayFadeOut(NewsFeedDetailActivity activity) {
        new NewsFeedDetailTransitionUtils(activity).fadeOutTopOverlay();
    }

    public static void animateTopOverlayFadeIn(NewsFeedDetailActivity activity) {
        new NewsFeedDetailTransitionUtils(activity).fadeInTopOverlay();
    }

    private void requestActivityTransition() {
        transitAfterViewLocationFix();
    }

    private void transitAfterViewLocationFix() {
        ViewTreeObserver observer = mRootLayout.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRootLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                initTransitionVariablesAfterViewLocationFix();
                prepareViewPropertiesBeforeTransition();

                addThumbnailTextViews();

                startTransition();

                return true;
            }
        });
    }

    private void addThumbnailTextViews() {
        mNewsTitleThumbnailTextView = new TextView(mActivity);
        mNewsFeedTitleThumbnailTextView = new TextView(mActivity);

        addThumbnailTextView(mNewsTitleThumbnailTextView, mTransTitleViewProperty);
        addThumbnailTextView(mNewsFeedTitleThumbnailTextView, mTransFeedTitleViewProperty);
    }

    /**
     * 기본적으로 모든 트랜지션은 트랜지션이 시작되기 직전, 혹은 트랜지션의 시작값으로 초기값을 설정해준다.
     * 그러므로 트랜지션들중 처음부터 시작되지 않는 뷰들의 속성은 여기에서 미리 설정해준다.
     */
    private void prepareViewPropertiesBeforeTransition() {
        mToolbar.setAlpha(0.0f);

        mTransitionLayout.setVisibility(View.VISIBLE);

        mTopTitleTextView.setAlpha(0.0f);
        mTopDescriptionTextView.setAlpha(0.0f);

        mTopTextLayout.setVisibility(View.INVISIBLE);
        mTopTextLayout.getLayoutParams().height = 0;
        mBottomNewsListRecyclerView.setVisibility(View.INVISIBLE);
        mBottomNewsListRecyclerView.getLayoutParams().height = 0;

        saveTopOverlayAlphaState();
        mTopGradientShadowView.setAlpha(0);
        mToolbarOverlayView.setAlpha(0);
    }

    private void startTransition() {
        revealBackground();
        animateThumbnailImageAndTexts();
    }

    private void revealBackground() {
        if (Device.hasLollipop()) {
            revealBackgroundAfterLollipop();
        } else {
            revealBackgroundBeforeLollipop();
        }
    }

    private void animateThumbnailImageAndTexts() {
        translateAndMoveImageWrapper();
//        translateImage();
//        scaleImageWrapper();
        fadeOutHeroImageColorFilter();
        fadeOutThumbnailTexts();
    }

    private void scaleTopNewsTextLayoutHeight() {
        mTopTextLayout.setVisibility(View.VISIBLE);
        ObjectAnimator topNewsTextLayoutHeightAnimator = ObjectAnimator.ofInt(
                this, "topNewsTextLayoutHeight", 0, mTopTextLayoutLocalVisibleRect.height());
        topNewsTextLayoutHeightAnimator.setDuration(mImageScaleAnimationDuration);

        topNewsTextLayoutHeightAnimator.start();
    }

    private void scaleRecyclerHeight() {
        mBottomNewsListRecyclerView.setVisibility(View.VISIBLE);
        ObjectAnimator topNewsTextLayoutHeightAnimator = ObjectAnimator.ofInt(
                this, "recyclerViewHeight", 0, mRecyclerLocalVisibleRect.height());
        topNewsTextLayoutHeightAnimator.setDuration(mDebugTempDuration);

        topNewsTextLayoutHeightAnimator.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealBackgroundAfterLollipop() {
        Animator animator = ViewAnimationUtils.createCircularReveal(
                mRevealView, getRevealCenter().x, getRevealCenter().y, getRevealStartRadius(), getRevealTargetRadius());
        animator.setDuration((int) mRevealAnimationDuration);
        animator.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                animateTopNewsTextAndRecycler();
            }
        });
        animator.start();
    }

    private void revealBackgroundBeforeLollipop() {
        SupportAnimator animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                mRevealView, getRevealCenter().x, getRevealCenter().y, getRevealStartRadius(), getRevealTargetRadius());
        animator.setDuration((int) mRevealAnimationDuration);
        animator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {
            }

            @Override
            public void onAnimationCancel() {
            }

            @Override
            public void onAnimationRepeat() {
            }

            @Override
            public void onAnimationEnd() {
                animateTopNewsTextAndRecycler();
            }
        });
        animator.start();
    }

    private void animateTopNewsTextAndRecycler() {
        scaleTopNewsTextLayoutHeight();
        scaleRecyclerHeight();
    }

    private Point getRevealCenter() {
        Point center = mTransImageViewProperty.getCenter();
        if (!Device.hasLollipop()) {
            center.y -= ScreenUtils.getStatusBarHeight(mActivity.getApplicationContext());
        }

        return center;
    }

    private int getRevealStartRadius() {
        return Math.min(mTransImageViewProperty.getWidth(), mTransImageViewProperty.getHeight()) / 2;
    }

    private int getRevealTargetRadius() {
        return getFarthestLengthFromRevealCenterToRevealCorner();
    }

    private int getFarthestLengthFromRevealCenterToRevealCorner() {
        Point center = getRevealCenter();
        int distanceToRevealViewLeft = center.x - mRevealView.getLeft();
        int distanceToRevealViewTop = center.y - mRevealView.getTop();
        int distanceToRevealViewRight = mRevealView.getRight() - center.x;
        int distanceToRevealViewBottom = mRevealView.getBottom() - center.y;

        int distanceToRevealLeftTop =
                (int)Math.hypot(distanceToRevealViewLeft, distanceToRevealViewTop);
        int distanceToRevealRightTop =
                (int)Math.hypot(distanceToRevealViewRight, distanceToRevealViewTop);
        int distanceToRevealRightBottom =
                (int)Math.hypot(distanceToRevealViewRight, distanceToRevealViewBottom);
        int distanceToRevealLeftBottom =
                (int)Math.hypot(distanceToRevealViewLeft, distanceToRevealViewBottom);

        return getLargestInteger(distanceToRevealLeftTop, distanceToRevealRightTop,
                distanceToRevealRightBottom, distanceToRevealLeftBottom);
    }

    // TODO 유틸 클래스로 extract 해야함.
    private static int getLargestInteger(int... ints) {
        if (ints.length <= 0) {
            throw new IndexOutOfBoundsException(
                    "Parameter MUST contain more than or equal to 1 value.");
        }

        int largestInteger = -1;
        for (int value : ints) {
            largestInteger = value > largestInteger ? value : largestInteger;
        }

        return largestInteger;
    }

    private void translateAndMoveImageWrapper() {
        Rect startRect = new Rect(mThumbnailLeftDelta, mThumbnailTopDelta,
                mThumbnailLeftDelta + mTransImageViewProperty.getWidth(),
                mThumbnailTopDelta + mTransImageViewProperty.getHeight());
        Rect endRect = new Rect(mThumbnailLeftTarget, mThumbnailTopTarget,
                mThumbnailLeftTarget + (int)(mTransImageViewProperty.getWidth() * mThumbnailScaleRatio),
                mThumbnailTopTarget + (int)(mTransImageViewProperty.getHeight() * mThumbnailScaleRatio));

        ObjectAnimator imageWrapperRectAnimator = ObjectAnimator.ofObject(
                this, "imageWrapperRect", new RectEvaluator(new Rect()), startRect, endRect);
        imageWrapperRectAnimator.setDuration(mImageTranslationAnimationDuration);

        imageWrapperRectAnimator.start();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setImageWrapperRect(Rect rect) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)mTopNewsImageWrapper.getLayoutParams();
        lp.leftMargin = rect.left;
        lp.topMargin = rect.top;
        lp.width = rect.width();
        lp.height = rect.height();
        mTopNewsImageWrapper.setLayoutParams(lp);

//        translateTextLayoutAndRecyclerOnTranslateImage(location.x);
    }

    private void translateImage() {
        // TODO leftMargin, topMargin 으로 조절하는 데에 문제가 있는것 같다...
//        mTopNewsImageWrapper.setTranslationX(mThumbnailLeftDelta);
//        mTopNewsImageWrapper.setTranslationY(mThumbnailTopDelta);
//        mTopNewsImageWrapper.animate()
//                .setDuration(mImageTranslationAnimationDuration)
//                .translationX(mThumbnailLeftTarget)
//                .translationY(mThumbnailTopTarget);
        Point startPoint = new Point(mThumbnailLeftDelta, mThumbnailTopDelta);
//        Point endPoint = new Point(mThumbnailLeftTarget, 0);
        Point endPoint = new Point(0, 0);
        ObjectAnimator imageWrapperLocationAnimator = ObjectAnimator.ofObject(
                this, "imageWrapperLocation", new PointEvaluator(new Point()), startPoint, endPoint);
        imageWrapperLocationAnimator.setDuration(mImageTranslationAnimationDuration);

        imageWrapperLocationAnimator.start();
    }

    private void scaleImageWrapper() {
        ObjectAnimator imageWrapperSizeAnimator = ObjectAnimator.ofFloat(
                this, "imageWrapperSize", 1.0f, mThumbnailScaleRatio);
//        PointF startPoint = new PointF(1.0f, 1.0f);
//        PointF endPoint = new PointF(mThumbnailWidthScaleRatio, mThumbnailHeightScaleRatio);
//        ObjectAnimator imageWrapperSizeAnimator = ObjectAnimator.ofObject(
//                this, "imageWrapperSize", new PointFEvaluator(new PointF()), startPoint, endPoint);
        imageWrapperSizeAnimator.setDuration(mImageScaleAnimationDuration);
        imageWrapperSizeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fadeInToolbar();
                fadeInTopOverlay();
            }
        });

        imageWrapperSizeAnimator.start();
    }

//    private void scaleImage() {
//        ObjectAnimator imageSizeAnimator = ObjectAnimator.ofFloat(
//                this, "imageSize", 1.0f, mThumbnailScaleRatio);
////        PointF startPoint = new PointF(1.0f, 1.0f);
////        PointF endPoint = new PointF(mThumbnailWidthScaleRatio, mThumbnailHeightScaleRatio);
////        ObjectAnimator imageWrapperSizeAnimator = ObjectAnimator.ofObject(
////                this, "imageWrapperSize", new PointFEvaluator(new PointF()), startPoint, endPoint);
//        imageWrapperSizeAnimator.setDuration(mImageScaleAnimationDuration);
//
//        imageWrapperSizeAnimator.start();
//    }

    private void fadeInToolbar() {
        mToolbar.animate().alpha(1.0f).setDuration(mToolbarAnimationDuration);
    }

    private void initViewsAndVariables(NewsFeedDetailActivity activity) {
        initVariables(activity);
        initViews();
    }

    private void initVariables(NewsFeedDetailActivity activity) {
        mActivity = activity;
        mToolbarTitle = activity.getToolbarTitle();
        mToolbarTitleColorSpan = activity.getToolbarTitleColorSpan();
    }

    private void initViews() {
        mToolbar = mActivity.getToolbar();
        mRootLayout = mActivity.getRootLayout();
        mTransitionLayout = mActivity.getTransitionLayout();
        mToolbarOverlayView = mActivity.getToolbarOverlayView();
        mTopGradientShadowView = mActivity.getTopGradientShadowView();
        mRevealView = mActivity.getRevealView();

        // Top
        mTopNewsImageWrapper = mActivity.getTopNewsImageWrapper();
        mTopImageView = mActivity.getTopImageView();
        mTopTextLayout = mActivity.getTopNewsTextLayout();
        mTopTitleTextView = mActivity.getTopTitleTextView();
        mTopDescriptionTextView = mActivity.getTopDescriptionTextView();

        // Bottom
        mBottomNewsListRecyclerView = mActivity.getBottomNewsListRecyclerView();
    }

    private void initTransitionVariablesAfterViewLocationFix() {
        // 액티비티 전환 관련 변수
        Bundle extras = mActivity.getIntent().getExtras();

        extractActivityTransitionProperties(extras);
        initImageTranslationVariables();
        initImageScaleVariables();
        initTopTextLayoutVariables();
        mRecyclerLocalVisibleRect = new Rect();
        mBottomNewsListRecyclerView.getGlobalVisibleRect(mRecyclerLocalVisibleRect);

        initDurationVariables();
    }

    private void extractActivityTransitionProperties(Bundle extras) {
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
    }

    private void initImageTranslationVariables() {
        Rect topNewsImageWrapperRect = new Rect();
        mTopNewsImageWrapper.getGlobalVisibleRect(topNewsImageWrapperRect);
        int left = topNewsImageWrapperRect.left;
        int top = topNewsImageWrapperRect.top;

        mThumbnailLeftDelta = mTransImageViewProperty.getLeft() - left;
        mThumbnailTopDelta = mTransImageViewProperty.getTop() - top;
    }

    private void initImageScaleVariables() {
        mThumbnailWidthScaleRatio = mTopNewsImageWrapper.getWidth()/(float)mTransImageViewProperty.getWidth();
        mThumbnailHeightScaleRatio = mTopNewsImageWrapper.getHeight()/(float)mTransImageViewProperty.getHeight();
        boolean fitWidth = mThumbnailWidthScaleRatio > mThumbnailHeightScaleRatio;
        mThumbnailScaleRatio = fitWidth ? mThumbnailWidthScaleRatio : mThumbnailHeightScaleRatio;

        int targetWidth = (int)(mTransImageViewProperty.getWidth() * mThumbnailScaleRatio);
        int imageWrapperLeft = mTopNewsImageWrapper.getLeft();
        mThumbnailLeftTarget = fitWidth
                ? imageWrapperLeft
                : imageWrapperLeft - (targetWidth - mTopNewsImageWrapper.getWidth())/2;
        mThumbnailTopTarget = mTopNewsImageWrapper.getTop();
    }

    private void initTopTextLayoutVariables() {
        mTopTextLayoutLocalVisibleRect = new Rect(
                mTopTextLayout.getLeft(),
                mTopTextLayout.getTop(),
                mTopTextLayout.getRight(),
                mTopTextLayout.getBottom()
        );
        mTopTitleLocalVisibleRect = new Rect(
                mTopTitleTextView.getLeft(),
                mTopTitleTextView.getTop(),
                mTopTitleTextView.getRight(),
                mTopTitleTextView.getBottom()
        );
        mTopDescriptionLocalVisibleRect = new Rect(
                mTopDescriptionTextView.getLeft(),
                mTopDescriptionTextView.getTop(),
                mTopDescriptionTextView.getRight(),
                mTopDescriptionTextView.getBottom()
        );
    }

    private void initDurationVariables() {
        // 애니메이션 속도 관련 변수
        Resources resources = mActivity.getResources();

        int animatorScale = isUseScaledDurationDebug(mActivity.getApplicationContext()) ?
                resources.getInteger(R.integer.news_feed_detail_debug_transition_scale) : 1;

        mRevealAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_reveal_duration_milli) * animatorScale;
        mImageFilterAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_image_filter_duration_milli) * animatorScale;
        mImageScaleAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_image_scale_duration_milli) * animatorScale;
        mImageTranslationAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_image_translation_duration_milli) * animatorScale;

        mThumbnailTextAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_thumbnail_text_duration_milli) * animatorScale;

        mToolbarAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_toolbar_duration_milli) * animatorScale;
        mToolbarBgAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_toolbar_bg_duration_milli) * animatorScale;
        
        mDebugTempDuration = mImageScaleAnimationDuration;
    }

    private void fadeOutHeroImageColorFilter() {
        int filterColor = mActivity.getFilterColor();

        int red = Color.red(filterColor);
        int green = Color.green(filterColor);
        int blue = Color.blue(filterColor);
        int argb = Color.argb(Color.alpha(filterColor), red, green, blue);
        ImageFilterAnimator.animate(mTopImageView, argb, 0, mImageFilterAnimationDuration);
    }

    private void fadeOutThumbnailTexts() {
        // 뉴스 타이틀 썸네일 텍스트뷰 애니메이션
        mNewsTitleThumbnailTextView.animate()
                .alpha(0.0f)
                .setDuration(mThumbnailTextAnimationDuration)
                .start();

        // 뉴스 피드 타이틀 썸네일 텍스트뷰 애니메이션
        mNewsFeedTitleThumbnailTextView.animate()
                .alpha(0.0f)
                .setDuration(mThumbnailTextAnimationDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mTransitionLayout.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void addThumbnailTextView(TextView textView,
                                      ActivityTransitionTextViewProperty textViewProperty) {
        int padding = textViewProperty.getPadding();

        // 뉴스 타이틀 썸네일 뷰 추가
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(textViewProperty.getText());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textViewProperty.getTextSize());
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
        if (!Device.hasLollipop()) {
            lp.topMargin -= ScreenUtils.getStatusBarHeight(mActivity.getApplicationContext());
        }
        mTransitionLayout.addView(view, lp);
    }

    private void fadeInTopOverlay() {
        if (mTopGradientShadowView.getTag() == null || mToolbarOverlayView.getTag() == null
                || mTopGradientShadowView.getAlpha() > 0 || mToolbarOverlayView.getAlpha() > 0) {
            return;
        }
        mTopGradientShadowView.animate()
                .setDuration(mToolbarBgAnimationDuration)
                .alpha((Float) mTopGradientShadowView.getTag())
                .setInterpolator(new DecelerateInterpolator());
        mToolbarOverlayView.animate()
                .setDuration(mToolbarBgAnimationDuration)
                .alpha((Float) mToolbarOverlayView.getTag())
                .setInterpolator(new DecelerateInterpolator());
    }

    private void fadeOutTopOverlay() {
        saveTopOverlayAlphaState();
        mTopGradientShadowView.animate()
                .setDuration(mToolbarBgAnimationDuration)
                .alpha(0f)
                .setInterpolator(new DecelerateInterpolator());
        mToolbarOverlayView.animate()
                .setDuration(mToolbarBgAnimationDuration)
                .alpha(0f)
                .setInterpolator(new DecelerateInterpolator());
    }

    private void saveTopOverlayAlphaState() {
        mTopGradientShadowView.setTag(mTopGradientShadowView.getAlpha());
        mToolbarOverlayView.setTag(mToolbarOverlayView.getAlpha());
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setImageWrapperLocation(Point location) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)mTopNewsImageWrapper.getLayoutParams();
        lp.leftMargin = location.x;
        lp.topMargin = location.y;
        mTopNewsImageWrapper.setLayoutParams(lp);

        translateTextLayoutAndRecyclerOnTranslateImage(location.x);
    }

    private void translateTextLayoutAndRecyclerOnTranslateImage(int leftMargin) {
        ViewGroup.MarginLayoutParams textLayoutLp = (ViewGroup.MarginLayoutParams) mTopTextLayout.getLayoutParams();
        textLayoutLp.leftMargin = leftMargin;
        mTopTextLayout.setLayoutParams(textLayoutLp);
        ViewGroup.MarginLayoutParams recyclerLp = (ViewGroup.MarginLayoutParams)mBottomNewsListRecyclerView.getLayoutParams();
        recyclerLp.leftMargin = leftMargin;
        mBottomNewsListRecyclerView.setLayoutParams(recyclerLp);
    }

    /**
     * runEnterAnimation 에서 이미지뷰 wrapper 스케일링에 사용될 메서드.
     * @param scale 계산된 사이즈
     */
//    @SuppressWarnings("UnusedDeclaration")
//    public void setImageWrapperSize(PointF scale) {
//        ViewGroup.LayoutParams lp = mTopNewsImageWrapper.getLayoutParams();
//        lp.width = (int)(mTransImageViewProperty.getWidth() * scale.x);
//        lp.height = (int)(mTransImageViewProperty.getHeight() * scale.y);
//        mTopNewsImageWrapper.setLayoutParams(lp);
//
//        NLLog.now("scale : " + scale.toString());
//
//        scaleTextLayoutAndRecyclerWidthOnScaleImage();
//    }
    @SuppressWarnings("UnusedDeclaration")
    public void setImageWrapperSize(float scale) {
        ViewGroup.LayoutParams lp = mTopNewsImageWrapper.getLayoutParams();
        lp.width = (int)(mTransImageViewProperty.getWidth() * scale);
        lp.height = (int)(mTransImageViewProperty.getHeight() * scale);
        mTopNewsImageWrapper.setLayoutParams(lp);

        scaleTextLayoutAndRecyclerWidthOnScaleImage();
    }

    private void scaleTextLayoutAndRecyclerWidthOnScaleImage() {
        ViewGroup.LayoutParams lp = mTopNewsImageWrapper.getLayoutParams();

        ViewGroup.LayoutParams textLayoutLp = mTopTextLayout.getLayoutParams();
        textLayoutLp.width = lp.width;
        mTopTextLayout.setLayoutParams(textLayoutLp);
        ViewGroup.LayoutParams recyclerLp = mBottomNewsListRecyclerView.getLayoutParams();
        recyclerLp.width = lp.width;
        mBottomNewsListRecyclerView.setLayoutParams(recyclerLp);

        animateTopTitleAndDescriptionIfSizeSufficient();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setTopNewsTextLayoutHeight(int height) {
        ViewGroup.LayoutParams lp = mTopTextLayout.getLayoutParams();
        lp.height = height;
        mTopTextLayout.setLayoutParams(lp);

        animateTopTitleAndDescriptionIfSizeSufficient();
    }

    private void updateTopTextLayoutSizeRect() {
        ViewGroup.LayoutParams lp = mTopTextLayout.getLayoutParams();
        mTopTextLayoutSizeRect.right = lp.width;
        mTopTextLayoutSizeRect.bottom = lp.height;
    }

    private void animateTopTitleAndDescriptionIfSizeSufficient() {
        updateTopTextLayoutSizeRect();

        if (readyToAnimateTopTitle()) {
            fadeInTopTitle();
        }
        if (readyToAnimateTopDescription()) {
            fadeInTopDescription();
        }
    }

    private void fadeInTopTitle() {
        mAnimatingTopTitleFadeIn = true;

//        mTopTitleTextView.setAlpha(0.0f);
        mTopTitleTextView.animate()
                .setDuration(mDebugTempDuration)
                .alpha(1.0f);
    }

    private void fadeInTopDescription() {
        mAnimatingTopDescriptionFadeIn = true;

        mTopDescriptionTextView.animate()
                .setDuration(mDebugTempDuration)
                .alpha(1.0f);
    }

    private boolean readyToAnimateTopTitle() {
        return !mAnimatingTopTitleFadeIn
                && mTopTextLayout.getVisibility() == View.VISIBLE
                && mTopTextLayoutSizeRect.contains(mTopTitleLocalVisibleRect);
    }

    private boolean readyToAnimateTopDescription() {
        return !mAnimatingTopDescriptionFadeIn
                && mTopTextLayout.getVisibility() == View.VISIBLE
                && mTopTextLayoutSizeRect.contains(mTopDescriptionLocalVisibleRect);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRecyclerViewHeight(int height) {
        ViewGroup.LayoutParams lp = mBottomNewsListRecyclerView.getLayoutParams();
        lp.height = height;
        mBottomNewsListRecyclerView.setLayoutParams(lp);
    }

    /**
     * runEnterAnimation 에서 액션바 타이틀 알파값 애니메이션에 사용될 메서드.
     * @param value 계산된 알파값
     */
    @SuppressWarnings("UnusedDeclaration")
    private void setToolbarTitleAlpha(float value) {
        mToolbarTitleColorSpan.setAlpha(value);
        mToolbarTitle.setSpan(mToolbarTitleColorSpan, 0, mToolbarTitle.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (mActivity.getSupportActionBar() != null) {
            mActivity.getSupportActionBar().setTitle(mToolbarTitle);
        }
    }

    public static void toggleUseScaledDurationDebug(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                SHARED_PREFERENCES_NEWSFEED_DETAIL_TRANSITION, Context.MODE_PRIVATE);
        boolean useScaledDuration = prefs.getBoolean(KEY_USE_SCALED_DURATION, false);
        prefs.edit().putBoolean(KEY_USE_SCALED_DURATION, !useScaledDuration).apply();
    }

    public static boolean isUseScaledDurationDebug(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                SHARED_PREFERENCES_NEWSFEED_DETAIL_TRANSITION, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_USE_SCALED_DURATION, false);
    }

//    /**
//     * runEnterAnimation 에서 루트뷰 clip bound 위치 조절에 사용될 메서드.
//     * @param newLoc 계산된 위치
//     */
//    @SuppressWarnings("UnusedDeclaration")
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    public void setRootClipBoundTranslation(PathPoint newLoc) {
//        mRootClipBound.left = (int)newLoc.mX;
//        mRootClipBound.top = (int)newLoc.mY;
//        mRootLayout.setClipBounds(mRootClipBound);
//    }

//    /**
//     * runEnterAnimation 에서 루트뷰 clip bound 스케일링에 사용될 메서드.
//     * @param scale 계산된 사이즈
//     */
//    @SuppressWarnings("UnusedDeclaration")
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    public void setRootClipBoundSize(PointF scale) {
//        mRootClipBound.right = mRootClipBound.left +
//                (int)(mTransImageViewProperty.getWidth() * scale.x);
//        mRootClipBound.bottom = mRootClipBound.top +
//                (int)(mTransImageViewProperty.getHeight() * scale.y);
//        mRootLayout.setClipBounds(mRootClipBound);
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    public void setRootVerticalClipBoundSize(int height) {
//        mRootClipBound.bottom = mRootClipBound.top + height;
//        mRootLayout.setClipBounds(mRootClipBound);
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    public void setRootWidthClipBoundSize(float scale) {
//        mRootClipBound.right = mRootClipBound.left +
//                (int)(mTransImageViewProperty.getWidth() * scale);
//        mRootLayout.setClipBounds(mRootClipBound);
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    public void setRootHeightClipBoundSize(float scale) {
//        mRootClipBound.bottom = mRootClipBound.top +
//                (int)(mTransImageViewProperty.getHeight() * scale);
//        NLLog.now("mRootClipBound.top : " + mRootClipBound.top);
//        NLLog.now("mRootClipBound.bottom : " + mRootClipBound.bottom);
//        NLLog.now("mRootClipBound.height() : " + (mRootClipBound.bottom - mRootClipBound.top));
//        mRootLayout.setClipBounds(mRootClipBound);
//    }

//    /**
//     * runEnterAnimation 에서 이미지뷰 wrapper 위치 이동에 사용될 메서드.
//     * @param newLoc 계산된 위치
//     */
//    @SuppressWarnings("UnusedDeclaration")
//    public void setImageWrapperTranslation(PathPoint newLoc) {
//        mTopNewsImageWrapper.setTranslationX((int) newLoc.mX);
//        mTopNewsImageWrapper.setTranslationY((int) newLoc.mY);
//    }

//    @SuppressWarnings("UnusedDeclaration")
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    public void setRootLayoutClipBound(Rect clipBound) {
//        mRootLayout.setClipBounds(clipBound);
//    }

//    private void _startTransitionAnimation() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mTopNewsImageWrapper.bringToFront();
//
//            ViewTreeObserver observer = mRootLayout.getViewTreeObserver();
//            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//
//                @Override
//                public boolean onPreDraw() {
//                    mRootLayout.getViewTreeObserver().removeOnPreDrawListener(this);
//
//                    initTransitionVariablesAfterViewLocationFix();
//
//                    mNewsTitleThumbnailTextView = new TextView(NewsFeedDetailActivity.this);
//                    mNewsFeedTitleThumbnailTextView = new TextView(NewsFeedDetailActivity.this);
//
//                    addThumbnailTextView(mNewsTitleThumbnailTextView, mTransTitleViewProperty);
//                    addThumbnailTextView(mNewsFeedTitleThumbnailTextView,
//                            mTransFeedTitleViewProperty);
//
//                    runEnterAnimation();
//                    return true;
//                }
//            });
//        }
//    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public void runEnterAnimation() {
//        mIsAnimatingActivityTransitionAnimation = true;
//
//        final PathInterpolator commonInterpolator = new PathInterpolator(.4f, .0f, 1.f, .2f);
//
//        // 애니메이션 실행 전 텍스트뷰, 리사이클러뷰, 배경 안보이게 해두기
////        mTopTextLayout.setAlpha(0);
////        mBottomNewsListRecyclerView.setAlpha(0);
////        mRootLayoutBackground.setAlpha(0);
//
//        // 루트 뷰, 이미지뷰의 위치 곡선 정의
//        AnimatorPath imageTranslationPath = new AnimatorPath();
//        imageTranslationPath.moveTo(mThumbnailLeftDelta, mThumbnailTopDelta);
////        imageTranslationPath.curveTo(
////                mThumbnailLeftDelta/2, mThumbnailTopDelta, 0, mThumbnailTopDelta/2,
////                mThumbnailTranslationTargetX, mThumbnailTranslationYDelta);
//        imageTranslationPath.lineTo(mThumbnailTranslationTargetX, mThumbnailTranslationYDelta);
//
//        ObjectAnimator rootClipBoundTranslationAnimator = ObjectAnimator.ofObject(NewsFeedDetailActivity.this,
//                "rootClipBoundTranslation", new PathEvaluator(), imageTranslationPath.getPoints().toArray());
//        rootClipBoundTranslationAnimator.setInterpolator(AnimationFactory.makeNewsFeedImageAndRootTransitionInterpolator(getApplicationContext()));
//        rootClipBoundTranslationAnimator.setDuration(mImageTranslationAnimationDuration);
//        rootClipBoundTranslationAnimator.start();
//
////        PropertyValuesHolder rootClipBoundTranslationPvh = PropertyValuesHolder.ofObject(
////                "RootClipBoundTranslation", new PathEvaluator(), imageTranslationPath.getPoints().toArray());
//
////        PropertyValuesHolder rootClipBoundTranslationPvh = PropertyValuesHolder.ofObject(
////                "RootClipBoundTranslation", new TypeEvaluator<Point>() {
////                    @Override
////                    public Point evaluate(float v, Point startPoint, Point endPoint) {
////                        return new Point((int)(startPoint.x * (1 - v) + endPoint.x * v),
////                                (int)(startPoint.y * (1 - v) + endPoint.y * v));
////                    }
////                },
////                new Point(mThumbnailLeftDelta, mThumbnailTopDelta),
////                new Point(mThumbnailTranslationTargetX,
////                        mThumbnailTranslationYDelta + (int)(mTopImageView.getHeight() * 0.1)));
//
//        // 루트 뷰의 외곽 크기 조절
//        mRootClipBound = new Rect(
//                mTransImageViewProperty.getLeft(),
//                mTransImageViewProperty.getTop(),
//                mTransImageViewProperty.getLeft() + mTransImageViewProperty.getWidth(),
//                mTransImageViewProperty.getTop() + mTransImageViewProperty.getHeight());
//        mRootLayout.setClipToOutline(true);
//        mRootLayout.setClipBounds(mRootClipBound);
//
//        ObjectAnimator rootClipBoundHorizontalSizeAnimator =
//                ObjectAnimator.ofObject(NewsFeedDetailActivity.this, "rootWidthClipBoundSize", new TypeEvaluator<Float>() {
//                    @Override
//                    public Float evaluate(float v, Float startScale, Float endScale) {
//                        return startScale * (1 - v) + endScale * v;
//                    }
//                }, 1.f, mThumbnailScaleRatio);
//        rootClipBoundHorizontalSizeAnimator.setInterpolator(AnimationFactory.makeNewsFeedRootBoundHorizontalInterpolator(getApplicationContext()));
//        rootClipBoundHorizontalSizeAnimator.setDuration(mRootViewHorizontalScaleAnimationDuration); // 530
//        rootClipBoundHorizontalSizeAnimator.start();
//
//        ObjectAnimator rootClipBoundVerticalSizeAnimator =
//                ObjectAnimator.ofInt(NewsFeedDetailActivity.this, "rootVerticalClipBoundSize",
//                        mTransImageViewProperty.getHeight(), mRootLayout.getHeight());
//        rootClipBoundVerticalSizeAnimator.setInterpolator(AnimationFactory.makeNewsFeedRootBoundVerticalInterpolator(getApplicationContext()));
//        rootClipBoundVerticalSizeAnimator.setDuration(mRootViewVerticalScaleAnimationDuration); //
//        // 530
//        rootClipBoundVerticalSizeAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//
//                mTopTextLayout.bringToFront();
//                mBottomNewsListRecyclerView.bringToFront();
//                mLoadingCoverView.bringToFront();
//
//                mIsAnimatingActivityTransitionAnimation = false;
//            }
//        });
//        rootClipBoundVerticalSizeAnimator.start();
//
//
////        PropertyValuesHolder rootClipBoundSizePvh = PropertyValuesHolder.ofObject(
////                "RootClipBoundSize", new TypeEvaluator<PointF>() {
////                    @Override
////                    public PointF evaluate(float v, PointF startScale, PointF endScale) {
////                        return new PointF(startScale.x * (1 - v) + endScale.x * v,
////                                startScale.y * (1 - v) + endScale.y * v);
////                    }
////                },
////                new PointF(1.0f, 1.0f),
////                new PointF(mThumbnailScaleRatio, mThumbnailScaleRatio * 0.6f));
//
//        // 곡선 이동 PropertyValuesHolder 준비
//        mTopNewsImageWrapper.setPivotX(0);
//        mTopNewsImageWrapper.setPivotY(0);
//        ObjectAnimator imageWrapperTranslationAnimator = ObjectAnimator.ofObject(
//                NewsFeedDetailActivity.this, "ImageWrapperTranslation", new PathEvaluator(),
//                imageTranslationPath.getPoints().toArray());
//        imageWrapperTranslationAnimator.setInterpolator(AnimationFactory.makeNewsFeedImageAndRootTransitionInterpolator(getApplicationContext()));
//        imageWrapperTranslationAnimator.setDuration(mImageTranslationAnimationDuration);
//        imageWrapperTranslationAnimator.start();
//
//        // 크기 변경 PropertyValuesHolder 준비
//        ViewGroup.LayoutParams lp = mTopNewsImageWrapper.getLayoutParams();
//        lp.width = mTransImageViewProperty.getWidth();
//        lp.height = mTransImageViewProperty.getHeight();
//        mTopNewsImageWrapper.setLayoutParams(lp);
//
//        ObjectAnimator imageWrapperSizeAnimator = ObjectAnimator.ofFloat(
//                NewsFeedDetailActivity.this, "ImageWrapperSize", 1.0f, mThumbnailScaleRatio);
//        imageWrapperSizeAnimator.setInterpolator(
//                AnimationFactory.makeNewsFeedImageScaleInterpolator(getApplicationContext()));
//        imageWrapperSizeAnimator.setDuration(mImageScaleAnimationDuration);
//        imageWrapperSizeAnimator.start();
//
//        // 준비해 놓은 PropertyValuesHolder들 실행
////        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(NewsFeedDetailActivity.this,
////                imageWrapperTranslationPvh, imageWrapperSizePvh
////        );
////        animator.setInterpolator(AnimationFactory.makeNewsFeedImageAndRootTransitionInterpolator());
////        animator.setDuration(mImageAnimationDuration);
//        imageWrapperSizeAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//
//                // 툴바 텍스트 페이드인
//                ObjectAnimator toolbarTitleAnimator = ObjectAnimator.ofFloat(
//                        NewsFeedDetailActivity.this, "toolbarTitleAlpha", 0.0f, 1.0f);
//                toolbarTitleAnimator.setDuration(mToolbarAnimationDuration);
//                toolbarTitleAnimator.setInterpolator(commonInterpolator);
//                toolbarTitleAnimator.start();
//
//                // 액션바 아이콘 페이드인
//                ObjectAnimator toolbarHomeIconAnimator =
//                        ObjectAnimator.ofInt(mToolbarHomeIcon, "alpha", 0, 255);
//                toolbarHomeIconAnimator.setDuration(mToolbarAnimationDuration);
//                toolbarHomeIconAnimator.setInterpolator(commonInterpolator);
//                toolbarHomeIconAnimator.start();
//
//                ObjectAnimator toolbarOverflowIconAnimator =
//                        ObjectAnimator.ofInt(mToolbarOverflowIcon, "alpha", 0, 255);
//                toolbarOverflowIconAnimator.setDuration(mToolbarAnimationDuration);
//                toolbarOverflowIconAnimator.setInterpolator(commonInterpolator);
//                toolbarOverflowIconAnimator.start();
//
//                fadeInTopOverlay();
//            }
//        });
//        /*
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            private boolean mHasAnimated = false;
//
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                float elapsedValue = valueAnimator.getAnimatedFraction();
//
//                if (elapsedValue > 0.7) {
//                    if (mHasAnimated) {
//                        return;
//                    }
//                    mHasAnimated = true;
//
//                    mTopTextLayout.setAlpha(1);
//                    mBottomNewsListRecyclerView.setAlpha(1);
//                    mRootLayoutBackground.setAlpha(1);
//
//                    Rect rootLayoutOriginalBound = new Rect(
//                            mRootLayout.getLeft(), mRootLayout.getTop(),
//                            mRootLayout.getRight(), mRootLayout.getBottom());
//
//                    ObjectAnimator clipBoundAnimator = ObjectAnimator.ofObject(
//                            NewsFeedDetailActivity.this, "RootLayoutClipBound",
//                            new TypeEvaluator<Rect>() {
//                                @Override
//                                public Rect evaluate(float v, Rect startRect, Rect endRect) {
//                                    Rect rect = new Rect();
//                                    rect.left = (int) (startRect.left * (1 - v) + endRect.left * v);
//                                    rect.top = (int) (startRect.top * (1 - v) + endRect.top * v);
//                                    rect.right = (int) (startRect.right * (1 - v) + endRect.right * v);
//                                    rect.bottom = (int) (startRect.bottom * (1 - v) + endRect.bottom * v);
//                                    return rect;
//                                }
//                            }, mRootClipBound, rootLayoutOriginalBound);
//                    clipBoundAnimator.setInterpolator(
//                            AnimationFactory.makeNewsFeedRootBoundHorizontalInterpolator());
//                    clipBoundAnimator.setDuration((int)(mDebugAnimDuration * 1.5));
//                    clipBoundAnimator.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
//
//                            mIsAnimatingActivityTransitionAnimation = false;
//                        }
//                    });
//                    clipBoundAnimator.start();
//                }
//            }
//        });
//        */
////        animator.start();
//
//        // 이미지뷰 컬러 필터 애니메이션
//        fadeOutHeroImageColorFilter();
//
//        // 상단 뉴스의 텍스트뷰 위치, 알파 애니메이션
////        Point displaySize = new Point();
////        getWindowManager().getDefaultDisplay().getSize(displaySize);
////        final int translationY = displaySize.y - mTopTextLayout.getTop();
////        mTopTextLayout.setAlpha(0);
////        mTopTextLayout.setTranslationY(translationY);
////        mTopTextLayout.animate().
////                alpha(1.0f).
////                translationY(0).
////                setDuration(mDebugAnimDuration).
////                setInterpolator(pathInterpolator).
////                start();
//
//        // 하단 리사이클러뷰 아이템들 위치 애니메이션
////        ViewTreeObserver observer = mBottomNewsListRecyclerView.getViewTreeObserver();
////        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
////            @Override
////            public boolean onPreDraw() {
////                mBottomNewsListRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
////
////                // animate bottom recycler view
////                for (int i = 0; i < mBottomNewsListRecyclerView.getChildCount(); i++) {
////                    View child = mBottomNewsListRecyclerView.getChildAt(i);
////
////                    child.setTranslationY(child.getTop() + child.getBottom());
////                    child.animate().
////                            translationY(0).
////                            setStartDelay(i*100).
////                            setDuration(mDebugAnimDuration).
////                            setInterpolator(pathInterpolator).
////                            start();
////                }
////
////                return true;
////            }
////        });
//
//        // 배경 색상 페이드인 애니메이션
////        mRootLayoutBackground.setAlpha(0);
////        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mRootLayoutBackground, "alpha", 0, 255);
////        bgAnim.setDuration(mDebugAnimDuration);
////        bgAnim.setInterpolator(pathInterpolator);
////        bgAnim.start();
////
////        mRecyclerViewBackground.setAlpha(0);
////        ObjectAnimator recyclerBgAnim = ObjectAnimator.ofInt(mRecyclerViewBackground, "alpha", 0,
////                255);
////        recyclerBgAnim.setDuration(mDebugAnimDuration);
////        recyclerBgAnim.setInterpolator(pathInterpolator);
////        recyclerBgAnim.start();
//
//        // 뉴스 타이틀 썸네일 텍스트뷰 애니메이션
//        mNewsTitleThumbnailTextView.setAlpha(0.0f);
//        ViewPropertyAnimator thumbnailAlphaAnimator = mNewsTitleThumbnailTextView.animate();
//        thumbnailAlphaAnimator.alpha(0.0f);
//        thumbnailAlphaAnimator.setDuration(mThumbnailTextAnimationDuration);
//        thumbnailAlphaAnimator.setInterpolator(commonInterpolator);
//        thumbnailAlphaAnimator.start();
//
//        // 뉴스 피드 타이틀 썸네일 텍스트뷰 애니메이션
//        mNewsFeedTitleThumbnailTextView.setAlpha(0.0f);
//        ViewPropertyAnimator feedTitleThumbnailAlphaAnimator
//                = mNewsFeedTitleThumbnailTextView.animate();
//        feedTitleThumbnailAlphaAnimator.alpha(0.0f);
//        feedTitleThumbnailAlphaAnimator.setDuration(mThumbnailTextAnimationDuration);
//        feedTitleThumbnailAlphaAnimator.setInterpolator(commonInterpolator);
//        feedTitleThumbnailAlphaAnimator.start();
//
//        // 탑 뉴스 텍스트(타이틀, 디스크립션) 애니메이션
//        fadeInTopTitle();
//
//        // 액션바 내용물 우선 숨겨두도록
//        mToolbarHomeIcon.setAlpha(0);
//        mColorSpan.setAlpha(0.0f);
//        mToolbarOverflowIcon.setAlpha(0);
//
//        //　액션바 배경, 오버레이 페이드 인
//        saveTopOverlayAlphaState();
//        mTopGradientShadowView.setAlpha(0);
//        mToolbarOverlayView.setAlpha(0);
//    }

//    @SuppressWarnings("UnusedDeclaration")
//    private void runExitAnimation() {
//        mIsAnimatingActivityTransitionAnimation = true;
//
//        final TimeInterpolator interpolator = AnimationFactory.makeNewsFeedReverseTransitionInterpolator();
//
//        // 곡선 이동 PropertyValuesHolder 준비
//        AnimatorPath path = new AnimatorPath();
//        path.moveTo(mThumbnailTranslationTargetX, mThumbnailTranslationYDelta);
//        path.curveTo(0, mThumbnailTopDelta /2, mThumbnailLeftDelta /2, mThumbnailTopDelta,
//                mThumbnailLeftDelta, mThumbnailTopDelta);
//
//        PropertyValuesHolder imageWrapperTranslationPvh = PropertyValuesHolder.ofObject(
//                "ImageWrapperTranslation", new PathEvaluator(), path.getPoints().toArray());
//
//        // 크기 변경 PropertyValuesHolder 준비
//        ViewGroup.LayoutParams lp = mTopNewsImageWrapper.getLayoutParams();
//        lp.width = mTransImageViewProperty.getWidth();
//        lp.height = mTransImageViewProperty.getHeight();
//        mTopNewsImageWrapper.setLayoutParams(lp);
//
//        PropertyValuesHolder imageWrapperSizePvh = PropertyValuesHolder.ofFloat(
//                "ImageWrapperSize", mThumbnailScaleRatio, 1.0f);
//
//        // 준비해 놓은 PropertyValuesHolder들 실행
//        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(NewsFeedDetailActivity.this,
//                imageWrapperTranslationPvh, imageWrapperSizePvh
//        );
//        animator.setInterpolator(interpolator);
//        animator.setDuration(mExitAnimationDuration);
//        animator.start();
//
//        // 이미지뷰 컬러 필터 애니메이션
//        fadeInHeroImageColorFilter();
//
//        // 상단 뉴스의 텍스트뷰 위치, 알파 애니메이션
//        Point displaySize = new Point();
//        getWindowManager().getDefaultDisplay().getSize(displaySize);
//        final int translationY = displaySize.y - mTopTextLayout.getTop();
//        mTopTextLayout.animate().
//                alpha(0.0f).
//                translationYBy(translationY).
//                setDuration(mExitAnimationDuration).
//                setInterpolator(interpolator).
//                start();
//
//        // 하단 리사이클러뷰 위치 애니메이션
//        mBottomNewsListRecyclerView.animate().
//                translationYBy(displaySize.y - mBottomNewsListRecyclerView.getTop() + mWindowInsetEnd).
//                setDuration(mExitAnimationDuration).
//                setInterpolator(interpolator).
//                withEndAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        NewsFeedDetailActivity.super.finish();
//                        overridePendingTransition(0, 0);
//                    }
//                }).
//                start();
//
//        // 배경 색상 페이드인 애니메이션
////        mRootLayoutBackground.setAlpha(1);
//        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mRootLayoutBackground, "alpha", 255, 0);
//        bgAnim.setDuration(mExitAnimationDuration);
//        bgAnim.setInterpolator(interpolator);
//        bgAnim.start();
//
////        mRecyclerViewBackground.setAlpha(1);
//        ObjectAnimator recyclerBgAnim = ObjectAnimator.ofInt(mRecyclerViewBackground, "alpha", 255,
//                0);
//        recyclerBgAnim.setDuration(mExitAnimationDuration);
//        recyclerBgAnim.setInterpolator(interpolator);
//        recyclerBgAnim.start();
//
//        // 뉴스 타이틀 썸네일 텍스트뷰 애니메이션
//        mNewsTitleThumbnailTextView.setAlpha(0.0f);
//        ViewPropertyAnimator thumbnailAlphaAnimator = mNewsTitleThumbnailTextView.animate();
//        thumbnailAlphaAnimator.alpha(1.0f);
//        thumbnailAlphaAnimator.setDuration(mExitAnimationDuration/2);
//        thumbnailAlphaAnimator.setInterpolator(interpolator);
//        thumbnailAlphaAnimator.start();
//
//        // 뉴스 피드 타이틀 썸네일 텍스트뷰 애니메이션
//        mNewsFeedTitleThumbnailTextView.setAlpha(0.0f);
//        ViewPropertyAnimator feedTitleThumbnailAlphaAnimator =
//                mNewsFeedTitleThumbnailTextView.animate();
//        feedTitleThumbnailAlphaAnimator.alpha(1.0f);
//        feedTitleThumbnailAlphaAnimator.setDuration(mExitAnimationDuration/2);
//        feedTitleThumbnailAlphaAnimator.setInterpolator(interpolator);
//        feedTitleThumbnailAlphaAnimator.start();
//
//        // 툴바 홈버튼 페이드인
////        mToolbarHomeIcon.setAlpha(1);
//        ObjectAnimator toolbarHomeIconAnimator =
//                ObjectAnimator.ofInt(mToolbarHomeIcon, "alpha", 255, 0);
//        toolbarHomeIconAnimator.setDuration(mExitAnimationDuration);
//        toolbarHomeIconAnimator.setInterpolator(interpolator);
//        toolbarHomeIconAnimator.start();
//
//        // 툴바 텍스트 페이드인
////        mColorSpan.setAlpha(1.0f);
//        ObjectAnimator toolbarTitleAnimator = ObjectAnimator.ofFloat(
//                NewsFeedDetailActivity.this, "ActionBarTitleAlpha", 1.0f, 0.0f);
//        toolbarTitleAnimator.setDuration(mExitAnimationDuration);
//        toolbarTitleAnimator.setInterpolator(interpolator);
//        toolbarTitleAnimator.start();
//
//        // 툴바 오버플로우 페이드인
////        mToolbarOverflowIcon.setAlpha(1);
//        ObjectAnimator toolbarOverflowIconAnimator =
//                ObjectAnimator.ofInt(mToolbarOverflowIcon, "alpha", 255, 0);
//        toolbarOverflowIconAnimator.setDuration(mExitAnimationDuration);
//        toolbarOverflowIconAnimator.setInterpolator(interpolator);
//        toolbarOverflowIconAnimator.start();
//
//        //　툴바 배경, 오버레이 페이드 인
//        fadeOutTopOverlay();
//    }
}
