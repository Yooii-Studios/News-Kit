package com.yooiistudios.newskit.model.debug;

import android.content.Context;
import android.support.annotation.Nullable;

import com.yooiistudios.newskit.core.cache.AsyncTask;
import com.yooiistudios.newskit.core.news.NewsFeed;
import com.yooiistudios.newskit.core.news.NewsFeedUrl;
import com.yooiistudios.newskit.core.news.NewsFeedUrlType;
import com.yooiistudios.newskit.core.news.NewsTopic;
import com.yooiistudios.newskit.core.news.curation.NewsContentProvider;
import com.yooiistudios.newskit.core.news.curation.NewsProvider;
import com.yooiistudios.newskit.core.news.curation.NewsProviderCountry;
import com.yooiistudios.newskit.core.news.curation.NewsProviderLangType;
import com.yooiistudios.newskit.core.news.curation.NewsProviderLanguage;
import com.yooiistudios.newskit.core.news.database.NewsDb;
import com.yooiistudios.newskit.core.news.util.NewsFeedFetchUtil;
import com.yooiistudios.newskit.core.util.ExternalStorage;
import com.yooiistudios.newskit.core.util.NLLog;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 4. 23.
 *
 * DebugNewsTopicValidateUtil
 *  뉴스 토픽이 valid 한 것인지 테스트하기 위해 fetch, 데이터베이스에 저장하는 클래스
 */
public class DebugNewsTopicValidateUtil {
    private static final String TAG = DebugNewsTopicValidateUtil.class.getSimpleName();

    private DebugNewsTopicValidateUtil() {
        throw new AssertionError("You MUST not create this class!");
    }

    public static void validateAll(final Context context) {
        new android.os.AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... args) {
                NLLog.d(TAG, "validateAll start");
                NewsDb.getInstance(context).clearArchiveDebug();

                int langCount = NewsProviderLangType.values().length;
                NewsContentProvider.getInstance(context).sortNewsProviderLanguage(context);
                for (int i = 0; i < langCount; i++) {
                    NewsProviderLanguage providerLanguage
                            = NewsContentProvider.getInstance(context).getNewsLanguage(i);

                    String isoCode = providerLanguage.languageCode;
                    if (providerLanguage.regionCode != null && providerLanguage.regionCode.length() > 0) {
                        isoCode += "_" + providerLanguage.regionCode;
                    }
                    int left = langCount - i;
                    NLLog.d(TAG, String.format(Locale.US, "NewsProviderLanguage : %5s(%3d/%3d)", isoCode, i, langCount));

                    ArrayList<NewsFeed> newsFeeds = getNewsFeedsFromNewsProviderLanguages(providerLanguage);

                    saveNewsFeedsAndExportWith(context, providerLanguage, newsFeeds);
                }
                NLLog.d(TAG, "validateAll end");
                return null;
            }
        }.execute();
    }

    public static void validateLanguage(final Context context,
                                        final String languageCode,
                                        @Nullable final String regionCode) {
        new android.os.AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... args) {
                NLLog.d(TAG, "validateLanguage start");
                NewsDb.getInstance(context).clearArchiveDebug();

                NewsContentProvider.getInstance(context).sortNewsProviderLanguage(context);

                NewsProviderLanguage providerLanguage = NewsContentProvider.getInstance(context)
                        .getNewsLanguageByLanguageAndRegionDebug(languageCode, regionCode);
                ArrayList<NewsFeed> newsFeeds = getNewsFeedsFromNewsProviderLanguages(providerLanguage);

                saveNewsFeedsAndExportWith(context, providerLanguage, newsFeeds);
                NLLog.d(TAG, "validateLanguage end");
                return null;
            }
        }.execute();
    }

    public static void validateUrl(final Context context, final String url) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    NewsFeed newsFeed = NewsFeedFetchUtil.fetch(new NewsFeedUrl(
                            "http://www.baomoi.com/Home/KinhTe.rss",
                            NewsFeedUrlType.CUSTOM), 10, false);
                    NLLog.now("newsFeed: " + newsFeed.toString());
                    return null;
                }
            }.execute();
    }

    public static void validateDebugNewsUrls(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                int failCount = 0;
                for (String url : DebugNewsUrls.sUrls) {
                    NewsFeed newsFeed = NewsFeedFetchUtil.fetch(new NewsFeedUrl(
                            url,
                            NewsFeedUrlType.CUSTOM), 10, false);
                    boolean failed = newsFeed.getNewsFeedFetchState().ordinal() > 1;
                    if (failed) {
                        failCount++;
                    }

                    NLLog.i("qwer", !failed + ", url: " + url);
                }
                NLLog.i("qwer", "failCount: " + failCount);
                return null;
            }
        }.execute();
    }

    private static void saveNewsFeedsAndExportWith(Context context, NewsProviderLanguage providerLanguage, ArrayList<NewsFeed> newsFeeds) {
        NewsDb.getInstance(context).clearArchiveDebug();
        NewsDb.getInstance(context).saveBottomNewsFeedList(newsFeeds);
        try {
            String postfix = providerLanguage.languageCode;
            if (providerLanguage.regionCode != null && providerLanguage.regionCode.length() > 0) {
                postfix += "_" + providerLanguage.regionCode;
            }
            NewsDb.copyDbToExternalStorageWithPostfix(context, postfix);
        } catch (ExternalStorage.ExternalStorageException ignored) {
            // 디버그 모드에서만 작동해야 하므로 예외상황시 무시한다
        }
    }

