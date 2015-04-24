package com.yooiistudios.newsflow.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.language.LanguageUtils;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.newsflow.model.Settings;

import static com.yooiistudios.newsflow.ui.fragment.SettingFragment.SettingItem;

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
        return SettingItem.values().length;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        return SettingItemFactory.inflate(mContext, parent, position);
    }

    private static class SettingItemFactory {
        private static View inflate(final Context context, ViewGroup parent, int position) {
            View view = null;

            SettingItem item = SettingItem.values()[position];
            switch (item) {
                case MAIN_SUB_HEADER:
                case GENERAL_SUB_HEADER:
                    view = LayoutInflater.from(context).inflate(R.layout.setting_item_sub_header, parent, false);
                    break;

                case LANGUAGE:
                case MAIN_PANEL_MATRIX:
                case PAIR_TV:
                case MAIN_AUTO_REFRESH_INTERVAL:
                    view = LayoutInflater.from(context).inflate(R.layout.setting_item_base, parent, false);
                    initBaseItem(context, item, view);
                    break;

                case KEEP_SCREEN_ON:
                case NOTIFICATION:
                    view = LayoutInflater.from(context).inflate(R.layout.setting_item_switch, parent, false);
                    initSwitchItem(context, item, view);
                    break;

                case MAIN_AUTO_REFRESH_SPEED:
                    view = LayoutInflater.from(context).inflate(R.layout.setting_item_seekbar, parent, false);
                    initSeekBarItem(context, item, view);
                    break;
            }

            if (view != null) {
                TextView  titleTextView = (TextView) view.findViewById(R.id.setting_item_title_textview);
                titleTextView.setText(item.getTitleResId());
            }

            // TODO 나중에 폰트의 영어 높이가 너무 높은 부분에 대해서 고민하기. 마이너스 마진을 통해서 해결해야 하지 않을까 생각
//            if (item == SettingItem.MAIN_SUB_HEADER || item == SettingItem.NEWS_FEED_SUB_HEADER) {
//                titleTextView.setTypeface(TypefaceUtils.getMediumTypeface(context));
//                titleTextView.setTypeface(TypefaceUtils.getEngRegularTypeface(context));
//            } else {
//                titleTextView.setTypeface(TypefaceUtils.getRegularTypeface(context));
//                titleTextView.setTypeface(TypefaceUtils.getEngRegularTypeface(context));
//            }

//            TextView descriptionTextView =
//                    (TextView) view.findViewById(R.id.setting_item_description_textview);
//            if (descriptionTextView != null) {
//                descriptionTextView.setTypeface(TypefaceUtils.getEngRegularTypeface(context));
//            }
            return view;
        }
    }

    private static void initBaseItem(Context context, SettingItem item, View view) {
        TextView descriptionTextView =
                (TextView) view.findViewById(R.id.setting_item_description_textview);

        if (item == SettingItem.LANGUAGE) {
            descriptionTextView.setText(
                    LanguageUtils.getCurrentLanguage(context).getLocalNotationStringId());
        } else if (item == SettingItem.MAIN_AUTO_REFRESH_INTERVAL) {
//            int autoRefreshInterval = Settings.getAutoRefreshInterval(context);
            int intervalMinute = Settings.getAutoRefreshIntervalMinute(context);
            int intervalSecond = Settings.getAutoRefreshIntervalSecond(context);

            String message = intervalMinute + context.getString(R.string.minute)
                    + " " + intervalSecond + context.getString(R.string.second);

            descriptionTextView.setText(message);
        } else if (item == SettingItem.MAIN_PANEL_MATRIX) {
            PanelMatrix currentPanelMatrix = PanelMatrixUtils.getCurrentPanelMatrix(context);
            descriptionTextView.setText(context.getString(
                    R.string.setting_main_panel_matrix_description, currentPanelMatrix.getDisplayName()));
        } else if (item == SettingItem.PAIR_TV) {
            descriptionTextView.setText(context.getString(
                    R.string.setting_pair_tv_description));
        }
    }

    private static void initSwitchItem(final Context context, SettingItem item, View view) {
        SwitchCompat switchCompat = (SwitchCompat) view.findViewById(R.id.setting_item_switch);
        if (item == SettingItem.KEEP_SCREEN_ON) {
            switchCompat.setChecked(Settings.isKeepScreenOn(context));
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Settings.setKeepScreenOn(context, isChecked);
                }
            });
        } else if (item == SettingItem.NOTIFICATION) {
            switchCompat.setChecked(Settings.isNotificationOn(context));
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Settings.setNotification(context, isChecked);
                }
            });
        }
    }

    private static void initSeekBarItem(final Context context, SettingItem item, final View view) {
        if (item == SettingItem.MAIN_AUTO_REFRESH_SPEED) {
            initAutoRefreshSpeedItem(context, view);
        }
    }

    private static void initAutoRefreshSpeedItem(final Context context, View view) {
        TextView titleTextView = (TextView) view.findViewById(R.id.setting_item_title_textview);
        final TextView statusTextView = (TextView) view.findViewById(R.id.setting_item_status_textview);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.setting_item_seekbar);

        titleTextView.setText(R.string.setting_main_auto_refresh_speed);
        int oldSpeedProgress = Settings.getAutoRefreshSpeedProgress(context);
        setAutoRefreshSpeedTextView(statusTextView, -1, oldSpeedProgress);
        seekBar.setProgress(oldSpeedProgress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int oldSpeedProgress = Settings.getAutoRefreshSpeedProgress(context);
                setAutoRefreshSpeedTextView(statusTextView, oldSpeedProgress, progress);
                Settings.setAutoRefreshSpeedProgress(context, progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // 텍스트를 한 번만 바꿔주게 예외처리
    private static void setAutoRefreshSpeedTextView(TextView textView, int oldSpeed, int newSpeed) {
        boolean isFirstLoad = false;
        if (oldSpeed == -1) {
            isFirstLoad = true;
        }
        // very slow 를 제외하고는 전부 oldSpeed 가 -1 보다 작을 경우를 체크하므로 이 경우만 isFirstLoad를 확인하면 됨
        if (newSpeed < 20) {
            if (oldSpeed >= 20 || isFirstLoad) {
                textView.setText(R.string.setting_news_feed_auto_scroll_very_slow);
            }
        } else if (newSpeed >= 20 && newSpeed < 40) {
            if (oldSpeed < 20 || oldSpeed >= 40) {
                textView.setText(R.string.setting_news_feed_auto_scroll_slow);
            }
        } else if (newSpeed >= 40 && newSpeed < 60) {
            if (oldSpeed < 40 || oldSpeed >= 60) {
                textView.setText(R.string.setting_news_feed_auto_scroll_normal);
            }
        } else if (newSpeed >= 60 && newSpeed < 80) {
            if (oldSpeed < 60 || oldSpeed >= 80) {
                textView.setText(R.string.setting_news_feed_auto_scroll_fast);
            }
        } else if (newSpeed >= 80){
            if (oldSpeed < 80) {
                textView.setText(R.string.setting_news_feed_auto_scroll_very_fast);
            }
        }
    }
}
