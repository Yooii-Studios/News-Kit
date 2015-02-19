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
import com.yooiistudios.newsflow.model.news.NewsProviderLanguage;
import com.yooiistudios.newsflow.model.news.NewsTopic;
import com.yooiistudios.newsflow.ui.adapter.NewsSelectRecyclerViewAdapter;
import com.yooiistudios.newsflow.ui.widget.NewsTopicSelectDialogFactory;
import com.yooiistudios.newsflow.ui.widget.recyclerview.DividerItemDecoration;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * NewsSelectFragment
 *  뉴스 선택화면의 한 페이지의 컨텐츠.
 */
public class NewsSelectFragment extends Fragment
        implements NewsSelectRecyclerViewAdapter.OnSelectionListener,
        NewsTopicSelectDialogFactory.OnItemClickListener {
    public static final String KEY_TAB_POSITION = "KEY_TAB_POSITION";

    public static final String KEY_SELECTED_NEWS_TOPIC = "KEY_SELECTED_NEWS_TOPIC";
    public static final String KEY_CUSTOM_RSS_URL = "KEY_CUSTOM_RSS_URL";

    private ViewHolder mViewHolder;
    private ArrayList<NewsProvider> mNewsProviderList;
    private NewsProviderLanguage mNewsProviderLanguage;
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

        // 기존 메서드
//        mNewsProviderList = NewsSelectPageContentProvider.getInstance().getNewsFeeds(context,
//                NewsSelectPageContentProvider.getInstance().getLanguageAt(context, mPosition));

        // 새 메서드
        // 추후 mNewsProviderList는 삭제하고 providerList에서만 사용하게 리팩토링이 필요
        mNewsProviderLanguage = NewsContentProvider.getInstance(context).getNewsRegion(mPosition);
        mNewsProviderList = mNewsProviderLanguage.newsProviders;
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
        adapter.setOnNewsProviderClickListener(this);
        mViewHolder.mRecyclerView.setAdapter(adapter);
        mViewHolder.mRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL_LIST));

        return root;
    }

    @Override
    public void onSelectNewsProvider(NewsProvider newsProvider) {
//        final AlertDialog alertDialog = NewsTopicSelectDialogFactory.makeAlertDialog(
//                getActivity(), newsProvider, this);
//
//        alertDialog.show();

        NewsTopicSelectDialogFactory.makeDialog(getActivity(), newsProvider, this).show();
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

        getActivity().getIntent().putExtra(KEY_SELECTED_NEWS_TOPIC, selectedTopic);
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
