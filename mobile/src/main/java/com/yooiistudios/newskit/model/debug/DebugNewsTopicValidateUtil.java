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
        new android.os.AsyncTask<Void, Void, ArrayList<NewsFeed>>() {
            @Override
            public ArrayList<NewsFeed> doInBackground(Void... args) {
                return getAll(context);
            }

            @Override
            protected void onPostExecute(ArrayList<NewsFeed> newsFeeds) {
                super.onPostExecute(newsFeeds);
                NLLog.d(TAG, "Test done. Saving to database...");
                NewsDb.getInstance(context).clearArchiveDebug();
                NewsDb.getInstance(context).saveBottomNewsFeedList(newsFeeds);
                try {
                    NewsDb.copyDbToExternalStorage(context);
                } catch (ExternalStorage.ExternalStorageException ignored) {
                    // 디버그 모드에서만 작동해야 하므로 예외상황시 무시한다
                }
            }
        }.execute();
    }

    private static ArrayList<NewsFeed> getAll(Context context) {
        ArrayList<NewsFeed> newsFeeds = new ArrayList<>();
        NLLog.d(TAG, "NewsProviderLanguage count: " + NewsProviderLangType.values().length);
        NewsContentProvider.getInstance(context).sortNewsProviderLanguage(context);
        for (int i = 0; i < NewsProviderLangType.values().length; i++) {
            NLLog.d(TAG, String.format("%3dth NewsProviderLanguage", i));
            NewsProviderLanguage providerLanguage
                    = NewsContentProvider.getInstance(context).getNewsLanguage(i);

            newsFeeds.addAll(getNewsFeedsFromNewsProviderLanguages(providerLanguage));
        }
        return newsFeeds;
    }

    private static ArrayList<NewsFeed> getNewsFeedsFromNewsProviderLanguages(NewsProviderLanguage providerLanguage) {
        ArrayList<NewsFeed> newsFeeds = new ArrayList<>();
        ArrayList<NewsProviderCountry> countries = providerLanguage.newsProviderCountries;
        NLLog.d(TAG, "NewsProviderCountry count: " + countries.size());
        for (int j = 0; j < countries.size(); j++) {
            NewsProviderCountry providerCountry = countries.get(j);
            NLLog.d(TAG, String.format("%3dth NewsProviderCountry", j));
            newsFeeds.addAll(getNewsFeedsFromNewsProviderCountries(providerCountry));
        }

        return newsFeeds;
    }

    private static ArrayList<NewsFeed> getNewsFeedsFromNewsProviderCountries(NewsProviderCountry providerCountry) {
        ArrayList<NewsFeed> newsFeeds = new ArrayList<>();
        ArrayList<NewsProvider> providers = providerCountry.newsProviders;
        NLLog.d(TAG, "NewsProvider count: " + providers.size());
        for (int k = 0; k < providers.size(); k++) {
            NewsProvider newsProvider = providers.get(k);
            NLLog.d(TAG, String.format("%3dth NewsProvider", k));
            newsFeeds.addAll(fromNewsProvider(newsProvider));
        }

        return newsFeeds;
    }

    private static ArrayList<NewsFeed> fromNewsProvider(NewsProvider newsProvider) {
        ArrayList<NewsFeed> newsFeeds = new ArrayList<>();
        ArrayList<NewsTopic> topics = newsProvider.getNewsTopicList();
        NLLog.d(TAG, "NewsTopic count: " + topics.size());
        for (int l = 0; l < topics.size(); l++) {
            NewsTopic topic = topics.get(l);
            NLLog.d(TAG, String.format("%3dth NewsTopic", l));
            NewsFeed newsFeed = NewsFeedFetchUtil.fetch(topic, 20, false);
            newsFeed.setTopicIdInfo(topic);

            newsFeeds.add(newsFeed);
        }

        return newsFeeds;
    }
}
