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

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 4.
 *
 * PanelMatrixSelectDialog
 *  뉴스피드 패널 매트릭스를 선택하는 다이얼로그 프래그먼트
 */
public class PairTvDialogFragment extends DialogFragment {
    private OnActionListener mListener;
    int previousTokenLength = 0;
    boolean isSpaceProcessStarted = false;

    public interface OnActionListener {
        void onConfirmPairing(String token);
    }

    public static PairTvDialogFragment newInstance(OnActionListener listener) {
        PairTvDialogFragment fragment = new PairTvDialogFragment();
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
                            // 간격을 모두 좁힘
                            token = token.replaceAll(" ", "");
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
                positiveButton.setEnabled(s.length() >= 9);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isSpaceProcessStarted) {
                    if (s.length() > 1 && s.length() > previousTokenLength) {
                        // 첫 입력시는 앞에 빈칸을 넣지 않기, 나머지는 한칸을 띄우고 숫자를 표시
                        String spacedToken = s.subSequence(0, s.length() - 1).toString();
                        spacedToken = spacedToken + " " + s.subSequence(s.length() - 1, s.length());

                        isSpaceProcessStarted = true;
                        tokenEditText.setText(spacedToken);
                        tokenEditText.setSelection(tokenEditText.getText().length());
                        isSpaceProcessStarted = false;

                        previousTokenLength = s.length() + 1;
                    } else if (s.length() > 1 && s.length() < previousTokenLength) {
                        // 삭제할 경우 빈칸도 삭제
                        String trimmedToken = s.subSequence(0, s.length() - 1).toString();

                        isSpaceProcessStarted = true;
                        tokenEditText.setText(trimmedToken);
                        tokenEditText.setSelection(tokenEditText.getText().length());
                        isSpaceProcessStarted = false;

                        previousTokenLength = s.length() - 1;
                    } else {
                        previousTokenLength = s.length();
                    }
                }
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
