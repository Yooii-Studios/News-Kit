package com.yooiistudios.newsflow.ui.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
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

import java.lang.reflect.Type;

import static com.yooiistudios.newsflow.ui.activity.MainActivity.INTENT_KEY_TRANSITION_PROPERTY;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 24.
 *
 * NewsFeedDetailTransitionUtils
 *  NewsFeedDetailActivity 의 액티비티 트랜지션 애니메이션을 래핑한 클래스
 */
public class NewsFeedDetailTransitionUtils {
    // 액티비티 전환 애니메이션 관련 변수
    private ActivityTransitionImageViewProperty mTransImageViewProperty;
    private ActivityTransitionTextViewProperty mTransTitleViewProperty;
    private ActivityTransitionTextViewProperty mTransFeedTitleViewProperty;

    private Rect mRootClipBound;

    private int mThumbnailLeftDelta;
    private int mThumbnailTopDelta;
    private float mThumbnailScaleRatio;
    private float mThumbnailWidthScaleRatio;
    private float mThumbnailHeightScaleRatio;

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
    private long mToolbarIconAnimationDuration;
    private long mToolbarBgAnimationDuration;

    private NewsFeedDetailActivity mActivity;

    private Toolbar mToolbar;
    private RelativeLayout mRootLayout;
    private FrameLayout mTransitionLayout;
    private View mToolbarOverlayView;
    private View mTopGradientShadowView;

    // Top
    private FrameLayout mTopNewsImageWrapper;
    private ImageView mTopImageView;
    private LinearLayout mTopNewsTextLayout;
    private TextView mTopTitleTextView;
    private TextView mTopDescriptionTextView;

    // Bottom
    private RecyclerView mBottomNewsListRecyclerView;

    private ColorDrawable mRootLayoutBackground;
    private SpannableString mToolbarTitle;
    private AlphaForegroundColorSpan mToolbarTitleColorSpan;

    public static void runEnterAnimation(NewsFeedDetailActivity activity) {
        new NewsFeedDetailTransitionUtils(activity).requestActivityTransition();
    }

    public static void animateTopOverlayFadeOut(NewsFeedDetailActivity activity) {
        new NewsFeedDetailTransitionUtils(activity).animateTopOverlayFadeOut();
    }

    public static void animateTopOverlayFadeIn(NewsFeedDetailActivity activity) {
        new NewsFeedDetailTransitionUtils(activity).animateTopOverlayFadeIn();
    }

    private NewsFeedDetailTransitionUtils(NewsFeedDetailActivity activity) {
        initViewsAndVariables(activity);
    }

    private void requestActivityTransition() {
        animateOnPreDraw();
    }

