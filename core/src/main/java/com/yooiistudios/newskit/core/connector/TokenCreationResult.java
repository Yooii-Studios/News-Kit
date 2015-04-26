package com.yooiistudios.newskit.core.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * BaseConnectorResult
 *  커넥터 액션에 대한 결과 객체의 최소 단위
 */
public class TokenCreationResult extends ConnectorResult {
    private String mToken;

    public TokenCreationResult(String message, int resultCode, String token) {
        super(message, resultCode);
        mToken = token;
    }

    public String getToken() {
        return mToken;
    }

    public static TokenCreationResult fromResultString(String result) throws ConnectorException {
        try {
            JSONObject resultJson = new JSONObject(result);
            int resultCode = getResultCodeFromResultJson(resultJson);
            String message = getMessageFromResultJson(resultJson);
            String token = null;
            if (resultCode == RC_SUCCESS) {
                token = resultJson.getString(KEY_TOKEN);
            }
            return new TokenCreationResult(message, resultCode, token);
        } catch (JSONException e) {
            throw new ConnectorException();
        }
    }
}
