package com.yooiistudios.news.ui.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.news.NewsApplication;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsFeedUtils;
import com.yooiistudios.news.model.news.TintType;
import com.yooiistudios.news.ui.activity.MainActivity;
import com.yooiistudios.news.ui.activity.NewsFeedDetailActivity;
import com.yooiistudios.news.util.ImageMemoryCache;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 *
 * MainNewsFeedFragment
 *  메인화면 상단의 뷰페이저에 들어갈 프레그먼트
 */
public class MainNewsFeedFragment extends Fragment
        implements View.OnClickListener {
    private static final String KEY_NEWS_FEED = "KEY_NEWS_FEED";
    private static final String KEY_NEWS = "KEY_NEWS";
    private static final String KEY_POSITION = "KEY_TAB_POSITION";

    private ImageLoader mImageLoader;
    private NewsFeed mNewsFeed;
    private News mNews;
    private int mPosition;
    private boolean mRecycled;

    public static MainNewsFeedFragment newInstance(NewsFeed newsFeed,
                                                  News news, int position) {
        MainNewsFeedFragment f = new MainNewsFeedFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(KEY_NEWS_FEED, newsFeed);
        args.putParcelable(KEY_NEWS, news);
        args.putInt(KEY_POSITION, position);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNewsFeed = getArguments().getParcelable(KEY_NEWS_FEED);
            mNews = getArguments().getParcelable(KEY_NEWS);
            mPosition = getArguments().getInt(KEY_POSITION);
        } else {
            mNews = null;
            mPosition = -1;
        }
        mRecycled = false;

        RequestQueue requestQueue = ((NewsApplication) getActivity().getApplication())
                .getRequestQueue();
        Context context = getActivity().getApplicationContext();
        mImageLoader = new ImageLoader(requestQueue,
                ImageMemoryCache.getInstance(context));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.fragment_main_top_viewpager_item,
                container, false);

        ItemViewHolder holder = new ItemViewHolder(root);

        if (mNews == null) {
            holder.imageView.setImageDrawable(null);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.titleTextView.setText("");

            root.setOnClickListener(null);

            return root;
        }

        holder.imageView.setViewName(MainActivity.VIEW_NAME_IMAGE_PREFIX +
                mPosition);
        applyImage(holder);

        holder.titleTextView.setViewName(MainActivity.VIEW_NAME_TITLE_PREFIX +
                mPosition);
        String newsName = mNews.getTitle();
        if (newsName != null) {
            holder.titleTextView.setText(newsName);
        }

        root.setOnClickListener(this);
        root.setTag(holder);

        return root;
    }

    public void applyImage() {
        View root;
        ItemViewHolder viewHolder;
        if ((root = getView()) != null &&
                (viewHolder = (ItemViewHolder)root.getTag()) != null) {
            applyImage(viewHolder);
        }

    }

    private void applyImage(final ItemViewHolder viewHolder) {
        viewHolder.imageView.setImageDrawable(null);

        // set image
        String imgUrl = mNews.getImageUrl();
        if (imgUrl != null) {
            mImageLoader.get(imgUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bitmap;

                    viewHolder.progressBar.setVisibility(View.GONE);

                    if (!mRecycled && (bitmap = response.getBitmap()) != null
                            && viewHolder.imageView != null) {
                        viewHolder.imageView.setImageBitmap(bitmap);
                        viewHolder.imageView.setColorFilter(NewsFeedUtils.getGrayFilterColor());
                        viewHolder.imageView.setTag(TintType.GRAYSCALE);
                    } else {
                        // TODO cache에 비트맵이 없는 경우. 기본적으로 없어야 할 상황.
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    showDummyImage(viewHolder);
                }
            });
        } else if (!mNews.isImageUrlChecked()) {
            // image url not yet fetched. show loading progress dialog
            viewHolder.progressBar.setVisibility(View.VISIBLE);

        } else {
            // failed to find image url
            showDummyImage(viewHolder);
        }
    }
    private void showDummyImage(ItemViewHolder viewHolder) {
        if (!mRecycled && viewHolder.imageView != null) {
            Bitmap dummyImage = NewsFeedUtils.getDummyNewsImage(getActivity().getApplicationContext());
            viewHolder.imageView.setImageBitmap(dummyImage);
            viewHolder.imageView.setColorFilter(NewsFeedUtils.getDummyImageFilterColor());
            viewHolder.imageView.setTag(TintType.DUMMY);

            viewHolder.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (!mNewsFeed.isValid() || mNews == null) {
            return;
        }
        ItemViewHolder viewHolder = (ItemViewHolder)view.getTag();

        ActivityOptions activityOptions =
                ActivityOptions.makeSceneTransitionAnimation(
                        getActivity(),
                        new Pair<View, String>(viewHolder.imageView,
                                viewHolder.imageView.getViewName()),
                        new Pair<View, String>(viewHolder.titleTextView,
                                viewHolder.titleTextView.getViewName())
                );

        Intent intent = new Intent(getActivity(),
                NewsFeedDetailActivity.class);
        intent.putExtra(NewsFeed.KEY_NEWS_FEED, mNewsFeed);
        intent.putExtra(News.KEY_NEWS, mPosition);
        intent.putExtra(MainActivity.INTENT_KEY_VIEW_NAME_IMAGE, viewHolder.imageView.getViewName());
        intent.putExtra(MainActivity.INTENT_KEY_VIEW_NAME_TITLE, viewHolder.titleTextView.getViewName());

        Object tintTag = viewHolder.imageView.getTag();
        TintType tintType = tintTag != null ? (TintType)tintTag : null;
        intent.putExtra(MainActivity.INTENT_KEY_TINT_TYPE, tintType);

//        Drawable drawable = viewHolder.imageView.getDrawable();
//        if (drawable != null) {
//            intent.putExtra("bitmap", ((BitmapDrawable) drawable).getBitmap());
//        }

        getActivity().startActivity(intent, activityOptions.toBundle());
    }
    public void setRecycled(boolean recycled) {
        mRecycled = recycled;
    }

    static class ItemViewHolder {
        @InjectView(R.id.main_top_feed_image_view)  ImageView imageView;
        @InjectView(R.id.main_top_feed_title)       TextView titleTextView;
        @InjectView(R.id.main_top_item_progress)    android.widget.ProgressBar progressBar;

        public ItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
