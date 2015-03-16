package com.yooiistudios.newsflow.ui.presenter;

import android.content.Context;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yooiistudios.newsflow.R;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 14.
 * SettingItemPresenter
 * Description
 */
public class SettingItemPresenter extends Presenter {
    private static final int GRID_ITEM_WIDTH = 250;
    private static final int GRID_ITEM_HEIGHT = 250;

    private static final int GRID_ITEM_IMG_PADDING = 50;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();

        ImageCardView imageCardView = new ImageCardView(context);
        imageCardView.setCardType(BaseCardView.CARD_TYPE_INFO_UNDER);
        imageCardView.setFocusable(true);
        imageCardView.setFocusableInTouchMode(true);
        imageCardView.setBackgroundColor(context.getResources().getColor(R.color.card_background));

        // main image
        imageCardView.setMainImageDimensions(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT);
        imageCardView.getMainImageView().setPadding(GRID_ITEM_IMG_PADDING, GRID_ITEM_IMG_PADDING,
                GRID_ITEM_IMG_PADDING, GRID_ITEM_IMG_PADDING);
        imageCardView.setMainImageScaleType(ImageView.ScaleType.CENTER_CROP);

        // title
        imageCardView.setInfoAreaBackgroundColor(
                context.getResources().getColor(R.color.card_info_background));

        return new ViewHolder(imageCardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ImageCardView imageCardView = ((ImageCardView) viewHolder.view);
        SettingObject settingObject = (SettingObject) item;
        Context context = imageCardView.getContext();
        if (settingObject.resourceId != -1) {
            imageCardView.setMainImage(context.getResources().getDrawable(settingObject.resourceId));
        }
        imageCardView.setTitleText(context.getString(settingObject.titleId));
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }

    public static class SettingObject {
        private int titleId;
        private int resourceId = -1;

        @SuppressWarnings("unused")
        private SettingObject() {}

        public SettingObject(int titleId) {
            this.titleId = titleId;
        }

        public SettingObject(int titleId, int resourceId) {
            this.titleId = titleId;
            this.resourceId = resourceId;
        }

        public int getTitleId() { return titleId; }
    }
}
