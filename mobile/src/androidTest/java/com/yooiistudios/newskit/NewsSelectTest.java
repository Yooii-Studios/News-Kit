package com.yooiistudios.newskit;

import android.test.AndroidTestCase;

import com.yooiistudios.newskit.core.language.DefaultLocale;
import com.yooiistudios.newskit.core.news.curation.NewsContentProvider;
import com.yooiistudios.newskit.core.news.curation.NewsProviderLanguage;

import java.util.Locale;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 2. 26.
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

    public void testFrenchSorting() {
        sortWithLanguage("fr", "FR");
        assertLanguageWithIndex("fr", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("de", 2);
        assertLanguageWithIndex("nl", 3);
        assertLanguageWithIndex("es", 4);
        assertLanguageWithIndex("pt", 5);

        // French Spanish Portuguese 공통
        assertLanguageWithIndex("it", 6);
        assertLanguageWithIndex("nb", 7);
        assertLanguageWithIndex("sv", 8);
        assertLanguageWithIndex("fi", 9);
        assertLanguageWithIndex("da", 10);
        assertLanguageWithIndex("tr", 11);
        assertLanguageWithIndex("el", 12);
        assertLanguageWithIndex("ru", 13);
        assertLanguageWithIndex("cs", 14);
        assertLanguageWithIndex("sk", 15);
        assertLanguageWithIndex("ee", 16);
        assertLanguageWithIndex("lt", 17);
        assertLanguageWithIndex("lv", 18);
        assertLanguageWithIndex("pl", 19);
        assertLanguageWithIndex("ro", 20);
        assertLanguageWithIndex("hr", 21);
        assertLanguageWithIndex("kk", 22);
        assertLanguageWithIndex("uk", 23);
        assertLanguageWithIndex("hu", 24);
        assertLanguageWithIndex("bg", 25);
        assertLanguageWithIndex("be", 26);
        assertLanguageWithIndex("sr", 27);
        assertLanguageWithIndex("il", 28);
    }

    public void testSpanishSorting() {
        sortWithLanguage("es", "ES");
        assertLanguageWithIndex("es", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("pt", 2);
        assertLanguageWithIndex("fr", 3);
        assertLanguageWithIndex("de", 4);
        assertLanguageWithIndex("nl", 5);

        // French Spanish Portuguese 공통
        assertLanguageWithIndex("it", 6);
        assertLanguageWithIndex("nb", 7);
        assertLanguageWithIndex("sv", 8);
        assertLanguageWithIndex("fi", 9);
        assertLanguageWithIndex("da", 10);
        assertLanguageWithIndex("tr", 11);
        assertLanguageWithIndex("el", 12);
        assertLanguageWithIndex("ru", 13);
        assertLanguageWithIndex("cs", 14);
        assertLanguageWithIndex("sk", 15);
        assertLanguageWithIndex("ee", 16);
        assertLanguageWithIndex("lt", 17);
        assertLanguageWithIndex("lv", 18);
        assertLanguageWithIndex("pl", 19);
        assertLanguageWithIndex("ro", 20);
        assertLanguageWithIndex("hr", 21);
        assertLanguageWithIndex("kk", 22);
        assertLanguageWithIndex("uk", 23);
        assertLanguageWithIndex("hu", 24);
        assertLanguageWithIndex("bg", 25);
        assertLanguageWithIndex("be", 26);
        assertLanguageWithIndex("sr", 27);
        assertLanguageWithIndex("il", 28);
    }

    public void testPortugueseSorting() {
        sortWithLanguage("pt", "PT");
        assertLanguageWithIndex("pt", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("es", 2);
        assertLanguageWithIndex("fr", 3);
        assertLanguageWithIndex("de", 4);
        assertLanguageWithIndex("nl", 5);

        // French Spanish Portuguese 공통
        assertLanguageWithIndex("it", 6);
        assertLanguageWithIndex("nb", 7);
        assertLanguageWithIndex("sv", 8);
        assertLanguageWithIndex("fi", 9);
        assertLanguageWithIndex("da", 10);
        assertLanguageWithIndex("tr", 11);
        assertLanguageWithIndex("el", 12);
        assertLanguageWithIndex("ru", 13);
        assertLanguageWithIndex("cs", 14);
        assertLanguageWithIndex("sk", 15);
        assertLanguageWithIndex("ee", 16);
        assertLanguageWithIndex("lt", 17);
        assertLanguageWithIndex("lv", 18);
        assertLanguageWithIndex("pl", 19);
        assertLanguageWithIndex("ro", 20);
        assertLanguageWithIndex("hr", 21);
        assertLanguageWithIndex("kk", 22);
        assertLanguageWithIndex("uk", 23);
        assertLanguageWithIndex("hu", 24);
        assertLanguageWithIndex("bg", 25);
        assertLanguageWithIndex("be", 26);
        assertLanguageWithIndex("sr", 27);
        assertLanguageWithIndex("il", 28);
    }

    public void testHindiSorting() {
        sortWithLanguage("hi", "HI");
        assertLanguageWithIndex("hi", 0);
    }

    public void testSimplifiedChineseSorting() {
        sortWithLanguage("zh", "CN");
        // TODO: check region
        assertLanguageWithIndex("zh", 0);
        assertLanguageWithIndex("zh", 1);
        assertLanguageWithIndex("en", 2);
        assertLanguageWithIndex("ja", 3);
        assertLanguageWithIndex("ko", 4);
    }

    public void testTraditionalChineseSorting() {
        sortWithLanguage("zh", "TW");
        // TODO: check region
        assertLanguageWithIndex("zh", 0);
        assertLanguageWithIndex("zh", 1);
        assertLanguageWithIndex("en", 2);
        assertLanguageWithIndex("ja", 3);
        assertLanguageWithIndex("ko", 4);
    }

    public void testJapaneseSorting() {
        sortWithLanguage("ja", "JP");
        assertLanguageWithIndex("ja", 0);
        assertLanguageWithIndex("en", 1);
        // TODO: check region
        assertLanguageWithIndex("zh", 2);
        assertLanguageWithIndex("zh", 3);
        assertLanguageWithIndex("ko", 4);
    }

    public void testKoreanSorting() {
        sortWithLanguage("ko", "KR");
        assertLanguageWithIndex("ko", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ja", 2);
        // TODO: check region
        assertLanguageWithIndex("zh", 3);
        assertLanguageWithIndex("zh", 4);
    }

    public void testVietnameseSorting() {
        sortWithLanguage("vi", "VN");
        assertLanguageWithIndex("vi", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("zh", 2);
        assertLanguageWithIndex("zh", 3);
        assertLanguageWithIndex("ja", 4);
        assertLanguageWithIndex("ko", 5);
        assertLanguageWithIndex("th", 6);
    }

    public void testArabicSorting() {
        sortWithLanguage("ar", "AR");
        assertLanguageWithIndex("ar", 0);
        assertLanguageWithIndex("fa", 1);
    }

    public void testPersianSorting() {
        sortWithLanguage("fa", "FA");
        assertLanguageWithIndex("fa", 0);
        assertLanguageWithIndex("ar", 1);
    }

    public void testRussianSorting() {
        sortWithLanguage("ru", "RU");
        assertLanguageWithIndex("ru", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("kk", 2);
        assertLanguageWithIndex("uk", 3);
        assertLanguageWithIndex("ee", 4);
        assertLanguageWithIndex("lt", 5);
        assertLanguageWithIndex("lv", 6);
    }

    public void testGermanSorting() {
        sortWithLanguage("de", "DE");
        assertLanguageWithIndex("de", 0);
    }

    public void testDutchSorting() {
        sortWithLanguage("nl", "NL");
        assertLanguageWithIndex("nl", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("de", 2);
    }

    public void testItalianSorting() {
        sortWithLanguage("it", "IT");
        assertLanguageWithIndex("it", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("fr", 2);
        assertLanguageWithIndex("es", 3);
        assertLanguageWithIndex("pt", 4);
        assertLanguageWithIndex("de", 5);
    }

    public void testNorwegianSorting() {
        sortWithLanguage("nb", "NO");
        assertLanguageWithIndex("nb", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("sv", 2);
        assertLanguageWithIndex("fi", 3);
        assertLanguageWithIndex("da", 4);
    }

    public void testSwedishSorting() {
        sortWithLanguage("sv", "SE");
        assertLanguageWithIndex("sv", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("nb", 2);
        assertLanguageWithIndex("fi", 3);
        assertLanguageWithIndex("da", 4);
    }

    public void testFinnishSorting() {
        sortWithLanguage("fi", "FI");
        assertLanguageWithIndex("fi", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("sv", 2);
        assertLanguageWithIndex("nb", 3);
        assertLanguageWithIndex("da", 4);
    }

    public void testDanishSorting() {
        sortWithLanguage("da", "DK");
        assertLanguageWithIndex("da", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("nb", 2);
        assertLanguageWithIndex("sv", 3);
        assertLanguageWithIndex("fi", 4);
    }

    public void testTurkishSorting() {
        sortWithLanguage("tr", "TR");
        assertLanguageWithIndex("tr", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("el", 2);
    }

    public void testGreekSorting() {
        sortWithLanguage("el", "EL");
        assertLanguageWithIndex("el", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("tr", 2);
    }

    public void testCzechSorting() {
        sortWithLanguage("cs", "CS");
        assertLanguageWithIndex("cs", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("sk", 2);
    }

    public void testSlovakSorting() {
        sortWithLanguage("sk", "SK");
        assertLanguageWithIndex("sk", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("cs", 2);
    }

    public void testEstonianSorting() {
        sortWithLanguage("ee", "EE");
        assertLanguageWithIndex("ee", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("lt", 2);
        assertLanguageWithIndex("lv", 3);
    }

    public void testLithuanianSorting() {
        sortWithLanguage("lt", "LT");
        assertLanguageWithIndex("lt", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ee", 2);
        assertLanguageWithIndex("lv", 3);
    }

    public void testLatvianSorting() {
        sortWithLanguage("lv", "LV");
        assertLanguageWithIndex("lv", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ee", 2);
        assertLanguageWithIndex("lt", 3);
    }

    public void testPolishSorting() {
        sortWithLanguage("pl", "PL");
        assertLanguageWithIndex("pl", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ru", 2);
    }

    public void testRomanianSorting() {
        sortWithLanguage("ro", "RO");
        assertLanguageWithIndex("ro", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ru", 2);
    }

    public void testCroatianSorting() {
        sortWithLanguage("hr", "HR");
        assertLanguageWithIndex("hr", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ru", 2);
    }

    public void testKazakhSorting() {
        sortWithLanguage("kk", "KK");
        assertLanguageWithIndex("kk", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ru", 2);
    }

    public void testUkrainianSorting() {
        sortWithLanguage("uk", "UK");
        assertLanguageWithIndex("uk", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ru", 2);
    }

    public void testHungarianSorting() {
        sortWithLanguage("hu", "HU");
        assertLanguageWithIndex("hu", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ru", 2);
    }

    public void testBulgarianSorting() {
        sortWithLanguage("bg", "BG");
        assertLanguageWithIndex("bg", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ru", 2);
    }

    public void testBelarusianSorting() {
        sortWithLanguage("be", "BE");
        assertLanguageWithIndex("be", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ru", 2);
    }

    public void testSerbianSorting() {
        sortWithLanguage("sr", "SR");
        assertLanguageWithIndex("sr", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ru", 2);
    }

    public void testHebrewSorting() {
        sortWithLanguage("il", "IL");
        assertLanguageWithIndex("il", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ar", 2);
        assertLanguageWithIndex("fa", 3);
    }

    public void testThaiSorting() {
        sortWithLanguage("th", "TH");
        assertLanguageWithIndex("th", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("vi", 2);
        assertLanguageWithIndex("ms", 3);
    }

    public void testMalaySorting() {
        sortWithLanguage("ms", "MY");
        assertLanguageWithIndex("ms", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("th", 2);
        assertLanguageWithIndex("vi", 3);
    }

    public void testIndonesianSorting() {
        sortWithLanguage("in", "ID");
        assertLanguageWithIndex("in", 0);
        assertLanguageWithIndex("en", 1);
        assertLanguageWithIndex("ms", 2);
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
