package com.yooiistudios.newsflow.core.news.newscontent;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.yooiistudios.snacktoryandroid.JResult;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 10.
 *
 * NewsContent
 *  뉴스 기사 내용의 컨텐츠(광고 등 제거된)를 가진 JResult 객체의 wrapper
 */
public class NewsContent implements Parcelable {
    private JResult mJResult;
//    private List<NewsContentImage> mImages;
    private NewsContentFetchState mFetchState;

    /**
     * 이 클래스는 파싱 성공시 무조건 mJResult 객체를 가져야 한다.
     * 기본 생성자는 에러를 뜻하며 내부에서만 사용되어야 한다.
     */
    protected NewsContent() {
        mJResult = new JResult();
    }

    private NewsContent(String title, String url, String text, String videoUrl,
                       NewsContentFetchState fetchState) {
        JResult result = new JResult();
        result.setTitle(title);
        result.setUrl(url);
        result.setText(text);
//        result.setImageUrl(imageUrl);
        result.setVideoUrl(videoUrl);
//        result.setTextList(texts);

        mJResult = result;
//        mImages = images;
        mFetchState = fetchState;
    }

    public NewsContent(@NonNull JResult content) {
        mJResult = content;
//        mImages = new ArrayList<>();
//        for (ImageResult imageResult : content.getImages()) {
//            mImages.add(new NewsContentImage(imageResult));
//        }
        mFetchState = NewsContentFetchState.SUCCESS;
    }

    public static NewsContent createEmptyObject() {
        NewsContent newsContent = new NewsContent();
        newsContent.setFetchState(NewsContentFetchState.NOT_FETCHED_YET);
        return newsContent;
    }

    public static NewsContent createErrorObject() {
        NewsContent newsContent = new NewsContent();
        newsContent.setFetchState(NewsContentFetchState.ERROR);
        return newsContent;
    }

    public NewsContent(Parcel source) {
        this();
        setTitle(source.readString());
        setUrl(source.readString());
        setText(source.readString());
//        setImageUrl(source.readString());
        setVideoUrl(source.readString());
        setFetchState((NewsContentFetchState) source.readSerializable());
//        readTextListFromParcel(source);
//        readNewsContentImagesFromParcel(source);
    }

//    private void readTextListFromParcel(Parcel source) {
//        List<String> textList = new ArrayList<>();
//        source.readStringList(textList);
//        setTextList(textList);
//    }
//
//    private void readNewsContentImagesFromParcel(Parcel source) {
//        ArrayList<NewsContentImage> images = new ArrayList<>();
//        while (source.dataAvail() > 0) {
//            String imageUrl = source.readString();
//            int weight = source.readInt();
//            int width = source.readInt();
//            int height = source.readInt();
//            NewsContentImage image = new NewsContentImage(imageUrl, weight, width, height);
//            images.add(image);
//        }
//
//        setImages(images);
//    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getTitle());
        dest.writeString(getUrl());
        dest.writeString(getText());
//        dest.writeString(getImageUrl());
        dest.writeString(getVideoUrl());
        dest.writeSerializable(mFetchState);
//        dest.writeStringList(getTextList());
//        writeNewsContentImagesToParcel(dest);
    }

//    private void writeNewsContentImagesToParcel(Parcel dest) {
//        for (NewsContentImage image : getImages()) {
//            dest.writeString(image.getImageUrl());
//            dest.writeInt(image.getWeight());
//            dest.writeInt(image.getWidth());
//            dest.writeInt(image.getHeight());
//        }
//    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Parcelable.Creator<NewsContent> CREATOR = new Parcelable
            .Creator<NewsContent>() {
        public NewsContent createFromParcel(Parcel data) {
            return new NewsContent(data);
        }
        public NewsContent[] newArray(int size) {
            return new NewsContent[size];
        }
    };

//    private boolean isFetchSucceed() {
//        return mFetchState == NewsContentFetchState.SUCCESS;
//    }

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

//    public String getImageUrl() {
//        String imageUrl = mJResult.getImageUrl();
//        if (imageUrl == null) {
//            imageUrl = "";
//        }
//        return imageUrl;
//    }
//
//    public void setImageUrl(String imageUrl) {
//        mJResult.setImageUrl(imageUrl);
//    }

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

    public boolean hasText() {
        return getText().trim().length() > 0;
    }

//    public List<String> getTextList() {
//        List<String> textList = mJResult.getTextList();
//        if(textList == null) {
//            textList = new ArrayList<>();
//        }
//        return textList;
//    }
//
//    public void setTextList(List<String> textList) {
//        mJResult.setTextList(textList);
//    }

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

    public NewsContentFetchState getFetchState() {
        return mFetchState;
    }

    public void setFetchState(NewsContentFetchState fetchState) {
        mFetchState = fetchState;
    }

//    public List<NewsContentImage> getImages() {
//        List<NewsContentImage> images = mImages;
//        if (images == null) {
//            images = Collections.emptyList();
//        }
//        return images;
//    }

//    public void setImages(List<NewsContentImage> images) {
//        mImages = images;
//    }

    public static class Builder {
        private String title;
        private String url;
        private String text;
//        private String imageUrl;
        private String videoUrl;
        private NewsContentFetchState fetchState;
//        private List<String> texts;
//        private List<NewsContentImage> images;

        public Builder setTitle(String title) {
            this.title = title;

            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

//        public Builder setImageUrl(String imageUrl) {
//            this.imageUrl = imageUrl;
//            return this;
//        }

        public Builder setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public Builder setFetchState(NewsContentFetchState fetchState) {
            this.fetchState = fetchState;
            return this;
        }

//        public Builder setTexts(List<String> texts) {
//            this.texts = texts;
//            return this;
//        }

//        public Builder setImages(List<NewsContentImage> images) {
//            this.images = images;
//            return this;
//        }

        public NewsContent build() {
            return new NewsContent(title, url, text, videoUrl, fetchState);
        }
    }
}
