package com.yooiistudios.newsflow.core.connector;

import android.content.Context;

import com.yooiistudios.newsflow.core.util.Device;
import com.yooiistudios.newsflow.core.util.HttpUtil;
import com.yooiistudios.newsflow.core.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * ConnectorRequest
 *  커넥터 요청에 사용될 자료구조
 */
public abstract class ConnectorRequest<T extends ConnectorResult> {
    public interface ResultListener<S extends ConnectorResult> {
        public void onSuccess(S result);
        public void onFail(ConnectorResult result);
    }
    protected static final String KEY_AUTH = "auth";
    protected static final String KEY_APP_CODE = "app";
    protected static final String KEY_NAME = "name";
    protected static final String KEY_TOKEN = "token";
    protected static final String KEY_DATA = "data";

    protected static final String REQUEST_URL_BASE = "http://www.yooiisoft.com/data/connector/";

    private static final String APP_CODE = "newsflow";

    private Context mContext;
    private ResultListener<ConnectorResult> mListener;

    public ConnectorRequest(Context context, ResultListener<ConnectorResult> listener) {
        mContext = context;
        mListener = listener;
    }

    public T execute() throws ConnectorException {
        try {
            String result = HttpUtil.postJson(getRequestUrl(), toJsonObject());
            return getResult(result);
        } catch(IOException |JSONException e) {
            throw ConnectorException.createPostJsonException();
        }
    }

    protected abstract String getRequestUrl();
    protected abstract JSONObject configOnConvertingToJsonObject(JSONObject jsonObject) throws JSONException;
    protected abstract T getResult(String resultString) throws ConnectorException;

    public void handleResult(ConnectorResult result) {
        if (mListener != null) {
            if (result.isSuccess()) {
                mListener.onSuccess(result);
            } else {
                mListener.onFail(result);
            }
        }
    }

    public final JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_AUTH, UUID.getDeviceUuid(mContext));
        jsonObject.put(KEY_APP_CODE, APP_CODE);
        jsonObject = configOnConvertingToJsonObject(jsonObject);

        return jsonObject;
    }

    protected JSONObject putName(JSONObject jsonObject) throws JSONException {
        jsonObject.put(KEY_NAME, Device.Profile.getUserNameOrDefault(mContext));

        return jsonObject;
    }

    protected JSONObject putData(JSONObject jsonObject, String data) throws JSONException {
        jsonObject.put(KEY_DATA, data);

        return jsonObject;
    }

    protected JSONObject putToken(JSONObject jsonObject, String token) throws JSONException {
        jsonObject.put(KEY_TOKEN, token);

        return jsonObject;
    }
}
