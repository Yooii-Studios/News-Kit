package com.yooiistudios.newsflow.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class PairActivity extends Activity {
    public static final String INTENT_KEY_TOP_URL = "intent_key_top_url";
    public static final String INTENT_KEY_BOTTOM_URL = "intent_key_bottom_url";
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

    private void initViews() {
        mPairTokenTextView = (TextView)findViewById(R.id.pair_token);
    }

    private void requestToken() {
        GetUniqueTokenRequest request = createGetUniqueTokenRequest();
        Connector.getUniqueToken(request);
    }

    private void startPairingTask() {
        if (mToken != null && mPairingTask == null) {
            DownloadRequest request = createDownloadRequest();

            mPairingTask = new PairingTask(request);
            mPairingTask.execute();
        }
    }

    private GetUniqueTokenRequest createGetUniqueTokenRequest() {
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
                // TODO result.resultCode == ConnectorResult.RC_TOKEN_CREATION_FAILED 일 경우 처리
                mPairTokenTextView.setText("에러...");
            }
        };
        return request;
    }

    private DownloadRequest createDownloadRequest() {
        DownloadRequest request = new DownloadRequest();
        request.context = this;
        request.token = mToken;
        request.listener = new DownloadRequest.ResultListener<DownloadResult>() {
            @Override
            public void onGetResult(DownloadResult result) {
                handleDownloadResult(result);
            }

            @Override
            public void onFail(ConnectorResult result) {
                // TODO 서버와 통신중 문제 발생. UI 처리 필요.
                NLLog.now("Download failed.");
            }
        };
        return request;
    }

    private void handleDownloadResult(DownloadResult result) {
        NLLog.now("Download succeed.");
        mPairTokenTextView.append("\nresult: ");
        try {
            byte[] dataBytes = Base64.decode(result.data, Base64.NO_WRAP);
            JSONArray jsonArray = new JSONArray(new String(dataBytes));
            int jsonArraySize = jsonArray.length();

            if (jsonArraySize > 0) {
                putReceivedDataAndFinish(jsonArray);
            } else {
                // TODO 하나도 받지 않은 상황. 유저에게 알려줘야함.
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void putReceivedDataAndFinish(JSONArray jsonArray) throws JSONException {
        int jsonArraySize = jsonArray.length();
        String topUrl = jsonArray.getString(0);
        ArrayList<String> bottomUrls = new ArrayList<>();
        for (int i = 1; i < jsonArraySize; i++) {
            String url = jsonArray.getString(i);
            bottomUrls.add(url);
        }

        getIntent().putExtra(INTENT_KEY_TOP_URL, topUrl);
        getIntent().putStringArrayListExtra(INTENT_KEY_BOTTOM_URL, bottomUrls);
        setResult(RESULT_OK, getIntent());
        finish();
    }
}
