package com.yooiistudios.newskit.ui.animation;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.ui.animation.activitytransition.ActivityTransitionHelper;
import com.yooiistudios.newskit.core.ui.animation.activitytransition.ActivityTransitionImageViewProperty;
import com.yooiistudios.newskit.core.ui.animation.activitytransition.ActivityTransitionProperty;
import com.yooiistudios.newskit.core.ui.animation.activitytransition.ActivityTransitionTextViewProperty;
import com.yooiistudios.newskit.core.util.Device;
import com.yooiistudios.newskit.core.util.Display;
import com.yooiistudios.newskit.core.util.IntegerMath;
import com.yooiistudios.newskit.model.AlphaForegroundColorSpan;
import com.yooiistudios.newskit.ui.activity.NewsFeedDetailActivity;
import com.yooiistudios.serialanimator.AnimatorListenerImpl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import io.codetail.animation.SupportAnimator;

import static com.yooiistudios.newskit.ui.activity.MainActivity.INTENT_KEY_TRANSITION_PROPERTY;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 24.
 *
 * NewsFeedDetailTransitionUtils
 *  NewsFeedDetailActivity 의 액티비티 트랜지션 애니메이션을 래핑한 클래스
 */
public class NewsFeedDetailTransitionUtils {
    public interface OnAnimationEndListener {
        void onTransitionEnd();
    }

    private static final String SHARED_PREFERENCES_NEWSFEED_DETAIL_TRANSITION
            = "shared_preferences_newsfeed_detail_transition";
    private static final String KEY_USE_SCALED_DURATION = "key_use_scale_duration";

    private static final String ANIM_TOOLBAR = "anim_toolbar";
    private static final String ANIM_TOP_OVERLAY = "anim_top_overlay";
    private static final String ANIM_COLOR_FILTER = "anim_color_filter";
    private static final String ANIM_THUMBNAIL_TEXT = "anim_thumbnail_text";
    private static final String ANIM_TOP_TITLE = "anim_top_title";
    private static final String ANIM_TOP_DESCRIPTION = "anim_top_description";
    private static final String ANIM_RECYCLER_TITLE = "anim_recycler_title";
    private static final String ANIM_RECYCLER_DESCRIPTION = "anim_recycler_description";
    private static final String[] TRACKING_ANIMATIONS = {
            ANIM_TOOLBAR,
            ANIM_TOP_OVERLAY,
            ANIM_COLOR_FILTER,
            ANIM_THUMBNAIL_TEXT,
            ANIM_TOP_TITLE,
            ANIM_TOP_DESCRIPTION,
            ANIM_RECYCLER_TITLE,
            ANIM_RECYCLER_DESCRIPTION
    };

    /**
     * Thumbnail properties extracted from intent
     */
    private ActivityTransitionImageViewProperty mTransImageViewProperty;
    private ActivityTransitionTextViewProperty mTransTitleViewProperty;
    private ActivityTransitionTextViewProperty mTransFeedTitleViewProperty;

    /**
     * Scale, translation properties for "Top image"
     */
    private Rect mImageWrapperRect;
    private Rect mThumbnailStartRect;
    private Rect mThumbnailEndRect;

    /**
     * Scale, translation properties for "Top text layout and it's components"
     */
    private Rect mTopTextLayoutLocalVisibleRect;
    private Rect mTopTextLayoutAnimatingLocalVisibleRect = new Rect();
    private Rect mTopTitleLocalVisibleRect;
    private Rect mTopDescriptionLocalVisibleRect;

    private Rect mRecyclerGlobalVisibleRect;
    private Rect mRecyclerAnimatingLocalVisibleRect = new Rect();
    private ArrayList<Rect> mRecyclerChildTitleLocalVisibleRects = new ArrayList<>();
    private ArrayList<Rect> mRecyclerChildDescriptionLocalVisibleRects = new ArrayList<>();
    private SparseArray<Boolean> mIsAnimatingRecyclerChildTitleArray = new SparseArray<>();
    private SparseArray<Boolean> mIsAnimatingRecyclerChildDescriptionArray = new SparseArray<>();

    private TextView mNewsTitleThumbnailTextView;
    private TextView mNewsFeedTitleThumbnailTextView;

    private long mRevealAnimDuration;
    private long mImageFilterAnimDuration;
    private long mImageTranslationAnimDuration;
    private long mThumbnailTextAnimDuration;
    private long mToolbarAnimDuration;
    private long mToolbarBgAnimDuration;
    private long mRecyclerHeightAnimDuration;
    private long mRecyclerHeightAnimStartDelay;
    private long mTextFadeAnimDuration;

    private long mImageAnimationStartOffset;

    private Toolbar mToolbar;
    private RelativeLayout mRootLayout;
    private FrameLayout mTransitionLayout;
    private View mToolbarOverlayView;
    private View mTopGradientShadowView;
    private View mRevealView;

    private int mRevealTargetRadius;

