package com.yooiistudios.newsflow;

import android.test.AndroidTestCase;

import com.yooiistudios.newsflow.core.language.DefaultLocale;
import com.yooiistudios.newsflow.core.news.curation.NewsContentProvider;
import com.yooiistudios.newsflow.core.news.curation.NewsProviderLanguage;

import java.util.Locale;

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
        sortWithLanguage("en", "US");
        assertLanguageWithIndex("en", 0);
    }

    public void testSpanishSorting() {
        sortWithLanguage("es", "ES");
        assertLanguageWithIndex("es", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("pt", 2);
        assertLanguageWithIndex("fr", 3);
        assertLanguageWithIndex("it", 4);
    }

    public void testPortugueseSorting() {
        sortWithLanguage("pt", "PT");
        assertLanguageWithIndex("pt", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("es", 2);
        assertLanguageWithIndex("fr", 3);
        assertLanguageWithIndex("it", 4);
    }

    public void testItalianSorting() {
        sortWithLanguage("it", "IT");
        assertLanguageWithIndex("it", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("es", 2);
        assertLanguageWithIndex("pt", 3);
        assertLanguageWithIndex("fr", 4);
    }

    public void testSimplifiedChineseSorting() {
        sortWithLanguage("zh", "CN");
        assertLanguageWithIndex("zh", 0);
    }

    public void testTraditionalChineseSorting() {
        sortWithLanguage("zh", "TW");
        assertLanguageWithIndex("zh", 0);
    }

    public void testKoreanSorting() {
        sortWithLanguage("ko", "KR");
        assertLanguageWithIndex("ko", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ja", 2);
    }

    public void testJapaneseSorting() {
        sortWithLanguage("ja", "JP");
        assertLanguageWithIndex("ja", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ko", 2);
    }

    public void testGermanSorting() {
        sortWithLanguage("de", "DE");
        assertLanguageWithIndex("de", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("fr", 2);
        assertLanguageWithIndex("es", 3);
        assertLanguageWithIndex("pt", 4);
    }

    public void testFrenchSorting() {
        sortWithLanguage("fr", "FR");
        assertLanguageWithIndex("fr", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("de", 2);
        assertLanguageWithIndex("es", 3);
        assertLanguageWithIndex("pt", 4);
    }

    public void testRussianSorting() {
        sortWithLanguage("ru", "RU");
        assertLanguageWithIndex("ru", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("es", 2);
        assertLanguageWithIndex("fr", 3);
    }

    public void testNorwegianSorting() {
        sortWithLanguage("nb", "NO");
        assertLanguageWithIndex("nb", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("sv", 2);
        assertLanguageWithIndex("fi", 3);
        assertLanguageWithIndex("da", 4);
        assertLanguageWithIndex("nl", 5);
        assertLanguageWithIndex("es", 6);
        assertLanguageWithIndex("fr", 7);
    }

    public void testSwedishSorting() {
        sortWithLanguage("sv", "SE");
        assertLanguageWithIndex("sv", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("nb", 2);
        assertLanguageWithIndex("fi", 3);
        assertLanguageWithIndex("da", 4);
        assertLanguageWithIndex("nl", 5);
        assertLanguageWithIndex("es", 6);
        assertLanguageWithIndex("fr", 7);
    }

    public void testFinnishSorting() {
        sortWithLanguage("fi", "FI");
        assertLanguageWithIndex("fi", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("nb", 2);
        assertLanguageWithIndex("sv", 3);
        assertLanguageWithIndex("da", 4);
        assertLanguageWithIndex("nl", 5);
        assertLanguageWithIndex("es", 6);
        assertLanguageWithIndex("fr", 7);
    }

    public void testDanishSorting() {
        sortWithLanguage("da", "DK");
        assertLanguageWithIndex("da", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("de", 2);
        assertLanguageWithIndex("fr", 3);
        assertLanguageWithIndex("nb", 4);
        assertLanguageWithIndex("sv", 5);
        assertLanguageWithIndex("fi", 6);
        assertLanguageWithIndex("nl", 7);
    }

    public void testDutchSorting() {
        sortWithLanguage("nl", "NL");
        assertLanguageWithIndex("nl", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("de", 2);
        assertLanguageWithIndex("fr", 3);
        assertLanguageWithIndex("nb", 4);
        assertLanguageWithIndex("sv", 5);
        assertLanguageWithIndex("fi", 6);
        assertLanguageWithIndex("da", 7);
    }

    public void testTurkishSorting() {
        sortWithLanguage("tr", "TR");
        assertLanguageWithIndex("tr", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("it", 2);
        assertLanguageWithIndex("es", 3);
        assertLanguageWithIndex("pt", 4);
        assertLanguageWithIndex("fr", 5);
    }

    public void testVietnameseSorting() {
        sortWithLanguage("vi", "VN");
        assertLanguageWithIndex("vi", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("zh", 2);
        assertLanguageWithIndex("zh", 3);
        assertLanguageWithIndex("th", 4);
        assertLanguageWithIndex("ms", 5);
        assertLanguageWithIndex("in", 6);
    }

    public void testThaiSorting() {
        sortWithLanguage("th", "TH");
        assertLanguageWithIndex("th", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("zh", 2);
        assertLanguageWithIndex("zh", 3);
        assertLanguageWithIndex("vi", 4);
        assertLanguageWithIndex("ms", 5);
        assertLanguageWithIndex("in", 6);
    }

    public void testMalaySorting() {
        sortWithLanguage("ms", "MY");
        assertLanguageWithIndex("ms", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("zh", 2);
        assertLanguageWithIndex("zh", 3);
        assertLanguageWithIndex("in", 4);
        assertLanguageWithIndex("th", 5);
        assertLanguageWithIndex("vi", 6);
    }

    public void testIndonesianSorting() {
        sortWithLanguage("in", "ID");
        assertLanguageWithIndex("in", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("zh", 2);
        assertLanguageWithIndex("zh", 3);
        assertLanguageWithIndex("ms", 4);
        assertLanguageWithIndex("th", 5);
        assertLanguageWithIndex("vi", 6);
    }

    private void sortWithLanguage(String language, String country) {
        Locale targetLocale = new Locale(language, country);
        DefaultLocale.saveDefaultLocale(getContext(), targetLocale);
        NewsContentProvider.getInstance(getContext()).sortNewsProviderLanguage(getContext());
    }

    private void assertLanguageWithIndex(String languageCode, int index) {
        NewsProviderLanguage newsProviderLanguage = mNewsContentProvider.getNewsLanguage(index);

        assertEquals(newsProviderLanguage.languageCode, languageCode);
    }
}
