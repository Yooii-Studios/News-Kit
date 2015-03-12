package com.yooiistudios.newsflow.core.util.connector;

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

    public String message = "";
    public int resultCode;

    public static ConnectorResult getErrorObject() {
        ConnectorResult result = new ConnectorResult();
        result.resultCode = RC_CONNECTOR_ERROR;

        return result;
    }
}
