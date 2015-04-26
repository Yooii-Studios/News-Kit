package com.yooiistudios.newskit.core.connector;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * GetUniqueTokenRequest
 *  커넥터 업로드 요청에 사용될 자료구조
 */
public class TokenCreationRequest extends ConnectorRequest {
    public TokenCreationRequest(Context context, ResultListener<TokenCreationResult> listener) {
        super(context, listener);
    }

    @Override
    protected String getRequestUrl() {
        return REQUEST_URL_BASE + "getuniquetoken.php";
    }

    @Override
    protected JSONObject configOnConvertingToJsonObject(JSONObject jsonObject) throws JSONException {
        return jsonObject;
    }

    @Override
    protected ConnectorResult getResult(String resultString) throws ConnectorException {
        return TokenCreationResult.fromResultString(resultString);
    }
}
