package com.yooiistudios.newskit.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.language.Language;
import com.yooiistudios.newskit.core.language.LanguageUtils;

import java.util.ArrayList;

/**
 * Created by Wooseong Kim on in News Kit from Yooii Studios Co., LTD. on 2015. 2. 4.
 *
 * LanguageSelectDialog
 *  언어를 선택하는 DialogFragment
 */
public class LanguageSelectDialogFragment extends DialogFragment {
    private OnActionListener mListener;

    public interface OnActionListener {
        void onSelectLanguage(int position);
    }

    public static LanguageSelectDialogFragment newInstance(OnActionListener listener) {
        LanguageSelectDialogFragment fragment = new LanguageSelectDialogFragment();
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

        Language currentLanguageType = LanguageUtils.getCurrentLanguage(getActivity());

        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.setting_language)
                .items(languages)
                .itemsCallbackSingleChoice(currentLanguageType.getIndex(), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (mListener != null) {
                            mListener.onSelectLanguage(which);
                        }
                        return true;
                    }
                })
                .contentColor(getResources().getColor(R.color.material_black_primary_text))
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
