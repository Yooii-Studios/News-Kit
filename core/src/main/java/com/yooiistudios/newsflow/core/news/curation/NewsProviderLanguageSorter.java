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
        String languageCode = defaultLocale.getLanguage();
        String countryCode = defaultLocale.getCountry();

        if (languageCode.equals("es")) {
            // Spanish English Portuguese French Italian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pt", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "it", null);
        } else if (languageCode.equals("pt")) {
            // Portuguese English Spanish French Italian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pt", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "it", null);
        } else if (languageCode.equals("it")) {
            // Italian English Spanish Portuguese French 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "it", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pt", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
        } else if (languageCode.equals("zh") && countryCode.equals("CN")) {
            // S-Chinese T-Chinese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
        } else if (languageCode.equals("zh") && countryCode.equals("TW")) {
            // T-Chinese S-Chinese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
        } else if (languageCode.equals("ko")) {
            // Korean English Japanese S-Chinese T-Chinese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ko", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ja", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
        } else if (languageCode.equals("ja")) {
            // Japanese English Korean S-Chinese T-Chinese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ja", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ko", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
        } else if (languageCode.equals("de")) {
            // German English French Spanish Portuguese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pt", null);
        } else if (languageCode.equals("fr")) {
            // French English German Spanish Portuguese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pt", null);
        } else if (languageCode.equals("ru")) {
            // Russian English Spanish French 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
        } else if (languageCode.equals("no")) {
            // Norwegian English Swedish Finnish Danish Dutch Spanish French Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "no", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sv", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "da", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nl", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("sv")) {
            // Swedish English Norwegian Finnish Danish Dutch Spanish French Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sv", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "no", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "da", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nl", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("fi")) {
            // Finnish English Norwegian Swedish Danish Dutch Spanish French Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "no", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sv", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "da", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nl", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("da")) {
            // Danish English German French Norwegian Swedish Finnish Dutch Spanish Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "da", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "no", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sv", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nl", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("nl")) {
            // Dutch English German French Norwegian Swedish Finnish Danish Spanish Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nl", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "no", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sv", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "da", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("tr")) {
            // Turkish English Italian Spanish Portuguese French 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "tr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "it", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pt", null);
        } else if (languageCode.equals("vi")) {
            // Vietnamese English S-Chinese T-Chinese Thai Malay Indonesian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "vi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "th", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ms", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "in", null);
        } else if (languageCode.equals("th")) {
            // Thai English S-Chinese T-Chinese Vietnamese Malay Indonesian German 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "th", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "vi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ms", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "in", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
        } else if (languageCode.equals("ms")) {
            // Malay English S-Chinese T-Chinese Indonesian Thai Vietnamese German 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ms", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "in", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "th", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "vi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
        } else if (languageCode.equals("in")) {
            // Indonesian English S-Chinese T-Chinese Malay Thai Vietnamese German 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "in", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ms", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "th", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "vi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
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
        // 대문자 소문자가 엉키면 키가 달라져서 중복 추가되는 경우가 있어 애초부터 소문자로 픽스
        return key.toLowerCase();
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
