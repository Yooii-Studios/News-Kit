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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.SparseArray;
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
import com.yooiistudios.newsflow.util.NLLog;
import com.yooiistudios.newsflow.util.ScreenUtils;
import com.yooiistudios.serialanimator.AnimatorListenerImpl;

import java.lang.reflect.Type;
import java.util.ArrayList;

import io.codetail.animation.SupportAnimator;

import static com.yooiistudios.newsflow.ui.activity.MainActivity.INTENT_KEY_TRANSITION_PROPERTY;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 24.
 *
 * NewsFeedDetailTransitionUtils
 *  NewsFeedDetailActivity 의 액티비티 트랜지션 애니메이션을 래핑한 클래스
 */
public class NewsFeedDetailTransitionUtils {
    public interface OnAnimationEndListener {
        public void onRecyclerScaleAnimationEnd();
    }

    private static final String SHARED_PREFERENCES_NEWSFEED_DETAIL_TRANSITION
            = "shared_preferences_newsfeed_detail_transition";
    private static final String KEY_USE_SCALED_DURATION = "key_use_scale_duration";

    /**
     * Thumbnail properties extracted from intent.
     */
    private ActivityTransitionImageViewProperty mTransImageViewProperty;
    private ActivityTransitionTextViewProperty mTransTitleViewProperty;
    private ActivityTransitionTextViewProperty mTransFeedTitleViewProperty;

    /**
     * Scale, translation properties for "Top image".
     */
    private Rect mImageWrapperRect;
    private Rect mThumbnailStartRect;
    private Rect mThumbnailEndRect;

    /**
     * Scale, translation properties for "Top text layout and it's components".
     */
    private Rect mTopTextLayoutLocalVisibleRect;
    private Rect mTopTextLayoutAnimatingLocalVisibleRect = new Rect();
    private Rect mTopTitleLocalVisibleRect;
    private Rect mTopDescriptionLocalVisibleRect;
    private boolean mIsAnimatingTopTitleFadeIn = false;
    private boolean mIsAnimatingTopDescriptionFadeIn = false;

    private Rect mRecyclerGlobalVisibleRect;
    private Rect mRecyclerAnimatingLocalVisibleRect = new Rect();
    private ArrayList<Rect> mRecyclerChildViewLocalVisibleRects = new ArrayList<>();
    private SparseArray<Boolean> mIsAnimatingRecyclerChildArray = new SparseArray<>();
    private ArrayList<Rect> mRecyclerChildTitleLocalVisibleRects = new ArrayList<>();
    private ArrayList<Rect> mRecyclerChildDescriptionLocalVisibleRects = new ArrayList<>();
    private SparseArray<Boolean> mIsAnimatingRecyclerChildTitleArray = new SparseArray<>();
    private SparseArray<Boolean> mIsAnimatingRecyclerChildDescriptionArray = new SparseArray<>();
    private int mRecyclerChildCountToAnimate;

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
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mRecyclerLayoutManager;

    private NewsFeedDetailActivity mActivity;
    private OnAnimationEndListener mListener;
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

    private void initTransitionVariablesAfterViewLocationFix() {
        extractActivityTransitionProperties();

        initImageTransitionVariables();
        initTopTextLayoutVariables();
        initRecyclerVariables();

        initDurationVariables();
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
        mRecyclerView.setVisibility(View.INVISIBLE);
        mRecyclerView.getLayoutParams().height = 0;

        saveTopOverlayAlphaState();
        mTopGradientShadowView.setAlpha(0);
        mToolbarOverlayView.setAlpha(0);
    }

