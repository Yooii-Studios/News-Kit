package com.yooiistudios.newsflow.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.yooiistudios.newsflow.ui.animation.PairTransitionUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
public class PairActivity extends Activity implements PairTransitionUtils.PairTransitionCallback {
    @Getter @InjectView(R.id.pair_container_layout) FrameLayout mContainerLayout;
    @Getter @InjectView(R.id.pair_dialog_layout) LinearLayout mDialogLayout;
    @InjectView(R.id.pair_token1_textview) TextView mToken1TextView;
    @InjectView(R.id.pair_token2_textview) TextView mToken2TextView;
    @InjectView(R.id.pair_token3_textview) TextView mToken3TextView;
    @InjectView(R.id.pair_token4_textview) TextView mToken4TextView;
    @InjectView(R.id.pair_token5_textview) TextView mToken5TextView;

    List<TextView> textViews = new ArrayList<>();
    private PairingTask mPairingTask;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);
        ButterKnife.inject(this);
        PairTransitionUtils.runEnterAnimation(this, this);
        initViews();
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
        textViews.add(mToken1TextView);
        textViews.add(mToken2TextView);
        textViews.add(mToken3TextView);
        textViews.add(mToken4TextView);
        textViews.add(mToken5TextView);
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
//                mPairTokenTextView.setText(result.token);
                mToken = result.token;
                setTokenToTextViews(mToken);
                startPairingTask();
            }

            @Override
            public void onFail(ConnectorResult result) {
                // TODO result.resultCode == ConnectorResult.RC_TOKEN_CREATION_FAILED 일 경우 처리
//                mPairTokenTextView.setText("에러...");
            }
        };
        return request;
    }

    private void setTokenToTextViews(String token) {
        if (token.length() == 5) {
            // 스트링에서 캐릭터를 하나씩 돌면서 대입해줌
            for (int i = 0; i < token.length(); i++) {
                char number = token.charAt(i);
                textViews.get(i).setText(String.valueOf(number));
            }
        } else {
            Toast.makeText(this, R.string.pair_error_msg, Toast.LENGTH_SHORT).show();
            finish();
        }
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
        /*
        try {
            printResultDebug(result);
            NLLog.now("Download succeed.");
        } catch (RssFetchableConverter.RssFetchableConvertException e) {
            // TODO 받아온 데이터 디코딩중 문제 발생. UI 처리 필요.
            e.printStackTrace();
        }
        */
    }

    @Override
    public void onTransitionAnimationEnd() {
        requestToken();
    }

    /*
    // For Test
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
    */
}
