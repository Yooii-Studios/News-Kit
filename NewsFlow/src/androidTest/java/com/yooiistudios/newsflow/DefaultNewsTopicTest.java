package com.yooiistudios.newsflow;

import android.test.AndroidTestCase;

import com.yooiistudios.newsflow.model.news.NewsContentProvider;
import com.yooiistudios.newsflow.model.news.NewsFeedDefaultUrlProvider;
import com.yooiistudios.newsflow.model.news.NewsTopic;

import java.util.ArrayList;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 24.
 *
 * DefaultNewsProviderTest
 *  첫 실행시 기본으로 설정되는 뉴스들을 테스트. 빈 값이(에러날 경우의 기본값)이 나와서는 안 된다.
 */
public class DefaultNewsTopicTest extends AndroidTestCase {
    private NewsFeedDefaultUrlProvider newsFeedDefaultUrlProvider;
    private NewsTopic defaultNewsTopicWhenNotFound;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        newsFeedDefaultUrlProvider = NewsFeedDefaultUrlProvider.getInstance(getContext());

        // en-US의 첫번째 프로바이더의 첫번째 토픽이 검색이 안될 경우 기본 프로바이더(이후 수정될 가능성 있음)
        defaultNewsTopicWhenNotFound = NewsContentProvider.getInstance(getContext())
                .getNewsProvider("en", null, "us", 1).getNewsTopicList().get(0);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testValidateDefaultTopDefaultNewsTopic() {
        NewsTopic topNewsTopic = newsFeedDefaultUrlProvider.getTopNewsTopic();
        NewsTopic targetTopic = defaultNewsTopicWhenNotFound;

        boolean isSameTopic = false;
        if (topNewsTopic.languageCode.equalsIgnoreCase(targetTopic.languageCode) &&
                topNewsTopic.regionCode.equalsIgnoreCase(targetTopic.regionCode) &&
                topNewsTopic.countryCode.equalsIgnoreCase(targetTopic.countryCode) &&
                topNewsTopic.newsProviderId == targetTopic.newsProviderId) {
            isSameTopic = true;
        }
        assertFalse(isSameTopic);
    }

    public void testValidateDefaultBottomNewsTopics() {
        ArrayList<NewsTopic> bottomNewsTopics = newsFeedDefaultUrlProvider.getBottomNewsTopicList();
        NewsTopic targetTopic = defaultNewsTopicWhenNotFound;

        for (NewsTopic bottomNewsTopic : bottomNewsTopics) {
            boolean isSameTopic = false;
            if (bottomNewsTopic.languageCode.equalsIgnoreCase(targetTopic.languageCode) &&
                    bottomNewsTopic.regionCode.equalsIgnoreCase(targetTopic.regionCode) &&
                    bottomNewsTopic.countryCode.equalsIgnoreCase(targetTopic.countryCode) &&
                    bottomNewsTopic.newsProviderId == targetTopic.newsProviderId) {
                isSameTopic = true;
            }
            assertFalse(isSameTopic);
        }
    }
}
