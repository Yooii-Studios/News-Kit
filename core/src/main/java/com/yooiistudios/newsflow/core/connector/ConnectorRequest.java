package com.yooiistudios.newsflow.core.connector;

import android.content.Context;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * ConnectorRequest
 *  커넥터 요청에 사용될 자료구조
 */
public abstract class ConnectorRequest<T extends ConnectorResult> {
    public interface ResultListener<S extends ConnectorResult> {
        public void onGetResult(S result);
        public void onFail(ConnectorResult result);
    }

    public Context context;
    public ResultListener<ConnectorResult> listener;

    public abstract T execute() throws ConnectorException;

    public void handleResult(ConnectorResult result) {
        if (listener != null) {
            if (result.resultCode == ConnectorResult.RC_SUCCESS) {
                listener.onGetResult(result);
            } else {
                listener.onFail(result);
            }
        }
    }
}
