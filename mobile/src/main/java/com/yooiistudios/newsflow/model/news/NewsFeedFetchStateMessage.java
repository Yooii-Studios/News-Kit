package com.yooiistudios.newsflow.model.news;

import android.content.Context;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.news.NewsFeed;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 4.
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
            case NOT_FETCHED_YET:
            case SUCCESS:
            default:
                return "";
            case ERROR_INVALID_URL:
                return newsFeed.hasTopicInfo()
                        ? context.getString(R.string.news_feed_fetch_error_invalid_topic_url)
                        : context.getString(R.string.news_feed_fetch_error_invalid_custom_url);
            case ERROR_TIMEOUT:
                return context.getString(R.string.news_feed_fetch_error_timeout);
            case ERROR_UNKNOWN:
                return context.getString(R.string.news_feed_fetch_error_unknown);
        }
    }
}
