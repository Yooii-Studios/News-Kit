package com.yooiistudios.news.ui.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yooiistudios.news.model.news.NewsProvider;
import com.yooiistudios.news.model.news.NewsTopic;
import com.yooiistudios.news.ui.adapter.NewsTopicSelectAdapter;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 12. 23.
 *
 * NewsTopicSelectDialogFactory
 *  뉴스 토픽 선택 다이얼로그 만드는 팩토리
 */
public class NewsTopicSelectDialogFactory {

    private NewsTopicSelectDialogFactory() {
        throw new AssertionError("You can't create this class!");
    }

    public static AlertDialog makeDialog(Activity activity, final NewsProvider newsProvider,
                                   final OnItemClickListener listener) {
        final ArrayList<NewsTopic> newsTopicList = newsProvider.getNewsTopicList();

        NewsTopicSelectAdapter adapter = new NewsTopicSelectAdapter(activity, newsTopicList);

        ListView newsTopicListView = new ListView(activity);
        newsTopicListView.setAdapter(adapter);

        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setView(newsTopicListView)
                .setTitle(newsProvider.getName()).create();

        newsTopicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onSelectNewsTopic(alertDialog, newsProvider, position);
            }
        });

        return alertDialog;
    }

    public interface OnItemClickListener {
        public void onSelectNewsTopic(AlertDialog dialog, NewsProvider newsProvider, int position);
    }
}
