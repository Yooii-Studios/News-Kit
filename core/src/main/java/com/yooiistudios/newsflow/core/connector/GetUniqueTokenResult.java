package com.yooiistudios.newsflow.core.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * BaseConnectorResult
 *  커넥터 액션에 대한 결과 객체의 최소 단위
 */
public class GetUniqueTokenResult extends ConnectorResult {
    public String token;
    public static GetUniqueTokenResult fromResultString(String result) throws ConnectorException {
        try {
            GetUniqueTokenResult connectorResult = new GetUniqueTokenResult();
            JSONObject resultJson = new JSONObject(result);
            connectorResult.resultCode = resultJson.getInt(KEY_RESULT_CODE);
            connectorResult.message = resultJson.getString(KEY_MESSAGE);
            if (connectorResult.resultCode == RC_SUCCESS) {
                connectorResult.token = resultJson.getString(KEY_TOKEN);
            }

            return connectorResult;
        } catch (JSONException e) {
            throw new ConnectorException();
        }
    }
}
