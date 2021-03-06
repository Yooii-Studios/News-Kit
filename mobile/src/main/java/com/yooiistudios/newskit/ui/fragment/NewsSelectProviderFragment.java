package com.yooiistudios.newskit.ui.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsTopic;
import com.yooiistudios.newskit.core.news.curation.NewsProvider;
import com.yooiistudios.newskit.ui.activity.NewsSelectActivity;
import com.yooiistudios.newskit.ui.activity.NewsSelectDetailActivity;
import com.yooiistudios.newskit.ui.adapter.NewsSelectDetailAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 2015. 2. 24.
 *
 * NewsSelectDetailProviderFragment
 *  뉴스 선택화면 - 언론사의 토픽을 표시하는 프래그먼트
 */
public class NewsSelectProviderFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String ARG_NEWS_PROVIDER_JSON = "arg_json_data";

    @InjectView(R.id.news_select_detail_listview) ListView mListView;
    private NewsProvider mNewsProvider;
    private NewsFeed mCurrentNewsFeed;

    public static NewsSelectProviderFragment newInstance(String jsonString, NewsFeed newsFeed) {
        NewsSelectProviderFragment fragment = new NewsSelectProviderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NEWS_PROVIDER_JSON, jsonString);
        args.putParcelable(NewsFeed.KEY_NEWS_FEED, newsFeed);
        fragment.setArguments(args);
        return fragment;
    }

    public NewsSelectProviderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String jsonString = getArguments().getString(ARG_NEWS_PROVIDER_JSON);
            if (jsonString != null) {
                Gson gson = new Gson();
                mNewsProvider = gson.fromJson(jsonString, NewsProvider.class);
            }
            initNewsFeed();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_select_detail, container, false);
        if (rootView != null) {
            ButterKnife.inject(this, rootView);
            initListView();
            if (mNewsProvider != null) {
                ((NewsSelectDetailActivity) getActivity()).setToolbarTitle(mNewsProvider.name);
            }
        }
        return rootView;
    }

    private void initNewsFeed() {
        NewsFeed newsFeed = getArguments().getParcelable(NewsFeed.KEY_NEWS_FEED);
        if (newsFeed != null) {
            Parcel parcel = Parcel.obtain();
            newsFeed.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            mCurrentNewsFeed = NewsFeed.CREATOR.createFromParcel(parcel);
            parcel.recycle();
        }
    }

    private void initListView() {
        mListView.setAdapter(new NewsSelectDetailAdapter(getActivity(), mNewsProvider,
                mCurrentNewsFeed));
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NewsTopic selectedTopic = mNewsProvider.getNewsTopicList().get(position);

        getActivity().getIntent().putExtra(NewsSelectActivity.KEY_RSS_FETCHABLE, selectedTopic);
        getActivity().setResult(Activity.RESULT_OK, getActivity().getIntent());
        getActivity().finish();
    }
}
