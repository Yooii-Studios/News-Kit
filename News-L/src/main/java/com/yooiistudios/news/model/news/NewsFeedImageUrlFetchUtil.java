package com.yooiistudios.news.model.news;

import com.yooiistudios.news.util.log.NLLog;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 21.
 *
 * NLNewsFeedImageUrlFetchUtil
 *  뉴스에서 이미지를 가져옴
 */
public class NewsFeedImageUrlFetchUtil {
    /**
     * 뉴스의 링크를 사용, 해당 링크 내에서 뉴스를 대표할 만한 이미지의 url을 추출한다.
     * 사용할 수 있는 이미지가 있는 경우 파라미터로 받은 news 인스턴스에 추가하고 아니면 아무것도
     * 하지 않는다.
     * 네트워크를 사용하므로 UI Thread에서는 부르지 말자.
     * @param news NLNews to set ImageUrl. May be null if there's no image src.
     */
    // Future use의 가능성이 있기 때문에 메서드로 빼놓음.
    public static String getImageUrl(News news) {
        // 뉴스의 링크를 읽음
        String originalLinkSource = null;
        try {
            long startMilli;
            long endMilli;

            startMilli = System.currentTimeMillis();
            originalLinkSource = NewsFeedUtils.requestHttpGet_(
                    news.getLink());
            endMilli = System.currentTimeMillis();
            NLLog.i("performance", "NLNewsFeedUtil.requestHttpGet : " +
                    (endMilli - startMilli));

        } catch(Exception e) {
            e.printStackTrace();
        }

        String imgUrl = null;
        if (originalLinkSource != null) {
            // 링크를 읽었다면 거기서 이미지를 추출.
            // 이미지는 두 장 이상 필요하지 않을것 같아서 우선 한장만 뽑도록 해둠.
            // future use를 생각해 구조는 리스트로 만들어 놓음.
            imgUrl = NewsFeedUtils.getImageUrl(
                    originalLinkSource);
        }

        return imgUrl;
    }
}
