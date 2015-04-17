package com.yooiistudios.newsflow.core.panelmatrix;

import android.content.Context;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 2014. 1. 15.
 *
 * PanelMatrixUtils
 *  메인 바텀 뉴스피드 패널 매트릭스의 유틸리티 클래스
 */
public class PanelMatrixUtils {
    public static final String PANEL_MATRIX_SHARED_PREFERENCES = "panel_matrix_shared_preferences";
    public static final String PANEL_MATRIX_KEY = "panel_matrix_key";
    public static final String PANEL_MATRIX_CHANGED_KEY = "panel_matrix_changed_key";

    private volatile static PanelMatrixUtils instance;
    private PanelMatrix mCurrentPanelMatrix;
    private boolean mIsPanelMatrixChanged;

    /**
     * Singleton
     */
    private PanelMatrixUtils(Context context) {
        int uniqueId = context.getSharedPreferences(PANEL_MATRIX_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .getInt(PANEL_MATRIX_KEY, -1);
        // 최초 설치시 2 X 2 로 설정
        if (uniqueId == -1) {
            uniqueId = PanelMatrix.TWO_BY_TWO.getUniqueId();
            context.getSharedPreferences(PANEL_MATRIX_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                    .edit().putInt(PANEL_MATRIX_KEY, uniqueId).apply();
        }
        mCurrentPanelMatrix = PanelMatrix.getByUniqueKey(uniqueId);
        mIsPanelMatrixChanged = false;
    }

    public static PanelMatrixUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (PanelMatrixUtils.class) {
                if (instance == null) {
                    instance = new PanelMatrixUtils(context);
                }
            }
        }
        return instance;
    }

    public static PanelMatrix getCurrentPanelMatrix(Context context) {
        return PanelMatrixUtils.getInstance(context).mCurrentPanelMatrix;
    }

    public static void setCurrentPanelMatrix(PanelMatrix newPanelMatrix, Context context) {
        PanelMatrixUtils.getInstance(context).mCurrentPanelMatrix = newPanelMatrix;
        context.getSharedPreferences(PANEL_MATRIX_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putInt(PANEL_MATRIX_KEY, newPanelMatrix.getUniqueId()).apply();

        setPanelMatrixChanged(context, true);
    }

    // 이 변수는 동적으로만 사용될 것이기에 SharedPreferences 에 저장을 하지는 않음
    public static void setPanelMatrixChanged(Context context, boolean changed) {
        PanelMatrixUtils.getInstance(context).mIsPanelMatrixChanged = changed;
    }

    public static boolean isPanelMatrixChanged(Context context) {
        return PanelMatrixUtils.getInstance(context).mIsPanelMatrixChanged;
    }
}