    private void addThumbnailTextViews() {
        mNewsTitleThumbnailTextView = new TextView(mActivity);
        mNewsFeedTitleThumbnailTextView = new TextView(mActivity);

        addThumbnailTextView(mNewsTitleThumbnailTextView, mTransTitleViewProperty);
        addThumbnailTextView(mNewsFeedTitleThumbnailTextView, mTransFeedTitleViewProperty);
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
        transitImageWrapper();
        fadeOutImageColorFilter();
        fadeOutThumbnailTexts();
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

    private void transitImageWrapper() {
        ObjectAnimator imageWrapperRectAnimator = ObjectAnimator.ofObject(
                this, "imageWrapperRect", new RectEvaluator(new Rect()), mThumbnailStartRect, mThumbnailEndRect);
        imageWrapperRectAnimator.setDuration(mImageTranslationAnimationDuration);
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
        mTopTextLayout.setVisibility(View.VISIBLE);
        ObjectAnimator topNewsTextLayoutHeightAnimator = ObjectAnimator.ofInt(
                this, "TopTextLayoutHeight", 0, mTopTextLayoutLocalVisibleRect.height());
        topNewsTextLayoutHeightAnimator.setDuration(mImageScaleAnimationDuration);

        topNewsTextLayoutHeightAnimator.start();
    }

    private void scaleRecyclerHeight() {
        mRecyclerView.setVisibility(View.VISIBLE);

//        prepareRecyclerChildAnimation();

        ObjectAnimator animator = ObjectAnimator.ofInt(
                this, "recyclerViewHeight", 0, mRecyclerGlobalVisibleRect.height());
        animator.setDuration(mDebugTempDuration);
        animator.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mListener.onRecyclerScaleAnimationEnd();
                NLLog.now("mRecyclerAnimatingLocalVisibleRect : " + mRecyclerAnimatingLocalVisibleRect.toShortString());
                for (int i = 0; i < mRecyclerChildTitleLocalVisibleRects.size(); i++) {
                    Rect titleRect = getRecyclerTitleRect(i);
                    NLLog.now("titleRect idx : " + i);
                    NLLog.now("titleRect : " + titleRect.toShortString());
                }
                for (int i = 0; i < mRecyclerChildDescriptionLocalVisibleRects.size(); i++) {
                    Rect descriptionRect = getRecyclerDescriptionRect(i);
                    NLLog.now("descriptionRect idx : " + i);
                    NLLog.now("descriptionRect : " + descriptionRect.toShortString());
                }
            }
        });

        animator.start();
    }

    private void prepareRecyclerChildAnimation() {
        for (int i = 0 ; i < mRecyclerChildCountToAnimate; i++) {
            View title = getTitleViewFromRecyclerChildAt(i);
            View description = getDescriptionViewFromRecyclerChildAt(i);

//            child.setHasTransientState(true);
            title.setAlpha(0.0f);
            description.setAlpha(0.0f);
        }
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
        setRecyclerWidth(width);
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

    private void setRecyclerWidth(int width) {
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
        if (readyToAnimateTopTitle()) {
            fadeInTopTitle();
        }
        if (readyToAnimateTopDescription()) {
            fadeInTopDescription();
        }
    }

    private boolean readyToAnimateTopTitle() {
        return !mIsAnimatingTopTitleFadeIn
                && mTopTextLayout.getVisibility() == View.VISIBLE
                && mTopTextLayoutAnimatingLocalVisibleRect.contains(mTopTitleLocalVisibleRect);
    }

    private boolean readyToAnimateTopDescription() {
        return !mIsAnimatingTopDescriptionFadeIn
                && mTopTextLayout.getVisibility() == View.VISIBLE
                && mTopTextLayoutAnimatingLocalVisibleRect.contains(mTopDescriptionLocalVisibleRect);
    }

    private void fadeInTopTitle() {
        mIsAnimatingTopTitleFadeIn = true;

        mTopTitleTextView.animate()
                .setDuration(mDebugTempDuration)
                .alpha(1.0f);
    }

    private void fadeInTopDescription() {
        mIsAnimatingTopDescriptionFadeIn = true;

        mTopDescriptionTextView.animate()
                .setDuration(mDebugTempDuration)
                .alpha(1.0f);
    }

    private void animateRecyclerChildIfSizeSufficient() {
//        for (int i = 0; i < mRecyclerLayoutManager.getChildCount(); i++) {
//            if (!isAnimatingRecyclerTitleAt(i)) {
//                getTitleViewFromRecyclerChildAt(i).setAlpha(0.0f);
//            }
//            if (!isAnimatingRecyclerDescriptionAt(i)) {
//                getDescriptionViewFromRecyclerChildAt(i).setAlpha(0.0f);
//            }
//        }
//        for (int i = 0 ; i < mRecyclerChildTitleLocalVisibleRects.size(); i++) {
//            if (!isAnimatingRecyclerTitleAt(i)
//                    && isRectPartiallyVisibleInRecyclerView(getRecyclerTitleRect(i))) {
//                View viewToAnimate = getTitleViewFromRecyclerChildAt(i);
//                viewToAnimate.setAlpha(0.0f);
//            }
//        }
//        for (int i = 0 ; i < mRecyclerChildDescriptionLocalVisibleRects.size(); i++) {
//            if (!isAnimatingRecyclerDescriptionAt(i)
//                    && isRectPartiallyVisibleInRecyclerView(getRecyclerDescriptionRect(i))) {
//                View viewToAnimate_ = getDescriptionViewFromRecyclerChildAt(i);
//                viewToAnimate_.setAlpha(0.0f);
//            }
//        }

//        for (int i = 0; i < mRecyclerChildViewLocalVisibleRects.size(); i++) {
//            Rect childRect = mRecyclerChildViewLocalVisibleRects.get(i);
//            if (isRectPartiallyVisibleInRecyclerView(childRect)
//                    && !mIsAnimatingRecyclerChildArray.get(i)) {
//                getTitleViewFromRecyclerChildAt(i).setAlpha(0.0f);
//                getDescriptionViewFromRecyclerChildAt(i).setAlpha(0.0f);
//                mIsAnimatingRecyclerChildArray.put(i, true);
//            }
//        }
        for (int i = 0 ; i < mRecyclerChildTitleLocalVisibleRects.size(); i++) {
            if (!isAnimatingRecyclerTitleAt(i)
                    && isRectPartiallyVisibleInRecyclerView(getRecyclerTitleRect(i))) {
                View viewToAnimate = getTitleViewFromRecyclerChildAt(i);
                viewToAnimate.setAlpha(0.0f);
            }
            if (readyToAnimateRecyclerChildTitleAt(i)) {
                NLLog.now("animate title : " + i);
                fadeInRecyclerTitleAt(i);
            }
        }
        for (int i = 0 ; i < mRecyclerChildDescriptionLocalVisibleRects.size(); i++) {
            if (!isAnimatingRecyclerDescriptionAt(i)
                    && isRectPartiallyVisibleInRecyclerView(getRecyclerDescriptionRect(i))) {
                View viewToAnimate = getDescriptionViewFromRecyclerChildAt(i);
                viewToAnimate.setAlpha(0.0f);
            }
            if (readyToAnimateRecyclerChildDescriptionAt(i)) {
                NLLog.now("animate desc : " + i);
                fadeInRecyclerDescriptionAt(i);
            }
        }
    }

    private boolean readyToAnimateRecyclerChildTitleAt(int index) {
        Rect rectToInspect = getRecyclerTitleRect(index);
        boolean isAnimating = isAnimatingRecyclerTitleAt(index);
        boolean isRecyclerViewVisible = isRecyclerViewVisible();
        return !isAnimating && isRecyclerViewVisible
                && isRectEnoughToAnimateInRecyclerView(rectToInspect);
    }

    private boolean readyToAnimateRecyclerChildDescriptionAt(int index) {
        Rect rectToInspect = getRecyclerDescriptionRect(index);
        boolean isAnimating = isAnimatingRecyclerDescriptionAt(index);
        boolean isRecyclerViewVisible = isRecyclerViewVisible();
        return !isAnimating && isRecyclerViewVisible
                && isRectEnoughToAnimateInRecyclerView(rectToInspect);
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
        return mRecyclerLayoutManager.getChildAt(i);
    }

    private View getTitleViewFromRecyclerChildAt(int index) {
        return getItemViewFromRecyclerViewAt(index)
                .findViewById(R.id.detail_bottom_news_item_title);
    }

    private View getDescriptionViewFromRecyclerChildAt(int index) {
        return getItemViewFromRecyclerViewAt(index)
                .findViewById(R.id.detail_bottom_news_item_description);
    }

    private boolean isRectEnoughToAnimateInRecyclerView(Rect rectToInspect) {
        if (isRectIntersectsWithRecyclerViewBottom(rectToInspect)) {
            return isRectPartiallyVisibleInRecyclerView(rectToInspect);
        } else {
            return isRectFullyVisibleInRecyclerView(rectToInspect);
        }
    }

    private boolean isRectFullyVisibleInRecyclerView(Rect rectToInspect) {
        return mRecyclerAnimatingLocalVisibleRect.contains(rectToInspect);
    }

    private boolean isRectIntersectsWithRecyclerViewBottom(Rect rectToInspect) {
        return
//                (rectToInspect.top > mRecyclerGlobalVisibleRect.bottom) &&
                        isRectPartiallyVisibleInRecyclerView(rectToInspect);
    }

    private boolean isRectPartiallyVisibleInRecyclerView(Rect rectToInspect) {
        return !isRectFullyVisibleInRecyclerView(rectToInspect)
                && Rect.intersects(mRecyclerAnimatingLocalVisibleRect, rectToInspect);
//        return mRecyclerAnimatingLocalVisibleRect.intersect(rectToInspect);
    }

    private void fadeInRecyclerTitleAt(int index) {
        mIsAnimatingRecyclerChildTitleArray.put(index, true);

        final View childView = getItemViewFromRecyclerViewAt(index);
        NLLog.now("childView : " + childView);
//        childView.setHasTransientState(true);

        View viewToAnimate = getTitleViewFromRecyclerChildAt(index);
        viewToAnimate.animate()
                .setDuration(mDebugTempDuration)
                .alpha(1.0f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
//                        childView.setHasTransientState(false);
                    }
                });
    }

    private void fadeInRecyclerDescriptionAt(int index) {
        mIsAnimatingRecyclerChildDescriptionArray.put(index, true);

        final View childView = getItemViewFromRecyclerViewAt(index);
        NLLog.now("childView : " + childView);
//        childView.setHasTransientState(true);

        View viewToAnimate = getDescriptionViewFromRecyclerChildAt(index);
        viewToAnimate.animate()
                .setDuration(mDebugTempDuration)
                .alpha(1.0f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
//                        childView.setHasTransientState(false);
                    }
                });
    }

    private void fadeInToolbar() {
        mToolbar.animate().alpha(1.0f).setDuration(mToolbarAnimationDuration);
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
        mRecyclerView = mActivity.getBottomNewsListRecyclerView();
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
        mRecyclerChildCountToAnimate = 0;
        for (int i = 0 ; i < childCount; i++) {
            View child = getItemViewFromRecyclerViewAt(i);
//            View child = mRecyclerLayoutManager.getChildAt(i);

            Rect childRect = new Rect(
                    child.getLeft(),
                    child.getTop(),
                    child.getRight(),
                    child.getBottom()
            );
            if (isRecyclerChildRectPartiallyOrFullyVisible(childRect)) {
                break;
            }
            mRecyclerChildViewLocalVisibleRects.add(childRect);
            mIsAnimatingRecyclerChildArray.put(i, false);

            putRecyclerChildTitleAndDescriptionAt(i, offsetFromRecyclerTop);

            mIsAnimatingRecyclerChildTitleArray.put(i, false);
            mIsAnimatingRecyclerChildDescriptionArray.put(i, false);

            offsetFromRecyclerTop += child.getHeight();
            mRecyclerChildCountToAnimate++;
        }
    }

    private boolean isRecyclerChildRectPartiallyOrFullyVisible(Rect childRect) {
        return childRect.top > mRecyclerGlobalVisibleRect.height();
    }

    private void putRecyclerChildTitleAndDescriptionAt(int index, int offsetFromRecyclerTop) {
        View title = getTitleViewFromRecyclerChildAt(index);
        View description = getDescriptionViewFromRecyclerChildAt(index);
        Rect titleRect = new Rect(
                title.getLeft() - title.getPaddingLeft(),
                title.getTop() - title.getPaddingTop() + offsetFromRecyclerTop,
                title.getRight() + title.getPaddingRight(),
                title.getBottom() + title.getPaddingBottom() + offsetFromRecyclerTop
        );
        Rect descriptionRect = new Rect(
                description.getLeft() - description.getPaddingLeft(),
                description.getTop() - description.getPaddingTop() + offsetFromRecyclerTop,
                description.getRight() + description.getPaddingRight(),
                description.getBottom() + description.getPaddingBottom() + offsetFromRecyclerTop
        );

        mRecyclerChildTitleLocalVisibleRects.add(titleRect);
        mRecyclerChildDescriptionLocalVisibleRects.add(descriptionRect);
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
        float scaleRatio = fitWidth ? widthScaleRatio : heightScaleRatio;

        int targetWidth = (int)(mTransImageViewProperty.getWidth() * scaleRatio);
        int imageWrapperLeft = mTopNewsImageWrapper.getLeft();
        int thumbnailLeftTarget = fitWidth
                ? imageWrapperLeft
                : imageWrapperLeft - (targetWidth - mTopNewsImageWrapper.getWidth())/2;
        int thumbnailTopTarget = mTopNewsImageWrapper.getTop();

        mThumbnailEndRect = new Rect(thumbnailLeftTarget, thumbnailTopTarget,
                thumbnailLeftTarget + (int)(mTransImageViewProperty.getWidth() * scaleRatio),
                thumbnailTopTarget + (int)(mTransImageViewProperty.getHeight() * scaleRatio));
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

    private void fadeOutImageColorFilter() {
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
}
