package com.yooiistudios.newsflow.core.news;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.yooiistudios.newsflow.core.news.newscontent.NewsContent;
import com.yooiistudios.newsflow.core.news.newscontent.NewsContentFetchState;
import com.yooiistudios.newsflow.core.util.NLLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * NLNews
 * Rss Feed의 <item> 하나를 표현하는 클래스
 */
public class News implements Comparable<News>, Parcelable {
    public static final String KEY_CURRENT_NEWS_INDEX = "KEY_CURRENT_NEWS_INDEX";

    private String mTitle;
    private String mLink;
    private Date mPubDate;
    private String mGuid;
    private String mDescription;
    private String mContent;
    private String mImageUrl;
    private boolean mImageUrlChecked;
    private String mOriginalDescription;
    private NewsContent mNewsContent = NewsContent.createEmptyObject();

    public News() {
        mImageUrlChecked = false;
    }

    public News(Parcel source) {
        this();
        mTitle = source.readString();
        mLink = source.readString();
        mPubDate = (Date) source.readSerializable();
        mGuid = source.readString();
        mDescription = source.readString();
        mContent = source.readString();
        mImageUrl = source.readString();
        mImageUrlChecked = source.readInt() == 1;
        mOriginalDescription = source.readString();
        mNewsContent = source.readParcelable(NewsContent.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mLink);
        dest.writeSerializable(mPubDate);
        dest.writeString(mGuid);
        dest.writeString(mDescription);
        dest.writeString(mContent);
        dest.writeString(mImageUrl);
        dest.writeInt(mImageUrlChecked ? 1 : 0); // 1 for true
        dest.writeString(mOriginalDescription);
        dest.writeParcelable(mNewsContent, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<News> CREATOR = new Parcelable
            .Creator<News>() {
        public News createFromParcel(Parcel data) {
            return new News(data);
        }
        public News[] newArray(int size) {
            return new News[size];
        }
    };


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String link) {
        this.mLink = link;
    }

    public Date getPubDate() {
        return mPubDate;
    }

    public void setPubDate(Date pubDate) {
        this.mPubDate = pubDate;
    }

    public void setPubDate(String pubDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            this.mPubDate = dateFormat.parse(pubDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getGuid() {
        return mGuid;
    }

    public void setGuid(String guid) {
        mGuid = guid;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public boolean hasDescription() {
        return mDescription != null && mDescription.trim().length() > 0;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    @Override
    public int compareTo(@NonNull News another) {
        if(getPubDate() != null && another.getPubDate() != null) {
            return getPubDate().compareTo(another.getPubDate());
        } else {
            return 0;
        }
    }

    public void setImageUrl(String url) {
        mImageUrl = url;
    }

    /**
     * 해당 뉴스를 대표하는 이미지의 url
     * @return First image in image list. May be null if there's no image.
     */
    public String getImageUrl() {
        return mImageUrl;
    }

    public boolean hasImageUrl() {
        return mImageUrl != null && mImageUrl.length() > 0;
    }

    public boolean isImageUrlChecked() {
        return mImageUrlChecked;
    }

    public void setImageUrlChecked(boolean checked) {
        mImageUrlChecked = checked;
    }

    public void setOriginalDescription(String originalDescription) {
        mOriginalDescription = originalDescription;
    }

    public NewsContent getNewsContent() {
        NewsContent newsContent = mNewsContent;
        if (newsContent == null) {
            newsContent = NewsContent.createEmptyObject();
        }
        return newsContent;
    }

    public void setNewsContent(NewsContent newsContent) {
        mNewsContent = newsContent;
    }

    public boolean hasNewsContent() {
        return !getNewsContent().getFetchState().equals(NewsContentFetchState.NOT_FETCHED_YET);
    }

    public boolean hasDisplayableDescription() {
        return hasDescription() || getNewsContent().hasText();
    }

    public String getDisplayableRssDescription() {
        return getDisplayableDescription(true);
    }

    public String getDisplayableNewsContentDescription() {
        return getDisplayableDescription(false);
    }

    private String getDisplayableDescription(boolean preferRss) {
        final int threshold = 150;

        String text;

        if (!hasDisplayableDescription()) {
            NLLog.now("Both does not exists.");
            text = "";
        } else {
            // TODO 둘 중 하나는 텍스트 있음
            if (hasDescription() && !getNewsContent().hasText()) {
                // TODO RSS 에만 텍스트 있음
                NLLog.now("RSS ONLY");
                text = getDescription();
            } else if (!hasDescription() && getNewsContent().hasText()) {
                // TODO NewsContent 에만 텍스트 있음
                NLLog.now("NewsContentText ONLY");
                text = getNewsContent().getText();
            } else {
                // TODO 둘 다 있음.
                NLLog.now("Both exists.");
                String rssText = getDescription();
                String newsContentText = getNewsContent().getText();
                int rssTextLength = rssText.length();
                int newsContentTextLength = newsContentText.length();
                NLLog.now("RSS length: " + rssTextLength + ", content length: " + newsContentTextLength);
                if (rssTextLength > threshold && newsContentTextLength > threshold) {
                    NLLog.now("Both sufficient. prefer " + (preferRss ? "RSS" : "NewsContent"));
                    // 1. RSS > Threshold && NewsContent > Threshold => 각 화면에 맞는 텍스트
                    return preferRss ? rssText : newsContentText;
                } else {
                    NLLog.now("Does not meet threshold condition. Pic longer one.");
                    // 2. RSS > NewsContent ? RSS : NewsContent
                    text = rssTextLength > newsContentTextLength ? rssText : newsContentText;
                }
            }
        }

        return text;
    }
}
