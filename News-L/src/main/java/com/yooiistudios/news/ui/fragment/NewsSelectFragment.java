package com.yooiistudios.news.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.NewsSelectPageContentProvider;
import com.yooiistudios.news.ui.adapter.NewsSelectRecyclerViewAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * NewsSelectFragment
 *  뉴스 선택화면의 한 페이지의 컨텐츠.
 */
public class NewsSelectFragment extends Fragment {
    public static final String KEY_POSITION = "KEY_POSITION";

    private ViewHolder mViewHolder;
    private ArrayList<NewsFeed> mNewsFeedList;
    private int mPosition;

    public static NewsSelectFragment newInstance(int pageNum) {
        NewsSelectFragment fragment = new NewsSelectFragment();

        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, pageNum);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPosition = getArguments().getInt(KEY_POSITION);
        } else {
            mPosition = 0;
        }

        Context context = getActivity().getApplicationContext();

        mNewsFeedList = NewsSelectPageContentProvider.getInstance().getNewsFeeds(context,
                NewsSelectPageContentProvider.getInstance().getLanguageAt(context, mPosition));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout
                .fragment_news_select_viewpager_item, container, false);

        mViewHolder = new ViewHolder(root);

        // init recycler view
        mViewHolder.mRecyclerView.setHasFixedSize(true);
        mViewHolder.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mViewHolder.mRecyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity().getApplicationContext()));

        mViewHolder.mRecyclerView.setAdapter(new NewsSelectRecyclerViewAdapter(mNewsFeedList));

        return root;
    }

    static class ViewHolder {
        @InjectView(R.id.news_select_viewpager_recycler_view) RecyclerView mRecyclerView;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
