package com.yooiistudios.news.model.news;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX;
import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX_KEY;
import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX_SHARED_PREFERENCES;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 25.
 *
 * NLNewsFeedArchiveUtil
 *  뉴스 피드 아카이빙 유틸
 */
public class NewsFeedArchiveUtils {
    private static final String SP_KEY_NEWS_FEED = "SP_KEY_NEWS_FEED";

    private static final String KEY_TOP_NEWS_FEED = "KEY_TOP_NEWS_FEED";
    private static final String KEY_NEWS_FEED_RECENT_REFRESH = "KEY_NEWS_FEED_RECENT_REFRESH";

    private static final String KEY_BOTTOM_NEWS_FEED = "KEY_BOTTOM_NEWS_FEED_";
    private static final String KEY_BOTTOM_NEWS_FEED_LARGEST_INDEX = "KEY_BOTTOM_NEWS_FEED_LARGEST_INDEX";

    // 60 Min * 60 Sec * 1000 millisec = 1 Hour
    private static final long REFRESH_TERM_MILLISEC = 60 * 60 * 1000;
//    private static final long REFRESH_TERM_MILLISEC = 10 * 1000;
//    private static final long REFRESH_TERM_MILLISEC = 24 * 60 * 60 * 1000;
    private static final long INVALID_REFRESH_TERM = -1;

    private NewsFeedArchiveUtils() {
        throw new AssertionError("You MUST not create this class!");
    }

    public static NewsFeed loadTopNewsFeed(Context context) {
        return loadTopNewsFeed(context, true);
    }

    public static NewsFeed loadTopNewsFeed(Context context, boolean shuffle) {
        SharedPreferences prefs = getSharedPreferences(context);

        String newsFeedStr = prefs.getString(KEY_TOP_NEWS_FEED, null);

        Type feedType = new TypeToken<NewsFeed>(){}.getType();
        NewsFeed newsFeed;
        if (newsFeedStr != null) {
            // cache된 내용 있는 경우. Gson으로 디코드 해서 사용.
            newsFeed = new Gson().fromJson(newsFeedStr, feedType);
            newsFeed.setDisplayingNewsIndex(0);
            if (shuffle){
                Collections.shuffle(newsFeed.getNewsList()); // 캐쉬된 뉴스들도 무조건 셔플
            }
        } else {
            // cache된 내용 없는 경우. 새로 만들어서 리턴.
            NewsFeedUrl url = NewsFeedUrlProvider.getInstance().getTopNewsFeedUrl();
            newsFeed = new NewsFeed();
            newsFeed.setNewsFeedUrl(url);
        }
        return newsFeed;
    }

    public static ArrayList<NewsFeed> loadBottomNews(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PANEL_MATRIX_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        int panelMatrixUniqueKey = prefs.getInt(PANEL_MATRIX_KEY, PANEL_MATRIX.getDefault().uniqueKey);
        PANEL_MATRIX panelMatrix = PANEL_MATRIX.getByUniqueKey(panelMatrixUniqueKey);

        prefs = getSharedPreferences(context);
        int bottomNewsFeedLargestIndex = prefs.getInt(KEY_BOTTOM_NEWS_FEED_LARGEST_INDEX, -1);
        ArrayList<NewsFeed> feedList = new ArrayList<>();
        if (bottomNewsFeedLargestIndex < 0) {
            // cache된 내용 없는 경우. 새로 만들어서 리턴.
            ArrayList<NewsFeedUrl> urlList =
                    NewsFeedUrlProvider.getInstance().getBottomNewsFeedUrlList();
            for (NewsFeedUrl newsFeedUrl : urlList) {
                NewsFeed newsFeed = new NewsFeed();
                newsFeed.setNewsFeedUrl(newsFeedUrl);
                feedList.add(newsFeed);

                if (feedList.size() >= panelMatrix.panelCount) {
                    break;
                }
            }
        } else {
            // cache된 내용 있는 경우. Gson으로 디코드 해서 사용.
            for (int i = 0; i <= bottomNewsFeedLargestIndex; i++) {
                NewsFeed newsFeed = loadBottomNewsFeedAt(prefs, i);
                if (newsFeed != null) {
                    newsFeed.setDisplayingNewsIndex(0);
                    Collections.shuffle(newsFeed.getNewsList()); // 캐쉬된 뉴스들도 무조건 셔플
                }
                feedList.add(newsFeed);

                if (feedList.size() >= panelMatrix.panelCount) {
                    break;
                }
            }
        }
        return feedList;
    }

