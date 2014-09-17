package com.yooiistudios.news.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
import com.yooiistudios.news.model.news.NewsPublisher;
import com.yooiistudios.news.model.news.NewsSelectPageContentProvider;
import com.yooiistudios.news.ui.adapter.NewsSelectRecyclerViewAdapter;
import com.yooiistudios.news.ui.widget.recyclerview.DividerItemDecoration;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * NewsSelectFragment
 *  뉴스 선택화면의 한 페이지의 컨텐츠.
 */
public class NewsSelectFragment extends Fragment implements NewsSelectRecyclerViewAdapter.OnNewsPublisherClickListener {
    public static final String KEY_TAB_POSITION = "KEY_TAB_POSITION";

    public static final String KEY_SELECTED_NEWS_FEED = "KEY_SELECTED_NEWS_FEED";

    private ViewHolder mViewHolder;
    private ArrayList<NewsPublisher> mNewsProviderList;
    private int mPosition;

    public static NewsSelectFragment newInstance(int pageNum) {
        NewsSelectFragment fragment = new NewsSelectFragment();

        Bundle args = new Bundle();
        args.putInt(KEY_TAB_POSITION, pageNum);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPosition = getArguments().getInt(KEY_TAB_POSITION);
        } else {
            mPosition = 0;
        }

        Context context = getActivity().getApplicationContext();

        mNewsProviderList = NewsSelectPageContentProvider.getInstance().getNewsFeeds(context,
                NewsSelectPageContentProvider.getInstance().getLanguageAt(context, mPosition));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout
                .fragment_news_select_viewpager_item, container, false);

        mViewHolder = new ViewHolder(root);

        Context context = getActivity().getApplicationContext();
        // init recycler view
        mViewHolder.mRecyclerView.setHasFixedSize(true);
        mViewHolder.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mViewHolder.mRecyclerView.setLayoutManager(
                new LinearLayoutManager(context));

        NewsSelectRecyclerViewAdapter adapter = new NewsSelectRecyclerViewAdapter(mNewsProviderList);
        adapter.setOnNewsPublisherClickListener(this);
        mViewHolder.mRecyclerView.setAdapter(adapter);
        mViewHolder.mRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL_LIST));

        return root;
    }

    @Override
    public void onNewsPublisherClick(final NewsPublisher newsPublisher) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // 뉴스피드들의 타이틀을 CharSequence 로 변경
        ArrayList<String> newsFeedTitleList = new ArrayList<String>();
        for (NewsFeed newsFeed : newsPublisher.getNewsFeedList()) {
            newsFeedTitleList.add(newsFeed.getTitle());
        }

        String[] titles = newsFeedTitleList.toArray(new String[newsFeedTitleList.size()]);
        AlertDialog alertDialog = builder.setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                getActivity().getIntent().putExtra(KEY_SELECTED_NEWS_FEED, newsPublisher.getNewsFeedList().get(i));
                getActivity().setResult(Activity.RESULT_OK, getActivity().getIntent());
                getActivity().finish();
            }
        }).setTitle(R.string.select_news_select_newsfeed_dialog_title).create();
        alertDialog.show();
    }

    static class ViewHolder {
        @InjectView(R.id.news_select_viewpager_recycler_view) RecyclerView mRecyclerView;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
