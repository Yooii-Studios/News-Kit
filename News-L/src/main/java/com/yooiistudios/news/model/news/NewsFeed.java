package com.yooiistudios.news.model.news;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.yooiistudios.news.R;
import com.yooiistudios.news.model.RssFetchable;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * Rss Feed 의 Feed 를 표현하는 클래스
 */
public class NewsFeed implements Parcelable {
    public static final String KEY_NEWS_FEED = "KEY_NEWS_FEED";

    private String mTitle;
    private NewsFeedUrl mNewsFeedUrl;
    private String mLink;
    private String mDescription;
    private String mLanguage;
    private ArrayList<News> mNewsList;
    private NewsFeedFetchState mNewsFeedFetchState;
    private int mDisplayingNewsIndex;

    // topic id
    private String mTopicLanguageCode = null;
    private String mTopicRegionCode = null;
    private int mTopicProviderId = -1;
    private int mTopicId = -1;

    public NewsFeed() {
        mNewsList = new ArrayList<>();
        mDisplayingNewsIndex = 0;
        mNewsFeedFetchState = NewsFeedFetchState.NOT_FETCHED_YET;
    }

    public NewsFeed(Parcel source) {
        this();
        mTitle = source.readString();
        mNewsFeedUrl = (NewsFeedUrl)source.readSerializable();
        mLink = source.readString();
        mDescription = source.readString();
        mLanguage = source.readString();
        source.readTypedList(mNewsList, News.CREATOR);

        mTopicLanguageCode = source.readString();
        mTopicRegionCode = source.readString();
        mTopicProviderId = source.readInt();
        mTopicId = source.readInt();
    }

    public NewsFeed(RssFetchable fetchable) {
        this();
        if (fetchable instanceof NewsFeedUrl) {
            mNewsFeedUrl = (NewsFeedUrl)fetchable;
        } else if (fetchable instanceof NewsTopic) {
            NewsTopic newsTopic = (NewsTopic)fetchable;
            mTitle = newsTopic.getTitle();
            mNewsFeedUrl = newsTopic.getNewsFeedUrl();

            mTopicLanguageCode = newsTopic.getLanguageCode();
            mTopicRegionCode = newsTopic.getRegionCode();
            mTopicProviderId = newsTopic.getNewsProviderId();
            mTopicId = newsTopic.getId();
        } else {
            throw new IllegalArgumentException("Unsupported RssFetchable.");
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeSerializable(mNewsFeedUrl);
        dest.writeString(mLink);
        dest.writeString(mDescription);
        dest.writeString(mLanguage);
        dest.writeTypedList(mNewsList);
        dest.writeString(mTopicLanguageCode);
        dest.writeString(mTopicRegionCode);
        dest.writeInt(mTopicProviderId);
        dest.writeInt(mTopicId);
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

    public boolean containsNews() {
        return getNewsList().size() > 0;
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

    // Topic ids
    public String getTopicLanguageCode() {
        return mTopicLanguageCode;
    }

    public void setTopicLanguageCode(String topicLanguageCode) {
        mTopicLanguageCode = topicLanguageCode;
    }

    public String getTopicRegionCode() {
        return mTopicRegionCode;
    }

    public void setTopicRegionCode(String topicRegionCode) {
        mTopicRegionCode = topicRegionCode;
    }

    public int getTopicProviderId() {
        return mTopicProviderId;
    }

    public void setTopicProviderId(int topicProviderId) {
        mTopicProviderId = topicProviderId;
    }

    public int getTopicId() {
        return mTopicId;
    }

    public void setTopicId(int topicId) {
        mTopicId = topicId;
    }

    public void setNewsFeedFetchState(NewsFeedFetchState newsFeedFetchState) {
        mNewsFeedFetchState = newsFeedFetchState;
    }

    public NewsFeedFetchState getNewsFeedFetchState() {
        return mNewsFeedFetchState;
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

    public void setTopicIdInfo(NewsTopic newsTopic) {
        setTopicLanguageCode(newsTopic.getLanguageCode());
        setTopicRegionCode(newsTopic.getRegionCode());
        setTopicProviderId(newsTopic.getNewsProviderId());
        setTopicId(newsTopic.getId());
    }

    public void setTopicIdInfo(NewsFeed newsFeed) {
        setTopicLanguageCode(newsFeed.getTopicLanguageCode());
        setTopicRegionCode(newsFeed.getTopicRegionCode());
        setTopicProviderId(newsFeed.getTopicProviderId());
        setTopicId(newsFeed.getTopicId());
    }

    private boolean hasTopicInfo() {
        return getTopicLanguageCode() != null
                && getTopicProviderId() >= 0
                && getTopicId() >= 0;
    }

    public String getFetchStateMessage(Context context) {
        switch (mNewsFeedFetchState) {
            case NOT_FETCHED_YET:
            case SUCCESS:
            default:
                return "";
            case ERROR_INVALID_URL:
                return hasTopicInfo()
                        ? context.getString(R.string.news_feed_fetch_error_invalid_topic_url)
                        : context.getString(R.string.news_feed_fetch_error_invalid_custom_url);
            case ERROR_TIMEOUT:
                return context.getString(R.string.news_feed_fetch_error_timeout);
            case ERROR_UNKNOWN:
                return context.getString(R.string.news_feed_fetch_error_unknown);
        }
    }

}
