package com.yooiistudios.newskit.tv;

import android.os.AsyncTask;

import com.yooiistudios.newskit.core.connector.ConnectorException;
import com.yooiistudios.newskit.core.connector.ConnectorResult;
import com.yooiistudios.newskit.core.connector.DownloadRequest;

/**
 * Created by Dongheyon Jeong in News Kit from Yooii Studios Co., LTD. on 15. 3. 12.
 *
 * PairingTask
 *  토큰을 가지고 커넥터 서버에 "데이터가 들어와 있나 Active 하게 체크" 하는 서비스
 */
public class PairingTask extends AsyncTask<Void, Void, ConnectorResult> {
    // 2 min
    private static final long REQUEST_THRESHOLD_MILLI = 2 * 60 * 1000;
    // 3 sec for test
//    private static final long REQUEST_THRESHOLD_MILLI = 3 * 1000;

    private DownloadRequest mRequest;
    private long mRequestStartTimeInMilli;

    public PairingTask(DownloadRequest request) {
        mRequest = request;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mRequestStartTimeInMilli = System.currentTimeMillis();
    }

    @Override
    protected ConnectorResult doInBackground(Void... params) {
        ConnectorResult result = null;

        while (shouldTryMore(result)) {
            try {
                Thread.sleep(1000);
                result = mRequest.execute();
            } catch (ConnectorException e) {
                break;
            } catch (InterruptedException ignored) {
            }
        }
        if (result == null) {
            result = ConnectorResult.createErrorObject();
        }
        // THRESHOLD 동안 결과를 못 얻을 경우는 Expire 을 날려줌
        if (isExpired()) {
            result = ConnectorResult.createExpiredObject();
        }
        return result;
    }

    private boolean shouldTryMore(ConnectorResult result) {
        return !isSuccess(result) && !isExpired();
    }

    private boolean isSuccess(ConnectorResult result) {
        return result != null && result.isSuccess();
    }

    private boolean isExpired() {
        long timePast = System.currentTimeMillis() - mRequestStartTimeInMilli;
        return timePast > REQUEST_THRESHOLD_MILLI || isCancelled();
    }

    @Override
    protected void onPostExecute(ConnectorResult result) {
        super.onPostExecute(result);

        if (!isCancelled()) {
            mRequest.handleResult(result);
        }
    }
}
