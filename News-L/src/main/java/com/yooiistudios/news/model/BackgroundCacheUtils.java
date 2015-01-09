package com.yooiistudios.news.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.yooiistudios.news.model.database.NewsDb;
import com.yooiistudios.news.model.news.News;
import com.yooiistudios.news.model.news.NewsFeed;
import com.yooiistudios.news.model.news.util.NewsFeedArchiveUtils;
import com.yooiistudios.news.model.news.NewsImageRequestQueue;
import com.yooiistudios.news.model.news.task.BottomNewsFeedFetchTask;
import com.yooiistudios.news.model.news.task.BottomNewsImageFetchTask;
import com.yooiistudios.news.model.news.task.TopFeedNewsImageUrlFetchTask;
import com.yooiistudios.news.model.news.task.TopNewsFeedFetchTask;
import com.yooiistudios.news.util.ImageMemoryCache;
import com.yooiistudios.news.util.NLLog;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * BackgroundCacheUtils
 *  모든 뉴스피드, 뉴스 이미지 캐싱을 담당하는 클래스
 */
public class BackgroundCacheUtils implements
        TopNewsFeedFetchTask.OnFetchListener,
        TopFeedNewsImageUrlFetchTask.OnTopFeedImageUrlFetchListener,
        BottomNewsFeedFetchTask.OnFetchListener {

    private static final String TAG = BackgroundCacheUtils.class.getName();

    private Context mContext;
    private SparseArray<AsyncTask> mTopNewsImageFetchTaskMap = null;
    private ArrayList<Integer> mBottomImageFetchMap;
    private ImageLoader mImageLoader;
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

    public void cache(Context context, OnCacheDoneListener listener) {
        mOnCacheDoneListener = listener;

        cache(context);
    }

    public void cache(Context context) {
        if (mIsRunning) {
            NLLog.i(TAG, "Already caching...");
            return;
        }

//        long recentRefreshMillisec = NewsFeedArchiveUtils.getRecentRefreshMillisec(context);
//        if (recentRefreshMillisec != NewsFeedArchiveUtils.INVALID_REFRESH_TERM
//                &&
//                System.currentTimeMillis() - recentRefreshMillisec < BackgroundServiceUtils.CACHE_INTERVAL_DAILY) {
//            return;
//        }
        NLLog.i(TAG, "Start caching...");
        mIsRunning = true;

        mContext = context;
        mImageLoader = new ImageLoader(NewsImageRequestQueue.getInstance(context).getRequestQueue(),
                ImageMemoryCache.getInstance(context));
        mTopNewsImageFetchTaskMap = new SparseArray<AsyncTask>();

        // cache top news feed
        NewsFeed topNewsFeed = NewsDb.getInstance(mContext).loadTopNewsFeed(mContext);
//        NewsFeed topNewsFeed = NewsFeedArchiveUtils.loadTopNewsFeed(mContext);
        new TopNewsFeedFetchTask(topNewsFeed, this, TopNewsFeedFetchTask.TaskType.CACHE, true)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // cache bottom news feed list
        mBottomImageFetchMap = new ArrayList<>();
        ArrayList<NewsFeed> bottomNewsFeedList = NewsDb.getInstance(mContext).loadBottomNewsFeedList(mContext);
//        ArrayList<NewsFeed> bottomNewsFeedList = NewsFeedArchiveUtils.loadBottomNewsFeedList(mContext);
        int count = bottomNewsFeedList.size();
        for (int i = 0; i < count; i++) {
            NewsFeed bottomNewsFeed = bottomNewsFeedList.get(i);
            if (bottomNewsFeed == null) {
                continue;
            }

            new BottomNewsFeedFetchTask(bottomNewsFeed, i, BottomNewsFeedFetchTask.TASK_CACHE, this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            // map for image fetch
            mBottomImageFetchMap.add(i);
        }
    }

    private boolean checkAllImageUrlFetched(NewsFeed newsFeed) {
        for (News news : newsFeed.getNewsList()) {
            if (!news.isImageUrlChecked()) {
                return false;
            }
        }

        return true;
    }

    private void checkAllFetched() {
        if (mTopNewsImageFetchTaskMap != null) {
            NLLog.i(TAG, "top img count remaining : " + mTopNewsImageFetchTaskMap.size());
        }
        NLLog.i(TAG, "bottom img count remaining : " + mBottomImageFetchMap.size());
        for (Integer pos : mBottomImageFetchMap) {
            NLLog.i(TAG, "bottom img remaining idx : " + pos);
        }
        if (mTopNewsImageFetchTaskMap.size() == 0 &&
                mBottomImageFetchMap.size() == 0) {
            NLLog.i(TAG, "All cached. Saving recent cache time...");
            NewsFeedArchiveUtils.saveRecentCacheMillisec(mContext);

            if (mOnCacheDoneListener != null) {
                mOnCacheDoneListener.onDone();
            }

            mIsRunning = false;
        }
    }

    @Override
    public void onTopNewsFeedFetch(final NewsFeed newsFeed,
                                   TopNewsFeedFetchTask.TaskType taskType) {
        NLLog.i(TAG, "T NF");
        NewsDb.getInstance(mContext).saveTopNewsFeed(newsFeed);
//        NewsFeedArchiveUtils.saveTopNewsFeed(mContext, newsFeed);

        if (newsFeed == null) {
            checkAllFetched();
            return;
        }

        for (int i = 0; i < newsFeed.getNewsList().size(); i++) {
            News news = newsFeed.getNewsList().get(i);
            TopFeedNewsImageUrlFetchTask task = new TopFeedNewsImageUrlFetchTask(
                    news, i, TopFeedNewsImageUrlFetchTask.TaskType.CACHE, new TopFeedNewsImageUrlFetchTask.OnTopFeedImageUrlFetchListener() {
                @Override
                public void onTopFeedImageUrlFetch(News news, String url, int position, TopFeedNewsImageUrlFetchTask.TaskType taskType) {
                    NLLog.i(TAG, "T NFI " + position);
                    news.setImageUrlChecked(true);
                    mTopNewsImageFetchTaskMap.delete(position);

                    if (checkAllImageUrlFetched(newsFeed)) {
                        NewsDb.getInstance(mContext).saveTopNewsFeed(newsFeed);
//                        NewsFeedArchiveUtils.saveTopNewsFeed(mContext, newsFeed);
                    }

                    checkAllFetched();

                    if (url != null) {
                        news.setImageUrl(url);
                        mImageLoader.get(url, new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                    }
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mTopNewsImageFetchTaskMap.put(i, task);
        }
    }

    @Override
    public void onTopFeedImageUrlFetch(News news, String url, int position, TopFeedNewsImageUrlFetchTask.TaskType taskType) {
        NLLog.i(TAG, "T NFI " + position);
        news.setImageUrlChecked(true);
        mTopNewsImageFetchTaskMap.delete(position);
        checkAllFetched();

        if (url != null) {
            news.setImageUrl(url);
            mImageLoader.get(url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
    }

    @Override
    public void onBottomNewsFeedFetch(final NewsFeed newsFeed, final int newsFeedPosition,
                                      int taskType) {
        NLLog.i(TAG, "B NF " + newsFeedPosition);
        NewsDb.getInstance(mContext).saveBottomNewsFeedAt(newsFeed, newsFeedPosition);
//        NewsFeedArchiveUtils.saveBottomNewsFeedAt(mContext, newsFeed, newsFeedPosition);

        if (newsFeed == null) {
            mBottomImageFetchMap.remove(Integer.valueOf(newsFeedPosition));

            checkAllFetched();
            return;
        }

        for (int i = 0; i < newsFeed.getNewsList().size(); i++) {
            News news = newsFeed.getNewsList().get(i);
            new BottomNewsImageFetchTask(mImageLoader, news, i,
                    BottomNewsImageFetchTask.TASK_CACHE, new BottomNewsImageFetchTask.OnBottomImageUrlFetchListener() {
                @Override
                public void onBottomImageUrlFetchSuccess(News news, String url, int position, int taskType) {
                    NLLog.i(TAG, "B NFI " + newsFeedPosition + " " + position);
                    news.setImageUrlChecked(true);
                    if (url != null) {
                        news.setImageUrl(url);
                    }

                    if (checkAllImageUrlFetched(newsFeed)) {
                        // archive

                        NewsDb.getInstance(mContext).saveBottomNewsFeedAt(newsFeed, newsFeedPosition);
//                        NewsFeedArchiveUtils.saveBottomNewsFeedAt(mContext,
//                                newsFeed, newsFeedPosition);
                        mBottomImageFetchMap.remove(Integer.valueOf(newsFeedPosition));

                        checkAllFetched();
                    }
                }

                @Override
                public void onFetchImage(News news, int position, int taskType) {

                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public interface OnCacheDoneListener {
        public void onDone();
    }
}
