package com.yooiistudios.newsflow.core.news.database;

import android.provider.BaseColumns;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 15. 1. 4.
 *
 * NewsDbContract
 *  뉴스 디비에 사용될 테이블의 메타데이터
 */
public class NewsDbContract {
    private NewsDbContract() {
        throw new AssertionError("You SHOULD NOT create an instance of this class!!");
    }

    public static abstract class NewsFeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "news_feeds";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_FEED_URL = "news_feed_url";
        public static final String COLUMN_NAME_FEED_URL_TYPE_KEY = "news_feed_url_type_key";
        public static final String COLUMN_NAME_LINK = "link";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
//        public static final String COLUMN_NAME_IS_VALID = "is_valid";

        public static final String COLUMN_NAME_TOPIC_REGION_CODE = "topic_region_code";
        public static final String COLUMN_NAME_TOPIC_LANGUAGE_CODE = "topic_language_code";
        public static final String COLUMN_NAME_TOPIC_COUNTRY_CODE = "topic_country_code";
        public static final String COLUMN_NAME_TOPIC_PROVIDER_ID = "topic_provider_id";
        public static final String COLUMN_NAME_TOPIC_ID = "topic_id";
    }

    public static abstract class NewsEntry implements BaseColumns {
        public static final String TABLE_NAME = "news_list";
        public static final String COLUMN_NAME_FEED_POSITION = "news_feed_position";
        public static final String COLUMN_NAME_INDEX = "news_index";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LINK = "link";
        public static final String COLUMN_NAME_GUID = "guid";
//        public static final String COLUMN_NAME_PUB_DATE = "pub_date";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_IMAGE_URL = "image_url";
        public static final String COLUMN_NAME_IMAGE_URL_CHECKED = "image_url_checked";
    }

    public static abstract class NewsContentEntry implements BaseColumns {
        public static final String TABLE_NAME = "news_content_list";
        public static final String COLUMN_NAME_GUID = "guid";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TEXT = "text";
//        public static final String COLUMN_NAME_IMAGE_URL = "image_url";
        public static final String COLUMN_NAME_VIDEO_URL = "video_url";
        public static final String COLUMN_NAME_FETCH_STATE = "fetch_state";
    }

//    public static abstract class NewsContentTextEntry implements BaseColumns {
//        public static final String TABLE_NAME = "news_content_text";
//        public static final String COLUMN_NAME_INDEX = "position";
//        public static final String COLUMN_NAME_GUID = "guid";
//        public static final String COLUMN_NAME_TEXT = "text";
//    }

//    public static abstract class NewsContentImageEntry implements BaseColumns {
//        public static final String TABLE_NAME = "news_content_image";
//        public static final String COLUMN_NAME_INDEX = "position";
//        public static final String COLUMN_NAME_GUID = "guid";
//        public static final String COLUMN_NAME_IMAGE_URL = "image_url";
//        public static final String COLUMN_NAME_WEIGHT = "weight";
//        public static final String COLUMN_NAME_WIDTH = "width";
//        public static final String COLUMN_NAME_HEIGHT = "height";
//    }
}
