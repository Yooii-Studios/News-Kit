package com.yooiistudios.newskit.model.debug;

import android.content.Context;

import com.yooiistudios.newskit.core.news.NewsFeed;
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

    public static void run(final Context context) {
        new android.os.AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... args) {
                NewsDb.getInstance(context).clearArchiveDebug();

                int langCount = NewsProviderLangType.values().length;
                int accumuNewsFeedSize = 0;
                NewsContentProvider.getInstance(context).sortNewsProviderLanguage(context);
                for (int i = 0; i < langCount; i++) {
                    int left = langCount - i;
                    NLLog.d(TAG, String.format("NewsProviderLanguage left: %3d", left));
                    NewsProviderLanguage providerLanguage
                            = NewsContentProvider.getInstance(context).getNewsLanguage(i);

                    ArrayList<NewsFeed> newsFeeds = getNewsFeedsFromNewsProviderLanguages(providerLanguage);

                    for (int j = 0; j < newsFeeds.size(); j++) {
                        saveNewsFeedAt(newsFeeds.get(j), j + accumuNewsFeedSize, context);
                    }
                    try {
                        NewsDb.copyDbToExternalStorage(context);
                    } catch (ExternalStorage.ExternalStorageException ignored) {
                        // 디버그 모드에서만 작동해야 하므로 예외상황시 무시한다
                    }
                    accumuNewsFeedSize += newsFeeds.size();
                }

                return null;
            }
        }.execute();
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

//    private static void configOnFetch(ArrayList<NewsFeed> newsFeeds, Context context) {
//        NLLog.d(TAG, "Test done. Saving to database...");
//        NewsDb.getInstance(context).clearArchiveDebug();
//        saveNewsFeedsAndCopyToSdCard(newsFeeds, context);
//    }

//    private static void saveNewsFeedsAndCopyToSdCard(ArrayList<NewsFeed> newsFeeds, Context context) {
//        NewsDb.getInstance(context).saveBottomNewsFeedList(newsFeeds);
//        //saveBottomNewsFeedAt
//        try {
//            NewsDb.copyDbToExternalStorage(context);
//        } catch (ExternalStorage.ExternalStorageException ignored) {
//            // 디버그 모드에서만 작동해야 하므로 예외상황시 무시한다
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
            int left = countryCount - i;
            NLLog.d(TAG, String.format("NewsProviderCountry left: %3d", left));
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
            int left = providerCount - i;
            NLLog.d(TAG, String.format("NewsProvider left: %3d", left));
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
            int left = topicCount - i;
            NLLog.d(TAG, String.format("NewsTopic left: %3d", left));
            NewsFeed newsFeed = NewsFeedFetchUtil.fetch(topic, 10, false);
            newsFeed.setTopicIdInfo(topic);

            newsFeeds.add(newsFeed);
        }

        return newsFeeds;
    }
}
