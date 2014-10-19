package com.yooiistudios.news.model.news;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * Rss Feed의 Feed를 표현하는 클래스
 */
public class NewsFeed implements Parcelable {
    public static final String KEY_NEWS_FEED = "KEY_NEWS_FEED";

    private String mTitle;
    private NewsFeedUrl mNewsFeedUrl;
    private String mLink;
    private String mDescription;
    private String mLanguage;
    private ArrayList<News> mNewsList;
    private boolean mIsValid;
    private int mDisplayingNewsIndex;

    public NewsFeed() {
        mNewsList = new ArrayList<News>();
        mIsValid = false;
        mDisplayingNewsIndex = 0;
    }

    public NewsFeed(Parcel source) {
        this();
        mTitle = source.readString();
        mNewsFeedUrl = (NewsFeedUrl)source.readSerializable();
        mLink = source.readString();
        mDescription = source.readString();
        mLanguage = source.readString();
        source.readTypedList(mNewsList, News.CREATOR);
        mIsValid = source.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeSerializable(mNewsFeedUrl);
        dest.writeString(mLink);
        dest.writeString(mDescription);
        dest.writeString(mLanguage);
        dest.writeTypedList(mNewsList);
        dest.writeInt(mIsValid ? 1 : 0);
    }

    public static final Parcelable.Creator<NewsFeed> CREATOR = new Parcelable.Creator<NewsFeed>() {
        public NewsFeed createFromParcel(Parcel data) {
            return new NewsFeed(data);
        }
        public NewsFeed[] newArray(int size) {
            return new NewsFeed[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    public String getTitle() {
        return mTitle;
    }
    public void setTitle(String title) {
        this.mTitle = title;
    }

    public NewsFeedUrl getNewsFeedUrl() {
        return mNewsFeedUrl;
    }
    public void setNewsFeedUrl(NewsFeedUrl newsFeedUrl) {
        mNewsFeedUrl = newsFeedUrl;
    }


    public String getLink() {
        return mLink;
    }
    public void setLink(String link) {
        this.mLink = link;
    }

    public String getDescription() {
        return mDescription;
    }
    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getLanguage() {
        return mLanguage;
    }
    public void setLanguage(String language) {
        this.mLanguage = language;
    }

    public void setNewsList(ArrayList<News> newsList) {
        mNewsList = newsList;
    }

    public ArrayList<News> getNewsList() {
        return mNewsList;
    }

    public void setValid(boolean isValid) {
        mIsValid = isValid;
    }

    public boolean isValid() {
        return mIsValid;
    }

    public void addNews(News news) {
        mNewsList.add(news);
    }

    public void addNewsAt(int idx, News news) {
        mNewsList.add(idx, news);
    }

    public void removeNewsAt(int idx) {
        mNewsList.remove(idx);
    }

    public int getDisplayingNewsIndex() {
        return mDisplayingNewsIndex;
    }
    public void setDisplayingNewsIndex(int index) {
        this.mDisplayingNewsIndex = index;
    }

    public int getNextNewsIndex() {
        int displayingNewsIndex = getDisplayingNewsIndex();
        if (displayingNewsIndex < getNewsList().size() - 1) {
            displayingNewsIndex += 1;
        } else {
            displayingNewsIndex = 0;
        }

        return displayingNewsIndex;
    }

    /**
     * 이미지 url을 포함하고 있는 뉴스만 반환한다.
     * @return ArrayList of NLNews which has image url.
     */
    public ArrayList<News> getNewsListContainsImageUrl() {
        ArrayList<News> containingList = new ArrayList<News>();

        for (News news : mNewsList) {
            if (news.getImageUrl() != null) {
                containingList.add(news);
            }
        }

        return containingList;
    }

}
