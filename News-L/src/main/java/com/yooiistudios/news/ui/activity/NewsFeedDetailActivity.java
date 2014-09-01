package com.yooiistudios.news.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.news.NewsApplication;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedUtils;
import com.yooiistudios.news.model.news.TintType;
import com.yooiistudios.news.ui.adapter.NewsFeedDetailAdapter;
import com.yooiistudios.news.ui.adapter.TransitionAdapter;
import com.yooiistudios.news.ui.itemanimator.DetailNewsItemAnimator;
import com.yooiistudios.news.ui.widget.ObservableScrollView;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.NLLog;
import com.yooiistudios.news.util.ScreenUtils;
import com.yooiistudios.news.util.WebUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NewsFeedDetailActivity extends Activity
        implements NewsFeedDetailAdapter.OnItemClickListener,
        ObservableScrollView.Callbacks, ImageLoader.ImageListener,
        RecyclerView.ItemAnimator.ItemAnimatorFinishedListener {
    @InjectView(R.id.detail_actionbar_overlay_view)         View mActionBarOverlayView;
    @InjectView(R.id.detail_top_overlay_view)               View mTopOverlayView;
    @InjectView(R.id.detail_scrollView)                     ObservableScrollView mScrollView;
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

    private Palette mPalette;

    private ImageLoader mImageLoader;

    private NewsFeed mNewsFeed;
    private News mTopNews;
    private Bitmap mTopImageBitmap;
    private NewsFeedDetailAdapter mAdapter;
    private TintType mTintType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        ButterKnife.inject(this);

        mImageLoader = new ImageLoader(((NewsApplication)getApplication()).getRequestQueue(),
                ImageMemoryCache.getInstance(getApplicationContext()));

        // retrieve feed from intent
        mNewsFeed = getIntent().getExtras().getParcelable(NewsFeed.KEY_NEWS_FEED);
        Object tintTypeObj = getIntent().getExtras().getSerializable(MainActivity.INTENT_KEY_TINT_TYPE);
        mTintType = tintTypeObj != null ? (TintType)tintTypeObj : TintType.GRAYSCALE;

        int topNewsIndex = getIntent().getExtras().getInt(News.KEY_NEWS);
        if (topNewsIndex < mNewsFeed.getNewsList().size()) {
            mTopNews = mNewsFeed.getNewsList().remove(topNewsIndex);
        }
        String imageViewName = getIntent().getExtras().getString(MainActivity
                .INTENT_KEY_VIEW_NAME_IMAGE, null);

        // set view name to animate
        mTopImageView.setViewName(imageViewName);

        // TODO ConcurrentModification 문제 우회를 위해 애니메이션이 끝나기 전 스크롤을 막던지 처리 해야함.
        applySystemWindowsBottomInset(R.id.detail_scrollView);
        initActionBar();
        initCustomScrollView();
        initTopNews();
        initBottomNewsList();


        getWindow().getEnterTransition().addListener(new TransitionAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
//                mUseGrayFilter
                ObjectAnimator color = ObjectAnimator.ofArgb(mTopImageView.getColorFilter(), "color", 0);
                color.addUpdateListener(new ColorFilterListener(mTopImageView));
                color.setDuration(TOP_NEWS_FILTER_ANIM_DURATION_UNIT_MILLI).start();

                getWindow().getEnterTransition().removeListener(this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        int filterColor;
        int alpha;
        switch (mTintType) {
            case PALETTE:
                filterColor = mPalette.getDarkVibrantColor().getRgb();
                alpha = getResources().getInteger(R.integer.vibrant_color_tint_alpha);
                break;
            case DUMMY:
            case GRAYSCALE:
            default:
                filterColor = NewsFeedUtils.getGrayFilterColor();
                alpha = Color.alpha(filterColor);
                break;
        }
//        if (mUseGrayFilter) {
//            filterColor = NewsFeedUtils.getGrayFilterColor();
//            alpha = Color.alpha(filterColor);
//        } else {
//            filterColor = mPalette.getDarkVibrantColor().getRgb();
//            alpha = getResources().getInteger(R.integer.vibrant_color_tint_alpha);
//        }
        int red = Color.red(filterColor);
        int green = Color.green(filterColor);
        int blue = Color.blue(filterColor);

        ObjectAnimator color = ObjectAnimator.ofArgb(mTopImageView.getColorFilter(), "color",
                Color.argb(alpha, red, green, blue));

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

    private void initActionBar() {
        initActionBarGradientView();

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

    private void initCustomScrollView() {
        mScrollView.addCallbacks(this);
    }

    private void initTopNews() {
        mTopTitleTextView.setAlpha(0);
        mTopDescriptionTextView.setAlpha(0);

//        mTopNews = mNewsFeed.getNewsListContainsImageUrl().get(0);
        if (mTopNews != null) {
            loadTopNews();
        } else {
            //TODO when NLNewsFeed is invalid.
        }
    }

    private void initBottomNewsList() {
        //init ui

        final RecyclerView.ItemAnimator itemAnimator;

        mBottomNewsListRecyclerView.setHasFixedSize(true);
        mBottomNewsListRecyclerView.setItemAnimator(
                itemAnimator = new DetailNewsItemAnimator(mBottomNewsListRecyclerView));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mBottomNewsListRecyclerView.setLayoutManager(layoutManager);

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
                        itemAnimator.isRunning(NewsFeedDetailActivity.this);
                    }
                }
            }, BOTTOM_NEWS_ANIM_DELAY_UNIT_MILLI * i + 1);
        }

        applyMaxBottomRecyclerViewHeight();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finishAfterTransition();
        return super.onOptionsItemSelected(item);
    }

    private void applyMaxBottomRecyclerViewHeight() {
        int maxRowHeight = NewsFeedDetailAdapter.measureMaximumRowHeight(getApplicationContext());
        NLLog.now("maxRowHeight : " + maxRowHeight);

        int newsListCount = mNewsFeed.getNewsList().size();
        mBottomNewsListRecyclerView.getLayoutParams().height =
                maxRowHeight * newsListCount;
    }

    private void loadTopNews() {
//        final ImageMemoryCache cache = ImageMemoryCache.INSTANCE;
        final ImageMemoryCache cache = ImageMemoryCache.getInstance
                (getApplicationContext());

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

        // set image
        String imgUrl = mTopNews.getImageUrl();
        Bitmap bitmap;
        if (imgUrl != null) {
            ImageLoader.ImageContainer imageContainer =
                    mImageLoader.get(imgUrl, this);
            bitmap = imageContainer.getBitmap();
            if (bitmap == null) {
                bitmap = NewsFeedUtils.getDummyNewsImage(getApplicationContext());
            }
        } else {
            bitmap = NewsFeedUtils.getDummyNewsImage(getApplicationContext());
        }

        setTopNewsImageBitmap(bitmap);
        colorize();

        mTopNewsImageRippleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NLLog.now("mTopNewsImageRippleView onClink");
                WebUtils.openLink(NewsFeedDetailActivity.this, mTopNews.getLink());
            }
        });

        mTopNewsTextRippleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NLLog.now("mTopNewsTextRippleLayout onClink");
                WebUtils.openLink(NewsFeedDetailActivity.this, mTopNews.getLink());
            }
        });

        animateTopItems();
    }

    private void setTopNewsImageBitmap(Bitmap bitmap) {
        mTopImageBitmap = bitmap;
        mTopImageView.setImageBitmap(mTopImageBitmap);
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

    private void colorize() {
        mPalette = Palette.generate(mTopImageBitmap);
        applyPalette();
    }

    private void applyPalette() {
        // TODO 공식 문서가 release 된 후 palette.get~ 메서드가 null 을 반환할 가능성이 있는지 체크
        PaletteItem lightVibrantColor = mPalette.getLightVibrantColor();
        PaletteItem darkVibrantColor = mPalette.getDarkVibrantColor();

        mTopTitleTextView.setTextColor(Color.WHITE);

        if (lightVibrantColor != null) {
            mTopDescriptionTextView.setTextColor(lightVibrantColor.getRgb());
        }

        int color;
        int alpha;

        switch(mTintType) {
            case DUMMY:
                color = NewsFeedUtils.getDummyImageFilterColor();
                alpha = Color.alpha(color);
                break;
            case PALETTE:
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
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        mTopContentLayout.setBackground(new ColorDrawable(color));
        mTopNewsTextLayout.setBackground(new ColorDrawable(color));

        mTopImageView.setColorFilter(Color.argb(alpha, red, green, blue));
    }

    @Override
    public void onItemClick(NewsFeedDetailAdapter.ViewHolder viewHolder, News news) {
        NLLog.now("detail bottom onItemClick");
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

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
//                    mTopImageBitmap = cache.getBitmapFromUrl(imgUrl);
//        if (mTopImageBitmap == null) {
//            mTopImageBitmap = response.getBitmap();
//
//            mTopImageView.setImageBitmap(mTopImageBitmap);
//            colorize(mTopImageBitmap);
//        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

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
}