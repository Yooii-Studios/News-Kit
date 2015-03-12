package com.yooiistudios.newsflow.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.connector.Connector;
import com.yooiistudios.newsflow.core.connector.ConnectorRequest;
import com.yooiistudios.newsflow.core.connector.ConnectorResult;
import com.yooiistudios.newsflow.core.connector.DownloadRequest;
import com.yooiistudios.newsflow.core.connector.DownloadResult;
import com.yooiistudios.newsflow.core.connector.GetUniqueTokenRequest;
import com.yooiistudios.newsflow.core.connector.GetUniqueTokenResult;
import com.yooiistudios.newsflow.core.util.NLLog;
import com.yooiistudios.newsflow.model.PairingTask;

public class PairActivity extends Activity {
    private TextView mPairTokenTextView;
    private PairingTask mPairingTask;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);

        initViews();
        requestToken();
    }

    private void initViews() {
        mPairTokenTextView = (TextView)findViewById(R.id.pair_token);
    }

    private void requestToken() {
        GetUniqueTokenRequest request = new GetUniqueTokenRequest();
        request.context = this;
        request.listener = new ConnectorRequest.ResultListener<GetUniqueTokenResult>() {
            @Override
            public void onGetResult(GetUniqueTokenResult result) {
                mPairTokenTextView.setText(result.token);
                mToken = result.token;

                startPairingTask();
            }

            @Override
            public void onFail(ConnectorResult result) {
                // TODO 결과 코드를 갖고 예외처리 하자
                mPairTokenTextView.setText("에러...");
            }
        };
        Connector.getUniqueToken(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPairingTask != null) {
            mPairingTask.cancel(true);
            mPairingTask = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startPairingTask();
    }

    private void startPairingTask() {
        if (mToken != null && mPairingTask == null) {
            DownloadRequest request = new DownloadRequest();
            request.context = this;
            request.token = mToken;
            request.listener = new DownloadRequest.ResultListener<DownloadResult>() {
                @Override
                public void onGetResult(DownloadResult result) {
                    NLLog.now("Download succeed.");
                }

                @Override
                public void onFail(ConnectorResult result) {
                    NLLog.now("Download failed.");
                }
            };

            mPairingTask = new PairingTask(request);
            mPairingTask.execute();
        }
    }
}