    public static NewsFeed loadBottomNewsFeedAt(Context context, int position) {
        return loadBottomNewsFeedAt(getSharedPreferences(context), position);
    }

    public static NewsFeed loadBottomNewsFeedAt(SharedPreferences prefs, int position) {
        String key = getBottomNewsFeedKey(position);
        String bottomNewsFeedStr = prefs.getString(key, null);
        if (!prefs.contains(key) || bottomNewsFeedStr == null) {
            return null;
        }
        Type feedType = new TypeToken<NewsFeed>(){}.getType();
        return new Gson().fromJson(bottomNewsFeedStr, feedType);
    }

    public static void save(Context context, NewsFeed topNewsFeed,
                            ArrayList<NewsFeed> bottomNewsFeedList) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        putTopNewsFeed(editor, topNewsFeed);
        putBottomNewsFeedList(prefs, editor, bottomNewsFeedList);

        // save recent saved millisec
//        editor.putLong(KEY_NEWS_FEED_RECENT_REFRESH, System.currentTimeMillis());
        editor.apply();
    }

    public static void saveBottomNewsFeedAt(Context context, NewsFeed bottomNewsFeed, int position) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        int largestIndex = prefs.getInt(KEY_BOTTOM_NEWS_FEED_LARGEST_INDEX, -1);

        putBottomNewsFeed(editor, bottomNewsFeed, largestIndex, position);

        editor.apply();
    }

    public static void saveTopNewsFeed(Context context, NewsFeed newsFeed) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        putTopNewsFeed(editor, newsFeed);

        editor.apply();
    }

    private static void putTopNewsFeed(SharedPreferences.Editor editor, NewsFeed topNewsFeed) {
        String topNewsFeedStr = topNewsFeed != null ? new Gson().toJson(topNewsFeed) : null;

        // save top news feed
        if (topNewsFeedStr != null) {
            editor.putString(KEY_TOP_NEWS_FEED, topNewsFeedStr);
        } else {
            editor.remove(KEY_TOP_NEWS_FEED);
        }
    }

    public static void saveBottomNewsFeedList(Context context,
                                           ArrayList<NewsFeed> bottomNewsFeedList) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        putBottomNewsFeedList(prefs, editor, bottomNewsFeedList);

        editor.apply();
    }

    private static void putBottomNewsFeedList(SharedPreferences pref,
                                              SharedPreferences.Editor editor,
                                              ArrayList<NewsFeed> bottomNewsFeedList) {
        // save bottom news feed
        int size = bottomNewsFeedList.size();
        int largestIndex = pref.getInt(KEY_BOTTOM_NEWS_FEED_LARGEST_INDEX, -1);
        for (int i = 0; i < size; i++) {
            putBottomNewsFeed(editor, bottomNewsFeedList.get(i), largestIndex, i);
        }
    }

    private static void putBottomNewsFeed(SharedPreferences.Editor editor,
                                          NewsFeed bottomNewsFeed, int largestIndex, int position) {
        String bottomNewsFeedStr = new Gson().toJson(bottomNewsFeed);

        editor.putString(getBottomNewsFeedKey(position), bottomNewsFeedStr);

        if (position > largestIndex) {
            editor.putInt(KEY_BOTTOM_NEWS_FEED_LARGEST_INDEX, position);
        }
    }

    public static void saveRecentCacheMillisec(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(KEY_NEWS_FEED_RECENT_REFRESH, System.currentTimeMillis());

        editor.apply();
    }

    private static String getBottomNewsFeedKey(int position) {
        return KEY_BOTTOM_NEWS_FEED + position;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SP_KEY_NEWS_FEED, Context.MODE_PRIVATE);
    }

    public static boolean newsNeedsToBeRefreshed(Context context) {
        long currentMillisec = System.currentTimeMillis();

        long recentRefreshMillisec = getRecentRefreshMillisec(context);

        if (recentRefreshMillisec == INVALID_REFRESH_TERM) {
            return true;
        }

        long gap = currentMillisec - recentRefreshMillisec;

        return gap > REFRESH_TERM_MILLISEC;
    }

    public static long getRecentRefreshMillisec(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getLong(KEY_NEWS_FEED_RECENT_REFRESH, INVALID_REFRESH_TERM);
    }

    public static void clearArchive(Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }
}
