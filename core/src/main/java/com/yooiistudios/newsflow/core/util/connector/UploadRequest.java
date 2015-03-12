package com.yooiistudios.newsflow.core.util.connector;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * UploadRequest
 *  커넥터 업로드 요청에 사용될 자료구조
 */
public class UploadRequest extends ConnectorRequest {
    public String token;
    public byte[] bytes;
//    public ResultListener<UploadResult> listener;

    @Override
    public ConnectorResult execute() throws ConnectorException {
        return Connector.requestUpload(this);
    }
}