//    public static void checkNewsUrls(final Context context, final String[] urls) {
//        new android.os.AsyncTask<Void, Void, ArrayList<NewsFeed>>() {
//            @Override
//            public ArrayList<NewsFeed> doInBackground(Void... args) {
//                ArrayList<NewsFeed> newsFeeds = new ArrayList<>();
//                int left = urls.length;
//                for (String url : urls) {
//                    NewsFeed newsFeed = NewsFeedFetchUtil.fetch(new NewsFeedUrl(
//                            url, NewsFeedUrlType.CUSTOM), 10, false);
//                    newsFeeds.add(newsFeed);
//                    NLLog.d(TAG, "left: " + --left);
//                }
//                NLLog.d(TAG, "done");
//                return newsFeeds;
//            }
//
//            @Override
//            protected void onPostExecute(ArrayList<NewsFeed> newsFeeds) {
//                super.onPostExecute(newsFeeds);
//                configOnFetch(newsFeeds, context);
//            }
//        }.execute();
//    }

//    private static void appendToDatabase(Context context, ArrayList<NewsFeed> newsFeeds, int accumuNewsFeedSize) {
//        for (int j = 0; j < newsFeeds.size(); j++) {
//            saveNewsFeedAt(newsFeeds.get(j), j + accumuNewsFeedSize, context);
//        }
//    }

    private static void saveNewsFeedAt(NewsFeed newsFeed, int position, Context context) {
        NewsDb.getInstance(context).saveBottomNewsFeedAt(newsFeed, position);
    }

    private static ArrayList<NewsFeed> getNewsFeedsFromNewsProviderLanguages(NewsProviderLanguage providerLanguage) {
        ArrayList<NewsFeed> newsFeeds = new ArrayList<>();
        ArrayList<NewsProviderCountry> countries = providerLanguage.newsProviderCountries;
        int countryCount = countries.size();
        for (int i = 0; i < countryCount; i++) {
            NewsProviderCountry providerCountry = countries.get(i);
            NLLog.d(TAG, String.format(Locale.US, "NewsProviderCountry : %2s(%3d/%3d)", providerCountry.countryCode, i, countryCount));
            newsFeeds.addAll(getNewsFeedsFromNewsProviderCountries(providerCountry));
        }

        return newsFeeds;
    }

    private static ArrayList<NewsFeed> getNewsFeedsFromNewsProviderCountries(NewsProviderCountry providerCountry) {
        ArrayList<NewsFeed> newsFeeds = new ArrayList<>();
        ArrayList<NewsProvider> providers = providerCountry.newsProviders;
        int providerCount = providers.size();
        for (int i = 0; i < providerCount; i++) {
            NewsProvider newsProvider = providers.get(i);
            NLLog.d(TAG, String.format(Locale.US, "NewsProvider : %s(%3d/%3d)", newsProvider.name, i, providerCount));
            newsFeeds.addAll(fromNewsProvider(newsProvider));
        }

        return newsFeeds;
    }

    private static ArrayList<NewsFeed> fromNewsProvider(NewsProvider newsProvider) {
        ArrayList<NewsFeed> newsFeeds = new ArrayList<>();
        ArrayList<NewsTopic> topics = newsProvider.getNewsTopicList();
        int topicCount = topics.size();
        for (int i = 0; i < topicCount; i++) {
            NewsTopic topic = topics.get(i);
            NLLog.d(TAG, String.format(Locale.US, "NewsTopic : %s(%3d/%3d)", topic.title, i, topicCount));
            NewsFeed newsFeed = NewsFeedFetchUtil.fetch(topic, 10, false);
            newsFeed.setTopicIdInfo(topic);

            newsFeeds.add(newsFeed);
        }

        return newsFeeds;
    }
}
