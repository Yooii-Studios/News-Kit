package com.yooiistudios.newsflow.core.news;

import android.content.Context;

import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrixUtils;

import java.util.ArrayList;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 3. 4.
 *
 * DefaultNewsFeedProvider
 *  기본 NewsFeed 를 제공하는 클래스
 */
public class DefaultNewsFeedProvider {
    public static NewsFeed getDefaultTopNewsFeed(Context context) {
        NewsTopic defaultNewsTopic = NewsFeedDefaultUrlProvider.getInstance(context).getTopNewsTopic();
        return new NewsFeed(defaultNewsTopic);
    }

    public static ArrayList<NewsFeed> getDefaultBottomNewsFeedList(Context context) {
        ArrayList<NewsFeed> newsFeedList = new ArrayList<>();
        ArrayList<NewsTopic> topicList =
                NewsFeedDefaultUrlProvider.getInstance(context).getBottomNewsTopicList();

        for (NewsTopic newsTopic : topicList) {
            newsFeedList.add(new NewsFeed(newsTopic));
        }

        PanelMatrix panelMatrix = PanelMatrixUtils.getCurrentPanelMatrix(context);

        int topicSize = topicList.size();
        if (topicSize > panelMatrix.getPanelCount()) {
            newsFeedList = new ArrayList<>(newsFeedList.subList(0, panelMatrix.getPanelCount()));
        } else if (topicSize < panelMatrix.getPanelCount()) {
            // 부족한 만큼 마지막 토픽을 더 채워준다
            // topicSize = 6, panelSize = 8일 경우
            int difference = panelMatrix.getPanelCount() - topicSize;
            for (int i = 0; i < difference; i++) {
                newsFeedList.add(new NewsFeed(topicList.get(topicSize - 1)));
            }
        }
        return newsFeedList;
    }
}
