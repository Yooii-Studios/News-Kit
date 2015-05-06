package com.yooiistudios.newskit.ui.fragment.dialog;

import android.app.Activity;
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
import com.yooiistudios.newskit.core.news.NewsFeedUrl;
import com.yooiistudios.newskit.core.news.NewsFeedUrlType;

/**
 * Created by Dongheyon Jeong on in morning-kit from Yooii Studios Co., LTD. on 2014. 7. 5.
 *
 * CustomRssDialogFragment
 *  커스텀 RSS 주소를 입력하는 다이얼로그
 */
public class CustomRssDialogFragment extends DialogFragment {
    private OnActionListener mCallback;

    public interface OnActionListener {
        void onEnterCustomRss(NewsFeedUrl feedUrl);
    }

    public static CustomRssDialogFragment newInstance() {
        return new CustomRssDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.custom_news_feed_dialog_title)
                .customView(R.layout.dialog_fragment_custom_url, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText urlEditText =
                                (EditText) dialog.getCustomView().findViewById(R.id.custom_rss_dialog_url_edit);
                        String url = urlEditText.getText()
                                .toString().replaceAll("\\s+", "");

                        // add "http://" if it's not entered.
                        if (!url.toLowerCase().matches("^\\w+://.*")) {
                            // "http://" 안붙은 url 도 저장
//                            NewsFeedUtils.addUrlToHistory(getActivity(), url);
                            url = "http://" + url;
                        }
                        mCallback.onEnterCustomRss(new NewsFeedUrl(url, NewsFeedUrlType.CUSTOM));
                    }
                })
                .build();
        materialDialog.setCancelable(false);
        materialDialog.setCanceledOnTouchOutside(false);

        final View positiveAction = materialDialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(false); // disabled by default

        final EditText urlEditText =
                (EditText) materialDialog.getCustomView().findViewById(R.id.custom_rss_dialog_url_edit);
        urlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                positiveAction.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        materialDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                urlEditText.requestFocus();
                urlEditText.setSelection(urlEditText.length());
                InputMethodManager imm = (InputMethodManager)getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(urlEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        return materialDialog;
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
