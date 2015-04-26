package com.yooiistudios.newskit.tv.model.ui.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.yooiistudios.newskit.tv.R;


/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 3. 6.
 *
 * TintedProgressBar
 *  바 색을 변경할 수 있는 ProgressBar, 뒷 색깔이 제대로 안 빠져서 일단 사용 안함
 */
public class TintedProgressBar extends ProgressBar {
    public TintedProgressBar(Context context) {
        super(context);
        if (!isInEditMode()) {
            init();
        }
    }

    public TintedProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init();
        }
    }

    public TintedProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init();
        }
    }

    public TintedProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            init();
        }
    }

    private void init() {
        int primaryColor = getResources().getColor(R.color.material_light_blue_500);
//        int primaryDarkColor = getResources().getColor(R.color.material_light_blue_500);
        getIndeterminateDrawable().setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
        getProgressDrawable().setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
    }
}
