package com.yooiistudios.newskit.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newskit.core.news.News;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.model.Settings;
import com.yooiistudios.newskit.model.cache.NewsImageLoader;
import com.yooiistudios.newskit.model.cache.NewsUrlSupplier;
import com.yooiistudios.newskit.ui.PanelDecoration;
import com.yooiistudios.newskit.ui.activity.MainActivity;
import com.yooiistudios.newskit.ui.adapter.MainTopPagerAdapter.OnItemClickListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 *
 * MainNewsFeedFragment
 *  메인화면 상단의 뷰페이저에 들어갈 프레그먼트
 */
public class MainTopFragment extends Fragment {
    private static final String KEY_NEWS_FEED = "KEY_NEWS_FEED";
    private static final String KEY_NEWS = "KEY_CURRENT_NEWS_INDEX";
    private static final String KEY_POSITION = "KEY_TAB_INDEX";

    private MainActivity mActivity;
    private NewsImageLoader mImageLoader;
    private NewsFeed mNewsFeed;
    private News mNews;
    private int mPosition;
    private boolean mRecycled;
    private float mOriginalTitleTextSize = -1;

    public static MainTopFragment newInstance(NewsFeed newsFeed, News news, int position) {
        MainTopFragment f = new MainTopFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(KEY_NEWS_FEED, newsFeed);
        args.putParcelable(KEY_NEWS, news);
        args.putInt(KEY_POSITION, position);
        f.setArguments(args);

        return f;
    }

    @SuppressWarnings("UnusedDeclaration")
    public MainTopFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNews = getArguments().getParcelable(KEY_NEWS);
            mNewsFeed = getArguments().getParcelable(KEY_NEWS_FEED);
            mPosition = getArguments().getInt(KEY_POSITION);
        } else {
            mNews = null;
            mPosition = -1;
        }
        mRecycled = false;
    }

    private void initImageLoader() {
        mImageLoader = mActivity.getImageLoader();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final ViewGroup root = (ViewGroup)inflater.inflate(R.layout.fragment_main_top_viewpager_item,
                container, false);

        ItemViewHolder holder = new ItemViewHolder(root);

        if (mNews == null) {
            holder.imageView.setImageDrawable(null);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.titleTextView.setText("");

            root.setOnClickListener(null);

            return root;
        }

        String newsName = mNews.getTitle();
        if (newsName != null) {
            holder.titleTextView.setText(newsName);

            if (mOriginalTitleTextSize == -1) {
                mOriginalTitleTextSize = holder.titleTextView.getTextSize();
            }
            holder.titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    mOriginalTitleTextSize * Settings.getHeadlineFontSize(getActivity()));
        }

        holder.rippleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemViewHolder viewHolder = (ItemViewHolder) root.getTag();
                getOnItemClickListener().onTopItemClick(viewHolder, mNewsFeed, mPosition);
            }
        });
        holder.rippleView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getOnLongClickListener().onLongClick(root);
                return true;
            }
        });
        root.setTag(holder);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        configOnActivityCreated(getActivity());

        applyImage();
    }

    private void configOnActivityCreated(Activity activity) {
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mActivity = (MainActivity)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActionListener");
        }

        initImageLoader();
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
        // TODO: 액티비티 유지 안함 옵션을 켠 경우 mActivity.getImageLoader() == null 인 경우가 있음.
        // 차후 단순한 null 체크가 아닌 더 나은 방법으로 수정 가능한지 확인 필요
        if (mImageLoader != null && mNews.hasImageUrl()) {
            mImageLoader.get(new NewsUrlSupplier(mNews, NewsFeed.INDEX_TOP),
                    new CacheImageLoader.ImageListener() {
                        @Override
                        public void onSuccess(CacheImageLoader.ImageResponse response) {
                            if (mRecycled) {
                                return;
                            }

                            viewHolder.progressBar.setVisibility(View.GONE);

                            if (response.bitmap != null && viewHolder.imageView != null) {
                                viewHolder.imageView.setImageBitmap(response.bitmap);
                                viewHolder.imageView.setColorFilter(
                                        PanelDecoration.getDefaultTopPaletteColor(),
                                        PorterDuff.Mode.SRC_OVER);
//                        viewHolder.imageView.setTag(TintType.GRAY_SCALE_TOP);
                            }
                        }

                        @Override
                        public void onFail(VolleyError error) {
                            showDummyImage(viewHolder);
                        }
                    });
        } else if (!mNews.isImageUrlChecked()) {
            // image url not yet fetched. show loading progress dialog
            viewHolder.imageView.setImageDrawable(null);
            viewHolder.progressBar.setVisibility(View.VISIBLE);

        } else {
            // failed to find image url
            showDummyImage(viewHolder);
        }
    }

    private void showDummyImage(final ItemViewHolder viewHolder) {
        // TODO: 액티비티 유지 안함 옵션을 켠 경우 mActivity.getImageLoader() == null 인 경우가 있음.
        // 차후 단순한 null 체크가 아닌 더 나은 방법으로 수정 가능한지 확인 필요
        if (!mRecycled && mImageLoader != null && viewHolder.imageView != null &&
                getActivity() != null) {
            PanelDecoration.getDummyNewsImageAsync(getActivity().getApplicationContext(),
                    mImageLoader, new PanelDecoration.OnLoadBitmapListener() {
                        @Override
                        public void onLoad(Bitmap bitmap) {
                            if (!mRecycled) {
                                viewHolder.imageView.setImageBitmap(bitmap);
                            }
                        }
                    });
            viewHolder.imageView.setColorFilter(PanelDecoration.getTopDummyImageFilterColor(),
                    PorterDuff.Mode.SRC_OVER);
//            viewHolder.imageView.setTag(TintType.DUMMY_TOP);

            viewHolder.progressBar.setVisibility(View.GONE);
        }
    }

    private OnItemClickListener getOnItemClickListener() {
        ViewGroup parentView = getMainTopContainerLayout();
        if (parentView instanceof OnItemClickListener) {
            return (OnItemClickListener)parentView;
        } else {
            throw new IllegalStateException("Containing layout should implement "
                    + OnItemClickListener.class.getSimpleName() + ".");
        }
    }

    private View.OnLongClickListener getOnLongClickListener() {
        ViewGroup parentView = getMainTopContainerLayout();
        if (parentView instanceof View.OnLongClickListener) {
            return (View.OnLongClickListener)parentView;
        } else {
            throw new IllegalStateException("Containing layout should implement "
                    + View.OnLongClickListener.class.getSimpleName() + ".");
        }
    }

    private ViewGroup getMainTopContainerLayout() {
        return ((MainActivity)getActivity()).getMainTopContainerLayout();
    }

    public void setRecycled(boolean recycled) {
        mRecycled = recycled;
    }

    public static class ItemViewHolder {
        public @InjectView(R.id.main_top_item_image_view)       ImageView imageView;
        public @InjectView(R.id.main_top_item_title_textview)   TextView titleTextView;
        public @InjectView(R.id.main_top_item_progress)         ProgressBar progressBar;
        public @InjectView(R.id.main_top_item_ripple_view)      View rippleView;

        public ItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
