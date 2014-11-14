package com.yooiistudios.news.util;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 12.
 *
 * ArrayUtils
 *  배열에 관한 편의기능을 제공하는 클래스
 */
public class ArrayUtils {
    public static <T> ArrayList<T> toArrayList(T[] array) {
        ArrayList<T> returnList = new ArrayList<T>(array.length);
        for (T item : array) {
            returnList.add(item);
        }

        return returnList;
    }
}
