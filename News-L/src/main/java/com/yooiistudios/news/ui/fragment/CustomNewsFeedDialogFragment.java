package com.yooiistudios.news.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.news.NewsFeedUrl;
import com.yooiistudios.news.model.news.NewsFeedUrlType;
import com.yooiistudios.news.model.news.NewsFeedUtils;

/**
 * Created by Dongheyon Jeong on in morning-kit from Yooii Studios Co., LTD. on 2014. 7. 5.
 *
 * CustomNewsFeedDialogFragment
 *  커스텀 RSS 주소를 입력하는 다이얼로그
 */
public class CustomNewsFeedDialogFragment extends DialogFragment {
    private AutoCompleteTextView mFeedUrlEditText;

    public static CustomNewsFeedDialogFragment newInstance() {
        return new CustomNewsFeedDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View root = inflater.inflate(R.layout.dialog_fragment_custom_news_feed, null, false);
        mFeedUrlEditText = (AutoCompleteTextView)root.findViewById(R.id.urlEditText);

        // config adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                NewsFeedUtils.getUrlHistory(getActivity()));
        mFeedUrlEditText.setAdapter(adapter);

        View clearButton = root.findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFeedUrlEditText.setText("");
            }
        });
        clearButton.bringToFront();

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.custom_news_feed_dialog_title)
                .setView(root)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Activity activity = getActivity();
                                if (activity != null && activity instanceof OnClickListener) {
                                    String url = mFeedUrlEditText.getText()
                                            .toString().replaceAll("\\s+","");

                                    // add "http://" if it's not entered.
                                    if (!url.toLowerCase().matches("^\\w+://.*")) {
                                        // "http://" 안붙은 url도 저장
                                        NewsFeedUtils.addUrlToHistory(getActivity(), url);

                                        url = "http://" + url;
                                    }
                                    NewsFeedUtils.addUrlToHistory(getActivity(), url);

                                    ((OnClickListener) activity).onConfirm(
                                            new NewsFeedUrl(url, NewsFeedUrlType.CUSTOM)
                                    );
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Activity activity = getActivity();
                                if (activity != null && activity instanceof OnClickListener) {
                                    ((OnClickListener) activity).onCancel();
                                }
                            }
                        }
                ).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                mFeedUrlEditText.requestFocus();
                mFeedUrlEditText.setSelection(mFeedUrlEditText.length());
                InputMethodManager imm =
                        (InputMethodManager)getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mFeedUrlEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });


        return dialog;
    }

    public interface OnClickListener {
        public void onConfirm(NewsFeedUrl feedUrl);
        public void onCancel();
    }
}
