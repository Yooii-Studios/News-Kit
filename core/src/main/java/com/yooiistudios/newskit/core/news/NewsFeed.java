package com.yooiistudios.newskit.core.news;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 16.
 *
 * Rss Feed 의 Feed 를 표현하는 클래스
 */
public class NewsFeed implements Parcelable, RssFetchable {
    public static final String KEY_NEWS_FEED = "KEY_NEWS_FEED";
    public static final int INDEX_TOP = -1;
    public static final int INDEX_BOTTOM_START = 0;

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
    private String mTopicCountryCode = null;
    private int mTopicProviderId = -1;
    private int mTopicId = -1;

    public NewsFeed() {
        init();
    }

    private void init() {
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
        mTopicCountryCode = source.readString();
        mTopicProviderId = source.readInt();
        mTopicId = source.readInt();
    }

    public NewsFeed(RssFetchable fetchable) {
        this();
        if (fetchable instanceof NewsFeedUrl) {
            mNewsFeedUrl = (NewsFeedUrl)fetchable;
        } else if (fetchable instanceof NewsTopic) {
            NewsTopic newsTopic = (NewsTopic)fetchable;
            mTitle = newsTopic.title;
            mNewsFeedUrl = newsTopic.newsFeedUrl;

            mTopicLanguageCode = newsTopic.languageCode;
            mTopicRegionCode = newsTopic.regionCode;
            mTopicCountryCode = newsTopic.countryCode;
            mTopicProviderId = newsTopic.newsProviderId;
            mTopicId = newsTopic.id;
        } else if (fetchable instanceof NewsFeed) {
            NewsFeed newsFeed = (NewsFeed)fetchable;
            mTitle = newsFeed.mTitle;
            mNewsFeedUrl = newsFeed.mNewsFeedUrl;

            mTopicLanguageCode = newsFeed.getTopicLanguageCode();
            mTopicRegionCode = newsFeed.mTopicRegionCode;
            mTopicCountryCode = newsFeed.mTopicCountryCode;
            mTopicProviderId = newsFeed.mTopicProviderId;
            mTopicId = newsFeed.mTopicId;
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
        dest.writeString(mTopicCountryCode);
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

    @Override
    public NewsFeedUrl getNewsFeedUrl() {
        return mNewsFeedUrl;
    }

    public void setNewsFeedUrl(NewsFeedUrl newsFeedUrl) {
        mNewsFeedUrl = newsFeedUrl;
    }

    public boolean isCustomRss() {
        return getNewsFeedUrl().getType().equals(NewsFeedUrlType.CUSTOM);
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

    public News getNewsByGuid(String guid) {
        for (News news : mNewsList) {
            if (news.getGuid().equals(guid)) {
                return news;
            }
        }

        return null;
    }

    /**
     * 뉴스피드가 화면에 보여질 수 있는 상태인지 체크한다.
     * @return 뉴스피드 fetch 에 성공했으며 가지고 뉴스가 존재할 경우
     */
    public boolean isDisplayable() {
        return getNewsFeedFetchState().equals(NewsFeedFetchState.SUCCESS) && containsNews();
    }

    public boolean containsNews() {
        return getNewsList() != null && getNewsList().size() > 0;
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

    public News getDisplayingNews() {
        return mNewsList.get(mDisplayingNewsIndex);
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

    public String getTopicCountryCode() {
        return mTopicCountryCode;
    }

    public void setTopicCountryCode(String topicCountryCode) {
        mTopicCountryCode = topicCountryCode;
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

    public boolean isNotFetchedYet() {
        return mNewsFeedFetchState.equals(NewsFeedFetchState.NOT_FETCHED_YET);
    }

    public News getNextNews() {
        return mNewsList.get(getNextNewsIndex());
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

    public void increaseDisplayingNewsIndex() {
        setDisplayingNewsIndex(getNextNewsIndex());
    }

    public void setTopicIdInfo(NewsTopic newsTopic) {
        setTopicLanguageCode(newsTopic.languageCode);
        setTopicRegionCode(newsTopic.regionCode);
        setTopicCountryCode(newsTopic.countryCode);
        setTopicProviderId(newsTopic.newsProviderId);
        setTopicId(newsTopic.id);
    }

    public void setTopicIdInfo(NewsFeed newsFeed) {
        setTopicLanguageCode(newsFeed.getTopicLanguageCode());
        setTopicRegionCode(newsFeed.getTopicRegionCode());
        setTopicCountryCode(newsFeed.getTopicCountryCode());
        setTopicProviderId(newsFeed.getTopicProviderId());
        setTopicId(newsFeed.getTopicId());
    }

    public boolean hasTopicInfo() {
        return getTopicLanguageCode() != null
                && getTopicProviderId() >= 0
                && getTopicId() >= 0;
    }

    public void clearFetchedInfo() {
        init();
        setTitle(null);
        setLink(null);
        setDescription(null);
        setLanguage(null);
    }

//    public NewsTopic createNewsTopicInfo() {
//        NewsTopic topic = new NewsTopic();
//        topic.languageCode = getTopicLanguageCode();
//        topic.regionCode = getTopicRegionCode();
//        topic.countryCode = getTopicCountryCode();
//        topic.newsProviderId = getTopicProviderId();
//        topic.id = getTopicId();
//
//        return topic;
//    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Title: ")
                .append(getTitle())
                .append("\nDescription: ")
                .append(getDescription());

        for (int i = 0 ; i < getNewsList().size(); i++) {
            News news = getNewsList().get(i);
            builder.append("\nNews At: ")
                    .append(i)
                    .append("\nTitle: ")
                    .append(news.getTitle())
                    .append("\nDescription: ")
                    .append(news.getDescription());
        }

        return builder.toString();
    }
}
