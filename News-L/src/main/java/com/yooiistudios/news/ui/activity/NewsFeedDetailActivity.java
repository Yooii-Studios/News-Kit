package com.yooiistudios.news.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.news.R;
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
import com.yooiistudios.news.ui.fragment.NewsSelectFragment;
import com.yooiistudios.news.ui.itemanimator.DetailNewsItemAnimator;
import com.yooiistudios.news.ui.widget.ObservableScrollView;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.NLLog;
import com.yooiistudios.news.util.ScreenUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_IMAGE_VIEW_LOCATION_HEIGHT;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_IMAGE_VIEW_LOCATION_LEFT;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_IMAGE_VIEW_LOCATION_TOP;
import static com.yooiistudios.news.ui.activity.MainActivity.INTENT_KEY_IMAGE_VIEW_LOCATION_WIDTH;

public class NewsFeedDetailActivity extends Activity
        implements NewsFeedDetailAdapter.OnItemClickListener, ObservableScrollView.Callbacks,
        RecyclerView.ItemAnimator.ItemAnimatorFinishedListener,
        NewsFeedDetailNewsFeedFetchTask.OnFetchListener,
        NewsFeedDetailNewsImageUrlFetchTask.OnImageUrlFetchListener {
    @InjectView(R.id.detail_actionbar_overlay_view)         View mActionBarOverlayView;
    @InjectView(R.id.detail_top_overlay_view)               View mTopOverlayView;
    @InjectView(R.id.detail_scrollView)                     ObservableScrollView mScrollView;
    @InjectView(R.id.news_detail_swipe_refresh_layout)      SwipeRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.detail_loading_cover)                  View mLoadingCoverView;

    // Top
    @InjectView(R.id.detail_top_content_layout)             RelativeLayout mTopContentLayout;
    @InjectView(R.id.detail_top_news_image_ripple_view)     View mTopNewsImageRippleView;
    @InjectView(R.id.detail_top_news_image_view)            ImageView mTopImageView;
    @InjectView(R.id.detail_top_news_text_layout)           LinearLayout mTopNewsTextLayout;
    @InjectView(R.id.detail_top_news_text_ripple_layout)    LinearLayout mTopNewsTextRippleLayout;
    @InjectView(R.id.detail_top_news_title_text_view)       TextView mTopTitleTextView;
    @InjectView(R.id.detail_top_news_description_text_view) TextView mTopDescriptionTextView;
    // Bottom
    @InjectView(R.id.detail_bottom_news_recycler_view)      RecyclerView mBottomNewsListRecyclerView;

    private static final int BOTTOM_NEWS_ANIM_DELAY_UNIT_MILLI = 60;
    private static final int TOP_NEWS_FILTER_ANIM_DURATION_UNIT_MILLI = 400;
    private static final String TAG = NewsFeedDetailActivity.class.getName();
    public static final String INTENT_KEY_NEWS = "INTENT_KEY_NEWS";
    public static final String INTENT_KEY_NEWSFEED_REPLACED = "INTENT_KEY_NEWSFEED_REPLACED";
    public static final String INTENT_KEY_IMAGE_LOADED = "INTENT_KEY_IMAGE_LOADED";
    public static final String INTENT_KEY_IMAGE_URL = "INTENT_KEY_IMAGE_URL";

    public static final int REQ_SELECT_NEWS_FEED = 13841;

    private Palette mPalette;

    private ImageLoader mImageLoader;

    private NewsFeed mNewsFeed;
    private News mTopNews;
    private Bitmap mTopImageBitmap;
    private NewsFeedDetailAdapter mAdapter;
    private TintType mTintType;

    private boolean mIsRefreshing = false;
    private boolean mHasAnimatedColorFilter = false;

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

        // TODO ConcurrentModification 문제 우회를 위해 애니메이션이 끝나기 전 스크롤을 막던지 처리 해야함.
        applySystemWindowsBottomInset(R.id.detail_scroll_content_wrapper);
        initActionBar();
        initSwipeRefreshView();
        initCustomScrollView();
        initTopNews();
        initBottomNewsList();
        initLoadingCoverView();

        mTopImageView.bringToFront();
        mTopContentLayout.bringToFront();

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (savedInstanceState == null && mTopImageView.getDrawable() != null) {
            ViewTreeObserver observer = mTopImageView.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    mTopImageView.getViewTreeObserver().removeOnPreDrawListener(this);

                    Bundle extras = getIntent().getExtras();
                    int thumbnailLeft = extras.getInt(INTENT_KEY_IMAGE_VIEW_LOCATION_LEFT);
                    int thumbnailTop = extras.getInt(INTENT_KEY_IMAGE_VIEW_LOCATION_TOP);
                    int thumbnailWidth = extras.getInt(INTENT_KEY_IMAGE_VIEW_LOCATION_WIDTH);
                    int thumbnailHeight = extras.getInt(INTENT_KEY_IMAGE_VIEW_LOCATION_HEIGHT);

                    // Figure out where the thumbnail and full size versions are, relative
                    // to the screen and each other
                    int[] screenLocation = new int[2];
                    mTopImageView.getLocationOnScreen(screenLocation);
                    int leftDelta = thumbnailLeft - screenLocation[0];
                    int topDelta = thumbnailTop - screenLocation[1];

                    // Scale factors to make the large version the same size as the thumbnail
                    float widthScale = (float) thumbnailWidth / mTopImageView.getWidth();
                    float heightScale = (float) thumbnailHeight / mTopImageView.getHeight();

                    runEnterAnimation(leftDelta, topDelta, widthScale, heightScale);

                    return true;
                }
            });
