package com.yooiistudios.news.model.main;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.detail.NLDetailActivity;
import com.yooiistudios.news.main.NLMainActivity;
import com.yooiistudios.news.model.NLNews;
import com.yooiistudios.news.model.NLNewsFeed;
import com.yooiistudios.news.util.ImageMemoryCache;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 *
 * NLTopNewsFeedViewPagerItem
 *  메인화면 상단의 뷰페이저에 들어갈 아이템
 */
public class NLTopNewsFeedViewPagerItem extends Fragment
        implements View.OnClickListener {
    private static final String KEY_NEWS_FEED = "KEY_NEWS_FEED";
    private static final String KEY_NEWS = "KEY_NEWS";
    private static final String KEY_POSITION = "KEY_POSITION";

    private NLNewsFeed mNewsFeed;
    private NLNews mNews;
    private int mPosition;

    static NLTopNewsFeedViewPagerItem newInstance(NLNewsFeed newsFeed,
                                                  NLNews news, int position) {
        NLTopNewsFeedViewPagerItem f = new NLTopNewsFeedViewPagerItem();

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout
                        .main_top_viewpager_item, container, false);

        ItemViewHolder holder = new ItemViewHolder(root);

        holder.imageView.setViewName(NLMainActivity.VIEW_NAME_IMAGE_PREFIX +
                mPosition);
        applyImage(getActivity().getApplicationContext(), holder);

        holder.titleTextView.setViewName(NLMainActivity.VIEW_NAME_TITLE_PREFIX +
                mPosition);
        holder.titleTextView.setText(mNews.getTitle());

        root.setOnClickListener(this);
        root.setTag(holder);

        return root;
    }

    public void applyImage(Context context) {
        View root;
        ItemViewHolder viewHolder;
        if ((root = getView()) != null &&
                (viewHolder = (ItemViewHolder)root.getTag()) != null) {
            applyImage(context, viewHolder);
        }

    }

    private void applyImage(Context context, ItemViewHolder viewHolder) {
        final ImageMemoryCache cache = ImageMemoryCache.getInstance(context);

        // set image
        String imgUrl = mNews.getImageUrl();
        if (imgUrl != null) {
            Bitmap bitmap = cache.getBitmapFromUrl(imgUrl);

            if (bitmap != null) {
                viewHolder.imageView.setImageBitmap(bitmap);
            } else {
                // TODO cache에 비트맵이 없는 경우. 기본적으로 없어야 할 상황.
            }
        } else {
            // TODO image url 없을 경우
        }
    }

    @Override
    public void onClick(View view) {
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
                NLDetailActivity.class);
        intent.putExtra(NLNewsFeed.KEY_NEWS_FEED, mNewsFeed);
        intent.putExtra(NLNews.KEY_NEWS, mNews);
        intent.putExtra(NLMainActivity.INTENT_KEY_VIEW_NAME_IMAGE,
                viewHolder.imageView.getViewName());
        intent.putExtra(NLMainActivity.INTENT_KEY_VIEW_NAME_TITLE,
                viewHolder.titleTextView.getViewName());

        Drawable drawable = viewHolder.imageView.getDrawable();
        if (drawable != null) {
            intent.putExtra("bitmap", ((BitmapDrawable) drawable).getBitmap());
        }

        getActivity().startActivity(intent, activityOptions.toBundle());
    }

    private static class ItemViewHolder {
        ImageView imageView;
        TextView titleTextView;

        public ItemViewHolder(View view) {
            imageView = (ImageView)view.findViewById(R.id.topFeedImage);
            titleTextView = (TextView)view.findViewById(R.id.topFeedTitle);
        }
    }
}
