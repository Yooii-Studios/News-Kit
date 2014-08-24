package com.yooiistudios.news.model;

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
public class NLNews implements Comparable<NLNews>, Parcelable {
    public static final String KEY_NEWS = "KEY_NEWS";

    private String title;
    private String link;
    private Date pubDate;
    private String description;
    private String content;
    private String imageUrl;

    public NLNews() {

    }

    public NLNews(Parcel source) {
        this();
        title = source.readString();
        link = source.readString();
        pubDate = (Date) source.readSerializable();
        description = source.readString();
        content = source.readString();
        imageUrl = source.readString();
//        source.readStringList(mImageUrlList);

    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(link);
        dest.writeSerializable(pubDate);
        dest.writeString(description);
        dest.writeString(content);
        dest.writeString(imageUrl);
//        dest.writeStringList(mImageUrlList);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Parcelable.Creator<NLNews> CREATOR = new Parcelable
            .Creator<NLNews>() {
        public NLNews createFromParcel(Parcel data) {
            return new NLNews(data);
        }
        public NLNews[] newArray(int size) {
            return new NLNews[size];
        }
    };


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public void setPubDate(String pubDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            this.pubDate = dateFormat.parse(pubDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int compareTo(NLNews another) {
        if(getPubDate() != null && another.getPubDate() != null) {
            return getPubDate().compareTo(another.getPubDate());
        } else {
            return 0;
        }
    }

    public void setImageUrl(String url) {
        imageUrl = url;
    }

    /**
     * 해당 뉴스를 대표하는 이미지의 url
     * @return First image in image list. May be null if there's no image.
     */
    public String getImageUrl() {
        return imageUrl;
    }
}
