package com.yooiistudios.newskit.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.model.news.CustomRssHistoryUtils;

import java.util.ArrayList;

import static com.yooiistudios.newskit.ui.fragment.SettingFragment.SettingItem;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 3.
 *
 * CustomRssAdapter
 *  최근 입력한 RSS 최신 10개 를 로드
 */
public class CustomRssAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mRssHistoryList;

    public CustomRssAdapter(Context context) {
        mContext = context;
        mRssHistoryList = CustomRssHistoryUtils.getUrlHistory(mContext);
    }

    @Override
    public int getCount() {
        int count = CustomRssHistoryUtils.getUrlHistory(mContext).size();
        if (count == 0) {
            return 0;
        } else {
            mRssHistoryList = CustomRssHistoryUtils.getUrlHistory(mContext);
            return count + 1;
        }
    }

    @Override
    public Object getItem(int position) {
        return SettingItem.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;

        switch (position) {
            case 0: {
                view = LayoutInflater.from(mContext).inflate(R.layout.setting_item_sub_header, parent, false);
                TextView titleTextView = (TextView) view.findViewById(R.id.setting_item_title_textview);
                titleTextView.setText(R.string.custom_rss_recent_list);
//                titleTextView.setTextColor(mContext.getResources().getColor(R.color.app_color_accent));
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        mContext.getResources().getDimensionPixelSize(R.dimen.morningkit_ad_guide_text_size));
                break;
            }

            default: {
                view = LayoutInflater.from(mContext).inflate(R.layout.custom_rss_item, parent, false);
                TextView titleTextView = (TextView) view.findViewById(R.id.custom_rss_item_title_textview);
                titleTextView.setText(mRssHistoryList.get(position - 1));
                titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                ImageView removeImageView = (ImageView) view.findViewById(R.id.custom_rss_item_remove_imageview);
                removeImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomRssHistoryUtils.removeUrlAtIndex(mContext, position - 1);
                        notifyDataSetChanged();
                    }
                });
                break;
            }
        }
        return view;
    }
}
