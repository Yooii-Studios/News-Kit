package com.yooiistudios.newsflow.core.news.newscontent;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 10.
 *
 * NewsContentFetchState
 *  뉴스 내용 파싱이 제대로 되었는지에 대한 상태
 */
public enum NewsContentFetchState {
    NOT_FETCHED_YET(0),
    SUCCESS(1),
    ERROR(2);

    private int mIndex;

    private NewsContentFetchState(int index) {
        mIndex = index;
    }

    public int getIndex() {
        return mIndex;
    }

    public static NewsContentFetchState getByIndex(int index) {
        for (NewsContentFetchState state : NewsContentFetchState.values()) {
            if (state.getIndex() == index) {
                return state;
            }
        }

        return NOT_FETCHED_YET;
    }
}
