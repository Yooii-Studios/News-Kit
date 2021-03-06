package com.yooiistudios.newskit.ui.fragment;


import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.curation.NewsProviderCountry;
import com.yooiistudios.newskit.ui.activity.NewsSelectDetailActivity;
import com.yooiistudios.newskit.ui.adapter.NewsSelectDetailAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 2015. 2. 24.
 *
 * NewsSelectDetailCountryFragment
 *  뉴스 선택화면 - 선택 국가의 언론사를 표시하는 프래그먼트
 */
public class NewsSelectCountryFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String ARG_NEWS_PROVIDER_COUNTRY_JSON = "arg_json_data";

    @InjectView(R.id.news_select_detail_listview) ListView mListView;
    private NewsProviderCountry mNewsProviderCountry;
    private NewsFeed mCurrentNewsFeed;

    public static NewsSelectCountryFragment newInstance(String jsonString, NewsFeed newsFeed) {
        NewsSelectCountryFragment fragment = new NewsSelectCountryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NEWS_PROVIDER_COUNTRY_JSON, jsonString);
        args.putParcelable(NewsFeed.KEY_NEWS_FEED, newsFeed);
        fragment.setArguments(args);
        return fragment;
    }

    public NewsSelectCountryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String jsonString = getArguments().getString(ARG_NEWS_PROVIDER_COUNTRY_JSON);
            if (jsonString != null) {
                Gson gson = new Gson();
                mNewsProviderCountry = gson.fromJson(jsonString, NewsProviderCountry.class);
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
            if (mNewsProviderCountry != null) {
                ((NewsSelectDetailActivity)getActivity()).setToolbarTitle(
                        mNewsProviderCountry.countryLocalName);
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
        mListView.setAdapter(new NewsSelectDetailAdapter(getActivity(), mNewsProviderCountry,
                mCurrentNewsFeed));
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < mNewsProviderCountry.newsProviders.size()) {
            Gson gson = new Gson();
            String jsonString = gson.toJson(mNewsProviderCountry.newsProviders.get(position));
            Fragment newsProviderFragment = NewsSelectProviderFragment.newInstance(jsonString, mCurrentNewsFeed);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.news_select_detail_container, newsProviderFragment)
                    .addToBackStack(null).commit();
        }
    }
}
