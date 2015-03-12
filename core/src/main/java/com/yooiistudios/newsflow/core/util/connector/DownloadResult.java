package com.yooiistudios.newsflow.core.util.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * BaseConnectorResult
 *  커넥터 액션에 대한 결과 객체의 최소 단위
 */
public class DownloadResult extends ConnectorResult {
    public String base64Data;
    public static DownloadResult fromResultString(String result) throws ConnectorException {
        try {
            DownloadResult connectorResult = new DownloadResult();
            JSONObject resultJson = new JSONObject(result);
            connectorResult.resultCode = resultJson.getInt(KEY_RESULT_CODE);
            connectorResult.message = resultJson.getString(KEY_MESSAGE);
            connectorResult.base64Data = resultJson.getString(KEY_DATA);

            return connectorResult;
        } catch (JSONException e) {
            throw new ConnectorException();
        }
    }
}
