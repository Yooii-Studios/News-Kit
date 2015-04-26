package com.yooiistudios.newskit.core.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * BaseConnectorResult
 *  커넥터 액션에 대한 결과 객체의 최소 단위
 */
public class TokenValidationResult extends ConnectorResult {
    private boolean mIsTokenValid;

    public TokenValidationResult(String message, int resultCode, boolean isTokenValid) {
        super(message, resultCode);
        mIsTokenValid = isTokenValid;
    }

    public boolean isTokenValid() {
        return mIsTokenValid;
    }

    public static TokenValidationResult fromResultString(String result) throws ConnectorException {
        try {
            JSONObject resultJson = new JSONObject(result);
            int resultCode = getResultCodeFromResultJson(resultJson);
            String message = getMessageFromResultJson(resultJson);
            boolean isTokenValid = getValidFromResultJson(resultJson);
            return new TokenValidationResult(message, resultCode, isTokenValid);
        } catch (JSONException e) {
            throw new ConnectorException();
        }
    }
}
