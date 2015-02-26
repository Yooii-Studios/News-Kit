package com.yooiistudios.newsflow.ui.fragment;

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
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.model.Settings;

/**
 * Created by Wooseong Kim on in News Flow from Yooii Studios Co., LTD. on 2015. 2. 4.
 *
 * AutoRefreshIntervalDialogFragment
 *  오토 리프레시 간격을 설정하는 다이얼로그 프래그먼트
 */
public class AutoRefreshIntervalDialogFragment extends DialogFragment {
    private OnActionListener mListener;

    public interface OnActionListener {
        public void onTypeAutoRefreshInterval(int interval);
    }

    public static AutoRefreshIntervalDialogFragment newInstance(OnActionListener listener) {
        AutoRefreshIntervalDialogFragment fragment = new AutoRefreshIntervalDialogFragment();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog materialDialog = makeDialog();
        EditText editText =
                (EditText) materialDialog.getCustomView().findViewById(R.id.auto_refresh_dialog_edit);
        initEditText(editText);
        initOkButton(materialDialog, editText);
        requestEditTextFocus(materialDialog, editText);
        return materialDialog;
    }

    private MaterialDialog makeDialog() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.setting_main_auto_refresh_interval)
                .customView(R.layout.dialog_fragment_auto_refresh_interval, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText editText =
                                (EditText) dialog.getCustomView().findViewById(R.id.auto_refresh_dialog_edit);
                        if (mListener != null) {
                            mListener.onTypeAutoRefreshInterval(Integer.valueOf(editText.getText().toString()));
                        }
                    }
                })
                .build();
        materialDialog.setCancelable(false);
        materialDialog.setCanceledOnTouchOutside(false);
        return materialDialog;
    }

    private EditText initEditText(EditText editText) {
        editText.setText(String.valueOf(Settings.getAutoRefreshInterval(getActivity())));
        return editText;
    }

    private void initOkButton(MaterialDialog materialDialog, EditText editText) {
        final View positiveAction = materialDialog.getActionButton(DialogAction.POSITIVE);

        editText.setText(String.valueOf(Settings.getAutoRefreshInterval(getActivity())));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                positiveAction.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (!(editText.getText().toString().trim().length() > 0)) {
            positiveAction.setEnabled(false);
        }
    }

    private void requestEditTextFocus(MaterialDialog materialDialog, final EditText editText) {
        materialDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                editText.requestFocus();
                editText.setSelection(editText.length());
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
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
