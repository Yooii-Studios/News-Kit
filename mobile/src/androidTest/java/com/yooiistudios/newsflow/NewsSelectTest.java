package com.yooiistudios.newsflow;

import android.test.AndroidTestCase;

import com.yooiistudios.newsflow.model.language.Language;
import com.yooiistudios.newsflow.model.language.LanguageUtils;
import com.yooiistudios.newsflow.model.news.NewsContentProvider;
import com.yooiistudios.newsflow.model.news.NewsProviderLanguage;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 2. 26.
 *
 * NewsSelectTest
 *  뉴스 선택과 관련된 테스트. 현재 언어를 설정하고 언어에 따라 표시할 언어를 정렬하는 것을 테스트
 */
public class NewsSelectTest extends AndroidTestCase {
    NewsContentProvider mNewsContentProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mNewsContentProvider = NewsContentProvider.getInstance(getContext());
    }

    public void testEnglishSorting() {
        sortWithLanguage(Language.ENGLISH);
        assertLanguageWithIndex(Language.ENGLISH, 0);
    }

    // TODO: 나중에 간체 신문 RSS 가 추가되면 나중에 추가 테스트 로직을 구현할 것
    public void testSimplifiedChineseSorting() {

    }

    public void testTraditionalChineseSorting() {
        Language targetLanguage = Language.TRADITIONAL_CHINESE;
        sortWithLanguage(targetLanguage);
        NewsProviderLanguage newsProviderLanguage = mNewsContentProvider.getNewsLanguage(0);

        String regionalLanguageName = newsProviderLanguage.regionalLanguageName;
        assertEquals(getContext().getString(targetLanguage.getLocalNotationStringId()),
                regionalLanguageName);
        assertEquals(mNewsContentProvider.getNewsLanguageTitle(0), regionalLanguageName);
    }

    public void testKoreanSorting() {
        sortWithLanguage(Language.KOREAN);
        assertLanguageWithIndex(Language.KOREAN, 0);
        assertLanguageWithIndex(Language.ENGLISH, 1);
        assertLanguageWithIndex(Language.JAPANESE, 2);
    }

    public void testJapaneseSorting() {
        Language targetLanguage = Language.JAPANESE;
        sortWithLanguage(targetLanguage);

        assertLanguageWithIndex(Language.JAPANESE, 0);
        assertLanguageWithIndex(Language.ENGLISH, 1);
        assertLanguageWithIndex(Language.KOREAN, 2);
    }

    private void sortWithLanguage(Language language) {
        LanguageUtils.setLanguageType(language, getContext());
        NewsContentProvider.getInstance(getContext()).sortNewsProviderLanguage(getContext());
    }

    private void assertLanguageWithIndex(Language language, int index) {
        NewsProviderLanguage newsProviderLanguage = mNewsContentProvider.getNewsLanguage(index);

        // test newsProviderLanguage
        String regionalLanguageName = newsProviderLanguage.regionalLanguageName;
        assertEquals(getContext().getString(language.getLocalNotationStringId()),
                regionalLanguageName);

        // test newsProviderLanguage title
        assertEquals(mNewsContentProvider.getNewsLanguageTitle(index), regionalLanguageName);
    }
}
