package com.yooiistudios.news.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.fragment.SettingFragment;

import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX;
import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX_KEY;
import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX_SHARED_PREFERENCES;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 3.
 *
 * SettingAdapter
 *  세팅화면에 사용될 어뎁터
 */
public class SettingAdapter extends BaseAdapter {

    private Context mContext;

    public SettingAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return SettingFragment.SettingItem.values().length;
    }

    @Override
    public Object getItem(int position) {
        return SettingFragment.SettingItem.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.setting_item, parent, false);
        }

        SettingFragment.SettingItem item = SettingFragment.SettingItem.values()[position];

        TextView titleTextView = (TextView)convertView.findViewById(R.id.setting_row_title);
        titleTextView.setText(item.getTitleResId());

        TextView descriptionTextView =
                (TextView)convertView.findViewById(R.id.setting_row_description);
        Switch optionalSwitch = (Switch)convertView.findViewById(R.id.setting_switch);

        SharedPreferences preferences;
        switch(item) {
            case KEEP_SCREEN_ON:
                // 필요한 뷰 보여주기
                descriptionTextView.setVisibility(View.VISIBLE);
                optionalSwitch.setVisibility(View.VISIBLE);
                optionalSwitch.setFocusable(false);
                optionalSwitch.setFocusableInTouchMode(false);

                // 설명글 set
                descriptionTextView.setText(R.string.setting_keep_screen_on_description);

                // on/off 체크
                preferences = mContext.getSharedPreferences(
                        SettingFragment.KEEP_SCREEN_ON_SHARED_PREFERENCES, Context.MODE_PRIVATE);
                optionalSwitch.setChecked(preferences.getBoolean(SettingFragment.KEEP_SCREEN_ON_KEY, false));
                optionalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPreferences preferences = mContext.getSharedPreferences(
                                SettingFragment.KEEP_SCREEN_ON_SHARED_PREFERENCES, Context.MODE_PRIVATE);
                        preferences.edit().putBoolean(SettingFragment.KEEP_SCREEN_ON_KEY, isChecked).apply();
                    }
                });
                break;
            case PANEL_COUNT:
                descriptionTextView.setVisibility(View.VISIBLE);

                preferences = mContext.getSharedPreferences(
                        PANEL_MATRIX_SHARED_PREFERENCES, Context.MODE_PRIVATE);
                int currentPanelUniqueKey = preferences.getInt(PANEL_MATRIX_KEY,
                        PANEL_MATRIX.getDefault().uniqueKey);

                PANEL_MATRIX currentPanelMatrix = PANEL_MATRIX.getByUniqueKey(currentPanelUniqueKey);

                descriptionTextView.setText(
                        mContext.getString(R.string.setting_panel_count_description,
                                currentPanelMatrix.displayName));

                break;
            default:
                descriptionTextView.setVisibility(View.GONE);
                optionalSwitch.setVisibility(View.GONE);
                break;
        }

        return convertView;
    }
}
