package com.yooiistudios.newsflow.core.connector;

/**
 * Created by Dongheyon Jeong in News Flow from Yooii Studios Co., LTD. on 15. 3. 11.
 *
 * ConnectorException
 *  커넥터 송수신에서 문제가 생긴 경우 예외
 */
public class ConnectorException extends Exception {
    public ConnectorException() {
    }

    public ConnectorException(String detailMessage) {
        super(detailMessage);
    }

    public static ConnectorException createPostJsonException() {
        return new ConnectorException("Error while posting json request");
    }
}
