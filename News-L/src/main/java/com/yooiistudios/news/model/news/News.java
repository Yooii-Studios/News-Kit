package com.yooiistudios.news.model.news;

import android.os.Parcel;
import android.os.Parcelable;

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
    private String mDescription;
    private String mContent;
    private String mImageUrl;
    private boolean mImageUrlChecked;
    private String mOriginalDescription;

    public News() {
        mImageUrlChecked = false;
    }

    public News(Parcel source) {
        this();
        mTitle = source.readString();
        mLink = source.readString();
        mPubDate = (Date) source.readSerializable();
        mDescription = source.readString();
        mContent = source.readString();
        mImageUrl = source.readString();
        mImageUrlChecked = source.readInt() == 1;
        mOriginalDescription = source.readString();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mLink);
        dest.writeSerializable(mPubDate);
        dest.writeString(mDescription);
        dest.writeString(mContent);
        dest.writeString(mImageUrl);
        dest.writeInt(mImageUrlChecked ? 1 : 0); // 1 for true
        dest.writeString(mOriginalDescription);
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

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    @Override
    public int compareTo(News another) {
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

    public boolean isImageUrlChecked() {
        return mImageUrlChecked;
    }
    public void setImageUrlChecked(boolean checked) {
        mImageUrlChecked = checked;
    }

    public String getOriginalDescription() {
        return mOriginalDescription;
    }

    public void setOriginalDescription(String originalDescription) {
        mOriginalDescription = originalDescription;
    }
}
