package com.yooiistudios.news.ui.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.news.R;
import com.yooiistudios.news.model.language.Language;
import com.yooiistudios.news.model.language.LanguageUtils;

import java.util.ArrayList;

/**
 * Created by Wooseong Kim on in News Flow from Yooii Studios Co., LTD. on 2015. 2. 4.
 *
 * LanguageSelectDialog
 *  언어를 선택하는 DialogFragment
 */
public class LanguageSelectDialog extends DialogFragment {
    private OnActionListener mListener;

    public interface OnActionListener {
        public void onSelectLanguage(int position);
    }

    public static LanguageSelectDialog newInstance(OnActionListener listener) {
        LanguageSelectDialog fragment = new LanguageSelectDialog();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<String> languageList = new ArrayList<>();
        for (int i = 0; i < Language.values().length; i++) {
            Language languageType = Language.valueOf(i);
            languageList.add(getString(languageType.getLocalNotationStringId()));
        }
        String[] languages = languageList.toArray(new String[languageList.size()]);

        Language currentLanguageType = LanguageUtils.getCurrentLanguageType(getActivity());

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
