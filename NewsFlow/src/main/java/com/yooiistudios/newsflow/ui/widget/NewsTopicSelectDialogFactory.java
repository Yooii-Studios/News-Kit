package com.yooiistudios.newsflow.ui.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.newsflow.model.news.NewsProvider;
import com.yooiistudios.newsflow.model.news.NewsTopic;
import com.yooiistudios.newsflow.ui.adapter.NewsTopicSelectAdapter;

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

    public static AlertDialog makeAlertDialog(Activity activity, final NewsProvider newsProvider,
                                              final OnItemClickListener listener) {
        final ArrayList<NewsTopic> newsTopicList = newsProvider.getNewsTopicList();

        NewsTopicSelectAdapter adapter = new NewsTopicSelectAdapter(activity, newsTopicList);

        ListView newsTopicListView = new ListView(activity);
        newsTopicListView.setAdapter(adapter);

        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setView(newsTopicListView)
                .setTitle(newsProvider.name).create();

        newsTopicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onSelectNewsTopic(alertDialog, newsProvider, position);
            }
        });

        return alertDialog;
    }

    public static Dialog makeDialog(Context context, final NewsProvider newsProvider,
                                    final OnItemClickListener listener) {
        // convert ArrayList to CharSequence[]
        ArrayList<String> newsTopicTitleList = new ArrayList<>();
        for (NewsTopic newsTopic : newsProvider.getNewsTopicList()) {
            newsTopicTitleList.add(newsTopic.title);
        }
        CharSequence[] newsTopicTitles =
                newsTopicTitleList.toArray(new CharSequence[newsTopicTitleList.size()]);

        // 추후 유료관련 토픽을 막을 경우를 생각해서 SingleChoice 를 취소하고 List 방식으로 변경
        return new MaterialDialog.Builder(context)
                .title(newsProvider.name)
                .items(newsTopicTitles)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        listener.onSelectNewsTopic(dialog, newsProvider, which);
                    }
                })
//                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallback() {
//                    @Override
//                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                        listener.onSelectNewsTopic(dialog, newsProvider, which);
//                    }
//                })
                .build();
    }

    public interface OnItemClickListener {
        public void onSelectNewsTopic(Dialog dialog, NewsProvider newsProvider, int position);
    }
}
