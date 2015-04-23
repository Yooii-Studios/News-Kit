package com.yooiistudios.newsflow.ui.widget;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.util.Display;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 20.
 *
 * MainBottomItemLayout
 *  뷰의 긴 변의 길이에 맞게 뷰의 크기를 변형해주는 클래스
 */
public class MainBottomItemLayout extends RatioFrameLayout {
    @IntDef(value = { PORTRAIT, LANDSCAPE })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {}
    public interface OnSupplyTargetAxisLengthListener {
        /**
         * 필요한 경우 기준 축, screen orientation 을 기준으로 값을 직접 제공하기 위한 메서드이다.
         * @param axis 기준이 되는 축. {@link #AXIS_WIDTH}, {@link #AXIS_HEIGHT} 중 하나여야 한다.
         * @param orientation screen orientation. {@link #PORTRAIT}, {@link #LANDSCAPE} 중 하나여야 한다.
         * @return 계산된 길이.
         */
        public int onSupply(@Axis int axis, @Orientation int orientation);
    }

    private static final String TAG = MainBottomItemLayout.class.getName();

    public static final int PORTRAIT = 0;
    public static final int LANDSCAPE = 1;

    public static final float HEIGHT_OVER_WIDTH_RATIO = 3.3f / 4.0f;

    private OnSupplyTargetAxisLengthListener mOnSupplyTargetAxisLengthListener;
    private int mOrientation = PORTRAIT;

    public MainBottomItemLayout(Context context) {
        super(context);
    }

    public MainBottomItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainBottomItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOrientation(@Orientation int orientation) {
        mOrientation = orientation;
    }

    public void setOnSupplyTargetAxisLengthListener(OnSupplyTargetAxisLengthListener onSupplyTargetAxisLengthListener) {
        mOnSupplyTargetAxisLengthListener = onSupplyTargetAxisLengthListener;
    }

    @Override
    protected float getTargetAxisLength(float baseAxisLength) {
        int axis = getBaseAxis();
        if (axis == AXIS_WIDTH) {
            if (mOnSupplyTargetAxisLengthListener != null) {
                int targetLength = mOnSupplyTargetAxisLengthListener.onSupply(axis, mOrientation);
                if (targetLength > 0) {
                    return targetLength;
                }
            }
            if (mOrientation == PORTRAIT) {
                return getRowHeight(baseAxisLength);
            } else {
                return -1;
            }
        } else {
            // 가로축 지원 안함.
            return 0;
        }
    }

    public static int measureParentHeightOnPortrait(Context context, int itemCount, int columnCount) {
        // get display width
        Point displaySize = Display.getDisplaySize(context);
        int displayWidth = displaySize.x;

        // main_bottom_margin_small : item padding = recyclerView margin
        float recyclerViewMargin = context.getResources().
                getDimension(R.dimen.main_bottom_margin_small);

        float rowWidth = (displayWidth - (recyclerViewMargin * 2)) / columnCount;
        float rowHeight = getRowHeight(rowWidth);

        int rowCount = itemCount / columnCount;
        if (itemCount % columnCount != 0) {
            rowCount += 1;
        }

        return Math.round(rowHeight * rowCount);
    }

    public static int measureParentHeightOnLandscape(ViewGroup parent) {
        Context context = parent.getContext();
        int height = Display.getDisplayHeightWithoutStatusBar(context);

        // 마진 대신 패딩으로 바꾸었기 때문에 광고 상관없이 바깥 패딩을 빼주고 계산
        int padding = context.getResources().getDimensionPixelSize(
                R.dimen.main_bottom_margin_small);

        height -= padding * 2;

        return height;
    }

    public static float getRowHeight(float width) {
        return width * (HEIGHT_OVER_WIDTH_RATIO);
    }

    public static float getRowWidth(float height) {
        return height * (1 / HEIGHT_OVER_WIDTH_RATIO);
    }
}
