package com.yooiistudios.newskit.core.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * BaseConnectorResult
 *  커넥터 액션에 대한 결과 객체의 최소 단위
 */
public class UploadResult extends ConnectorResult {
    public UploadResult(String message, int resultCode) {
        super(message, resultCode);
    }

    public static UploadResult fromResultString(String result) throws ConnectorException {
        try {
            JSONObject resultJson = new JSONObject(result);
            int resultCode = getResultCodeFromResultJson(resultJson);
            String message = getMessageFromResultJson(resultJson);

            return new UploadResult(message, resultCode);
        } catch (JSONException e) {
            throw new ConnectorException();
        }
    }
}
