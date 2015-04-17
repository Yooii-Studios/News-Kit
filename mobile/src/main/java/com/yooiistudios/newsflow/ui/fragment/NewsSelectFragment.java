package com.yooiistudios.newsflow.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.curation.NewsContentProvider;
import com.yooiistudios.newsflow.core.news.curation.NewsProviderCountry;
import com.yooiistudios.newsflow.ui.adapter.NewsSelectRecyclerAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * NewsSelectFragment
 *  뉴스 선택화면의 한 페이지의 컨텐츠
 */
public class NewsSelectFragment extends Fragment {
    public static final String KEY_TAB_INDEX = "key_tab_index";

    private ArrayList<NewsProviderCountry> mNewsProviderCountries;
    private NewsFeed mNewsFeed;

    public static NewsSelectFragment newInstance(int pageNum, NewsFeed newsFeed) {
        NewsSelectFragment fragment = new NewsSelectFragment();

        Bundle args = new Bundle();
        args.putInt(KEY_TAB_INDEX, pageNum);
        args.putParcelable(NewsFeed.KEY_NEWS_FEED, newsFeed);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int index = 0;
        if (getArguments() != null) {
            index = getArguments().getInt(KEY_TAB_INDEX);
            initNewsFeed();
        }
        NewsContentProvider newsContentProvider = NewsContentProvider.getInstance(getActivity());
        mNewsProviderCountries = newsContentProvider.getNewsLanguage(index).newsProviderCountries;
    }

    private void initNewsFeed() {
        NewsFeed newsFeed = getArguments().getParcelable(NewsFeed.KEY_NEWS_FEED);
        if (newsFeed != null) {
            Parcel parcel = Parcel.obtain();
            newsFeed.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            mNewsFeed = NewsFeed.CREATOR.createFromParcel(parcel);
            parcel.recycle();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout
                .fragment_news_select_viewpager_item, container, false);

        ViewHolder mViewHolder = new ViewHolder(root);

        Context context = getActivity();

        // init recycler view
        mViewHolder.mRecyclerView.setHasFixedSize(true);
        mViewHolder.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mViewHolder.mRecyclerView.setLayoutManager(
                new LinearLayoutManager(context));

        NewsSelectRecyclerAdapter adapter = new NewsSelectRecyclerAdapter(mNewsProviderCountries,
                mNewsFeed);
        adapter.setOnNewsProviderClickListener(
                (NewsSelectRecyclerAdapter.OnSelectionListener)getActivity());
        mViewHolder.mRecyclerView.setAdapter(adapter);

        return root;
    }

    static class ViewHolder {
        @InjectView(R.id.news_select_viewpager_recycler_view) RecyclerView mRecyclerView;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
