package com.yooiistudios.newsflow.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.util.connector.Connector;
import com.yooiistudios.newsflow.core.util.connector.ConnectorRequest;
import com.yooiistudios.newsflow.core.util.connector.GetUniqueTokenRequest;
import com.yooiistudios.newsflow.core.util.connector.GetUniqueTokenResult;

public class PairActivity extends Activity
        implements ConnectorRequest.ResultListener<GetUniqueTokenResult> {
    private TextView mPairTokenTextView;

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
        request.listener = this;
        Connector.getUniqueToken(request);
    }

    @Override
    public void onGetResult(GetUniqueTokenResult result) {
        mPairTokenTextView.setText(result.token);
    }

    @Override
    public void onFail() {
        mPairTokenTextView.setText("에러...");
    }
}