    // Top
    private FrameLayout mTopNewsImageWrapper;
    private ImageView mTopImageView;
    private LinearLayout mTopTextLayout;
    private TextView mTopTitleTextView;
    private TextView mTopDescriptionTextView;

    // Bottom
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mRecyclerLayoutManager;

    private NewsFeedDetailActivity mActivity;
    private OnAnimationEndListener mListener;
    private SpannableString mToolbarTitle;
    private AlphaForegroundColorSpan mToolbarTitleColorSpan;

    private HashMap<String, Boolean> mFinishedAnimations;

    private NewsFeedDetailTransitionUtils(NewsFeedDetailActivity activity) {
        initViewsAndVariables(activity);
    }

    public static NewsFeedDetailTransitionUtils runEnterAnimation(NewsFeedDetailActivity activity) {
        NewsFeedDetailTransitionUtils animator = new NewsFeedDetailTransitionUtils(activity);
        animator.requestActivityTransition();

        return animator;
    }

    public static void animateTopOverlayFadeOut(NewsFeedDetailActivity activity) {
        new NewsFeedDetailTransitionUtils(activity).fadeOutTopOverlay();
    }

    public static void animateTopOverlayFadeIn(NewsFeedDetailActivity activity) {
        new NewsFeedDetailTransitionUtils(activity).fadeInTopOverlay();
    }

    public boolean isAnimating() {
        return mFinishedAnimations != null
                && mFinishedAnimations.containsValue(false);
    }

