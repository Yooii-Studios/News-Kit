package com.yooiistudios.newsflow.core.news;

import android.support.annotation.NonNull;

import com.yooiistudios.snacktoryandroid.ImageResult;
import com.yooiistudios.snacktoryandroid.JResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 10.
 *
 * NewsContent
 *  뉴스 기사 내용의 컨텐츠(광고 등 제거된)를 가진 JResult 객체의 wrapper
 */
public class NewsContent {
    private JResult mJResult;
    private NewsContentFetchState mFetchState;

    /**
     * 이 클래스는 파싱 성공시 무조건 mJResult 객체를 가져야 한다.
     * 기본 생성자는 에러를 뜻하며 내부에서만 사용되어야 한다.
     */
    private NewsContent() {
        mFetchState = NewsContentFetchState.ERROR;
    }

    public NewsContent(@NonNull JResult content) {
        mJResult = content;
    }

    public static NewsContent createErrorObject() {
        return new NewsContent();
    }

    public String getUrl() {
        String url = mJResult.getUrl();
        if (url == null) {
            url = "";
        }
        return url;
    }

    public void setUrl(String url) {
        mJResult.setUrl(url);
    }

    public String getImageUrl() {
        String imageUrl = mJResult.getImageUrl();
        if (imageUrl == null) {
            imageUrl = "";
        }
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mJResult.setImageUrl(imageUrl);
    }

    public String getText() {
        String text = mJResult.getText();
        if (text == null) {
            text = "";
        }

        return text;
    }

    public void setText(String text) {
        mJResult.setText(text);
    }

    public List<String> getTextList() {
        List<String> textList = mJResult.getTextList();
        if(textList == null) {
            textList = new ArrayList<>();
        }
        return textList;
    }

    public void setTextList(List<String> textList) {
        mJResult.setTextList(textList);
    }

    public String getTitle() {
        String title = mJResult.getTitle();
        if (title == null) {
            title = "";
        }
        return title;
    }

    public void setTitle(String title) {
        mJResult.setTitle(title);
    }

    public String getVideoUrl() {
        String videoUrl = mJResult.getVideoUrl();
        if (videoUrl == null) {
            videoUrl = "";
        }
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        mJResult.setVideoUrl(videoUrl);
    }

    /**
     * @return images list
     */
    public List<ImageResult> getImages() {
        List<ImageResult> images = mJResult.getImages();
        if (images == null) {
            images = Collections.emptyList();
        }
        return images;
    }

    /**
     * set images list
     */
    public void setImages(List<ImageResult> images) {
        mJResult.setImages(images);
    }
}
