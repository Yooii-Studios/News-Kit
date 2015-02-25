package com.yooiistudios.newsflow.ui.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.model.news.NewsProvider;
import com.yooiistudios.newsflow.model.news.NewsTopic;
import com.yooiistudios.newsflow.ui.activity.NewsSelectActivity;
import com.yooiistudios.newsflow.util.NLLog;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 2015. 2. 24.
 *
 * NewsSelectDetailProviderFragment
 *  뉴스 선택화면 - 언론사의 토픽을 표시하는 프래그먼트
 */
public class NewsSelectProviderFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String ARG_NEWS_PROVIDER_JSON = "arg_json_data";

    @InjectView(R.id.news_select_detail_listview) ListView mListView;
    private NewsProvider mNewsProvider;

    public static NewsSelectProviderFragment newInstance(String jsonString) {
        NewsSelectProviderFragment fragment = new NewsSelectProviderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NEWS_PROVIDER_JSON, jsonString);
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
        for (NewsTopic newsTopic : mNewsProvider.getNewsTopicList()) {
            adapter.add(newsTopic.title);
        }
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NLLog.now("position: " + position);

        NewsTopic selectedTopic = mNewsProvider.getNewsTopicList().get(position);

        getActivity().getIntent().putExtra(NewsSelectActivity.KEY_RSS_FETCHABLE, selectedTopic);
        getActivity().setResult(Activity.RESULT_OK, getActivity().getIntent());
        getActivity().finish();
    }
}
