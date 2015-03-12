package com.yooiistudios.newsflow.core.news.util;

import android.util.Base64;

import com.yooiistudios.newsflow.core.news.RssFetchable;
import com.yooiistudios.newsflow.core.util.ObjectConverter;

import java.io.IOException;
import java.util.List;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 12.
 *
 * RssFetchableConverter
 *  RssFetchable 객체를 다른 형태로 변형해주는 유틸
 */
public class RssFetchableConverter {
    private RssFetchableConverter() {
        throw new AssertionError("You MUST NOT create the instance of this class!!");
    }

    public static String toBase64String(RssFetchable fetchable) throws RssFetchableConvertException {
        try {
            byte[] dataBytes = ObjectConverter.toByteArray(fetchable);
            return Base64.encodeToString(dataBytes, Base64.NO_WRAP);
        } catch (IOException e) {
            throw new RssFetchableConvertException();
        }
    }

    public static RssFetchable toRssFetchable(String base64String) throws RssFetchableConvertException {
        try {
            byte[] dataBytes = Base64.decode(base64String, Base64.NO_WRAP);
            return (RssFetchable)ObjectConverter.fromByteArray(dataBytes);
        } catch (IOException|ClassNotFoundException e) {
            throw new RssFetchableConvertException();
        }
    }

//    public static String rssFetchablesToBase64String(List<RssFetchable> fetchables) {
//        for (RssFetchable fetchable : fetchables) {
//
//        }
//    }

    public static class RssFetchableConvertException extends Exception {}
}
