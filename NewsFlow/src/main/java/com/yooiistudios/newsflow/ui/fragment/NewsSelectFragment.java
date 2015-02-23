package com.yooiistudios.newsflow.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.Toast;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.iab.IabProducts;
import com.yooiistudios.newsflow.model.news.NewsContentProvider;
import com.yooiistudios.newsflow.model.news.NewsProvider;
import com.yooiistudios.newsflow.model.news.NewsProviderCountry;
import com.yooiistudios.newsflow.model.news.NewsTopic;
import com.yooiistudios.newsflow.ui.adapter.NewsSelectRecyclerAdapter;
import com.yooiistudios.newsflow.ui.widget.NewsTopicSelectDialogFactory;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * NewsSelectFragment
 *  뉴스 선택화면의 한 페이지의 컨텐츠
 */
public class NewsSelectFragment extends Fragment
        implements NewsSelectRecyclerAdapter.OnSelectionListener,
        NewsTopicSelectDialogFactory.OnItemClickListener {
    public static final String KEY_TAB_INDEX = "key_tab_index";
    public static final String KEY_SELECTED_RSS_FETCHABLE = "key_selected_rss_fetchable";

    private ArrayList<NewsProviderCountry> mNewsProviderCountries;

    public static NewsSelectFragment newInstance(int pageNum) {
        NewsSelectFragment fragment = new NewsSelectFragment();

        Bundle args = new Bundle();
        args.putInt(KEY_TAB_INDEX, pageNum);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int index;
        if (getArguments() != null) {
            index = getArguments().getInt(KEY_TAB_INDEX);
        } else {
            index = 0;
        }
        NewsContentProvider newsContentProvider = NewsContentProvider.getInstance(getActivity());
        mNewsProviderCountries = newsContentProvider.getNewsLanguage(index).newsProviderCountries;
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

        NewsSelectRecyclerAdapter adapter = new NewsSelectRecyclerAdapter(mNewsProviderCountries);
        adapter.setOnNewsProviderClickListener(this);
        mViewHolder.mRecyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onSelectNewsProvider(NewsProvider newsProvider) {
        NewsTopicSelectDialogFactory.makeDialog(getActivity(), newsProvider, this).show();
    }

    @Override
    public void onSelectNewsProviderCountry(NewsProviderCountry newsProviderCountry) {

    }

    @Override
    public void onSelectNewsTopic(Dialog dialog, NewsProvider newsProvider, int position) {
        final ArrayList<NewsTopic> newsTopicList = newsProvider.getNewsTopicList();
        if (!newsTopicList.get(position).isDefault() &&
                !IabProducts.containsSku(getActivity(), IabProducts.SKU_TOPIC_SELECT)) {
            Toast.makeText(getActivity(), R.string.iab_item_unavailable, Toast.LENGTH_LONG).show();
            return;
        }
        dialog.dismiss();

        NewsTopic selectedTopic = newsProvider.getNewsTopicList().get(position);

        getActivity().getIntent().putExtra(KEY_SELECTED_RSS_FETCHABLE, selectedTopic);
        getActivity().setResult(Activity.RESULT_OK, getActivity().getIntent());
        getActivity().finish();
    }

    static class ViewHolder {
        @InjectView(R.id.news_select_viewpager_recycler_view) RecyclerView mRecyclerView;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
