package com.yooiistudios.newsflow.core.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * UploadRequest
 *  커넥터 업로드 요청에 사용될 자료구조
 */
public class UploadRequest extends ConnectorRequest {
    public String token;
    public String data;

    @Override
    protected String getRequestUrl() {
        return REQUEST_URL_BASE + "uploadfile.php";
    }

    @Override
    protected ConnectorResult getResult(String resultString) throws ConnectorException {
        return UploadResult.fromResultString(resultString);
    }

    @Override
    protected JSONObject configOnConvertingToJsonObject(JSONObject jsonObject) throws JSONException {
        jsonObject = putName(jsonObject);
        jsonObject = putData(jsonObject, data);
        jsonObject = putToken(jsonObject, token);
        return jsonObject;
    }
}
