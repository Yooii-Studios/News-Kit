package com.yooiistudios.newsflow.core.util.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * BaseConnectorResult
 *  커넥터 액션에 대한 결과 객체의 최소 단위
 */
public class UploadResult extends ConnectorResult {
    public static UploadResult fromResultString(String result) throws ConnectorException {
        try {
            UploadResult connectorResult = new UploadResult();
            JSONObject resultJson = new JSONObject(result);
            connectorResult.resultCode = resultJson.getInt(KEY_RESULT_CODE);
            connectorResult.message = resultJson.getString(KEY_MESSAGE);

            return connectorResult;
        } catch (JSONException e) {
            throw new ConnectorException();
        }
    }
}
