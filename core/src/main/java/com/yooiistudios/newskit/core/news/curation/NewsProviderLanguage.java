package com.yooiistudios.newskit.core.news.curation;

import java.util.ArrayList;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 25.
 *
 * NewsProviderLanguage
 *  뉴스 선택 화면의 탭에 들어가는 가장 상위 모델
 *  해당 언어의 표기, 코드 및 이를 사용하는 국가의 리스트를 가지는 자료 구조
 */
public class NewsProviderLanguage {
    public String englishLanguageName;
    public String regionalLanguageName;

    public String languageCode;
    public String regionCode;

    public ArrayList<NewsProviderCountry> newsProviderCountries = new ArrayList<>();
}
