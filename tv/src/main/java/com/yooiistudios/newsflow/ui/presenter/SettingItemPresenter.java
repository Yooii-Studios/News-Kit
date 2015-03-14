package com.yooiistudios.newsflow.ui.presenter;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yooiistudios.newsflow.R;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 14.
 * SettingItemPresenter
 * Description
 */
public class SettingItemPresenter extends Presenter {
    private static final int GRID_ITEM_WIDTH = 250;
    private static final int GRID_ITEM_HEIGHT = 250;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();

        TextView view = new TextView(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setBackgroundColor(context.getResources().getColor(R.color.card_background_dark));
        view.setTextColor(context.getResources().getColor(R.color.material_white_primary_text));
        view.setTextAppearance(context, R.style.TextAppearance_Leanback_DetailsDescriptionSubtitle);
        view.setGravity(Gravity.CENTER);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((TextView) viewHolder.view).setText((String) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}
