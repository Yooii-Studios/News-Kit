package com.yooiistudios.newskit.model.news;

import android.content.Context;

import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.news.NewsFeed;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 3. 4.
 *
 * NewsFeedFetchStateMessage
 *  NewsFeedFetchState 의 메세지를 메인 프로젝트에서 출력
 */
public class NewsFeedFetchStateMessage {
    private NewsFeedFetchStateMessage() {
        throw new AssertionError("You MUST NOT create this class!");
    }

    public static String getMessage(Context context, NewsFeed newsFeed) {
        switch (newsFeed.getNewsFeedFetchState()) {
            case ERROR_INVALID_URL:
                return newsFeed.hasTopicInfo()
                        ? context.getString(R.string.news_feed_fetch_error_invalid_topic_url)
                        : context.getString(R.string.news_feed_fetch_error_invalid_custom_url);
            case ERROR_TIMEOUT:
                return context.getString(R.string.news_feed_fetch_error_timeout);
            case ERROR_UNKNOWN:
                return context.getString(R.string.news_feed_fetch_error_unknown);
            case ERROR_NO_NEWS:
                return context.getString(R.string.news_feed_fetch_error_invalid_topic_url);
            case SUCCESS:
            case NOT_FETCHED_YET:
            default:
                return "";
        }
    }
}
