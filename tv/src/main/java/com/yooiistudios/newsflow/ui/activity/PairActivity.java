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
import com.yooiistudios.newsflow.core.news.NewsFeedUrl;
import com.yooiistudios.newsflow.core.news.NewsTopic;
import com.yooiistudios.newsflow.core.news.RssFetchable;
import com.yooiistudios.newsflow.core.news.util.RssFetchableConverter;
import com.yooiistudios.newsflow.core.util.NLLog;
import com.yooiistudios.newsflow.model.PairingTask;

import java.util.List;

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
        try {
            printResultDebug(result);
            NLLog.now("Download succeed.");
        } catch (RssFetchableConverter.RssFetchableConvertException e) {
            // TODO 받아온 데이터 디코딩중 문제 발생. UI 처리 필요.
            e.printStackTrace();
        }
    }

    private void printResultDebug(DownloadResult result) throws RssFetchableConverter.RssFetchableConvertException {
        mPairTokenTextView.append("\nresult: ");
        List<RssFetchable> fetchables =
                RssFetchableConverter.base64StringToRssFetchables(result.data);
        for (RssFetchable fetchable : fetchables) {
            if (fetchable instanceof NewsTopic) {
                NewsTopic topic = (NewsTopic)fetchable;
                mPairTokenTextView.append("\ntype: NewsTopic");
                mPairTokenTextView.append("\nlanguageCode: " + topic.languageCode);
                mPairTokenTextView.append("\nregionCode: " + topic.regionCode);
                mPairTokenTextView.append("\ncountryCode: " + topic.countryCode);
                mPairTokenTextView.append("\nnewsProviderId: " + topic.newsProviderId);
                mPairTokenTextView.append("\nid: " + topic.id);
            } else if (fetchable instanceof NewsFeedUrl) {
                NewsFeedUrl feedUrl = (NewsFeedUrl)fetchable;
                mPairTokenTextView.append("\ntype: NewsFeedUrl");
                mPairTokenTextView.append("\nurl: " + feedUrl.getUrl());
            }
        }
    }
}
