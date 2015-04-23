package com.yooiistudios.newsflow.core.news.curation;

import android.content.Context;

import com.yooiistudios.newsflow.core.language.DefaultLocale;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 25.
 *
 * NewsProviderLanguageComparator
 *  현재 언어에 따라 뉴스 선택 언어를 정렬해주는 클래스
 */
public class NewsProviderLanguageSorter {
    private NewsProviderLanguageSorter() {
        throw new AssertionError("You MUST NOT create this class!");
    }

    public static LinkedHashMap<String, NewsProviderLanguage> sortLanguages(
            Context context, ArrayList<NewsProviderLanguage> newsProviderLanguageList) {

        LinkedHashMap<String, NewsProviderLanguage> newsProviderLanguages = new LinkedHashMap<>();

        // extract 를 하기 때문에 원본 리스트를 유지하기 위해서 clone
        ArrayList<NewsProviderLanguage> clonedLanguageList = new ArrayList<>(newsProviderLanguageList);

        // 언어별로 특정 언어만 앞으로 빼고 나머지는 순서대로 표시
//        Language currentLanguage = LanguageUtils.getCurrentLanguage(context);

        // 수정: 이사님께서 앱 지원 언어가 아닌 기기 언어를 기준으로 소팅을 원하셔서 수정
        Locale defaultLocale = DefaultLocale.loadDefaultLocale(context);

        if (defaultLocale.getLanguage().equals("zh") && defaultLocale.getCountry().equals("TW")) {
            // T-Chinese S-Chinese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "TW");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "CN");
        } else if (defaultLocale.getLanguage().equals("zh") && defaultLocale.getCountry().equals("CN")) {
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "CN");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "TW");
        } else if (defaultLocale.getCountry().equals("JP")) {
            // Japanese English Korean S-Chinese T-Chinese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ja", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ko", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "CN");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "TW");
        } else if (defaultLocale.getCountry().equals("KR")) {
            // Korean English Japanese S-Chinese T-Chinese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ko", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ja", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "CN");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "TW");
        }

        for (NewsProviderLanguage newsProviderLanguage : newsProviderLanguageList) {
            String key = makeKey(newsProviderLanguage.languageCode, newsProviderLanguage.regionCode);
            newsProviderLanguages.put(key, newsProviderLanguage);
        }

        /*
        // Test
        Set keySet = newsProviderLanguages.keySet();
        for (Object keyObject : keySet) {
            String key = (String) keyObject;
            NewsProviderLanguage newsProviderLanguage = newsProviderLanguages.get(key);
            System.out.println("KEY=" + key + ", VALUE=" + newsProviderLanguage.englishLanguageName);
        }
        */
        return newsProviderLanguages;
    }

    private static void putNewsProviderLanguage(
            LinkedHashMap<String, NewsProviderLanguage> newsProviderLanguages,
            ArrayList<NewsProviderLanguage> languageList,
            String languageCode, String regionCode) {
        NewsProviderLanguage newsProviderLanguage =
                extractNewsProviderLanguage(languageList, languageCode, regionCode);
        if (newsProviderLanguage != null) {
            String key = makeKey(languageCode, regionCode);
            newsProviderLanguages.put(key, newsProviderLanguage);
        }
    }

    private static String makeKey(String languageCode, String regionCode) {
        String key;
        if (regionCode == null) {
            key = languageCode;
        } else {
            key = languageCode + "-" + regionCode;
        }
        return key;
    }

    private static NewsProviderLanguage extractNewsProviderLanguage(
            ArrayList<NewsProviderLanguage> newsProviderLanguageList, String targetLanguageCode,
            String targetRegionCode) {
        Iterator<NewsProviderLanguage> iterator = newsProviderLanguageList.iterator();
        while (iterator.hasNext()) {
            NewsProviderLanguage newsProviderLanguage = iterator.next(); // must be called before you can call i.remove()
            if (targetLanguageCode.equalsIgnoreCase(newsProviderLanguage.languageCode)) {
                if (targetRegionCode == null || targetRegionCode.equalsIgnoreCase(newsProviderLanguage.regionCode)) {
                    iterator.remove();
                    return newsProviderLanguage;
                }

            }
        }
        return null;
    }
}