    private boolean isAnimatingElement(String animKey) {
        return mFinishedAnimations != null
                && mFinishedAnimations.containsKey(animKey)
                && !mFinishedAnimations.get(animKey);
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

    private void initTransitionVariablesAfterViewLocationFix() {
        extractActivityTransitionProperties();

        initImageTransitionVariables();
        initTopTextLayoutVariables();
        initRecyclerVariables();

        initDurationVariables();
    }

    private void extractActivityTransitionProperties() {
        Bundle extras = mActivity.getIntent().getExtras();

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

    private void initImageTransitionVariables() {
        initTopImageWrapperRect();
        initThumbnailStartRect();
        initThumbnailEndRect();
    }

    private void initTopImageWrapperRect() {
        mImageWrapperRect = new Rect();
        mTopNewsImageWrapper.getGlobalVisibleRect(mImageWrapperRect);
    }

    private void initThumbnailStartRect() {
        int thumbnailLeftDelta = mTransImageViewProperty.getLeft() - mImageWrapperRect.left;
        int thumbnailTopDelta = mTransImageViewProperty.getTop() - mImageWrapperRect.top;
        mThumbnailStartRect = new Rect(thumbnailLeftDelta, thumbnailTopDelta,
                thumbnailLeftDelta + mTransImageViewProperty.getWidth(),
                thumbnailTopDelta + mTransImageViewProperty.getHeight());
    }

    private void initThumbnailEndRect() {
        float widthScaleRatio = mTopNewsImageWrapper.getWidth()
                / (float)mTransImageViewProperty.getWidth();
        float heightScaleRatio = mTopNewsImageWrapper.getHeight()
                / (float)mTransImageViewProperty.getHeight();
        boolean fitWidth = widthScaleRatio > heightScaleRatio;
//        float scaleRatio = fitWidth ? widthScaleRatio : heightScaleRatio;

        int targetWidth = fitWidth
                ? mTopNewsImageWrapper.getWidth()
                : (int)Math.ceil(mTransImageViewProperty.getWidth() * heightScaleRatio);

//        int targetWidth = (int)(mTransImageViewProperty.getWidth() * scaleRatio);
        int imageWrapperLeft = mTopNewsImageWrapper.getLeft();
        int thumbnailLeftTarget = fitWidth
                ? imageWrapperLeft
                : imageWrapperLeft - (targetWidth - mTopNewsImageWrapper.getWidth())/2;
        int thumbnailTopTarget = mTopNewsImageWrapper.getTop();

        mThumbnailEndRect = new Rect(thumbnailLeftTarget, thumbnailTopTarget,
                thumbnailLeftTarget + targetWidth,
                thumbnailTopTarget + mTopNewsImageWrapper.getHeight());
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
        Resources resources = mActivity.getResources();

        int animatorScale = isUseScaledDurationDebug(mActivity.getApplicationContext()) ?
                resources.getInteger(R.integer.news_feed_detail_debug_transition_scale) : 1;

        mRevealAnimDuration = resources.getInteger(
                R.integer.news_feed_detail_reveal_duration) * animatorScale;
        mImageAnimationStartOffset = resources.getInteger(
                R.integer.news_feed_detail_image_filter_start_offset) * animatorScale;
        mImageFilterAnimDuration = resources.getInteger(
                R.integer.news_feed_detail_image_filter_duration) * animatorScale;
//        mImageScaleAnimDuration = resources.getInteger(
//                R.integer.news_feed_detail_image_scale_duration) * animatorScale;
        mImageTranslationAnimDuration = resources.getInteger(
                R.integer.news_feed_detail_image_translation_duration) * animatorScale;

        mThumbnailTextAnimDuration = resources.getInteger(
                R.integer.news_feed_detail_thumbnail_text_duration) * animatorScale;

        mToolbarAnimDuration = resources.getInteger(
                R.integer.news_feed_detail_toolbar_duration) * animatorScale;
        mToolbarBgAnimDuration = resources.getInteger(
                R.integer.news_feed_detail_toolbar_bg_duration) * animatorScale;

        mRecyclerHeightAnimDuration = resources.getInteger(
                R.integer.news_feed_detail_recycler_height_duration) * animatorScale;

        mRecyclerHeightAnimStartDelay = resources.getInteger(
                R.integer.news_feed_detail_recycler_height_start_delay) * animatorScale;

        mTextFadeAnimDuration = resources.getInteger(
                R.integer.news_feed_detail_text_fade_duration) * animatorScale;

//        mDebugTempDuration = mImageScaleAnimDuration;
    }

    /**
     * 기본적으로 모든 트랜지션은 트랜지션이 시작되기 직전, 혹은 트랜지션의 시작값으로 초기값을 설정해준다.
     * 그러므로 트랜지션들중 처음부터 시작되지 않는 뷰들의 속성은 여기에서 미리 설정해준다.
     */
    private void prepareViewPropertiesBeforeTransition() {
        mToolbar.setAlpha(0.0f);

        hideRootBackground();

        mTransitionLayout.setVisibility(View.VISIBLE);

        setImageWrapperRect(mThumbnailStartRect);

        mTopTitleTextView.setAlpha(0.0f);
        mTopDescriptionTextView.setAlpha(0.0f);

        mTopTextLayout.setVisibility(View.INVISIBLE);
        mTopTextLayout.getLayoutParams().height = 0;
        mRecyclerView.setVisibility(View.INVISIBLE);

        // 현재 액티비티에서 onPreDraw 에서 다시 높이를 잡아주고 있기 때문에 의미가 없는 로직
        // 추후 수정이 된다면 여기서 잡아줘도 괜찮을듯
//        mRecyclerView.getLayoutParams().height = 0;

        saveTopOverlayAlphaState();
        mTopGradientShadowView.setAlpha(0);
        mToolbarOverlayView.setAlpha(0);
    }

    private void addThumbnailTextViews() {
        if (mActivity.isFromTopNewsFeed()) {
            // 탑에서 불렸을 때
            mNewsTitleThumbnailTextView = (TextView) LayoutInflater.from(mActivity)
                    .inflate(R.layout.main_top_title_textview, null);
            mNewsFeedTitleThumbnailTextView = (TextView) LayoutInflater.from(mActivity)
                    .inflate(R.layout.main_top_newsfeed_title_textview, null);
        } else {
            // 바텀에서 불렸을 때
            mNewsTitleThumbnailTextView = (TextView) LayoutInflater.from(mActivity)
                    .inflate(R.layout.main_bottom_title_textview, null);
            mNewsFeedTitleThumbnailTextView = (TextView) LayoutInflater.from(mActivity)
                    .inflate(R.layout.main_bottom_newsfeed_title_textview, null);
        }
        addThumbnailTextView(mNewsTitleThumbnailTextView, mTransTitleViewProperty);
        addThumbnailTextView(mNewsFeedTitleThumbnailTextView, mTransFeedTitleViewProperty);
    }

    private void notifyAnimationStart(String animKey) {
        mFinishedAnimations.put(animKey, false);
    }

    private void notifyAnimationEnd(String animKey) {
        if (mFinishedAnimations.containsKey(animKey)) {
            mFinishedAnimations.put(animKey, true);
        }

        if (!mFinishedAnimations.containsValue(false)) {
            mListener.onTransitionEnd();
        }
    }

    private void startTransition() {
        revealBackground();
        animateThumbnailImageAndTexts();
        animateTopNewsTextAndRecycler();
    }

    private void revealBackground() {
        if (Device.hasLollipop()) {
            revealBackgroundAfterLollipop();
        } else {
            revealBackgroundBeforeLollipop();
        }
    }

    private void animateThumbnailImageAndTexts() {
        transitImageWrapper();
        fadeOutImageColorFilter();
        fadeOutThumbnailTexts();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealBackgroundAfterLollipop() {
        Animator animator = ViewAnimationUtils.createCircularReveal(
                mRevealView, getRevealCenter().x, getRevealCenter().y, getRevealStartRadius(), getRevealTargetRadius());
        animator.setDuration((int) mRevealAnimDuration);
        animator.setInterpolator(AnimationFactory.createFastOutSlowInInterpolator());
        animator.start();
    }

    private void revealBackgroundBeforeLollipop() {
        SupportAnimator animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                mRevealView, getRevealCenter().x, getRevealCenter().y, getRevealStartRadius(), getRevealTargetRadius());
        animator.setDuration((int) mRevealAnimDuration);
        android.view.animation.Interpolator interpolator = (android.view.animation.Interpolator)
                AnimationFactory.createFastOutSlowInInterpolator();
        animator.setInterpolator(interpolator);
        animator.start();
    }

    private void transitImageWrapper() {
        ObjectAnimator imageWrapperRectAnimator = ObjectAnimator.ofObject(this, "imageWrapperRect",
                new RectEvaluator(new Rect()), mThumbnailStartRect, mThumbnailEndRect);

        double radiusRatio = getRevealTargetRadius() / getRevealRadiusFromDeviceCenter();
        long startOffset = (long) (mImageAnimationStartOffset * radiusRatio);
        if (!Device.hasLollipop()) {
            startOffset *= 1.8;
        }
        imageWrapperRectAnimator.setStartDelay(startOffset);
        imageWrapperRectAnimator.setDuration(mImageTranslationAnimDuration);
        imageWrapperRectAnimator.setInterpolator(AnimationFactory.createFastOutSlowInInterpolator());
        imageWrapperRectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                fadeInToolbar();
                fadeInTopOverlay();
            }
        });

