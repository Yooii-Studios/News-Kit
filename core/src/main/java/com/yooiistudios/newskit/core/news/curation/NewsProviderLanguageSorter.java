package com.yooiistudios.newskit.core.news.curation;

import android.content.Context;

import com.yooiistudios.newskit.core.language.DefaultLocale;

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
        if (languageCode.equals("fr")) {
            // French English German Dutch Spanish Portuguese [French Spanish Portuguese 공통] 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nl", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pt", null);

            putFrenchSpanishPortugueseNewsProviderLanguage(newsProviderLanguages, clonedLanguageList);
        } else if (languageCode.equals("es")) {
            // Spanish English Portuguese French German	Dutch [French Spanish Portuguese 공통] 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pt", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nl", null);

            putFrenchSpanishPortugueseNewsProviderLanguage(newsProviderLanguages, clonedLanguageList);
        } else if (languageCode.equals("pt")) {
            // Portuguese English Spanish French German	Dutch [French Spanish Portuguese 공통] 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pt", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nl", null);

            putFrenchSpanishPortugueseNewsProviderLanguage(newsProviderLanguages, clonedLanguageList);
        } else if (languageCode.equals("hi")) {
            // Hindi 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "hi", null);
        } else if (languageCode.equals("zh") && countryCode.equals("CN")) {
            // S-Chinese T-Chinese English Japanese	Korean 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ja", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ko", null);
        } else if (languageCode.equals("zh") && countryCode.equals("TW")) {
            // T-Chinese S-Chinese English Japanese	Korean 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ja", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ko", null);
        } else if (languageCode.equals("ja")) {
            // Japanese English S-Chinese T-Chinese Korean 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ja", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ko", null);
        } else if (languageCode.equals("ko")) {
            // Korean English Japanese S-Chinese T-Chinese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ko", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ja", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
        } else if (languageCode.equals("vi")) {
            // Vietnamese English S-Chinese T-Chinese Japanese Korean Thai 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "vi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "cn");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "zh", "tw");
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ja", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ko", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "th", null);
        } else if (languageCode.equals("ar")) {
            // Arabic Persian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ar", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fa", null);
        } else if (languageCode.equals("fa")) {
            // Persian Arabic 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fa", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ar", null);
        } else if (languageCode.equals("ru")) {
            // Russian English Kazakh Ukrainian Estonian Lithuanian Latvian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "kk", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "uk", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ee", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "lt", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "lv", null);
        } else if (languageCode.equals("de")) {
            // German 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
        } else if (languageCode.equals("nl")) {
            // Dutch English German 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nl", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
        } else if (languageCode.equals("it")) {
            // Italian English French Spanish Portuguese German 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "it", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "es", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pt", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "de", null);
        } else if (languageCode.equals("nb")) {
            // Norwegian English Swedish Finnish Danish 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nb", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sv", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "da", null);
        } else if (languageCode.equals("sv")) {
            // Swedish English Norwegian Finnish Danish 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sv", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nb", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "da", null);
        } else if (languageCode.equals("fi")) {
            // Finnish English Swedish Norwegian Danish 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sv", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nb", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "da", null);
        } else if (languageCode.equals("da")) {
            // Danish English Norwegian Swedish Finnish 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "da", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nb", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sv", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fi", null);
        } else if (languageCode.equals("tr")) {
            // Turkish English Italian Spanish Portuguese French 우선정렬
            // Turkish English Greek 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "tr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "el", null);
        } else if (languageCode.equals("el")) {
            // Greek English Turkish 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "el", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "tr", null);
        } else if (languageCode.equals("cs")) {
            // Czech English Slovak 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "cs", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sk", null);
        } else if (languageCode.equals("sk")) {
            // Slovak English Czech 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sk", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "cs", null);
        } else if (languageCode.equals("ee")) {
            // Estonian English Lithuanian Latvian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ee", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "lt", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "lv", null);
        } else if (languageCode.equals("lt")) {
            // Lithuanian English Estonian Latvian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "lt", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ee", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "lv", null);
        } else if (languageCode.equals("lv")) {
            // Latvian English Estonian Lithuanian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "lv", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ee", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "lt", null);
        } else if (languageCode.equals("pl")) {
            // Polish English Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pl", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("ro")) {
            // Romanian English Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ro", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("hr")) {
            // Croatian English Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "hr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("kk")) {
            // Kazakh English Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "kk", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("uk")) {
            // Ukrainian English Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "uk", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("hu")) {
            // Hungarian English Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "hu", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("bg")) {
            // Bulgarian English Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "bg", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("be")) {
            // Belarusian English Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "be", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("sr")) {
            // Serbian English Russian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sr", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        } else if (languageCode.equals("il")) {
            // Hebrew English Arabic Persian 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "il", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ar", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fa", null);
        } else if (languageCode.equals("th")) {
            // Thai English Vietnamese Malay 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "th", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "vi", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ms", null);
        } else if (languageCode.equals("ms")) {
            // Malay English Thai Vietnamese 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ms", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "th", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "vi", null);
        } else if (languageCode.equals("in")) {
            // Indonesia English Malay 우선정렬
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "in", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "en", null);
            putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ms", null);
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

    private static void putFrenchSpanishPortugueseNewsProviderLanguage(LinkedHashMap<String, NewsProviderLanguage> newsProviderLanguages, ArrayList<NewsProviderLanguage> clonedLanguageList) {
        // Italian Norwegian Swedish Finnish Danish	Turkish	Greek Russian Czech Slovak Estonian
        // Lithuanian Latvian Polish Romanian Croatian Kazakh Ukrainian Hungarian Bulgarian
        // Belarusian Serbian Hebrew
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "it", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "nb", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sv", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "fi", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "da", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "tr", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "el", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ru", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "cs", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sk", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ee", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "lt", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "lv", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "pl", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "ro", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "hr", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "kk", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "uk", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "hu", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "bg", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "be", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "sr", null);
        putNewsProviderLanguage(newsProviderLanguages, clonedLanguageList, "il", null);
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
