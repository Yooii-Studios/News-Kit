package com.yooiistudios.newskit.core.util;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 12.
 *
 * ArrayUtils
 *  배열에 관한 편의기능을 제공하는 클래스
 */
public class ArrayUtils {
    public static <T> ArrayList<T> toArrayList(T[] array) {
        ArrayList<T> returnList = new ArrayList<>(array.length);
        Collections.addAll(returnList, array);

        return returnList;
    }

    /**
     * SparseArray 의 key(index) 순서대로 ArrayList 를 만들어 준다.
     * SparseArray 에 빈 공간(0, 1, 3의 경우 2가 없음)은 무시하고 trim 한다.
     * @param sparseArray ArrayList 로 변환할 SparseArray
     * @return 순서대로 trim 된 ArrayList
     */
    public static <T> ArrayList<T> toArrayList(SparseArray<T> sparseArray) {
        ArrayList<T> arrayList = new ArrayList<>();

        int taskCount = sparseArray.size();
        for (int i = 0; i < taskCount; i++) {
            int key = sparseArray.keyAt(i);
            T value = sparseArray.get(key);
            arrayList.add(value);
        }

        return arrayList;
    }
}
