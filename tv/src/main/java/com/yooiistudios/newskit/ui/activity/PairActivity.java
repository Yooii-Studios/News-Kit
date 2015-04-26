package com.yooiistudios.newskit.ui.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.core.connector.Connector;
import com.yooiistudios.newskit.core.connector.ConnectorRequest;
import com.yooiistudios.newskit.core.connector.ConnectorResult;
import com.yooiistudios.newskit.core.connector.DownloadRequest;
import com.yooiistudios.newskit.core.connector.DownloadResult;
import com.yooiistudios.newskit.core.connector.TokenCreationRequest;
import com.yooiistudios.newskit.core.connector.TokenCreationResult;
import com.yooiistudios.newskit.model.PairingTask;
import com.yooiistudios.newskit.ui.animation.PairTransitionUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;
import lombok.experimental.Accessors;


@Accessors(prefix = "m")
public class PairActivity extends Activity implements PairTransitionUtils.PairTransitionCallback {
    public static final String INTENT_KEY_TOP_URL = "intent_key_top_url";
    public static final String INTENT_KEY_BOTTOM_URL = "intent_key_bottom_url";

    @Getter @InjectView(R.id.pair_container_layout) FrameLayout mContainerLayout;
    @Getter @InjectView(R.id.pair_dialog_layout) LinearLayout mDialogLayout;
    @InjectView(R.id.pair_token1_textview) TextView mToken1TextView;
    @InjectView(R.id.pair_token2_textview) TextView mToken2TextView;
    @InjectView(R.id.pair_token3_textview) TextView mToken3TextView;
    @InjectView(R.id.pair_token4_textview) TextView mToken4TextView;
    @InjectView(R.id.pair_token5_textview) TextView mToken5TextView;

    private List<TextView> textViews = new ArrayList<>();
    private PairingTask mPairingTask;
    private String mToken;
    private boolean mIsBeingPaused = false;

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
        stopPairingTask();
        mIsBeingPaused = true;
        super.onPause();
    }

    private void initViews() {
        textViews.add(mToken1TextView);
        textViews.add(mToken2TextView);
        textViews.add(mToken3TextView);
        textViews.add(mToken4TextView);
        textViews.add(mToken5TextView);
    }

    private void stopPairingTask() {
        if (mPairingTask != null) {
            mPairingTask.cancel(true);
            mPairingTask = null;
        }
    }

    private void startPairingTask() {
        stopPairingTask();

        if (mToken != null && mPairingTask == null) {
            DownloadRequest request = createDownloadRequest();

            mPairingTask = new PairingTask(request);
            mPairingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private TokenCreationRequest createNewTokenRequest() {
        ConnectorRequest.ResultListener<TokenCreationResult> listener =
                new ConnectorRequest.ResultListener<TokenCreationResult>() {
                    @Override
                    public void onSuccess(TokenCreationResult result) {
                        mToken = result.getToken();
                        setTokenToTextViews(mToken);
                        startPairingTask();
                    }

                    @Override
                    public void onFail(ConnectorResult result) {
                        Toast.makeText(PairActivity.this, R.string.pair_error_msg,
                                Toast.LENGTH_SHORT).show();
                    }
                };
        return new TokenCreationRequest(getApplicationContext(), listener);
    }

    private void requestToken() {
        // FIXME: 조금 늦게 뜨는 것을 위해 1초 기다림
        TokenCreationRequest request = createNewTokenRequest();
        Connector.execute(request);
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
        DownloadRequest.ResultListener<DownloadResult> listener =
                new DownloadRequest.ResultListener<DownloadResult>() {
                    @Override
                    public void onSuccess(DownloadResult result) {
                        handleDownloadResult(result);
                    }

                    @Override
                    public void onFail(ConnectorResult result) {
                        if (result.getResultCode() == ConnectorResult.RC_CONNECTOR_EXPIRED) {
                            // 일정 시간이 지나 만료될 경우는 새로 토큰을 요청
                            requestToken();
                        } else {
                            Toast.makeText(PairActivity.this, R.string.pair_error_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };
        return new DownloadRequest(getApplicationContext(), listener, mToken);
    }

    private void handleDownloadResult(DownloadResult result) {
        try {
            byte[] dataBytes = Base64.decode(result.getData(), Base64.NO_WRAP);
            JSONArray jsonArray = new JSONArray(new String(dataBytes));
            int jsonArraySize = jsonArray.length();

            if (jsonArraySize > 0) {
                putReceivedDataAndFinish(jsonArray);
            } else {
                Toast.makeText(PairActivity.this, R.string.pair_error_msg,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTransitionAnimationEnd() {
        // 애니메이션 중간에 나갈 경우에도 레퍼런스가 남아 콜백이 불리는데 이를 막기 위함
        if (!mIsBeingPaused) {
            requestToken();
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
