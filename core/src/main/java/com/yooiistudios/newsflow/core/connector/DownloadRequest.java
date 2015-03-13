package com.yooiistudios.newsflow.core.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * UploadRequest
 *  커넥터 업로드 요청에 사용될 자료구조
 */
public class DownloadRequest extends ConnectorRequest {
    public String token;

    @Override
    protected String getRequestUrl() {
        return REQUEST_URL_BASE + "downloadfile.php";
    }

    @Override
    protected ConnectorResult getResult(String resultString) throws ConnectorException {
        return DownloadResult.fromResultString(resultString);
    }

    @Override
    protected JSONObject configOnConvertingToJsonObject(JSONObject jsonObject) throws JSONException {
        jsonObject = putName(jsonObject);
        jsonObject = putToken(jsonObject, token);
        return jsonObject;
    }
}
