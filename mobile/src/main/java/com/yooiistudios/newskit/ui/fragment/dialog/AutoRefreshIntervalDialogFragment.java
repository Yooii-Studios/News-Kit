package com.yooiistudios.newskit.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.model.Settings;

/**
 * Created by Wooseong Kim on in News Kit from Yooii Studios Co., LTD. on 2015. 2. 4.
 *
 * AutoRefreshIntervalDialogFragment
 *  오토 리프레시 간격을 설정하는 다이얼로그 프래그먼트
 */
public class AutoRefreshIntervalDialogFragment extends DialogFragment {
    private MaterialDialog mMaterialDialog;
    private EditText mMinuteEditText;
    private EditText mSecondEditText;
    private OnActionListener mListener;

    public interface OnActionListener {
        void onTypeAutoRefreshInterval(int interval);
    }

    public static AutoRefreshIntervalDialogFragment newInstance(OnActionListener listener) {
        AutoRefreshIntervalDialogFragment fragment = new AutoRefreshIntervalDialogFragment();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initDialog();
        initViews();
        initEditText();
        initOkButton();
        requestEditTextFocus();
        return mMaterialDialog;
    }

    private void initViews() {
        mMinuteEditText = (EditText) mMaterialDialog.getCustomView().findViewById(
                R.id.auto_refresh_dialog_edit_text_min);
        mSecondEditText = (EditText) mMaterialDialog.getCustomView().findViewById(
                R.id.auto_refresh_dialog_edit_text_sec);
    }

    private void initDialog() {
        mMaterialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.setting_main_auto_refresh_interval)
                .customView(R.layout.dialog_fragment_auto_refresh_interval, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (mListener != null) {
                            mListener.onTypeAutoRefreshInterval(getEnteredTime());
                        }
                    }
                })
                .build();
        mMaterialDialog.setCancelable(false);
        mMaterialDialog.setCanceledOnTouchOutside(false);
    }

    private void initEditText() {
        mMinuteEditText.setText(String.valueOf(getSavedAutoRefreshIntervalMinuteString()));
        mMinuteEditText.setHint("0");
        mSecondEditText.setText(String.valueOf(getSavedAutoRefreshIntervalSecondString()));
        mSecondEditText.setHint("0");
    }

    private String getSavedAutoRefreshIntervalMinuteString() {
        int minute = Settings.getAutoRefreshIntervalMinute(getActivity());
        return minute != 0 ? String.valueOf(minute) : "";
    }

    private String getSavedAutoRefreshIntervalSecondString() {
        int second = Settings.getAutoRefreshIntervalSecond(getActivity());
        return second != 0 ? String.valueOf(second) : "";
    }

    private void initOkButton() {
        final View positiveAction = mMaterialDialog.getActionButton(DialogAction.POSITIVE);

        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adjustEditTextValue(mMinuteEditText);
                adjustEditTextValue(mSecondEditText);

                positiveAction.setEnabled(mMinuteEditText.getText().length() > 0
                                || mSecondEditText.getText().length() > 0);
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
        mMinuteEditText.addTextChangedListener(textWatcher);
        mSecondEditText.addTextChangedListener(textWatcher);
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

    private int getEnteredTime() {
        return getEnteredMinute() * 60 + getEnteredSecond();
    }

    private int getEnteredMinute() {
        return stringToInt(mMinuteEditText.getText().toString());
    }

    private int getEnteredSecond() {
        return stringToInt(mSecondEditText.getText().toString());
    }

    private int stringToInt(String string) {
        return string.length() > 0 ? Integer.parseInt(string) : 0;
    }

    private void requestEditTextFocus() {
        mMaterialDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mSecondEditText.requestFocus();
                mSecondEditText.setSelection(mSecondEditText.length());
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSecondEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setListener(OnActionListener listener) {
        mListener = listener;
    }
}
