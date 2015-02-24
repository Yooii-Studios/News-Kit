package com.yooiistudios.newsflow.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.model.news.NewsProvider;
import com.yooiistudios.newsflow.model.news.NewsProviderCountry;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 2015. 2. 24.
 *
 * NewsSelectDetailCountryFragment
 *  뉴스 선택화면 - 선택 국가의 언론사를 표시하는 프래그먼트
 */
public class NewsSelectCountryFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String ARG_NEWS_PROVIDER_COUNTRY_JSON = "arg_json_data";

    @InjectView(R.id.news_select_detail_listview) ListView mListView;
    private NewsProviderCountry mNewsProviderCountry;

    public static NewsSelectCountryFragment newInstance(String jsonString) {
        NewsSelectCountryFragment fragment = new NewsSelectCountryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NEWS_PROVIDER_COUNTRY_JSON, jsonString);
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
//                initToolbarTitle(newsProviderCountry.countryLocalName);
            }
        }
    }

//    private void initToolbarTitle(String title) {
//        if (getActivity().getActionBar() != null) {
//            getActivity().getActionBar().setTitle(title);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_select_detail, container, false);
        if (rootView != null) {
            ButterKnife.inject(this, rootView);
            initListView();
        }
        return rootView;
    }

    private void initListView() {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getActivity(), R.layout.news_select_detail_simple_item);
        for (NewsProvider newsProvider : mNewsProviderCountry.newsProviders) {
            adapter.add(newsProvider.name);
        }
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < mNewsProviderCountry.newsProviders.size()) {
            Gson gson = new Gson();
            String jsonString = gson.toJson(mNewsProviderCountry.newsProviders.get(position));
            Fragment newsProviderFragment = NewsSelectProviderFragment.newInstance(jsonString);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.news_select_detail_container, newsProviderFragment)
                    .addToBackStack(null).commit();;
        }
    }
}