    private void animateOnPreDraw() {
        ViewTreeObserver observer = mRootLayout.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRootLayout.getViewTreeObserver().removeOnPreDrawListener(this);

                initTransitionVariablesAfterLocationFix();
                startTransition();

                return true;
            }
        });
    }

    private void startTransition() {
        // TODO 나중에 각자의 애니메이션 부분의 시작 부분으로 옮겨야 함. 그 전까지는 따로 메서드로 추출하지 않음.
        mTopNewsTextLayout.setAlpha(0.0f);
        mBottomNewsListRecyclerView.setAlpha(0.0f);
        mRootLayoutBackground.setAlpha(0);
        mToolbar.setAlpha(0.0f);

        revealRootView();
        translateImage();
        scaleImage();
    }

    private void revealRootView() {
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mRootLayoutBackground, "alpha", 0, 255);
        bgAnim.setDuration(mRootViewTranslationAnimationDuration);
        bgAnim.start();
    }

    private void translateImage() {
        mTopNewsImageWrapper.setPivotX(0.0f);
        mTopNewsImageWrapper.setPivotY(0.0f);
        mTopNewsImageWrapper.setTranslationX(mThumbnailLeftDelta);
        mTopNewsImageWrapper.setTranslationY(mThumbnailTopDelta);
        mTopNewsImageWrapper.animate()
                .translationX(0)
                .translationY(0)
                .setDuration(mRootViewTranslationAnimationDuration);
    }

    private void scaleImage() {
        ObjectAnimator imageWrapperSizeAnimator = ObjectAnimator.ofFloat(
                this, "ImageWrapperSize", 1.0f, mThumbnailScaleRatio);
        imageWrapperSizeAnimator.setDuration(mImageScaleAnimationDuration);
        imageWrapperSizeAnimator.start();
    }

    private void initViewsAndVariables(NewsFeedDetailActivity activity) {
        initVariables(activity);
        initViews();
    }

    private void initVariables(NewsFeedDetailActivity activity) {
        mActivity = activity;
        mRootLayoutBackground = activity.getRootLayoutBackground();
        mToolbarTitle = activity.getToolbarTitle();
        mToolbarTitleColorSpan = activity.getToolbarTitleColorSpan();
    }

    private void initViews() {
        mToolbar = mActivity.getToolbar();
        mRootLayout = mActivity.getRootLayout();
        mTransitionLayout = mActivity.getTransitionLayout();
        mToolbarOverlayView = mActivity.getToolbarOverlayView();
        mTopGradientShadowView = mActivity.getTopGradientShadowView();

        // Top
        mTopNewsImageWrapper = mActivity.getTopNewsImageWrapper();
        mTopImageView = mActivity.getTopImageView();
        mTopNewsTextLayout = mActivity.getTopNewsTextLayout();
        mTopTitleTextView = mActivity.getTopTitleTextView();
        mTopDescriptionTextView = mActivity.getTopDescriptionTextView();

        // Bottom
        mBottomNewsListRecyclerView = mActivity.getBottomNewsListRecyclerView();
    }

    private void initTransitionVariablesAfterLocationFix() {
        // 액티비티 전환 관련 변수
        Bundle extras = mActivity.getIntent().getExtras();

        extractActivityTransitionProperties(extras);
        initImageTranslationVariables();
        initImageScaleVariables();
        initDurationVariabless();
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
    }

    private void initDurationVariabless() {
        // 애니메이션 속도 관련 변수
        Resources resources = mActivity.getResources();
        mImageFilterAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_image_filter_duration_milli) * sAnimatorScale;
        mImageScaleAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_image_scale_duration_milli) * sAnimatorScale;
        mRootViewHorizontalScaleAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_root_horizontal_scale_duration_milli) * sAnimatorScale;
        mRootViewVerticalScaleAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_root_vertical_scale_duration_milli) * sAnimatorScale;
        mRootViewTranslationAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_root_translation_duration_milli) * sAnimatorScale;
        mThumbnailTextAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_thumbnail_text_duration_milli) * sAnimatorScale;
        mToolbarIconAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_action_bar_content_duration_milli) * sAnimatorScale;
        mToolbarBgAnimationDuration = resources.getInteger(
                R.integer.news_feed_detail_action_bar_bg_duration_milli) * sAnimatorScale;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void darkenHeroImage() {
        int filterColor = mActivity.getFilterColor();

        int red = Color.red(filterColor);
        int green = Color.green(filterColor);
        int blue = Color.blue(filterColor);

        ObjectAnimator color = ObjectAnimator.ofArgb(mTopImageView.getColorFilter(), "color",
                Color.argb(Color.alpha(filterColor), red, green, blue));

        color.addUpdateListener(new ColorFilterListener(mTopImageView));
        color.setDuration(mImageFilterAnimationDuration);
        color.start();
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

    private void animateTopOverlayFadeIn() {
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

    private void animateTopOverlayFadeOut() {
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
     * runEnterAnimation 에서 이미지뷰 wrapper 스케일링에 사용될 메서드.
     * @param scale 계산된 사이즈
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setImageWrapperSize(float scale) {
        ViewGroup.LayoutParams lp = mTopNewsImageWrapper.getLayoutParams();
        lp.width = (int)(mTransImageViewProperty.getWidth() * scale);
        lp.height = (int)(mTransImageViewProperty.getHeight() * scale);
        mTopNewsImageWrapper.setLayoutParams(lp);
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateTopImageViewColorFilter() {
        int filterColor = mActivity.getFilterColor();

        int red = Color.red(filterColor);
        int green = Color.green(filterColor);
        int blue = Color.blue(filterColor);
        mTopImageView.setColorFilter(Color.argb(Color.alpha(filterColor), red, green, blue));

        ObjectAnimator color = ObjectAnimator.ofArgb(mTopImageView.getColorFilter(), "color", 0);
        color.addUpdateListener(new ColorFilterListener(mTopImageView));
        color.setDuration(mImageFilterAnimationDuration).start();
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
//                    initTransitionVariablesAfterLocationFix();
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
////        mTopNewsTextLayout.setAlpha(0);
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
//        rootClipBoundTranslationAnimator.setDuration(mRootViewTranslationAnimationDuration);
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
//                mTopNewsTextLayout.bringToFront();
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
//        imageWrapperTranslationAnimator.setDuration(mRootViewTranslationAnimationDuration);
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
//                toolbarTitleAnimator.setDuration(mToolbarIconAnimationDuration);
//                toolbarTitleAnimator.setInterpolator(commonInterpolator);
//                toolbarTitleAnimator.start();
//
//                // 액션바 아이콘 페이드인
//                ObjectAnimator toolbarHomeIconAnimator =
//                        ObjectAnimator.ofInt(mToolbarHomeIcon, "alpha", 0, 255);
//                toolbarHomeIconAnimator.setDuration(mToolbarIconAnimationDuration);
//                toolbarHomeIconAnimator.setInterpolator(commonInterpolator);
//                toolbarHomeIconAnimator.start();
//
//                ObjectAnimator toolbarOverflowIconAnimator =
//                        ObjectAnimator.ofInt(mToolbarOverflowIcon, "alpha", 0, 255);
//                toolbarOverflowIconAnimator.setDuration(mToolbarIconAnimationDuration);
//                toolbarOverflowIconAnimator.setInterpolator(commonInterpolator);
//                toolbarOverflowIconAnimator.start();
//
//                animateTopOverlayFadeIn();
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
//                    mTopNewsTextLayout.setAlpha(1);
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
//        animateTopImageViewColorFilter();
//
//        // 상단 뉴스의 텍스트뷰 위치, 알파 애니메이션
////        Point displaySize = new Point();
////        getWindowManager().getDefaultDisplay().getSize(displaySize);
////        final int translationY = displaySize.y - mTopNewsTextLayout.getTop();
////        mTopNewsTextLayout.setAlpha(0);
////        mTopNewsTextLayout.setTranslationY(translationY);
////        mTopNewsTextLayout.animate().
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
//        animateTopItems();
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
//        darkenHeroImage();
//
//        // 상단 뉴스의 텍스트뷰 위치, 알파 애니메이션
//        Point displaySize = new Point();
//        getWindowManager().getDefaultDisplay().getSize(displaySize);
//        final int translationY = displaySize.y - mTopNewsTextLayout.getTop();
//        mTopNewsTextLayout.animate().
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
//        animateTopOverlayFadeOut();
//    }
}