        imageWrapperRectAnimator.start();
    }

    private void scaleTopNewsTextLayoutHeight() {
        ObjectAnimator animator = ObjectAnimator.ofInt(
                this, "TopTextLayoutHeight", 0, mTopTextLayoutLocalVisibleRect.height());

        double radiusRatio = getRevealTargetRadius() / getRevealRadiusFromDeviceCenter();
        animator.setStartDelay((long) (mRecyclerHeightAnimStartDelay * radiusRatio));
        animator.setDuration(mRecyclerHeightAnimDuration);
        animator.setInterpolator(AnimationFactory.createFastOutSlowInInterpolator());
        animator.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mTopTextLayout.setVisibility(View.VISIBLE);
            }
        });

        animator.start();
    }

    private void scaleRecyclerHeight() {
        ObjectAnimator animator = ObjectAnimator.ofInt(
                this, "recyclerViewHeight", 0, mRecyclerGlobalVisibleRect.height());

        double radiusRatio = getRevealTargetRadius() / getRevealRadiusFromDeviceCenter();
        animator.setStartDelay((long) (mRecyclerHeightAnimStartDelay * radiusRatio));
        animator.setDuration(mRecyclerHeightAnimDuration);
        animator.setInterpolator(AnimationFactory.createFastOutSlowInInterpolator());
        animator.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                showRootBackground();
//                mListener.onRecyclerScaleAnimationEnd();
            }
        });

        animator.start();
    }

    private void hideRootBackground() {
        mRevealView.setVisibility(View.VISIBLE);
        mRootLayout.setBackgroundColor(Color.TRANSPARENT);
    }

    private void showRootBackground() {
        mRevealView.setVisibility(View.INVISIBLE);
        mRootLayout.setBackgroundColor(
                mActivity.getResources().getColor(R.color.newsfeed_detail_background_color));
    }

    private void animateTopNewsTextAndRecycler() {
        scaleTopNewsTextLayoutHeight();
        scaleRecyclerHeight();
    }

    private Point getRevealCenter() {
        Point center = mTransImageViewProperty.getCenter();
        if (!Device.hasLollipop()) {
            center.y -= Display.getStatusBarHeight(mActivity.getApplicationContext());
        }

        return center;
    }

    private int getRevealStartRadius() {
        return (Math.min(mTransImageViewProperty.getWidth(), mTransImageViewProperty.getHeight()) / 2);
    }

    private int getRevealTargetRadius() {
        return calculateFarthestLengthFromRevealCenterToRevealCorner();
    }

    private int calculateFarthestLengthFromRevealCenterToRevealCorner() {
        if (mRevealTargetRadius != 0) {
            return mRevealTargetRadius;
        }

        Point center = getRevealCenter();
        int distanceToRevealViewLeft = center.x - mRevealView.getLeft();
        int distanceToRevealViewTop = center.y - mRevealView.getTop();
        int distanceToRevealViewRight = mRevealView.getRight() - center.x;
        int distanceToRevealViewBottom = mRevealView.getBottom() - center.y;

        int distanceToRevealLeftTop =
                (int) Math.hypot(distanceToRevealViewLeft, distanceToRevealViewTop);
        int distanceToRevealRightTop =
                (int) Math.hypot(distanceToRevealViewRight, distanceToRevealViewTop);
        int distanceToRevealRightBottom =
                (int) Math.hypot(distanceToRevealViewRight, distanceToRevealViewBottom);
        int distanceToRevealLeftBottom =
                (int) Math.hypot(distanceToRevealViewLeft, distanceToRevealViewBottom);

        mRevealTargetRadius = IntegerMath.getLargestInteger(distanceToRevealLeftTop,
                distanceToRevealRightTop, distanceToRevealRightBottom, distanceToRevealLeftBottom);

        return mRevealTargetRadius;
    }

    private double getRevealRadiusFromDeviceCenter() {
        Point displaySize = Display.getDisplaySize(mActivity);
        return (Math.hypot(displaySize.x, displaySize.y) / 2);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setImageWrapperRect(Rect rect) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)mTopNewsImageWrapper.getLayoutParams();
        lp.leftMargin = rect.left;
        lp.topMargin = rect.top;
        lp.rightMargin = mImageWrapperRect.right - rect.right;
        lp.width = rect.width();
        lp.height = rect.height();
        mTopNewsImageWrapper.setLayoutParams(lp);

        translateTextLayoutAndRecyclerOnTranslateImage(rect);
        scaleTextLayoutAndRecyclerWidthOnScaleImage(rect);
    }

    private void translateTextLayoutAndRecyclerOnTranslateImage(Rect imageRect) {
        int leftMargin = imageRect.left;
        int targetLeftMargin = leftMargin >= 0 ? leftMargin : 0;

        ViewGroup.MarginLayoutParams textLayoutLp = (ViewGroup.MarginLayoutParams) mTopTextLayout.getLayoutParams();
        textLayoutLp.leftMargin = targetLeftMargin;
        mTopTextLayout.setLayoutParams(textLayoutLp);

        ViewGroup.MarginLayoutParams recyclerLp = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
        recyclerLp.leftMargin = targetLeftMargin;
        mRecyclerView.setLayoutParams(recyclerLp);
    }

    private void scaleTextLayoutAndRecyclerWidthOnScaleImage(Rect imageRect) {
        int width = getTopTextAndRecyclerTargetWidth(imageRect);

        setTopTextLayoutWidth(width);
        setRecyclerViewWidth(width);
    }

    private int getTopTextAndRecyclerTargetWidth(Rect imageRect) {
        int leftTarget = Math.max(imageRect.left, 0);
        int rightTarget = Math.min(imageRect.right, mImageWrapperRect.right);
        return rightTarget - leftTarget;
    }

    private void setTopTextLayoutWidth(int width) {
        ViewGroup.LayoutParams textLayoutLp = mTopTextLayout.getLayoutParams();
        textLayoutLp.width = width;
        mTopTextLayout.setLayoutParams(textLayoutLp);

        mTopTextLayoutAnimatingLocalVisibleRect.right = width;

        animateTopTitleAndDescriptionIfSizeSufficient();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setTopTextLayoutHeight(int height) {
        ViewGroup.LayoutParams lp = mTopTextLayout.getLayoutParams();
        lp.height = height;
        mTopTextLayout.setLayoutParams(lp);

        mTopTextLayoutAnimatingLocalVisibleRect.bottom = height;

        animateTopTitleAndDescriptionIfSizeSufficient();
    }

    private void setRecyclerViewWidth(int width) {
        ViewGroup.LayoutParams recyclerLp = mRecyclerView.getLayoutParams();
        recyclerLp.width = width;
        mRecyclerView.setLayoutParams(recyclerLp);

        mRecyclerAnimatingLocalVisibleRect.right = width;

        animateRecyclerChildIfSizeSufficient();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRecyclerViewHeight(int height) {
        ViewGroup.LayoutParams lp = mRecyclerView.getLayoutParams();
        lp.height = height;
        mRecyclerView.setLayoutParams(lp);

        mRecyclerAnimatingLocalVisibleRect.bottom = height;

        animateRecyclerChildIfSizeSufficient();
    }

    private void animateTopTitleAndDescriptionIfSizeSufficient() {
        if (isReadyToAnimateTopTitle()) {
            fadeInTopTitle();
        }
        if (isReadyToAnimateTopDescription()) {
            fadeInTopDescription();
        }
    }

    private boolean isReadyToAnimateTopTitle() {
        return !isAnimatingElement(ANIM_TOP_TITLE)
                && mTopTextLayout.getVisibility() == View.VISIBLE
                && mTopTextLayoutAnimatingLocalVisibleRect.contains(mTopTitleLocalVisibleRect);
    }

    private boolean isReadyToAnimateTopDescription() {
        return !isAnimatingElement(ANIM_TOP_DESCRIPTION)
                && mTopTextLayout.getVisibility() == View.VISIBLE
                && mTopTextLayoutAnimatingLocalVisibleRect.contains(mTopDescriptionLocalVisibleRect);
    }

    private void fadeInTopTitle() {
        notifyAnimationStart(ANIM_TOP_TITLE);

        mTopTitleTextView.animate()
                .setDuration(mTextFadeAnimDuration)
                .alpha(1.0f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        notifyAnimationEnd(ANIM_TOP_TITLE);
                    }
                });
    }

    private void fadeInTopDescription() {
        notifyAnimationStart(ANIM_TOP_DESCRIPTION);

        mTopDescriptionTextView.animate()
                .setDuration(mTextFadeAnimDuration)
                .alpha(1.0f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        notifyAnimationEnd(ANIM_TOP_DESCRIPTION);
                    }
                });
    }

    private void animateRecyclerChildIfSizeSufficient() {
        for (int i = 0 ; i < mRecyclerChildTitleLocalVisibleRects.size(); i++) {
            if (shouldPrepareToAnimateRecyclerTitleAt(i)) {
                prepareRecyclerTitleAt(i);
            }
            if (isReadyToAnimateRecyclerChildTitleAt(i)) {
                fadeInRecyclerTitleAt(i);
            }
        }
        for (int i = 0 ; i < mRecyclerChildDescriptionLocalVisibleRects.size(); i++) {
            if (shouldPrepareToAnimateRecyclerDescriptionAt(i)) {
                prepareRecyclerDescriptionAt(i);
            }
            if (isReadyToAnimateRecyclerChildDescriptionAt(i)) {
                fadeInRecyclerDescriptionAt(i);
            }
        }
    }

    private boolean shouldPrepareToAnimateRecyclerTitleAt(int index) {
        return !isAnimatingRecyclerTitleAt(index)
                && isRectPartiallyVisibleInAnimatingRecyclerView(getRecyclerTitleRect(index));
    }

    private boolean shouldPrepareToAnimateRecyclerDescriptionAt(int i) {
        return !isAnimatingRecyclerDescriptionAt(i)
                && isRectPartiallyVisibleInAnimatingRecyclerView(getRecyclerDescriptionRect(i));
    }

    private void prepareRecyclerTitleAt(int index) {
        try {
            View viewToAnimate = getTitleViewFromRecyclerChildAt(index);
            viewToAnimate.setAlpha(0.0f);
        } catch(ChildNotFoundException ignored) {
        }
    }

    private void prepareRecyclerDescriptionAt(int index) {
        try {
            View viewToAnimate = getDescriptionViewFromRecyclerChildAt(index);
            viewToAnimate.setAlpha(0.0f);
        } catch(ChildNotFoundException ignored) {
        }
    }

    private boolean isReadyToAnimateRecyclerChildTitleAt(int index) {
        Rect rectToInspect = getRecyclerTitleRect(index);
        boolean isAnimating = isAnimatingRecyclerTitleAt(index);
        boolean isRecyclerViewVisible = isRecyclerViewVisible();
        return !isAnimating && isRecyclerViewVisible
                && isRectFullyVisibleInRecyclerView(rectToInspect);
    }

    private boolean isReadyToAnimateRecyclerChildDescriptionAt(int index) {
        Rect rectToInspect = getRecyclerDescriptionRect(index);
        boolean isAnimating = isAnimatingRecyclerDescriptionAt(index);
        boolean isRecyclerViewVisible = isRecyclerViewVisible();
        return !isAnimating && isRecyclerViewVisible
                && isRectFullyVisibleInRecyclerView(rectToInspect);
    }

    private Rect getRecyclerTitleRect(int index) {
        return mRecyclerChildTitleLocalVisibleRects.get(index);
    }

    private boolean isAnimatingRecyclerTitleAt(int index) {
        return mIsAnimatingRecyclerChildTitleArray.get(index);
    }

    private Rect getRecyclerDescriptionRect(int index) {
        return mRecyclerChildDescriptionLocalVisibleRects.get(index);
    }

    private boolean isAnimatingRecyclerDescriptionAt(int index) {
        return mIsAnimatingRecyclerChildDescriptionArray.get(index);
    }

    private boolean isRecyclerViewVisible() {
        return mRecyclerView.getVisibility() == View.VISIBLE;
    }

    private View getItemViewFromRecyclerViewAt(int i) {
        View childView = mRecyclerLayoutManager.getChildAt(i);
        if (childView == null) {
            /**
             * RecyclerView.LayoutManager 의 getChildAt 메서드는 잘못된 index 를 넘길 경우
             * IndexOutOfBoundsException 을 던지지 않고 null 을 반환한다.
             * 그러므로 명시적으로 IndexOutOfBoundsException 을 던져 윗쪽에서 처리할 수 있도록 한다.
             */
            throw new IndexOutOfBoundsException();
        }else {
            return childView;
        }
    }

    private View getTitleViewFromRecyclerChildAt(int index) throws ChildNotFoundException {
        try {
            return getItemViewFromRecyclerViewAt(index).findViewById(
                    R.id.detail_bottom_news_item_title);
        } catch(IndexOutOfBoundsException e) {
            throw new ChildNotFoundException();
        }
    }

    private View getDescriptionViewFromRecyclerChildAt(int index) throws ChildNotFoundException {
        try {
            return getItemViewFromRecyclerViewAt(index).findViewById(
                    R.id.detail_bottom_news_item_description);
        } catch(IndexOutOfBoundsException e) {
            throw new ChildNotFoundException();
        }
    }

    private boolean isRectFullyVisibleInRecyclerView(Rect rectToInspect) {
        return mRecyclerAnimatingLocalVisibleRect.contains(rectToInspect);
    }

    private boolean isRectIntersectsWithRecyclerViewBottom(Rect rectToInspect) {
        return rectToInspect.top < mRecyclerGlobalVisibleRect.height()
                && rectToInspect.bottom > mRecyclerGlobalVisibleRect.height();
    }

    private boolean isRectPartiallyVisibleInAnimatingRecyclerView(Rect rectToInspect) {
        return !isRectFullyVisibleInRecyclerView(rectToInspect)
                && Rect.intersects(mRecyclerAnimatingLocalVisibleRect, rectToInspect);
    }

    private void fadeInRecyclerTitleAt(final int index) {
        try {
            View viewToAnimate = getTitleViewFromRecyclerChildAt(index);
            ViewPropertyAnimator animator = viewToAnimate.animate()
                    .setDuration(mTextFadeAnimDuration)
                    .alpha(1.0f);
            if (isLastRecyclerTitle(index)) {
                notifyAnimationStart(ANIM_RECYCLER_TITLE);
                animator.withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        notifyAnimationEnd(ANIM_RECYCLER_TITLE);
                    }
                });
            }
            mIsAnimatingRecyclerChildTitleArray.put(index, true);
        } catch(ChildNotFoundException ignored) {
        }
    }

    private void fadeInRecyclerDescriptionAt(final int index) {
        try {
            View viewToAnimate = getDescriptionViewFromRecyclerChildAt(index);
            ViewPropertyAnimator animator = viewToAnimate.animate()
                    .setDuration(mTextFadeAnimDuration)
                    .alpha(1.0f);
            if (isLastRecyclerDescription(index)) {
                notifyAnimationStart(ANIM_RECYCLER_DESCRIPTION);
                animator.withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        notifyAnimationEnd(ANIM_RECYCLER_DESCRIPTION);
                    }
                });
            }
            mIsAnimatingRecyclerChildDescriptionArray.put(index, true);

        } catch(ChildNotFoundException ignored) {
        }
    }

    private boolean isLastRecyclerTitle(int index) {
        return index == mRecyclerChildTitleLocalVisibleRects.size() - 1
                && index == mRecyclerChildDescriptionLocalVisibleRects.size();
    }

    private boolean isLastRecyclerDescription(int index) {
        return index == mRecyclerChildTitleLocalVisibleRects.size() - 1
                && index == mRecyclerChildDescriptionLocalVisibleRects.size() - 1;
    }

    private void fadeInToolbar() {
        notifyAnimationStart(ANIM_TOOLBAR);
        mToolbar.animate().
                alpha(1.0f).
                setDuration(mToolbarAnimDuration).
                withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        notifyAnimationEnd(ANIM_TOOLBAR);
                    }
                });
    }

    private void initViewsAndVariables(NewsFeedDetailActivity activity) {
        initVariables(activity);
        initViews();
    }

    private void initVariables(NewsFeedDetailActivity activity) {
        mActivity = activity;
        mListener = activity;
        mToolbarTitle = activity.getToolbarTitle();
        mToolbarTitleColorSpan = activity.getToolbarTitleColorSpan();
        mFinishedAnimations = new HashMap<>();
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
        mRecyclerView = mActivity.getBottomRecyclerView();
    }

    private void initRecyclerVariables() {
        mRecyclerGlobalVisibleRect = new Rect();
        mRecyclerView.getGlobalVisibleRect(mRecyclerGlobalVisibleRect);

        mRecyclerLayoutManager = (LinearLayoutManager)mRecyclerView.getLayoutManager();

        initRecyclerChildVariables();
    }

    private void initRecyclerChildVariables() {
        int childCount = mRecyclerLayoutManager.getChildCount();
        int offsetFromRecyclerTop = 0;
        for (int i = 0 ; i < childCount; i++) {
            Rect childRect = getRecyclerChildRect(i);
            if (isRecyclerChildRectInvisible(childRect)) {
                break;
            }
            try {
                putRecyclerChildTitleAt(i, offsetFromRecyclerTop);
            } catch (ChildNotFoundException e) {
                break;
            }
            try {
                putRecyclerChildDescriptionAt(i, offsetFromRecyclerTop);
            } catch (ChildNotFoundException e) {
                break;
            }

            offsetFromRecyclerTop += childRect.height();
        }
    }

    private Rect getRecyclerChildRect(int i) {
        View child = getItemViewFromRecyclerViewAt(i);

        return new Rect(
                child.getLeft(),
                child.getTop(),
                child.getRight(),
                child.getBottom()
        );
    }

    private boolean isRecyclerChildRectInvisible(Rect childRect) {
        return childRect.top > mRecyclerGlobalVisibleRect.height();
    }

    private void putRecyclerChildTitleAt(int index, int offsetFromRecyclerTop) throws ChildNotFoundException {
        View title = getTitleViewFromRecyclerChildAt(index);
        Rect titleRect = new Rect(
                title.getLeft(),
                title.getTop() + offsetFromRecyclerTop,
                title.getRight(),
                title.getBottom() + offsetFromRecyclerTop
        );

        if (isRecyclerChildRectInvisible(titleRect)) {
            throw new ChildNotFoundException();
        }
        titleRect = adjustRectIfIntersectsWithRecyclerBottom(titleRect);
        mRecyclerChildTitleLocalVisibleRects.add(titleRect);
        mIsAnimatingRecyclerChildTitleArray.put(index, false);
    }

    private void putRecyclerChildDescriptionAt(int index, int offsetFromRecyclerTop) throws ChildNotFoundException {
        View description = getDescriptionViewFromRecyclerChildAt(index);
        Rect descriptionRect = new Rect(
                description.getLeft(),
                description.getTop() + offsetFromRecyclerTop,
                description.getRight(),
                description.getBottom() + offsetFromRecyclerTop
        );

        if (isRecyclerChildRectInvisible(descriptionRect)) {
            throw new ChildNotFoundException();
        }
        descriptionRect = adjustRectIfIntersectsWithRecyclerBottom(descriptionRect);
        mRecyclerChildDescriptionLocalVisibleRects.add(descriptionRect);
        mIsAnimatingRecyclerChildDescriptionArray.put(index, false);
    }

    private Rect adjustRectIfIntersectsWithRecyclerBottom(Rect rectToInspect) {
        if (isRectIntersectsWithRecyclerViewBottom(rectToInspect)) {
            int invisibleArea = rectToInspect.bottom - mRecyclerGlobalVisibleRect.height();

            rectToInspect.bottom -= invisibleArea;
        }
        return rectToInspect;
    }

    private void fadeOutImageColorFilter() {
        notifyAnimationStart(ANIM_COLOR_FILTER);
        int filterColor = mActivity.getFilterColor();

        int red = Color.red(filterColor);
        int green = Color.green(filterColor);
        int blue = Color.blue(filterColor);
        int argb = Color.argb(Color.alpha(filterColor), red, green, blue);
        ImageFilterAnimator.animate(mTopImageView, argb, 0, mImageFilterAnimDuration,
                mImageAnimationStartOffset, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        notifyAnimationEnd(ANIM_COLOR_FILTER);
                    }
                });
    }

    private void fadeOutThumbnailTexts() {
        mNewsTitleThumbnailTextView.animate()
                .alpha(0.0f)
                .setDuration(mThumbnailTextAnimDuration)
                .start();

        notifyAnimationStart(ANIM_THUMBNAIL_TEXT);
        mNewsFeedTitleThumbnailTextView.animate()
                .alpha(0.0f)
                .setDuration(mThumbnailTextAnimDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mTransitionLayout.setVisibility(View.GONE);
                        notifyAnimationEnd(ANIM_THUMBNAIL_TEXT);
                    }
                })
                .start();
    }

    private void addThumbnailTextView(TextView textView,
                                      ActivityTransitionTextViewProperty textViewProperty) {
        textView.setEllipsize(
                TextUtils.TruncateAt.values()[textViewProperty.getEllipsizeOrdinal()]);
        textView.setMaxLines(textViewProperty.getMaxLine());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textViewProperty.getTextSize());
        textView.setGravity(textViewProperty.getGravity());
        textView.setText(textViewProperty.getText());

        addThumbnailView(textView, textViewProperty);
    }

    private void addThumbnailView(View view, ActivityTransitionProperty property) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                property.getWidth(), property.getHeight());
        lp.leftMargin = property.getLeft();
        lp.topMargin = property.getTop();
        if (!Device.hasLollipop()) {
            lp.topMargin -= Display.getStatusBarHeight(mActivity.getApplicationContext());
        }
        mTransitionLayout.addView(view, lp);
    }

    private void fadeInTopOverlay() {
        if (mTopGradientShadowView.getTag() == null || mToolbarOverlayView.getTag() == null
                || mTopGradientShadowView.getAlpha() > 0 || mToolbarOverlayView.getAlpha() > 0) {
            return;
        }
        mTopGradientShadowView.animate()
                .setDuration(mToolbarBgAnimDuration)
                .alpha((Float) mTopGradientShadowView.getTag())
                .setInterpolator(new DecelerateInterpolator());

        notifyAnimationStart(ANIM_TOP_OVERLAY);
        mToolbarOverlayView.animate()
                .setDuration(mToolbarBgAnimDuration)
                .alpha((Float) mToolbarOverlayView.getTag())
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        notifyAnimationEnd(ANIM_TOP_OVERLAY);
                    }
                });
    }

    private void fadeOutTopOverlay() {
        saveTopOverlayAlphaState();
        mTopGradientShadowView.animate()
                .setDuration(mToolbarBgAnimDuration)
                .alpha(0f)
                .setInterpolator(new DecelerateInterpolator());
        mToolbarOverlayView.animate()
                .setDuration(mToolbarBgAnimDuration)
                .alpha(0f)
                .setInterpolator(new DecelerateInterpolator());
    }

    private void saveTopOverlayAlphaState() {
        mTopGradientShadowView.setTag(mTopGradientShadowView.getAlpha());
        mToolbarOverlayView.setTag(mToolbarOverlayView.getAlpha());
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

    private static boolean isUseScaledDurationDebug(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                SHARED_PREFERENCES_NEWSFEED_DETAIL_TRANSITION, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_USE_SCALED_DURATION, false);
    }

    private static class ChildNotFoundException extends Exception {
    }
}
