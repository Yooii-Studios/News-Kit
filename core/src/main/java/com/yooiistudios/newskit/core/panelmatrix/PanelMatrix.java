package com.yooiistudios.newskit.core.panelmatrix;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 4.
 *
 * PanelMatrix
 *  뉴스피드 패널 매트릭스 타입
 */
public enum PanelMatrix {
    TWO_BY_TWO(0, 4, "2 X 2"),
    THREE_BY_TWO(1, 6, "3 X 2"),
    FOUR_BY_TWO(2, 8, "4 X 2");

    private int uniqueKey;
    private int panelCount;
    private String displayName;

    private PanelMatrix(int uniqueKey, int panelCount, String displayName) {
        this.uniqueKey = uniqueKey;
        this.panelCount = panelCount;
        this.displayName = displayName;
    }

    public static PanelMatrix getByUniqueKey(int uniqueKey) {
        for (PanelMatrix item : PanelMatrix.values()) {
            if (item.uniqueKey == uniqueKey) {
                return item;
            }
        }
        return getDefault();
    }

    public static PanelMatrix getDefault() {
        return TWO_BY_TWO;
    }

    public int getUniqueId() {
        return uniqueKey;
    }

    public int getPanelCount() {
        return panelCount;
    }

    public String getDisplayName() {
        return displayName;
    }
}
