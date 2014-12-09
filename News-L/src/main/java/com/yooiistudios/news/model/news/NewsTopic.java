package com.yooiistudios.news.model.news;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 12. 9.
 *
 * NewsTopic
 *  뉴스 피드에 default로 제공되어야 하는지의 정보를 추가한 클래스
 */
public class NewsTopic extends NewsFeed {
    private boolean mIsDefault;

    public boolean isDefault() {
        return mIsDefault;
    }

    public void setDefault(boolean isDefault) {
        mIsDefault = isDefault;
    }
}
