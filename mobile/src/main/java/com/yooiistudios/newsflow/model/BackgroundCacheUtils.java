package com.yooiistudios.newsflow.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.android.volley.VolleyError;
import com.yooiistudios.newsflow.core.cache.volley.CacheImageLoader;
import com.yooiistudios.newsflow.core.news.News;
import com.yooiistudios.newsflow.core.news.NewsFeed;
import com.yooiistudios.newsflow.core.news.database.NewsDb;
import com.yooiistudios.newsflow.core.news.task.NewsFeedFetchTask;
import com.yooiistudios.newsflow.core.news.task.NewsImageUrlFetchTask;
import com.yooiistudios.newsflow.core.news.util.NewsFeedArchiveUtils;
import com.yooiistudios.newsflow.core.news.util.NewsFeedFetchUtil;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.newsflow.core.util.NLLog;
import com.yooiistudios.newsflow.model.cache.NewsImageLoader;
import com.yooiistudios.newsflow.model.cache.NewsUrlSupplier;
import com.yooiistudios.newsflow.util.NotificationUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * BackgroundCacheUtils
 *  모든 뉴스피드, 뉴스 이미지 캐싱을 담당하는 클래스
 */
public class BackgroundCacheUtils implements
        NewsFeedFetchTask.OnFetchListener,
        NewsImageUrlFetchTask.OnImageUrlFetchListener {
    public interface OnCacheDoneListener {
        void onDone();
    }

    private Context mContext;
    private SparseArray<NewsFeedFetchTask> mNewsFeedFetchTasks;
    private SparseArray<HashMap<String, NewsImageUrlFetchTask>> mNewsImageUrlFetchTasks;
    private SparseArray<NewsFeed> mNewsFeeds;
    private NewsImageLoader mImageLoader;
    private BackgroundServiceUtils.CacheTime mCacheTime;
    private OnCacheDoneListener mOnCacheDoneListener;

    private static BackgroundCacheUtils instance;
    private boolean mIsRunning = false;

    public static BackgroundCacheUtils getInstance() {
        if (instance == null) {
            instance = new BackgroundCacheUtils();
        }

        return instance;
    }

    private BackgroundCacheUtils() { }

    public void cache(Context context, BackgroundServiceUtils.CacheTime cacheTime,
                      OnCacheDoneListener listener) {
        mOnCacheDoneListener = listener;
        mCacheTime = cacheTime;

        cache(context);
    }

    private void cache(Context context) {
        if (mIsRunning) {
            return;
        }
        mIsRunning = true;
        mContext = context;

        if (BackgroundServiceUtils.DEBUG) {
            notifyCacheDone();
            return;
        }
        mImageLoader = NewsImageLoader.createWithNonRetainingCache(context);
        mNewsFeedFetchTasks = new SparseArray<>();
        mNewsImageUrlFetchTasks = new SparseArray<>();
        mNewsFeeds = new SparseArray<>();

        fetchTopNewsFeed();
        fetchBottomNewsFeeds(context);
    }

    private void fetchTopNewsFeed() {
        NewsFeed topNewsFeed = NewsDb.getInstance(mContext).loadTopNewsFeed(mContext);
        NewsFeedFetchTask task = new NewsFeedFetchTask(topNewsFeed, this, NewsFeed.INDEX_TOP,
                NewsFeedFetchUtil.FETCH_LIMIT_TOP);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mNewsFeedFetchTasks.put(NewsFeed.INDEX_TOP, task);
    }

    private void fetchBottomNewsFeeds(Context context) {
        PanelMatrix currentMatrix = PanelMatrixUtils.getCurrentPanelMatrix(context);
        ArrayList<NewsFeed> bottomNewsFeedList =
                NewsDb.getInstance(mContext).loadBottomNewsFeedList(mContext, currentMatrix.getPanelCount());

        int count = bottomNewsFeedList.size();
        for (int i = 0; i < count; i++) {
            NewsFeed bottomNewsFeed = bottomNewsFeedList.get(i);
            if (bottomNewsFeed == null) {
                continue;
            }
            NewsFeedFetchTask task = new NewsFeedFetchTask(bottomNewsFeed, this, i,
                    NewsFeedFetchUtil.FETCH_LIMIT_BOTTOM);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mNewsFeedFetchTasks.put(i, task);
        }
    }

    private void configOnFetchNewsFeed(NewsFeed newsFeed, int position) {
        archiveNewsFeed(newsFeed, position);
        notifyNewsFeedFetched(newsFeed, position);
    }

    private void archiveNewsFeed(NewsFeed newsFeed, int position) {
        if (position == NewsFeed.INDEX_TOP) {
            NewsDb.getInstance(mContext).saveTopNewsFeed(newsFeed);
        } else {
            NewsDb.getInstance(mContext).saveBottomNewsFeedAt(newsFeed, position);
        }
    }

    private void notifyNewsFeedFetched(NewsFeed newsFeed, int position) {
        mNewsFeeds.put(position, newsFeed);
        mNewsFeedFetchTasks.remove(position);

        if (mNewsFeedFetchTasks.size() == 0) {
            fetchImageUrls();
        }
    }

    private void fetchImageUrls() {
        for (int i = 0; i < mNewsFeeds.size(); i++) {
            int newsFeedIndex = mNewsFeeds.keyAt(i);
            NewsFeed newsFeed = mNewsFeeds.get(newsFeedIndex);
            HashMap<String, NewsImageUrlFetchTask> newsGuidMap = new HashMap<>();
            for (News news : newsFeed.getNewsList()) {
                NewsImageUrlFetchTask imageUrlTask = new NewsImageUrlFetchTask(news, this, newsFeedIndex);
                imageUrlTask.execute();
                newsGuidMap.put(news.getGuid(), imageUrlTask);
            }
            if (newsGuidMap.size() > 0) {
                mNewsImageUrlFetchTasks.put(newsFeedIndex, newsGuidMap);
            }
        }
        checkAllImagesFetched();
    }

    private void archiveNewsImageUrl(News news, String url, int newsFeedPosition) {
        if (newsFeedPosition == NewsFeed.INDEX_TOP) {
            NewsDb.getInstance(mContext).saveTopNewsImageUrlWithGuid(url, news.getGuid());
        } else {
            NewsDb.getInstance(mContext).saveBottomNewsImageUrlWithGuid(
                    url, newsFeedPosition, news.getGuid());
        }
    }

    private void cacheImages(final News news, final int newsFeedPosition) {
        mImageLoader.get(new NewsUrlSupplier(news, newsFeedPosition),
                new CacheImageLoader.ImageListener() {
                    @Override
                    public void onSuccess(CacheImageLoader.ImageResponse response) {
                        notifyImageFetched(news, newsFeedPosition);
                    }

                    @Override
                    public void onFail(VolleyError error) {
                        notifyImageFetched(news, newsFeedPosition);
                    }
                });
    }

    private void notifyImageFetched(News news, int newsFeedPosition) {
        HashMap<String, NewsImageUrlFetchTask> imageFetchMap
                = mNewsImageUrlFetchTasks.get(newsFeedPosition);
        imageFetchMap.remove(news.getGuid());
        NLLog.now("News image left at " + newsFeedPosition + ": " + imageFetchMap.size());
        if (imageFetchMap.size() == 0) {
            mNewsImageUrlFetchTasks.remove(newsFeedPosition);
        }
        checkAllImagesFetched();
    }

    private void checkAllImagesFetched() {
        if (mNewsImageUrlFetchTasks.size() == 0) {
            NewsFeedArchiveUtils.saveRecentCacheMillisec(mContext);
            notifyCacheDone();
        }
    }

    private void notifyCacheDone() {
        if (BackgroundServiceUtils.CacheTime.isTimeToIssueNotification(mCacheTime)) {
            NotificationUtils.issueAsync(mContext);
        }
        if (mOnCacheDoneListener != null) {
            mOnCacheDoneListener.onDone();
        }

        mIsRunning = false;
    }

    @Override
    public void onFetchNewsFeed(NewsFeed newsFeed, int position) {
        NLLog.now("NewsFeed: " + position);
        configOnFetchNewsFeed(newsFeed, position);
    }

    @Override
    public void onFetchImageUrl(News news, String url, int newsFeedPosition) {
        archiveNewsImageUrl(news, url, newsFeedPosition);
        cacheImages(news, newsFeedPosition);
    }
}
