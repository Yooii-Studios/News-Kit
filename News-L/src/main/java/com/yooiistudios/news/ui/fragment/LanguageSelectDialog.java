package com.yooiistudios.news.ui.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.language.LanguageType;

import java.util.ArrayList;

/**
 * Created by Wooseong Kim on in News Flow from Yooii Studios Co., LTD. on 2015. 2. 4.
 *
 * LanguageSelectDialog
 *  언어를 선택하는 DialogFragment
 */
public class LanguageSelectDialog extends DialogFragment {
    private static final String PARAM_LANGUAGE_ID = "param_language_id";
    private int mLanguageId;
    private OnActionListener mListener;

    public interface OnActionListener {
        public void onSelectLanguage(int position);
    }

    public static LanguageSelectDialog newInstance(int languageId, OnActionListener listener) {
        LanguageSelectDialog fragment = new LanguageSelectDialog();
        fragment.setListener(listener);
        Bundle args = new Bundle();
        args.putInt(PARAM_LANGUAGE_ID, languageId);
        fragment.setArguments(args);
        return fragment;
    }

    public LanguageSelectDialog() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLanguageId = getArguments().getInt(PARAM_LANGUAGE_ID);
        } else {
            mLanguageId = LanguageType.ENGLISH.getUniqueId();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LanguageType currentLanguageType = LanguageType.valueOfUniqueId(mLanguageId);

        ArrayList<String> languageList = new ArrayList<>();
        for (int i = 0; i < LanguageType.values().length; i++) {
            LanguageType languageType = LanguageType.valueOf(i);
            languageList.add(getString(languageType.getLocalNotationStringId()));
        }
        String[] languages = languageList.toArray(new String[languageList.size()]);

        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.setting_language)
                .items(languages)
                .itemsCallbackSingleChoice(currentLanguageType.getIndex(), new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (mListener != null) {
                            mListener.onSelectLanguage(which);
                        }
                    }
                })
                .negativeText(R.string.cancel)
                .build();
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
