package com.yooiistudios.newskit.util;

import com.yooiistudios.newskit.model.PanelEditMode;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 21.
 *
 * OnEditModeChangeListener
 *  Edit mode 가 변경될 경우 불릴 콜백
 */
public interface OnMainPanelEditModeEventListener {
    void onEditModeChange(PanelEditMode editMode);
    void onTouchBottomEditLayout();
}
