package com.yooiistudios.newsflow.core.util.connector;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

import com.yooiistudios.newsflow.core.util.Device;
import com.yooiistudios.newsflow.core.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * Connector
 *  유이 커넥터 통신
 */
public class Connector {
    protected static final String KEY_AUTH = "auth";
    protected static final String KEY_APP_CODE = "app";
    protected static final String KEY_NAME = "name";
    protected static final String KEY_TOKEN = "token";
    protected static final String KEY_DATA = "data";

    private static final int CONNECTION_TIMEOUT = 2500;
    private static final int SOCKET_TIMEOUT = 3500;

    private static final String URL_UPLOAD =
            "http://www.yooiisoft.com/data/connector/uploadfile.php";
    private static final String URL_DOWNLOAD =
            "http://www.yooiisoft.com/data/connector/downloadfile.php";
    private static final String URL_GET_UNIQUE_TOKEN =
            "http://www.yooiisoft.com/data/connector/getuniquetoken.php";
    private static final String APP_CODE = "newsflow";

    public static void upload(UploadRequest request) {
        new ConnectorTask(request).execute();
    }

//    public static void download(DownloadRequest request) {
//        new ConnectorTask(request).execute();
//    }

    public static void getUniqueToken(GetUniqueTokenRequest request) {
        new ConnectorTask(request).execute();
    }

    protected static UploadResult requestUpload(UploadRequest request)
            throws ConnectorException {
        try {
            JSONObject json = new JSONObject();
            putAuth(request.context, json);
            putAppCode(json);
            putDeviceName(request.context, json);
            putToken(request.token, json);
            putDataBytes(request.bytes, json);
            String result = postJson(URL_UPLOAD, json);

            return UploadResult.fromResultString(result);
        } catch(IOException|JSONException e) {
            throw ConnectorException.createPostJsonException();
        }
    }

//    protected static DownloadResult requestDownload(DownloadRequest request)
//            throws ConnectorException {
//        try {
//            JSONObject json = new JSONObject();
//            putAuth(context, json);
//            putAppCode(json);
//            putDeviceName(context, json);
//            putToken(token, json);
//            String result = postJson(URL_DOWNLOAD, json);
//
//            return DownloadResult.fromResultString(result);
//        } catch(IOException|JSONException e) {
//            throw new ConnectorException();
//        }
//    }

    protected static GetUniqueTokenResult requestGetUniqueToken(GetUniqueTokenRequest request)
            throws ConnectorException {
        try {
            JSONObject json = new JSONObject();
            putAuth(request.context, json);
            String result = postJson(URL_GET_UNIQUE_TOKEN, json);

            return GetUniqueTokenResult.fromResultString(result);
        } catch(IOException|JSONException e) {
            throw ConnectorException.createPostJsonException();
        }
    }

    private static void putDataBytes(byte[] bytes, JSONObject json) throws JSONException {
        String encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);
        json.put(KEY_DATA, encoded);
    }

    private static void putAuth(Context context, JSONObject json) throws JSONException {
        json.put(KEY_AUTH, UUID.getDeviceUuid(context));
    }

    private static void putAppCode(JSONObject json) throws JSONException {
        json.put(KEY_APP_CODE, APP_CODE);
    }

    private static void putDeviceName(Context context, JSONObject json) throws JSONException {
        json.put(KEY_NAME, Device.Profile.getUserName(context));
    }

    private static void putToken(String token, JSONObject json) throws JSONException {
        json.put(KEY_TOKEN, token);
    }

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

    private static class ConnectorTask extends AsyncTask<Void, Void, ConnectorResult> {
        private ConnectorRequest mRequest;

        public ConnectorTask(ConnectorRequest request) {
            mRequest = request;
        }

        @Override
        protected ConnectorResult doInBackground(Void... params) {
            ConnectorResult result;
            try {
                result = mRequest.execute();
            } catch (ConnectorException e) {
                e.printStackTrace();
                result = ConnectorResult.getErrorObject();
            }
            return result;
        }

        @Override
        protected void onPostExecute(ConnectorResult result) {
            super.onPostExecute(result);

            mRequest.handleResult(result);
        }
    }
}