//        }
//        if (mTopImageView.getDrawable() != null) {
//            hideLoadingCover();
//            getWindow().getEnterTransition().addListener(new TransitionAdapter() {
//                @Override
//                public void onTransitionEnd(Transition transition) {
//                    animateTopImageViewColorFilter();
//
//                    getWindow().getEnterTransition().removeListener(this);
//                }
//            });
        } else {
            showLoadingCover();
        }
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    public void runEnterAnimation(int leftDelta, int topDelta, float widthScale,
                                  float heightScale) {
        // TODO duration 바꾸기
        final long duration = 1000;

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        mTopImageView.setPivotX(0);
        mTopImageView.setPivotY(0);
        mTopImageView.setScaleX(widthScale);
        mTopImageView.setScaleY(heightScale);
        mTopImageView.setTranslationX(leftDelta);
        mTopImageView.setTranslationY(topDelta);

        // We'll fade the text in later
//        mTextView.setAlpha(0);

        // Animate scale and translation to go from thumbnail to full size
        // TODO interpolator 바꾸기
        mTopImageView.animate().setDuration(duration).
                scaleX(1).scaleY(1).
                translationX(0).translationY(0).
                setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(new Runnable() {
                    public void run() {
                        // Animate the description in after the image animation
                        // is done. Slide and fade the text in from underneath
                        // the picture.
//                        mTextView.setTranslationY(-mTextView.getHeight());
//                        mTextView.animate().setDuration(duration/2).
//                                translationY(0).alpha(1).
//                                setInterpolator(sDecelerator);

                        animateTopImageViewColorFilter();
                    }
                })
        ;

        // Fade in the black background
        mBottomNewsListRecyclerView.setAlpha(0);
        mBottomNewsListRecyclerView.animate().setDuration(duration).alpha(1);
//        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
//        bgAnim.setDuration(duration);
//        bgAnim.start();

        // Animate a color filter to take the image from grayscale to full color.
        // This happens in parallel with the image scaling and moving into place.
//        ObjectAnimator colorizer = ObjectAnimator.ofFloat(PictureDetailsActivity.this,
//                "saturation", 0, 1);
//        colorizer.setDuration(duration);
//        colorizer.start();

        // Animate a drop-shadow of the image
//        ObjectAnimator shadowAnim = ObjectAnimator.ofFloat(mShadowLayout, "shadowDepth", 0, 1);
//        shadowAnim.setDuration(duration);
//        shadowAnim.start();
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
        color.setDuration(TOP_NEWS_FILTER_ANIM_DURATION_UNIT_MILLI).start();
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
        color.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finishAfterTransition();
            }
        });
        color.setDuration(TOP_NEWS_FILTER_ANIM_DURATION_UNIT_MILLI);
        color.start();
    }

    @Override
    public void onBackPressed() {
        if (mTopImageView.getDrawable() == null) {
            super.onBackPressed();
            return;
        }
        darkenHeroImage();
    }

    private void initActionBar() {
        initActionBarGradientView();
        applyActionBarTitle();
    }

    private void applyActionBarTitle() {
        if (getActionBar() != null && mNewsFeed != null) {
            getActionBar().setTitle(mNewsFeed.getTitle());
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
                return mIsRefreshing;
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

        notifyBottomNewsChanged();
    }
    private void notifyBottomNewsChanged() {
        mAdapter = new NewsFeedDetailAdapter(this, this);

        mBottomNewsListRecyclerView.setAdapter(mAdapter);

        // make bottom news array list. EXCLUDE top news.
//        mBottomNewsList = new ArrayList<NLNews>(mNewsFeed.getNewsList());

        final int newsCount = mNewsFeed.getNewsList().size();
        for (int i = 0; i < newsCount; i++) {
            final News news = mNewsFeed.getNewsList().get(i);
            final int idx = i;
            mBottomNewsListRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.addNews(news);

                    if (idx == (mNewsFeed.getNewsList().size() - 1)) {
                        mBottomNewsListRecyclerView.getItemAnimator()
                                .isRunning(NewsFeedDetailActivity.this);
                    }
                }
            }, BOTTOM_NEWS_ANIM_DELAY_UNIT_MILLI * i + 1);
        }

        applyMaxBottomRecyclerViewHeight();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.news_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finishAfterTransition();
                return true;

            case R.id.action_settings:
                startActivityForResult(new Intent(NewsFeedDetailActivity.this, NewsSelectActivity.class),
                        REQ_SELECT_NEWS_FEED);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applyMaxBottomRecyclerViewHeight() {
        int maxRowHeight = NewsFeedDetailAdapter.measureMaximumRowHeight(getApplicationContext());
        NLLog.now("maxRowHeight : " + maxRowHeight);

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
                .setDuration(250)
                .alpha((Float)mTopOverlayView.getTag())
                .setInterpolator(new DecelerateInterpolator());
        mActionBarOverlayView.animate()
                .setDuration(250)
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
        mTopOverlayView.setTag(mTopOverlayView.getAlpha());
        mTopOverlayView.animate()
                .setDuration(250)
                .alpha(0f)
                .setInterpolator(new DecelerateInterpolator());
        mActionBarOverlayView.setTag(mActionBarOverlayView.getAlpha());
        mActionBarOverlayView.animate()
                .setDuration(250)
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
        // TODO 공식 문서가 release 된 후 palette.get~ 메서드가 null 을 반환할 가능성이 있는지 체크
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
        mTopContentLayout.setBackground(new ColorDrawable(colorWithoutAlpha));
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
        NLLog.now("detail bottom onItemClick");

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
                    view.setPadding(0, 0, 0, windowInsets.getSystemWindowInsetBottom());
                } else {
                    view.setPadding(0, 0, windowInsets.getSystemWindowInsetRight(), 0);
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
            mTopImageView.setTranslationY(scrollY * 0.4f);

            mActionBarOverlayView.setAlpha(scrollY * 0.0005f);
            if (mActionBarOverlayView.getAlpha() >= 0.6f) {
                mActionBarOverlayView.setAlpha(0.6f);
            }
        } else {
            mTopImageView.setTranslationY(0);
            if (mActionBarOverlayView.getAlpha() != 0) {
                mActionBarOverlayView.setAlpha(0);
            }
        }
    }

    /**
     * RecyclerView.ItemAnimator.ItemAnimatorFinishedListener
     */
    @Override
    public void onAnimationsFinished() {
        int totalHeight = 0;
        int childCount = mBottomNewsListRecyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            totalHeight += mBottomNewsListRecyclerView.getChildAt(i).getHeight();
        }

        mBottomNewsListRecyclerView.getLayoutParams().height = totalHeight;
        mAdapter.notifyDataSetChanged();
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
        NLLog.now("onActivityResult-req:" + requestCode + "/result:" + resultCode);
    }
}
