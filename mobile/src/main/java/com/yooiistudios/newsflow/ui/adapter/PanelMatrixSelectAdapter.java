package com.yooiistudios.newsflow.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.iab.IabProducts;
import com.yooiistudios.newsflow.model.panelmatrix.PanelMatrix;
import com.yooiistudios.newsflow.model.panelmatrix.PanelMatrixUtils;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 12. 9.
 *
 * PanelMatrixSelectAdapter
 *  세팅 화면 패널 매트릭스(갯수) 선택 다이얼로그의 리스트뷰에 사용될 어뎁터
 */
public class PanelMatrixSelectAdapter extends BaseAdapter {

    private Context mContext;
    private PanelMatrix mCurrentPanelMatrix;

    public PanelMatrixSelectAdapter(Context context, PanelMatrix currentPanelMatrix) {
        mContext = context;
        mCurrentPanelMatrix = currentPanelMatrix;
    }

    @Override
    public int getCount() {
        return PanelMatrix.values().length;
    }

    @Override
    public Object getItem(int position) {
        return PanelMatrix.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.panel_matrix_select_dialog_list_item, null);
        }

        PanelMatrix panelMatrix = PanelMatrix.values()[position];

        TextView panelMatrixNameTextView = (TextView)convertView.findViewById(R.id.panel_matrix_name);
        panelMatrixNameTextView.setText(panelMatrix.getDisplayName());

        ImageView lockImageView = (ImageView)convertView.findViewById(R.id.setting_item_lock_imageview);
        if (IabProducts.containsSku(mContext, IabProducts.SKU_MORE_PANELS)
                || PanelMatrixUtils.isMatrixAvailable(mContext, panelMatrix)) {
            lockImageView.setVisibility(View.INVISIBLE);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            lockImageView.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(
                    mContext.getResources().getColor(R.color.setting_locked_item_background));
        }

        ImageView checkImageView = (ImageView)convertView.findViewById(R.id.setting_item_check_imageview);
        if (panelMatrix.equals(mCurrentPanelMatrix)) {
            checkImageView.setVisibility(View.VISIBLE);
        } else {
            checkImageView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
}
