package com.yooiistudios.newskit.core.util;

import com.yooiistudios.newskit.core.connector.ConnectorException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 13.
 *
 * HttpUtil
 *  Http 연결 요청을 처리하는 유틸
 */
public class HttpUtil {
    private static final int CONNECTION_TIMEOUT = 2500;
    private static final int SOCKET_TIMEOUT = 3500;

    public static String postJson(String endpoint, JSONObject json) throws IOException, ConnectorException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), SOCKET_TIMEOUT);

        HttpPost postMethod = new HttpPost(endpoint);
        postMethod.setEntity(new StringEntity(json.toString(), "UTF-8"));

        HttpResponse response = httpClient.execute(postMethod);

        int status = response.getStatusLine().getStatusCode();
        if (status != 200) {
            throw new ConnectorException("Response status code: " + status);
        }
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }
}
