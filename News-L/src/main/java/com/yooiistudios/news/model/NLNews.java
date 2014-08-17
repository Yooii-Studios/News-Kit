package com.yooiistudios.news.model;

import java.util.ArrayList;

import nl.matshofman.saxrssreader.RssItem;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * NLNews
 * Rss Feed의 <item> 하나를 표현하는 클래스
 */
public class NLNews extends RssItem {

    private ArrayList<String> mImageUrlList;

    public NLNews() {
        mImageUrlList = new ArrayList<String>();
    }

    public void addImageUrl(String url) {
        mImageUrlList.add(url);
    }
    public void setImageUrlList(ArrayList<String> list) {
        mImageUrlList = list;
    }
    public ArrayList<String> getImageUrlList() {
        return mImageUrlList;
    }

    /**
     * 해당 뉴스를 대표하는 이미지의 url
     * @return First image in image list. May be null if there's no image.
     */
    public String getMainImageUrl() {
        if (mImageUrlList.size() > 0) {
            return mImageUrlList.get(0);
        }
        else {
            return null;
        }
    }
}
