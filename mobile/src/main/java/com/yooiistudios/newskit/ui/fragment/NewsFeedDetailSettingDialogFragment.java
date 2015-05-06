package com.yooiistudios.newskit.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.model.NewsFeedDetailSettings;

/**
 * Created by Wooseong Kim in News-Kit from Yooii Studios Co., LTD. on 15. 5. 5.
 *
 * NewsFeedDetailSettingFragment
 *  뉴스피드 디테일에 있는 설정 다이얼로그
 */
public class NewsFeedDetailSettingDialogFragment extends DialogFragment {
    private OnActionListener mCallback;

    public interface OnActionListener {
        void onDismissSettingDialog();
    }

    public static NewsFeedDetailSettingDialogFragment newInstance() {
        return new NewsFeedDetailSettingDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.action_settings)
                .customView(R.layout.dialog_newsfeed_detail_setting, true)
                .build();

        initAutoScrollSwitch(materialDialog);
        initStartOffsetEditText(materialDialog);
        initScrollSpeed(materialDialog);

        return materialDialog;
    }

    private void initAutoScrollSwitch(MaterialDialog materialDialog) {
        final SwitchCompat autoScrollSwitch = (SwitchCompat) materialDialog.getCustomView().findViewById(
                R.id.newsfeed_detail_setting_auto_scroll_switch);

        autoScrollSwitch.setChecked(NewsFeedDetailSettings.isNewsFeedAutoScroll(getActivity()));
        autoScrollSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                NewsFeedDetailSettings.setNewsFeedAutoScroll(getActivity(), isChecked);
            }
        });
    }

    private void initStartOffsetEditText(MaterialDialog materialDialog) {
        final EditText secondEditText = (EditText) materialDialog.getCustomView().findViewById(
                R.id.newsfeed_detail_setting_delay_edittext);
        secondEditText.setText(String.valueOf(NewsFeedDetailSettings.getStartDelaySecond(getActivity())));
        secondEditText.setSelection(secondEditText.getText().toString().length());

        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adjustEditTextValue(secondEditText);
                if (s.length() > 0) {
                    int startOffsetSecond = Integer.valueOf(secondEditText.getText().toString());
                    NewsFeedDetailSettings.setStartDelaySecond(getActivity(), startOffsetSecond);
                }
            }

            private void adjustEditTextValue(EditText editText) {
                String text = editText.getText().toString();
                if (isNumeric(text)) {
                    int integer = Integer.parseInt(text);
                    if (integer >= 60) {
                        editText.setText("59");
                        editText.setSelection(editText.length());
                    }
                } else if (text.contains("-")) {
                    editText.setText(text.replaceAll("-", ""));
                }
            }
        };

        secondEditText.addTextChangedListener(textWatcher);
    }

    private void initScrollSpeed(MaterialDialog materialDialog) {
        final SeekBar speedSeekBar = (SeekBar) materialDialog.getCustomView().findViewById(
                R.id.newsfeed_detail_setting_speed_seekbar);
        final TextView statusTextView = (TextView) materialDialog.getCustomView().findViewById(
                R.id.newsfeed_detail_setting_speed_textview);

        int oldSpeedProgress = NewsFeedDetailSettings.getSpeed(getActivity());
        setAutoRefreshSpeedTextView(statusTextView, -1, oldSpeedProgress);

        speedSeekBar.setProgress(oldSpeedProgress);
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int oldSpeedProgress = NewsFeedDetailSettings.getSpeed(getActivity());
                setAutoRefreshSpeedTextView(statusTextView, oldSpeedProgress, progress);
                NewsFeedDetailSettings.setSpeed(getActivity(), progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public static boolean isNumeric(String string) {
        if (string == null || string.length() == 0)
            return false;

        int l = string.length();
        for (int i = 0; i < l; i++) {
            if (!Character.isDigit(string.codePointAt(i)))
                return false;
        }
        return true;
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mCallback.onDismissSettingDialog();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActionListener");
        }
    }
}
