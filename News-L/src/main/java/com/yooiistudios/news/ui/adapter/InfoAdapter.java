package com.yooiistudios.news.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.activity.InfoActivity;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 3.
 *
 * InfoAdapter
 *  인포화면에 사용될 어뎁터
 */
public class InfoAdapter extends BaseAdapter {

    private Context mContext;

    public InfoAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return InfoActivity.InfoItem.values().length;
    }

    @Override
    public Object getItem(int position) {
        return InfoActivity.InfoItem.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.info_item, parent, false);
        }

        InfoActivity.InfoItem item = InfoActivity.InfoItem.values()[position];

        TextView titleTextView = (TextView)convertView.findViewById(R.id.info_item_title);
        titleTextView.setText(item.getTitleResId());
        return convertView;
    }
}
