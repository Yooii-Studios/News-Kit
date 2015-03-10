package com.yooiistudios.newsflow.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.util.TypefaceUtils;

import java.util.ArrayList;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 25.
 *
 * NewsSelectDetailAdapter
 *  뉴스 선택 디테일 프래그먼트에서 활용될 어댑터
 */
public class NewsSelectDetailAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mArrayList;

    public NewsSelectDetailAdapter(Context context, ArrayList<String> arrayList) {
        mContext = context;
        mArrayList = arrayList;
    }

    @Override
    public int getCount() {
        if (mArrayList != null) {
            return mArrayList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) LayoutInflater.from(mContext)
                .inflate(R.layout.news_select_detail_simple_item, parent, false);

        textView.setText(mArrayList.get(position));
        textView.setTypeface(TypefaceUtils.getRegularTypeface(mContext));
        return textView;
    }
}
