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

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 4.
 *
 * PanelMatrixSelectDialog
 *  뉴스피드 패널 매트릭스를 선택하는 다이얼로그 프래그먼트
 */
public class PairTvDialog extends DialogFragment {
    private OnActionListener mListener;

    public interface OnActionListener {
        public void onConfirmPairing(String token);
    }

    public static PairTvDialog newInstance(OnActionListener listener) {
        PairTvDialog fragment = new PairTvDialog();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.setting_pair_tv)
                .customView(R.layout.dialog_pair_tv, true)
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (mListener != null) {
                            String token =
                                    ((EditText) dialog.findViewById(R.id.dialog_pair_tv_token_edittext))
                                            .getText().toString();
                            mListener.onConfirmPairing(token);
                        }

                    }
                })
                .build();

        final View positiveButton = materialDialog.getActionButton(DialogAction.POSITIVE);
        final EditText tokenEditText =
                (EditText) materialDialog.getCustomView().findViewById(R.id.dialog_pair_tv_token_edittext);

        tokenEditText.setText("");
        tokenEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() >= 5) {
                    positiveButton.setEnabled(true);
                } else {
                    positiveButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        materialDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                tokenEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(tokenEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        materialDialog.show();
        positiveButton.setEnabled(false);

        materialDialog.setCancelable(false);
        materialDialog.setCanceledOnTouchOutside(false);
        return materialDialog;
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
