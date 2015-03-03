package com.yooiistudios.newsflow.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.ui.activity.MoreInfoActivity;
import com.yooiistudios.newsflow.util.AppInfo;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;

/**
 * Created by StevenKim in Morning Kit from Yooii Studios Co., LTD. on 2014. 1. 7.
 *
 * MoreInfoListViewAdapter
 *  인포 프래그먼트의 리스트 어댑터
 */
public class MoreInfoListAdapter extends BaseAdapter {
    @SuppressWarnings("UnusedDeclaration")
    private MoreInfoListAdapter() {}
    public MoreInfoListAdapter(Context context) {
        this.context = context;
    }

    private Context context;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MoreInfoActivity.MoreInfoItem item = MoreInfoActivity.MoreInfoItem.values()[position];
        if (item.getTitleResId() != R.string.more_info_version) {
            convertView = LayoutInflater.from(context).inflate(R.layout.info_item, parent, false);
            if (convertView != null) {
                MNSettingInfoItemViewHolder viewHolder = new MNSettingInfoItemViewHolder(convertView);
                viewHolder.getTextView().setText(item.getTitleResId());
            }
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.more_info_version_item, parent, false);
            if (convertView != null) {
                MNSettingInfoVersionItemViewHolder viewHolder = new MNSettingInfoVersionItemViewHolder(convertView);
                viewHolder.getTitleTextView().setText(R.string.more_info_version);
                viewHolder.getDetailTextView().setText(AppInfo.getVersionName(context));
            }
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return MoreInfoActivity.MoreInfoItem.values().length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * ViewHolder
     */
    static class MNSettingInfoItemViewHolder {
        @Getter @InjectView(R.id.info_item_title) TextView textView;

        public MNSettingInfoItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    static class MNSettingInfoVersionItemViewHolder {
        @Getter @InjectView(R.id.more_info_version_title_textview) TextView titleTextView;
        @Getter @InjectView(R.id.more_info_version_detail_textview) TextView detailTextView;

        public MNSettingInfoVersionItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
