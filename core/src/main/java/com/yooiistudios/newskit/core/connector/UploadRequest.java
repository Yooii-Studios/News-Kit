package com.yooiistudios.newskit.core.connector;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * UploadRequest
 *  커넥터 업로드 요청에 사용될 자료구조
 */
public class UploadRequest extends ConnectorRequest {
    private String mToken;
    private String mData;

    public UploadRequest(Context context, ResultListener<UploadResult> listener, String token,
                         String data) {
        super(context, listener);
        mToken = token;
        mData = data;
    }

    @Override
    protected String getRequestUrl() {
        return REQUEST_URL_BASE + "uploadfile.php";
    }

    @Override
    protected JSONObject configOnConvertingToJsonObject(JSONObject jsonObject) throws JSONException {
        jsonObject = putName(jsonObject);
        jsonObject = putData(jsonObject, mData);
        jsonObject = putToken(jsonObject, mToken);
        return jsonObject;
    }

    @Override
    protected ConnectorResult getResult(String resultString) throws ConnectorException {
        return UploadResult.fromResultString(resultString);
    }
}
