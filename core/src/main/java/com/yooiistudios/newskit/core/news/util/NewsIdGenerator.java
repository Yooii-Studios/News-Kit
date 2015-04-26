package com.yooiistudios.newskit.core.news.util;

import com.yooiistudios.newskit.core.util.IntegerMath;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 10.
 *
 * NewsIdGenerator
 *  뉴스피드 index, 뉴스 index 로부터 Unique Id 를 생성
 */
public class NewsIdGenerator {
    private static final int DIGIT_PER_ID = 8;
    // MAX_ID_VALUE : 255
    private static final int MAX_ID_VALUE = (int)Math.pow(2, DIGIT_PER_ID) - 1;
    private static final int MIN_ID_VALUE = 0;

    public static String generateKey(int newsFeedPosition, int newsPosition) {
        checkPositionRange(newsFeedPosition);
        checkPositionRange(newsPosition);

        String newsFeedId = IntegerMath.convertToBitPresentation(newsFeedPosition);
        String newsId = IntegerMath.convertToBitPresentation(newsPosition);

        return newsFeedId + newsId;
    }

    private static void checkPositionRange(int position){
        if (position < MIN_ID_VALUE || position > MAX_ID_VALUE) {
            throw new RuntimeException();
        }
    }
}
