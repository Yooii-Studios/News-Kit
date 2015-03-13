package com.yooiistudios.newsflow.core.connector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * ConnectorResult
 *  모든 커넥터 결과에 포함되는 기본 결과값
 */
public class ConnectorResult {
    public static final int RC_CONNECTOR_ERROR = -1;
    public static final int RC_SUCCESS = 0;
    public static final int RC_NETWORK_ERROR = 1;
    public static final int RC_EXCEEDED_DOWNLOAD_LIMIT = 2;
    public static final int RC_DUPLICATED_TOKEN = 3;
    public static final int RC_INVALID_UUID = 4;
    public static final int RC_EXCEEDED_UPLOAD_LIMIT = 5;
    public static final int RC_FILE_NOT_FOUND = 6;
    public static final int RC_TOKEN_CREATION_FAILED = 7;

    protected static final String KEY_RESULT_CODE = "result";
    protected static final String KEY_MESSAGE = "msg";
    protected static final String KEY_DATA = "data";
    protected static final String KEY_TOKEN = "token";
    protected static final String KEY_VALID = "valid";

    private String mMessage = "";
    private int mResultCode;

    public ConnectorResult(String message, int resultCode) {
        mMessage = message;
        mResultCode = resultCode;
    }

    public String getMessage() {
        return mMessage;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public boolean isSuccess() {
        return mResultCode == RC_SUCCESS;
    }

    protected static String getDataFromResultJson(JSONObject resultJson) throws JSONException {
        return resultJson.getString(KEY_DATA);
    }

    protected static String getMessageFromResultJson(JSONObject resultJson) throws JSONException {
        return resultJson.getString(KEY_MESSAGE);
    }

    protected static int getResultCodeFromResultJson(JSONObject resultJson) throws JSONException {
        return resultJson.getInt(KEY_RESULT_CODE);
    }

    protected static boolean getValidFromResultJson(JSONObject resultJson) throws JSONException {
        return resultJson.getInt(KEY_VALID) == 1;
    }

    public static ConnectorResult getErrorObject() {
        return new ConnectorResult("", RC_CONNECTOR_ERROR);
    }
}
