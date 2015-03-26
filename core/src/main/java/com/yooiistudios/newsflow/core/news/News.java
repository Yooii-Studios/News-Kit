package com.yooiistudios.newsflow.core.news;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.yooiistudios.newsflow.core.R;
import com.yooiistudios.newsflow.core.news.newscontent.NewsContent;
import com.yooiistudios.newsflow.core.news.newscontent.NewsContentFetchState;
import com.yooiistudios.newsflow.core.util.Device;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * NLNews
 * Rss Feed의 <item> 하나를 표현하는 클래스
 */
public class News implements Comparable<News>, Parcelable {
    public static final String KEY_CURRENT_NEWS_INDEX = "KEY_CURRENT_NEWS_INDEX";
    public static final long INVALID_LONG = -1;

    private String mTitle;
    private String mLink;
    private long mPubDateInMillis = INVALID_LONG;
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
        mPubDateInMillis = source.readLong();
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
        dest.writeLong(mPubDateInMillis);
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

    public long getPubDateInMillis() {
        return mPubDateInMillis;
    }

    private long getElapsedTimeInMillisSincePubDate() {
        long elapsedTime;
        if (mPubDateInMillis == INVALID_LONG) {
            elapsedTime = INVALID_LONG;
        } else {
            DateTime curDateTime = new DateTime(DateTimeZone.UTC);

            elapsedTime = curDateTime.getMillis() - mPubDateInMillis;
        }

        return elapsedTime;
    }

    public String getDisplayableElapsedTimeSincePubDate(Context context) {
        long elapsedTime = getElapsedTimeInMillisSincePubDate();
        String message;
        if (elapsedTime == INVALID_LONG) {
            message = "";
        } else {
            int inSeconds = (int) (elapsedTime / 1000);
            int inMinutes = inSeconds / 60;
            int inHours = inMinutes / 60;
            int inDays = inHours / 24;
            int inWeeks = inDays / 7;

//        int seconds = inSeconds % 60;
            int minutes = inMinutes % 60;
            int hours = inHours % 24;
            int days = inDays % 7;
            int weeks = inWeeks % 4;

            boolean overMonths = inWeeks > 4;
            boolean overWeeks = inDays > 7;
            boolean overDays = inHours > 24;
            boolean overHours = inMinutes > 60;

            if (overMonths) {
                message = context.getResources().getString(R.string.pub_date_months_ago);
            } else if (overWeeks) {
                message = context.getResources().getString(R.string.pub_date_week_ago);
            } else if (overDays) {
                if (days > 1) {
                    message = context.getResources().getString(R.string.pub_date_days_ago, days);
                } else {
                    message = context.getResources().getString(R.string.pub_date_day_ago);
                }
            } else if (overHours) {
                if (hours > 1) {
                    message = context.getResources().getString(R.string.pub_date_hours_ago, hours);
                } else {
                    message = context.getResources().getString(R.string.pub_date_hour_ago);
                }
            } else {
                if (minutes > 1) {
                    message = context.getResources().getString(R.string.pub_date_minutes_ago, minutes);
                } else {
                    message = context.getResources().getString(R.string.pub_date_minute_ago);
                }
            }
        }

        return message;
    }

    public void setPubDate(String pubDate) {
        // "EEE MMM dd HH:mm:ss zZZ yyyy"
        DateTime targetDateTime = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss z")
                .withLocale(Locale.US)
                .withZoneUTC()
                .parseDateTime(pubDate);
        mPubDateInMillis = targetDateTime.getMillis();
    }

    public void setPubDate(long pubDateInMillis) {
        mPubDateInMillis = pubDateInMillis;
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
        if(getPubDateInMillis() != INVALID_LONG && another.getPubDateInMillis() != INVALID_LONG) {
            long pubDateInMillis = getPubDateInMillis();
            long targetPubDateInMillis = another.getPubDateInMillis();
            if (Device.hasJellyBean()) {
                return Long.compare(pubDateInMillis, targetPubDateInMillis);
            } else {
                return pubDateInMillis < targetPubDateInMillis
                        ? -1
                        : (pubDateInMillis == targetPubDateInMillis ? 0 : 1);
            }
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
        return mImageUrl != null/* && mImageUrl.length() > 0*/;
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
            text = "";
        } else {
            if (hasDescription() && !getNewsContent().hasText()) {
                text = getDescription();
            } else if (!hasDescription() && getNewsContent().hasText()) {
                text = getNewsContent().getText();
            } else {
                String rssText = getDescription();
                String newsContentText = getNewsContent().getText();
                int rssTextLength = rssText.length();
                int newsContentTextLength = newsContentText.length();
                if (rssTextLength > threshold && newsContentTextLength > threshold) {
                    return preferRss ? rssText : newsContentText;
                } else {
                    text = rssTextLength > newsContentTextLength ? rssText : newsContentText;
                }
            }
        }

        return text;
    }
}
