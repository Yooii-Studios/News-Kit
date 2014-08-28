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

    private String title;
    private String link;
    private String description;
    private String language;
    private ArrayList<News> mNewsList;

    public NewsFeed() {
        mNewsList = new ArrayList<News>();
    }

    public NewsFeed(Parcel source) {
        this();
        title = source.readString();
        link = source.readString();
        description = source.readString();
        language = source.readString();
        source.readTypedList(mNewsList, News.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(link);
        dest.writeString(description);
        dest.writeString(language);
        dest.writeTypedList(mNewsList);
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

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public void setNewsList(ArrayList<News> newsList) {
        mNewsList = newsList;
    }

    public ArrayList<News> getNewsList() {
        return mNewsList;
    }


    public void addNews(News news) {
        mNewsList.add(news);
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
