package com.yooiistudios.news.ui.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.NewsProvider;
import com.yooiistudios.news.model.news.NewsTopic;
import com.yooiistudios.news.ui.adapter.NewsTopicSelectAdapter;
import com.yooiistudios.news.util.TypefaceUtils;

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
                .setTitle(newsProvider.getName()).create();

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
            newsTopicTitleList.add(newsTopic.getTitle());
        }
        CharSequence[] newsTopicTitles =
                newsTopicTitleList.toArray(new CharSequence[newsTopicTitleList.size()]);

        return new MaterialDialog.Builder(context)
                .title(R.string.store_topic_select_title)
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
                .positiveText(R.string.choose)
                .typeface(TypefaceUtils.getMediumTypeface(context), TypefaceUtils.getRegularTypeface(context))
                .build();
    }

    public interface OnItemClickListener {
        public void onSelectNewsTopic(Dialog dialog, NewsProvider newsProvider, int position);
    }
}
