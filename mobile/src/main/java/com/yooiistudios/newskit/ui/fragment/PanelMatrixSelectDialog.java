package com.yooiistudios.newskit.ui.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newskit.core.panelmatrix.PanelMatrixUtils;

import java.util.ArrayList;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 4.
 *
 * PanelMatrixSelectDialog
 *  뉴스피드 패널 매트릭스를 선택하는 다이얼로그 프래그먼트
 */
public class PanelMatrixSelectDialog extends DialogFragment {
    private OnActionListener mListener;

    public interface OnActionListener {
        public void onSelectMatrix(int position);
    }

    public static PanelMatrixSelectDialog newInstance(OnActionListener listener) {
        PanelMatrixSelectDialog fragment = new PanelMatrixSelectDialog();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<String> matrixTypeList = new ArrayList<>();
        for (int i = 0; i < PanelMatrix.values().length; i++) {
            PanelMatrix panelMatrixType = PanelMatrix.getByUniqueKey(i);
            matrixTypeList.add(panelMatrixType.getDisplayName());
        }
        String[] matrixArr = matrixTypeList.toArray(new String[matrixTypeList.size()]);

        PanelMatrix matrixType = PanelMatrixUtils.getCurrentPanelMatrix(getActivity());

        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.setting_main_panel_matrix)
                .items(matrixArr)
                .itemsCallbackSingleChoice(matrixType.getUniqueId(), new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (mListener != null) {
                            mListener.onSelectMatrix(which);
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
