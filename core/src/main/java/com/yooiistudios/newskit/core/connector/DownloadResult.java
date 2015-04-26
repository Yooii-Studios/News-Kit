package com.yooiistudios.newskit.core.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * BaseConnectorResult
 *  커넥터 액션에 대한 결과 객체의 최소 단위
 */
public class DownloadResult extends ConnectorResult {
    private String mData;

    public DownloadResult(String message, int resultCode, String data) {
        super(message, resultCode);
        mData = data;
    }

    public String getData() {
        return mData;
    }

    public static DownloadResult fromResultString(String result) throws ConnectorException {
        try {
            JSONObject resultJson = new JSONObject(result);
            int resultCode = getResultCodeFromResultJson(resultJson);
            String message = getMessageFromResultJson(resultJson);

            String data = null;
            if (resultCode == RC_SUCCESS) {
                data = getDataFromResultJson(resultJson);
            }

            return new DownloadResult(message, resultCode, data);
        } catch (JSONException e) {
            throw new ConnectorException();
        }
    }
}
