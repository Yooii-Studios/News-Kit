package com.yooiistudios.newsflow.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.iab.IabProducts;
import com.yooiistudios.newsflow.model.news.NewsTopic;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 12. 5.
 *
 * NewsTopicSelectAdapter
 *  뉴스사의 토픽 선택 다이얼로그에 쓰이는 어댑터
 */
public class NewsTopicSelectAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<NewsTopic> mNewsTopicArr;

    public NewsTopicSelectAdapter(Context context, ArrayList<NewsTopic> newsTopicArr) {
        mContext = context;
        mNewsTopicArr = newsTopicArr;
    }

    @Override
    public int getCount() {
        return mNewsTopicArr.size();
    }

    @Override
    public Object getItem(int position) {
        return mNewsTopicArr.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.news_topic_select_dialog_list_item, null);
        }
        NewsTopic newsTopic = mNewsTopicArr.get(position);

        TextView topicNameTextView = (TextView)convertView.findViewById(R.id.news_topic_name);
        topicNameTextView.setText(newsTopic.getTitle());

        // background
        if (newsTopic.isDefault()) {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            if (IabProducts.containsSku(mContext, IabProducts.SKU_TOPIC_SELECT)) {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                convertView.setBackgroundColor(
                        mContext.getResources().getColor(R.color.setting_locked_item_background)
                );
            }
        }

        return convertView;
    }
}
