package com.yooiistudios.newsflow.core.news.newscontent;

import com.yooiistudios.snacktoryandroid.ImageResult;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 10.
 *
 * NewsContentImage
 *  ImageResult 의 wrapper 클래스
 */
public class NewsContentImage {
    private ImageResult mImageResult;

    public NewsContentImage(String url, int weight, int width, int height) {
        mImageResult = new ImageResult(url, weight, "", height, width, "", false);
    }

    public NewsContentImage(ImageResult imageResult) {
        mImageResult = imageResult;
    }

    public String getImageUrl() {
        return mImageResult.src;
    }

    public int getWeight() {
        return mImageResult.weight;
    }

    public int getWidth() {
        return mImageResult.width;
    }

    public int getHeight() {
        return mImageResult.height;
    }
}
