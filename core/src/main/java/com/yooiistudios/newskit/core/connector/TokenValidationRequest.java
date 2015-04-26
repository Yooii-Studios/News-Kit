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
public class TokenValidationRequest extends ConnectorRequest {
    private String mToken;

    public TokenValidationRequest(Context context,
                                  ResultListener<TokenValidationResult> listener,
                                  String token) {
        super(context, listener);
        mToken = token;
    }

    @Override
    protected String getRequestUrl() {
        return REQUEST_URL_BASE + "isuniquetokenvalid.php";
    }

    @Override
    protected JSONObject configOnConvertingToJsonObject(JSONObject jsonObject) throws JSONException {
        jsonObject = putToken(jsonObject, mToken);
        return jsonObject;
    }

    @Override
    protected ConnectorResult getResult(String resultString) throws ConnectorException {
        return TokenValidationResult.fromResultString(resultString);
    }
}
