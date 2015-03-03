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

    public void testDefaultNewsTopicWhenNotFound() {
        // 테스트로 영어의 첫째 프로바이더의 첫째 토픽을 만들어주게 해두었는데, 이 기획이 변하지 않음을 테스트
        // 나중에 이는 바뀔 수도 있기 때문
        NewsTopic defaultNewsTopicWhenErrorHappened = NewsContentProvider.getInstance(getContext())
                .makeDefaultNewsProvider().getNewsTopicList().get(0);
        assertSame(defaultNewsTopicWhenErrorHappened, defaultNewsTopicWhenNotFound);
    }

    public void testValidateDefaultTopDefaultNewsTopic() {
        NewsTopic topNewsTopic = newsFeedDefaultUrlProvider.getTopNewsTopic();
        NewsTopic defaultErrorTopic = defaultNewsTopicWhenNotFound;
        assertFalse(isSameTopic(defaultErrorTopic, topNewsTopic));
    }

    public void testValidateDefaultBottomNewsTopics() {
        ArrayList<NewsTopic> bottomNewsTopics = newsFeedDefaultUrlProvider.getBottomNewsTopicList();
        NewsTopic defaultErrorTopic = defaultNewsTopicWhenNotFound;

        for (NewsTopic bottomNewsTopic : bottomNewsTopics) {
            assertFalse(isSameTopic(defaultErrorTopic, bottomNewsTopic));
        }
    }

    private boolean isSameTopic(NewsTopic expectedNewsTopic, NewsTopic actualNewsTopic) {
        if (actualNewsTopic.languageCode.equalsIgnoreCase(expectedNewsTopic.languageCode)) {
            if (actualNewsTopic.regionCode == null ||
                    actualNewsTopic.regionCode.equalsIgnoreCase(expectedNewsTopic.regionCode)) {
                if (actualNewsTopic.countryCode.equalsIgnoreCase(expectedNewsTopic.countryCode) &&
                        actualNewsTopic.newsProviderId == expectedNewsTopic.newsProviderId) {
                    return true;
                }
            }
        }
        return false;
    }
}
